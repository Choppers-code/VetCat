import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vetcat_core/vetcat_core.dart';

import 'estado_de_propietarios.dart';
import 'pantallas_de_propietarios.dart';
import 'repositorio_de_propietarios.dart';

const mensajeDeDuplicado =
    'Ya existe un propietario con ese teléfono. ¿Registrarlo de todos modos?';

/// Alta y edición. La dirección solo se ofrece si el plan incluye visitas a
/// domicilio; el backend la rechaza igual si se fuerza.
class FormularioDePropietario extends ConsumerStatefulWidget {
  const FormularioDePropietario({this.propietario, super.key});

  final PropietarioResponse? propietario;

  @override
  ConsumerState<FormularioDePropietario> createState() => _Estado();
}

class _Estado extends ConsumerState<FormularioDePropietario> {
  final _clave = GlobalKey<FormState>();
  late final _nombre = TextEditingController(text: widget.propietario?.nombre);
  late final _telefono = TextEditingController(
    text: widget.propietario?.telefono,
  );
  late final _correo = TextEditingController(text: widget.propietario?.correo);
  late final _direccion = TextEditingController(
    text: widget.propietario?.direccion,
  );
  bool _guardando = false;
  String? _error;

  @override
  void dispose() {
    for (final c in [_nombre, _telefono, _correo, _direccion]) {
      c.dispose();
    }
    super.dispose();
  }

  String? _vacioA(String texto) => texto.trim().isEmpty ? null : texto.trim();

  String? _obligatorio(String? v) =>
      (v ?? '').trim().isEmpty ? 'Obligatorio' : null;

  String? _validarCorreo(String? v) {
    final texto = (v ?? '').trim();
    return texto.isEmpty || texto.contains('@') ? null : 'Correo no válido';
  }

  Future<void> _guardar({bool confirmarDuplicado = false}) async {
    if (!_clave.currentState!.validate()) return;
    setState(() {
      _guardando = true;
      _error = null;
    });
    final datos = PropietarioRequest(
      nombre: _nombre.text.trim(),
      telefono: _telefono.text.trim(),
      correo: _vacioA(_correo.text),
      direccion: ref.read(visitasDomicilioProvider)
          ? _vacioA(_direccion.text)
          : null,
      confirmarDuplicado: confirmarDuplicado,
    );
    final repositorio = ref.read(repositorioDePropietariosProvider);
    final original = widget.propietario;
    try {
      final guardado = original == null
          ? await repositorio.registrar(datos)
          : await repositorio.editar(original.id, datos);
      ref
        ..invalidate(fichaProvider(guardado.id))
        ..invalidate(resultadosProvider);
      ref.read(pasoProvider.notifier).ir(PasoFicha(guardado.id));
    } on ErrorDeApi catch (e) {
      if (!mounted) return;
      if (e.tipo == TipoDeError.conflicto && original == null) {
        setState(() => _guardando = false);
        if (await _confirmarDuplicado() && mounted) {
          await _guardar(confirmarDuplicado: true);
        }
        return;
      }
      setState(() {
        _guardando = false;
        _error = textoDeError(e);
      });
    } on Object catch (e) {
      if (!mounted) return;
      setState(() {
        _guardando = false;
        _error = textoDeError(e);
      });
    }
  }

  Future<bool> _confirmarDuplicado() async {
    final acepta = await showDialog<bool>(
      context: context,
      builder: (contexto) => AlertDialog(
        content: const Text(mensajeDeDuplicado),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(contexto, false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(contexto, true),
            child: const Text('Registrar de todos modos'),
          ),
        ],
      ),
    );
    return acepta ?? false;
  }

  @override
  Widget build(BuildContext context) {
    final conDireccion = ref.watch(visitasDomicilioProvider);
    return Scaffold(
      body: Form(
        key: _clave,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            Text(
              widget.propietario == null
                  ? 'Nuevo propietario'
                  : 'Editar propietario',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            TextFormField(
              controller: _nombre,
              decoration: const InputDecoration(labelText: 'Nombre'),
              validator: _obligatorio,
            ),
            TextFormField(
              controller: _telefono,
              decoration: const InputDecoration(labelText: 'Teléfono'),
              keyboardType: TextInputType.phone,
              validator: _obligatorio,
            ),
            TextFormField(
              controller: _correo,
              decoration: const InputDecoration(labelText: 'Correo'),
              keyboardType: TextInputType.emailAddress,
              validator: _validarCorreo,
            ),
            if (conDireccion)
              TextFormField(
                controller: _direccion,
                decoration: const InputDecoration(labelText: 'Dirección'),
              ),
            if (_error != null)
              Padding(
                padding: const EdgeInsets.only(top: 12),
                child: Text(
                  _error!,
                  style: TextStyle(color: Theme.of(context).colorScheme.error),
                ),
              ),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: _guardando ? null : _guardar,
              child: Text(_guardando ? 'Guardando…' : 'Guardar'),
            ),
          ],
        ),
      ),
    );
  }
}
