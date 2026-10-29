import '../datasources/audit_remote_data_source.dart';
import '../../domain/entities/audit_page.dart';
import '../../domain/repositories/audit_repository.dart';
import '../models/audit_page_model.dart';

class AuditRepositoryImpl implements AuditRepository {
  const AuditRepositoryImpl(this.remote);
  final AuditRemoteDataSource remote;
  @override
  Future<AuditPage> loadAudit(AuditQuery query) async =>
      AuditPageModel.fromResponse(await remote.load(query));
}
