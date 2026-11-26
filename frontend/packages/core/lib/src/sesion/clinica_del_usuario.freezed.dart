// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
// ignore_for_file: type=lint, type=warning, deprecated_member_use, deprecated_member_use_from_same_package
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'clinica_del_usuario.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$ClinicaDelUsuario {

 int get id; String get nombre;/// `ACTIVA`, `SUSPENDIDA` o `BAJA`.
 String get estado;/// Roles del usuario en esa clínica, no en la clínica activa.
 List<String> get roles;
/// Create a copy of ClinicaDelUsuario
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$ClinicaDelUsuarioCopyWith<ClinicaDelUsuario> get copyWith => _$ClinicaDelUsuarioCopyWithImpl<ClinicaDelUsuario>(this as ClinicaDelUsuario, _$identity);

  /// Serializes this ClinicaDelUsuario to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  final _this = this as ClinicaDelUsuario;
  return identical(this, other) || (other.runtimeType == runtimeType&&other is ClinicaDelUsuario&&(identical(other.id, _this.id) || other.id == _this.id)&&(identical(other.nombre, _this.nombre) || other.nombre == _this.nombre)&&(identical(other.estado, _this.estado) || other.estado == _this.estado)&&const DeepCollectionEquality().equals(other.roles, _this.roles));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode {
  final _this = this as ClinicaDelUsuario;
  return Object.hash(runtimeType,_this.id,_this.nombre,_this.estado,const DeepCollectionEquality().hash(_this.roles));
}

@override
String toString() {
  final _this = this as ClinicaDelUsuario;
  return 'ClinicaDelUsuario(id: ${_this.id}, nombre: ${_this.nombre}, estado: ${_this.estado}, roles: ${_this.roles})';
}


}

/// @nodoc
abstract mixin class $ClinicaDelUsuarioCopyWith<$Res>  {
  factory $ClinicaDelUsuarioCopyWith(ClinicaDelUsuario value, $Res Function(ClinicaDelUsuario) _then) = _$ClinicaDelUsuarioCopyWithImpl;
@useResult
$Res call({
 int id, String nombre, String estado, List<String> roles
});




}
/// @nodoc
class _$ClinicaDelUsuarioCopyWithImpl<$Res>
    implements $ClinicaDelUsuarioCopyWith<$Res> {
  _$ClinicaDelUsuarioCopyWithImpl(this._self, this._then);

  final ClinicaDelUsuario _self;
  final $Res Function(ClinicaDelUsuario) _then;

/// Create a copy of ClinicaDelUsuario
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? id = null,Object? nombre = null,Object? estado = null,Object? roles = null,}) {
  return _then(ClinicaDelUsuario(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as int,nombre: null == nombre ? _self.nombre : nombre // ignore: cast_nullable_to_non_nullable
as String,estado: null == estado ? _self.estado : estado // ignore: cast_nullable_to_non_nullable
as String,roles: null == roles ? _self.roles : roles // ignore: cast_nullable_to_non_nullable
as List<String>,
  ));
}

}


/// Adds pattern-matching-related methods to [ClinicaDelUsuario].
extension ClinicaDelUsuarioPatterns on ClinicaDelUsuario {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _ClinicaDelUsuario value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _ClinicaDelUsuario() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _ClinicaDelUsuario value)  $default,){
final _that = this;
switch (_that) {
case _ClinicaDelUsuario():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _ClinicaDelUsuario value)?  $default,){
final _that = this;
switch (_that) {
case _ClinicaDelUsuario() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( int id,  String nombre,  String estado,  List<String> roles)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _ClinicaDelUsuario() when $default != null:
return $default(_that.id,_that.nombre,_that.estado,_that.roles);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( int id,  String nombre,  String estado,  List<String> roles)  $default,) {final _that = this;
switch (_that) {
case _ClinicaDelUsuario():
return $default(_that.id,_that.nombre,_that.estado,_that.roles);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( int id,  String nombre,  String estado,  List<String> roles)?  $default,) {final _that = this;
switch (_that) {
case _ClinicaDelUsuario() when $default != null:
return $default(_that.id,_that.nombre,_that.estado,_that.roles);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _ClinicaDelUsuario extends ClinicaDelUsuario {
  const _ClinicaDelUsuario({required this.id, required this.nombre, required this.estado, required  List<String> roles}): _roles = roles,super._();
  factory _ClinicaDelUsuario.fromJson(Map<String, dynamic> json) => _$ClinicaDelUsuarioFromJson(json);

@override final  int id;
@override final  String nombre;
/// `ACTIVA`, `SUSPENDIDA` o `BAJA`.
@override final  String estado;
/// Roles del usuario en esa clínica, no en la clínica activa.
 final  List<String> _roles;
/// Roles del usuario en esa clínica, no en la clínica activa.
@override List<String> get roles {
  if (_roles is EqualUnmodifiableListView) return _roles;
  // ignore: implicit_dynamic_type
  return EqualUnmodifiableListView(_roles);
}


/// Create a copy of ClinicaDelUsuario
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$ClinicaDelUsuarioCopyWith<_ClinicaDelUsuario> get copyWith => __$ClinicaDelUsuarioCopyWithImpl<_ClinicaDelUsuario>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$ClinicaDelUsuarioToJson(this, );
}

@override
bool operator ==(Object other) {
    return identical(this, other) || (other.runtimeType == runtimeType&&other is _ClinicaDelUsuario&&(identical(other.id, id) || other.id == id)&&(identical(other.nombre, nombre) || other.nombre == nombre)&&(identical(other.estado, estado) || other.estado == estado)&&const DeepCollectionEquality().equals(other.roles, _roles));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode {
    return Object.hash(runtimeType,id,nombre,estado,const DeepCollectionEquality().hash(_roles));
}

@override
String toString() {
    return 'ClinicaDelUsuario(id: $id, nombre: $nombre, estado: $estado, roles: $roles)';
}


}

/// @nodoc
abstract mixin class _$ClinicaDelUsuarioCopyWith<$Res> implements $ClinicaDelUsuarioCopyWith<$Res> {
  factory _$ClinicaDelUsuarioCopyWith(_ClinicaDelUsuario value, $Res Function(_ClinicaDelUsuario) _then) = __$ClinicaDelUsuarioCopyWithImpl;
@override @useResult
$Res call({
 int id, String nombre, String estado, List<String> roles
});




}
/// @nodoc
class __$ClinicaDelUsuarioCopyWithImpl<$Res>
    implements _$ClinicaDelUsuarioCopyWith<$Res> {
  __$ClinicaDelUsuarioCopyWithImpl(this._self, this._then);

  final _ClinicaDelUsuario _self;
  final $Res Function(_ClinicaDelUsuario) _then;

/// Create a copy of ClinicaDelUsuario
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? id = null,Object? nombre = null,Object? estado = null,Object? roles = null,}) {
  return _then(_ClinicaDelUsuario(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as int,nombre: null == nombre ? _self.nombre : nombre // ignore: cast_nullable_to_non_nullable
as String,estado: null == estado ? _self.estado : estado // ignore: cast_nullable_to_non_nullable
as String,roles: null == roles ? _self._roles : roles // ignore: cast_nullable_to_non_nullable
as List<String>,
  ));
}


}

// dart format on
