import '../../domain/entities/admin_resource.dart';

class RawResourceModel extends RawResource {
  RawResourceModel(Object? value) : super(_freeze(value));
}

Object? _freeze(Object? value) => switch (value) {
  Map value => AdminRecord.from(value),
  List value => List.unmodifiable(value.map(_freeze)),
  _ => value,
};

class ResourceDocumentModel extends ResourceDocument {
  const ResourceDocumentModel(super.data);
  factory ResourceDocumentModel.fromJson(Map<String, dynamic> json) =>
      ResourceDocumentModel(AdminRecord.from(json));
}

class ResourceCollectionModel extends ResourceCollection {
  const ResourceCollectionModel(super.items);
  factory ResourceCollectionModel.fromJson(List<dynamic> json) =>
      ResourceCollectionModel(
        List.unmodifiable(json.map((item) => AdminRecord.from(item as Map))),
      );

  factory ResourceCollectionModel.fromResponse(Object? value) {
    if (value is List) return ResourceCollectionModel.fromJson(value);
    if (value is Map) {
      final map = Map<String, dynamic>.from(value);
      for (final key in const ['items', 'content', 'data', 'results']) {
        final rows = map[key];
        if (rows is List) return ResourceCollectionModel.fromJson(rows);
      }
    }
    throw const FormatException(
      'Expected a list response or an object containing items.',
    );
  }
}

extension ResourceDocumentResponseModel on ResourceDocumentModel {
  static ResourceDocumentModel fromResponse(Object? value) {
    if (value is Map) {
      return ResourceDocumentModel.fromJson(Map<String, dynamic>.from(value));
    }
    if (value is List) {
      return ResourceDocumentModel.fromJson({
        'items': value,
        'page': 0,
        'size': value.length,
        'total': value.length,
        'total_pages': value.isEmpty ? 0 : 1,
      });
    }
    throw const FormatException('Expected an object response.');
  }
}
