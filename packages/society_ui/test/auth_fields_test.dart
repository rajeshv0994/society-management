import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  testWidgets(
    'authentication fields render icons and toggle password visibility',
    (tester) async {
      final email = TextEditingController();
      final password = TextEditingController();
      addTearDown(email.dispose);
      addTearDown(password.dispose);

      await tester.pumpWidget(
        MaterialApp(
          theme: SocietyTheme.light(),
          home: Scaffold(
            body: Column(
              children: [
                SocietyEmailField(controller: email),
                SocietyPasswordField(controller: password),
              ],
            ),
          ),
        ),
      );

      expect(find.byIcon(Icons.mail_outline_rounded), findsOneWidget);
      expect(find.byIcon(Icons.lock_outline_rounded), findsOneWidget);
      expect(find.byIcon(Icons.visibility_outlined), findsOneWidget);
      expect(
        tester.widgetList<TextField>(find.byType(TextField)).last.obscureText,
        isTrue,
      );

      await tester.tap(find.byTooltip('Show password'));
      await tester.pump();

      expect(find.byIcon(Icons.visibility_off_outlined), findsOneWidget);
      expect(
        tester.widgetList<TextField>(find.byType(TextField)).last.obscureText,
        isFalse,
      );
    },
  );
}
