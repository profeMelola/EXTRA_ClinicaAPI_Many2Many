package es.daw.clinicaapi.repository;

import es.daw.clinicaapi.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    boolean existsByAppointmentId(Long appointmentId);
}

