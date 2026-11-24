package com.vetcat.shared;

/**
 * Sobre único de la API: `{success, message, data}` también cuando la respuesta es correcta.
 *
 * @param message mensaje para la persona, no para el log
 */
public record Sobre<T>(boolean success, String message, T data) {

  public static <T> Sobre<T> ok(T datos) {
    return new Sobre<>(true, "ok", datos);
  }

  public static <T> Sobre<T> error(String mensaje) {
    return new Sobre<>(false, mensaje, null);
  }
}
