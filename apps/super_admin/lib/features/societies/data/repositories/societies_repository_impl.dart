import '../datasources/societies_remote_data_source.dart';
import '../../../services/data/models/service_catalog_item_model.dart';
import '../../domain/entities/society_summary.dart';
import '../../domain/repositories/societies_repository.dart';
import '../models/society_models.dart';

class SocietiesRepositoryImpl implements SocietiesRepository {
  const SocietiesRepositoryImpl(this.remote);
  final SocietiesRemoteDataSource remote;
  Future<List<ServiceCatalogItemModel>> _services() async =>
      (await remote.services())
          .map(ServiceCatalogItemModel.fromJson)
          .toList(growable: false);
  @override
  Future<List<SocietySummary>> loadSocieties({
    String? status,
    String? search,
  }) async => (await remote.societies(
    status: status,
    search: search,
  )).map(SocietySummaryModel.fromJson).toList(growable: false);
  @override
  Future<Map<String, dynamic>> approve(String id) => remote.approve(id);
  @override
  Future<void> reject(String id, String reason) => remote.reject(id, reason);
  @override
  Future<void> verifyDocument(
    String societyId,
    String documentId,
    String status,
  ) => remote.verifyDocument(societyId, documentId, status);
  @override
  Future<SocietyCreateWorkspace> loadCreateWorkspace() async =>
      SocietyCreateWorkspace(await _services());
  @override
  Future<SocietyDetailWorkspace> loadDetail(String id) async {
    final values = await Future.wait([remote.society(id), _services()]);
    return SocietyDetailWorkspace(
      detail: SocietyDetailModel.fromJson(values[0] as Map<String, dynamic>),
      services: values[1] as List<ServiceCatalogItemModel>,
      baseUrl: remote.absoluteUrl(''),
    );
  }

  @override
  Future<SocietyCreateResult> create(CreateSocietyCommand command) async =>
      SocietyCreateResultModel.fromJson(await remote.create(command.values));
  @override
  Future<void> update(UpdateSocietyCommand command) =>
      remote.update(command.id, command.values);
  @override
  Future<void> uploadLogo(UploadSocietyLogoCommand command) =>
      remote.uploadLogo(command.id, command.bytes, command.filename);
}
