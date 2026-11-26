package com.vetcat.shared;

import com.vetcat.configuracion.service.AutoasignacionDeRolesException;
import com.vetcat.configuracion.service.MiembroExistenteException;
import com.vetcat.mascotas.service.MascotaNoEncontradaException;
import com.vetcat.propietarios.service.DireccionNoDisponibleException;
import com.vetcat.propietarios.service.PropietarioNoEncontradoException;
import com.vetcat.propietarios.service.TelefonoDuplicadoException;
import com.vetcat.security.TokenTenantInvalidoException;
import com.vetcat.sesion.SinAccesoALaClinicaException;
import java.util.Comparator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

  @ExceptionHandler(PropietarioNoEncontradoException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Sobre<Void> noEncontrado(PropietarioNoEncontradoException excepcion) {
    return Sobre.error(excepcion.getMessage());
  }

  @ExceptionHandler(MascotaNoEncontradaException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Sobre<Void> mascotaNoEncontrada(MascotaNoEncontradaException excepcion) {
    return Sobre.error(excepcion.getMessage());
  }

  @ExceptionHandler(TelefonoDuplicadoException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Sobre<Void> duplicado(TelefonoDuplicadoException excepcion) {
    return Sobre.error(excepcion.getMessage());
  }

  @ExceptionHandler(DireccionNoDisponibleException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
  public Sobre<Void> direccionNoDisponible(DireccionNoDisponibleException excepcion) {
    return Sobre.error(excepcion.getMessage());
  }

  @ExceptionHandler(AutoasignacionDeRolesException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Sobre<Void> autoasignacion(AutoasignacionDeRolesException excepcion) {
    return Sobre.error(excepcion.getMessage());
  }

  @ExceptionHandler(MiembroExistenteException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Sobre<Void> miembroExistente(MiembroExistenteException excepcion) {
    return Sobre.error(excepcion.getMessage());
  }

  /** Entrada que no supera {@code @Valid}: se informa el primer campo, en orden alfabético. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
  public Sobre<Void> entradaInvalida(MethodArgumentNotValidException excepcion) {
    String mensaje =
        excepcion.getBindingResult().getFieldErrors().stream()
            .sorted(Comparator.comparing(FieldError::getField))
            // Un fallo de conversión trae nombres de clases Java: no sale al cliente.
            .map(e -> e.isBindingFailure() ? "La entrada no es válida" : e.getDefaultMessage())
            .findFirst()
            .orElse("La entrada no es válida");
    return Sobre.error(mensaje);
  }

  /** Cuerpo que no se puede leer: el motivo técnico no sale al cliente. */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
  public Sobre<Void> cuerpoIlegible(HttpMessageNotReadableException excepcion) {
    return Sobre.error("La entrada no es válida");
  }

  @ExceptionHandler(AgendaException.class)
  public ResponseEntity<Sobre<Void>> agenda(AgendaException excepcion) {
    return ResponseEntity.status(excepcion.getEstado()).body(Sobre.error(excepcion.getMessage()));
  }
}
