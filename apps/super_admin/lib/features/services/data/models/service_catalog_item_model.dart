import '../../domain/entities/service_catalog_item.dart';

class ServiceCatalogItemModel extends ServiceCatalogItem {
  ServiceCatalogItemModel(super.data);
  factory ServiceCatalogItemModel.fromJson(Map<String, dynamic> json) =>
      ServiceCatalogItemModel(Map.unmodifiable(json));
}
