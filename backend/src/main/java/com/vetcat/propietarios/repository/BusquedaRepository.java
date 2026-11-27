package com.vetcat.propietarios.repository;

import com.vetcat.propietarios.entity.Propietario;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Consultas de búsqueda sobre los propietarios de la clínica activa (PRO-009). */
public interface BusquedaRepository extends Repository<Propietario, Long> {

  /**
   * Coincidencia parcial sin acentos ni mayúsculas. Se usa {@code strpos} y no {@code LIKE} para
   * que {@code %} y {@code _} sean texto literal sin necesidad de escaparlos.
   */
  String COINCIDE =
      """
      FROM propietario
      WHERE (:incluirInactivos OR activo)
        AND (strpos(ext.unaccent(lower(nombre)), ext.unaccent(lower(:texto))) > 0
          OR strpos(ext.unaccent(lower(coalesce(correo, ''))), ext.unaccent(lower(:texto))) > 0
          OR strpos(lower(telefono), lower(:texto)) > 0
          OR (:digitos <> '' AND strpos(telefono_normalizado, :digitos) > 0))
      """;

  @Query(value = "SELECT count(*) " + COINCIDE, nativeQuery = true)
  long contarCoincidencias(
      @Param("texto") String texto,
      @Param("digitos") String digitos,
      @Param("incluirInactivos") boolean incluirInactivos);

  @Query(
      value =
          "SELECT * "
              + COINCIDE
              + " ORDER BY ext.unaccent(lower(nombre)), id LIMIT :limite OFFSET :desde",
      nativeQuery = true)
  List<Propietario> coincidencias(
      @Param("texto") String texto,
      @Param("digitos") String digitos,
      @Param("incluirInactivos") boolean incluirInactivos,
      @Param("desde") long desde,
      @Param("limite") int limite);
}
