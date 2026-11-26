// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'propietario.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_PropietarioResponse _$PropietarioResponseFromJson(Map<String, dynamic> json) =>
    _PropietarioResponse(
      id: (json['id'] as num).toInt(),
      nombre: json['nombre'] as String,
      telefono: json['telefono'] as String,
      activo: json['activo'] as bool,
      mascotas:
          (json['mascotas'] as List<dynamic>?)
              ?.map((e) => MascotaResponse.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const [],
      correo: json['correo'] as String?,
      direccion: json['direccion'] as String?,
    );

Map<String, dynamic> _$PropietarioResponseToJson(
  _PropietarioResponse instance,
) => <String, dynamic>{
  'id': instance.id,
  'nombre': instance.nombre,
  'telefono': instance.telefono,
  'activo': instance.activo,
  'mascotas': instance.mascotas,
  'correo': instance.correo,
  'direccion': instance.direccion,
};

_PropietarioRequest _$PropietarioRequestFromJson(Map<String, dynamic> json) =>
    _PropietarioRequest(
      nombre: json['nombre'] as String,
      telefono: json['telefono'] as String,
      correo: json['correo'] as String?,
      direccion: json['direccion'] as String?,
      confirmarDuplicado: json['confirmarDuplicado'] as bool? ?? false,
    );

Map<String, dynamic> _$PropietarioRequestToJson(_PropietarioRequest instance) =>
    <String, dynamic>{
      'nombre': instance.nombre,
      'telefono': instance.telefono,
      'correo': ?instance.correo,
      'direccion': ?instance.direccion,
      'confirmarDuplicado': instance.confirmarDuplicado,
    };

_ResultadoDeBusqueda _$ResultadoDeBusquedaFromJson(Map<String, dynamic> json) =>
    _ResultadoDeBusqueda(
      propietario: PropietarioResponse.fromJson(
        json['propietario'] as Map<String, dynamic>,
      ),
      mascota: json['mascota'] == null
          ? null
          : MascotaResponse.fromJson(json['mascota'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$ResultadoDeBusquedaToJson(
  _ResultadoDeBusqueda instance,
) => <String, dynamic>{
  'propietario': instance.propietario,
  'mascota': instance.mascota,
};
