package es.daw.clinicaapi.controller;

import es.daw.clinicaapi.dto.request.invoice.InvoicePayRequest;
import es.daw.clinicaapi.dto.response.invoice.InvoiceResponse;
import es.daw.clinicaapi.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','BILLING')")
    public ResponseEntity<InvoiceResponse> payInvoice(
            @PathVariable("id") Long invoiceId,
            @RequestBody @Valid InvoicePayRequest request
    ) {
        return ResponseEntity.ok(invoiceService.payInvoice(invoiceId, request));
    }

}
