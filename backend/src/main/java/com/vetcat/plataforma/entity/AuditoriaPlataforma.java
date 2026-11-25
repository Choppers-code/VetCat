package com.vetcat.plataforma.entity;

import java.time.Instant;
import java.util.UUID;

/** Una fila de la auditoría de plataforma, ya leída. */
public record AuditoriaPlataforma(
    Long id,
    UUID actorKeycloakId,
    AccionDePlataforma accion,
    Long clinicaId,
    String detalle,
    Instant creadaEn) {}
