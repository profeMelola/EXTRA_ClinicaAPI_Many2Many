package es.daw.clinicaapi.dto.request.invoice;

import es.daw.clinicaapi.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record InvoicePayRequest(
        @NotNull PaymentMethod paymentMethod
) {}