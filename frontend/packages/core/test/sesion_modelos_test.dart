import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_core/vetcat_core.dart';

void main() {
  test('ClinicaDelUsuario lee el JSON del contrato', () {
    final c = ClinicaDelUsuario.fromJson({
      'id': 7,
      'nombre': 'Patitas',
      'estado': 'SUSPENDIDA',
      'roles': ['veterinario', 'cliente'],
    });

    expect(c.id, 7);
    expect(c.suspendida, isTrue);
    expect(c.activa, isFalse);
    expect(c.puedeCambiarDeVista, isTrue);
    expect(c.roles, ['veterinario', 'cliente']);
  });

  test('BAJA no es visible y un cliente puro no cambia de vista', () {
    final baja = ClinicaDelUsuario.fromJson({
      'id': 1,
      'nombre': 'X',
      'estado': 'BAJA',
      'roles': ['cliente'],
    });

    expect(baja.visible, isFalse);
    expect(baja.puedeCambiarDeVista, isFalse);
  });

  test('SesionResponse lee el JSON y no muestra el token al imprimirse', () {
    final s = SesionResponse.fromJson({
      'token': 'secreto-muy-largo',
      'clinicaId': 3,
      'nombre': 'Patitas',
      'roles': ['cliente'],
    });

    expect(s.token, 'secreto-muy-largo');
    expect(s.clinicaId, 3);
    expect('$s', isNot(contains('secreto')));
  });
}
