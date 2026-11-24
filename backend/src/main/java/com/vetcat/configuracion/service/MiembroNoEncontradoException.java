package com.vetcat.configuracion.service;

/** No hay un miembro del personal con ese identificador en la clínica (404). */
public class MiembroNoEncontradoException extends RuntimeException {
  public MiembroNoEncontradoException() {
    super("Miembro del personal no encontrado");
  }
}
