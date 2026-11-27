package com.vetcat.propietarios.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vetcat.mascotas.dto.MascotaResponse;

/** Un hallazgo: un propietario, o una mascota junto a su propietario (PRO-009). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResultadoBusquedaResponse(PropietarioResponse propietario, MascotaResponse mascota) {}
