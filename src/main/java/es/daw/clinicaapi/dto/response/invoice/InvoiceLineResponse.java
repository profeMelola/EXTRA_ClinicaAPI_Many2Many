package es.daw.clinicaapi.dto.response.invoice;

import java.math.BigDecimal;

public record InvoiceLineResponse(
    Long id,
    Long medicalServiceId,
    String medicalServiceName,
    Integer qty,
    BigDecimal unitPrice,
    String vatRate,
    BigDecimal lineTotal
) {}

