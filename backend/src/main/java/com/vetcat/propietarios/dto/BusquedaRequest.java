package com.vetcat.propietarios.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Parámetros de la búsqueda. Los nombres son los del contrato.
 *
 * @param q texto a buscar, sin contar los espacios de los extremos
 * @param incluirInactivos solo lo admite el {@code clinica_admin}
 */
public record BusquedaRequest(
    @Size(min = 3, message = "Escribe al menos 3 caracteres") String q,
    Boolean incluirInactivos,
    @Min(value = 0, message = "La página empieza en 0") Integer page,
    @Min(value = 1, message = "El tamaño va de 1 a 100")
        @Max(value = 100, message = "El tamaño va de 1 a 100")
        Integer size) {

  public BusquedaRequest {
    q = q == null ? "" : q.strip();
    incluirInactivos = Boolean.TRUE.equals(incluirInactivos);
    page = page == null ? 0 : page;
    size = size == null ? 20 : size;
  }
}
