package com.vetcat.propietarios.controller;

import com.vetcat.propietarios.dto.PropietarioRequest;
import com.vetcat.propietarios.dto.PropietarioResponse;
import com.vetcat.propietarios.service.PropietarioService;
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

/**
 * Propietarios de la clínica del token. El {@code cliente} no usa estos endpoints.
 *
 * <p>La entrada se valida antes que el permiso: un {@code cliente} con datos inválidos recibe 422 y
 * con datos válidos 403. Es deliberado, porque solo revela reglas de validación.
 */
@RestController
@RequestMapping("/api/v1/propietarios")
@PreAuthorize("hasAnyRole('clinica_admin', 'recepcionista', 'veterinario')")
public class PropietarioController {

  private final PropietarioService propietarios;

  public PropietarioController(PropietarioService propietarios) {
    this.propietarios = propietarios;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Sobre<PropietarioResponse> registrar(@Valid @RequestBody PropietarioRequest datos) {
    return Sobre.ok(propietarios.registrar(datos));
  }

  @GetMapping("/{id}")
  public Sobre<PropietarioResponse> consultar(@PathVariable long id) {
    return Sobre.ok(propietarios.consultar(id));
  }

  @PutMapping("/{id}")
  public Sobre<PropietarioResponse> editar(
      @PathVariable long id, @Valid @RequestBody PropietarioRequest datos) {
    return Sobre.ok(propietarios.editar(id, datos));
  }
}
