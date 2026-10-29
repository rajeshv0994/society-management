import 'dart:io';

import 'package:flutter_test/flutter_test.dart';

void main() {
  test(
    'feature pages do not orchestrate remote data source or ApiClient calls',
    () {
      final views = Directory('lib/features')
          .listSync(recursive: true)
          .whereType<File>()
          .where(
            (file) =>
                file.path.contains('/presentation/pages/') &&
                file.path.endsWith('.dart'),
          );

      for (final file in views) {
        final name = file.uri.pathSegments.last;
        final source = file.readAsStringSync();
        expect(
          source.contains('/core/data/'),
          isFalse,
          reason: '$name must not import the data layer',
        );
        expect(
          RegExp(
            r'\.api\.(get|post|put|patch|delete|upload)\(',
          ).hasMatch(source),
          isFalse,
          reason: '$name must not call ApiClient directly',
        );
        expect(source, isNot(contains("'/api/")), reason: name);
        expect(source, isNot(contains('"/api/')), reason: name);
        expect(source, isNot(contains('ApiClient')), reason: name);
        expect(source, isNot(contains('HttpApiClient')), reason: name);
      }
    },
  );

  test('feature views do not use FutureBuilder for remote state', () {
    final views = Directory('lib/features')
        .listSync(recursive: true)
        .whereType<File>()
        .where(
          (file) =>
              file.path.contains('/presentation/pages/') &&
              file.path.endsWith('.dart'),
        );

    for (final file in views) {
      final source = file.readAsStringSync();
      expect(
        source.contains('FutureBuilder<'),
        isFalse,
        reason:
            '${file.uri.pathSegments.last} must render remote state from BlocBuilder/BlocConsumer',
      );
    }
  });

  test('presentation BLoCs depend on use cases, not data or repositories', () {
    final blocs = Directory('lib/features')
        .listSync(recursive: true)
        .whereType<File>()
        .where(
          (file) =>
              file.path.contains('/presentation/bloc/') &&
              file.path.endsWith('.dart'),
        );
    for (final file in blocs) {
      final source = file.readAsStringSync();
      expect(
        source.contains('/data/'),
        isFalse,
        reason: '${file.path} bypasses the domain boundary',
      );
      expect(
        source.contains('/repositories/'),
        isFalse,
        reason: '${file.path} must invoke feature use cases',
      );
    }
  });

  test('remote features contain every Clean Architecture layer', () {
    const features = [
      'audit',
      'authentication',
      'billing',
      'dashboard',
      'management',
      'requirements',
      'services',
      'societies',
      'storage',
      'support',
    ];
    const folders = [
      'data/datasources',
      'data/models',
      'data/repositories',
      'domain/entities',
      'domain/repositories',
      'domain/usecases',
      'presentation/bloc',
      'presentation/pages',
    ];
    for (final feature in features) {
      for (final folder in folders) {
        expect(
          Directory('lib/features/$feature/$folder').existsSync(),
          isTrue,
          reason: '$feature is missing $folder',
        );
      }
    }
  });

  test('domain layer is independent from Flutter and data layer', () {
    final files = Directory('lib/features')
        .listSync(recursive: true)
        .whereType<File>()
        .where(
          (file) =>
              file.path.contains('/domain/') && file.path.endsWith('.dart'),
        );
    for (final file in files) {
      final source = file.readAsStringSync();
      expect(
        source.contains('package:flutter'),
        isFalse,
        reason: '${file.path} makes domain depend on Flutter',
      );
      expect(
        source.contains('/data/'),
        isFalse,
        reason: '${file.path} makes domain depend on data',
      );
    }
  });

  test('legacy aggregate platform models and repositories stay removed', () {
    expect(
      File('lib/core/domain/entities/platform_models.dart').existsSync(),
      isFalse,
    );
    expect(
      File(
        'lib/core/domain/repositories/platform_repository.dart',
      ).existsSync(),
      isFalse,
    );
    expect(
      File(
        'lib/core/data/datasources/platform_remote_data_source.dart',
      ).existsSync(),
      isFalse,
    );
    expect(Directory('lib/data').existsSync(), isFalse);
    expect(Directory('lib/presentation').existsSync(), isFalse);
    expect(Directory('lib/core/data').existsSync(), isFalse);
    expect(Directory('lib/core/domain').existsSync(), isFalse);
  });
}
