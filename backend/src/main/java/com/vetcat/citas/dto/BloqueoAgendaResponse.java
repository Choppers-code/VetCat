package com.vetcat.citas.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BloqueoAgendaResponse(
        UUID id,
        UUID consultorioId,
        UUID veterinarioId,
        OffsetDateTime inicio,
        OffsetDateTime fin,
        String motivo,
        String creadoPor) {
}
