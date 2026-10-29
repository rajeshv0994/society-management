import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  test('inactive society response becomes an actionable access message', () {
    final failure = appFailureFromResponse(
      'Society account is not active',
      statusCode: 401,
    );

    expect(failure.kind, AppFailureKind.societyInactive);
    expect(failure.message, contains('platform administrator'));
    expect(failure.message, isNot(contains('401')));
  });

  test('membership payment response explains the Society Admin recovery', () {
    final failure = appFailureFromResponse(
      'Platform membership invoice is due or overdue. Payment is required',
      statusCode: 403,
    );

    expect(failure.kind, AppFailureKind.paymentRequired);
    expect(failure.message, contains('Society Admin'));
  });

  test(
    'HTTP client outage is retryable and never leaks transport details',
    () async {
      final client = MockClient((_) async {
        throw http.ClientException('Connection refused: secret-host:8080');
      });
      final api = HttpApiClient(
        baseUrl: 'https://api.example',
        httpClient: client,
      );

      await expectLater(
        api.get('/api/public/societies'),
        throwsA(
          isA<ApiException>()
              .having((error) => error.retryable, 'retryable', isTrue)
              .having(
                (error) => error.message,
                'message',
                contains('Unable to reach'),
              )
              .having(
                (error) => error.message,
                'technical details',
                isNot(contains('secret-host')),
              ),
        ),
      );
    },
  );

  test('HTTP timeout produces a bounded retryable failure', () async {
    final client = MockClient((_) async {
      await Future<void>.delayed(const Duration(milliseconds: 50));
      return http.Response('{}', 200);
    });
    final api = HttpApiClient(
      baseUrl: 'https://api.example',
      httpClient: client,
      requestTimeout: const Duration(milliseconds: 1),
    );

    await expectLater(
      api.get('/slow'),
      throwsA(
        isA<ApiException>()
            .having((error) => error.kind, 'kind', AppFailureKind.timeout)
            .having((error) => error.retryable, 'retryable', isTrue),
      ),
    );
  });
}
