/// Tokens de diseño de VetCat: el único lugar donde viven los colores, la
/// tipografía, los espaciados y los radios. Cambiar el aspecto de la app es
/// cambiar este archivo; el resto del código solo los referencia.
///
/// Paleta propuesta por el área 4 (turquesa con acento coral), pendiente de
/// aprobación del equipo (ver issue #60).
library;

import 'package:flutter/painting.dart';

/// Colores de la marca y de los estados.
abstract final class VetcatColores {
  /// Turquesa de la marca. Contraste 4,9:1 con texto blanco.
  static const Color principal = Color(0xFF0E7C86);

  /// Coral de acento. Con texto blanco no llega a 4,5:1: usar [sobreAcento].
  static const Color acento = Color(0xFFF28C6B);

  static const Color fondo = Color(0xFFF7FAFA);
  static const Color superficie = Color(0xFFFFFFFF);
  static const Color texto = Color(0xFF1E2A2B);
  static const Color borde = Color(0xFFD3E0E1);

  static const Color exito = Color(0xFF2E7D32);
  static const Color aviso = Color(0xFF9A5B00);
  static const Color error = Color(0xFFC62828);

  /// Color del texto sobre [principal].
  static const Color sobrePrincipal = Color(0xFFFFFFFF);

  /// Color del texto sobre [acento].
  static const Color sobreAcento = texto;

  /// Color del texto sobre [exito], [aviso] y [error].
  static const Color sobreEstado = Color(0xFFFFFFFF);
}

/// Escala tipográfica (tamaños en puntos lógicos). La fuente es la del
/// sistema; no se añade ninguna dependencia de fuentes.
abstract final class VetcatTipografia {
  static const double titulo = 28;
  static const double subtitulo = 20;
  static const double cuerpo = 16;
  static const double cuerpoPequeno = 14;
  static const double etiqueta = 12;

  static const FontWeight pesoNormal = FontWeight.w400;
  static const FontWeight pesoEnfasis = FontWeight.w600;
}

/// Espaciados y tamaños de contacto.
abstract final class VetcatEspaciado {
  static const double xs = 4;
  static const double s = 8;
  static const double m = 16;
  static const double l = 24;
  static const double xl = 32;

  /// Tamaño mínimo de un objetivo táctil.
  static const double objetivoTactil = 48;
}

/// Radios de esquina.
abstract final class VetcatRadios {
  static const double s = 8;
  static const double m = 12;
  static const double l = 16;
}
