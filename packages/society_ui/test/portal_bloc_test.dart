import 'package:flutter_test/flutter_test.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  test('portal start restores session and typed society options', () async {
    final sessions = _FakeSessions();
    final bloc = PortalAuthBloc(
      role: PortalRole.member,
      sessions: sessions,
      repository: PortalRepository(_PublicGateway()),
    );
    addTearDown(bloc.close);

    bloc.add(const PortalAuthStarted());
    final state = await bloc.stream.firstWhere(
      (state) => state.status != PortalAuthStatus.restoring,
    );

    expect(state.status, PortalAuthStatus.signedOut);
    expect(state.societies.single.code, 'GREEN');
    expect(sessions.restoreCalls, 1);
  });

  test('portal login validation is represented in BLoC state', () async {
    final sessions = _FakeSessions();
    final bloc = PortalAuthBloc(
      role: PortalRole.staff,
      sessions: sessions,
      repository: PortalRepository(_PublicGateway()),
    );
    addTearDown(bloc.close);

    bloc.add(
      const PortalLoginSubmitted(email: 'staff@example.test', password: 'x'),
    );
    final state = await bloc.stream.firstWhere(
      (state) => state.status == PortalAuthStatus.signedOut,
    );

    expect(state.error, contains('society'));
    expect(sessions.loginCalls, 0);
  });
}

class _FakeSessions extends SessionManager {
  _FakeSessions()
    : super(storageKey: 'TEST', expectedRole: 'STAFF', baseUrl: 'http://test');

  SessionData? value;
  int restoreCalls = 0;
  int loginCalls = 0;

  @override
  SessionData? get current => value;

  @override
  bool get isAuthenticated => value != null;

  @override
  Future<SessionData?> restore() async {
    restoreCalls++;
    return value;
  }

  @override
  Future<SessionData> login(
    String email,
    String password, {
    String? societyCode,
  }) async {
    loginCalls++;
    value = const SessionData(
      accessToken: 'access',
      refreshToken: 'refresh',
      expiresIn: 3600,
      userId: 'staff-1',
      fullName: 'Staff User',
      role: 'STAFF',
    );
    notifyListeners();
    return value!;
  }

  @override
  Future<void> logout() async {
    value = null;
    notifyListeners();
  }
}

class _PublicGateway implements ApiGateway {
  @override
  String get baseUrl => 'https://example.test';

  @override
  Future<Object?> get(String path) async => [
    {'society_code': 'GREEN', 'name': 'Green Valley'},
  ];

  @override
  Future<Object?> post(String path, [Object? body]) async => null;
  @override
  Future<Object?> put(String path, [Object? body]) async => null;
  @override
  Future<Object?> patch(String path, [Object? body]) async => null;
  @override
  Future<Object?> delete(String path) async => null;
  @override
  Future<List<int>> getBytes(String path) async => const [];
  @override
  Future<Object?> upload(
    String path, {
    required List<int> bytes,
    required String filename,
    String field = 'file',
  }) async => null;
}
