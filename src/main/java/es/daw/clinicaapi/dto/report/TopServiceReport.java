package es.daw.clinicaapi.dto.report;


import java.math.BigDecimal;

public record TopServiceReport(
    Long serviceId,
    String serviceName,
    long linesCount,
    long unitsSold,
    BigDecimal totalBilled
) {}
