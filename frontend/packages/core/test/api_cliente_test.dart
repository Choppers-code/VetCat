import 'dart:convert';
import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_core/vetcat_core.dart';

/// Adaptador HTTP falso: devuelve respuestas preparadas, sin tocar la red.
class AdaptadorFalso implements HttpClientAdapter {
  final List<ResponseBody Function(RequestOptions)> _respuestas = [];

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
    return _respuestas.removeAt(0)(options);
  }

  @override
  void close({bool force = false}) {}
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
  late ApiCliente api;

  setUp(() {
    red = AdaptadorFalso();
    api = ApiCliente(urlBase: 'http://api.prueba/api/v1', adaptador: red);
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
}
