import '../entities/admin_resource.dart';

abstract interface class AdminResourceRepository {
  Future<AdminResource> load(ResourceRequest request);
  Future<Object?> execute(ResourceCommand command);
  String absoluteUrl(String path);
}
