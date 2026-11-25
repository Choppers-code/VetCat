import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_app/sesion/pantallas_de_sesion.dart';
import 'package:vetcat_app/sesion/tokens_de_identidad.dart';

import 'apoyo.dart';

void main() {
  late Entorno e;

  setUp(() => e = Entorno());

  testWidgets('sin sesión guardada pide iniciar sesión', (tester) async {
    await e.abrir(tester);

    expect(find.text('Iniciar sesión'), findsOneWidget);
    expect(find.text(mensajeDeAccesoFallido), findsNothing);
  });

  testWidgets('credenciales válidas: guarda los tokens y entra', (
    tester,
  ) async {
    await e.entrar(tester);

    expect(find.text('Clínica 1'), findsOneWidget);
    expect(e.almacen.guardado?.acceso, 'acceso');
  });

  testWidgets('cualquier fallo da el mismo mensaje y no guarda nada', (
    tester,
  ) async {
    final mensajes = <String>[];
    for (final error in <Object>[
      StateError('usuario no existe'),
      Exception('contraseña incorrecta'),
    ]) {
      e.auth.alIniciar = error;
      await e.abrir(tester);
      await tester.tap(find.text('Iniciar sesión'));
      await tester.pumpAndSettle();
      mensajes.add(
        tester.widgetList<Text>(find.byType(Text)).map((t) => t.data).join('|'),
      );
    }

    expect(mensajes.toSet(), hasLength(1));
    expect(mensajes.first, contains(mensajeDeAccesoFallido));
    expect(mensajes.first, isNot(contains('existe')));
    expect(e.almacen.guardado, isNull);
  });

  testWidgets('cancelar no es un error', (tester) async {
    e.auth.alIniciar = null;
    await e.abrir(tester);

    await tester.tap(find.text('Iniciar sesión'));
    await tester.pumpAndSettle();

    expect(find.text('Iniciar sesión'), findsOneWidget);
    expect(find.text(mensajeDeAccesoFallido), findsNothing);
  });

  testWidgets('cerrar sesión borra los tokens y no deja a dónde volver', (
    tester,
  ) async {
    await e.entrar(tester);

    await tocarMenu(tester, 'Cerrar sesión');

    expect(e.almacen.guardado, isNull);
    expect(e.auth.cierres, 1);
    expect(e.auth.idTokenAlCerrar, 'id');
    expect(find.text('Iniciar sesión'), findsOneWidget);
    expect(find.text('Clínica 1'), findsNothing);
  });

  testWidgets('si Keycloak falla al cerrar, los tokens se borran igual', (
    tester,
  ) async {
    e.auth.alCerrar = Exception('sin red');
    await e.entrar(tester);

    await tocarMenu(tester, 'Cerrar sesión');

    expect(e.almacen.guardado, isNull);
    expect(find.text('Iniciar sesión'), findsOneWidget);
  });

  group('al abrir la app con tokens guardados', () {
    testWidgets('vigentes: entra sin pedir nada', (tester) async {
      e.almacen.guardado = tokens();

      await e.abrir(tester);

      expect(find.text('Clínica 1'), findsOneWidget);
      expect(e.auth.renovaciones, 0);
    });

    testWidgets('vencidos con refresco: renueva y entra', (tester) async {
      e.almacen.guardado = tokens(vida: const Duration(minutes: -1));
      e.auth.alRenovar = tokens(refresco: 'nuevo');

      await e.abrir(tester);

      expect(find.text('Clínica 1'), findsOneWidget);
      expect(e.almacen.guardado?.refresco, 'nuevo');
    });

    testWidgets('vencidos y sin renovación: borra y pide entrar', (
      tester,
    ) async {
      e.almacen.guardado = tokens(vida: const Duration(minutes: -1));
      e.auth.alRenovar = null;

      await e.abrir(tester);

      expect(find.text('Iniciar sesión'), findsOneWidget);
      expect(e.almacen.guardado, isNull);
    });
  });

  test('los tokens se guardan y se leen sin perder datos', () {
    final original = tokens();

    final leido = TokensDeIdentidad.desdeJson(original.aJson());

    expect(leido.acceso, original.acceso);
    expect(leido.refresco, original.refresco);
    expect(leido.idToken, original.idToken);
    expect(
      leido.vence.millisecondsSinceEpoch,
      original.vence.millisecondsSinceEpoch,
    );
  });
}
