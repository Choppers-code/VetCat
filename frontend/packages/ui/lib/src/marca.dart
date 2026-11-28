import 'package:flutter/material.dart';

import 'tokens.dart';

/// Logo de VetCat: una huella con una cruz. Dibujado sobre una malla de 100×92.
class VetcatLogo extends StatelessWidget {
  const VetcatLogo({required this.ancho, super.key});

  final double ancho;

  @override
  Widget build(BuildContext context) => ExcludeSemantics(
    child: CustomPaint(
      size: Size(ancho, ancho * 0.92),
      painter: const _Huella(),
    ),
  );
}

/// El nombre del producto, con «Cat» en el color principal.
class VetcatMarca extends StatelessWidget {
  const VetcatMarca({super.key});

  @override
  Widget build(BuildContext context) => const Text.rich(
    TextSpan(
      text: 'Vet',
      children: [
        TextSpan(
          text: 'Cat',
          style: TextStyle(color: VetcatColores.principal),
        ),
      ],
    ),
    style: TextStyle(
      fontSize: VetcatTipografia.marca,
      fontWeight: FontWeight.w800,
      color: VetcatColores.texto,
    ),
    textAlign: TextAlign.center,
  );
}

class _Huella extends CustomPainter {
  const _Huella();

  @override
  void paint(Canvas canvas, Size size) {
    canvas.scale(size.width / 100);
    final turquesa = Paint()..color = VetcatColores.principal;

    // Dedos: centro, radios e inclinación en radianes.
    for (final (x, y, rx, ry, giro) in const [
      (14.0, 42.0, 11.0, 14.0, -0.38),
      (36.0, 17.0, 11.5, 15.0, -0.14),
      (64.0, 17.0, 11.5, 15.0, 0.14),
      (86.0, 42.0, 11.0, 14.0, 0.38),
    ]) {
      canvas
        ..save()
        ..translate(x, y)
        ..rotate(giro)
        ..drawOval(
          Rect.fromCenter(center: Offset.zero, width: rx * 2, height: ry * 2),
          turquesa,
        )
        ..restore();
    }

    final planta = Path()
      ..moveTo(50, 37)
      ..relativeCubicTo(17, 0, 31, 18, 31, 33)
      ..relativeCubicTo(0, 13, -10, 19, -19, 19)
      ..relativeCubicTo(-5, 0, -7, -3, -12, -3)
      ..relativeCubicTo(-5, 0, -7, 3, -12, 3)
      ..relativeCubicTo(-9, 0, -19, -6, -19, -19)
      ..relativeCubicTo(0, -15, 14, -33, 31, -33)
      ..close();
    canvas.drawPath(planta, turquesa);

    final cruz = Paint()..color = VetcatColores.superficie;
    canvas
      ..drawRect(const Rect.fromLTWH(45, 52, 10, 28), cruz)
      ..drawRect(const Rect.fromLTWH(36, 61, 28, 10), cruz);
  }

  @override
  bool shouldRepaint(_Huella anterior) => false;
}
