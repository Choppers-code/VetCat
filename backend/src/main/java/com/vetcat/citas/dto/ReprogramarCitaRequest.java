package com.vetcat.citas.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record ReprogramarCitaRequest(
        @NotNull @Future OffsetDateTime inicio,
        @NotNull OffsetDateTime fin,
        @Size(max = 500) String motivoCancelacion) {

    @AssertTrue(message = "La fecha final debe ser posterior a la inicial")
    public boolean isRangoValido() {
        return inicio == null || fin == null || fin.isAfter(inicio);
    }
}
