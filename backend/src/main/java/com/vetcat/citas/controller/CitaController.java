package com.vetcat.citas.controller;

import com.vetcat.citas.dto.ApiResponse;
import com.vetcat.citas.dto.CambiarEstadoCitaRequest;
import com.vetcat.citas.dto.CitaRequest;
import com.vetcat.citas.dto.CitaResponse;
import com.vetcat.citas.dto.ReprogramarCitaRequest;
import com.vetcat.citas.entity.EstadoCita;
import com.vetcat.citas.service.AgendaService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/citas")
@PreAuthorize("hasAnyRole('administrador_clinica','recepcionista','veterinario')")
public class CitaController {
    private final AgendaService agenda;

    public CitaController(AgendaService agenda) {
        this.agenda = agenda;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CitaResponse>> crear(@Valid @RequestBody CitaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cita creada", agenda.crearCita(request)));
    }

    @GetMapping
    public ApiResponse<List<CitaResponse>> buscar(
            @RequestParam(required = false) OffsetDateTime desde,
            @RequestParam(required = false) OffsetDateTime hasta,
            @RequestParam(required = false) UUID consultorioId,
            @RequestParam(required = false) UUID veterinarioId,
            @RequestParam(required = false) EstadoCita estado) {
        return ApiResponse.ok("Citas encontradas",
                agenda.buscarCitas(desde, hasta, consultorioId, veterinarioId, estado));
    }

    @PatchMapping("/{id}/reprogramacion")
    public ApiResponse<CitaResponse> reprogramar(
            @PathVariable UUID id,
            @Valid @RequestBody ReprogramarCitaRequest request,
            Principal principal) {
        return ApiResponse.ok("Cita reprogramada",
                agenda.reprogramar(id, request, principal.getName()));
    }

    @PatchMapping("/{id}/estado")
    public ApiResponse<CitaResponse> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody CambiarEstadoCitaRequest request,
            Principal principal) {
        return ApiResponse.ok("Estado de cita actualizado",
                agenda.cambiarEstado(id, request, principal.getName()));
    }
}
