package com.vetcat.propietarios.controller;

import com.vetcat.propietarios.dto.BusquedaRequest;
import com.vetcat.propietarios.dto.ResultadoBusquedaResponse;
import com.vetcat.propietarios.service.BusquedaService;
import com.vetcat.shared.PaginaResponse;
import com.vetcat.shared.Sobre;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Búsqueda de propietarios y mascotas de la clínica del token. */
@RestController
@RequestMapping("/api/v1/busqueda")
public class BusquedaController {

  private final BusquedaService busqueda;

  public BusquedaController(BusquedaService busqueda) {
    this.busqueda = busqueda;
  }

  /** Ver inactivos es solo del {@code clinica_admin}; a los demás se les niega, no se ignora. */
  @GetMapping
  @PreAuthorize(
      "hasRole('clinica_admin') or "
          + "(hasAnyRole('recepcionista', 'veterinario') and !#peticion.incluirInactivos())")
  public Sobre<PaginaResponse<ResultadoBusquedaResponse>> buscar(
      @Valid @ModelAttribute BusquedaRequest peticion) {
    return Sobre.ok(busqueda.buscar(peticion));
  }
}
