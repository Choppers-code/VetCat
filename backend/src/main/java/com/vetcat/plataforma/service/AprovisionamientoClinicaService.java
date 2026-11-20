package com.vetcat.plataforma.service;

import com.vetcat.catalogo.entity.Clinica;
import com.vetcat.catalogo.entity.UsuarioClinica;
import com.vetcat.catalogo.repository.ClinicaRepository;
import com.vetcat.catalogo.repository.UsuarioClinicaRepository;
import com.vetcat.plataforma.dto.AltaClinicaRequest;
import com.vetcat.plataforma.dto.ClinicaResponse;
import com.vetcat.plataforma.repository.EsquemaClinicaRepository;
import com.vetcat.plataforma.repository.MigracionTenantRepository;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Alta de una clínica: registro en el catálogo, esquema, migración y administrador.
 *
 * <p>Si un paso falla dentro de la misma operación se deshace todo, para que no quede una clínica a
 * medias (CLI-001). Lo que quede pendiente por una caída del proceso lo repara la reconciliación
 * (ADR-0010).
 */
@Service
public class AprovisionamientoClinicaService {

  private static final Logger LOG = LoggerFactory.getLogger(AprovisionamientoClinicaService.class);

  private final ClinicaRepository clinicas;
  private final UsuarioClinicaRepository membresias;
  private final MigracionTenantRepository estados;
  private final EsquemaClinicaRepository esquemas;
  private final MigradorTenant migrador;
  private final RegistroAdministrador administradores;

  public AprovisionamientoClinicaService(
      ClinicaRepository clinicas,
      UsuarioClinicaRepository membresias,
      MigracionTenantRepository estados,
      EsquemaClinicaRepository esquemas,
      MigradorTenant migrador,
      RegistroAdministrador administradores) {
    this.clinicas = clinicas;
    this.membresias = membresias;
    this.estados = estados;
    this.esquemas = esquemas;
    this.migrador = migrador;
    this.administradores = administradores;
  }

  public ClinicaResponse aprovisionar(AltaClinicaRequest alta) {
    validar(alta);
    Clinica nueva = clinicas.save(new Clinica(alta.nombre().strip(), alta.zonaHoraria()));
    // El esquema lo genera la base: se relee para no duplicar la regla en la aplicación.
    String esquema = clinicas.findById(nueva.getId()).orElseThrow().getEsquema();
    try {
      estados.registrarPendiente(esquema);
      migrador.aplicar(esquema);
      UUID administrador =
          administradores.registrar(
              nueva.getId(), alta.nombreAdministrador(), alta.correoAdministrador());
      membresias.save(new UsuarioClinica(administrador, nueva.getId(), "clinica_admin"));
      return new ClinicaResponse(nueva.getId(), nueva.getNombre(), esquema, nueva.getEstado());
    } catch (RuntimeException e) {
      deshacer(nueva.getId(), esquema);
      throw e;
    }
  }

  private static void validar(AltaClinicaRequest alta) {
    if (alta.nombre() == null || alta.nombre().isBlank()) {
      throw new IllegalArgumentException("El nombre de la clínica es obligatorio");
    }
    if (alta.correoAdministrador() == null || alta.correoAdministrador().isBlank()) {
      throw new IllegalArgumentException("El correo del administrador es obligatorio");
    }
    try {
      ZoneId.of(alta.zonaHoraria());
    } catch (DateTimeException | NullPointerException e) {
      throw new IllegalArgumentException("La zona horaria no es válida");
    }
  }

  /** Deshace en orden inverso lo creado por esta operación; un fallo aquí queda registrado. */
  private void deshacer(Long clinicaId, String esquema) {
    try {
      membresias.eliminarPorClinica(clinicaId);
      esquemas.eliminar(esquema);
      estados.eliminar(esquema);
      clinicas.deleteById(clinicaId);
    } catch (RuntimeException e) {
      LOG.error("No se pudo deshacer un alta fallida; la clínica requiere limpieza manual", e);
    }
  }
}
