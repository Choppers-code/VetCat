package com.vetcat.citas.dto;

import java.util.UUID;

public record ConsultorioResponse(UUID id, String nombre, String descripcion, boolean activo) {
}
