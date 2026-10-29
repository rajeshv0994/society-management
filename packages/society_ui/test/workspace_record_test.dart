import 'package:flutter_test/flutter_test.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  test('workspace records deeply freeze variable backend payloads', () {
    final record = WorkspaceRecord.from({
      'name': 'Example',
      'nested': {'status': 'READY'},
      'items': [
        {'id': 'one'},
      ],
    });

    expect(record['name'], 'Example');
    expect(record['nested'], isA<WorkspaceRecord>());
    expect((record['items'] as List).single, isA<WorkspaceRecord>());
    expect(() => record['name'] = 'Changed', throwsUnsupportedError);
    expect(
      () => (record['nested'] as WorkspaceRecord)['status'] = 'CHANGED',
      throwsUnsupportedError,
    );
  });
}
