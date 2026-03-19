package es.daw.clinicaapi.dto.report;

import java.math.BigDecimal;

public record ServiceSummaryReport(
        Long serviceId,
        String serviceName,
        Long invoiceLinesCount,
        Long totalQuantity,
        BigDecimal totalBilledAmount
) {}