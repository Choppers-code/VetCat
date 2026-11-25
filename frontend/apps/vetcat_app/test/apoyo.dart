import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/misc.dart' show Override;
import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_app/features/sesion/almacen_de_clinica.dart';
import 'package:vetcat_app/features/sesion/controlador_de_clinica.dart';
import 'package:vetcat_app/features/sesion/repositorio_de_sesion.dart';
import 'package:vetcat_app/sesion/almacen_de_tokens.dart';
import 'package:vetcat_app/sesion/autenticador.dart';
import 'package:vetcat_app/sesion/controlador_de_sesion.dart';
import 'package:vetcat_app/sesion/pantallas_de_sesion.dart';
import 'package:vetcat_app/sesion/tokens_de_identidad.dart';
import 'package:vetcat_core/vetcat_core.dart';

TokensDeIdentidad tokens({
  Duration vida = const Duration(minutes: 5),
  String? refresco = 'refresco',
}) => TokensDeIdentidad(
  acceso: 'acceso',
  refresco: refresco,
  idToken: 'id',
  vence: DateTime.now().add(vida),
);

ClinicaDelUsuario clinica(
  int id, {
  String estado = 'ACTIVA',
  List<String> roles = const ['veterinario'],
}) => ClinicaDelUsuario(
  id: id,
  nombre: 'Clínica $id',
  estado: estado,
  roles: roles,
);

SesionResponse sesionDe(
  int id, {
  String token = 'token-clinica',
  List<String> roles = const ['veterinario'],
}) => SesionResponse(
  token: token,
  clinicaId: id,
  nombre: 'Clínica $id',
  roles: roles,
);

ErrorDeApi errorDeApi(int estado, String mensaje) => ErrorDeApi(
  tipo: ErrorDeApi.tipoPorEstado(estado),
  mensaje: mensaje,
  codigoHttp: estado,
);

class AlmacenEnMemoria implements AlmacenDeTokens {
  AlmacenEnMemoria([this.guardado]);
  TokensDeIdentidad? guardado;

  @override
  Future<TokensDeIdentidad?> leer() async => guardado;
  @override
  Future<void> guardar(TokensDeIdentidad t) async => guardado = t;
  @override
  Future<void> borrar() async => guardado = null;
}

class AlmacenDeClinicaFalso implements AlmacenDeClinica {
  int? id;

  @override
  Future<int?> leer() async => id;
  @override
  Future<void> guardar(int clinicaId) async => id = clinicaId;
  @override
  Future<void> borrar() async => id = null;
}

class AutenticadorFalso implements Autenticador {
  Object? alIniciar; // TokensDeIdentidad, null (cancelar) o un error.
  Object? alRenovar;
  Object? alCerrar;
  String? idTokenAlCerrar;
  int cierres = 0;
  int renovaciones = 0;
  int inicios = 0;

  @override
  Future<TokensDeIdentidad?> iniciar() async {
    inicios++;
    final r = alIniciar;
    if (r is TokensDeIdentidad?) return r;
    throw r;
  }

  @override
  Future<TokensDeIdentidad?> renovar(String refresco) async {
    renovaciones++;
    final r = alRenovar;
    if (r is TokensDeIdentidad?) return r;
    throw r;
  }

  @override
  Future<void> cerrar(String? idToken) async {
    cierres++;
    idTokenAlCerrar = idToken;
    if (alCerrar != null) throw alCerrar!;
  }
}

/// Repositorio sin red: lo que se prepara es lo que responde.
class RepositorioFalso implements RepositorioDeSesion {
  List<ClinicaDelUsuario> lista = [clinica(1)];
  Object? alListar; // un error, o null para devolver [lista].
  final Map<int, Object> alAbrir = {}; // SesionResponse o un error.
  final List<int> aperturas = [];

  @override
  Future<List<ClinicaDelUsuario>> clinicas() async {
    if (alListar != null) throw alListar!;
    return lista;
  }

  @override
  Future<SesionResponse> abrir(int clinicaId) async {
    aperturas.add(clinicaId);
    final r = alAbrir[clinicaId] ?? sesionDe(clinicaId);
    if (r is SesionResponse) return r;
    throw r;
  }
}

/// Todo lo que una prueba de sesión necesita, ya conectado.
class Entorno {
  final almacen = AlmacenEnMemoria();
  final ultima = AlmacenDeClinicaFalso();
  final auth = AutenticadorFalso();
  final repositorio = RepositorioFalso();

  List<Override> get overrides => [
    almacenProvider.overrideWithValue(almacen),
    almacenDeClinicaProvider.overrideWithValue(ultima),
    autenticadorProvider.overrideWithValue(auth),
    repositorioDeSesionProvider.overrideWithValue(repositorio),
  ];

  Future<void> abrir(WidgetTester tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: overrides,
        child: const MaterialApp(home: RaizDeSesion()),
      ),
    );
    await tester.pumpAndSettle();
  }

  /// Inicia sesión desde la pantalla de acceso.
  Future<void> entrar(WidgetTester tester) async {
    auth.alIniciar = tokens();
    await abrir(tester);
    await tester.tap(find.text('Iniciar sesión'));
    await tester.pumpAndSettle();
  }
}

final menu = find.byWidgetPredicate((w) => w is PopupMenuButton);

Future<void> tocarMenu(WidgetTester tester, String opcion) async {
  await tester.tap(menu);
  await tester.pumpAndSettle();
  await tester.tap(find.text(opcion));
  await tester.pumpAndSettle();
}
