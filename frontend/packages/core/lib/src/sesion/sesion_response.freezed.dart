// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
// ignore_for_file: type=lint, type=warning, deprecated_member_use, deprecated_member_use_from_same_package
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'sesion_response.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$SesionResponse {

 String get token; int get clinicaId; String get nombre; List<String> get roles;
/// Create a copy of SesionResponse
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$SesionResponseCopyWith<SesionResponse> get copyWith => _$SesionResponseCopyWithImpl<SesionResponse>(this as SesionResponse, _$identity);

  /// Serializes this SesionResponse to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  final _this = this as SesionResponse;
  return identical(this, other) || (other.runtimeType == runtimeType&&other is SesionResponse&&(identical(other.token, _this.token) || other.token == _this.token)&&(identical(other.clinicaId, _this.clinicaId) || other.clinicaId == _this.clinicaId)&&(identical(other.nombre, _this.nombre) || other.nombre == _this.nombre)&&const DeepCollectionEquality().equals(other.roles, _this.roles));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode {
  final _this = this as SesionResponse;
  return Object.hash(runtimeType,_this.token,_this.clinicaId,_this.nombre,const DeepCollectionEquality().hash(_this.roles));
}



}

/// @nodoc
abstract mixin class $SesionResponseCopyWith<$Res>  {
  factory $SesionResponseCopyWith(SesionResponse value, $Res Function(SesionResponse) _then) = _$SesionResponseCopyWithImpl;
@useResult
$Res call({
 String token, int clinicaId, String nombre, List<String> roles
});




}
/// @nodoc
class _$SesionResponseCopyWithImpl<$Res>
    implements $SesionResponseCopyWith<$Res> {
  _$SesionResponseCopyWithImpl(this._self, this._then);

  final SesionResponse _self;
  final $Res Function(SesionResponse) _then;

/// Create a copy of SesionResponse
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? token = null,Object? clinicaId = null,Object? nombre = null,Object? roles = null,}) {
  return _then(SesionResponse(
token: null == token ? _self.token : token // ignore: cast_nullable_to_non_nullable
as String,clinicaId: null == clinicaId ? _self.clinicaId : clinicaId // ignore: cast_nullable_to_non_nullable
as int,nombre: null == nombre ? _self.nombre : nombre // ignore: cast_nullable_to_non_nullable
as String,roles: null == roles ? _self.roles : roles // ignore: cast_nullable_to_non_nullable
as List<String>,
  ));
}

}


/// Adds pattern-matching-related methods to [SesionResponse].
extension SesionResponsePatterns on SesionResponse {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _SesionResponse value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _SesionResponse() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _SesionResponse value)  $default,){
final _that = this;
switch (_that) {
case _SesionResponse():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _SesionResponse value)?  $default,){
final _that = this;
switch (_that) {
case _SesionResponse() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String token,  int clinicaId,  String nombre,  List<String> roles)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _SesionResponse() when $default != null:
return $default(_that.token,_that.clinicaId,_that.nombre,_that.roles);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String token,  int clinicaId,  String nombre,  List<String> roles)  $default,) {final _that = this;
switch (_that) {
case _SesionResponse():
return $default(_that.token,_that.clinicaId,_that.nombre,_that.roles);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String token,  int clinicaId,  String nombre,  List<String> roles)?  $default,) {final _that = this;
switch (_that) {
case _SesionResponse() when $default != null:
return $default(_that.token,_that.clinicaId,_that.nombre,_that.roles);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _SesionResponse extends SesionResponse {
  const _SesionResponse({required this.token, required this.clinicaId, required this.nombre, required  List<String> roles}): _roles = roles,super._();
  factory _SesionResponse.fromJson(Map<String, dynamic> json) => _$SesionResponseFromJson(json);

@override final  String token;
@override final  int clinicaId;
@override final  String nombre;
 final  List<String> _roles;
@override List<String> get roles {
  if (_roles is EqualUnmodifiableListView) return _roles;
  // ignore: implicit_dynamic_type
  return EqualUnmodifiableListView(_roles);
}


/// Create a copy of SesionResponse
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$SesionResponseCopyWith<_SesionResponse> get copyWith => __$SesionResponseCopyWithImpl<_SesionResponse>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$SesionResponseToJson(this, );
}

@override
bool operator ==(Object other) {
    return identical(this, other) || (other.runtimeType == runtimeType&&other is _SesionResponse&&(identical(other.token, token) || other.token == token)&&(identical(other.clinicaId, clinicaId) || other.clinicaId == clinicaId)&&(identical(other.nombre, nombre) || other.nombre == nombre)&&const DeepCollectionEquality().equals(other.roles, _roles));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode {
    return Object.hash(runtimeType,token,clinicaId,nombre,const DeepCollectionEquality().hash(_roles));
}



}

/// @nodoc
abstract mixin class _$SesionResponseCopyWith<$Res> implements $SesionResponseCopyWith<$Res> {
  factory _$SesionResponseCopyWith(_SesionResponse value, $Res Function(_SesionResponse) _then) = __$SesionResponseCopyWithImpl;
@override @useResult
$Res call({
 String token, int clinicaId, String nombre, List<String> roles
});




}
/// @nodoc
class __$SesionResponseCopyWithImpl<$Res>
    implements _$SesionResponseCopyWith<$Res> {
  __$SesionResponseCopyWithImpl(this._self, this._then);

  final _SesionResponse _self;
  final $Res Function(_SesionResponse) _then;

/// Create a copy of SesionResponse
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? token = null,Object? clinicaId = null,Object? nombre = null,Object? roles = null,}) {
  return _then(_SesionResponse(
token: null == token ? _self.token : token // ignore: cast_nullable_to_non_nullable
as String,clinicaId: null == clinicaId ? _self.clinicaId : clinicaId // ignore: cast_nullable_to_non_nullable
as int,nombre: null == nombre ? _self.nombre : nombre // ignore: cast_nullable_to_non_nullable
as String,roles: null == roles ? _self._roles : roles // ignore: cast_nullable_to_non_nullable
as List<String>,
  ));
}


}

// dart format on
