/// Design system de VetCat: componentes reutilizables de las apps.
library;

import 'package:flutter/material.dart';

export 'src/tema.dart';
export 'src/tokens.dart';

/// Pantalla mínima con un título centrado. Toma sus colores y su tipografía
/// del tema de la app (`VetcatTema`); sirve de base mientras llegan los
/// componentes reales.
class PantallaMinima extends StatelessWidget {
  const PantallaMinima({super.key, required this.titulo});

  final String titulo;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Text(titulo, style: Theme.of(context).textTheme.headlineMedium),
      ),
    );
  }
}
