// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sesion_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_SesionResponse _$SesionResponseFromJson(Map<String, dynamic> json) =>
    _SesionResponse(
      token: json['token'] as String,
      clinicaId: (json['clinicaId'] as num).toInt(),
      nombre: json['nombre'] as String,
      roles: (json['roles'] as List<dynamic>).map((e) => e as String).toList(),
    );

Map<String, dynamic> _$SesionResponseToJson(_SesionResponse instance) =>
    <String, dynamic>{
      'token': instance.token,
      'clinicaId': instance.clinicaId,
      'nombre': instance.nombre,
      'roles': instance.roles,
    };
