import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:vetcat_core/vetcat_core.dart';
import 'package:vetcat_ui/vetcat_ui.dart';

import 'sesion/pantallas_de_sesion.dart';

void main() => runApp(const ProviderScope(child: VetcatApp()));

class VetcatApp extends StatelessWidget {
  const VetcatApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: nombreProducto,
      theme: VetcatTema.claro,
      home: const RaizDeSesion(),
    );
  }
}
