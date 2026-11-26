package com.vetcat.tenancy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint solo para probar el filtro: devuelve el tenant del contexto. */
@RestController
public class ControladorDeTenantDePrueba {

  @GetMapping("/api/v1/prueba-tenant")
  public long tenant() {
    return TenantContext.actual().orElse(-1L);
  }
}
