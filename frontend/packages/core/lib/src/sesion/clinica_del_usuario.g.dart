// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'clinica_del_usuario.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_ClinicaDelUsuario _$ClinicaDelUsuarioFromJson(Map<String, dynamic> json) =>
    _ClinicaDelUsuario(
      id: (json['id'] as num).toInt(),
      nombre: json['nombre'] as String,
      estado: json['estado'] as String,
      roles: (json['roles'] as List<dynamic>).map((e) => e as String).toList(),
    );

Map<String, dynamic> _$ClinicaDelUsuarioToJson(_ClinicaDelUsuario instance) =>
    <String, dynamic>{
      'id': instance.id,
      'nombre': instance.nombre,
      'estado': instance.estado,
      'roles': instance.roles,
    };
