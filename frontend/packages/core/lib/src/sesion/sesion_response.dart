import 'package:freezed_annotation/freezed_annotation.dart';

part 'sesion_response.freezed.dart';
part 'sesion_response.g.dart';

/// Respuesta de `POST /api/v1/sesion/clinicas/{id}`: el token de clínica y a
/// qué clínica pertenece. Vive solo en memoria (ADR-0003).
@freezed
abstract class SesionResponse with _$SesionResponse {
  const factory SesionResponse({
    required String token,
    required int clinicaId,
    required String nombre,
    required List<String> roles,
  }) = _SesionResponse;

  factory SesionResponse.fromJson(Map<String, Object?> json) =>
      _$SesionResponseFromJson(json);

  const SesionResponse._();

  /// Sin el token: este texto acaba en registros y mensajes de prueba.
  @override
  String toString() => 'SesionResponse(clinicaId: $clinicaId, roles: $roles)';
}
