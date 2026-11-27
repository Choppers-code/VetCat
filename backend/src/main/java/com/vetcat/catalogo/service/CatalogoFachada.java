package com.vetcat.catalogo.service;

import com.vetcat.catalogo.entity.UsuarioClinica;
import com.vetcat.catalogo.repository.ClinicaRepository;
import com.vetcat.catalogo.repository.UsuarioClinicaRepository;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Datos globales que los módulos de clínica necesitan consultar sin acceder a sus repositorios. */
@Service
@Transactional("catalogoTransactionManager")
public class CatalogoFachada {

  private final ClinicaRepository clinicas;
  private final UsuarioClinicaRepository membresias;

  public CatalogoFachada(ClinicaRepository clinicas, UsuarioClinicaRepository membresias) {
    this.clinicas = clinicas;
    this.membresias = membresias;
  }

  @Transactional(value = "catalogoTransactionManager", readOnly = true)
  public Optional<ZoneId> zonaHorariaActiva(long clinicaId) {
    return clinicas
        .findById(clinicaId)
        .filter(clinica -> "ACTIVA".equals(clinica.getEstado()))
        .map(clinica -> ZoneId.of(clinica.getZonaHoraria()));
  }

  @Transactional(value = "catalogoTransactionManager", readOnly = true)
  public Optional<UUID> keycloakIdDeVeterinarioActivo(long clinicaId, long veterinarioId) {
    return membresias
        .findByIdAndClinicaIdAndRolAndEstado(veterinarioId, clinicaId, "veterinario", "ACTIVO")
        .map(UsuarioClinica::getKeycloakId);
  }
}
