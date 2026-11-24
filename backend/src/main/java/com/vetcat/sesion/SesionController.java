package com.vetcat.sesion;

import com.vetcat.shared.Sobre;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sesión: las clínicas del usuario y apertura de una de ellas (IDA-002, IDA-003).
 *
 * <p>Estos endpoints se autentican con el token de identidad de Keycloak; devuelven el token de
 * tenant, que es el único que abre el negocio.
 */
@RestController
@RequestMapping("/api/v1/sesion/clinicas")
public class SesionController {

  private final SesionService sesion;

  public SesionController(SesionService sesion) {
    this.sesion = sesion;
  }

  @GetMapping
  public Sobre<List<ClinicaDelUsuario>> clinicas(@AuthenticationPrincipal Jwt identidad) {
    return Sobre.ok(sesion.clinicas(usuario(identidad)));
  }

  @PostMapping("/{clinicaId}")
  public Sobre<SesionResponse> abrir(
      @AuthenticationPrincipal Jwt identidad, @PathVariable Long clinicaId) {
    return Sobre.ok(sesion.abrir(usuario(identidad), clinicaId));
  }

  private static UUID usuario(Jwt identidad) {
    return UUID.fromString(identidad.getSubject());
  }
}
