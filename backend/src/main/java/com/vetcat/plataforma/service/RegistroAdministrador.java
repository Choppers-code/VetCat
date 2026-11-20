package com.vetcat.plataforma.service;

import java.util.UUID;

/**
 * Registra al administrador de una clínica en Keycloak (CLI-002).
 *
 * <p>Es un puerto: la implementación real llega con la fase de autenticación, que es la que crea el
 * realm y el cliente de la Admin API. Debe ser idempotente por correo, porque el aprovisionamiento
 * puede reintentarse.
 */
public interface RegistroAdministrador {

  /** Crea (o reutiliza) al usuario y le envía el correo para fijar su contraseña. */
  UUID registrar(Long clinicaId, String nombre, String correo);
}
