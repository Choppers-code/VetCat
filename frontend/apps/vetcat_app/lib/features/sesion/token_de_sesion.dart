import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vetcat_core/vetcat_core.dart';

import '../../sesion/controlador_de_sesion.dart';
import 'controlador_de_clinica.dart';

/// Qué token va en cada ruta y cómo se recupera (ADR-0003): identidad en
/// `/sesion/**` y `/plataforma/**`, token de clínica en el resto.
class ProveedorDeTokenDeSesion implements ProveedorDeToken {
  ProveedorDeTokenDeSesion(this._ref);

  final Ref _ref;

  /// El último fallo fue de una ruta de negocio: la identidad sigue buena y
  /// basta con volver al selector, sin pedir credenciales.
  bool _alSelector = false;

  static bool _esDeIdentidad(String ruta) =>
      ruta.startsWith('/sesion/') || ruta.startsWith('/plataforma/');

  ControladorDeSesion get _sesion => _ref.read(sesionProvider.notifier);

  @override
  Future<String?> token(String ruta) async {
    if (!_esDeIdentidad(ruta)) {
      final clinica = _ref.read(clinicaProvider);
      return clinica is EnClinica ? clinica.sesion.token : null;
    }
    _alSelector = false;
    final estado = _ref.read(sesionProvider);
    if (estado is! ConSesion) return null;
    if (!estado.tokens.vigente) await _sesion.renovarIdentidad();
    final actual = _ref.read(sesionProvider);
    return actual is ConSesion ? actual.tokens.acceso : null;
  }

  @override
  Future<bool> renovar(String ruta) async {
    if (_esDeIdentidad(ruta)) {
      _alSelector = false;
      return _sesion.renovarIdentidad();
    }
    // El token de clínica no se refresca: se vuelve a abrir la sesión con la
    // identidad, renovándola antes si hace falta.
    final estado = _ref.read(sesionProvider);
    if (estado is! ConSesion) return false;
    if (!estado.tokens.vigente && !await _sesion.renovarIdentidad()) {
      _alSelector = false;
      return false;
    }
    _alSelector = true;
    return _ref.read(clinicaProvider.notifier).reabrir();
  }

  @override
  void sesionExpirada() {
    if (_alSelector) {
      _alSelector = false;
      _ref.read(clinicaProvider.notifier).volverAlSelector();
    } else {
      _sesion.expirar();
    }
  }
}
