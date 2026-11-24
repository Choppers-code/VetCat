package com.vetcat.security;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

/** Emite y valida el token de tenant del ADR-0003: `sub`, `tenant_id`, roles, `jti` y 15 min. */
@Service
public class TokenTenantService {

  private final TokenTenantProperties propiedades;
  private final JwtEncoder codificador;
  private final JwtDecoder decodificador;
  private final Clock reloj;

  public TokenTenantService(
      TokenTenantProperties propiedades,
      @Qualifier("codificadorDeTokenTenant") JwtEncoder codificador,
      @Qualifier("decodificadorDeTokenTenant") JwtDecoder decodificador,
      Clock reloj) {
    this.propiedades = propiedades;
    this.codificador = codificador;
    this.decodificador = decodificador;
    this.reloj = reloj;
  }

  /**
   * Firma un token acotado a una clínica.
   *
   * @throws TokenTenantInvalidoException si no hay roles: un token sin roles no sirve para nada y
   *     no debe poder emitirse
   */
  public String emitir(String keycloakId, long clinicaId, List<String> roles) {
    if (keycloakId == null || keycloakId.isBlank()) {
      throw new TokenTenantInvalidoException("Sin usuario no hay token de tenant");
    }
    if (clinicaId <= 0) {
      throw new TokenTenantInvalidoException("Sin clínica no hay token de tenant");
    }
    if (roles == null || roles.isEmpty()) {
      throw new TokenTenantInvalidoException("Sin roles no hay token de tenant");
    }

    Instant ahora = reloj.instant();
    JwtClaimsSet reclamaciones =
        JwtClaimsSet.builder()
            .issuer(propiedades.emisor())
            .subject(keycloakId)
            .issuedAt(ahora)
            .expiresAt(ahora.plus(propiedades.vigencia()))
            .id(UUID.randomUUID().toString())
            .claim("tenant_id", clinicaId)
            .claim("roles", List.copyOf(roles))
            .build();
    JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).keyId(propiedades.kid()).build();

    return codificador.encode(JwtEncoderParameters.from(cabecera, reclamaciones)).getTokenValue();
  }

  /**
   * Valida firma, emisor y caducidad, y devuelve lo que el token dice.
   *
   * @throws TokenTenantInvalidoException si el token no sirve o no trae clínica y roles
   */
  public TokenTenant validar(String token) {
    try {
      Jwt jwt = decodificador.decode(token);
      Object crudo = jwt.getClaim("tenant_id");
      if (!(crudo instanceof Number numero)) {
        throw new TokenTenantInvalidoException("El token no trae tenant_id");
      }
      return new TokenTenant(
          jwt.getSubject(), numero.longValue(), jwt.getClaimAsStringList("roles"));
    } catch (JwtException excepcion) {
      throw new TokenTenantInvalidoException("Token de tenant inválido", excepcion);
    }
  }
}
