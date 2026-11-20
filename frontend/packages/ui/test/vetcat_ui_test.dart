import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_ui/vetcat_ui.dart';

void main() {
  testWidgets('PantallaMinima muestra el título', (tester) async {
    await tester.pumpWidget(
      const MaterialApp(home: PantallaMinima(titulo: 'Hola')),
    );
    expect(find.text('Hola'), findsOneWidget);
  });
}
