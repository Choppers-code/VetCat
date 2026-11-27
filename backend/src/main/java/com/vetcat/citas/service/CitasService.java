package com.vetcat.citas.service;

import com.vetcat.catalogo.service.CatalogoFachada;
import com.vetcat.citas.dto.CitaRequest;
import com.vetcat.citas.dto.CitaResponse;
import com.vetcat.citas.entity.BloqueoAgenda;
import com.vetcat.citas.entity.Cita;
import com.vetcat.citas.entity.Consultorio;
import com.vetcat.citas.entity.HorarioConsultorio;
import com.vetcat.citas.repository.BloqueoAgendaRepository;
import com.vetcat.citas.repository.CitaRepository;
import com.vetcat.citas.repository.ConsultorioRepository;
import com.vetcat.citas.repository.HorarioConsultorioRepository;
import com.vetcat.mascotas.dto.MascotaResponse;
import com.vetcat.mascotas.service.MascotaFachada;
import com.vetcat.propietarios.service.PropietarioFachada;
import com.vetcat.shared.AgendaException;
import com.vetcat.tenancy.TenantContext;
import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("tenantTransactionManager")
public class CitasService {

  private final CitaRepository citas;
  private final ConsultorioRepository consultorios;
  private final HorarioConsultorioRepository horarios;
  private final BloqueoAgendaRepository bloqueos;
  private final CatalogoFachada catalogo;
  private final MascotaFachada mascotas;
  private final PropietarioFachada propietarios;

  public CitasService(
      CitaRepository citas,
      ConsultorioRepository consultorios,
      HorarioConsultorioRepository horarios,
      BloqueoAgendaRepository bloqueos,
      CatalogoFachada catalogo,
      MascotaFachada mascotas,
      PropietarioFachada propietarios) {
    this.citas = citas;
    this.consultorios = consultorios;
    this.horarios = horarios;
    this.bloqueos = bloqueos;
    this.catalogo = catalogo;
    this.mascotas = mascotas;
    this.propietarios = propietarios;
  }

  @PreAuthorize("hasAnyRole('clinica_admin', 'recepcionista', 'veterinario')")
  public CitaResponse crear(CitaRequest request, Jwt identidad) {
    long clinicaId = TenantContext.actual().orElseThrow();
    ZoneId zona =
        catalogo
            .zonaHorariaActiva(clinicaId)
            .orElseThrow(() -> error(HttpStatus.FORBIDDEN, "La clínica activa no está disponible"));
    UUID keycloakIdVeterinario =
        catalogo
            .keycloakIdDeVeterinarioActivo(clinicaId, request.veterinarioId())
            .orElseThrow(
                () ->
                    error(
                        HttpStatus.NOT_FOUND,
                        "No se encontró un veterinario activo en la clínica"));
    if (identidad.getClaimAsStringList("roles").contains("veterinario")) {
      UUID usuario = identidadDelVeterinario(identidad.getSubject());
      if (!keycloakIdVeterinario.equals(usuario)) {
        throw error(
            HttpStatus.FORBIDDEN, "Un veterinario solo puede crear citas en su propia agenda");
      }
    }

    MascotaResponse mascota = mascotas.activa(request.mascotaId());
    propietarios.retenerActivo(mascota.propietarioId());
    Consultorio consultorio =
        consultorios
            .findById(request.consultorioId())
            .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No se encontró el consultorio"));
    if (!consultorio.isActivo()) {
      throw error(HttpStatus.UNPROCESSABLE_CONTENT, "El consultorio está dado de baja");
    }

    Instant inicio = request.inicio().toInstant();
    Instant fin = inicio.plus(Duration.ofMinutes(request.duracionMinutos()));
    if (inicio.isBefore(Instant.now())) {
      throw error(HttpStatus.UNPROCESSABLE_CONTENT, "No se puede crear una cita en el pasado");
    }
    validarBloqueos(request.consultorioId(), request.veterinarioId(), inicio, fin);
    validarHorario(request.consultorioId(), inicio, fin, zona);

    try {
      Cita cita =
          citas.saveAndFlush(
              new Cita(
                  request.mascotaId(),
                  request.veterinarioId(),
                  request.consultorioId(),
                  "CONSULTORIO",
                  null,
                  inicio,
                  fin,
                  opcional(request.motivo()),
                  "PERSONAL"));
      return respuesta(cita);
    } catch (DataIntegrityViolationException | PersistenceException excepcion) {
      if (contieneEmpalme(excepcion)) {
        throw error(HttpStatus.CONFLICT, "La franja ya está ocupada por otra cita");
      }
      throw excepcion;
    }
  }

  private void validarBloqueos(
      Long consultorioId, Long veterinarioId, Instant inicio, Instant fin) {
    List<BloqueoAgenda> encontrados =
        bloqueos.buscarSolapados(consultorioId, veterinarioId, inicio, fin);
    if (!encontrados.isEmpty()) {
      BloqueoAgenda bloqueo = encontrados.getFirst();
      String destino = bloqueo.getConsultorioId() == null ? "del veterinario" : "del consultorio";
      throw error(
          HttpStatus.UNPROCESSABLE_CONTENT,
          "La cita cae dentro de un bloqueo " + destino + ": " + bloqueo.getMotivo());
    }
  }

  private void validarHorario(Long consultorioId, Instant inicio, Instant fin, ZoneId zona) {
    ZonedDateTime inicioLocal = inicio.atZone(zona);
    ZonedDateTime finLocal = fin.atZone(zona);
    LocalDate fecha = inicioLocal.toLocalDate();
    List<HorarioConsultorio> activos =
        horarios.findByConsultorioIdOrderById(consultorioId).stream()
            .filter(HorarioConsultorio::isActivo)
            .toList();
    List<HorarioConsultorio> excepciones =
        activos.stream().filter(horario -> fecha.equals(horario.getFechaExcepcion())).toList();
    List<HorarioConsultorio> efectivos =
        excepciones.isEmpty()
            ? activos.stream()
                .filter(horario -> horario.getDiaSemana() != null)
                .filter(horario -> horario.getDiaSemana() == fecha.getDayOfWeek().getValue())
                .toList()
            : excepciones;

    if (efectivos.stream().anyMatch(HorarioConsultorio::isEsCierre)) {
      throw error(HttpStatus.UNPROCESSABLE_CONTENT, "El consultorio está cerrado en esa fecha");
    }
    boolean dentro =
        inicioLocal.toLocalDate().equals(finLocal.toLocalDate())
            && efectivos.stream()
                .anyMatch(
                    horario ->
                        !horario.isEsCierre()
                            && !inicioLocal.toLocalTime().isBefore(horario.getInicio())
                            && !finLocal.toLocalTime().isAfter(horario.getFin()));
    if (!dentro) {
      throw error(
          HttpStatus.UNPROCESSABLE_CONTENT,
          "La cita está fuera del horario efectivo del consultorio");
    }
  }

  private static UUID identidadDelVeterinario(String subject) {
    try {
      return UUID.fromString(subject);
    } catch (IllegalArgumentException excepcion) {
      throw error(HttpStatus.FORBIDDEN, "No se pudo validar la identidad del veterinario");
    }
  }

  private static boolean contieneEmpalme(Throwable excepcion) {
    for (Throwable causa = excepcion; causa != null; causa = causa.getCause()) {
      if (causa instanceof SQLException sql) {
        for (SQLException siguiente = sql;
            siguiente != null;
            siguiente = siguiente.getNextException()) {
          if ("23P01".equals(siguiente.getSQLState())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static String opcional(String texto) {
    return texto == null || texto.isBlank() ? null : texto.strip();
  }

  private static CitaResponse respuesta(Cita cita) {
    return new CitaResponse(
        cita.getId(),
        cita.getMascotaId(),
        cita.getVeterinarioId(),
        cita.getTipo(),
        cita.getConsultorioId(),
        cita.getInicio(),
        cita.getFin(),
        cita.getEstado(),
        cita.getMotivo());
  }

  private static AgendaException error(HttpStatus estado, String mensaje) {
    return new AgendaException(estado, mensaje);
  }
}
