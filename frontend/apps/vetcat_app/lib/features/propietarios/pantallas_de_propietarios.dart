import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vetcat_core/vetcat_core.dart';

import 'estado_de_propietarios.dart';

/// Texto fijo por tipo de error: nunca el mensaje crudo del backend.
String textoDeError(Object error) => ErrorDeApi.mensajePorTipo(
  error is ErrorDeApi ? error.tipo : TipoDeError.desconocido,
);

/// Búsqueda y ficha de propietarios (PRO-002, PRO-009). Va dentro del área de la clínica, que ya trae su barra.
class ModuloDePropietarios extends ConsumerWidget {
  const ModuloDePropietarios({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final paso = ref.watch(pasoProvider);
    return PopScope(
      canPop: paso is PasoLista,
      onPopInvokedWithResult: (volvio, _) {
        if (!volvio) ref.read(pasoProvider.notifier).atras();
      },
      child: switch (paso) {
        PasoLista() => const _Lista(),
        PasoFicha(:final id) => _Ficha(id: id),
      },
    );
  }
}

class _Lista extends ConsumerWidget {
  const _Lista();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final resultados = ref.watch(resultadosProvider);
    return Scaffold(
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: TextField(
              decoration: const InputDecoration(
                labelText: 'Buscar propietario o mascota',
                prefixIcon: Icon(Icons.search),
              ),
              textInputAction: TextInputAction.search,
              onSubmitted: ref.read(consultaProvider.notifier).buscar,
            ),
          ),
          Expanded(
            child: resultados.when(
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (e, _) => _Mensaje(
                textoDeError(e),
                alReintentar: () => ref.invalidate(resultadosProvider),
              ),
              data: (pagina) {
                if (pagina == null) {
                  return const _Mensaje(
                    'Escribe al menos $minimoDeLetras letras y busca.',
                  );
                }
                if (pagina.contenido.isEmpty) {
                  return const _Mensaje('Sin resultados.');
                }
                return ListView(
                  children: [
                    for (final r in pagina.contenido)
                      ListTile(
                        title: Text(r.propietario.nombre),
                        subtitle: Text(
                          [
                            r.propietario.telefono,
                            if (r.mascota != null)
                              '${r.mascota!.nombre} (${r.mascota!.especie.etiqueta})',
                          ].join(' · '),
                        ),
                        onTap: () => ref
                            .read(pasoProvider.notifier)
                            .ir(PasoFicha(r.propietario.id)),
                      ),
                  ],
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

class _Ficha extends ConsumerWidget {
  const _Ficha({required this.id});

  final int id;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ref
        .watch(fichaProvider(id))
        .when(
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => _Mensaje(
            textoDeError(e),
            alReintentar: () => ref.invalidate(fichaProvider(id)),
          ),
          data: (p) => Scaffold(
            body: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                Text(p.nombre, style: Theme.of(context).textTheme.titleLarge),
                if (!p.activo) const Text('Dado de baja'),
                Text('Teléfono: ${p.telefono}'),
                if (p.correo != null) Text('Correo: ${p.correo}'),
                if (p.direccion != null) Text('Dirección: ${p.direccion}'),
                const SizedBox(height: 16),
                Text(
                  'Mascotas',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                if (p.mascotas.isEmpty) const Text('Sin mascotas activas.'),
                for (final m in p.mascotas)
                  ListTile(
                    title: Text(m.nombre),
                    subtitle: Text([m.especie.etiqueta, ?m.raza].join(' · ')),
                  ),
              ],
            ),
          ),
        );
  }
}

class _Mensaje extends StatelessWidget {
  const _Mensaje(this.texto, {this.alReintentar});

  final String texto;
  final VoidCallback? alReintentar;

  @override
  Widget build(BuildContext context) => Center(
    child: Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(texto, textAlign: TextAlign.center),
          if (alReintentar != null) ...[
            const SizedBox(height: 12),
            OutlinedButton(
              onPressed: alReintentar,
              child: const Text('Reintentar'),
            ),
          ],
        ],
      ),
    ),
  );
}
