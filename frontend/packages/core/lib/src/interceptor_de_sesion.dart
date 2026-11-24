import 'package:dio/dio.dart';

import 'proveedor_de_token.dart';

/// Único interceptor del cliente: adjunta el token y, ante un 401, refresca la
/// sesión una sola vez y repite la petición una sola vez.
///
/// No registra nada: el token no debe aparecer en ningún registro. Tampoco
/// añade el tenant: viaja dentro del token, nunca en la petición.
class InterceptorDeSesion extends Interceptor {
  InterceptorDeSesion(this._dio, this._tokens);

  static const _marcaDeReintento = 'vetcat_reintentado';

  final Dio _dio;
  final ProveedorDeToken _tokens;

  /// Refresco en curso, para que varias peticiones con 401 compartan uno solo.
  Future<bool>? _refresco;

  @override
  Future<void> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final token = await _tokens.token();
    if (token != null && token.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    if (err.response?.statusCode != 401) {
      handler.next(err);
      return;
    }
    final peticion = err.requestOptions;
    if (peticion.extra[_marcaDeReintento] == true) {
      // Ya se repitió con un token nuevo y sigue sin valer: no hay bucle.
      _tokens.sesionExpirada();
      handler.next(err);
      return;
    }
    final renovado = await (_refresco ??= _renovar());
    if (!renovado) {
      _tokens.sesionExpirada();
      handler.next(err);
      return;
    }
    peticion.extra[_marcaDeReintento] = true;
    try {
      handler.resolve(await _dio.fetch<Object?>(peticion));
    } on DioException catch (e) {
      handler.next(e);
    }
  }

  Future<bool> _renovar() async {
    try {
      return await _tokens.refrescar();
    } on Object {
      return false;
    } finally {
      _refresco = null;
    }
  }
}
