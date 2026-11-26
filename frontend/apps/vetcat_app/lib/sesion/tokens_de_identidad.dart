import 'dart:convert';

/// Tokens que entrega Keycloak al iniciar sesión. Solo valen para abrir sesión
/// de clínica (`/sesion/**`) y para la plataforma (ADR-0003).
class TokensDeIdentidad {
  const TokensDeIdentidad({
    required this.acceso,
    required this.refresco,
    required this.idToken,
    required this.vence,
  });

  factory TokensDeIdentidad.desdeJson(String texto) {
    final mapa = jsonDecode(texto) as Map<String, Object?>;
    return TokensDeIdentidad(
      acceso: mapa['acceso']! as String,
      refresco: mapa['refresco'] as String?,
      idToken: mapa['idToken'] as String?,
      vence: DateTime.fromMillisecondsSinceEpoch(mapa['vence']! as int),
    );
  }

  final String acceso;
  final String? refresco;

  /// Hace falta para terminar la sesión en Keycloak.
  final String? idToken;
  final DateTime vence;

  /// Margen de 30 s para no mandar un token que vence en el camino.
  bool get vigente =>
      vence.isAfter(DateTime.now().add(const Duration(seconds: 30)));

  String aJson() => jsonEncode({
    'acceso': acceso,
    'refresco': refresco,
    'idToken': idToken,
    'vence': vence.millisecondsSinceEpoch,
  });
}
