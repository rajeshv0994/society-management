import '../entities/audit_page.dart';

abstract interface class AuditRepository {
  Future<AuditPage> loadAudit(AuditQuery query);
}
