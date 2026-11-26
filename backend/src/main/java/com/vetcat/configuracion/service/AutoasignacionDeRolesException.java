package com.vetcat.configuracion.service;

/** Nadie puede añadirse roles a sí mismo: sería escalar sus propios privilegios (403). */
public class AutoasignacionDeRolesException extends RuntimeException {
  public AutoasignacionDeRolesException() {
    super("No puedes añadirte roles a ti mismo");
  }
}
