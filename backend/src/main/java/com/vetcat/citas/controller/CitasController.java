package com.vetcat.citas.controller;

import com.vetcat.citas.dto.CitaRequest;
import com.vetcat.citas.dto.CitaResponse;
import com.vetcat.citas.service.CitasService;
import com.vetcat.shared.Sobre;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/citas")
public class CitasController {

  private final CitasService citas;

  public CitasController(CitasService citas) {
    this.citas = citas;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Sobre<CitaResponse> crear(
      @Valid @RequestBody CitaRequest request, @AuthenticationPrincipal Jwt identidad) {
    return Sobre.ok(citas.crear(request, identidad));
  }
}
