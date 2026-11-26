package com.vetcat.tenancy;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Tenant de la operación en curso, ligado al hilo y nunca a un bean.
 *
 * <p>Ningún singleton guarda el tenant en un campo (docs/Decisiones_tecnicas.md §2). El valor lo
 * fija solo el código que ya lo validó contra {@code catalogo.usuario_clinica}, y siempre se
 * restablece en un {@code finally}.
 */
public final class TenantContext {

  private static final ThreadLocal<Long> ACTUAL = new ThreadLocal<>();

  private TenantContext() {}

  /** Ejecuta la operación con el tenant indicado y restablece el anterior al terminar. */
  public static <T> T ejecutar(long tenantId, Supplier<T> operacion) {
    Long anterior = ACTUAL.get();
    ACTUAL.set(tenantId);
    try {
      return operacion.get();
    } finally {
      if (anterior == null) {
        ACTUAL.remove();
      } else {
        ACTUAL.set(anterior);
      }
    }
  }

  /** Tenant del hilo actual, o vacío si no hay ninguno. */
  public static Optional<Long> actual() {
    return Optional.ofNullable(ACTUAL.get());
  }

  /**
   * Fija el tenant del hilo y devuelve el anterior, para poder restaurarlo.
   *
   * <p>Lo usa el filtro web, que necesita fijarlo y restaurarlo alrededor de una cadena que lanza
   * excepciones comprobadas; {@link #ejecutar(long, Supplier)} no sirve ahí.
   */
  static Long fijar(long tenantId) {
    Long anterior = ACTUAL.get();
    ACTUAL.set(tenantId);
    return anterior;
  }

  /** Restablece el tenant anterior; {@code null} deja el hilo sin tenant. */
  static void restaurar(Long anterior) {
    if (anterior == null) {
      ACTUAL.remove();
    } else {
      ACTUAL.set(anterior);
    }
  }
}
