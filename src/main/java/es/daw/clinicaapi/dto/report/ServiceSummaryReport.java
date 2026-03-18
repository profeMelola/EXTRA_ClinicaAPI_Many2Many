package es.daw.clinicaapi.dto.report;

import java.math.BigDecimal;

public record ServiceSummaryReport(
        Long serviceId,
        String serviceName,
        long invoiceLinesCount,
        long totalQuantity,
        BigDecimal totalBilledAmount
) {}