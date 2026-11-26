// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
// ignore_for_file: type=lint, type=warning, deprecated_member_use, deprecated_member_use_from_same_package
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'mascota_response.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$MascotaResponse {

 int get id; int get propietarioId; String get nombre; Especie get especie; bool get activo; String? get raza;
/// Create a copy of MascotaResponse
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$MascotaResponseCopyWith<MascotaResponse> get copyWith => _$MascotaResponseCopyWithImpl<MascotaResponse>(this as MascotaResponse, _$identity);

  /// Serializes this MascotaResponse to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  final _this = this as MascotaResponse;
  return identical(this, other) || (other.runtimeType == runtimeType&&other is MascotaResponse&&(identical(other.id, _this.id) || other.id == _this.id)&&(identical(other.propietarioId, _this.propietarioId) || other.propietarioId == _this.propietarioId)&&(identical(other.nombre, _this.nombre) || other.nombre == _this.nombre)&&(identical(other.especie, _this.especie) || other.especie == _this.especie)&&(identical(other.activo, _this.activo) || other.activo == _this.activo)&&(identical(other.raza, _this.raza) || other.raza == _this.raza));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode {
  final _this = this as MascotaResponse;
  return Object.hash(runtimeType,_this.id,_this.propietarioId,_this.nombre,_this.especie,_this.activo,_this.raza);
}

@override
String toString() {
  final _this = this as MascotaResponse;
  return 'MascotaResponse(id: ${_this.id}, propietarioId: ${_this.propietarioId}, nombre: ${_this.nombre}, especie: ${_this.especie}, activo: ${_this.activo}, raza: ${_this.raza})';
}


}

/// @nodoc
abstract mixin class $MascotaResponseCopyWith<$Res>  {
  factory $MascotaResponseCopyWith(MascotaResponse value, $Res Function(MascotaResponse) _then) = _$MascotaResponseCopyWithImpl;
@useResult
$Res call({
 int id, int propietarioId, String nombre, Especie especie, bool activo, String? raza
});




}
/// @nodoc
class _$MascotaResponseCopyWithImpl<$Res>
    implements $MascotaResponseCopyWith<$Res> {
  _$MascotaResponseCopyWithImpl(this._self, this._then);

  final MascotaResponse _self;
  final $Res Function(MascotaResponse) _then;

/// Create a copy of MascotaResponse
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? id = null,Object? propietarioId = null,Object? nombre = null,Object? especie = null,Object? activo = null,Object? raza = freezed,}) {
  return _then(MascotaResponse(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as int,propietarioId: null == propietarioId ? _self.propietarioId : propietarioId // ignore: cast_nullable_to_non_nullable
as int,nombre: null == nombre ? _self.nombre : nombre // ignore: cast_nullable_to_non_nullable
as String,especie: null == especie ? _self.especie : especie // ignore: cast_nullable_to_non_nullable
as Especie,activo: null == activo ? _self.activo : activo // ignore: cast_nullable_to_non_nullable
as bool,raza: freezed == raza ? _self.raza : raza // ignore: cast_nullable_to_non_nullable
as String?,
  ));
}

}


/// Adds pattern-matching-related methods to [MascotaResponse].
extension MascotaResponsePatterns on MascotaResponse {
/// A variant of `map` that fallback to returning `orElse`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _MascotaResponse value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _MascotaResponse() when $default != null:
return $default(_that);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// Callbacks receives the raw object, upcasted.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case final Subclass2 value:
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _MascotaResponse value)  $default,){
final _that = this;
switch (_that) {
case _MascotaResponse():
return $default(_that);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `map` that fallback to returning `null`.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case final Subclass value:
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _MascotaResponse value)?  $default,){
final _that = this;
switch (_that) {
case _MascotaResponse() when $default != null:
return $default(_that);case _:
  return null;

}
}
/// A variant of `when` that fallback to an `orElse` callback.
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return orElse();
/// }
/// ```

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( int id,  int propietarioId,  String nombre,  Especie especie,  bool activo,  String? raza)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _MascotaResponse() when $default != null:
return $default(_that.id,_that.propietarioId,_that.nombre,_that.especie,_that.activo,_that.raza);case _:
  return orElse();

}
}
/// A `switch`-like method, using callbacks.
///
/// As opposed to `map`, this offers destructuring.
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case Subclass2(:final field2):
///     return ...;
/// }
/// ```

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( int id,  int propietarioId,  String nombre,  Especie especie,  bool activo,  String? raza)  $default,) {final _that = this;
switch (_that) {
case _MascotaResponse():
return $default(_that.id,_that.propietarioId,_that.nombre,_that.especie,_that.activo,_that.raza);case _:
  throw StateError('Unexpected subclass');

}
}
/// A variant of `when` that fallback to returning `null`
///
/// It is equivalent to doing:
/// ```dart
/// switch (sealedClass) {
///   case Subclass(:final field):
///     return ...;
///   case _:
///     return null;
/// }
/// ```

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( int id,  int propietarioId,  String nombre,  Especie especie,  bool activo,  String? raza)?  $default,) {final _that = this;
switch (_that) {
case _MascotaResponse() when $default != null:
return $default(_that.id,_that.propietarioId,_that.nombre,_that.especie,_that.activo,_that.raza);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _MascotaResponse implements MascotaResponse {
  const _MascotaResponse({required this.id, required this.propietarioId, required this.nombre, required this.especie, required this.activo, this.raza});
  factory _MascotaResponse.fromJson(Map<String, dynamic> json) => _$MascotaResponseFromJson(json);

@override final  int id;
@override final  int propietarioId;
@override final  String nombre;
@override final  Especie especie;
@override final  bool activo;
@override final  String? raza;

/// Create a copy of MascotaResponse
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$MascotaResponseCopyWith<_MascotaResponse> get copyWith => __$MascotaResponseCopyWithImpl<_MascotaResponse>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$MascotaResponseToJson(this, );
}

@override
bool operator ==(Object other) {
    return identical(this, other) || (other.runtimeType == runtimeType&&other is _MascotaResponse&&(identical(other.id, id) || other.id == id)&&(identical(other.propietarioId, propietarioId) || other.propietarioId == propietarioId)&&(identical(other.nombre, nombre) || other.nombre == nombre)&&(identical(other.especie, especie) || other.especie == especie)&&(identical(other.activo, activo) || other.activo == activo)&&(identical(other.raza, raza) || other.raza == raza));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode {
    return Object.hash(runtimeType,id,propietarioId,nombre,especie,activo,raza);
}

@override
String toString() {
    return 'MascotaResponse(id: $id, propietarioId: $propietarioId, nombre: $nombre, especie: $especie, activo: $activo, raza: $raza)';
}


}

/// @nodoc
abstract mixin class _$MascotaResponseCopyWith<$Res> implements $MascotaResponseCopyWith<$Res> {
  factory _$MascotaResponseCopyWith(_MascotaResponse value, $Res Function(_MascotaResponse) _then) = __$MascotaResponseCopyWithImpl;
@override @useResult
$Res call({
 int id, int propietarioId, String nombre, Especie especie, bool activo, String? raza
});




}
/// @nodoc
class __$MascotaResponseCopyWithImpl<$Res>
    implements _$MascotaResponseCopyWith<$Res> {
  __$MascotaResponseCopyWithImpl(this._self, this._then);

  final _MascotaResponse _self;
  final $Res Function(_MascotaResponse) _then;

/// Create a copy of MascotaResponse
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? id = null,Object? propietarioId = null,Object? nombre = null,Object? especie = null,Object? activo = null,Object? raza = freezed,}) {
  return _then(_MascotaResponse(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as int,propietarioId: null == propietarioId ? _self.propietarioId : propietarioId // ignore: cast_nullable_to_non_nullable
as int,nombre: null == nombre ? _self.nombre : nombre // ignore: cast_nullable_to_non_nullable
as String,especie: null == especie ? _self.especie : especie // ignore: cast_nullable_to_non_nullable
as Especie,activo: null == activo ? _self.activo : activo // ignore: cast_nullable_to_non_nullable
as bool,raza: freezed == raza ? _self.raza : raza // ignore: cast_nullable_to_non_nullable
as String?,
  ));
}


}

// dart format on
