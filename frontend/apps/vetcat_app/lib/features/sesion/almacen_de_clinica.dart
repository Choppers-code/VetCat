import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Última clínica usada. Solo el `id` y solo como sugerencia: el tenant sale
/// siempre de `POST /sesion/clinicas/{id}`, nunca de aquí.
abstract interface class AlmacenDeClinica {
  Future<int?> leer();
  Future<void> guardar(int clinicaId);
  Future<void> borrar();
}

class AlmacenDeClinicaSeguro implements AlmacenDeClinica {
  AlmacenDeClinicaSeguro([FlutterSecureStorage? almacen])
    : _almacen = almacen ?? const FlutterSecureStorage();

  static const _clave = 'vetcat.ultima_clinica';

  final FlutterSecureStorage _almacen;

  @override
  Future<int?> leer() async =>
      int.tryParse(await _almacen.read(key: _clave) ?? '');

  @override
  Future<void> guardar(int clinicaId) =>
      _almacen.write(key: _clave, value: '$clinicaId');

  @override
  Future<void> borrar() => _almacen.delete(key: _clave);
}
