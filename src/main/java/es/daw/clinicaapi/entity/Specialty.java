package es.daw.clinicaapi.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "specialties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Specialty {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)  // ← schema: VARCHAR(30)
    private String code;

    @Column(nullable = false, length = 80)                 // ← schema: VARCHAR(80), no 120
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    // Lado inverso de la relación N:M
    // mappedBy apunta al campo "specialty" de DoctorSpecialty
    // Sin CascadeType.ALL: no queremos borrar DoctorSpecialty en cascada desde Specialty
    @OneToMany(mappedBy = "specialty", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<DoctorSpecialty> doctorSpecialties = new HashSet<>();
}

