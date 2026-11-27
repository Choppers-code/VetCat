// ignore_for_file: invalid_annotation_target
import 'package:freezed_annotation/freezed_annotation.dart';

import 'mascota_response.dart';

part 'propietario.freezed.dart';
part 'propietario.g.dart';

/// Ficha de un propietario con sus mascotas activas.
@freezed
abstract class PropietarioResponse with _$PropietarioResponse {
  const factory PropietarioResponse({
    required int id,
    required String nombre,
    required String telefono,
    required bool activo,
    @Default([]) List<MascotaResponse> mascotas,
    String? correo,
    String? direccion,
  }) = _PropietarioResponse;

  factory PropietarioResponse.fromJson(Map<String, Object?> json) =>
      _$PropietarioResponseFromJson(json);
}

/// Cuerpo de alta y edición. Lo que va vacío no se manda.
@freezed
abstract class PropietarioRequest with _$PropietarioRequest {
  @JsonSerializable(includeIfNull: false)
  const factory PropietarioRequest({
    required String nombre,
    required String telefono,
    String? correo,

    /// Solo si el plan incluye `visitas_domicilio`.
    String? direccion,

    /// Crea el propietario aunque el teléfono ya exista.
    @Default(false) bool confirmarDuplicado,
  }) = _PropietarioRequest;

  factory PropietarioRequest.fromJson(Map<String, Object?> json) =>
      _$PropietarioRequestFromJson(json);
}

/// Un resultado de `/busqueda`: el propietario y, si coincidió por ella, la
/// mascota.
@freezed
abstract class ResultadoDeBusqueda with _$ResultadoDeBusqueda {
  const factory ResultadoDeBusqueda({
    required PropietarioResponse propietario,
    MascotaResponse? mascota,
  }) = _ResultadoDeBusqueda;

  factory ResultadoDeBusqueda.fromJson(Map<String, Object?> json) =>
      _$ResultadoDeBusquedaFromJson(json);
}
