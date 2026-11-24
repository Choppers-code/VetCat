package com.vetcat.plataforma.service;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Implementación provisional del puerto: falla cerrada hasta que exista la real (fase 3).
 *
 * <p>Un alta sin administrador dejaría una clínica a la que nadie puede entrar, así que es mejor
 * que el alta falle y se deshaga.
 */
@Component
class RegistroAdministradorPendiente implements RegistroAdministrador {

  @Override
  public UUID registrar(Long clinicaId, String nombre, String correo) {
    throw new IllegalStateException("El registro del administrador en Keycloak aún no existe");
  }
}
