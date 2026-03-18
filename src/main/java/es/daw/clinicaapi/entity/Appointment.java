package es.daw.clinicaapi.entity;

import es.daw.clinicaapi.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
//@Table(name = "appointments", indexes = {
//        @Index(name="idx_appointment_doctor_start", columnList = "doctor_id,startAt"),
//        @Index(name="idx_appointment_patient_start", columnList = "patient_id,startAt")
//})
@Table(name="appointments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Appointment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="patient_id",nullable = false)
    private Patient patient;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="doctor_id",nullable = false)
    private Doctor doctor;

    @Column(nullable=false)
    private LocalDateTime startAt;

    @Column(nullable=false)
    private LocalDateTime endAt;

    @Column(nullable=false)
    private int minutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private AppointmentStatus status;

    @Column(length=250)
    private String reason;

    @Column(length=250)
    private String cancellationReason;

    //@OneToOne(mappedBy = "appointment", fetch = FetchType.LAZY)
    @OneToOne(mappedBy = "appointment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Invoice invoice;
}

