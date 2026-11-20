package com.vetcat.catalogo.repository;

import com.vetcat.catalogo.entity.UsuarioClinica;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Pertenencia usuario-clínica, leída siempre del catálogo global. */
public interface UsuarioClinicaRepository extends JpaRepository<UsuarioClinica, Long> {

  /**
   * Roles de un usuario en una clínica con el estado indicado; su unión son los roles efectivos.
   */
  List<UsuarioClinica> findByKeycloakIdAndClinicaIdAndEstado(
      UUID keycloakId, Long clinicaId, String estado);
}
