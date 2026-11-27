package com.vetcat.shared;

import java.util.List;

/**
 * Página de una lista, con los nombres del contrato ({@code Paginacion}).
 *
 * @param pagina número de página, empezando en 0
 * @param total elementos de todas las páginas
 */
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long total) {}
