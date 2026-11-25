import 'package:freezed_annotation/freezed_annotation.dart';

part 'clinica_del_usuario.freezed.dart';
part 'clinica_del_usuario.g.dart';

/// Una clínica del usuario en el selector (`GET /api/v1/sesion/clinicas`).
@freezed
abstract class ClinicaDelUsuario with _$ClinicaDelUsuario {
  const factory ClinicaDelUsuario({
    required int id,
    required String nombre,

    /// `ACTIVA`, `SUSPENDIDA` o `BAJA`.
    required String estado,

    /// Roles del usuario en esa clínica, no en la clínica activa.
    required List<String> roles,
  }) = _ClinicaDelUsuario;

  factory ClinicaDelUsuario.fromJson(Map<String, Object?> json) =>
      _$ClinicaDelUsuarioFromJson(json);

  const ClinicaDelUsuario._();

  bool get activa => estado == 'ACTIVA';
  bool get suspendida => estado == 'SUSPENDIDA';

  /// `BAJA` no se muestra: la clínica ya no existe para el usuario.
  bool get visible => estado != 'BAJA';

  /// `true` si el usuario es cliente y además personal de la clínica (IDA-010).
  bool get puedeCambiarDeVista =>
      roles.contains(rolCliente) && roles.any(rolesDePersonal.contains);

  static const rolCliente = 'cliente';
  static const rolesDePersonal = {
    'clinica_admin',
    'veterinario',
    'recepcionista',
  };
}
