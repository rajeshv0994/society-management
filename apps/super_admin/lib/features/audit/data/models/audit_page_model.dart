import '../../domain/entities/audit_page.dart';

class AuditEventModel extends AuditEventRecord {
  AuditEventModel(super.data);
  factory AuditEventModel.fromJson(Map<String, dynamic> json) =>
      AuditEventModel(Map.unmodifiable(json));
}

class AuditPageModel extends AuditPage {
  const AuditPageModel({
    required super.items,
    required super.page,
    required super.size,
    required super.total,
    required super.totalPages,
  });
  factory AuditPageModel.fromJson(Map<String, dynamic> json) => AuditPageModel(
    items: (json['items'] as List? ?? const [])
        .map(
          (item) =>
              AuditEventModel.fromJson(Map<String, dynamic>.from(item as Map)),
        )
        .toList(growable: false),
    page: (json['page'] as num?)?.toInt() ?? 0,
    size: (json['size'] as num?)?.toInt() ?? 50,
    total: (json['total'] as num?)?.toInt() ?? 0,
    totalPages: (json['total_pages'] as num?)?.toInt() ?? 0,
  );
  factory AuditPageModel.fromResponse(Object? response) {
    if (response is Map) {
      return AuditPageModel.fromJson(Map<String, dynamic>.from(response));
    }
    if (response is List) {
      final items = response
          .map(
            (item) => AuditEventModel.fromJson(
              Map<String, dynamic>.from(item as Map),
            ),
          )
          .toList(growable: false);
      return AuditPageModel(
        items: items,
        page: 0,
        size: items.length,
        total: items.length,
        totalPages: items.isEmpty ? 0 : 1,
      );
    }
    throw const FormatException('Invalid audit response');
  }
}
