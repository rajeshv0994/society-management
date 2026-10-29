import 'dart:async';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  test('stalled requests fail with a useful timeout error', () async {
    final client = HttpApiClient(
      baseUrl: 'http://unused',
      requestTimeout: const Duration(milliseconds: 20),
      httpClient: MockClient((_) => Completer<http.Response>().future),
    );

    await expectLater(
      client.get('/stalled'),
      throwsA(
        isA<ApiException>().having(
          (error) => error.message,
          'message',
          contains('timed out'),
        ),
      ),
    );
  });
}
