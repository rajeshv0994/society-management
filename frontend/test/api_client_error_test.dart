import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:society_app/src/api.dart';

void main() {
  test(
    'public frontend explains a service outage without technical details',
    () async {
      final api = ApiClient(
        baseUrl: 'https://api.example',
        httpClient: MockClient((_) async {
          throw http.ClientException('Connection refused: private-host');
        }),
      );

      await expectLater(
        api.get('/api/public/plans'),
        throwsA(
          isA<ApiException>()
              .having(
                (error) => error.message,
                'message',
                contains('Unable to reach'),
              )
              .having(
                (error) => error.message,
                'technical details',
                isNot(contains('private-host')),
              ),
        ),
      );
    },
  );

  test('public frontend maps inactive society response', () async {
    final api = ApiClient(
      baseUrl: 'https://api.example',
      httpClient: MockClient(
        (_) async => http.Response(
          jsonEncode({'message': 'Society account is not active'}),
          401,
        ),
      ),
    );

    await expectLater(
      api.post('/api/auth/login', const {}),
      throwsA(
        isA<ApiException>().having(
          (error) => error.message,
          'message',
          contains('not active yet'),
        ),
      ),
    );
  });
}
