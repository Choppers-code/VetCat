package com.vetcat.propietarios.service;

/**
 * El propietario no existe en la clínica activa. El mensaje es el mismo si pertenece a otra, para
 * no revelar que existe.
 */
public class PropietarioNoEncontradoException extends RuntimeException {

  public PropietarioNoEncontradoException() {
    super("Propietario no encontrado");
  }
}
