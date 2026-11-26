// GENERATED CODE - DO NOT MODIFY BY HAND
// coverage:ignore-file
// ignore_for_file: type=lint, type=warning, deprecated_member_use, deprecated_member_use_from_same_package
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'propietario.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// dart format off
T _$identity<T>(T value) => value;

/// @nodoc
mixin _$PropietarioResponse {

 int get id; String get nombre; String get telefono; bool get activo; List<MascotaResponse> get mascotas; String? get correo; String? get direccion;
/// Create a copy of PropietarioResponse
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$PropietarioResponseCopyWith<PropietarioResponse> get copyWith => _$PropietarioResponseCopyWithImpl<PropietarioResponse>(this as PropietarioResponse, _$identity);

  /// Serializes this PropietarioResponse to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  final _this = this as PropietarioResponse;
  return identical(this, other) || (other.runtimeType == runtimeType&&other is PropietarioResponse&&(identical(other.id, _this.id) || other.id == _this.id)&&(identical(other.nombre, _this.nombre) || other.nombre == _this.nombre)&&(identical(other.telefono, _this.telefono) || other.telefono == _this.telefono)&&(identical(other.activo, _this.activo) || other.activo == _this.activo)&&const DeepCollectionEquality().equals(other.mascotas, _this.mascotas)&&(identical(other.correo, _this.correo) || other.correo == _this.correo)&&(identical(other.direccion, _this.direccion) || other.direccion == _this.direccion));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode {
  final _this = this as PropietarioResponse;
  return Object.hash(runtimeType,_this.id,_this.nombre,_this.telefono,_this.activo,const DeepCollectionEquality().hash(_this.mascotas),_this.correo,_this.direccion);
}

@override
String toString() {
  final _this = this as PropietarioResponse;
  return 'PropietarioResponse(id: ${_this.id}, nombre: ${_this.nombre}, telefono: ${_this.telefono}, activo: ${_this.activo}, mascotas: ${_this.mascotas}, correo: ${_this.correo}, direccion: ${_this.direccion})';
}


}

/// @nodoc
abstract mixin class $PropietarioResponseCopyWith<$Res>  {
  factory $PropietarioResponseCopyWith(PropietarioResponse value, $Res Function(PropietarioResponse) _then) = _$PropietarioResponseCopyWithImpl;
@useResult
$Res call({
 int id, String nombre, String telefono, bool activo, List<MascotaResponse> mascotas, String? correo, String? direccion
});




}
/// @nodoc
class _$PropietarioResponseCopyWithImpl<$Res>
    implements $PropietarioResponseCopyWith<$Res> {
  _$PropietarioResponseCopyWithImpl(this._self, this._then);

  final PropietarioResponse _self;
  final $Res Function(PropietarioResponse) _then;

/// Create a copy of PropietarioResponse
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? id = null,Object? nombre = null,Object? telefono = null,Object? activo = null,Object? mascotas = null,Object? correo = freezed,Object? direccion = freezed,}) {
  return _then(PropietarioResponse(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as int,nombre: null == nombre ? _self.nombre : nombre // ignore: cast_nullable_to_non_nullable
as String,telefono: null == telefono ? _self.telefono : telefono // ignore: cast_nullable_to_non_nullable
as String,activo: null == activo ? _self.activo : activo // ignore: cast_nullable_to_non_nullable
as bool,mascotas: null == mascotas ? _self.mascotas : mascotas // ignore: cast_nullable_to_non_nullable
as List<MascotaResponse>,correo: freezed == correo ? _self.correo : correo // ignore: cast_nullable_to_non_nullable
as String?,direccion: freezed == direccion ? _self.direccion : direccion // ignore: cast_nullable_to_non_nullable
as String?,
  ));
}

}


/// Adds pattern-matching-related methods to [PropietarioResponse].
extension PropietarioResponsePatterns on PropietarioResponse {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _PropietarioResponse value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _PropietarioResponse() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _PropietarioResponse value)  $default,){
final _that = this;
switch (_that) {
case _PropietarioResponse():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _PropietarioResponse value)?  $default,){
final _that = this;
switch (_that) {
case _PropietarioResponse() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( int id,  String nombre,  String telefono,  bool activo,  List<MascotaResponse> mascotas,  String? correo,  String? direccion)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _PropietarioResponse() when $default != null:
return $default(_that.id,_that.nombre,_that.telefono,_that.activo,_that.mascotas,_that.correo,_that.direccion);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( int id,  String nombre,  String telefono,  bool activo,  List<MascotaResponse> mascotas,  String? correo,  String? direccion)  $default,) {final _that = this;
switch (_that) {
case _PropietarioResponse():
return $default(_that.id,_that.nombre,_that.telefono,_that.activo,_that.mascotas,_that.correo,_that.direccion);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( int id,  String nombre,  String telefono,  bool activo,  List<MascotaResponse> mascotas,  String? correo,  String? direccion)?  $default,) {final _that = this;
switch (_that) {
case _PropietarioResponse() when $default != null:
return $default(_that.id,_that.nombre,_that.telefono,_that.activo,_that.mascotas,_that.correo,_that.direccion);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _PropietarioResponse implements PropietarioResponse {
  const _PropietarioResponse({required this.id, required this.nombre, required this.telefono, required this.activo,  List<MascotaResponse> mascotas = const [], this.correo, this.direccion}): _mascotas = mascotas;
  factory _PropietarioResponse.fromJson(Map<String, dynamic> json) => _$PropietarioResponseFromJson(json);

@override final  int id;
@override final  String nombre;
@override final  String telefono;
@override final  bool activo;
 final  List<MascotaResponse> _mascotas;
@override@JsonKey() List<MascotaResponse> get mascotas {
  if (_mascotas is EqualUnmodifiableListView) return _mascotas;
  // ignore: implicit_dynamic_type
  return EqualUnmodifiableListView(_mascotas);
}

@override final  String? correo;
@override final  String? direccion;

/// Create a copy of PropietarioResponse
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$PropietarioResponseCopyWith<_PropietarioResponse> get copyWith => __$PropietarioResponseCopyWithImpl<_PropietarioResponse>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$PropietarioResponseToJson(this, );
}

@override
bool operator ==(Object other) {
    return identical(this, other) || (other.runtimeType == runtimeType&&other is _PropietarioResponse&&(identical(other.id, id) || other.id == id)&&(identical(other.nombre, nombre) || other.nombre == nombre)&&(identical(other.telefono, telefono) || other.telefono == telefono)&&(identical(other.activo, activo) || other.activo == activo)&&const DeepCollectionEquality().equals(other.mascotas, _mascotas)&&(identical(other.correo, correo) || other.correo == correo)&&(identical(other.direccion, direccion) || other.direccion == direccion));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode {
    return Object.hash(runtimeType,id,nombre,telefono,activo,const DeepCollectionEquality().hash(_mascotas),correo,direccion);
}

@override
String toString() {
    return 'PropietarioResponse(id: $id, nombre: $nombre, telefono: $telefono, activo: $activo, mascotas: $mascotas, correo: $correo, direccion: $direccion)';
}


}

/// @nodoc
abstract mixin class _$PropietarioResponseCopyWith<$Res> implements $PropietarioResponseCopyWith<$Res> {
  factory _$PropietarioResponseCopyWith(_PropietarioResponse value, $Res Function(_PropietarioResponse) _then) = __$PropietarioResponseCopyWithImpl;
@override @useResult
$Res call({
 int id, String nombre, String telefono, bool activo, List<MascotaResponse> mascotas, String? correo, String? direccion
});




}
/// @nodoc
class __$PropietarioResponseCopyWithImpl<$Res>
    implements _$PropietarioResponseCopyWith<$Res> {
  __$PropietarioResponseCopyWithImpl(this._self, this._then);

  final _PropietarioResponse _self;
  final $Res Function(_PropietarioResponse) _then;

/// Create a copy of PropietarioResponse
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? id = null,Object? nombre = null,Object? telefono = null,Object? activo = null,Object? mascotas = null,Object? correo = freezed,Object? direccion = freezed,}) {
  return _then(_PropietarioResponse(
id: null == id ? _self.id : id // ignore: cast_nullable_to_non_nullable
as int,nombre: null == nombre ? _self.nombre : nombre // ignore: cast_nullable_to_non_nullable
as String,telefono: null == telefono ? _self.telefono : telefono // ignore: cast_nullable_to_non_nullable
as String,activo: null == activo ? _self.activo : activo // ignore: cast_nullable_to_non_nullable
as bool,mascotas: null == mascotas ? _self._mascotas : mascotas // ignore: cast_nullable_to_non_nullable
as List<MascotaResponse>,correo: freezed == correo ? _self.correo : correo // ignore: cast_nullable_to_non_nullable
as String?,direccion: freezed == direccion ? _self.direccion : direccion // ignore: cast_nullable_to_non_nullable
as String?,
  ));
}


}


/// @nodoc
mixin _$PropietarioRequest {

 String get nombre; String get telefono; String? get correo;/// Solo si el plan incluye `visitas_domicilio`.
 String? get direccion;/// Crea el propietario aunque el teléfono ya exista.
 bool get confirmarDuplicado;
/// Create a copy of PropietarioRequest
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$PropietarioRequestCopyWith<PropietarioRequest> get copyWith => _$PropietarioRequestCopyWithImpl<PropietarioRequest>(this as PropietarioRequest, _$identity);

  /// Serializes this PropietarioRequest to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  final _this = this as PropietarioRequest;
  return identical(this, other) || (other.runtimeType == runtimeType&&other is PropietarioRequest&&(identical(other.nombre, _this.nombre) || other.nombre == _this.nombre)&&(identical(other.telefono, _this.telefono) || other.telefono == _this.telefono)&&(identical(other.correo, _this.correo) || other.correo == _this.correo)&&(identical(other.direccion, _this.direccion) || other.direccion == _this.direccion)&&(identical(other.confirmarDuplicado, _this.confirmarDuplicado) || other.confirmarDuplicado == _this.confirmarDuplicado));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode {
  final _this = this as PropietarioRequest;
  return Object.hash(runtimeType,_this.nombre,_this.telefono,_this.correo,_this.direccion,_this.confirmarDuplicado);
}

@override
String toString() {
  final _this = this as PropietarioRequest;
  return 'PropietarioRequest(nombre: ${_this.nombre}, telefono: ${_this.telefono}, correo: ${_this.correo}, direccion: ${_this.direccion}, confirmarDuplicado: ${_this.confirmarDuplicado})';
}


}

/// @nodoc
abstract mixin class $PropietarioRequestCopyWith<$Res>  {
  factory $PropietarioRequestCopyWith(PropietarioRequest value, $Res Function(PropietarioRequest) _then) = _$PropietarioRequestCopyWithImpl;
@useResult
$Res call({
 String nombre, String telefono, String? correo, String? direccion, bool confirmarDuplicado
});




}
/// @nodoc
class _$PropietarioRequestCopyWithImpl<$Res>
    implements $PropietarioRequestCopyWith<$Res> {
  _$PropietarioRequestCopyWithImpl(this._self, this._then);

  final PropietarioRequest _self;
  final $Res Function(PropietarioRequest) _then;

/// Create a copy of PropietarioRequest
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? nombre = null,Object? telefono = null,Object? correo = freezed,Object? direccion = freezed,Object? confirmarDuplicado = null,}) {
  return _then(PropietarioRequest(
nombre: null == nombre ? _self.nombre : nombre // ignore: cast_nullable_to_non_nullable
as String,telefono: null == telefono ? _self.telefono : telefono // ignore: cast_nullable_to_non_nullable
as String,correo: freezed == correo ? _self.correo : correo // ignore: cast_nullable_to_non_nullable
as String?,direccion: freezed == direccion ? _self.direccion : direccion // ignore: cast_nullable_to_non_nullable
as String?,confirmarDuplicado: null == confirmarDuplicado ? _self.confirmarDuplicado : confirmarDuplicado // ignore: cast_nullable_to_non_nullable
as bool,
  ));
}

}


/// Adds pattern-matching-related methods to [PropietarioRequest].
extension PropietarioRequestPatterns on PropietarioRequest {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _PropietarioRequest value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _PropietarioRequest() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _PropietarioRequest value)  $default,){
final _that = this;
switch (_that) {
case _PropietarioRequest():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _PropietarioRequest value)?  $default,){
final _that = this;
switch (_that) {
case _PropietarioRequest() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( String nombre,  String telefono,  String? correo,  String? direccion,  bool confirmarDuplicado)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _PropietarioRequest() when $default != null:
return $default(_that.nombre,_that.telefono,_that.correo,_that.direccion,_that.confirmarDuplicado);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( String nombre,  String telefono,  String? correo,  String? direccion,  bool confirmarDuplicado)  $default,) {final _that = this;
switch (_that) {
case _PropietarioRequest():
return $default(_that.nombre,_that.telefono,_that.correo,_that.direccion,_that.confirmarDuplicado);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( String nombre,  String telefono,  String? correo,  String? direccion,  bool confirmarDuplicado)?  $default,) {final _that = this;
switch (_that) {
case _PropietarioRequest() when $default != null:
return $default(_that.nombre,_that.telefono,_that.correo,_that.direccion,_that.confirmarDuplicado);case _:
  return null;

}
}

}

/// @nodoc

@JsonSerializable(includeIfNull: false)
class _PropietarioRequest implements PropietarioRequest {
  const _PropietarioRequest({required this.nombre, required this.telefono, this.correo, this.direccion, this.confirmarDuplicado = false});
  factory _PropietarioRequest.fromJson(Map<String, dynamic> json) => _$PropietarioRequestFromJson(json);

@override final  String nombre;
@override final  String telefono;
@override final  String? correo;
/// Solo si el plan incluye `visitas_domicilio`.
@override final  String? direccion;
/// Crea el propietario aunque el teléfono ya exista.
@override@JsonKey() final  bool confirmarDuplicado;

/// Create a copy of PropietarioRequest
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$PropietarioRequestCopyWith<_PropietarioRequest> get copyWith => __$PropietarioRequestCopyWithImpl<_PropietarioRequest>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$PropietarioRequestToJson(this, );
}

@override
bool operator ==(Object other) {
    return identical(this, other) || (other.runtimeType == runtimeType&&other is _PropietarioRequest&&(identical(other.nombre, nombre) || other.nombre == nombre)&&(identical(other.telefono, telefono) || other.telefono == telefono)&&(identical(other.correo, correo) || other.correo == correo)&&(identical(other.direccion, direccion) || other.direccion == direccion)&&(identical(other.confirmarDuplicado, confirmarDuplicado) || other.confirmarDuplicado == confirmarDuplicado));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode {
    return Object.hash(runtimeType,nombre,telefono,correo,direccion,confirmarDuplicado);
}

@override
String toString() {
    return 'PropietarioRequest(nombre: $nombre, telefono: $telefono, correo: $correo, direccion: $direccion, confirmarDuplicado: $confirmarDuplicado)';
}


}

/// @nodoc
abstract mixin class _$PropietarioRequestCopyWith<$Res> implements $PropietarioRequestCopyWith<$Res> {
  factory _$PropietarioRequestCopyWith(_PropietarioRequest value, $Res Function(_PropietarioRequest) _then) = __$PropietarioRequestCopyWithImpl;
@override @useResult
$Res call({
 String nombre, String telefono, String? correo, String? direccion, bool confirmarDuplicado
});




}
/// @nodoc
class __$PropietarioRequestCopyWithImpl<$Res>
    implements _$PropietarioRequestCopyWith<$Res> {
  __$PropietarioRequestCopyWithImpl(this._self, this._then);

  final _PropietarioRequest _self;
  final $Res Function(_PropietarioRequest) _then;

/// Create a copy of PropietarioRequest
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? nombre = null,Object? telefono = null,Object? correo = freezed,Object? direccion = freezed,Object? confirmarDuplicado = null,}) {
  return _then(_PropietarioRequest(
nombre: null == nombre ? _self.nombre : nombre // ignore: cast_nullable_to_non_nullable
as String,telefono: null == telefono ? _self.telefono : telefono // ignore: cast_nullable_to_non_nullable
as String,correo: freezed == correo ? _self.correo : correo // ignore: cast_nullable_to_non_nullable
as String?,direccion: freezed == direccion ? _self.direccion : direccion // ignore: cast_nullable_to_non_nullable
as String?,confirmarDuplicado: null == confirmarDuplicado ? _self.confirmarDuplicado : confirmarDuplicado // ignore: cast_nullable_to_non_nullable
as bool,
  ));
}


}


/// @nodoc
mixin _$ResultadoDeBusqueda {

 PropietarioResponse get propietario; MascotaResponse? get mascota;
/// Create a copy of ResultadoDeBusqueda
/// with the given fields replaced by the non-null parameter values.
@JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
$ResultadoDeBusquedaCopyWith<ResultadoDeBusqueda> get copyWith => _$ResultadoDeBusquedaCopyWithImpl<ResultadoDeBusqueda>(this as ResultadoDeBusqueda, _$identity);

  /// Serializes this ResultadoDeBusqueda to a JSON map.
  Map<String, dynamic> toJson();


@override
bool operator ==(Object other) {
  final _this = this as ResultadoDeBusqueda;
  return identical(this, other) || (other.runtimeType == runtimeType&&other is ResultadoDeBusqueda&&(identical(other.propietario, _this.propietario) || other.propietario == _this.propietario)&&(identical(other.mascota, _this.mascota) || other.mascota == _this.mascota));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode {
  final _this = this as ResultadoDeBusqueda;
  return Object.hash(runtimeType,_this.propietario,_this.mascota);
}

@override
String toString() {
  final _this = this as ResultadoDeBusqueda;
  return 'ResultadoDeBusqueda(propietario: ${_this.propietario}, mascota: ${_this.mascota})';
}


}

/// @nodoc
abstract mixin class $ResultadoDeBusquedaCopyWith<$Res>  {
  factory $ResultadoDeBusquedaCopyWith(ResultadoDeBusqueda value, $Res Function(ResultadoDeBusqueda) _then) = _$ResultadoDeBusquedaCopyWithImpl;
@useResult
$Res call({
 PropietarioResponse propietario, MascotaResponse? mascota
});


$PropietarioResponseCopyWith<$Res> get propietario;$MascotaResponseCopyWith<$Res>? get mascota;

}
/// @nodoc
class _$ResultadoDeBusquedaCopyWithImpl<$Res>
    implements $ResultadoDeBusquedaCopyWith<$Res> {
  _$ResultadoDeBusquedaCopyWithImpl(this._self, this._then);

  final ResultadoDeBusqueda _self;
  final $Res Function(ResultadoDeBusqueda) _then;

/// Create a copy of ResultadoDeBusqueda
/// with the given fields replaced by the non-null parameter values.
@pragma('vm:prefer-inline') @override $Res call({Object? propietario = null,Object? mascota = freezed,}) {
  return _then(ResultadoDeBusqueda(
propietario: null == propietario ? _self.propietario : propietario // ignore: cast_nullable_to_non_nullable
as PropietarioResponse,mascota: freezed == mascota ? _self.mascota : mascota // ignore: cast_nullable_to_non_nullable
as MascotaResponse?,
  ));
}
/// Create a copy of ResultadoDeBusqueda
/// with the given fields replaced by the non-null parameter values.
@override
@pragma('vm:prefer-inline')
$PropietarioResponseCopyWith<$Res> get propietario {
  
  return $PropietarioResponseCopyWith<$Res>(_self.propietario, (value) {
    return _then(_self.copyWith(propietario: value));
  });
}/// Create a copy of ResultadoDeBusqueda
/// with the given fields replaced by the non-null parameter values.
@override
@pragma('vm:prefer-inline')
$MascotaResponseCopyWith<$Res>? get mascota {
    if (_self.mascota == null) {
    return null;
  }

  return $MascotaResponseCopyWith<$Res>(_self.mascota!, (value) {
    return _then(_self.copyWith(mascota: value));
  });
}
}


/// Adds pattern-matching-related methods to [ResultadoDeBusqueda].
extension ResultadoDeBusquedaPatterns on ResultadoDeBusqueda {
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

@optionalTypeArgs TResult maybeMap<TResult extends Object?>(TResult Function( _ResultadoDeBusqueda value)?  $default,{required TResult orElse(),}){
final _that = this;
switch (_that) {
case _ResultadoDeBusqueda() when $default != null:
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

@optionalTypeArgs TResult map<TResult extends Object?>(TResult Function( _ResultadoDeBusqueda value)  $default,){
final _that = this;
switch (_that) {
case _ResultadoDeBusqueda():
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

@optionalTypeArgs TResult? mapOrNull<TResult extends Object?>(TResult? Function( _ResultadoDeBusqueda value)?  $default,){
final _that = this;
switch (_that) {
case _ResultadoDeBusqueda() when $default != null:
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

@optionalTypeArgs TResult maybeWhen<TResult extends Object?>(TResult Function( PropietarioResponse propietario,  MascotaResponse? mascota)?  $default,{required TResult orElse(),}) {final _that = this;
switch (_that) {
case _ResultadoDeBusqueda() when $default != null:
return $default(_that.propietario,_that.mascota);case _:
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

@optionalTypeArgs TResult when<TResult extends Object?>(TResult Function( PropietarioResponse propietario,  MascotaResponse? mascota)  $default,) {final _that = this;
switch (_that) {
case _ResultadoDeBusqueda():
return $default(_that.propietario,_that.mascota);case _:
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

@optionalTypeArgs TResult? whenOrNull<TResult extends Object?>(TResult? Function( PropietarioResponse propietario,  MascotaResponse? mascota)?  $default,) {final _that = this;
switch (_that) {
case _ResultadoDeBusqueda() when $default != null:
return $default(_that.propietario,_that.mascota);case _:
  return null;

}
}

}

/// @nodoc
@JsonSerializable()

class _ResultadoDeBusqueda implements ResultadoDeBusqueda {
  const _ResultadoDeBusqueda({required this.propietario, this.mascota});
  factory _ResultadoDeBusqueda.fromJson(Map<String, dynamic> json) => _$ResultadoDeBusquedaFromJson(json);

@override final  PropietarioResponse propietario;
@override final  MascotaResponse? mascota;

/// Create a copy of ResultadoDeBusqueda
/// with the given fields replaced by the non-null parameter values.
@override @JsonKey(includeFromJson: false, includeToJson: false)
@pragma('vm:prefer-inline')
_$ResultadoDeBusquedaCopyWith<_ResultadoDeBusqueda> get copyWith => __$ResultadoDeBusquedaCopyWithImpl<_ResultadoDeBusqueda>(this, _$identity);

@override
Map<String, dynamic> toJson() {
  return _$ResultadoDeBusquedaToJson(this, );
}

@override
bool operator ==(Object other) {
    return identical(this, other) || (other.runtimeType == runtimeType&&other is _ResultadoDeBusqueda&&(identical(other.propietario, propietario) || other.propietario == propietario)&&(identical(other.mascota, mascota) || other.mascota == mascota));
}

@JsonKey(includeFromJson: false, includeToJson: false)
@override
int get hashCode {
    return Object.hash(runtimeType,propietario,mascota);
}

@override
String toString() {
    return 'ResultadoDeBusqueda(propietario: $propietario, mascota: $mascota)';
}


}

/// @nodoc
abstract mixin class _$ResultadoDeBusquedaCopyWith<$Res> implements $ResultadoDeBusquedaCopyWith<$Res> {
  factory _$ResultadoDeBusquedaCopyWith(_ResultadoDeBusqueda value, $Res Function(_ResultadoDeBusqueda) _then) = __$ResultadoDeBusquedaCopyWithImpl;
@override @useResult
$Res call({
 PropietarioResponse propietario, MascotaResponse? mascota
});


@override $PropietarioResponseCopyWith<$Res> get propietario;@override $MascotaResponseCopyWith<$Res>? get mascota;

}
/// @nodoc
class __$ResultadoDeBusquedaCopyWithImpl<$Res>
    implements _$ResultadoDeBusquedaCopyWith<$Res> {
  __$ResultadoDeBusquedaCopyWithImpl(this._self, this._then);

  final _ResultadoDeBusqueda _self;
  final $Res Function(_ResultadoDeBusqueda) _then;

/// Create a copy of ResultadoDeBusqueda
/// with the given fields replaced by the non-null parameter values.
@override @pragma('vm:prefer-inline') $Res call({Object? propietario = null,Object? mascota = freezed,}) {
  return _then(_ResultadoDeBusqueda(
propietario: null == propietario ? _self.propietario : propietario // ignore: cast_nullable_to_non_nullable
as PropietarioResponse,mascota: freezed == mascota ? _self.mascota : mascota // ignore: cast_nullable_to_non_nullable
as MascotaResponse?,
  ));
}

/// Create a copy of ResultadoDeBusqueda
/// with the given fields replaced by the non-null parameter values.
@override
@pragma('vm:prefer-inline')
$PropietarioResponseCopyWith<$Res> get propietario {
  
  return $PropietarioResponseCopyWith<$Res>(_self.propietario, (value) {
    return _then(_self.copyWith(propietario: value));
  });
}/// Create a copy of ResultadoDeBusqueda
/// with the given fields replaced by the non-null parameter values.
@override
@pragma('vm:prefer-inline')
$MascotaResponseCopyWith<$Res>? get mascota {
    if (_self.mascota == null) {
    return null;
  }

  return $MascotaResponseCopyWith<$Res>(_self.mascota!, (value) {
    return _then(_self.copyWith(mascota: value));
  });
}
}

// dart format on
