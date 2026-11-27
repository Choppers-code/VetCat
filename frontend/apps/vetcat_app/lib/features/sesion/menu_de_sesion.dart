import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../sesion/controlador_de_sesion.dart';
import 'controlador_de_clinica.dart';
import 'vista.dart';

enum _Accion { cambiarDeClinica, cambiarDeVista, cerrarSesion }

/// Menú mínimo de la clínica activa. Lo reemplaza la navegación real (#184).
class MenuDeSesion extends ConsumerWidget {
  const MenuDeSesion({required this.estado, super.key});

  final EnClinica estado;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final clinica = estado.clinicas.firstWhere(
      (c) => c.id == estado.sesion.clinicaId,
    );
    return PopupMenuButton<_Accion>(
      onSelected: (accion) => switch (accion) {
        _Accion.cambiarDeClinica =>
          ref.read(clinicaProvider.notifier).cambiarDeClinica(),
        _Accion.cambiarDeVista => ref.read(vistaProvider.notifier).alternar(),
        _Accion.cerrarSesion => ref.read(sesionProvider.notifier).cerrar(),
      },
      itemBuilder: (_) => [
        if (estado.puedeCambiarDeClinica)
          const PopupMenuItem(
            value: _Accion.cambiarDeClinica,
            child: Text('Cambiar de clínica'),
          ),
        if (clinica.puedeCambiarDeVista)
          const PopupMenuItem(
            value: _Accion.cambiarDeVista,
            child: Text('Cambiar de vista'),
          ),
        const PopupMenuItem(
          value: _Accion.cerrarSesion,
          child: Text('Cerrar sesión'),
        ),
      ],
    );
  }
}
