package com.vetcat.citas.service;

import com.vetcat.citas.dto.ConsultorioRequest;
import com.vetcat.citas.dto.ConsultorioResponse;
import com.vetcat.citas.entity.Consultorio;
import com.vetcat.citas.exception.RecursoAgendaNoEncontradoException;
import com.vetcat.citas.repository.ConsultorioRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConsultorioService {
    private final ConsultorioRepository consultorios;

    public ConsultorioService(ConsultorioRepository consultorios) {
        this.consultorios = consultorios;
    }

    @Transactional(readOnly = true)
    public List<ConsultorioResponse> listar() {
        return consultorios.findAll().stream().map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public ConsultorioResponse obtener(UUID id) {
        return respuesta(buscar(id));
    }

    public ConsultorioResponse crear(ConsultorioRequest request) {
        Consultorio consultorio = new Consultorio(request.nombre().trim(), request.descripcion());
        if (request.activo() != null) consultorio.setActivo(request.activo());
        return respuesta(consultorios.save(consultorio));
    }

    public ConsultorioResponse actualizar(UUID id, ConsultorioRequest request) {
        Consultorio consultorio = buscar(id);
        consultorio.setNombre(request.nombre().trim());
        consultorio.setDescripcion(request.descripcion());
        if (request.activo() != null) consultorio.setActivo(request.activo());
        return respuesta(consultorios.save(consultorio));
    }

    public ConsultorioResponse darDeBaja(UUID id) {
        return cambiarEstado(id, false);
    }

    public ConsultorioResponse cambiarEstado(UUID id, boolean activo) {
        Consultorio consultorio = buscar(id);
        consultorio.setActivo(activo);
        return respuesta(consultorios.save(consultorio));
    }

    private Consultorio buscar(UUID id) {
        return consultorios.findById(id)
                .orElseThrow(() -> new RecursoAgendaNoEncontradoException("No se encontró el consultorio"));
    }

    private ConsultorioResponse respuesta(Consultorio c) {
        return new ConsultorioResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.isActivo());
    }
}
