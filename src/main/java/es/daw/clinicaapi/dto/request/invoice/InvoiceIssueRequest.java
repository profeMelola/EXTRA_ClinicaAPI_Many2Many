package es.daw.clinicaapi.dto.request.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record InvoiceIssueRequest(
    @Valid @NotEmpty List<InvoiceLineCreateRequest> lines
) {}

