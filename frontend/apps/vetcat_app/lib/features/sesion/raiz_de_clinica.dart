import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../sesion/pantallas_de_sesion.dart';
import 'controlador_de_clinica.dart';
import 'menu_de_sesion.dart';
import 'pantallas_de_clinica.dart';
import 'vista.dart';

/// Pantalla según el estado de la clínica, ya con la identidad abierta.
class RaizDeClinica extends ConsumerWidget {
  const RaizDeClinica({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return switch (ref.watch(clinicaProvider)) {
      CargandoClinicas() => const PantallaDeCarga(),
      SinClinicas() => const PantallaSinClinicas(),
      ErrorDeCarga(:final mensaje) => PantallaErrorDeCarga(mensaje: mensaje),
      final ElegirClinica e => PantallaDeClinicas(estado: e),
      // La clave destruye el estado de los widgets de la clínica anterior.
      final EnClinica e => AreaDeClinica(
        key: ValueKey(e.sesion.clinicaId),
        estado: e,
      ),
    };
  }
}

/// Marco de la clínica activa. Aquí colgarán los módulos (#184).
class AreaDeClinica extends ConsumerWidget {
  const AreaDeClinica({required this.estado, super.key});

  final EnClinica estado;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final vista = ref.watch(vistaProvider);
    return Scaffold(
      appBar: AppBar(
        title: Text(estado.sesion.nombre),
        actions: [MenuDeSesion(estado: estado)],
      ),
      body: Center(
        child: Text(
          vista == Vista.profesional ? 'Vista profesional' : 'Vista de cliente',
        ),
      ),
    );
  }
}
