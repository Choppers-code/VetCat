package com.vetcat.tenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Pone el tenant del token en el contexto del hilo durante la petición.
 *
 * <p>Corre después de la cadena de seguridad, así que el JWT ya está validado y su `tenant_id` es
 * de fiar. En las rutas de sesión y plataforma no hay clínica y el filtro se salta. Sin tenant en
 * una ruta de negocio, falla cerrado: 401 con el sobre y ningún esquema por defecto
 * (docs/Decisiones_tecnicas.md §2).
 */
@Component
public class FiltroDeTenant extends OncePerRequestFilter {

  /** Rutas que se autentican con el token de identidad y no tienen clínica. */
  private static final String[] RUTAS_SIN_TENANT = {"/api/v1/sesion", "/api/v1/plataforma"};

  private static final String CUERPO_401 =
      "{\"success\":false,\"message\":\"No autenticado o sesión caducada\",\"data\":null}";

  @Override
  protected void doFilterInternal(
      HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
      throws ServletException, IOException {
    if (esRutaSinTenant(peticion.getRequestURI())) {
      cadena.doFilter(peticion, respuesta);
      return;
    }

    Long tenant = tenantDeLaAutenticacion();
    if (tenant == null) {
      respuesta.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
      respuesta.setCharacterEncoding("UTF-8");
      respuesta.getWriter().write(CUERPO_401);
      return;
    }

    Long anterior = TenantContext.fijar(tenant);
    try {
      cadena.doFilter(peticion, respuesta);
    } finally {
      TenantContext.restaurar(anterior);
    }
  }

  private static boolean esRutaSinTenant(String ruta) {
    for (String prefijo : RUTAS_SIN_TENANT) {
      if (ruta.startsWith(prefijo)) {
        return true;
      }
    }
    return false;
  }

  private static Long tenantDeLaAutenticacion() {
    Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
    if (autenticacion instanceof JwtAuthenticationToken jwt) {
      Object valor = jwt.getToken().getClaim("tenant_id");
      if (valor instanceof Number numero) {
        return numero.longValue();
      }
    }
    return null;
  }
}
