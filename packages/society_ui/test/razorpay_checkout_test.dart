import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  test('desktop checkout fails immediately with a recovery path', () async {
    if (Platform.isAndroid || Platform.isIOS) return;

    await expectLater(
      openRazorpayCheckout(const {}),
      throwsA(
        isA<UnsupportedError>().having(
          (error) => error.message,
          'message',
          contains('web app'),
        ),
      ),
    );
  });
}
