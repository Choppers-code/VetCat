package com.vetcat.citas.service;

import com.vetcat.citas.dto.HorarioConsultorioRequest;
import com.vetcat.citas.dto.HorarioConsultorioResponse;
import com.vetcat.citas.entity.Consultorio;
import com.vetcat.citas.entity.HorarioConsultorio;
import com.vetcat.citas.exception.RecursoAgendaNoEncontradoException;
import com.vetcat.citas.repository.ConsultorioRepository;
import com.vetcat.citas.repository.HorarioConsultorioRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HorarioConsultorioService {
    private final ConsultorioRepository consultorios;
    private final HorarioConsultorioRepository horarios;

    public HorarioConsultorioService(
            ConsultorioRepository consultorios, HorarioConsultorioRepository horarios) {
        this.consultorios = consultorios;
        this.horarios = horarios;
    }

    @Transactional(readOnly = true)
    public List<HorarioConsultorioResponse> listar(UUID consultorioId) {
        buscarConsultorio(consultorioId);
        return horarios.findByConsultorio_IdOrderByDiaSemanaAscFechaExcepcionAscHoraInicioAsc(consultorioId)
                .stream().map(this::respuesta).toList();
    }

    public HorarioConsultorioResponse crear(UUID consultorioId, HorarioConsultorioRequest request) {
        Consultorio consultorio = buscarConsultorio(consultorioId);
        return respuesta(horarios.save(new HorarioConsultorio(
                consultorio, request.diaSemana(), request.fechaExcepcion(),
                request.horaInicio(), request.horaFin(), request.disponible())));
    }

    public HorarioConsultorioResponse actualizar(
            UUID consultorioId, UUID horarioId, HorarioConsultorioRequest request) {
        buscarConsultorio(consultorioId);
        HorarioConsultorio horario = buscarHorario(consultorioId, horarioId);
        horario.setDiaSemana(request.diaSemana());
        horario.setFechaExcepcion(request.fechaExcepcion());
        horario.setHoraInicio(request.horaInicio());
        horario.setHoraFin(request.horaFin());
        horario.setDisponible(request.disponible());
        return respuesta(horarios.save(horario));
    }

    public void eliminar(UUID consultorioId, UUID horarioId) {
        buscarConsultorio(consultorioId);
        horarios.delete(buscarHorario(consultorioId, horarioId));
    }

    private Consultorio buscarConsultorio(UUID id) {
        return consultorios.findById(id)
                .orElseThrow(() -> new RecursoAgendaNoEncontradoException("No se encontró el consultorio"));
    }

    private HorarioConsultorio buscarHorario(UUID consultorioId, UUID horarioId) {
        return horarios.findById(horarioId)
                .filter(horario -> horario.getConsultorio().getId().equals(consultorioId))
                .orElseThrow(() -> new RecursoAgendaNoEncontradoException("No se encontró el horario"));
    }

    private HorarioConsultorioResponse respuesta(HorarioConsultorio h) {
        return new HorarioConsultorioResponse(
                h.getId(), h.getConsultorio().getId(), h.getDiaSemana(), h.getFechaExcepcion(),
                h.getHoraInicio(), h.getHoraFin(), h.isDisponible());
    }
}
