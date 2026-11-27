import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_app/features/propietarios/repositorio_de_propietarios.dart';
import 'package:vetcat_core/vetcat_core.dart';

import 'apoyo.dart';

PropietarioResponse ana({List<MascotaResponse> mascotas = const []}) =>
    PropietarioResponse(
      id: 5,
      nombre: 'Ana Pérez',
      telefono: '5551234',
      activo: true,
      correo: 'ana@correo.mx',
      mascotas: mascotas,
    );

const luna = MascotaResponse(
  id: 9,
  propietarioId: 5,
  nombre: 'Luna',
  especie: Especie.gato,
  activo: true,
  raza: 'Siamés',
);

class PropietariosFalso implements RepositorioDePropietarios {
  List<ResultadoDeBusqueda> resultados = [];
  Object? alBuscar;
  PropietarioResponse ficha0 = ana(mascotas: [luna]);
  final consultas = <String>[];

  @override
  Future<Pagina<ResultadoDeBusqueda>> buscar(
    String texto, {
    int pagina = 0,
  }) async {
    consultas.add(texto);
    if (alBuscar != null) throw alBuscar!;
    return Pagina(
      contenido: resultados,
      pagina: 0,
      tamano: 20,
      total: resultados.length,
    );
  }

  @override
  Future<PropietarioResponse> ficha(int id) async => ficha0;
}

void main() {
  late Entorno e;
  late PropietariosFalso repo;

  setUp(() {
    e = Entorno();
    repo = PropietariosFalso();
    e.extra.add(repositorioDePropietariosProvider.overrideWithValue(repo));
  });

  Future<void> buscar(WidgetTester tester, String texto) async {
    await tester.enterText(find.byType(TextField), texto);
    await tester.testTextInput.receiveAction(TextInputAction.search);
    await tester.pumpAndSettle();
  }

  Future<void> abrirFicha(WidgetTester tester) async {
    repo.resultados = [ResultadoDeBusqueda(propietario: ana(), mascota: luna)];
    await e.entrar(tester);
    await buscar(tester, 'luna');
    await tester.tap(find.text('Ana Pérez'));
    await tester.pumpAndSettle();
  }

  group('búsqueda', () {
    testWidgets('al empezar pide escribir; con menos de 3 letras no busca', (
      tester,
    ) async {
      await e.entrar(tester);
      expect(find.textContaining('al menos 3 letras'), findsOneWidget);

      await buscar(tester, 'lu');

      expect(repo.consultas, isEmpty);
    });

    testWidgets('muestra propietario, teléfono y mascota', (tester) async {
      repo.resultados = [
        ResultadoDeBusqueda(propietario: ana(), mascota: luna),
      ];
      await e.entrar(tester);

      await buscar(tester, 'luna');

      expect(repo.consultas, ['luna']);
      expect(find.text('Ana Pérez'), findsOneWidget);
      expect(find.text('5551234 · Luna (Gato)'), findsOneWidget);
    });

    testWidgets('sin coincidencias lo dice', (tester) async {
      await e.entrar(tester);

      await buscar(tester, 'zzz');

      expect(find.text('Sin resultados.'), findsOneWidget);
    });

    testWidgets('un 403 no muestra el mensaje del backend', (tester) async {
      repo.alBuscar = errorDeApi(403, 'rol recepcionista sin permiso en X');
      await e.entrar(tester);

      await buscar(tester, 'luna');

      expect(find.textContaining('recepcionista'), findsNothing);
      expect(
        find.text(ErrorDeApi.mensajePorTipo(TipoDeError.prohibido)),
        findsOneWidget,
      );
    });
  });

  group('aislamiento', () {
    testWidgets('otra clínica no conserva la búsqueda ni sus resultados', (
      tester,
    ) async {
      repo.resultados = [ResultadoDeBusqueda(propietario: ana())];
      e.repositorio.lista = [clinica(1), clinica(2)];
      await e.entrar(tester);
      await tester.tap(find.text('Clínica 1'));
      await tester.pumpAndSettle();
      await buscar(tester, 'ana');
      expect(find.text('Ana Pérez'), findsOneWidget);

      await tocarMenu(tester, 'Cambiar de clínica');
      await tester.tap(find.text('Clínica 2'));
      await tester.pumpAndSettle();

      expect(find.text('Ana Pérez'), findsNothing);
      expect(find.widgetWithText(TextField, 'ana'), findsNothing);
      expect(repo.consultas, ['ana']);
    });
  });

  group('ficha', () {
    testWidgets('muestra datos y mascotas activas', (tester) async {
      await abrirFicha(tester);

      expect(find.text('Teléfono: 5551234'), findsOneWidget);
      expect(find.text('Correo: ana@correo.mx'), findsOneWidget);
      expect(find.text('Siamés'), findsNothing);
      expect(find.text('Gato · Siamés'), findsOneWidget);
    });

    testWidgets('atrás conserva la búsqueda y sus resultados', (tester) async {
      await abrirFicha(tester);

      await tester.binding.handlePopRoute();
      await tester.pumpAndSettle();

      expect(find.widgetWithText(TextField, 'luna'), findsOneWidget);
      expect(find.text('Ana Pérez'), findsOneWidget);
    });

    testWidgets('atrás vuelve a la lista sin cerrar la clínica', (
      tester,
    ) async {
      await abrirFicha(tester);

      await tester.binding.handlePopRoute();
      await tester.pumpAndSettle();

      expect(find.text('Teléfono: 5551234'), findsNothing);
      expect(find.text('Clínica 1'), findsOneWidget);
    });
  });
}
