import 'package:flutter_test/flutter_test.dart';
import 'package:society_admin_app/core/data/models/admin_resource_model.dart';

void main() {
  test('document model preserves a JSON object as immutable data', () {
    final model = ResourceDocumentModel.fromJson({
      'id': 7,
      'name': 'Audit',
      'metadata': {
        'tags': ['sensitive'],
      },
    });

    expect(model.data['id'], 7);
    expect(model.data['name'], 'Audit');
    expect(() => model.data['id'] = 8, throwsUnsupportedError);
    final metadata = model.data['metadata'] as Map<String, dynamic>;
    expect(() => metadata['owner'] = 'admin', throwsUnsupportedError);
    expect(
      () => (metadata['tags'] as List<Object?>).add('changed'),
      throwsUnsupportedError,
    );
  });

  test('collection model converts a JSON array into immutable rows', () {
    final model = ResourceCollectionModel.fromJson([
      {'id': 1, 'action': 'UPDATE'},
      {'id': 2, 'action': 'DELETE'},
    ]);

    expect(model.items, hasLength(2));
    expect(model.items.first['action'], 'UPDATE');
    expect(() => model.items.first['id'] = 3, throwsUnsupportedError);
  });

  test('collection model accepts paged API response wrappers', () {
    final model = ResourceCollectionModel.fromResponse({
      'items': [
        {'id': 1},
      ],
      'total': 1,
    });

    expect(model.items.single['id'], 1);
  });

  test('legacy array can be normalized for a document page', () {
    final model = ResourceDocumentResponseModel.fromResponse([
      {'id': 1, 'sensitive': true},
    ]);

    expect(model.data['items'], hasLength(1));
    expect(model.data['total'], 1);
  });
}
