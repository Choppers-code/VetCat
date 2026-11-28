import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_ui/vetcat_ui.dart';

void main() {
  Future<void> mostrar(WidgetTester tester, Widget hijo) => tester.pumpWidget(
    MaterialApp(
      theme: VetcatTema.claro,
      home: Scaffold(body: Center(child: hijo)),
    ),
  );

  testWidgets('el logo se pinta con el ancho pedido', (tester) async {
    await mostrar(tester, const VetcatLogo(ancho: 100));

    expect(tester.takeException(), isNull);
    expect(tester.getSize(find.byType(VetcatLogo)).width, 100);
  });

  testWidgets('el fondo de ondas muestra su contenido', (tester) async {
    await mostrar(tester, const VetcatFondoDeOndas(child: Text('contenido')));

    expect(tester.takeException(), isNull);
    expect(find.text('contenido').hitTestable(), findsOneWidget);
  });

  testWidgets('la marca dice VetCat', (tester) async {
    await mostrar(tester, const VetcatMarca());

    expect(find.text('VetCat'), findsOneWidget);
  });
}
