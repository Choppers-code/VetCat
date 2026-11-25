package com.vetcat.sesion;

/** El usuario no tiene membresía activa en esa clínica, o la clínica no está disponible. */
public class SinAccesoALaClinicaException extends RuntimeException {

  public SinAccesoALaClinicaException(String mensaje) {
    super(mensaje);
  }
}
