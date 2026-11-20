import 'package:flutter/material.dart';

import 'tokens.dart';

/// Tema de VetCat. Se construye solo con los tokens de `tokens.dart`: aquí no
/// hay colores ni medidas escritas a mano.
abstract final class VetcatTema {
  static final ThemeData claro = _construir();

  static ThemeData _construir() {
    const esquema = ColorScheme(
      brightness: Brightness.light,
      primary: VetcatColores.principal,
      onPrimary: VetcatColores.sobrePrincipal,
      secondary: VetcatColores.acento,
      onSecondary: VetcatColores.sobreAcento,
      error: VetcatColores.error,
      onError: VetcatColores.sobreEstado,
      surface: VetcatColores.superficie,
      onSurface: VetcatColores.texto,
      outline: VetcatColores.borde,
    );

    final bordeCampo = OutlineInputBorder(
      borderRadius: BorderRadius.circular(VetcatRadios.m),
      borderSide: const BorderSide(color: VetcatColores.borde),
    );
    final forma = RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(VetcatRadios.m),
    );
    const tamanoBoton = Size.fromHeight(VetcatEspaciado.objetivoTactil);

    return ThemeData(
      useMaterial3: true,
      colorScheme: esquema,
      scaffoldBackgroundColor: VetcatColores.fondo,
      dividerColor: VetcatColores.borde,
      textTheme: const TextTheme(
        headlineMedium: TextStyle(
          fontSize: VetcatTipografia.titulo,
          fontWeight: VetcatTipografia.pesoEnfasis,
          color: VetcatColores.texto,
        ),
        titleMedium: TextStyle(
          fontSize: VetcatTipografia.subtitulo,
          fontWeight: VetcatTipografia.pesoEnfasis,
          color: VetcatColores.texto,
        ),
        bodyLarge: TextStyle(
          fontSize: VetcatTipografia.cuerpo,
          fontWeight: VetcatTipografia.pesoNormal,
          color: VetcatColores.texto,
        ),
        bodyMedium: TextStyle(
          fontSize: VetcatTipografia.cuerpoPequeno,
          fontWeight: VetcatTipografia.pesoNormal,
          color: VetcatColores.texto,
        ),
        labelLarge: TextStyle(
          fontSize: VetcatTipografia.cuerpoPequeno,
          fontWeight: VetcatTipografia.pesoEnfasis,
          color: VetcatColores.texto,
        ),
        bodySmall: TextStyle(
          fontSize: VetcatTipografia.etiqueta,
          fontWeight: VetcatTipografia.pesoNormal,
          color: VetcatColores.texto,
        ),
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: VetcatColores.principal,
        foregroundColor: VetcatColores.sobrePrincipal,
        elevation: 0,
      ),
      cardTheme: CardThemeData(
        color: VetcatColores.superficie,
        elevation: 0,
        shape: forma.copyWith(side: const BorderSide(color: VetcatColores.borde)),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: VetcatColores.superficie,
        contentPadding: const EdgeInsets.symmetric(
          horizontal: VetcatEspaciado.m,
          vertical: VetcatEspaciado.s,
        ),
        border: bordeCampo,
        enabledBorder: bordeCampo,
        focusedBorder: bordeCampo.copyWith(
          borderSide: const BorderSide(color: VetcatColores.principal),
        ),
        errorBorder: bordeCampo.copyWith(
          borderSide: const BorderSide(color: VetcatColores.error),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          minimumSize: tamanoBoton,
          shape: forma,
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          minimumSize: tamanoBoton,
          shape: forma,
          side: const BorderSide(color: VetcatColores.principal),
        ),
      ),
    );
  }
}
