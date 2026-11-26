import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vetcat_core/vetcat_core.dart';

import 'almacen_de_clinica.dart';
import 'repositorio_de_sesion.dart';
import 'token_de_sesion.dart';

const mensajeSinClinicas =
    'Aún no perteneces a ninguna clínica. Pide a tu clínica que te dé de alta.';
const mensajeSinAcceso = 'No tienes acceso a esta clínica.';

/// Único texto del backend que se muestra tal cual: el usuario ya es miembro y
/// necesita saber por qué no entra.
const mensajeClinicaNoDisponible = 'La clínica no está disponible';

sealed class EstadoDeClinica {
  const EstadoDeClinica();
}

class CargandoClinicas extends EstadoDeClinica {
  const CargandoClinicas();
}

class SinClinicas extends EstadoDeClinica {
  const SinClinicas();
}

class ErrorDeCarga extends EstadoDeClinica {
  const ErrorDeCarga(this.mensaje);
  final String mensaje;
}

class ElegirClinica extends EstadoDeClinica {
  const ElegirClinica(this.clinicas, {this.sugerida, this.aviso});
  final List<ClinicaDelUsuario> clinicas;

  /// Última clínica usada: se marca, pero no salta la elección.
  final int? sugerida;
  final String? aviso;
}

class EnClinica extends EstadoDeClinica {
  const EnClinica(this.sesion, this.clinicas);
  final SesionResponse sesion;
  final List<ClinicaDelUsuario> clinicas;

  bool get puedeCambiarDeClinica => clinicas.length > 1;
}

final almacenDeClinicaProvider = Provider<AlmacenDeClinica>(
  (ref) => AlmacenDeClinicaSeguro(),
);

final apiProvider = Provider<ApiCliente>(
  (ref) => ApiCliente.desdeEntorno(tokens: ProveedorDeTokenDeSesion(ref)),
);

final repositorioDeSesionProvider = Provider<RepositorioDeSesion>(
  (ref) => RepositorioDeSesion(ref.watch(apiProvider)),
);

/// Se libera al cerrar sesión: el token de clínica no sobrevive a la sesión.
final clinicaProvider =
    NotifierProvider.autoDispose<ControladorDeClinica, EstadoDeClinica>(
      ControladorDeClinica.new,
    );

/// Identificador de la clínica activa. **Todo provider con datos de una
/// clínica debe hacer `ref.watch` de este**, para que cambiar de clínica lo
/// descarte (IDA-003).
final clinicaIdProvider = Provider.autoDispose<int?>(
  (ref) => switch (ref.watch(clinicaProvider)) {
    EnClinica(:final sesion) => sesion.clinicaId,
    _ => null,
  },
);

class ControladorDeClinica extends Notifier<EstadoDeClinica> {
  String? _avisoPendiente;

  @override
  EstadoDeClinica build() {
    Future.microtask(cargar);
    return const CargandoClinicas();
  }

  RepositorioDeSesion get _repositorio => ref.read(repositorioDeSesionProvider);
  AlmacenDeClinica get _ultima => ref.read(almacenDeClinicaProvider);

  Future<void> cargar() async {
    state = const CargandoClinicas();
    try {
      final todas = await _repositorio.clinicas();
      final clinicas = todas.where((c) => c.visible).toList();
      if (clinicas.isEmpty) {
        state = const SinClinicas();
      } else if (clinicas.length == 1 && clinicas.single.activa) {
        await _abrir(clinicas.single.id, clinicas);
      } else {
        final ultima = await _ultima.leer();
        state = ElegirClinica(
          clinicas,
          sugerida: clinicas.any((c) => c.id == ultima) ? ultima : null,
        );
      }
    } on ErrorDeApi catch (e) {
      state = ErrorDeCarga(ErrorDeApi.mensajePorTipo(e.tipo));
    } on Object {
      state = ErrorDeCarga(ErrorDeApi.mensajePorTipo(TipoDeError.desconocido));
    }
  }

  Future<void> elegir(ClinicaDelUsuario clinica) async {
    final actual = state;
    if (actual is! ElegirClinica || !clinica.activa) return;
    await _abrir(clinica.id, actual.clinicas);
  }

  Future<void> _abrir(int id, List<ClinicaDelUsuario> clinicas) async {
    state = const CargandoClinicas();
    try {
      final sesion = await _repositorio.abrir(id);
      await _ultima.guardar(id);
      state = EnClinica(sesion, clinicas);
    } on ErrorDeApi catch (e) {
      state = ElegirClinica(clinicas, sugerida: id, aviso: _aviso(e));
    }
  }

  /// Cierra el contexto de la clínica y vuelve al selector sin pedir
  /// credenciales (IDA-003).
  void cambiarDeClinica() {
    final actual = state;
    if (actual is EnClinica && actual.puedeCambiarDeClinica) {
      state = ElegirClinica(actual.clinicas, sugerida: actual.sesion.clinicaId);
    }
  }

  /// Renueva el token de clínica tras un 401 (no hay refresco: se reabre). Una
  /// sola vez; el estado no pasa por «cargando» para no desmontar la pantalla.
  Future<bool> reabrir() async {
    final actual = state;
    if (actual is! EnClinica) return false;
    try {
      final sesion = await _repositorio.abrir(actual.sesion.clinicaId);
      state = EnClinica(sesion, actual.clinicas);
      return true;
    } on ErrorDeApi catch (e) {
      _avisoPendiente = _aviso(e);
      return false;
    }
  }

  /// La clínica ya no se puede usar: al selector con un aviso.
  void volverAlSelector() {
    final actual = state;
    if (actual is! EnClinica) return;
    state = ElegirClinica(
      actual.clinicas,
      sugerida: actual.sesion.clinicaId,
      aviso: _avisoPendiente ?? mensajeSinAcceso,
    );
    _avisoPendiente = null;
  }

  static String _aviso(ErrorDeApi e) {
    if (e.mensaje == mensajeClinicaNoDisponible) return e.mensaje;
    return e.tipo == TipoDeError.prohibido
        ? mensajeSinAcceso
        : ErrorDeApi.mensajePorTipo(e.tipo);
  }
}
