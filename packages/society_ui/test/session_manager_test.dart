import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  setUp(() {
    SharedPreferences.setMockInitialValues({});
  });

  test('login validates role and persists the complete session', () async {
    final client = MockClient((request) async {
      expect(request.url.path, '/api/auth/login');
      return http.Response(
        jsonEncode(_session(access: 'access-1', refresh: 'refresh-1')),
        200,
      );
    });
    final manager = SessionManager(
      storageKey: 'MEMBER_TEST',
      expectedRole: 'RESIDENT',
      baseUrl: 'https://api.example',
      httpClient: client,
    );

    final session = await manager.login('member@example.com', 'secret');

    expect(session.accessToken, 'access-1');
    expect(manager.isAuthenticated, isTrue);
    final preferences = await SharedPreferences.getInstance();
    expect(preferences.getString('MEMBER_TEST_session'), isNotNull);
    manager.dispose();
  });

  test('concurrent refresh calls share one rotating request', () async {
    var refreshRequests = 0;
    final client = MockClient((request) async {
      if (request.url.path == '/api/auth/login') {
        return http.Response(
          jsonEncode(_session(access: 'access-1', refresh: 'refresh-1')),
          200,
        );
      }
      refreshRequests++;
      await Future<void>.delayed(const Duration(milliseconds: 10));
      return http.Response(
        jsonEncode(_session(access: 'access-2', refresh: 'refresh-2')),
        200,
      );
    });
    final manager = SessionManager(
      storageKey: 'STAFF_TEST',
      expectedRole: 'RESIDENT',
      baseUrl: 'https://api.example',
      httpClient: client,
    );
    await manager.login('member@example.com', 'secret');

    final results = await Future.wait([manager.refresh(), manager.refresh()]);

    expect(refreshRequests, 1);
    expect(
      results.every((session) => session.accessToken == 'access-2'),
      isTrue,
    );
    manager.dispose();
  });

  test('role mismatch is rejected and not persisted', () async {
    var revoked = false;
    final client = MockClient((request) async {
      if (request.url.path == '/api/auth/logout') {
        revoked = true;
        return http.Response('', 204);
      }
      return http.Response(
        jsonEncode(
          _session(access: 'access', refresh: 'refresh', role: 'SOCIETY_ADMIN'),
        ),
        200,
      );
    });
    final manager = SessionManager(
      storageKey: 'MEMBER_TEST',
      expectedRole: 'RESIDENT',
      baseUrl: 'https://api.example',
      httpClient: client,
    );

    await expectLater(
      manager.login('admin@example.com', 'secret'),
      throwsA(isA<SessionException>()),
    );
    expect(manager.isAuthenticated, isFalse);
    expect(revoked, isTrue);
    manager.dispose();
  });

  test('service outage during login has a safe actionable message', () async {
    final client = MockClient((request) async {
      throw http.ClientException('Failed host lookup: internal-api-host');
    });
    final manager = SessionManager(
      storageKey: 'MEMBER_TEST',
      expectedRole: 'RESIDENT',
      baseUrl: 'https://api.example',
      httpClient: client,
    );

    await expectLater(
      manager.login('member@example.com', 'secret'),
      throwsA(
        isA<SessionException>()
            .having((error) => error.retryable, 'retryable', isTrue)
            .having(
              (error) => error.message,
              'message',
              contains('Unable to reach'),
            )
            .having(
              (error) => error.message,
              'technical details',
              isNot(contains('internal-api-host')),
            ),
      ),
    );
    manager.dispose();
  });

  test('inactive society response is mapped consistently at login', () async {
    final client = MockClient(
      (request) async => http.Response(
        jsonEncode({'message': 'Society account is not active'}),
        401,
      ),
    );
    final manager = SessionManager(
      storageKey: 'MEMBER_TEST',
      expectedRole: 'RESIDENT',
      baseUrl: 'https://api.example',
      httpClient: client,
    );

    await expectLater(
      manager.login('member@example.com', 'secret', societyCode: 'GREEN'),
      throwsA(
        isA<SessionException>()
            .having(
              (error) => error.kind,
              'kind',
              AppFailureKind.societyInactive,
            )
            .having(
              (error) => error.message,
              'message',
              contains('not active yet'),
            ),
      ),
    );
    manager.dispose();
  });
}

Map<String, dynamic> _session({
  required String access,
  required String refresh,
  String role = 'RESIDENT',
}) => {
  'accessToken': access,
  'token': access,
  'refreshToken': refresh,
  'expiresIn': 900,
  'userId': '11111111-1111-1111-1111-111111111111',
  'fullName': 'Test User',
  'role': role,
  'societyId': '22222222-2222-2222-2222-222222222222',
};
