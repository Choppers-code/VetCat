import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_app/main.dart';

void main() {
  testWidgets('muestra el nombre de la app', (tester) async {
    await tester.pumpWidget(const VetcatApp());
    expect(find.text('VetCat móvil'), findsOneWidget);
  });
}
