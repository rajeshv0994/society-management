import 'dart:io';

import 'package:flutter_test/flutter_test.dart';

void main() {
  test('frontend presentation owns no transport or FutureBuilder state', () {
    final source = File('lib/main.dart').readAsStringSync();
    expect(source, isNot(contains('FutureBuilder')));
    expect(source, isNot(contains('ApiClient')));
    expect(source, isNot(contains("'/api/")));
    expect(source, isNot(contains('"/api/')));
    expect(source, isNot(contains('.get(')));
    expect(source, isNot(contains('.post(')));
  });

  test('frontend has clean public feature layers', () {
    for (final layer in ['domain', 'data', 'presentation']) {
      expect(
        Directory('lib/features/public_portal/$layer').existsSync(),
        isTrue,
      );
    }
    final cubits = File(
      'lib/features/public_portal/presentation/bloc/public_cubits.dart',
    ).readAsStringSync();
    expect(cubits, contains('extends Cubit'));
    expect(cubits, isNot(contains('/api/')));
    expect(cubits, isNot(contains('ApiClient')));
  });
}
