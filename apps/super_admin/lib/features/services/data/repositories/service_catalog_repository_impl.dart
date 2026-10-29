import '../datasources/service_catalog_remote_data_source.dart';
import '../../domain/entities/service_catalog_item.dart';
import '../../domain/repositories/service_catalog_repository.dart';
import '../models/service_catalog_item_model.dart';

class ServiceCatalogRepositoryImpl implements ServiceCatalogRepository {
  const ServiceCatalogRepositoryImpl(this.remote);
  final ServiceCatalogRemoteDataSource remote;
  @override
  Future<List<ServiceCatalogItem>> loadCatalog() async => (await remote.load())
      .map(ServiceCatalogItemModel.fromJson)
      .toList(growable: false);
  @override
  Future<void> saveService(SaveServiceCommand command) => command.code == null
      ? remote.create(command.values)
      : remote.update(command.code!, command.values);
}
