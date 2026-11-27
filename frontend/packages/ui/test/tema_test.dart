import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_ui/vetcat_ui.dart';

/// Contraste WCAG entre dos colores (de 1 a 21).
double contraste(Color a, Color b) {
  final claro = a.computeLuminance() > b.computeLuminance() ? a : b;
  final oscuro = identical(claro, a) ? b : a;
  return (claro.computeLuminance() + 0.05) / (oscuro.computeLuminance() + 0.05);
}

void main() {
  const minimo = 4.5;

  group('contraste mínimo de 4,5:1', () {
    final casos = <String, (Color, Color)>{
      'texto sobre el color principal': (
        VetcatColores.sobrePrincipal,
        VetcatColores.principal,
      ),
      'texto sobre el color de acento': (
        VetcatColores.sobreAcento,
        VetcatColores.acento,
      ),
      'texto sobre el fondo': (VetcatColores.texto, VetcatColores.fondo),
      'texto sobre la superficie': (
        VetcatColores.texto,
        VetcatColores.superficie,
      ),
      'texto secundario sobre el fondo': (
        VetcatColores.textoSecundario,
        VetcatColores.fondo,
      ),
      'texto secundario sobre la superficie': (
        VetcatColores.textoSecundario,
        VetcatColores.superficie,
      ),
      'texto sobre el turquesa suave': (
        VetcatColores.principalOscuro,
        VetcatColores.suave,
      ),
      'texto sobre durazno': (VetcatColores.texto, VetcatColores.durazno),
      'texto sobre el turquesa oscuro': (
        VetcatColores.sobrePrincipal,
        VetcatColores.principalOscuro,
      ),
      'texto sobre éxito': (VetcatColores.sobreEstado, VetcatColores.exito),
      'texto sobre aviso': (VetcatColores.sobreEstado, VetcatColores.aviso),
      'texto sobre error': (VetcatColores.sobreEstado, VetcatColores.error),
    };
    casos.forEach((nombre, par) {
      test(nombre, () {
        expect(contraste(par.$1, par.$2), greaterThanOrEqualTo(minimo));
      });
    });
  });

  test('VetcatTema se construye con los tokens', () {
    final tema = VetcatTema.claro;
    expect(tema.colorScheme.primary, VetcatColores.principal);
    expect(tema.colorScheme.secondary, VetcatColores.acento);
    expect(tema.scaffoldBackgroundColor, VetcatColores.fondo);
    expect(tema.textTheme.headlineMedium?.fontSize, VetcatTipografia.titulo);
  });

  group('formas del rediseño', () {
    final tema = VetcatTema.claro;

    RoundedRectangleBorder forma(ButtonStyle? estilo) =>
        estilo!.shape!.resolve({})! as RoundedRectangleBorder;

    test('los botones son píldoras', () {
      final esperado = BorderRadius.circular(VetcatRadios.pildora);

      expect(forma(tema.filledButtonTheme.style).borderRadius, esperado);
      expect(forma(tema.outlinedButtonTheme.style).borderRadius, esperado);
    });

    test('el botón flotante también es una píldora', () {
      final figura =
          tema.floatingActionButtonTheme.shape! as RoundedRectangleBorder;

      expect(figura.borderRadius, BorderRadius.circular(VetcatRadios.pildora));
    });

    test('los campos son píldoras', () {
      final borde =
          tema.inputDecorationTheme.enabledBorder! as OutlineInputBorder;

      expect(borde.borderRadius, BorderRadius.circular(VetcatRadios.pildora));
    });

    test('las tarjetas son redondeadas, sin borde y con sombra', () {
      final figura = tema.cardTheme.shape! as RoundedRectangleBorder;

      expect(figura.borderRadius, BorderRadius.circular(VetcatRadios.xl));
      expect(figura.side, BorderSide.none);
      expect(tema.cardTheme.elevation, greaterThan(0));
      expect(tema.cardTheme.shadowColor, VetcatColores.sombra);
    });

    test('la barra es clara y los iconos del sistema se leen', () {
      final barra = tema.appBarTheme;

      expect(barra.backgroundColor, VetcatColores.fondo);
      expect(barra.foregroundColor, VetcatColores.texto);
      expect(
        barra.systemOverlayStyle?.statusBarIconBrightness,
        Brightness.dark,
      );
    });
  });

  test('no hay colores escritos a mano fuera de tokens.dart', () {
    final prohibido = RegExp(r'Color\(0x|Colors\.');
    final archivos = Directory('lib')
        .listSync(recursive: true)
        .whereType<File>()
        .where((f) => f.path.endsWith('.dart'))
        .where(
          (f) => !f.path.replaceAll('\\', '/').endsWith('src/tokens.dart'),
        );
    for (final f in archivos) {
      expect(
        prohibido.hasMatch(f.readAsStringSync()),
        isFalse,
        reason: '${f.path} define un color fuera de tokens.dart',
      );
    }
  });

  testWidgets('PantallaMinima usa el tema', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        theme: VetcatTema.claro,
        home: const PantallaMinima(titulo: 'Hola'),
      ),
    );
    final texto = tester.widget<Text>(find.text('Hola'));
    final contexto = tester.element(find.text('Hola'));
    expect(
      Theme.of(contexto).textTheme.headlineMedium?.color,
      VetcatColores.texto,
    );
    expect(texto.data, 'Hola');
  });
}
