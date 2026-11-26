import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_app/features/propietarios/formulario_de_propietario.dart';
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
  Object? alRegistrar;
  Object? alEditar;
  PropietarioResponse ficha0 = ana(mascotas: [luna]);
  final consultas = <String>[];
  final registros = <PropietarioRequest>[];
  final ediciones = <PropietarioRequest>[];

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

  @override
  Future<PropietarioResponse> registrar(PropietarioRequest datos) async {
    registros.add(datos);
    final error = alRegistrar;
    if (error != null) {
      alRegistrar = null; // solo falla la primera vez
      throw error;
    }
    return ficha0;
  }

  @override
  Future<PropietarioResponse> editar(int id, PropietarioRequest datos) async {
    ediciones.add(datos);
    if (alEditar != null) throw alEditar!;
    return ficha0;
  }
}

void main() {
  late Entorno e;
  late PropietariosFalso repo;

  setUp(() {
    e = Entorno();
    repo = PropietariosFalso();
    e.extra.add(repositorioDePropietariosProvider.overrideWithValue(repo));
  });

  Finder campo(String etiqueta) => find.widgetWithText(TextFormField, etiqueta);

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

  group('alta', () {
    Future<void> llenar(WidgetTester tester) async {
      await e.entrar(tester);
      await tester.tap(find.text('Nuevo propietario'));
      await tester.pumpAndSettle();
      await tester.enterText(campo('Nombre'), 'Ana Pérez');
      await tester.enterText(campo('Teléfono'), '5551234');
    }

    testWidgets('guarda y abre la ficha', (tester) async {
      await llenar(tester);

      await tester.tap(find.text('Guardar'));
      await tester.pumpAndSettle();

      expect(repo.registros.single.nombre, 'Ana Pérez');
      expect(repo.registros.single.correo, isNull);
      expect(find.text('Teléfono: 5551234'), findsOneWidget);
    });

    testWidgets('sin nombre no llama al backend', (tester) async {
      await e.entrar(tester);
      await tester.tap(find.text('Nuevo propietario'));
      await tester.pumpAndSettle();

      await tester.tap(find.text('Guardar'));
      await tester.pumpAndSettle();

      expect(find.text('Obligatorio'), findsNWidgets(2));
      expect(repo.registros, isEmpty);
    });

    testWidgets('teléfono repetido: pide confirmar y reenvía', (tester) async {
      repo.alRegistrar = errorDeApi(409, 'duplicado');
      await llenar(tester);

      await tester.tap(find.text('Guardar'));
      await tester.pumpAndSettle();
      expect(find.text(mensajeDeDuplicado), findsOneWidget);
      await tester.tap(find.text('Registrar de todos modos'));
      await tester.pumpAndSettle();

      expect(repo.registros.map((r) => r.confirmarDuplicado), [false, true]);
      expect(find.text('Teléfono: 5551234'), findsOneWidget);
    });

    testWidgets('teléfono repetido y se cancela: no se crea', (tester) async {
      repo.alRegistrar = errorDeApi(409, 'duplicado');
      await llenar(tester);

      await tester.tap(find.text('Guardar'));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Cancelar'));
      await tester.pumpAndSettle();

      expect(repo.registros, hasLength(1));
      expect(find.text('Nuevo propietario'), findsOneWidget);
    });

    testWidgets('un 422 muestra texto fijo, no el del backend', (tester) async {
      repo.alRegistrar = errorDeApi(422, 'direccion no permitida por el plan');
      await llenar(tester);

      await tester.tap(find.text('Guardar'));
      await tester.pumpAndSettle();

      expect(find.textContaining('direccion'), findsNothing);
      expect(
        find.text(ErrorDeApi.mensajePorTipo(TipoDeError.entradaInvalida)),
        findsOneWidget,
      );
    });

    testWidgets('sin visitas a domicilio no hay dirección ni se manda', (
      tester,
    ) async {
      await llenar(tester);

      expect(campo('Dirección'), findsNothing);
      await tester.tap(find.text('Guardar'));
      await tester.pumpAndSettle();

      expect(repo.registros.single.direccion, isNull);
    });

    testWidgets('con visitas a domicilio la dirección aparece y se manda', (
      tester,
    ) async {
      e.extra.add(visitasDomicilioProvider.overrideWithValue(true));
      await llenar(tester);

      await tester.enterText(campo('Dirección'), 'Calle 1');
      await tester.tap(find.text('Guardar'));
      await tester.pumpAndSettle();

      expect(repo.registros.single.direccion, 'Calle 1');
    });
  });

  group('edición', () {
    testWidgets('precarga los datos y guarda con el id', (tester) async {
      await abrirFicha(tester);

      await tester.tap(find.text('Editar'));
      await tester.pumpAndSettle();
      expect(
        tester.widget<TextFormField>(campo('Nombre')).controller!.text,
        'Ana Pérez',
      );
      await tester.enterText(campo('Nombre'), 'Ana P.');
      await tester.tap(find.text('Guardar'));
      await tester.pumpAndSettle();

      expect(repo.ediciones.single.nombre, 'Ana P.');
      expect(find.text('Teléfono: 5551234'), findsOneWidget);
    });

    testWidgets('un 409 al editar no ofrece crear un duplicado', (
      tester,
    ) async {
      repo.alEditar = errorDeApi(409, 'conflicto');
      await abrirFicha(tester);
      await tester.tap(find.text('Editar'));
      await tester.pumpAndSettle();

      await tester.tap(find.text('Guardar'));
      await tester.pumpAndSettle();

      expect(find.text(mensajeDeDuplicado), findsNothing);
      expect(
        find.text(ErrorDeApi.mensajePorTipo(TipoDeError.conflicto)),
        findsOneWidget,
      );
    });
  });
}
