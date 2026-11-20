/// Design system de VetCat: componentes reutilizables de las apps.
library;

import 'package:flutter/material.dart';

/// Pantalla mínima con un título centrado. Sirve de base mientras llegan
/// el tema y los componentes reales.
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
