package es.daw.clinicaapi.dto.response.invoice;

import es.daw.clinicaapi.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceResponse(
    Long id,
    Long appointmentId,
    String status,
    BigDecimal subtotal,
    BigDecimal taxTotal,
    BigDecimal total,
    LocalDateTime issuedAt,
    LocalDateTime paidAt,
    String paymentMethod,
    List<InvoiceLineResponse> lines
) {}

