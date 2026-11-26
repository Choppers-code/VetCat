import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vetcat_core/vetcat_core.dart';

import '../sesion/controlador_de_clinica.dart';

/// Llamadas del módulo de propietarios (contrato `docs/api/v1/openapi.yaml`).
class RepositorioDePropietarios {
  RepositorioDePropietarios(this._api);

  final ApiCliente _api;

  /// No hay lista completa: solo búsqueda, de 3 caracteres o más.
  Future<Pagina<ResultadoDeBusqueda>> buscar(String texto, {int pagina = 0}) =>
      _api.obtenerPagina(
        '/busqueda',
        parametros: {'q': texto, 'page': pagina, 'size': 20},
        convertir: (e) =>
            ResultadoDeBusqueda.fromJson(e! as Map<String, Object?>),
      );

  Future<PropietarioResponse> ficha(int id) =>
      _api.obtener('/propietarios/$id', convertir: _propietario);

  Future<PropietarioResponse> registrar(PropietarioRequest datos) => _api
      .enviar('/propietarios', cuerpo: datos.toJson(), convertir: _propietario);

  Future<PropietarioResponse> editar(int id, PropietarioRequest datos) =>
      _api.actualizar(
        '/propietarios/$id',
        cuerpo: datos.toJson(),
        convertir: _propietario,
      );

  static PropietarioResponse _propietario(Object? datos) =>
      PropietarioResponse.fromJson(datos! as Map<String, Object?>);
}

final repositorioDePropietariosProvider = Provider<RepositorioDePropietarios>(
  (ref) => RepositorioDePropietarios(ref.watch(apiProvider)),
);

/// Si el plan de la clínica incluye `visitas_domicilio` (PLA-004). **Todavía no
/// hay de dónde leerlo**: el token no trae las funciones del plan y su
/// verificación está aparcada (#156). Hasta entonces la dirección no se ofrece.
final visitasDomicilioProvider = Provider<bool>((ref) => false);
