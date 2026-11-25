import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vetcat_core/vetcat_core.dart';

import 'controlador_de_sesion.dart';

/// Mismo texto para cualquier fallo: no distingue si el correo existe.
const mensajeDeAccesoFallido =
    'No pudimos iniciar sesión. Revisa tus datos e inténtalo de nuevo.';

/// Elige la pantalla según la sesión. No apila rutas: al cerrar sesión no hay
/// pantalla anterior a la que volver con el botón «atrás».
class RaizDeSesion extends ConsumerWidget {
  const RaizDeSesion({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return switch (ref.watch(sesionProvider)) {
      Cargando() => const PantallaDeCarga(),
      SinSesion(:final fallo) => PantallaDeAcceso(fallo: fallo),
      ConSesion() => const PantallaProvisional(),
    };
  }
}

class PantallaDeCarga extends StatelessWidget {
  const PantallaDeCarga({super.key});

  @override
  Widget build(BuildContext context) =>
      const Scaffold(body: Center(child: CircularProgressIndicator()));
}

class PantallaDeAcceso extends ConsumerWidget {
  const PantallaDeAcceso({required this.fallo, super.key});

  final bool fallo;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tema = Theme.of(context);
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                nombreProducto,
                style: tema.textTheme.headlineMedium,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 32),
              if (fallo) ...[
                Text(
                  mensajeDeAccesoFallido,
                  style: TextStyle(color: tema.colorScheme.error),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 16),
              ],
              FilledButton(
                onPressed: () => ref.read(sesionProvider.notifier).iniciar(),
                child: const Text('Iniciar sesión'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// Sustituye al selector de clínica (#153) hasta que exista.
class PantallaProvisional extends ConsumerWidget {
  const PantallaProvisional({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Sesión iniciada'),
            const SizedBox(height: 16),
            OutlinedButton(
              onPressed: () => ref.read(sesionProvider.notifier).cerrar(),
              child: const Text('Cerrar sesión'),
            ),
          ],
        ),
      ),
    );
  }
}
