import 'dart:io';

import 'package:flutter_test/flutter_test.dart';

/// El aspecto se cambia en `packages/ui`: la app solo referencia sus tokens
/// (#263).
void main() {
  test('la app no tiene colores escritos a mano', () {
    final prohibido = RegExp(r'Color\(0x|Color\.from|Colors\.');
    final archivos = Directory('lib')
        .listSync(recursive: true)
        .whereType<File>()
        .where((f) => f.path.endsWith('.dart'));

    expect(archivos, isNotEmpty);
    for (final f in archivos) {
      expect(
        prohibido.hasMatch(f.readAsStringSync()),
        isFalse,
        reason: '${f.path} define un color: usa los tokens de vetcat_ui',
      );
    }
  });
}
