import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_core/vetcat_core.dart';

void main() {
  test('el alta no manda lo que va vacío', () {
    final json = const PropietarioRequest(
      nombre: 'Ana',
      telefono: '555',
    ).toJson();

    expect(json, {
      'nombre': 'Ana',
      'telefono': '555',
      'confirmarDuplicado': false,
    });
  });

  test('lee la ficha con sus mascotas y la especie del contrato', () {
    final p = PropietarioResponse.fromJson({
      'id': 5,
      'nombre': 'Ana',
      'telefono': '555',
      'activo': true,
      'mascotas': [
        {
          'id': 9,
          'propietarioId': 5,
          'nombre': 'Luna',
          'especie': 'GATO',
          'activo': true,
        },
      ],
    });

    expect(p.mascotas.single.especie, Especie.gato);
    expect(p.correo, isNull);
  });

  test('un resultado de búsqueda puede venir sin mascota', () {
    final r = ResultadoDeBusqueda.fromJson({
      'propietario': {
        'id': 5,
        'nombre': 'Ana',
        'telefono': '555',
        'activo': true,
        'mascotas': <Object?>[],
      },
    });

    expect(r.mascota, isNull);
  });
}
