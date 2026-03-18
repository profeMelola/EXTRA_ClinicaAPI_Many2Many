package es.daw.clinicaapi.repository;

import es.daw.clinicaapi.dto.report.TopServiceReport;
import es.daw.clinicaapi.entity.InvoiceLine;
import es.daw.clinicaapi.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, Long> {

    @Query("""
        select new es.daw.clinicaapi.dto.report.TopServiceReport(
            s.id,
            s.name,
            count(l),
            sum(l.quantity),
            sum(l.lineTotal)
            )
                from InvoiceLine l
                    join l.invoice i
                    join l.service s
                where i.issuedAt is not null
                and i.issuedAt >= :from and i.issuedAt <= :to
                and (:status is null or i.status = :status)
            group by s.id, s.name
            order by sum(l.lineTotal) desc
                
    """)
    List<TopServiceReport> topServicesByIssuedAt(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") InvoiceStatus status,
            Pageable pageable
    );

    /* countQuery:
    Tienes GROUP BY
    Estás proyectando a DTO
    El total de páginas depende del número de servicios distintos
    La generación automática puede fallar o ser incorrecta
    En queries agregadas complejas, definir countQuery explícitamente es buena práctica profesional.
     */
    @Query(value = """
        select new es.daw.clinicaapi.dto.report.TopServiceReport(
            s.id,
            s.name,
            count(l),
            sum(l.quantity),
            sum(l.lineTotal)
        )
        from InvoiceLine l
        join l.service s
        join l.invoice i
        where i.issuedAt is not null
          and i.issuedAt >= :from and i.issuedAt <= :to
          and (:status is null or i.status = :status)
        group by s.id, s.name
        order by sum(l.lineTotal) desc
    """,
            countQuery = """
        select count(distinct s.id)
        from InvoiceLine l
        join l.service s
        join l.invoice i
        where i.issuedAt is not null
          and i.issuedAt >= :from and i.issuedAt <= :to
          and (:status is null or i.status = :status)
    """)
    Page<TopServiceReport> topServicesByIssuedAtWithPage(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") InvoiceStatus status,
            Pageable pageable
    );


}