import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vetcat_core/vetcat_core.dart';

import '../../sesion/controlador_de_sesion.dart';
import 'controlador_de_clinica.dart';
import 'vista.dart';

/// Las tres pantallas previas a entrar en una clínica. Todas dejan salir.
class PantallaDeClinicas extends ConsumerWidget {
  const PantallaDeClinicas({required this.estado, super.key});

  final ElegirClinica estado;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // La sugerida va primero; el resto conserva el orden del backend.
    final clinicas = [...estado.clinicas]
      ..sort((a, b) {
        if (a.id == estado.sugerida) return -1;
        if (b.id == estado.sugerida) return 1;
        return 0;
      });
    return _Marco(
      titulo: 'Elige tu clínica',
      hijo: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          if (estado.aviso != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: Text(
                estado.aviso!,
                style: TextStyle(color: Theme.of(context).colorScheme.error),
              ),
            ),
          for (final c in clinicas)
            TarjetaDeClinica(
              clinica: c,
              sugerida: c.id == estado.sugerida,
              alElegir: () => ref.read(clinicaProvider.notifier).elegir(c),
            ),
        ],
      ),
    );
  }
}

class TarjetaDeClinica extends StatelessWidget {
  const TarjetaDeClinica({
    required this.clinica,
    required this.sugerida,
    required this.alElegir,
    super.key,
  });

  final ClinicaDelUsuario clinica;
  final bool sugerida;
  final VoidCallback alElegir;

  @override
  Widget build(BuildContext context) {
    // El estado va en texto, no solo en color.
    final etiqueta = !clinica.activa
        ? 'Suspendida'
        : sugerida
        ? 'Última usada'
        : null;
    return Card(
      child: ListTile(
        enabled: clinica.activa,
        onTap: clinica.activa ? alElegir : null,
        title: Text(clinica.nombre),
        subtitle: Text(clinica.roles.map(etiquetaDeRol).join(' · ')),
        trailing: etiqueta == null ? null : Text(etiqueta),
      ),
    );
  }
}

class PantallaSinClinicas extends StatelessWidget {
  const PantallaSinClinicas({super.key});

  @override
  Widget build(BuildContext context) => const _Marco(
    titulo: 'Sin clínicas',
    hijo: Center(
      child: Padding(
        padding: EdgeInsets.all(24),
        child: Text(mensajeSinClinicas, textAlign: TextAlign.center),
      ),
    ),
  );
}

class PantallaErrorDeCarga extends ConsumerWidget {
  const PantallaErrorDeCarga({required this.mensaje, super.key});

  final String mensaje;

  @override
  Widget build(BuildContext context, WidgetRef ref) => _Marco(
    titulo: 'Tus clínicas',
    hijo: Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(mensaje, textAlign: TextAlign.center),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: () => ref.read(clinicaProvider.notifier).cargar(),
              child: const Text('Reintentar'),
            ),
          ],
        ),
      ),
    ),
  );
}

class _Marco extends ConsumerWidget {
  const _Marco({required this.titulo, required this.hijo});

  final String titulo;
  final Widget hijo;

  @override
  Widget build(BuildContext context, WidgetRef ref) => Scaffold(
    appBar: AppBar(
      title: Text(titulo),
      actions: [
        TextButton(
          onPressed: () => ref.read(sesionProvider.notifier).cerrar(),
          child: const Text('Cerrar sesión'),
        ),
      ],
    ),
    body: hijo,
  );
}
