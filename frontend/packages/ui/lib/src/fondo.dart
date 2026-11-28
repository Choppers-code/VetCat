import 'package:flutter/material.dart';

import 'tokens.dart';

/// Fondo con dos manchas en la esquina superior derecha, detrás de [child].
class VetcatFondoDeOndas extends StatelessWidget {
  const VetcatFondoDeOndas({required this.child, super.key});

  final Widget child;

  @override
  Widget build(BuildContext context) => Stack(
    fit: StackFit.expand,
    children: [
      const Positioned(
        top: -90,
        right: -100,
        child: _Mancha(260, 200, VetcatColores.principal),
      ),
      Positioned(
        top: 50,
        right: -80,
        child: _Mancha(170, 120, VetcatColores.acento.withValues(alpha: 0.45)),
      ),
      child,
    ],
  );
}

class _Mancha extends StatelessWidget {
  const _Mancha(this.ancho, this.alto, this.color);

  final double ancho;
  final double alto;
  final Color color;

  @override
  Widget build(BuildContext context) => Container(
    width: ancho,
    height: alto,
    decoration: BoxDecoration(
      color: color,
      borderRadius: BorderRadius.all(Radius.elliptical(ancho / 2, alto / 2)),
    ),
  );
}
