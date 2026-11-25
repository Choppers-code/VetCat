import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vetcat_core/vetcat_core.dart';

import 'controlador_de_clinica.dart';

/// Con qué rol se usa la clínica cuando se tienen los dos (IDA-010). Solo
/// cambia la interfaz: el token es el mismo y el backend decide.
enum Vista { profesional, cliente }

final vistaProvider = NotifierProvider.autoDispose<VistaActiva, Vista>(
  VistaActiva.new,
);

class VistaActiva extends Notifier<Vista> {
  @override
  Vista build() {
    // Al cambiar de clínica se vuelve a calcular la vista inicial.
    ref.watch(clinicaIdProvider);
    final estado = ref.read(clinicaProvider);
    final roles = estado is EnClinica ? estado.sesion.roles : const <String>[];
    return roles.any(ClinicaDelUsuario.rolesDePersonal.contains)
        ? Vista.profesional
        : Vista.cliente;
  }

  void alternar() =>
      state = state == Vista.profesional ? Vista.cliente : Vista.profesional;
}

/// Nombre de cada rol para mostrarlo a la persona.
String etiquetaDeRol(String rol) => switch (rol) {
  'clinica_admin' => 'Administrador',
  'veterinario' => 'Veterinario',
  'recepcionista' => 'Recepción',
  'cliente' => 'Cliente',
  _ => rol,
};
