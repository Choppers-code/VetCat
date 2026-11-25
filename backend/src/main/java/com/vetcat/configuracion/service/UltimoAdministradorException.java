package com.vetcat.configuracion.service;

/** La operación dejaría a la clínica sin ningún {@code clinica_admin} activo (409, CON-003). */
public class UltimoAdministradorException extends RuntimeException {
  public UltimoAdministradorException() {
    super("La clínica debe conservar al menos un administrador activo");
  }
}
