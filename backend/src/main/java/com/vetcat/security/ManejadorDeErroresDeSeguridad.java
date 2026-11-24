package com.vetcat.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 401 y 403 con el sobre de error del proyecto: `{success, message, data}`.
 *
 * <p>Nunca se devuelve una traza, ni el nombre de una clase, ni el motivo técnico del rechazo.
 */
@Component
public class ManejadorDeErroresDeSeguridad
    implements AuthenticationEntryPoint, AccessDeniedHandler {

  private static final String CUERPO_401 =
      "{\"success\":false,\"message\":\"No autenticado o sesión caducada\",\"data\":null}";
  private static final String CUERPO_403 =
      "{\"success\":false,\"message\":\"No tienes permiso para esta acción\",\"data\":null}";

  @Override
  public void commence(
      HttpServletRequest peticion, HttpServletResponse respuesta, AuthenticationException excepcion)
      throws IOException {
    escribir(respuesta, 401, CUERPO_401);
  }

  @Override
  public void handle(
      HttpServletRequest peticion, HttpServletResponse respuesta, AccessDeniedException excepcion)
      throws IOException {
    escribir(respuesta, 403, CUERPO_403);
  }

  private static void escribir(HttpServletResponse respuesta, int estado, String cuerpo)
      throws IOException {
    respuesta.setStatus(estado);
    respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
    respuesta.setCharacterEncoding("UTF-8");
    respuesta.getWriter().write(cuerpo);
  }
}
