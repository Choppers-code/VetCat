package com.vetcat.citas.dto;

import java.time.Instant;

public record CitaResponse(
    Long id,
    Long mascotaId,
    Long veterinarioId,
    String tipo,
    Long consultorioId,
    Instant inicio,
    Instant fin,
    String estado,
    String motivo) {}
