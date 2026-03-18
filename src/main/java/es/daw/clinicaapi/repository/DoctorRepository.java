package es.daw.clinicaapi.repository;

import es.daw.clinicaapi.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
