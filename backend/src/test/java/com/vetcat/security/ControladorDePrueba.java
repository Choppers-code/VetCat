package com.vetcat.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint solo para probar la autorización: no existe en la aplicación. */
@RestController
public class ControladorDePrueba {

  @GetMapping("/api/v1/prueba")
  @PreAuthorize("hasRole('veterinario')")
  public String prueba() {
    return "ok";
  }
}
