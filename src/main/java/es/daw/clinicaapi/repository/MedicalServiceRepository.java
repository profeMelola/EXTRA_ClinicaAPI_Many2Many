package es.daw.clinicaapi.repository;

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


    @Query("""
        select new es.daw.clinicaapi.dto.report.ServiceSummaryReport(
            s.id,
            s.name,
            count(l),
            sum(l.quantity),
            sum(l.lineTotal)
            )
                from MedicalService s
                    join s.lines l
                where l.invoice.issuedAt is not null
                and l.invoice.issuedAt >= :from and l.invoice.issuedAt <= :to
                and (:status is null or l.invoice.status = :status)
            group by s.id, s.name
            order by s.name desc
                
    """)
    List<TopServiceReport> findServicesSummary(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") InvoiceStatus status
    );


}

