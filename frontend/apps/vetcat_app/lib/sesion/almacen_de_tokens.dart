import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'tokens_de_identidad.dart';

/// Dónde se guardan los tokens. Solo el almacenamiento seguro del sistema:
/// nunca preferencias normales ni texto plano.
abstract interface class AlmacenDeTokens {
  Future<TokensDeIdentidad?> leer();
  Future<void> guardar(TokensDeIdentidad tokens);
  Future<void> borrar();
}

/// Keystore de Android a través de `flutter_secure_storage`.
class AlmacenSeguro implements AlmacenDeTokens {
  AlmacenSeguro([FlutterSecureStorage? almacen])
    : _almacen = almacen ?? const FlutterSecureStorage();

  static const _clave = 'vetcat.tokens_de_identidad';

  final FlutterSecureStorage _almacen;

  @override
  Future<TokensDeIdentidad?> leer() async {
    final texto = await _almacen.read(key: _clave);
    if (texto == null) return null;
    try {
      return TokensDeIdentidad.desdeJson(texto);
    } on Object {
      // Contenido ilegible: se descarta y el usuario vuelve a entrar.
      await borrar();
      return null;
    }
  }

  @override
  Future<void> guardar(TokensDeIdentidad tokens) =>
      _almacen.write(key: _clave, value: tokens.aJson());

  @override
  Future<void> borrar() => _almacen.delete(key: _clave);
}
