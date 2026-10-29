import '../entities/admin_resource.dart';
import '../repositories/admin_resource_repository.dart';

class AdminResourceUseCases {
  const AdminResourceUseCases(this.repository);
  final AdminResourceRepository repository;
  Future<AdminResource> load(ResourceRequest request) =>
      repository.load(request);
  Future<Object?> execute(ResourceCommand command) =>
      repository.execute(command);
  String absoluteUrl(String path) => repository.absoluteUrl(path);
}
