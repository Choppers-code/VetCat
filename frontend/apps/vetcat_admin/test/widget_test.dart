import 'package:flutter_test/flutter_test.dart';
import 'package:vetcat_admin/main.dart';

void main() {
  testWidgets('muestra el nombre de la app', (tester) async {
    await tester.pumpWidget(const VetcatAdmin());
    expect(find.text('VetCat Admin'), findsOneWidget);
  });
}
