package es.daw.clinicaapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;


/*
Buenas prácticas y recomendaciones rápidas
Añade serialVersionUID explícito para clases Serializable:
private static final long serialVersionUID = 1L; Evita problemas con serialización/IDE warnings.
equals()/hashCode(): deben basarse únicamente en los atributos que definen la clave (aquí doctorId y specialtyId). Lombok @EqualsAndHashCode hace esto por defecto; si quieres control explícito, usa @EqualsAndHashCode(onlyExplicitlyIncluded = true) y marca cada campo con @EqualsAndHashCode.Include.
Constructor sin-args: JPA lo exige. Ya tienes @NoArgsConstructor.
Tipos wrapper (Long) vs primitivos (long): usar wrappers es correcto (permiten null).
Inmutabilidad: idealmente las claves compuestas son inmutables. Considera quitar setters o no exponerlos si tu diseño lo permite. (Lombok @Getter sin @Setter o @Setter(AccessLevel.NONE)).
toString(): útil en debugging; evita incluir datos sensibles. En claves compuestas es seguro incluir los ids.
Evita incluir colecciones o relaciones perezosas dentro de toString() (riesgo de LazyInitializationException o logs enormes). No aplica para esta clase simple.
 */
@Embeddable
@Getter
//@Setter - no setters porque la clase tiene que ser inmutable... una vez que se inicializa no se modifica!!!!!

@NoArgsConstructor //constructor vacío
@AllArgsConstructor // constructor con todos los atributos
// siempre que uses @Embeddable como clave primaria
// JPA: Las clases que representen claves primarias compuestas deben implementar Serializable.
//@EqualsAndHashCode //si obligatorio sobreescribirlo. Deben basarse únicamente en los atributos que definen la clave
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// @ToString - no obligatorio sobreescribirlo
public class DoctorSpecialtyId implements Serializable {

    /* serialVersionUID
serialVersionUID es un identificador de versión para la serialización Java (java.io.Serializable).
Es un número (long) que se asocia a la “forma” de la clase.
Cuando un objeto se serializa (se convierte a bytes) se graba la clase y su serialVersionUID.
Al deserializar, la JVM compara el serialVersionUID de los bytes con el de la clase cargada:
si difieren, lanza InvalidClassException porque considera que la clase ha cambiado y puede ser incompatible.
Si no lo declaras, el compilador/JVM calcula uno automáticamente a partir de la estructura de la clase (nombres de campos, métodos, firmas, etc.). Ese cálculo no es estable entre compiladores/versiones y cambia con pequeñas modificaciones (incluso algunas que son compatibles).
Declararlo evita sorpresas: mantienes un identificador estable aunque hagas cambios no incompatibles (p. ej. añadir un método).
Si la clase se usa para serialización real (persistencia binaria, colas, caches, sessions distribuidas, etc.) es crítico controlar la versión.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    @Column(name="doctor_id")
    private Long doctorId;

    @EqualsAndHashCode.Include
    @Column(name="specialty_id")
    private Long specialtyId;
//    @Enumerated(EnumType.STRING)
//    @Column(length = 30)
//    private Specialty specialty;


}
