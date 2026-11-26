package com.vetcat.propietarios.service;

/** Otro propietario ya tiene ese teléfono y la petición no confirmó el duplicado (PRO-001). */
public class TelefonoDuplicadoException extends RuntimeException {

  public TelefonoDuplicadoException() {
    super("Ya existe un propietario con ese teléfono; confirma para guardarlo de todos modos");
  }
}
