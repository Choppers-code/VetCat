package com.vetcat.citas.exception;

import com.vetcat.citas.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;

@RestControllerAdvice(basePackages = "com.vetcat.citas")
public class AgendaExceptionHandler {

    @ExceptionHandler(ConflictoAgendaException.class)
    public ResponseEntity<ApiResponse<Void>> conflicto(ConflictoAgendaException e) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RecursoAgendaNoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> noEncontrado(RecursoAgendaNoEncontradoException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({
            SolicitudAgendaInvalidaException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ApiResponse<Void>> solicitudInvalida(Exception e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiResponse<Void>> formatoInvalido(Exception e) {
        return respuesta(HttpStatus.BAD_REQUEST, "La solicitud contiene un valor o formato inválido");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    private ResponseEntity<ApiResponse<Void>> respuesta(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.error(message));
    }
}
