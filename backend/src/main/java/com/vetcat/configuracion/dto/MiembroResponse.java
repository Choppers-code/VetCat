package com.vetcat.configuracion.dto;

import java.util.List;
import java.util.UUID;

/** Miembro del personal de una clínica con la unión de sus roles de personal. */
public record MiembroResponse(UUID keycloakId, List<String> roles, String estado) {}
