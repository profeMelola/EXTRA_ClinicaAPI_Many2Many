package es.daw.clinicaapi.repository;

import es.daw.clinicaapi.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Nuevo método para saber si existe un doctor con citas
    boolean existsByDoctor_Id(Long doctorId);

    boolean existsByDoctorId(Long doctorId);

}
