package com.vetcat.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Convierte los roles del token en autoridades `ROLE_*`.
 *
 * <p>En el token de tenant los roles vienen en `roles`. En el de Keycloak, en `realm_access.roles`;
 * de ahí solo interesa `super_admin`, que es el único rol que no pertenece a una clínica.
 */
public final class ConversionDeRoles {

  private ConversionDeRoles() {}

  /** Autoridades del token de tenant: los roles efectivos de la clínica. */
  public static Converter<Jwt, AbstractAuthenticationToken> deTenant() {
    return jwt -> new JwtAuthenticationToken(jwt, autoridades(jwt.getClaimAsStringList("roles")));
  }

  /** Autoridades del token de identidad: solo los roles de realm. */
  public static Converter<Jwt, AbstractAuthenticationToken> deIdentidad() {
    return jwt -> new JwtAuthenticationToken(jwt, autoridades(rolesDeRealm(jwt)));
  }

  private static List<String> rolesDeRealm(Jwt jwt) {
    Map<String, Object> acceso = jwt.getClaim("realm_access");
    if (acceso == null || !(acceso.get("roles") instanceof Collection<?> roles)) {
      return List.of();
    }
    return roles.stream().filter(String.class::isInstance).map(String.class::cast).toList();
  }

  private static Collection<GrantedAuthority> autoridades(List<String> roles) {
    if (roles == null) {
      return List.of();
    }
    return roles.stream()
        .map(rol -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + rol))
        .toList();
  }
}
