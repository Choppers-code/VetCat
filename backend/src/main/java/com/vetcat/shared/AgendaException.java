package com.vetcat.shared;

import org.springframework.http.HttpStatus;

public class AgendaException extends RuntimeException {

  private final HttpStatus estado;

  public AgendaException(HttpStatus estado, String mensaje) {
    super(mensaje);
    this.estado = estado;
  }

  public HttpStatus getEstado() {
    return estado;
  }
}
