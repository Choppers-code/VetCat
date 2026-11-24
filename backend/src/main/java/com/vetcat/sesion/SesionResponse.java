package com.vetcat.sesion;

import java.util.List;

/**
 * Token de tenant ya emitido, con lo necesario para que la app sepa en qué clínica está.
 *
 * @param token el JWT que la app manda en las peticiones de negocio
 */
public record SesionResponse(String token, Long clinicaId, String nombre, List<String> roles) {}
