package com.vetcat.citas.service;

import com.vetcat.citas.dto.ConsultorioRequest;
import com.vetcat.citas.dto.ConsultorioResponse;
import com.vetcat.citas.dto.HorarioRequest;
import com.vetcat.citas.dto.HorarioResponse;
import com.vetcat.citas.entity.Consultorio;
import com.vetcat.citas.entity.HorarioConsultorio;
import com.vetcat.citas.repository.ConsultorioRepository;
import com.vetcat.citas.repository.HorarioConsultorioRepository;
import com.vetcat.shared.AgendaException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("tenantTransactionManager")
public class ConfiguracionAgendaService {

  private final ConsultorioRepository consultorios;
  private final HorarioConsultorioRepository horarios;

  public ConfiguracionAgendaService(
      ConsultorioRepository consultorios, HorarioConsultorioRepository horarios) {
    this.consultorios = consultorios;
    this.horarios = horarios;
  }

  @Transactional(value = "tenantTransactionManager", readOnly = true)
  @PreAuthorize("hasAnyRole('clinica_admin', 'veterinario', 'recepcionista')")
  public List<ConsultorioResponse> listarConsultorios() {
    return consultorios.findAllByOrderById().stream()
        .map(ConfiguracionAgendaService::respuesta)
        .toList();
  }

  @PreAuthorize("hasRole('clinica_admin')")
  public ConsultorioResponse crearConsultorio(ConsultorioRequest request) {
    if (request == null) {
      throw error(HttpStatus.UNPROCESSABLE_ENTITY, "Indica los datos del consultorio");
    }
    validarNombre(request.nombre());
    Consultorio consultorio = new Consultorio(request.nombre().trim());
    consultorio.actualizar(request.nombre().trim(), activo(request.activo()));
    return respuesta(consultorios.saveAndFlush(consultorio));
  }

  @PreAuthorize("hasRole('clinica_admin')")
  public ConsultorioResponse editarConsultorio(long id, ConsultorioRequest request) {
    if (request == null) {
      throw error(HttpStatus.UNPROCESSABLE_ENTITY, "Indica los datos del consultorio");
    }
    validarNombre(request.nombre());
    Consultorio consultorio =
        consultorios
            .findById(id)
            .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No se encontró el consultorio"));
    consultorio.actualizar(request.nombre().trim(), activo(request.activo()));
    return respuesta(consultorios.saveAndFlush(consultorio));
  }

  @Transactional(value = "tenantTransactionManager", readOnly = true)
  @PreAuthorize("hasAnyRole('clinica_admin', 'veterinario', 'recepcionista')")
  public List<HorarioResponse> listarHorarios(Long consultorioId, LocalDate fecha) {
    List<HorarioConsultorio> resultado =
        consultorioId == null
            ? horarios.findAllByOrderById()
            : horarios.findByConsultorioIdOrderById(consultorioId);
    if (fecha != null) {
      Set<Long> consultoriosConExcepcion =
          resultado.stream()
              .filter(HorarioConsultorio::isActivo)
              .filter(horario -> fecha.equals(horario.getFechaExcepcion()))
              .map(HorarioConsultorio::getConsultorioId)
              .collect(Collectors.toSet());
      short diaSemana = (short) fecha.getDayOfWeek().getValue();
      resultado =
          resultado.stream()
              .filter(HorarioConsultorio::isActivo)
              .filter(
                  horario ->
                      consultoriosConExcepcion.contains(horario.getConsultorioId())
                          ? fecha.equals(horario.getFechaExcepcion())
                          : horario.getDiaSemana() != null && horario.getDiaSemana() == diaSemana)
              .toList();
    }
    return resultado.stream().map(ConfiguracionAgendaService::respuesta).toList();
  }

  @PreAuthorize("hasRole('clinica_admin')")
  public HorarioResponse crearHorario(HorarioRequest request) {
    validar(request);
    validarConsultorio(request.consultorioId());
    try {
      return respuesta(horarios.saveAndFlush(nuevo(request)));
    } catch (DataIntegrityViolationException excepcion) {
      throw conflicto();
    }
  }

  @PreAuthorize("hasRole('clinica_admin')")
  public HorarioResponse editarHorario(long id, HorarioRequest request) {
    validar(request);
    validarConsultorio(request.consultorioId());
    HorarioConsultorio horario =
        horarios
            .findById(id)
            .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No se encontró el horario"));
    horario.actualizar(
        request.consultorioId(),
        request.diaSemana(),
        request.fecha(),
        request.horaInicio(),
        request.horaFin(),
        request.esCierre(),
        activo(request.activo()));
    try {
      return respuesta(horarios.saveAndFlush(horario));
    } catch (DataIntegrityViolationException excepcion) {
      throw conflicto();
    }
  }

  private void validarConsultorio(Long id) {
    Consultorio consultorio =
        consultorios
            .findById(id)
            .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No se encontró el consultorio"));
    if (!consultorio.isActivo()) {
      throw error(HttpStatus.UNPROCESSABLE_ENTITY, "El consultorio está dado de baja");
    }
  }

  private static void validarNombre(String nombre) {
    if (nombre == null || nombre.isBlank() || nombre.length() > 100) {
      throw error(HttpStatus.UNPROCESSABLE_ENTITY, "El nombre debe tener entre 1 y 100 caracteres");
    }
  }

  private static void validar(HorarioRequest request) {
    if (request == null) {
      throw error(HttpStatus.UNPROCESSABLE_ENTITY, "Indica los datos del horario");
    }
    if (request.consultorioId() == null
        || request.consultorioId() <= 0
        || request.esCierre() == null) {
      throw error(HttpStatus.UNPROCESSABLE_ENTITY, "Indica consultorioId y esCierre");
    }
    boolean semanal = request.diaSemana() != null;
    boolean excepcion = request.fecha() != null;
    if (semanal == excepcion || (semanal && (request.diaSemana() < 1 || request.diaSemana() > 7))) {
      throw error(HttpStatus.UNPROCESSABLE_ENTITY, "Indica un día semanal o una fecha válida");
    }
    if (request.esCierre()) {
      if (request.horaInicio() != null || request.horaFin() != null) {
        throw error(HttpStatus.UNPROCESSABLE_ENTITY, "Un cierre no admite horas");
      }
    } else if (request.horaInicio() == null
        || request.horaFin() == null
        || !request.horaFin().isAfter(request.horaInicio())) {
      throw error(HttpStatus.UNPROCESSABLE_ENTITY, "El horario requiere un rango válido");
    }
  }

  private HorarioConsultorio nuevo(HorarioRequest request) {
    HorarioConsultorio horario =
        new HorarioConsultorio(
            request.consultorioId(),
            request.diaSemana(),
            request.fecha(),
            request.horaInicio(),
            request.horaFin(),
            request.esCierre());
    horario.setActivo(activo(request.activo()));
    return horario;
  }

  private static boolean activo(Boolean valor) {
    return valor == null || valor;
  }

  private static AgendaException conflicto() {
    return error(HttpStatus.CONFLICT, "El horario se solapa o contradice otro horario activo");
  }

  private static AgendaException error(HttpStatus estado, String mensaje) {
    return new AgendaException(estado, mensaje);
  }

  private static ConsultorioResponse respuesta(Consultorio consultorio) {
    return new ConsultorioResponse(
        consultorio.getId(), consultorio.getNombre(), consultorio.isActivo());
  }

  private static HorarioResponse respuesta(HorarioConsultorio horario) {
    return new HorarioResponse(
        horario.getId(),
        horario.getConsultorioId(),
        horario.isEsCierre(),
        horario.isActivo(),
        horario.getDiaSemana(),
        horario.getFechaExcepcion(),
        horario.getInicio(),
        horario.getFin());
  }
}
