import '../entities/service_catalog_item.dart';
import '../repositories/service_catalog_repository.dart';

class ServiceCatalogUseCases {
  const ServiceCatalogUseCases(this.repository);
  final ServiceCatalogRepository repository;
  Future<List<ServiceCatalogItem>> load() => repository.loadCatalog();
  Future<void> save(SaveServiceCommand command) =>
      repository.saveService(command);
}
