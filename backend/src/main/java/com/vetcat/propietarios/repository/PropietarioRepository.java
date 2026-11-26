package com.vetcat.propietarios.repository;

import com.vetcat.propietarios.entity.Propietario;
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
}
