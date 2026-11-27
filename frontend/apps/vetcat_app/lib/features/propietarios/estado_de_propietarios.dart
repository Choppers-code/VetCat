import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vetcat_core/vetcat_core.dart';

import '../sesion/controlador_de_clinica.dart';
import 'repositorio_de_propietarios.dart';

/// Dónde está la persona dentro del módulo. Se navega por estado y no con
/// `Navigator.push`: una ruta apilada sobreviviría al cierre de sesión y
/// dejaría datos a la vista (IDA-004). #184 lo sustituirá por el router.
sealed class Paso {
  const Paso();
}

class PasoLista extends Paso {
  const PasoLista();
}

class PasoFicha extends Paso {
  const PasoFicha(this.id);
  final int id;
}

final pasoProvider = NotifierProvider.autoDispose<PasoActual, Paso>(
  PasoActual.new,
);

class PasoActual extends Notifier<Paso> {
  @override
  Paso build() {
    ref.watch(clinicaIdProvider); // otra clínica empieza en la lista
    return const PasoLista();
  }

  void ir(Paso paso) => state = paso;

  /// Atrás: de la ficha a la lista.
  void atras() => state = const PasoLista();
}

final consultaProvider = NotifierProvider.autoDispose<Consulta, String>(
  Consulta.new,
);

class Consulta extends Notifier<String> {
  @override
  String build() {
    ref.watch(clinicaIdProvider);
    return '';
  }

  void buscar(String texto) => state = texto.trim();
}

const minimoDeLetras = 3;

/// `null` mientras la consulta es demasiado corta.
final resultadosProvider =
    FutureProvider.autoDispose<Pagina<ResultadoDeBusqueda>?>((ref) async {
      ref.watch(clinicaIdProvider);
      final texto = ref.watch(consultaProvider);
      if (texto.length < minimoDeLetras) return null;
      return ref.watch(repositorioDePropietariosProvider).buscar(texto);
    });

final fichaProvider = FutureProvider.autoDispose
    .family<PropietarioResponse, int>((ref, id) {
      ref.watch(clinicaIdProvider);
      return ref.watch(repositorioDePropietariosProvider).ficha(id);
    });
