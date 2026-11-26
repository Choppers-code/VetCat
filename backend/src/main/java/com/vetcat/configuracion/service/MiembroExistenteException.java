package com.vetcat.configuracion.service;

/** La persona ya es del personal; sus roles se cambian de forma explícita (409). */
public class MiembroExistenteException extends RuntimeException {
  public MiembroExistenteException() {
    super("Esa persona ya es parte del personal de la clínica");
  }
}
