import 'package:flutter_test/flutter_test.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  test(
    'society discovery maps transport JSON to typed immutable models',
    () async {
      final repository = PortalRepository(_PortalGateway());

      final societies = await repository.loadSocieties();

      expect(societies.single.code, 'GREEN');
      expect(societies.single.name, 'Green Valley');
      expect(
        () => societies.add(const SocietyOption(code: 'X', name: 'X')),
        throwsUnsupportedError,
      );
    },
  );

  test('portal access maps permissions and enabled services', () async {
    final repository = PortalRepository(_PortalGateway());

    final access = await repository.loadAccess(
      PortalRole.staff,
      _PortalGateway(),
    );

    expect(access.permissions, {'TASK_VIEW', 'VISITOR_VIEW'});
    expect(access.enabledServices, {'TASKS', 'VISITORS'});
  });
}

class _PortalGateway implements ApiGateway {
  @override
  String get baseUrl => 'https://example.test';

  @override
  Future<Object?> get(String path) async => switch (path) {
    '/api/public/societies' => [
      {'society_code': 'GREEN', 'name': 'Green Valley'},
    ],
    '/api/configuration/reference' => {
      'enabled_modules': ['TASKS', 'VISITORS'],
    },
    '/api/society/staff/me' => {
      'permissions': ['TASK_VIEW', 'VISITOR_VIEW'],
    },
    _ => <String, Object?>{},
  };

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
