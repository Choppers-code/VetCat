import 'package:freezed_annotation/freezed_annotation.dart';

part 'mascota_response.freezed.dart';
part 'mascota_response.g.dart';

enum Especie {
  @JsonValue('PERRO')
  perro('Perro'),
  @JsonValue('GATO')
  gato('Gato'),
  @JsonValue('AVE')
  ave('Ave'),
  @JsonValue('ROEDOR')
  roedor('Roedor'),
  @JsonValue('REPTIL')
  reptil('Reptil'),
  @JsonValue('OTRA')
  otra('Otra');

  const Especie(this.etiqueta);
  final String etiqueta;
}

/// Mascota tal como la devuelve la API (`MascotaResponse` del contrato).
@freezed
abstract class MascotaResponse with _$MascotaResponse {
  const factory MascotaResponse({
    required int id,
    required int propietarioId,
    required String nombre,
    required Especie especie,
    required bool activo,
    String? raza,
  }) = _MascotaResponse;

  factory MascotaResponse.fromJson(Map<String, Object?> json) =>
      _$MascotaResponseFromJson(json);
}
