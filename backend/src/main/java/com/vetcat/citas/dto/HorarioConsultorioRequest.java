package com.vetcat.citas.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record HorarioConsultorioRequest(
        @Min(1) @Max(7) Integer diaSemana,
        LocalDate fechaExcepcion,
        LocalTime horaInicio,
        LocalTime horaFin,
        @NotNull Boolean disponible) {

    @AssertTrue(message = "El horario debe definir día semanal o fecha y un intervalo válido")
    public boolean isDefinicionValida() {
        if (disponible == null || ((fechaExcepcion == null) == (diaSemana == null))) return false;
        if (!disponible) return horaInicio == null && horaFin == null;
        return horaInicio != null && horaFin != null && horaFin.isAfter(horaInicio);
    }
}
