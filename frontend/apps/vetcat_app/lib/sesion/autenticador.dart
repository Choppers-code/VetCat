import 'package:flutter_appauth/flutter_appauth.dart';

import 'tokens_de_identidad.dart';

/// Habla con Keycloak. La app no sabe cómo: solo pide entrar, renovar o salir.
abstract interface class Autenticador {
  /// Abre el inicio de sesión. `null` si la persona lo canceló.
  Future<TokensDeIdentidad?> iniciar();

  /// Renueva con el token de refresco. `null` si Keycloak ya no lo acepta.
  Future<TokensDeIdentidad?> renovar(String refresco);

  /// Termina la sesión en Keycloak.
  Future<void> cerrar(String? idToken);
}

/// Authorization Code + PKCE contra el cliente público `vetcat-app`.
///
/// El emisor llega por `--dart-define=KEYCLOAK_EMISOR=...`: nada de la máquina
/// de quien desarrolla queda escrito en el código.
class AutenticadorAppAuth implements Autenticador {
  AutenticadorAppAuth({FlutterAppAuth? appAuth, String? emisor})
    : _appAuth = appAuth ?? FlutterAppAuth(),
      _emisor = emisor ?? const String.fromEnvironment('KEYCLOAK_EMISOR');

  static const _cliente = 'vetcat-app';
  static const _redireccion = 'vetcat://callback';
  static const _alcances = ['openid', 'profile', 'offline_access'];

  final FlutterAppAuth _appAuth;
  final String _emisor;

  /// Solo se admite http en desarrollo; con https no cambia nada.
  bool get _inseguro => _emisor.startsWith('http://');

  String get _emisorValido {
    if (_emisor.isEmpty) {
      throw StateError('Falta --dart-define=KEYCLOAK_EMISOR');
    }
    return _emisor;
  }

  @override
  Future<TokensDeIdentidad?> iniciar() async {
    try {
      final r = await _appAuth.authorizeAndExchangeCode(
        AuthorizationTokenRequest(
          _cliente,
          _redireccion,
          issuer: _emisorValido,
          scopes: _alcances,
          allowInsecureConnections: _inseguro,
        ),
      );
      return _convertir(r, r.refreshToken);
    } on FlutterAppAuthUserCancelledException {
      return null;
    }
  }

  @override
  Future<TokensDeIdentidad?> renovar(String refresco) async {
    final r = await _appAuth.token(
      TokenRequest(
        _cliente,
        _redireccion,
        issuer: _emisorValido,
        refreshToken: refresco,
        grantType: GrantType.refreshToken,
        allowInsecureConnections: _inseguro,
      ),
    );
    return _convertir(r, r.refreshToken ?? refresco);
  }

  @override
  Future<void> cerrar(String? idToken) async {
    final emisor = _emisorValido;
    await _appAuth.endSession(
      EndSessionRequest(
        idTokenHint: idToken,
        postLogoutRedirectUrl: _redireccion,
        issuer: _inseguro ? null : emisor,
        serviceConfiguration: _inseguro ? _rutasDeKeycloak(emisor) : null,
        allowInsecureConnections: _inseguro,
      ),
    );
  }

  /// Solo para http: flutter_appauth 13.0.0 ignora `allowInsecureConnections`
  /// al descubrir la configuración en `endSession` y la app se cierra (#253).
  /// Con las rutas dadas no hay descubrimiento.
  AuthorizationServiceConfiguration _rutasDeKeycloak(String emisor) {
    final base = '$emisor/protocol/openid-connect';
    return AuthorizationServiceConfiguration(
      authorizationEndpoint: '$base/auth',
      tokenEndpoint: '$base/token',
      endSessionEndpoint: '$base/logout',
    );
  }

  TokensDeIdentidad _convertir(TokenResponse r, String? refresco) {
    final acceso = r.accessToken;
    if (acceso == null) throw StateError('Keycloak no devolvió token');
    return TokensDeIdentidad(
      acceso: acceso,
      refresco: refresco,
      idToken: r.idToken,
      vence:
          r.accessTokenExpirationDateTime ??
          DateTime.now().add(const Duration(minutes: 5)),
    );
  }
}
