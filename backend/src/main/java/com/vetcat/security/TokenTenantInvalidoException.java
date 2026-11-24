package com.vetcat.security;

/** El token de tenant que llega no sirve: firma, emisor, caducidad o contenido (ADR-0003). */
public class TokenTenantInvalidoException extends RuntimeException {

  public TokenTenantInvalidoException(String mensaje) {
    super(mensaje);
  }

  public TokenTenantInvalidoException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}
