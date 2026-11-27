package com.vetcat.citas.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record CitaRequest(
    @NotNull @Positive Long mascotaId,
    @NotNull @Positive Long veterinarioId,
    @NotNull @Positive Long consultorioId,
    @NotNull OffsetDateTime inicio,
    @NotNull @Min(1) @Max(1440) Integer duracionMinutos,
    @Size(max = 500) String motivo) {}
