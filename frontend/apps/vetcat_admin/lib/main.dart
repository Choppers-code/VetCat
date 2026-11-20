import 'package:flutter/material.dart';
import 'package:vetcat_core/vetcat_core.dart';
import 'package:vetcat_ui/vetcat_ui.dart';

void main() => runApp(const VetcatAdmin());

class VetcatAdmin extends StatelessWidget {
  const VetcatAdmin({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: nombreProducto,
      home: const PantallaMinima(titulo: '$nombreProducto Admin'),
    );
  }
}
