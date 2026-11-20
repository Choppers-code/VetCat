package com.vetcat.citas.controller;

import com.vetcat.citas.dto.ApiResponse;
import com.vetcat.citas.dto.ConsultorioRequest;
import com.vetcat.citas.dto.ConsultorioResponse;
import com.vetcat.citas.dto.HorarioConsultorioRequest;
import com.vetcat.citas.dto.HorarioConsultorioResponse;
import com.vetcat.citas.service.ConsultorioService;
import com.vetcat.citas.service.HorarioConsultorioService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/consultorios")
@PreAuthorize("hasAnyRole('administrador_clinica','recepcionista')")
public class ConsultorioController {
    private final ConsultorioService consultorioService;
    private final HorarioConsultorioService horarioService;

    public ConsultorioController(
            ConsultorioService consultorioService, HorarioConsultorioService horarioService) {
        this.consultorioService = consultorioService;
        this.horarioService = horarioService;
    }

    @GetMapping
    public ApiResponse<List<ConsultorioResponse>> listar() {
        return ApiResponse.ok("Consultorios encontrados", consultorioService.listar());
    }

    @GetMapping("/{id}")
    public ApiResponse<ConsultorioResponse> obtener(@PathVariable UUID id) {
        return ApiResponse.ok("Consultorio encontrado", consultorioService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConsultorioResponse>> crear(
            @Valid @RequestBody ConsultorioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Consultorio creado", consultorioService.crear(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<ConsultorioResponse> actualizar(
            @PathVariable UUID id, @Valid @RequestBody ConsultorioRequest request) {
        return ApiResponse.ok("Consultorio actualizado", consultorioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<ConsultorioResponse> darDeBaja(@PathVariable UUID id) {
        return ApiResponse.ok("Consultorio dado de baja", consultorioService.darDeBaja(id));
    }

    @PatchMapping("/{id}/estado")
    public ApiResponse<ConsultorioResponse> cambiarEstado(
            @PathVariable UUID id, @RequestParam boolean activo) {
        return ApiResponse.ok("Estado de consultorio actualizado",
                consultorioService.cambiarEstado(id, activo));
    }

    @GetMapping("/{id}/horarios")
    public ApiResponse<List<HorarioConsultorioResponse>> listarHorarios(@PathVariable UUID id) {
        return ApiResponse.ok("Horarios encontrados", horarioService.listar(id));
    }

    @PostMapping("/{id}/horarios")
    public ResponseEntity<ApiResponse<HorarioConsultorioResponse>> crearHorario(
            @PathVariable UUID id, @Valid @RequestBody HorarioConsultorioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Horario creado", horarioService.crear(id, request)));
    }

    @PutMapping("/{id}/horarios/{horarioId}")
    public ApiResponse<HorarioConsultorioResponse> actualizarHorario(
            @PathVariable UUID id,
            @PathVariable UUID horarioId,
            @Valid @RequestBody HorarioConsultorioRequest request) {
        return ApiResponse.ok("Horario actualizado",
                horarioService.actualizar(id, horarioId, request));
    }

    @DeleteMapping("/{id}/horarios/{horarioId}")
    public ApiResponse<Void> eliminarHorario(@PathVariable UUID id, @PathVariable UUID horarioId) {
        horarioService.eliminar(id, horarioId);
        return ApiResponse.ok("Horario eliminado", null);
    }
}
