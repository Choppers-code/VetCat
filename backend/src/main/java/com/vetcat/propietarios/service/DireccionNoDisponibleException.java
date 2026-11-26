package com.vetcat.propietarios.service;

/**
 * La dirección exige la función {@code visitas_domicilio} del plan (PRO-003). Mientras no exista su
 * verificación, se rechaza siempre.
 */
public class DireccionNoDisponibleException extends RuntimeException {

  public DireccionNoDisponibleException() {
    super("La dirección no está disponible para esta clínica");
  }
}
