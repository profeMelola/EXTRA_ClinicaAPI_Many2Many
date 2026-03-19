package es.daw.clinicaapi.controller;

import es.daw.clinicaapi.dto.report.ServiceSummaryReport;
import es.daw.clinicaapi.dto.report.TopServiceReport;
import es.daw.clinicaapi.enums.InvoiceStatus;
import es.daw.clinicaapi.exception.BadRequestException;
import es.daw.clinicaapi.service.ReportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/top-services")
    @PreAuthorize("hasRole('BILLING')")
    // @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // Fecha incorrecta lanzaría MethodArgumentTypeMismatchException
    // Sin la T falla: fecha correcta 2026-05-01T10:00:00

    // Indicar explícitamente a Spring cómo parsear el texto del request a LocalDateTime
    // Spring ya intenta usar ISO-8601 para LocalDateTime
    // pero es buena práctica indicarlo explícitamente porque evita ambigüedades

    public ResponseEntity<List<TopServiceReport>> topServices(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false)InvoiceStatus status,
            @PageableDefault(page=0, size = 3) Pageable pageable
            ) {

        // MEJORA: si no mando un parámetro obligatorio salta esta excepción:
        // [org.springframework.web.bind.MissingServletRequestParameterException:
        // Required request parameter 'to' for method parameter type LocalDateTime is not present]
//        {
//            "message": "falta el parámetro xxxxxx"
//        }


        // MEJORA: estado incorrecto
        // MethodArgumentTypeMismatchException: Method parameter 'status':
        // Failed to convert value of type 'java.lang.String' to required
        // type 'es.daw.clinicaapi.enums.InvoiceStatus';

        List<TopServiceReport> result = reportService.getTopServices(from, to, status, pageable);

        return ResponseEntity.ok(result);

    }

    @GetMapping("/top-services-page")
    @PreAuthorize("hasRole('BILLING')")
    public ResponseEntity<Page<TopServiceReport>> topServicesPage(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) InvoiceStatus status,
            @PageableDefault(page = 0, size = 3) Pageable pageable
    ) {
        Page<TopServiceReport> result =
                reportService.getTopServicesPage(from, to, status, pageable);

        return ResponseEntity.ok(result);
    }

    // ------------------------------------------

    // ENDPOINT PÚBLICO!!!!
    @GetMapping("/top-services-status-string")
    // Otra solución para valor de status incorrecto... no se ajusta a nuestra forma de trabajar en clase
    public ResponseEntity<List<TopServiceReport>> topServices2(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String status,
            @PageableDefault(page=0, size = 3) Pageable pageable
    ) {
        // Si no se proporciona status -> consultar sin filtro de estado
        if (status == null || status.isBlank()) {
            List<TopServiceReport> result = reportService.getTopServices(from, to, null, pageable);
            return ResponseEntity.ok(result);
        }

        // Si se proporciona, intentar convertir a InvoiceStatus y manejar error
        try {
            InvoiceStatus statusEnum = InvoiceStatus.valueOf(status.trim().toUpperCase());
            List<TopServiceReport> result = reportService.getTopServices(from, to, statusEnum, pageable);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }

    }


    // ---------- Montando Pageable manualmente para validar los parámetros de paginación ----------
    // NOOOOOOOOOOOOOOOOOOO PARA EL EXAMEN
    @GetMapping("/top-services-pageable-manual")
    @PreAuthorize("hasRole('BILLING')")
    public ResponseEntity<List<TopServiceReport>> topServices3(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "3") @Min(1) @Max(5) int size
    ) {
        // Montamos a mano Pageable
        Pageable pageable = PageRequest.of(page, size);
        List<TopServiceReport> result = reportService.getTopServices(from, to, status, pageable);
        return ResponseEntity.ok(result);
    }


    // ----------- RETO 2: https://github.com/profeMelola/DWES-REFUERZO-EXTRAORDINARIA/blob/main/SPRING/ordinaria/api.md#reto-2-resumen-de-servicios-m%C3%A9dicos-facturados-incluyendo-no-facturados
    @GetMapping("/services-summary")
    @PreAuthorize("hasRole('BILLING')")
    public ResponseEntity<List<ServiceSummaryReport>> servicesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) InvoiceStatus status){

        // PENDIENTE!!! MEJORA!!! USAR EXPRESIÓN REGULAR QUE CONTENGA : DRAFT, ISSUED, PAID, CANCELLED, PENDING
        // Validar vía expresión regular que status contenga uno de esos valores. Si no es así... lanzará
        // excepción MethodArgumentTypeMismatchException... o similar...
        // @Pattern("xxxxxxxx") @RequestParam(required = false) InvoiceStatus status
        // pte servicio..
        List<ServiceSummaryReport> result = reportService.getServiceSummary(from,to,status);
        return ResponseEntity.ok(result);

    }


}
