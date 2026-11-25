package com.vetcat.configuracion.service;

import com.vetcat.catalogo.entity.UsuarioClinica;
import com.vetcat.catalogo.repository.ClinicaRepository;
import com.vetcat.catalogo.repository.UsuarioClinicaRepository;
import com.vetcat.configuracion.dto.MiembroResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Personal de la clínica: una fila de {@code usuario_clinica} por rol (CON-002 a CON-004,
 * ADR-0006).
 *
 * <p>La clínica llega siempre como parámetro, sacada del token por quien llama; ningún método la
 * deduce de otro dato. Solo toca roles de personal: la fila {@code cliente} de la misma persona
 * (IDA-009) no se lee ni se modifica. Una persona nunca se borra: se desactiva y se puede
 * reactivar.
 */
@Service
@Transactional("catalogoTransactionManager")
public class PersonalClinicaService {

  /**
   * Roles que el {@code clinica_admin} puede asignar; ni {@code super_admin} ni {@code cliente}.
   */
  public static final Set<String> ROLES_DE_PERSONAL =
      Set.of("clinica_admin", "veterinario", "recepcionista");

  private static final String ADMIN = "clinica_admin";
  private static final String ACTIVO = "ACTIVO";
  private static final String INACTIVO = "INACTIVO";

  private final ClinicaRepository clinicas;
  private final UsuarioClinicaRepository membresias;

  public PersonalClinicaService(ClinicaRepository clinicas, UsuarioClinicaRepository membresias) {
    this.clinicas = clinicas;
    this.membresias = membresias;
  }

  @Transactional(value = "catalogoTransactionManager", readOnly = true)
  public List<MiembroResponse> listar(long clinicaId) {
    Map<UUID, List<UsuarioClinica>> porUsuario = new LinkedHashMap<>();
    for (UsuarioClinica fila : membresias.findByClinicaIdAndRolIn(clinicaId, ROLES_DE_PERSONAL)) {
      porUsuario.computeIfAbsent(fila.getKeycloakId(), k -> new ArrayList<>()).add(fila);
    }
    return porUsuario.entrySet().stream()
        .map(e -> aMiembro(e.getKey(), e.getValue()))
        .sorted(Comparator.comparing(m -> m.keycloakId().toString()))
        .toList();
  }

  /** Agrega a una persona con sus roles; si ya era del personal, deja exactamente esos roles. */
  public MiembroResponse agregar(long clinicaId, UUID keycloakId, Set<String> roles) {
    validar(roles);
    bloquear(clinicaId);
    return aplicar(clinicaId, keycloakId, roles, false);
  }

  /** Reemplaza los roles de personal de un miembro existente (CON-003). */
  public MiembroResponse cambiarRoles(long clinicaId, UUID keycloakId, Set<String> roles) {
    validar(roles);
    bloquear(clinicaId);
    return aplicar(clinicaId, keycloakId, roles, true);
  }

  /** Impide el acceso a la clínica conservando al miembro y sus roles (CON-004). */
  public MiembroResponse desactivar(long clinicaId, UUID keycloakId) {
    return cambiarEstado(clinicaId, keycloakId, INACTIVO);
  }

  public MiembroResponse reactivar(long clinicaId, UUID keycloakId) {
    return cambiarEstado(clinicaId, keycloakId, ACTIVO);
  }

  private MiembroResponse cambiarEstado(long clinicaId, UUID keycloakId, String estado) {
    bloquear(clinicaId);
    long administradoresAntes = administradoresActivos(clinicaId);
    List<UsuarioClinica> filas = filasDelMiembro(clinicaId, keycloakId);
    if (filas.isEmpty()) {
      throw new MiembroNoEncontradoException();
    }
    filas.forEach(f -> f.setEstado(estado));
    membresias.saveAllAndFlush(filas);
    exigirAdministrador(clinicaId, administradoresAntes);
    return aMiembro(keycloakId, filas);
  }

  private MiembroResponse aplicar(
      long clinicaId, UUID keycloakId, Set<String> roles, boolean debeExistir) {
    long administradoresAntes = administradoresActivos(clinicaId);
    List<UsuarioClinica> actuales = filasDelMiembro(clinicaId, keycloakId);
    if (debeExistir && actuales.isEmpty()) {
      throw new MiembroNoEncontradoException();
    }
    // Una persona desactivada conserva su estado al cambiarle los roles.
    String estado =
        actuales.isEmpty() || actuales.stream().anyMatch(f -> ACTIVO.equals(f.getEstado()))
            ? ACTIVO
            : INACTIVO;
    List<UsuarioClinica> resultado = new ArrayList<>();
    List<UsuarioClinica> sobrantes = new ArrayList<>();
    for (UsuarioClinica fila : actuales) {
      if (roles.contains(fila.getRol())) {
        fila.setEstado(estado);
        resultado.add(fila);
      } else {
        sobrantes.add(fila);
      }
    }
    for (String rol : roles) {
      if (actuales.stream().noneMatch(f -> f.getRol().equals(rol))) {
        UsuarioClinica nueva = new UsuarioClinica(keycloakId, clinicaId, rol);
        nueva.setEstado(estado);
        resultado.add(nueva);
      }
    }
    membresias.deleteAll(sobrantes);
    membresias.saveAllAndFlush(resultado);
    exigirAdministrador(clinicaId, administradoresAntes);
    return aMiembro(keycloakId, resultado);
  }

  private void validar(Set<String> roles) {
    if (roles == null || roles.isEmpty()) {
      throw new PersonalInvalidoException("Indica al menos un rol");
    }
    if (!ROLES_DE_PERSONAL.containsAll(roles)) {
      throw new PersonalInvalidoException("Rol no permitido para el personal de la clínica");
    }
  }

  /**
   * Serializa los cambios de personal de una clínica para que el conteo de administradores sea
   * fiable.
   */
  private void bloquear(long clinicaId) {
    clinicas.bloquearPorId(clinicaId).orElseThrow(MiembroNoEncontradoException::new);
  }

  private long administradoresActivos(long clinicaId) {
    return membresias.countByClinicaIdAndRolAndEstado(clinicaId, ADMIN, ACTIVO);
  }

  /** Falla si la operación dejó sin administradores a una clínica que sí tenía alguno. */
  private void exigirAdministrador(long clinicaId, long administradoresAntes) {
    if (administradoresAntes > 0 && administradoresActivos(clinicaId) == 0) {
      throw new UltimoAdministradorException();
    }
  }

  private List<UsuarioClinica> filasDelMiembro(long clinicaId, UUID keycloakId) {
    return membresias.findByKeycloakIdAndClinicaIdAndRolIn(
        keycloakId, clinicaId, ROLES_DE_PERSONAL);
  }

  private static MiembroResponse aMiembro(UUID keycloakId, List<UsuarioClinica> filas) {
    String estado = filas.stream().anyMatch(f -> ACTIVO.equals(f.getEstado())) ? ACTIVO : INACTIVO;
    List<String> roles = filas.stream().map(UsuarioClinica::getRol).sorted().toList();
    return new MiembroResponse(keycloakId, roles, estado);
  }
}
