package com.vetcat.security;

import java.util.List;

/**
 * Lo que lleva un token de tenant ya validado: quién es, en qué clínica y con qué roles.
 *
 * @param sujeto el `keycloak_id` del usuario
 * @param clinicaId identificador interno de `catalogo.clinica`, nunca el nombre del esquema
 * @param roles roles efectivos de esa clínica, leídos de `catalogo.usuario_clinica`
 */
public record TokenTenant(String sujeto, long clinicaId, List<String> roles) {

  public TokenTenant {
    if (sujeto == null || sujeto.isBlank()) {
      throw new TokenTenantInvalidoException("El token no trae sujeto");
    }
    if (clinicaId <= 0) {
      throw new TokenTenantInvalidoException("El token no trae una clínica válida");
    }
    if (roles == null || roles.isEmpty()) {
      throw new TokenTenantInvalidoException("El token no trae roles para la clínica");
    }
    roles = List.copyOf(roles);
  }
}
