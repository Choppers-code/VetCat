package com.vetcat.mascotas.controller;

import com.vetcat.mascotas.dto.MascotaActualizacionRequest;
import com.vetcat.mascotas.dto.MascotaRequest;
import com.vetcat.mascotas.dto.MascotaResponse;
import com.vetcat.mascotas.service.MascotaService;
import com.vetcat.shared.Sobre;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Mascotas de la clínica del token. El {@code cliente} no usa estos endpoints. */
@RestController
@RequestMapping("/api/v1/mascotas")
@PreAuthorize("hasAnyRole('clinica_admin', 'recepcionista', 'veterinario')")
public class MascotaController {

  private final MascotaService mascotas;

  public MascotaController(MascotaService mascotas) {
    this.mascotas = mascotas;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Sobre<MascotaResponse> registrar(@Valid @RequestBody MascotaRequest datos) {
    return Sobre.ok(mascotas.registrar(datos));
  }

  @GetMapping("/{id}")
  public Sobre<MascotaResponse> consultar(@PathVariable long id) {
    return Sobre.ok(mascotas.consultar(id));
  }

  @PutMapping("/{id}")
  public Sobre<MascotaResponse> editar(
      @PathVariable long id, @Valid @RequestBody MascotaActualizacionRequest datos) {
    return Sobre.ok(mascotas.editar(id, datos));
  }
}
