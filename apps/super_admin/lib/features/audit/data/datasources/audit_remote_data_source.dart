import 'package:society_ui/society_ui.dart';
import '../../domain/entities/audit_page.dart';

class AuditRemoteDataSource {
  const AuditRemoteDataSource(this.api);
  final ApiClient api;
  Future<Object?> load(AuditQuery value) {
    final query = Uri(
      queryParameters: {
        'page': '${value.page}',
        'size': '${value.size}',
        if (value.search?.trim().isNotEmpty == true)
          'search': value.search!.trim(),
        if (value.action?.trim().isNotEmpty == true)
          'action': value.action!.trim().toUpperCase(),
        if (value.entityType?.trim().isNotEmpty == true)
          'entityType': value.entityType!.trim().toUpperCase(),
        if (value.from != null) 'from': value.from!.toUtc().toIso8601String(),
        if (value.to != null) 'to': value.to!.toUtc().toIso8601String(),
      },
    ).query;
    return api.get('/api/admin/platform/audit?$query');
  }
}
