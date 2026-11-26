package com.vetcat.sesion;

import com.vetcat.catalogo.entity.Clinica;
import com.vetcat.catalogo.entity.UsuarioClinica;
import com.vetcat.catalogo.repository.ClinicaRepository;
import com.vetcat.catalogo.repository.UsuarioClinicaRepository;
import com.vetcat.security.TokenTenantService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abre la sesión de una clínica: lista las del usuario y emite el token de tenant.
 *
 * <p>La pertenencia se lee siempre de `catalogo.usuario_clinica` con la unidad de persistencia del
 * catálogo, que no es multi-tenant (ADR-0003).
 */
@Service
public class SesionService {

  private static final String ACTIVA = "ACTIVA";
  private static final String MEMBRESIA_ACTIVA = "ACTIVO";

  private final UsuarioClinicaRepository usuarios;
  private final ClinicaRepository clinicas;
  private final TokenTenantService tokens;

  public SesionService(
      UsuarioClinicaRepository usuarios, ClinicaRepository clinicas, TokenTenantService tokens) {
    this.usuarios = usuarios;
    this.clinicas = clinicas;
    this.tokens = tokens;
  }

  /**
   * Clínicas del usuario con sus roles en cada una; las suspendidas también, para poder mostrarlas.
   */
  @Transactional("catalogoTransactionManager")
  public List<ClinicaDelUsuario> clinicas(UUID keycloakId) {
    Map<Long, List<String>> rolesPorClinica =
        usuarios.findByKeycloakId(keycloakId).stream()
            .filter(fila -> MEMBRESIA_ACTIVA.equals(fila.getEstado()))
            .collect(
                Collectors.groupingBy(
                    UsuarioClinica::getClinicaId,
                    Collectors.mapping(UsuarioClinica::getRol, Collectors.toList())));

    return clinicas.findAllById(rolesPorClinica.keySet()).stream()
        .map(clinica -> aClinicaDelUsuario(clinica, rolesPorClinica.get(clinica.getId())))
        .sorted(Comparator.comparing(ClinicaDelUsuario::nombre))
        .toList();
  }

  /**
   * Valida la pertenencia en la clínica y emite el token de tenant.
   *
   * <p>Los dos rechazos llevan mensajes distintos a propósito: «La clínica no está disponible» solo
   * lo ve alguien que ya es miembro, así que no revela nada a quien no lo es.
   *
   * @throws SinAccesoALaClinicaException si no hay membresía activa o la clínica no está activa
   */
  @Transactional("catalogoTransactionManager")
  public SesionResponse abrir(UUID keycloakId, Long clinicaId) {
    List<UsuarioClinica> membresias =
        usuarios.findByKeycloakIdAndClinicaIdAndEstado(keycloakId, clinicaId, MEMBRESIA_ACTIVA);
    if (membresias.isEmpty()) {
      throw new SinAccesoALaClinicaException("No tienes acceso a esta clínica");
    }

    Clinica clinica =
        clinicas
            .findById(clinicaId)
            .orElseThrow(() -> new SinAccesoALaClinicaException("No tienes acceso a esta clínica"));
    if (!ACTIVA.equals(clinica.getEstado())) {
      throw new SinAccesoALaClinicaException("La clínica no está disponible");
    }

    List<String> roles =
        membresias.stream().map(UsuarioClinica::getRol).distinct().sorted().toList();
    String token = tokens.emitir(keycloakId.toString(), clinicaId, roles);

    return new SesionResponse(token, clinicaId, clinica.getNombre(), roles);
  }

  private static ClinicaDelUsuario aClinicaDelUsuario(Clinica clinica, List<String> roles) {
    return new ClinicaDelUsuario(
        clinica.getId(),
        clinica.getNombre(),
        clinica.getEstado(),
        roles.stream().distinct().sorted().toList());
  }
}
