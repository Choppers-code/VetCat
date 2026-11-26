package com.vetcat.mascotas.service;

/**
 * La mascota no existe en la clínica activa. El mensaje es el mismo si pertenece a otra, para no
 * revelar que existe.
 */
public class MascotaNoEncontradaException extends RuntimeException {

  public MascotaNoEncontradaException() {
    super("Mascota no encontrada");
  }
}
