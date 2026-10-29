import '../entities/service_catalog_item.dart';

abstract interface class ServiceCatalogRepository {
  Future<List<ServiceCatalogItem>> loadCatalog();
  Future<void> saveService(SaveServiceCommand command);
}
