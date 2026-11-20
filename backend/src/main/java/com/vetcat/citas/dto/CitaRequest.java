package com.vetcat.citas.dto;

import com.vetcat.citas.entity.TipoCita;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CitaRequest(
        @NotNull UUID mascotaId,
        @NotNull UUID veterinarioId,
        @NotNull TipoCita tipo,
        UUID consultorioId,
        @Size(max = 500) String direccion,
        @NotNull @Future OffsetDateTime inicio,
        @NotNull OffsetDateTime fin,
        String motivo) {

    @AssertTrue(message = "El intervalo o el lugar de la cita no es válido")
    public boolean isCitaValida() {
        if (tipo == null || inicio == null || fin == null || !fin.isAfter(inicio)) return false;
        return tipo == TipoCita.CONSULTORIO
                ? consultorioId != null
                : consultorioId == null && direccion != null && !direccion.isBlank();
    }
}
