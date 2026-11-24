/// Clases de error que la app distingue al hablar con la API.
enum TipoDeError {
  /// 401: falta el token o no es válido.
  noAutenticado,

  /// 403: el rol no permite la operación o la función no está en el plan.
  prohibido,

  /// 404: el recurso no existe en la clínica activa.
  noEncontrado,

  /// 409: conflicto con datos existentes (duplicados, horarios, citas).
  conflicto,

  /// 422: la entrada no supera la validación.
  entradaInvalida,

  /// 5xx: fallo del servidor.
  servidor,

  /// No hubo respuesta: sin red o tiempo agotado.
  sinConexion,

  /// Cualquier otro caso, incluida una respuesta que no es un sobre válido.
  desconocido,
}

/// Error de una llamada a la API, ya traducido del sobre. [mensaje] es el texto
/// en español del backend, o uno genérico del mismo [tipo] si no mandó ninguno.
class ErrorDeApi implements Exception {
  const ErrorDeApi({
    required this.tipo,
    required this.mensaje,
    this.codigoHttp,
  });

  final TipoDeError tipo;
  final String mensaje;

  /// Código HTTP, o `null` si no hubo respuesta.
  final int? codigoHttp;

  /// Tipo de error que corresponde a un código HTTP.
  static TipoDeError tipoPorEstado(int? estado) {
    if (estado == null) return TipoDeError.desconocido;
    return switch (estado) {
      401 => TipoDeError.noAutenticado,
      403 => TipoDeError.prohibido,
      404 => TipoDeError.noEncontrado,
      409 => TipoDeError.conflicto,
      422 => TipoDeError.entradaInvalida,
      >= 500 && < 600 => TipoDeError.servidor,
      _ => TipoDeError.desconocido,
    };
  }

  /// Texto genérico para cuando el backend no manda un mensaje.
  static String mensajePorTipo(TipoDeError tipo) {
    return switch (tipo) {
      TipoDeError.noAutenticado =>
        'Tu sesión no es válida. Inicia sesión de nuevo.',
      TipoDeError.prohibido => 'No tienes permiso para hacer esto.',
      TipoDeError.noEncontrado => 'No se encontró lo que buscas.',
      TipoDeError.conflicto =>
        'La operación entra en conflicto con datos existentes.',
      TipoDeError.entradaInvalida => 'Revisa los datos e inténtalo de nuevo.',
      TipoDeError.servidor =>
        'El servidor tuvo un problema. Inténtalo más tarde.',
      TipoDeError.sinConexion =>
        'No hay conexión con el servidor. Revisa tu conexión.',
      TipoDeError.desconocido => 'Ocurrió un error inesperado.',
    };
  }

  @override
  String toString() => 'ErrorDeApi(${tipo.name}, $codigoHttp): $mensaje';
}
