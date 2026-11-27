package com.vetcat.propietarios.repository;

import com.vetcat.propietarios.entity.Propietario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Propietarios de la clínica activa.
 *
 * <p>El teléfono se normaliza en SQL con la misma expresión que la columna generada de {@code
 * tenant/V1}, para que Java y la base de datos no puedan discrepar.
 */
public interface PropietarioRepository extends JpaRepository<Propietario, Long> {

  /**
   * Serializa las altas de un mismo teléfono hasta el fin de la transacción. La clave incluye el
   * esquema porque los candados consultivos son de toda la base de datos.
   */
  @Query(
      value =
          """
          SELECT 1 FROM (SELECT pg_advisory_xact_lock(hashtextextended(
              current_schema() || ':' || regexp_replace(:telefono, '\\D', '', 'g'), 0))) AS candado
          """,
      nativeQuery = true)
  Integer bloquearTelefono(@Param("telefono") String telefono);

  @Query(
      value =
          """
          SELECT EXISTS (SELECT 1 FROM propietario
              WHERE telefono_normalizado = regexp_replace(:telefono, '\\D', '', 'g'))
          """,
      nativeQuery = true)
  boolean existeConTelefono(@Param("telefono") String telefono);

  /**
   * Indica si el teléfono es nuevo para ese propietario y ya lo tiene otro. Conservar el propio no
   * cuenta, aunque se comparta con otra ficha por un duplicado confirmado.
   */
  @Query(
      value =
          """
          SELECT EXISTS (SELECT 1 FROM propietario otro, propietario propio
              WHERE propio.id = :id AND otro.id <> propio.id
              AND otro.telefono_normalizado = regexp_replace(:telefono, '\\D', '', 'g')
              AND otro.telefono_normalizado <> propio.telefono_normalizado)
          """,
      nativeQuery = true)
  boolean cambiaAlTelefonoDeOtro(@Param("id") long id, @Param("telefono") String telefono);

  /**
   * Id del propietario si está activo, con un candado compartido hasta el fin de la transacción:
   * una baja en curso hace esperar y una posterior espera a esta transacción.
   */
  @Query(
      value = "SELECT id FROM propietario WHERE id = :id AND activo FOR SHARE",
      nativeQuery = true)
  Optional<Long> compartirActivo(@Param("id") long id);
}
