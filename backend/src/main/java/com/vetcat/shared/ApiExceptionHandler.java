package com.vetcat.shared;

import com.vetcat.security.TokenTenantInvalidoException;
import com.vetcat.sesion.SinAccesoALaClinicaException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Todo error sale por aquí con el sobre y sin filtrar trazas ni nombres internos.
 *
 * <p>Crece con cada módulo: una excepción de negocio, un manejador.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(SinAccesoALaClinicaException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Sobre<Void> sinAcceso(SinAccesoALaClinicaException excepcion) {
    return Sobre.error(excepcion.getMessage());
  }

  @ExceptionHandler(TokenTenantInvalidoException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public Sobre<Void> tokenInvalido(TokenTenantInvalidoException excepcion) {
    return Sobre.error("La sesión no es válida");
  }
}
