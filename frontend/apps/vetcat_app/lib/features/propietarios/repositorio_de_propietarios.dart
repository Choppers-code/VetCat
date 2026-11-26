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

  static PropietarioResponse _propietario(Object? datos) =>
      PropietarioResponse.fromJson(datos! as Map<String, Object?>);
}

final repositorioDePropietariosProvider = Provider<RepositorioDePropietarios>(
  (ref) => RepositorioDePropietarios(ref.watch(apiProvider)),
);
