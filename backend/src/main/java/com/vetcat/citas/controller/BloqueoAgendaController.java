package com.vetcat.citas.controller;

import com.vetcat.citas.dto.ApiResponse;
import com.vetcat.citas.dto.BloqueoAgendaRequest;
import com.vetcat.citas.dto.BloqueoAgendaResponse;
import com.vetcat.citas.service.AgendaService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agenda/bloqueos")
@PreAuthorize("hasAnyRole('administrador_clinica','recepcionista','veterinario')")
public class BloqueoAgendaController {
    private final AgendaService agenda;

    public BloqueoAgendaController(AgendaService agenda) {
        this.agenda = agenda;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BloqueoAgendaResponse>> crear(
            @Valid @RequestBody BloqueoAgendaRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Bloqueo creado", agenda.crearBloqueo(request, principal.getName())));
    }

    @GetMapping
    public ApiResponse<List<BloqueoAgendaResponse>> buscar(
            @RequestParam OffsetDateTime desde,
            @RequestParam OffsetDateTime hasta,
            @RequestParam(required = false) UUID consultorioId,
            @RequestParam(required = false) UUID veterinarioId) {
        return ApiResponse.ok("Bloqueos encontrados",
                agenda.buscarBloqueos(desde, hasta, consultorioId, veterinarioId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> eliminar(@PathVariable UUID id) {
        agenda.eliminarBloqueo(id);
        return ApiResponse.ok("Bloqueo eliminado", null);
    }
}
