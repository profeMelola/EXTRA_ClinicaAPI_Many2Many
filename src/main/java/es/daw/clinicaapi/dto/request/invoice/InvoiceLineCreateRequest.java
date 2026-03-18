package es.daw.clinicaapi.dto.request.invoice;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InvoiceLineCreateRequest(
    Long medicalServiceId,
    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 20, message = "Quantity must not exceed 20")
    Integer qty
    //@Positive(message = "La cantidad debe ser mayor a 0") Integer qty

) {}

// @Min : solo para tipos primitivos (int, long, double, etc) y sus wrappers (Integer, Long, Double, etc), no funciona con BigDecimal; para eso se usaría @DecimalMin

