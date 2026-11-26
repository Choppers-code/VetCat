import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_app/features/sesion/controlador_de_clinica.dart';
import 'package:vetcat_app/features/sesion/token_de_sesion.dart';
import 'package:vetcat_app/sesion/controlador_de_sesion.dart';

import 'apoyo.dart';

void main() {
  late Entorno e;
  late ProviderContainer c;
  late ProveedorDeTokenDeSesion tokensDeSesion;

  /// Sesión abierta en la clínica 1, como tras elegirla.
  Future<void> iniciarEnClinica1() async {
    e = Entorno()
      ..almacen.guardado = tokens()
      ..repositorio.lista = [clinica(1), clinica(2)];
    final proveedor = Provider((ref) => ProveedorDeTokenDeSesion(ref));
    c = ProviderContainer(overrides: e.overrides);
    addTearDown(c.dispose);
    c.listen(sesionProvider, (_, _) {});
    c.listen(clinicaProvider, (_, _) {});
    await pumpEventQueue();
    await c.read(clinicaProvider.notifier).elegir(clinica(1));
    e.repositorio.aperturas.clear();
    tokensDeSesion = c.read(proveedor);
  }

  /// La identidad vence con la sesión de clínica ya abierta.
  void vencerIdentidad() {
    // ignore: invalid_use_of_protected_member
    c.read(sesionProvider.notifier).state = ConSesion(
      tokens(vida: const Duration(minutes: -1)),
    );
  }

  setUp(iniciarEnClinica1);

  group('token según la ruta', () {
    test(
      'identidad en /sesion y /plataforma, de clínica en el resto',
      () async {
        expect(await tokensDeSesion.token('/sesion/clinicas'), 'acceso');
        expect(await tokensDeSesion.token('/plataforma/clinicas'), 'acceso');
        expect(await tokensDeSesion.token('/mascotas/3'), 'token-clinica');
      },
    );

    test('una ruta que solo empieza parecido no recibe la identidad', () async {
      expect(await tokensDeSesion.token('/sesiones-nuevas'), 'token-clinica');
    });

    test('sin clínica abierta, el negocio no lleva ningún token', () async {
      c.read(clinicaProvider.notifier).cambiarDeClinica();

      expect(await tokensDeSesion.token('/mascotas/3'), isNull);
    });

    test('la identidad vencida se renueva antes de mandarla', () async {
      vencerIdentidad();
      e.auth.alRenovar = tokens(refresco: 'otro');

      expect(await tokensDeSesion.token('/sesion/clinicas'), 'acceso');
      expect(e.auth.renovaciones, 1);
    });
  });

  group('401 en una ruta de negocio', () {
    test('reabre la sesión de la clínica con la identidad, una vez', () async {
      e.repositorio.alAbrir[1] = sesionDe(1, token: 'token-nuevo');

      final renovado = await tokensDeSesion.renovar('/mascotas/3');

      expect(renovado, isTrue);
      expect(e.repositorio.aperturas, [1]);
      expect(await tokensDeSesion.token('/mascotas/3'), 'token-nuevo');
      expect(e.auth.renovaciones, 0);
    });

    test('no se mantiene en pantalla el estado de carga al reabrir', () async {
      final estados = <Type>[];
      c.listen(clinicaProvider, (_, nuevo) => estados.add(nuevo.runtimeType));

      await tokensDeSesion.renovar('/mascotas/3');

      expect(estados, [EnClinica]);
    });

    test('403 al reabrir: al selector, sin pedir credenciales', () async {
      e.repositorio.alAbrir[1] = errorDeApi(403, mensajeClinicaNoDisponible);

      expect(await tokensDeSesion.renovar('/mascotas/3'), isFalse);
      tokensDeSesion.sesionExpirada();

      final estado = c.read(clinicaProvider);
      expect(estado, isA<ElegirClinica>());
      expect((estado as ElegirClinica).aviso, mensajeClinicaNoDisponible);
      expect(c.read(sesionProvider), isA<ConSesion>());
      expect(e.auth.inicios, 0);
    });

    test('un segundo 401 tras reabrir también va al selector', () async {
      await tokensDeSesion.renovar('/mascotas/3');

      tokensDeSesion.sesionExpirada();

      expect(c.read(clinicaProvider), isA<ElegirClinica>());
      expect(c.read(sesionProvider), isA<ConSesion>());
    });

    test('si la identidad también venció y no se renueva, al acceso', () async {
      vencerIdentidad();
      e.auth.alRenovar = null;

      expect(await tokensDeSesion.renovar('/mascotas/3'), isFalse);
      tokensDeSesion.sesionExpirada();
      await pumpEventQueue();

      expect(c.read(sesionProvider), isA<SinSesion>());
      expect(e.almacen.guardado, isNull);
    });
  });

  group('401 en una ruta de identidad', () {
    test('se renueva con Keycloak, no con la clínica', () async {
      e.auth.alRenovar = tokens(refresco: 'otro');

      expect(await tokensDeSesion.renovar('/sesion/clinicas'), isTrue);
      expect(e.auth.renovaciones, 1);
      expect(e.repositorio.aperturas, isEmpty);
    });

    test('si Keycloak ya no la acepta, la sesión expira', () async {
      e.auth.alRenovar = null;

      expect(await tokensDeSesion.renovar('/sesion/clinicas'), isFalse);
      tokensDeSesion.sesionExpirada();
      await pumpEventQueue();

      expect(c.read(sesionProvider), isA<SinSesion>());
    });
  });
}
