package com.vetcat.propietarios.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vetcat.mascotas.dto.MascotaResponse;
import java.util.List;

/**
 * Ficha de un propietario con sus mascotas activas. Los opcionales vacíos no se envían.
 *
 * <p>La dirección se incorpora con la verificación de funciones del plan (PRO-003).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PropietarioResponse(
    Long id,
    String nombre,
    String telefono,
    String correo,
    boolean activo,
    List<MascotaResponse> mascotas) {}
