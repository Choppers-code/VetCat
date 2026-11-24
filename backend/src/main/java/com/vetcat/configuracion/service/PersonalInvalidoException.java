package com.vetcat.configuracion.service;

/** La petición de personal no cumple las reglas: rol ausente o no permitido (422). */
public class PersonalInvalidoException extends RuntimeException {
  public PersonalInvalidoException(String mensaje) {
    super(mensaje);
  }
}
