package com.vetcat.citas.service;

import com.vetcat.citas.dto.BloqueoAgendaRequest;
import com.vetcat.citas.dto.BloqueoAgendaResponse;
import com.vetcat.citas.dto.CambiarEstadoCitaRequest;
import com.vetcat.citas.dto.CitaRequest;
import com.vetcat.citas.dto.CitaResponse;
import com.vetcat.citas.dto.ReprogramarCitaRequest;
import com.vetcat.citas.entity.BloqueoAgenda;
import com.vetcat.citas.entity.Cita;
import com.vetcat.citas.entity.Consultorio;
import com.vetcat.citas.entity.EstadoCita;
import com.vetcat.citas.entity.HorarioConsultorio;
import com.vetcat.citas.entity.TipoCita;
import com.vetcat.citas.exception.ConflictoAgendaException;
import com.vetcat.citas.exception.RecursoAgendaNoEncontradoException;
import com.vetcat.citas.exception.SolicitudAgendaInvalidaException;
import com.vetcat.citas.port.ClinicaContextPort;
import com.vetcat.citas.repository.BloqueoAgendaRepository;
import com.vetcat.citas.repository.CitaRepository;
import com.vetcat.citas.repository.ConsultorioRepository;
import com.vetcat.citas.repository.HorarioConsultorioRepository;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AgendaService {
    private final CitaRepository citas;
    private final BloqueoAgendaRepository bloqueos;
    private final ConsultorioRepository consultorios;
    private final HorarioConsultorioRepository horarios;
    private final ClinicaContextPort clinica;

    public AgendaService(CitaRepository citas, BloqueoAgendaRepository bloqueos,
            ConsultorioRepository consultorios, HorarioConsultorioRepository horarios,
            ClinicaContextPort clinica) {
        this.citas = citas;
        this.bloqueos = bloqueos;
        this.consultorios = consultorios;
        this.horarios = horarios;
        this.clinica = clinica;
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> buscarCitas(
            OffsetDateTime desde, OffsetDateTime hasta, UUID consultorioId, UUID veterinarioId, EstadoCita estado) {
        if ((desde == null) != (hasta == null) || (desde != null && !hasta.isAfter(desde))) {
            throw new SolicitudAgendaInvalidaException("El rango de fechas es inválido");
        }
        return citas.buscarPorFiltros(desde, hasta, consultorioId, veterinarioId, estado)
                .stream().map(this::respuesta).toList();
    }

    public CitaResponse crearCita(CitaRequest request) {
        Consultorio consultorio = obtenerConsultorio(request.tipo(), request.consultorioId());
        validarDisponibilidad(request.veterinarioId(), consultorio, request.tipo(),
                request.inicio(), request.fin());
        Cita cita = new Cita(
                request.mascotaId(), request.veterinarioId(), request.tipo(), consultorio,
                request.direccion(), request.inicio(), request.fin(), request.motivo(),
                EstadoCita.PENDIENTE, null, null, null);
        return respuesta(guardar(cita));
    }

    public CitaResponse reprogramar(UUID id, ReprogramarCitaRequest request, String actor) {
        Cita anterior = buscarCita(id);
        if (anterior.getEstado() == EstadoCita.CANCELADA) {
            throw new ConflictoAgendaException("No se puede reprogramar una cita cancelada");
        }
        anterior.setEstado(EstadoCita.CANCELADA);
        anterior.setCanceladaPor(actor);
        anterior.setMotivoCancelacion(request.motivoCancelacion() == null
                ? "Reprogramada" : request.motivoCancelacion());
        citas.saveAndFlush(anterior);

        validarDisponibilidad(anterior.getVeterinarioId(), anterior.getConsultorio(), anterior.getTipo(),
                request.inicio(), request.fin());
        Cita nueva = new Cita(
                anterior.getMascotaId(), anterior.getVeterinarioId(), anterior.getTipo(),
                anterior.getConsultorio(), anterior.getDireccion(), request.inicio(), request.fin(),
                anterior.getMotivo(), EstadoCita.PENDIENTE, null, null, anterior);
        return respuesta(guardar(nueva));
    }

    public CitaResponse cambiarEstado(UUID id, CambiarEstadoCitaRequest request, String actor) {
        Cita cita = buscarCita(id);
        if (request.estado() != EstadoCita.CANCELADA && cita.getEstado() == EstadoCita.CANCELADA) {
            validarDisponibilidad(cita.getVeterinarioId(), cita.getConsultorio(), cita.getTipo(),
                    cita.getInicio(), cita.getFin());
        }
        cita.setEstado(request.estado());
        cita.setCanceladaPor(request.estado() == EstadoCita.CANCELADA ? actor : null);
        cita.setMotivoCancelacion(request.estado() == EstadoCita.CANCELADA
                ? request.motivoCancelacion() : null);
        return respuesta(guardar(cita));
    }

    public BloqueoAgendaResponse crearBloqueo(BloqueoAgendaRequest request, String actor) {
        validarRango(request.inicio(), request.fin());
        Consultorio consultorio = request.consultorioId() == null ? null
                : consultorios.findById(request.consultorioId())
                        .orElseThrow(() -> new RecursoAgendaNoEncontradoException("No se encontró el consultorio"));
        if (citas.existeEmpalmeConBloqueo(
                request.veterinarioId(), request.consultorioId(), request.inicio(), request.fin())) {
            throw new ConflictoAgendaException("El bloqueo coincide con una cita existente");
        }
        return respuesta(bloqueos.saveAndFlush(new BloqueoAgenda(
                consultorio, request.veterinarioId(), request.inicio(), request.fin(), request.motivo(), actor)));
    }

    @Transactional(readOnly = true)
    public List<BloqueoAgendaResponse> buscarBloqueos(
            OffsetDateTime desde, OffsetDateTime hasta, UUID consultorioId, UUID veterinarioId) {
        validarRango(desde, hasta);
        return bloqueos.buscarPorRango(desde, hasta, consultorioId, veterinarioId)
                .stream().map(this::respuesta).toList();
    }

    public void eliminarBloqueo(UUID id) {
        bloqueos.delete(bloqueos.findById(id)
                .orElseThrow(() -> new RecursoAgendaNoEncontradoException("No se encontró el bloqueo")));
    }

    private void validarDisponibilidad(
            UUID veterinarioId, Consultorio consultorio, TipoCita tipo,
            OffsetDateTime inicio, OffsetDateTime fin) {
        validarRango(inicio, fin);
        UUID consultorioId = consultorio == null ? null : consultorio.getId();
        if (consultorio != null && !consultorio.isActivo()) {
            throw new ConflictoAgendaException("El consultorio está inactivo");
        }
        if (tipo == TipoCita.CONSULTORIO) validarHorario(consultorioId, inicio, fin);
        if (bloqueos.existeConflicto(veterinarioId, consultorioId, inicio, fin)) {
            throw new ConflictoAgendaException("La cita coincide con un bloqueo de agenda");
        }
        if (citas.existeEmpalmePorVeterinario(veterinarioId, inicio, fin)
                || (consultorioId != null
                && citas.existeEmpalmePorConsultorio(consultorioId, inicio, fin))) {
            throw new ConflictoAgendaException("La cita se empalma con otra cita activa");
        }
    }

    private void validarHorario(UUID consultorioId, OffsetDateTime inicio, OffsetDateTime fin) {
        ZoneId zona = clinica.getZonaHorariaActual();
        var inicioLocal = inicio.atZoneSameInstant(zona);
        var finLocal = fin.atZoneSameInstant(zona);
        if (!inicioLocal.toLocalDate().equals(finLocal.toLocalDate())) {
            throw new ConflictoAgendaException("La cita debe quedar dentro del horario de un solo día");
        }
        List<HorarioConsultorio> excepciones = horarios
                .findByConsultorio_IdAndFechaExcepcionOrderByHoraInicio(consultorioId, inicioLocal.toLocalDate());
        List<HorarioConsultorio> vigentes = excepciones.isEmpty()
                ? horarios.findByConsultorio_IdAndFechaExcepcionIsNullAndDiaSemanaOrderByHoraInicio(
                        consultorioId, inicioLocal.getDayOfWeek().getValue())
                : excepciones;
        boolean disponible = vigentes.stream().anyMatch(h -> h.isDisponible()
                && !inicioLocal.toLocalTime().isBefore(h.getHoraInicio())
                && !finLocal.toLocalTime().isAfter(h.getHoraFin()));
        if (excepciones.stream().anyMatch(h -> !h.isDisponible())) disponible = false;
        if (!disponible) throw new ConflictoAgendaException("La cita está fuera del horario del consultorio");
    }

    private Consultorio obtenerConsultorio(TipoCita tipo, UUID id) {
        if (tipo == TipoCita.DOMICILIO) return null;
        return consultorios.findById(id)
                .orElseThrow(() -> new RecursoAgendaNoEncontradoException("No se encontró el consultorio"));
    }

    private void validarRango(OffsetDateTime desde, OffsetDateTime hasta) {
        if (desde == null || hasta == null || !hasta.isAfter(desde)) {
            throw new SolicitudAgendaInvalidaException("El rango debe incluir inicio y fin, con fin posterior");
        }
    }

    private Cita guardar(Cita cita) {
        try {
            return citas.saveAndFlush(cita);
        } catch (DataIntegrityViolationException e) {
            if (esViolacionExclusion(e)) {
                throw new ConflictoAgendaException("La cita se empalma con otra cita activa");
            }
            throw e;
        }
    }

    private boolean esViolacionExclusion(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && "23P01".equals(sql.getSQLState())) return true;
        }
        return false;
    }

    private Cita buscarCita(UUID id) {
        return citas.findById(id)
                .orElseThrow(() -> new RecursoAgendaNoEncontradoException("No se encontró la cita"));
    }

    private CitaResponse respuesta(Cita c) {
        return new CitaResponse(
                c.getId(), c.getMascotaId(), c.getVeterinarioId(), c.getTipo(),
                c.getConsultorio() == null ? null : c.getConsultorio().getId(), c.getDireccion(),
                c.getInicio(), c.getFin(), c.getMotivo(), c.getEstado(), c.getCanceladaPor(),
                c.getMotivoCancelacion(), c.getReprogramadaDe() == null ? null : c.getReprogramadaDe().getId());
    }

    private BloqueoAgendaResponse respuesta(BloqueoAgenda b) {
        return new BloqueoAgendaResponse(
                b.getId(), b.getConsultorio() == null ? null : b.getConsultorio().getId(),
                b.getVeterinarioId(), b.getInicio(), b.getFin(), b.getMotivo(), b.getCreadoPor());
    }
}
