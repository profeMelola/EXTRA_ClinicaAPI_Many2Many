package es.daw.clinicaapi.service;

import es.daw.clinicaapi.dto.request.invoice.InvoiceIssueRequest;
import es.daw.clinicaapi.dto.request.invoice.InvoiceLineCreateRequest;
import es.daw.clinicaapi.dto.request.invoice.InvoicePayRequest;
import es.daw.clinicaapi.dto.response.invoice.InvoiceResponse;
import es.daw.clinicaapi.entity.Appointment;
import es.daw.clinicaapi.entity.Invoice;
import es.daw.clinicaapi.entity.InvoiceLine;
import es.daw.clinicaapi.entity.MedicalService;
import es.daw.clinicaapi.enums.AppointmentStatus;
import es.daw.clinicaapi.enums.DiscountType;
import es.daw.clinicaapi.enums.InvoiceStatus;
import es.daw.clinicaapi.enums.VatRate;
import es.daw.clinicaapi.mapper.InvoiceMapper;
import es.daw.clinicaapi.exception.BadRequestException;
import es.daw.clinicaapi.exception.BusinessRuleException;
import es.daw.clinicaapi.exception.ConflictException;
import es.daw.clinicaapi.exception.NotFoundException;
import es.daw.clinicaapi.repository.AppointmentRepository;
import es.daw.clinicaapi.repository.InvoiceRepository;
import es.daw.clinicaapi.repository.MedicalServiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final MedicalServiceRepository medicalServiceRepository;

    @Value("${invoice.max-lines}")
    private int invoiceMaxLines;

    @Transactional
    public InvoiceResponse issueInvoiceForAppointment(Long appointmentId, InvoiceIssueRequest req) {

        // 1. No puede haber servicios duplicados en el json request; si no → BadRequestException (400)
        checkDuplicateServices(req.lines());

        // 1.1 Máximo 10 líneas por factura
        // Para pruebas por ahora no pongo 10 porque no dispongo de este set de datos...
        if (req.lines().size() > invoiceMaxLines) {
            throw new BusinessRuleException("Invoice cannot have more than " + invoiceMaxLines + " lines. Requested: " + req.lines().size());
        }

        // 2. La cita debe existir; si no → NotFoundException (404).
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found with id: "+appointmentId));

        // 3. La cita no puede estar ya facturada; si no → ConflictException (409).
        // solo refleja lo que hay en ese objeto en memoria (y cómo se haya cargado).
        // No es una garantía de unicidad en BD.
        // Si invoice es LAZY, el campo puede no estar cargado. Revisad la entidad Appointment.
        if (appointment.getInvoice() != null || invoiceRepository.existsByAppointmentId(appointmentId)) {
            throw new ConflictException("Appointment already has an invoice");
        }

        // 4 La cita no puede estar CANCELLED. si no → BusinessRuleException (422).
        if (appointment.getStatus() == AppointmentStatus.CANCELLED)
            throw new BusinessRuleException("Cannot issue invoice for cancelled appointment");

        // 5 La cita debe estar COMPLETED si no → BussinessRuleException (422).
        if (appointment.getStatus() != AppointmentStatus.COMPLETED)
            throw new BusinessRuleException("Cannot issue invoice for non-completed appointment");

        // 6. Cada medicalServiceId debe existir y estar active=true; si no: inexistente → 404 / inactivo → 422
        //validateMedicalServices(req.lines());

        // Construir la factura y sus líneas a partir de la cita y los servicios indicados en el request.
        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatus.PENDING); //acorde a la captura del enunciado
        invoice.setIssuedAt(LocalDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;

        for (InvoiceLineCreateRequest lineReq : req.lines()) {

            // 6. Cada medicalServiceId debe existir y estar active=true; si no: inexistente → 404 / inactivo → 422
            MedicalService service = medicalServiceRepository.findById(lineReq.medicalServiceId())
                    .orElseThrow(() -> new NotFoundException("Servicio médico inexistente"));

            if (!service.isActive()) {
                throw new BusinessRuleException("Medical service is not active: " + lineReq.medicalServiceId());
            }

            InvoiceLine line = new InvoiceLine();
            line.setService(service);
            line.setQuantity(lineReq.qty());

            // Las líneas de factura se emitirán sin aplicar ningún tipo de descuento.
            line.setDiscountType(DiscountType.NONE);
            line.setDiscountValue(BigDecimal.ZERO);

            // El tipo de IVA aplicado en las líneas de factura será siempre del 21% (VAT_21).
            line.setVatRate(VatRate.VAT_21);

            // unitPrice viene del precio base del servicio médico
            BigDecimal unitPrice = service.getBasePrice();
            line.setUnitPrice(unitPrice);

            BigDecimal qty = BigDecimal.valueOf(lineReq.qty());
            BigDecimal base = unitPrice.multiply(qty); // contiene el precio total sin descuento ni impuestos
            BigDecimal tax = base.multiply(VatRate.VAT_21.getRate()); // contiene el importe de impuestos sin descuento

            // ¿Qué diferencia hay entre redondear por línea vs redondear al final?
            // Que puede cambiar el total por acumulación de decimales (y depende de normativa/criterio de negocio).
            BigDecimal lineTotal = base.add(tax).setScale(2, RoundingMode.HALF_UP); // 125.1234 → 125.12; 125.1267 → 125.13
            //BigDecimal lineTotal = base.add(tax);
            line.setLineTotal(lineTotal);

            invoice.addLine(line); // mantener bidireccionalidad. Si no hacemos esto, el mappedBy puede quedar incoherente

            subtotal = subtotal.add(base); //sin iva
            taxTotal = taxTotal.add(tax); // con iva
        }

        // redondeo una vez que he hecho el sumatorio del precio sin iva (subtotal) y del importe de impuestos (taxTotal),
        // para evitar la acumulación de decimales que puede ocurrir al redondear por línea.
        // De esta forma, el total de la factura reflejará exactamente la suma del subtotal y el taxTotal,
        // sin desviaciones por redondeos intermedios.
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        taxTotal = taxTotal.setScale(2, RoundingMode.HALF_UP);

        invoice.setSubtotal(subtotal);
        invoice.setTaxTotal(taxTotal);

        invoice.setTotal(subtotal.add(taxTotal).setScale(2, RoundingMode.HALF_UP));

        // lo dejamos para el final, porque invoice es el dueño de la relación,
        // y al hacer invoice.setAppointment() se hace el setInvoice() en el otro lado de la relación,
        // y si appointment.getInvoice() ya tenía un valor
        // (ej: por un error de lógica o por cómo se haya cargado la cita), entonces se perdería esa referencia y se quedaría con null, lo que podría causar problemas en las validaciones posteriores (ej: checkDuplicateServices) o en la lógica de negocio (ej: al cargar la cita y querer acceder a su factura). Al hacer invoice.setAppointment() al final, nos aseguramos de que no haya referencias previas que puedan interferir con la lógica de negocio o las validaciones.
        invoice.setAppointment(appointment);
        appointment.setInvoice(invoice); // mantener la consistencia de la relación bidireccional

        // Guardar la factura (con cascada, se guardan las líneas también)
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Mapear y retornar la respuesta
        return InvoiceMapper.toResponse(savedInvoice);

    }

    // método encargado de facturar...
    /*
        La factura debe existir:
            Si no existe: 404 NOT FOUND.
        Solo se puede pagar si está en estado PENDING:
            Si está en cualquier otro estado (PAID, CANCELLED, etc.): 409 CONFLICT.
            "Invoice cannot be paid from status: X"
        Al pagar:
            status = PAID
            paidAt = now()
            paymentMethod = req.paymentMethod()

        La operación debe ser transaccional (@Transactional).
     */
    @Transactional
    public InvoiceResponse payInvoice(Long invoiceId, InvoicePayRequest request){

        // La factura debe existir
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found with id: "+invoiceId));

        // Solo se puede pagar si está en estado PENDING
        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            throw new ConflictException("Invoice cannot be paid from status: "+invoice.getStatus());
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setPaymentMethod(request.paymentMethod());

        // recibo un entity con la fila actualizada
        // No hace falta hacer save pero necesito el entity actualizado para el dto del response...
        Invoice invoiceUpdated = invoiceRepository.save(invoice);

        // convierto el entity a un dto
        InvoiceResponse invoiceResponse = InvoiceMapper.toResponse(invoiceUpdated);
        //InvoiceResponse invoiceResponse = InvoiceMapper.toResponse(invoice);

        return InvoiceMapper.toResponse(invoiceUpdated);
        //return invoiceResponse;

    }

    // ---------- MÉTODO PROPIOS DE UTILIDADES -------
    /**
     * Comprueba si existen servicios duplicados
     * @param lines
     */
    private void checkDuplicateServices(List<InvoiceLineCreateRequest> lines) {
        Set<Long> ids = new HashSet<>();
        Set<Long> duplicates = new HashSet<>();
        //boolean repetidos = false;

        for (InvoiceLineCreateRequest line : lines) {
            if(!ids.add(line.medicalServiceId())) {
                duplicates.add(line.medicalServiceId());
                //throw new BadRequestException("Duplicate medical service id in lines: "+line.medicalServiceId());
                //repetidos = true;
            }
        }

        if (!duplicates.isEmpty()) {
            throw new BadRequestException("Duplicate medical service id in lines: "+duplicates);
        }

    }
    // Método que finalmente no uso porque lo integro en el servicio de creación de la factura, pero lo dejo aquí para que veáis cómo se haría la validación de cada servicio y el lanzamiento de las excepciones correspondientes.
//    private void validateMedicalServices(List<InvoiceLineCreateRequest> lines) {
//        for (InvoiceLineCreateRequest line : lines) {
//            Long serviceId = line.medicalServiceId();
//            MedicalService service = medicalServiceRepository.findById(serviceId)
//                    .orElseThrow(() -> new NotFoundException("Medical service not found with id: " + serviceId));
//
//            if (!service.isActive()) {
//                throw new BusinessRuleException("Medical service is not active: " + serviceId);
//            }
//        }
//    }

}
