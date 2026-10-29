import '../entities/audit_page.dart';
import '../repositories/audit_repository.dart';

class AuditUseCases {
  const AuditUseCases(this.repository);
  final AuditRepository repository;
  Future<AuditPage> load(AuditQuery query) => repository.loadAudit(query);
}
