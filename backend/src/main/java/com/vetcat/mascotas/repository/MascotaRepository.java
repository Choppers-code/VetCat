package com.vetcat.mascotas.repository;

import com.vetcat.mascotas.entity.Mascota;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Mascotas de la clínica activa. */
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

  List<Mascota> findByPropietarioIdAndActivoTrueOrderByNombreAscIdAsc(Long propietarioId);

  List<Mascota> findByPropietarioIdInAndActivoTrueOrderByNombreAscIdAsc(
      Collection<Long> propietarioIds);

  /** Nombre que contiene el texto, sin acentos ni mayúsculas y sin comodines. */
  String COINCIDE =
      """
      FROM mascota
      WHERE (:incluirInactivos OR activo)
        AND strpos(ext.unaccent(lower(nombre)), ext.unaccent(lower(:texto))) > 0
      """;

  @Query(value = "SELECT count(*) " + COINCIDE, nativeQuery = true)
  long contarCoincidencias(
      @Param("texto") String texto, @Param("incluirInactivos") boolean incluirInactivos);

  @Query(
      value =
          "SELECT * "
              + COINCIDE
              + " ORDER BY ext.unaccent(lower(nombre)), id LIMIT :limite OFFSET :desde",
      nativeQuery = true)
  List<Mascota> coincidencias(
      @Param("texto") String texto,
      @Param("incluirInactivos") boolean incluirInactivos,
      @Param("desde") long desde,
      @Param("limite") int limite);
}
