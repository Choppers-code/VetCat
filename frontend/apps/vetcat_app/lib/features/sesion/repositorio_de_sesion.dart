import 'package:vetcat_core/vetcat_core.dart';

/// Las dos llamadas de sesión. Van con el token de identidad (ADR-0003).
class RepositorioDeSesion {
  RepositorioDeSesion(this._api);

  final ApiCliente _api;

  Future<List<ClinicaDelUsuario>> clinicas() => _api.obtener(
    '/sesion/clinicas',
    convertir: (datos) => [
      for (final e in datos! as List<Object?>)
        ClinicaDelUsuario.fromJson(e! as Map<String, Object?>),
    ],
  );

  /// Abre la sesión de una clínica y devuelve su token (15 minutos).
  Future<SesionResponse> abrir(int clinicaId) => _api.enviar(
    '/sesion/clinicas/$clinicaId',
    convertir: (datos) =>
        SesionResponse.fromJson(datos! as Map<String, Object?>),
  );
}
