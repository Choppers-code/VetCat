import 'package:flutter_appauth/flutter_appauth.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_app/sesion/autenticador.dart';

/// Guarda la petición de cierre que la app le pasa al plugin.
class _AppAuthFalso extends Fake implements FlutterAppAuth {
  EndSessionRequest? cierre;

  @override
  Future<EndSessionResponse> endSession(EndSessionRequest request) async {
    cierre = request;
    return EndSessionResponse(null);
  }
}

void main() {
  Future<EndSessionRequest> cerrarCon(String emisor) async {
    final appAuth = _AppAuthFalso();
    await AutenticadorAppAuth(appAuth: appAuth, emisor: emisor).cerrar('id');
    return appAuth.cierre!;
  }

  // flutter_appauth 13.0.0 no respeta allowInsecureConnections al descubrir la
  // configuración en endSession y la app se cierra (#253).
  test('con http, el cierre no depende del descubrimiento', () async {
    const emisor = 'http://localhost:8080/realms/vetcat';

    final peticion = await cerrarCon(emisor);

    expect(
      peticion.serviceConfiguration?.endSessionEndpoint,
      '$emisor/protocol/openid-connect/logout',
    );
    expect(peticion.issuer, isNull);
    expect(peticion.idTokenHint, 'id');
  });

  test('con https se sigue descubriendo desde el emisor', () async {
    const emisor = 'https://auth.ejemplo.test/realms/vetcat';

    final peticion = await cerrarCon(emisor);

    expect(peticion.issuer, emisor);
    expect(peticion.serviceConfiguration, isNull);
    expect(peticion.allowInsecureConnections, isFalse);
  });
}
