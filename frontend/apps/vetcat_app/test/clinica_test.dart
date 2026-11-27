import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_app/features/sesion/controlador_de_clinica.dart';

import 'apoyo.dart';

void main() {
  late Entorno e;

  setUp(() => e = Entorno());

  group('selector', () {
    testWidgets('sin clínicas: texto fijo de la app', (tester) async {
      e.repositorio.lista = [];

      await e.entrar(tester);

      expect(find.text(mensajeSinClinicas), findsOneWidget);
    });

    testWidgets('una sola clínica activa: entra directo', (tester) async {
      await e.entrar(tester);

      expect(e.repositorio.aperturas, [1]);
      expect(find.text('Elige tu clínica'), findsNothing);
      expect(find.text('Vista profesional'), findsOneWidget);
    });

    testWidgets('una sola clínica suspendida: deshabilitada y sin abrir', (
      tester,
    ) async {
      e.repositorio.lista = [clinica(1, estado: 'SUSPENDIDA')];

      await e.entrar(tester);
      await tester.tap(find.text('Clínica 1'));
      await tester.pumpAndSettle();

      expect(find.text('Suspendida'), findsOneWidget);
      expect(e.repositorio.aperturas, isEmpty);
    });

    testWidgets('las dadas de baja no se muestran', (tester) async {
      e.repositorio.lista = [clinica(1), clinica(2, estado: 'BAJA')];

      await e.entrar(tester);

      expect(e.repositorio.aperturas, [1]);
    });

    testWidgets('varias: nombre y roles, y no entra sola', (tester) async {
      e.repositorio.lista = [
        clinica(1, roles: ['veterinario', 'clinica_admin']),
        clinica(2, roles: ['cliente']),
      ];

      await e.entrar(tester);

      expect(find.text('Veterinario · Administrador'), findsOneWidget);
      expect(find.text('Cliente'), findsOneWidget);
      expect(e.repositorio.aperturas, isEmpty);
    });

    testWidgets('la última usada va primero y no salta la elección', (
      tester,
    ) async {
      e.repositorio.lista = [clinica(1), clinica(2)];
      e.ultima.id = 2;

      await e.entrar(tester);

      expect(find.text('Última usada'), findsOneWidget);
      expect(
        tester.getTopLeft(find.text('Clínica 2')).dy,
        lessThan(tester.getTopLeft(find.text('Clínica 1')).dy),
      );
      expect(e.repositorio.aperturas, isEmpty);
    });

    testWidgets('una última clínica que ya no está se ignora', (tester) async {
      e.repositorio.lista = [clinica(1), clinica(2)];
      e.ultima.id = 99;

      await e.entrar(tester);

      expect(find.text('Última usada'), findsNothing);
    });

    testWidgets('elegir abre esa clínica y la recuerda', (tester) async {
      e.repositorio.lista = [clinica(1), clinica(2)];
      await e.entrar(tester);

      await tester.tap(find.text('Clínica 2'));
      await tester.pumpAndSettle();

      expect(e.repositorio.aperturas, [2]);
      expect(e.ultima.id, 2);
      expect(find.text('Vista profesional'), findsOneWidget);
    });

    testWidgets('sin conexión al listar: texto genérico y reintento', (
      tester,
    ) async {
      e.repositorio.alListar = errorDeApi(500, 'java.lang.NullPointer en X');

      await e.entrar(tester);

      expect(find.textContaining('NullPointer'), findsNothing);
      e.repositorio.alListar = null;
      await tester.tap(find.text('Reintentar'));
      await tester.pumpAndSettle();
      expect(find.text('Vista profesional'), findsOneWidget);
    });
  });

  group('403 al abrir', () {
    testWidgets('clínica ajena: texto genérico, no el del backend', (
      tester,
    ) async {
      e.repositorio
        ..lista = [clinica(1), clinica(2)]
        ..alAbrir[2] = errorDeApi(403, 'No tienes acceso a la clínica #2 (x)');
      await e.entrar(tester);

      await tester.tap(find.text('Clínica 2'));
      await tester.pumpAndSettle();

      expect(find.text(mensajeSinAcceso), findsOneWidget);
      expect(find.textContaining('#2'), findsNothing);
      expect(find.text('Elige tu clínica'), findsOneWidget);
      expect(e.ultima.id, isNull);
    });

    testWidgets('suspendida entre listar y abrir: se muestra el motivo', (
      tester,
    ) async {
      e.repositorio
        ..lista = [clinica(1), clinica(2)]
        ..alAbrir[2] = errorDeApi(403, mensajeClinicaNoDisponible);
      await e.entrar(tester);

      await tester.tap(find.text('Clínica 2'));
      await tester.pumpAndSettle();

      expect(find.text(mensajeClinicaNoDisponible), findsOneWidget);
    });
  });

  group('cambiar de clínica', () {
    testWidgets('vuelve al selector sin pedir credenciales', (tester) async {
      e.repositorio.lista = [clinica(1), clinica(2)];
      await e.entrar(tester);
      await tester.tap(find.text('Clínica 1'));
      await tester.pumpAndSettle();

      await tocarMenu(tester, 'Cambiar de clínica');

      expect(find.text('Elige tu clínica'), findsOneWidget);
      expect(e.auth.inicios, 1);
      expect(find.text('Vista profesional'), findsNothing);
    });

    testWidgets('con una sola clínica la opción no aparece', (tester) async {
      await e.entrar(tester);

      await tester.tap(menu);
      await tester.pumpAndSettle();

      expect(find.text('Cambiar de clínica'), findsNothing);
    });

    testWidgets('los datos de la clínica anterior no sobreviven', (
      tester,
    ) async {
      e.repositorio.lista = [clinica(1), clinica(2)];
      var construidos = 0;
      // Así deben declararse los datos de una clínica: mirando su id.
      final datos = Provider.autoDispose((ref) {
        final id = ref.watch(clinicaIdProvider);
        return 'datos de $id #${construidos++}';
      });
      await e.entrar(tester);
      await tester.tap(find.text('Clínica 1'));
      await tester.pumpAndSettle();
      final contenedor = ProviderScope.containerOf(
        tester.element(find.byType(Scaffold).first),
      );
      final sub = contenedor.listen(datos, (_, _) {});
      expect(sub.read(), startsWith('datos de 1'));

      await tocarMenu(tester, 'Cambiar de clínica');
      await tester.tap(find.text('Clínica 2'));
      await tester.pumpAndSettle();

      expect(sub.read(), startsWith('datos de 2'));
      sub.close();
    });
  });

  group('cambiar de vista', () {
    testWidgets('solo si es cliente y personal en esa clínica', (tester) async {
      e.repositorio.lista = [
        clinica(1, roles: ['veterinario']),
      ];
      await e.entrar(tester);

      await tester.tap(menu);
      await tester.pumpAndSettle();

      expect(find.text('Cambiar de vista'), findsNothing);
    });

    testWidgets('cliente y veterinario: alterna sin cerrar sesión', (
      tester,
    ) async {
      e.repositorio.lista = [
        clinica(1, roles: ['veterinario', 'cliente']),
      ];
      await e.entrar(tester);
      expect(find.text('Vista profesional'), findsOneWidget);

      await tocarMenu(tester, 'Cambiar de vista');

      expect(find.text('Vista de cliente'), findsOneWidget);
      expect(e.repositorio.aperturas, [1]);
    });

    testWidgets('quien solo es cliente empieza en la vista de cliente', (
      tester,
    ) async {
      e.repositorio
        ..lista = [
          clinica(1, roles: ['cliente']),
        ]
        ..alAbrir[1] = sesionDe(1, roles: ['cliente']);

      await e.entrar(tester);

      expect(find.text('Vista de cliente'), findsOneWidget);
    });
  });
}
