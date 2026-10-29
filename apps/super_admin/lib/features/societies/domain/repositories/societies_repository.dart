import '../entities/society_summary.dart';

abstract interface class SocietiesRepository {
  Future<List<SocietySummary>> loadSocieties({String? status, String? search});
  Future<Map<String, dynamic>> approve(String id);
  Future<void> reject(String id, String reason);
  Future<void> verifyDocument(
    String societyId,
    String documentId,
    String status,
  );
  Future<SocietyCreateWorkspace> loadCreateWorkspace();
  Future<SocietyDetailWorkspace> loadDetail(String id);
  Future<SocietyCreateResult> create(CreateSocietyCommand command);
  Future<void> update(UpdateSocietyCommand command);
  Future<void> uploadLogo(UploadSocietyLogoCommand command);
}
