package es.daw.clinicaapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.LazyInitializationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "doctors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Doctor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=40)
    private String licenseNumber;

    @Column(nullable=false, length=120)
    private String fullName;

    @Column(nullable=false, length=150)
    private String email;

    @Column(nullable=false)
    private boolean active = true;

    //@OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @OneToMany(mappedBy = "doctor", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Appointment> appointments = new ArrayList<>();

    public void addAppointment(Appointment a) {
        appointments.add(a);
        a.setDoctor(this);
    }


    // Caso 1: Decidimos que SÍ hay que borrar un doctor con especilidad asignada
    //@OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)

    // Caso 2: Al borrar un doctor con especialidad asignada da error: Violación de una restricción de Integridad Referencial: FK_DS_DOCTOR
    //@OneToMany(mappedBy = "doctor", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    //private List<DoctorSpecialty> specialties = new ArrayList<>();

//      --------------- CAMBIAMOS A SET Y PONEMOS ORPHANREMOVAL ----------
//    Si mantienes List, cuidado con duplicados y con índices.
//    Usar Set suele ser más natural para colecciones de asociación donde no deseas duplicados y no te importa el orden de inserción.

//    CascadeType.REMOVE: elimina dependientes durante la operación de eliminación del padre.
//    orphanRemoval = true: elimina dependientes cuando se les quita de la colección del padre
//    (por ejemplo, doctor.getSpecialties().remove(ds)) aunque no estés borrando el padre.
//    Si quieres que eliminar una asociación de la colección provoque el borrado en la BD, usa orphanRemoval = true.
//    Ejemplo típico cuando la entidad dependiente no tiene sentido sin el padre: cascade = ALL + orphanRemoval = true.
//    Asegúrate de que DoctorSpecialty.equals/hashCode se base en la clave (por ejemplo, DoctorSpecialtyId)
//    y no en relaciones con lazy-loading que puedan causar LazyInitializationException en toString/equals si se usan fuera del contexto.

    // One2Many es Lazy por defecto. cuando cargas un Doctor, el Set<DoctorSpecialty> no se carga hasta que accedes a él explícitamente.
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DoctorSpecialty> specialties = new HashSet<>();

    // ------- MÉTODOS HELPER -----------------
    public void addSpecialty(DoctorSpecialty s) {
        if (s == null) return;
        if (specialties.add(s)) { // add devuelve true si se añade
            s.setDoctor(this);
        }
    }

    public void removeSpecialty(DoctorSpecialty s) {
        if (s == null) return;
        if (specialties.remove(s)) {
            s.setDoctor(null); // ????????????????
        }
    }

    // --------------------------- ANTIGUO. VERSIÓN ONE2MANY - MANY2ONE ------------------------------
    //  versión del examen sin tabla intermedia....

    // -------- ASOCIACIÓN ENTRE DOCTOR Y ESPECIALIDADES ------------
    // Un doctor puede tener varias especialidades
    // Un doctor NO puede repetir la misma especialidad
    // Distintos doctores pueden compartir especialidad
    // 1:N desde doctor a especilidad

//    En términos JPA:
//    Doctor → lado inverso (no propietario). Es la que tiene mappedBy
//    DoctorSpecialty → lado propietario (tiene la FK)

//    CascadeType.ALL Propaga operaciones del Doctor a sus DoctorSpecialty.
//    Si haces: entityManager.persist(doctor); En Spring no usamos entityManager. Hacemos save directamente en el repo
//    También se persisten sus especialidades.
//    orphanRemoval = true Si una especialidad deja de estar asociada al doctor, se elimina de la base de datos.

//    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<DoctorSpecialty> specialties = new HashSet<>();

    // HELPERS
//    En una relación bidireccional JPA, el lado propietario es el que tiene la FK
//    y es el único que realmente actualiza la base de datos.

//    Los métodos helper existen para mantener sincronizado el grafo de objetos en memoria.
//    En una relación bidireccional:
//    JPA NO sincroniza automáticamente ambos lados en memoria.
//    Si solo haces: specialties.add(ds);
//    El objeto DoctorSpecialty no sabe que pertenece a ese doctor.
//    El estado del grafo de objetos queda inconsistente.
//
//    Y como el lado propietario es DoctorSpecialty,
//    si no haces ds.setDoctor(this) → la FK no se actualiza en BD.
//    public void addSpecialty(DoctorSpecialty s) {
//        specialties.add(s);
//        s.setDoctor(this);
//    }
//
//    public void removeSpecialty(DoctorSpecialty s) {
//        specialties.remove(s);
//        s.setDoctor(null);
//    }


}
