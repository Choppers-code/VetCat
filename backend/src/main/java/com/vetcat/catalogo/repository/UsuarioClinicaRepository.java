package com.vetcat.catalogo.repository;

import com.vetcat.catalogo.entity.UsuarioClinica;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** Pertenencia usuario-clínica, leída siempre del catálogo global. */
public interface UsuarioClinicaRepository extends JpaRepository<UsuarioClinica, Long> {

  /**
   * Roles de un usuario en una clínica con el estado indicado; su unión son los roles efectivos.
   */
  List<UsuarioClinica> findByKeycloakIdAndClinicaIdAndEstado(
      UUID keycloakId, Long clinicaId, String estado);

  /** Filas de una clínica con alguno de los roles dados, de cualquier usuario y estado. */
  List<UsuarioClinica> findByClinicaIdAndRolIn(Long clinicaId, Collection<String> roles);

  /** Filas de un usuario en una clínica con alguno de los roles dados, de cualquier estado. */
  List<UsuarioClinica> findByKeycloakIdAndClinicaIdAndRolIn(
      UUID keycloakId, Long clinicaId, Collection<String> roles);

  long countByClinicaIdAndRolAndEstado(Long clinicaId, String rol, String estado);

  /** Quita todas las membresías de una clínica; solo para deshacer un alta fallida. */
  @Modifying
  @Transactional("catalogoTransactionManager")
  @Query("delete from UsuarioClinica u where u.clinicaId = :clinicaId")
  void eliminarPorClinica(@Param("clinicaId") Long clinicaId);
}
