import 'dart:io';
import 'package:flutter_test/flutter_test.dart';

Iterable<File> dartFiles(String path) => Directory(path)
    .listSync(recursive: true)
    .whereType<File>()
    .where((file) => file.path.endsWith('.dart'));

void main() {
  test('legacy layer-first folders and service are absent', () {
    expect(Directory('lib/presentation').existsSync(), isFalse);
    expect(Directory('lib/data').existsSync(), isFalse);
    expect(
      File('lib/data/services/society_admin_service.dart').existsSync(),
      isFalse,
    );
  });

  test('feature pages do not own remote state or infrastructure', () {
    for (final file in dartFiles('lib/features')) {
      if (!file.path.contains('/presentation/pages/')) continue;
      final source = file.readAsStringSync();
      expect(source, isNot(contains('FutureBuilder')), reason: file.path);
      expect(source, isNot(contains('ApiClient(')), reason: file.path);
      expect(source, isNot(contains('HttpApiClient(')), reason: file.path);
      expect(source, isNot(contains('/data/')), reason: file.path);
      expect(source, isNot(contains("'/api/")), reason: file.path);
      expect(source, isNot(contains('"/api/')), reason: file.path);
      expect(source, isNot(contains('ResourceRequest(')), reason: file.path);
      expect(source, isNot(contains('ResourceCommand(')), reason: file.path);
      expect(source, isNot(contains('.queryDocument(')), reason: file.path);
      expect(source, isNot(contains('.queryCollection(')), reason: file.path);
      expect(source, isNot(contains('.execute(')), reason: file.path);
    }
  });

  test('feature BLoCs enter remote operations through use cases', () {
    for (final file in dartFiles('lib/features')) {
      if (!file.path.contains('/presentation/bloc/')) continue;
      final source = file.readAsStringSync();
      expect(source, isNot(contains('/data/')), reason: file.path);
      expect(source, isNot(contains('ApiClient')), reason: file.path);
      expect(source, isNot(contains('Completer<')), reason: file.path);
      expect(source, isNot(contains("'/api/")), reason: file.path);
      expect(source, isNot(contains('"/api/')), reason: file.path);
    }
  });

  test('core BLoC events use state transitions instead of completers', () {
    final source = File(
      'lib/core/presentation/bloc/admin_resource_bloc.dart',
    ).readAsStringSync();
    expect(source, isNot(contains('Completer<')));
    expect(source, contains('ResourceOperationStatus'));
    expect(source, contains('operations'));
  });

  test('domain layer is independent from Flutter and data layer', () {
    for (final file in dartFiles('lib')) {
      if (!file.path.contains('/domain/')) continue;
      final source = file.readAsStringSync();
      expect(source, isNot(contains('package:flutter')), reason: file.path);
      expect(source, isNot(contains('/data/')), reason: file.path);
    }
  });

  test('presentation never receives the concrete API client', () {
    for (final file in dartFiles('lib')) {
      if (!file.path.contains('/presentation/')) continue;
      final source = file.readAsStringSync();
      expect(source, isNot(contains('final ApiClient')), reason: file.path);
      expect(source, isNot(contains('HttpApiClient')), reason: file.path);
      expect(source, isNot(contains('dependencies.client')), reason: file.path);
    }
  });

  test('application dependencies expose use cases, not infrastructure', () {
    final source = File('lib/app/dependency_injection.dart').readAsStringSync();
    final societyDependencies = source.substring(
      source.indexOf('class SocietyAdminDependencies'),
    );
    expect(societyDependencies, isNot(contains('final ApiClient')));
  });

  test('shared workspaces require a BLoC-managed API port', () {
    for (final file in dartFiles('../../packages/society_ui/lib/src')) {
      if (!file.path.endsWith('_workspace.dart')) continue;
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
    }
  });
}
