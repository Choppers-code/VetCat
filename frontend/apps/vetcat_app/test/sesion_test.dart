import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_app/features/sesion/controlador_de_clinica.dart';
import 'package:vetcat_app/sesion/almacen_de_tokens.dart';
import 'package:vetcat_app/sesion/autenticador.dart';
import 'package:vetcat_app/sesion/controlador_de_sesion.dart';
import 'package:vetcat_app/sesion/pantallas_de_sesion.dart';
import 'package:vetcat_app/sesion/tokens_de_identidad.dart';

import 'apoyo.dart' show AlmacenDeClinicaFalso;

TokensDeIdentidad tokens({
  Duration vida = const Duration(minutes: 5),
  String? refresco = 'refresco',
}) => TokensDeIdentidad(
  acceso: 'acceso',
  refresco: refresco,
  idToken: 'id',
  vence: DateTime.now().add(vida),
);

class AlmacenEnMemoria implements AlmacenDeTokens {
  AlmacenEnMemoria([this.guardado]);
  TokensDeIdentidad? guardado;

  @override
  Future<TokensDeIdentidad?> leer() async => guardado;
  @override
  Future<void> guardar(TokensDeIdentidad t) async => guardado = t;
  @override
  Future<void> borrar() async => guardado = null;
}

class AutenticadorFalso implements Autenticador {
  Object? alIniciar; // TokensDeIdentidad, null (cancelar) o un error.
  Object? alRenovar;
  Object? alCerrar;
  String? idTokenAlCerrar;
  int cierres = 0;
  int renovaciones = 0;

  @override
  Future<TokensDeIdentidad?> iniciar() async {
    final r = alIniciar;
    if (r is TokensDeIdentidad?) return r;
    throw r;
  }

  @override
  Future<TokensDeIdentidad?> renovar(String refresco) async {
    renovaciones++;
    final r = alRenovar;
    if (r is TokensDeIdentidad?) return r;
    throw r;
  }

  @override
  Future<void> cerrar(String? idToken) async {
    cierres++;
    idTokenAlCerrar = idToken;
    if (alCerrar != null) throw alCerrar!;
  }
}

void main() {
  late AlmacenEnMemoria almacen;
  late AutenticadorFalso auth;

  Future<void> abrir(WidgetTester tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          almacenDeClinicaProvider.overrideWithValue(AlmacenDeClinicaFalso()),
          almacenProvider.overrideWithValue(almacen),
          autenticadorProvider.overrideWithValue(auth),
        ],
        child: const MaterialApp(home: RaizDeSesion()),
      ),
    );
    await tester.pumpAndSettle();
  }

  setUp(() {
    almacen = AlmacenEnMemoria();
    auth = AutenticadorFalso();
  });

  testWidgets('sin sesión guardada pide iniciar sesión', (tester) async {
    await abrir(tester);

    expect(find.text('Iniciar sesión'), findsOneWidget);
    expect(find.text(mensajeDeAccesoFallido), findsNothing);
  });

  testWidgets('credenciales válidas: guarda los tokens y entra', (
    tester,
  ) async {
    auth.alIniciar = tokens();
    await abrir(tester);

    await tester.tap(find.text('Iniciar sesión'));
    await tester.pumpAndSettle();

    expect(find.text('Sesión iniciada'), findsOneWidget);
    expect(almacen.guardado?.acceso, 'acceso');
  });

  testWidgets('cualquier fallo da el mismo mensaje y no guarda nada', (
    tester,
  ) async {
    final mensajes = <String>[];
    for (final error in <Object>[
      StateError('usuario no existe'),
      Exception('contraseña incorrecta'),
    ]) {
      auth.alIniciar = error;
      await abrir(tester);
      await tester.tap(find.text('Iniciar sesión'));
      await tester.pumpAndSettle();
      mensajes.add(
        tester.widgetList<Text>(find.byType(Text)).map((t) => t.data).join('|'),
      );
    }

    expect(mensajes.toSet(), hasLength(1));
    expect(mensajes.first, contains(mensajeDeAccesoFallido));
    expect(mensajes.first, isNot(contains('existe')));
    expect(almacen.guardado, isNull);
  });

  testWidgets('cancelar no es un error', (tester) async {
    auth.alIniciar = null;
    await abrir(tester);

    await tester.tap(find.text('Iniciar sesión'));
    await tester.pumpAndSettle();

    expect(find.text('Iniciar sesión'), findsOneWidget);
    expect(find.text(mensajeDeAccesoFallido), findsNothing);
  });

  testWidgets('cerrar sesión borra los tokens y no deja a dónde volver', (
    tester,
  ) async {
    auth.alIniciar = tokens();
    await abrir(tester);
    await tester.tap(find.text('Iniciar sesión'));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Cerrar sesión'));
    await tester.pumpAndSettle();

    expect(almacen.guardado, isNull);
    expect(auth.cierres, 1);
    expect(auth.idTokenAlCerrar, 'id');
    expect(find.text('Iniciar sesión'), findsOneWidget);
    expect(find.text('Sesión iniciada'), findsNothing);
    expect(Navigator.of(tester.element(find.byType(Scaffold))).canPop(), false);
  });

  testWidgets('si Keycloak falla al cerrar, los tokens se borran igual', (
    tester,
  ) async {
    auth
      ..alIniciar = tokens()
      ..alCerrar = Exception('sin red');
    await abrir(tester);
    await tester.tap(find.text('Iniciar sesión'));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Cerrar sesión'));
    await tester.pumpAndSettle();

    expect(almacen.guardado, isNull);
    expect(find.text('Iniciar sesión'), findsOneWidget);
  });

  group('al abrir la app con tokens guardados', () {
    testWidgets('vigentes: entra sin pedir nada', (tester) async {
      almacen.guardado = tokens();

      await abrir(tester);

      expect(find.text('Sesión iniciada'), findsOneWidget);
      expect(auth.renovaciones, 0);
    });

    testWidgets('vencidos con refresco: renueva y entra', (tester) async {
      almacen.guardado = tokens(vida: const Duration(minutes: -1));
      auth.alRenovar = tokens(refresco: 'nuevo');

      await abrir(tester);

      expect(find.text('Sesión iniciada'), findsOneWidget);
      expect(almacen.guardado?.refresco, 'nuevo');
    });

    testWidgets('vencidos y sin renovación: borra y pide entrar', (
      tester,
    ) async {
      almacen.guardado = tokens(vida: const Duration(minutes: -1));
      auth.alRenovar = null;

      await abrir(tester);

      expect(find.text('Iniciar sesión'), findsOneWidget);
      expect(almacen.guardado, isNull);
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
