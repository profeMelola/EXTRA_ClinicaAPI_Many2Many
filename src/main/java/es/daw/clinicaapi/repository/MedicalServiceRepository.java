package es.daw.clinicaapi.repository;

import es.daw.clinicaapi.dto.report.ServiceSummaryReport;
import es.daw.clinicaapi.dto.report.TopServiceReport;
import es.daw.clinicaapi.entity.MedicalService;
import es.daw.clinicaapi.enums.InvoiceStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicalServiceRepository extends JpaRepository<MedicalService, Long> {

    //MedicalServiceRepository gestiona consultas cuyo agregado raíz es MedicalService.
    // Técnicamente podrías poner el método con el JPQL en cualquier repositorio.
    // Hibernate lo ejecuta igualmente, pero no es la organización adecuada.
    // Sí podríamos crear un repositorio Custom de reporting para no mezclar CRUD con informes complejos

    // PROBLEMAS ENCONTRADOS
    // 1. .NullPointerException: Cannot invoke "java.lang.Number.longValue()"
    // Los sum() devuelven null para servicios sin líneas facturadas, y los tipos primitivos long del record no admiten null.
//    2. COALESCE: devuelve el primer valor no nulo de una lista de expresiones. Si todos los valores son nulos, devuelve null.
//    Aplica coalesce a los tres agregados:
//            coalesce(sum(l.quantity), 0L),
//            coalesce(CAST(sum(l.lineTotal) AS bigdecimal), 0)

    @Query("""
        select new es.daw.clinicaapi.dto.report.ServiceSummaryReport(
            s.id,
            s.name,
            count(l),
            sum(l.quantity),
            sum(l.lineTotal) 
            )
                from MedicalService s
                left join s.lines l
                on l.invoice.issuedAt is not null
                and l.invoice.issuedAt >= :from and l.invoice.issuedAt <= :to
                and (:status is null or l.invoice.status = :status)
            group by s.id, s.name
            order by s.name desc
                
    """)
    List<ServiceSummaryReport> findServicesSummary(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") InvoiceStatus status
    );


}

