// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mascota_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_MascotaResponse _$MascotaResponseFromJson(Map<String, dynamic> json) =>
    _MascotaResponse(
      id: (json['id'] as num).toInt(),
      propietarioId: (json['propietarioId'] as num).toInt(),
      nombre: json['nombre'] as String,
      especie: $enumDecode(_$EspecieEnumMap, json['especie']),
      activo: json['activo'] as bool,
      raza: json['raza'] as String?,
    );

Map<String, dynamic> _$MascotaResponseToJson(_MascotaResponse instance) =>
    <String, dynamic>{
      'id': instance.id,
      'propietarioId': instance.propietarioId,
      'nombre': instance.nombre,
      'especie': _$EspecieEnumMap[instance.especie]!,
      'activo': instance.activo,
      'raza': instance.raza,
    };

const _$EspecieEnumMap = {
  Especie.perro: 'PERRO',
  Especie.gato: 'GATO',
  Especie.ave: 'AVE',
  Especie.roedor: 'ROEDOR',
  Especie.reptil: 'REPTIL',
  Especie.otra: 'OTRA',
};
