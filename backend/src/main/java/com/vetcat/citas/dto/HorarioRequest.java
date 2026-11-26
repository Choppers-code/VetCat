package com.vetcat.citas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record HorarioRequest(
    Long consultorioId,
    Boolean esCierre,
    Boolean activo,
    Short diaSemana,
    LocalDate fecha,
    LocalTime horaInicio,
    LocalTime horaFin) {}
