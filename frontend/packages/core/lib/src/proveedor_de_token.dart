/// Lo que el cliente HTTP necesita de la sesión (ADR-0003). Lo implementa el
/// inicio de sesión de la app; el cliente no sabe cómo se obtiene ni se guarda
/// ningún token.
///
/// Hay dos tokens: el de identidad de Keycloak, válido solo en `/sesion/**` y
/// `/plataforma/**`, y el de clínica que emite la API, válido en el resto. Por
/// eso las dos operaciones reciben la ruta y el proveedor elige el que toca.
abstract interface class ProveedorDeToken {
  /// Token que corresponde a [ruta], o `null` si no hay sesión.
  Future<String?> token(String ruta);

  /// Intenta recuperar la sesión tras un 401 en [ruta]. Devuelve `true` si ya
  /// hay un token nuevo para esa ruta.
  ///
  /// En `/sesion/**` y `/plataforma/**` es el refresco de Keycloak. En el resto
  /// no hay refresco: se reabre la sesión de la clínica con el token de
  /// identidad. Nunca debe devolver el token de identidad para una ruta de
  /// negocio.
  Future<bool> renovar(String ruta);

  /// Se llama cuando la sesión no se pudo recuperar y hay que volver a entrar.
  void sesionExpirada();
}
