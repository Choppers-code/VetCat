package com.vetcat.citas.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record HorarioConsultorioResponse(
        UUID id,
        UUID consultorioId,
        Integer diaSemana,
        LocalDate fechaExcepcion,
        LocalTime horaInicio,
        LocalTime horaFin,
        boolean disponible) {
}
