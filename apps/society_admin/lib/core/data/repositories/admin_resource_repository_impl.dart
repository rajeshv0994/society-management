import '../../domain/entities/admin_resource.dart';
import '../../domain/repositories/admin_resource_repository.dart';
import '../datasources/society_admin_remote_data_source.dart';
import '../models/admin_resource_model.dart';

class AdminResourceRepositoryImpl implements AdminResourceRepository {
  const AdminResourceRepositoryImpl(this.remote);
  final SocietyAdminRemoteDataSource remote;
  @override
  Future<AdminResource> load(ResourceRequest request) async {
    final value = await remote.load(request);
    return switch (request.kind) {
      ResourceKind.document => ResourceDocumentResponseModel.fromResponse(
        value,
      ),
      ResourceKind.collection => ResourceCollectionModel.fromResponse(value),
      ResourceKind.raw => RawResourceModel(value),
    };
  }

  @override
  Future<Object?> execute(ResourceCommand command) => remote.execute(command);
  @override
  String absoluteUrl(String path) => remote.absoluteUrl(path);
}
