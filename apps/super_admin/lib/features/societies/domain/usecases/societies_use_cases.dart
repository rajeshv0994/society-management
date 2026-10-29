import '../entities/society_summary.dart';
import '../repositories/societies_repository.dart';

class SocietiesUseCases {
  const SocietiesUseCases(this.repository);
  final SocietiesRepository repository;
  Future<List<SocietySummary>> load({String? status, String? search}) =>
      repository.loadSocieties(status: status, search: search);
  Future<Map<String, dynamic>> approve(String id) => repository.approve(id);
  Future<void> reject(String id, String reason) =>
      repository.reject(id, reason);
  Future<void> verifyDocument(
    String societyId,
    String documentId,
    String status,
  ) => repository.verifyDocument(societyId, documentId, status);
  Future<SocietyCreateWorkspace> loadCreateWorkspace() =>
      repository.loadCreateWorkspace();
  Future<SocietyDetailWorkspace> loadDetail(String id) =>
      repository.loadDetail(id);
  Future<SocietyCreateResult> create(CreateSocietyCommand command) =>
      repository.create(command);
  Future<void> update(UpdateSocietyCommand command) =>
      repository.update(command);
  Future<void> uploadLogo(UploadSocietyLogoCommand command) =>
      repository.uploadLogo(command);
}
