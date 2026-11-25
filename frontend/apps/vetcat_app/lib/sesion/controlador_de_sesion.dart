import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'almacen_de_tokens.dart';
import 'autenticador.dart';
import 'tokens_de_identidad.dart';

sealed class EstadoDeSesion {
  const EstadoDeSesion();
}

/// Restaurando o abriendo la sesión.
class Cargando extends EstadoDeSesion {
  const Cargando();
}

/// Sin sesión. [fallo] es `true` solo si el intento terminó mal; el motivo
/// nunca llega a la pantalla, para no revelar si el correo existe (IDA-001).
class SinSesion extends EstadoDeSesion {
  const SinSesion({this.fallo = false});
  final bool fallo;
}

class ConSesion extends EstadoDeSesion {
  const ConSesion(this.tokens);
  final TokensDeIdentidad tokens;
}

final autenticadorProvider = Provider<Autenticador>(
  (ref) => AutenticadorAppAuth(),
);
final almacenProvider = Provider<AlmacenDeTokens>((ref) => AlmacenSeguro());

final sesionProvider = NotifierProvider<ControladorDeSesion, EstadoDeSesion>(
  ControladorDeSesion.new,
);

class ControladorDeSesion extends Notifier<EstadoDeSesion> {
  @override
  EstadoDeSesion build() {
    Future.microtask(_restaurar);
    return const Cargando();
  }

  Autenticador get _autenticador => ref.read(autenticadorProvider);
  AlmacenDeTokens get _almacen => ref.read(almacenProvider);

  Future<void> _restaurar() async {
    try {
      final guardados = await _almacen.leer();
      if (guardados == null) {
        state = const SinSesion();
      } else if (guardados.vigente) {
        state = ConSesion(guardados);
      } else {
        await _renovar(guardados.refresco);
      }
    } on Object {
      await _almacen.borrar();
      state = const SinSesion();
    }
  }

  Future<void> _renovar(String? refresco) async {
    final nuevos = refresco == null
        ? null
        : await _autenticador.renovar(refresco);
    if (nuevos == null) {
      await _almacen.borrar();
      state = const SinSesion();
      return;
    }
    await _almacen.guardar(nuevos);
    state = ConSesion(nuevos);
  }

  Future<void> iniciar() async {
    state = const Cargando();
    try {
      final tokens = await _autenticador.iniciar();
      if (tokens == null) {
        state = const SinSesion();
        return;
      }
      await _almacen.guardar(tokens);
      state = ConSesion(tokens);
    } on Object {
      state = const SinSesion(fallo: true);
    }
  }

  /// Borra los tokens primero y después termina la sesión en Keycloak, para que
  /// un fallo del navegador no deje credenciales en el equipo (IDA-004).
  Future<void> cerrar() async {
    final actual = state;
    final idToken = actual is ConSesion ? actual.tokens.idToken : null;
    state = const SinSesion();
    await _almacen.borrar();
    try {
      await _autenticador.cerrar(idToken);
    } on Object {
      // La sesión local ya no existe; la de Keycloak vence sola.
    }
  }
}
