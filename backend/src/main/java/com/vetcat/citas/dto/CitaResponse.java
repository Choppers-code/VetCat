package com.vetcat.citas.dto;

import com.vetcat.citas.entity.EstadoCita;
import com.vetcat.citas.entity.TipoCita;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CitaResponse(
        UUID id,
        UUID mascotaId,
        UUID veterinarioId,
        TipoCita tipo,
        UUID consultorioId,
        String direccion,
        OffsetDateTime inicio,
        OffsetDateTime fin,
        String motivo,
        EstadoCita estado,
        String canceladaPor,
        String motivoCancelacion,
        UUID reprogramadaDeId) {
}
