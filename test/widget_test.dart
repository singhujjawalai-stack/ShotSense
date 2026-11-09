import 'package:flutter_test/flutter_test.dart';

import 'package:shotsense/main.dart';

void main() {
  testWidgets('Home screen smoke test', (WidgetTester tester) async {
    await tester.pumpWidget(const ShotSenseApp());
    await tester.pump();

    expect(find.text('ShotSense'), findsOneWidget);
    expect(find.text('Auto-detect screenshots'), findsOneWidget);
    expect(find.text('Search screenshots by name or content...'), findsOneWidget);
  });
}
