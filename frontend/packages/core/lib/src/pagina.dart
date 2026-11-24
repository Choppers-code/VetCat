/// Página de resultados, con la forma del esquema `Paginacion` del contrato.
class Pagina<T> {
  const Pagina({
    required this.contenido,
    required this.pagina,
    required this.tamano,
    required this.total,
  });

  /// Lee la sección `data` de una respuesta paginada.
  factory Pagina.desdeJson(
    Object? datos,
    T Function(Object? elemento) convertir,
  ) {
    final mapa = datos as Map<String, Object?>;
    return Pagina<T>(
      contenido: (mapa['contenido'] as List<Object?>).map(convertir).toList(),
      pagina: mapa['pagina'] as int,
      tamano: mapa['tamano'] as int,
      total: mapa['total'] as int,
    );
  }

  final List<T> contenido;

  /// Número de página, empezando en 0.
  final int pagina;
  final int tamano;

  /// Total de elementos, no de páginas.
  final int total;
}
