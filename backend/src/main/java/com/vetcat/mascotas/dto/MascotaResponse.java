package com.vetcat.mascotas.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vetcat.mascotas.entity.Especie;
import com.vetcat.mascotas.entity.Sexo;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos generales de una mascota; nada clínico. Los opcionales vacíos no se envían. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MascotaResponse(
    Long id,
    Long propietarioId,
    String nombre,
    Especie especie,
    String raza,
    Sexo sexo,
    LocalDate fechaNacimiento,
    BigDecimal pesoKg,
    String notas,
    boolean activo) {}
