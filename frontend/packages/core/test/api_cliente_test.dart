import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_core/vetcat_core.dart';

/// Adaptador HTTP falso: devuelve respuestas preparadas, sin tocar la red.
class AdaptadorFalso implements HttpClientAdapter {
  final List<ResponseBody Function(RequestOptions)> _respuestas = [];

  /// Copia de las cabeceras de cada petición, tal como salieron.
  final List<Map<String, Object?>> cabeceras = [];
  final List<RequestOptions> peticiones = [];

  void responder(int estado, Map<String, Object?> sobre) {
    _respuestas.add(
      (_) => ResponseBody.fromString(
        jsonEncode(sobre),
        estado,
        headers: {
          Headers.contentTypeHeader: [Headers.jsonContentType],
        },
      ),
    );
  }

  void fallarConexion() {
    _respuestas.add(
      (opciones) => throw DioException.connectionError(
        requestOptions: opciones,
        reason: 'sin red',
      ),
    );
  }

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    peticiones.add(options);
    cabeceras.add(Map<String, Object?>.of(options.headers));
    return _respuestas.removeAt(0)(options);
  }

  @override
  void close({bool force = false}) {}
}

/// Dos tokens, como el ADR-0003: identidad en `/sesion` y `/plataforma`, y el
/// de clínica en el resto.
class TokensFalsos implements ProveedorDeToken {
  TokensFalsos({this.actual = 'token-viejo', this.alRenovar = 'token-nuevo'});

  /// Token de clínica.
  String? actual;
  String identidad = 'token-identidad';
  final String? alRenovar;
  final List<String> rutasRenovadas = [];
  int get renovaciones => rutasRenovadas.length;
  int expiraciones = 0;

  static bool _esDeIdentidad(String ruta) =>
      ruta.startsWith('/sesion') || ruta.startsWith('/plataforma');

  @override
  Future<String?> token(String ruta) async =>
      _esDeIdentidad(ruta) ? identidad : actual;

  @override
  Future<bool> renovar(String ruta) async {
    rutasRenovadas.add(ruta);
    if (alRenovar == null) return false;
    if (_esDeIdentidad(ruta)) {
      identidad = alRenovar!;
    } else {
      actual = alRenovar;
    }
    return true;
  }

  @override
  void sesionExpirada() => expiraciones++;
}

Map<String, Object?> exito(Object? datos) => {
  'success': true,
  'message': 'OK',
  'data': datos,
};

Map<String, Object?> fallo(String mensaje) => {
  'success': false,
  'message': mensaje,
  'data': null,
};

void main() {
  late AdaptadorFalso red;
  late TokensFalsos tokens;
  late ApiCliente api;

  setUp(() {
    red = AdaptadorFalso();
    tokens = TokensFalsos();
    api = ApiCliente(
      urlBase: 'http://api.prueba/api/v1',
      tokens: tokens,
      adaptador: red,
    );
  });

  Future<Object?> llamar() =>
      api.obtener<Object?>('/health', convertir: (d) => d);

  test('éxito: devuelve el data del sobre ya convertido', () async {
    red.responder(200, exito({'nombre': 'Luna'}));

    final nombre = await api.obtener<String>(
      '/mascotas/1',
      convertir: (d) => (d as Map<String, Object?>)['nombre'] as String,
    );

    expect(nombre, 'Luna');
  });

  test('adjunta el token y no manda nada que parezca el tenant', () async {
    red.responder(200, exito(null));

    await llamar();

    final cabeceras = red.cabeceras.single;
    expect(cabeceras['Authorization'], 'Bearer token-viejo');
    expect(
      cabeceras.keys.map((k) => k.toLowerCase()),
      everyElement(isNot(anyOf(contains('tenant'), contains('clinica')))),
    );
    expect(red.peticiones.single.queryParameters, isEmpty);
  });

  test('sin sesión no manda Authorization', () async {
    tokens.actual = null;
    red.responder(200, exito(null));

    await llamar();

    expect(red.cabeceras.single.containsKey('Authorization'), isFalse);
  });

  group('errores con el sobre', () {
    final casos = <int, TipoDeError>{
      403: TipoDeError.prohibido,
      404: TipoDeError.noEncontrado,
      409: TipoDeError.conflicto,
      422: TipoDeError.entradaInvalida,
      500: TipoDeError.servidor,
    };
    casos.forEach((estado, tipo) {
      test(
        '$estado se traduce a ${tipo.name} con el mensaje del backend',
        () async {
          red.responder(estado, fallo('Mensaje del backend'));

          await expectLater(
            llamar(),
            throwsA(
              isA<ErrorDeApi>()
                  .having((e) => e.tipo, 'tipo', tipo)
                  .having((e) => e.codigoHttp, 'codigoHttp', estado)
                  .having((e) => e.mensaje, 'mensaje', 'Mensaje del backend'),
            ),
          );
        },
      );
    });

    test('sin cuerpo válido usa el texto genérico del tipo', () async {
      red.responder(403, {});

      await expectLater(
        llamar(),
        throwsA(
          isA<ErrorDeApi>().having(
            (e) => e.mensaje,
            'mensaje',
            ErrorDeApi.mensajePorTipo(TipoDeError.prohibido),
          ),
        ),
      );
    });
  });

  test('sin conexión: se traduce a sinConexion', () async {
    red.fallarConexion();

    await expectLater(
      llamar(),
      throwsA(
        isA<ErrorDeApi>()
            .having((e) => e.tipo, 'tipo', TipoDeError.sinConexion)
            .having((e) => e.codigoHttp, 'codigoHttp', isNull),
      ),
    );
  });

  group('dos tokens según la ruta', () {
    test('en /sesion va el de identidad y en negocio el de clínica', () async {
      red
        ..responder(200, exito(null))
        ..responder(200, exito(null));

      await api.obtener<Object?>('/sesion/clinicas', convertir: (d) => d);
      await llamar();

      expect(red.cabeceras.map((c) => c['Authorization']), [
        'Bearer token-identidad',
        'Bearer token-viejo',
      ]);
    });

    test('un 401 de negocio no se repite con el token de identidad', () async {
      red
        ..responder(401, fallo('Tu sesión venció'))
        ..responder(200, exito('listo'));

      await api.obtener<String>('/health', convertir: (d) => d as String);

      expect(tokens.rutasRenovadas, ['/health']);
      expect(
        red.cabeceras.map((c) => c['Authorization']),
        everyElement(isNot('Bearer token-identidad')),
      );
    });
  });

  group('401 y recuperación de sesión', () {
    test(
      'renueva una vez, repite una vez con el token nuevo y tiene éxito',
      () async {
        red
          ..responder(401, fallo('Tu sesión venció'))
          ..responder(200, exito('listo'));

        final resultado = await api.obtener<String>(
          '/health',
          convertir: (d) => d as String,
        );

        expect(resultado, 'listo');
        expect(tokens.renovaciones, 1);
        expect(tokens.expiraciones, 0);
        expect(red.cabeceras.map((c) => c['Authorization']), [
          'Bearer token-viejo',
          'Bearer token-nuevo',
        ]);
      },
    );

    test(
      'si la renovación falla, avisa que la sesión expiró y no repite',
      () async {
        tokens = TokensFalsos(alRenovar: null);
        api = ApiCliente(
          urlBase: 'http://api.prueba/api/v1',
          tokens: tokens,
          adaptador: red,
        );
        red.responder(401, fallo('Tu sesión venció'));

        await expectLater(
          llamar(),
          throwsA(
            isA<ErrorDeApi>().having(
              (e) => e.tipo,
              'tipo',
              TipoDeError.noAutenticado,
            ),
          ),
        );

        expect(red.peticiones, hasLength(1));
        expect(tokens.renovaciones, 1);
        expect(tokens.expiraciones, 1);
      },
    );

    test('si tras renovar sigue el 401, no entra en bucle', () async {
      red
        ..responder(401, fallo('Tu sesión venció'))
        ..responder(401, fallo('Sigue sin valer'));

      await expectLater(
        llamar(),
        throwsA(
          isA<ErrorDeApi>().having(
            (e) => e.mensaje,
            'mensaje',
            'Sigue sin valer',
          ),
        ),
      );

      expect(red.peticiones, hasLength(2));
      expect(tokens.renovaciones, 1);
      expect(tokens.expiraciones, 1);
    });
  });

  test('paginación: lee contenido, página, tamaño y total', () async {
    red.responder(
      200,
      exito({
        'contenido': [
          {'id': 1},
          {'id': 2},
        ],
        'pagina': 0,
        'tamano': 20,
        'total': 2,
      }),
    );

    final pagina = await api.obtenerPagina<int>(
      '/busqueda',
      parametros: {'q': 'luna', 'page': 0, 'size': 20},
      convertir: (e) => (e as Map<String, Object?>)['id'] as int,
    );

    expect(pagina.contenido, [1, 2]);
    expect(pagina.pagina, 0);
    expect(pagina.tamano, 20);
    expect(pagina.total, 2);
    expect(red.peticiones.single.queryParameters, {
      'q': 'luna',
      'page': 0,
      'size': 20,
    });
  });

  test('un data con otra forma de la esperada no revienta la app', () async {
    red.responder(200, exito('texto'));

    await expectLater(
      api.obtener<int>(
        '/health',
        convertir: (d) => (d as Map<String, Object?>).length,
      ),
      throwsA(
        isA<ErrorDeApi>().having(
          (e) => e.tipo,
          'tipo',
          TipoDeError.desconocido,
        ),
      ),
    );
  });

  test('POST y PUT mandan el cuerpo como JSON', () async {
    red
      ..responder(201, exito(null))
      ..responder(200, exito(null));

    await api.enviar<Object?>(
      '/propietarios',
      cuerpo: {'nombre': 'Ana'},
      convertir: (d) => d,
    );
    await api.actualizar<Object?>(
      '/propietarios/1',
      cuerpo: {'nombre': 'Ana B'},
      convertir: (d) => d,
    );

    expect(red.peticiones.map((p) => p.method), ['POST', 'PUT']);
    expect(red.peticiones.first.data, {'nombre': 'Ana'});
  });

  test('desdeEntorno exige la variable API_BASE_URL', () {
    expect(
      () => ApiCliente.desdeEntorno(tokens: tokens),
      throwsA(isA<StateError>()),
    );
  });
}
