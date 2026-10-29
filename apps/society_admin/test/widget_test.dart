import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:society_admin_app/main.dart' as app;
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  testWidgets('renders society admin login', (tester) async {
    SharedPreferences.setMockInitialValues({});
    app.main();
    await tester.pump();
    await tester.pump();
    expect(find.text('Society Administration'), findsOneWidget);
    expect(find.byIcon(Icons.mail_outline_rounded), findsOneWidget);
    expect(find.byIcon(Icons.lock_outline_rounded), findsOneWidget);
    expect(find.byIcon(Icons.visibility_outlined), findsOneWidget);
  });
}
