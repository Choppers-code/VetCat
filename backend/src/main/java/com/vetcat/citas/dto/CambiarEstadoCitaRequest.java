package com.vetcat.citas.dto;

import com.vetcat.citas.entity.EstadoCita;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambiarEstadoCitaRequest(
        @NotNull EstadoCita estado,
        @Size(max = 500) String motivoCancelacion) {
}
