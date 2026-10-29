import 'dart:io';

import 'package:flutter_test/flutter_test.dart';

void main() {
  test('member presentation has no transport or remote FutureBuilder', () {
    for (final file
        in Directory('lib')
            .listSync(recursive: true)
            .whereType<File>()
            .where(
              (file) =>
                  file.path.endsWith('_page.dart') ||
                  file.path.contains('/presentation/'),
            )) {
      final source = file.readAsStringSync();
      expect(source, isNot(contains("'/api/")), reason: file.path);
      expect(source, isNot(contains('"/api/')), reason: file.path);
      expect(source, isNot(contains('ApiClient')), reason: file.path);
      expect(source, isNot(contains('HttpApiClient')), reason: file.path);
      expect(source, isNot(contains('FutureBuilder')), reason: file.path);
      expect(source, isNot(contains('widget.client')), reason: file.path);
    }
  });

  test('member domain is independent from Flutter and data', () {
    for (final file
        in Directory('lib/features')
            .listSync(recursive: true)
            .whereType<File>()
            .where((file) => file.path.contains('/domain/'))) {
      final source = file.readAsStringSync();
      expect(source, isNot(contains('package:flutter')), reason: file.path);
      expect(source, isNot(contains('/data/')), reason: file.path);
    }
  });

  test('member domain entities do not serialize transport JSON', () {
    for (final file
        in Directory('lib/features')
            .listSync(recursive: true)
            .whereType<File>()
            .where(
              (file) =>
                  file.path.contains('/domain/') && file.path.endsWith('.dart'),
            )) {
      final source = file.readAsStringSync();
      expect(source, isNot(contains('fromJson')), reason: file.path);
      expect(source, isNot(contains('toJson()')), reason: file.path);
    }
  });
}
