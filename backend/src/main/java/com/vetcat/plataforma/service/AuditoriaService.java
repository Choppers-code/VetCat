package com.vetcat.plataforma.service;

import com.vetcat.plataforma.entity.AccionDePlataforma;
import com.vetcat.plataforma.entity.AuditoriaPlataforma;
import com.vetcat.plataforma.repository.AuditoriaPlataformaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Registra las acciones de plataforma y las devuelve para consulta.
 *
 * <p>El autor llega del token de identidad; este servicio no lo resuelve por su cuenta.
 */
@Service
public class AuditoriaService {

  private final AuditoriaPlataformaRepository repositorio;

  public AuditoriaService(AuditoriaPlataformaRepository repositorio) {
    this.repositorio = repositorio;
  }

  public void registrar(
      UUID actorKeycloakId, AccionDePlataforma accion, Long clinicaId, String detalle) {
    if (actorKeycloakId == null) {
      throw new IllegalArgumentException("Una acción de plataforma siempre tiene autor");
    }
    if (accion == null) {
      throw new IllegalArgumentException("Una acción de plataforma siempre tiene tipo");
    }
    repositorio.guardar(actorKeycloakId, accion, clinicaId, detalle);
  }

  /**
   * Acciones de una clínica, de la más reciente a la más antigua; sin clínica, las del catálogo.
   */
  public List<AuditoriaPlataforma> ultimasDeClinica(Long clinicaId) {
    return clinicaId == null ? repositorio.deCatalogo() : repositorio.deClinica(clinicaId);
  }
}
