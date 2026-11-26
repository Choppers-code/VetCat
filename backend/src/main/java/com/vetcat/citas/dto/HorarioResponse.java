package com.vetcat.citas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record HorarioResponse(
    Long id,
    Long consultorioId,
    boolean esCierre,
    boolean activo,
    Short diaSemana,
    LocalDate fecha,
    LocalTime horaInicio,
    LocalTime horaFin) {}
