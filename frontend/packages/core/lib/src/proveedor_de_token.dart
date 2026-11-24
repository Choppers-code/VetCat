/// Lo que el cliente HTTP necesita de la sesión. Lo implementa el inicio de
/// sesión de la app; el cliente no sabe cómo se obtiene ni se guarda el token.
abstract interface class ProveedorDeToken {
  /// Token de acceso vigente, o `null` si no hay sesión.
  Future<String?> token();

  /// Intenta renovar la sesión. Devuelve `true` si ya hay un token nuevo.
  Future<bool> refrescar();

  /// Se llama cuando la sesión no se pudo renovar y hay que volver a entrar.
  void sesionExpirada();
}
