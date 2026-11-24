import 'package:dio/dio.dart';

import 'error_de_api.dart';

/// Cliente HTTP de la API. Habla el sobre `{success, message, data}`: en éxito
/// devuelve `data` convertido y en error lanza un [ErrorDeApi]. Lo usan los
/// repositorios; la interfaz nunca llama a la API directamente.
class ApiCliente {
  ApiCliente({
    required String urlBase,
    HttpClientAdapter? adaptador,
    Duration tiempoDeConexion = const Duration(seconds: 10),
    Duration tiempoDeRespuesta = const Duration(seconds: 20),
  }) : _dio = Dio(
         BaseOptions(
           baseUrl: urlBase,
           connectTimeout: tiempoDeConexion,
           receiveTimeout: tiempoDeRespuesta,
           headers: {Headers.acceptHeader: Headers.jsonContentType},
           contentType: Headers.jsonContentType,
         ),
       ) {
    if (adaptador != null) _dio.httpClientAdapter = adaptador;
  }

  /// URL base de `--dart-define=API_BASE_URL`; nunca hay una fija en el código.
  factory ApiCliente.desdeEntorno() {
    const urlBase = String.fromEnvironment('API_BASE_URL');
    if (urlBase.isEmpty) {
      throw StateError(
        'Falta --dart-define=API_BASE_URL con la URL de la API.',
      );
    }
    return ApiCliente(urlBase: urlBase);
  }

  final Dio _dio;

  /// GET que devuelve el `data` de la respuesta convertido con [convertir].
  Future<T> obtener<T>(
    String ruta, {
    Map<String, Object?>? parametros,
    required T Function(Object? datos) convertir,
  }) => solicitar('GET', ruta, parametros: parametros, convertir: convertir);

  /// POST con cuerpo JSON.
  Future<T> enviar<T>(
    String ruta, {
    Object? cuerpo,
    required T Function(Object? datos) convertir,
  }) => solicitar('POST', ruta, cuerpo: cuerpo, convertir: convertir);

  /// PUT con cuerpo JSON.
  Future<T> actualizar<T>(
    String ruta, {
    Object? cuerpo,
    required T Function(Object? datos) convertir,
  }) => solicitar('PUT', ruta, cuerpo: cuerpo, convertir: convertir);

  Future<T> solicitar<T>(
    String metodo,
    String ruta, {
    Object? cuerpo,
    Map<String, Object?>? parametros,
    required T Function(Object? datos) convertir,
  }) async {
    final Response<Object?> respuesta;
    try {
      respuesta = await _dio.request<Object?>(
        ruta,
        data: cuerpo,
        queryParameters: parametros,
        options: Options(method: metodo),
      );
    } on DioException catch (e) {
      throw _traducirError(e);
    }
    return _traducirExito(respuesta, convertir);
  }

  T _traducirExito<T>(
    Response<Object?> respuesta,
    T Function(Object? datos) convertir,
  ) {
    final sobre = respuesta.data;
    if (sobre is! Map<String, Object?> || sobre['success'] != true) {
      throw _errorDeSobre(respuesta.statusCode, sobre);
    }
    try {
      return convertir(sobre['data']);
    } on TypeError {
      throw const ErrorDeApi(
        tipo: TipoDeError.desconocido,
        mensaje: 'No se pudo leer la respuesta del servidor.',
      );
    }
  }

  ErrorDeApi _traducirError(DioException e) {
    return switch (e.type) {
      DioExceptionType.connectionTimeout ||
      DioExceptionType.sendTimeout ||
      DioExceptionType.receiveTimeout ||
      DioExceptionType.transformTimeout ||
      DioExceptionType.connectionError ||
      DioExceptionType.badCertificate => ErrorDeApi(
        tipo: TipoDeError.sinConexion,
        mensaje: ErrorDeApi.mensajePorTipo(TipoDeError.sinConexion),
      ),
      DioExceptionType.badResponse => _errorDeSobre(
        e.response?.statusCode,
        e.response?.data,
      ),
      DioExceptionType.cancel || DioExceptionType.unknown => ErrorDeApi(
        tipo: TipoDeError.desconocido,
        mensaje: ErrorDeApi.mensajePorTipo(TipoDeError.desconocido),
      ),
    };
  }

  /// Error a partir del código HTTP y, si lo hay, el mensaje del sobre.
  ErrorDeApi _errorDeSobre(int? estado, Object? cuerpo) {
    final tipo = ErrorDeApi.tipoPorEstado(estado);
    final mensaje = cuerpo is Map<String, Object?> ? cuerpo['message'] : null;
    return ErrorDeApi(
      tipo: tipo,
      codigoHttp: estado,
      mensaje: mensaje is String && mensaje.isNotEmpty
          ? mensaje
          : ErrorDeApi.mensajePorTipo(tipo),
    );
  }
}
