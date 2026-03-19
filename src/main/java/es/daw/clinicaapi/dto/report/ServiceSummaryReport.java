package es.daw.clinicaapi.dto.report;

import java.math.BigDecimal;

/*
Un record en Java es una clase inmutable y compacta pensada para transportar datos,
donde el compilador genera automáticamente constructor, getters, equals, hashCode y toString.
 */
/*
1. En un record, Java genera automáticamente un constructor “canónico” con todos los campos.
2. Cuando escribes public ServiceSummaryReport { ... }, estás interceptando ese constructor
para añadir lógica de validación/normalización.
3. Las variables totalQuantity y totalBilledAmount dentro del bloque son los parámetros de entrada del constructor.
4. Si llegan null, tú las sustituyes por valores por defecto (0L y BigDecimal.ZERO).
5. Al terminar el bloque, Java asigna esos valores (ya corregidos) a los componentes finales del record.
 */

public record ServiceSummaryReport(
        Long serviceId,
        String serviceName,
        Long invoiceLinesCount,
        Long totalQuantity,
        BigDecimal totalBilledAmount
) {
    public ServiceSummaryReport{
        if (totalQuantity == null)  totalQuantity = 0L;
        if (totalBilledAmount == null)  totalBilledAmount = BigDecimal.ZERO;
    }
}