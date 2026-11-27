import 'dart:io';

import 'package:flutter_test/flutter_test.dart';

/// Ajustes de Android sin los que el inicio de sesión falla en el equipo (#253).
void main() {
  String manifiesto(String variante) =>
      File('android/app/src/$variante/AndroidManifest.xml').readAsStringSync();

  test('sin taskAffinity vacía: la respuesta de Keycloak vuelve a la app', () {
    expect(manifiesto('main'), isNot(contains('android:taskAffinity=""')));
  });

  test('http solo en depuración', () {
    const permiso = 'android:usesCleartextTraffic="true"';

    expect(manifiesto('debug'), contains(permiso));
    expect(manifiesto('main'), isNot(contains(permiso)));
    expect(manifiesto('profile'), isNot(contains(permiso)));
  });
}
