import 'dart:io';

import 'package:flutter_test/flutter_test.dart';

void main() {
  test(
    'portal presentation has no remote FutureBuilder or direct requests',
    () {
      final source = File('lib/src/portal_app.dart').readAsStringSync();
      expect(source, isNot(contains('FutureBuilder')));
      expect(source, isNot(contains("'/api/")));
      expect(source, isNot(contains('"/api/')));
      expect(source, isNot(contains('.get(')));
      expect(source, isNot(contains('.post(')));
    },
  );

  test('shared workspaces use the API port and BLoC-owned read state', () {
    for (final file in Directory('lib/src').listSync().whereType<File>().where(
      (file) => file.path.endsWith('_workspace.dart'),
    )) {
      final source = file.readAsStringSync();
      expect(source, isNot(contains('ApiClient')), reason: file.path);
      expect(source, isNot(contains('HttpApiClient')), reason: file.path);
      expect(source, isNot(contains('FutureBuilder')), reason: file.path);
      expect(
        source,
        isNot(contains('final ApiGateway client')),
        reason: file.path,
      );
      expect(source, contains('BlocManagedApiGateway'), reason: file.path);
      if (source.contains('Future<void> _load()')) {
        expect(source, contains('AsyncDataBloc'), reason: file.path);
      }
    }
  });
}
