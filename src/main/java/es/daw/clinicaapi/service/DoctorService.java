package es.daw.clinicaapi.service;

import es.daw.clinicaapi.entity.Doctor;
import es.daw.clinicaapi.exception.BusinessRuleException;
import es.daw.clinicaapi.exception.NotFoundException;
import es.daw.clinicaapi.repository.AppointmentRepository;
import es.daw.clinicaapi.repository.DoctorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    // POR QUÉ TRANSACTIONAL????
//    Cuando borras un doctor, Hibernate necesita:
//    Borrar primero las filas hijas en doctor_specialties (por orphanRemoval + CascadeType.ALL)
//    Borrar después la fila en doctors
//    Esas dos operaciones deben ocurrir en la misma transacción.
    @Transactional
    public void deleteDoctor(Long doctorId){
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: "+doctorId));

        // Para los doctores que tienen citas y encima las citas están facturadas...
        // decidimos que no se puede eliminar
        if (appointmentRepository.existsByDoctorId(doctorId)){
            throw new BusinessRuleException("El doctor tiene citas asociadas. Imposible borrar!!!");
        }

        doctorRepository.delete(doctor);

    }
}
