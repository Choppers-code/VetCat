package com.vetcat.citas.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BloqueoAgendaRequest(
        UUID consultorioId,
        UUID veterinarioId,
        @NotNull @Future OffsetDateTime inicio,
        @NotNull OffsetDateTime fin,
        @NotBlank @Size(max = 300) String motivo) {

    @AssertTrue(message = "La fecha final debe ser posterior a la inicial")
    public boolean isRangoValido() {
        return inicio == null || fin == null || fin.isAfter(inicio);
    }
}
