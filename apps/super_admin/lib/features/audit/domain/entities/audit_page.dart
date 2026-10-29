import '../../../../core/immutable_fields.dart';

class AuditEventRecord {
  AuditEventRecord(Map<String, dynamic> data)
    : data = ImmutableFields.from(data);
  final ImmutableFields data;
  String get action => data['action']?.toString() ?? 'UNKNOWN';
  String get entityType => data['entity_type']?.toString() ?? 'UNKNOWN';
  String? get entityId => data['entity_id']?.toString();
  String get actorName => data['actor_name']?.toString() ?? 'System';
  String? get societyName => data['society_name']?.toString();
  DateTime? get occurredAt =>
      DateTime.tryParse(data['occurred_at']?.toString() ?? '')?.toLocal();
  Object? get details => data['details'];
}

class AuditPage {
  const AuditPage({
    required this.items,
    required this.page,
    required this.size,
    required this.total,
    required this.totalPages,
  });
  final List<AuditEventRecord> items;
  final int page, size, total, totalPages;
}

class AuditQuery {
  const AuditQuery({
    this.page = 0,
    this.size = 50,
    this.search,
    this.action,
    this.entityType,
    this.from,
    this.to,
  });
  final int page, size;
  final String? search, action, entityType;
  final DateTime? from, to;
}
