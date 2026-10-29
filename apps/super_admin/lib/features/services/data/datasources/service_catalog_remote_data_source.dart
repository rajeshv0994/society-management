import 'package:society_ui/society_ui.dart';

class ServiceCatalogRemoteDataSource {
  const ServiceCatalogRemoteDataSource(this.api);
  final ApiClient api;
  Future<List<Map<String, dynamic>>> load() async =>
      (await api.get('/api/admin/platform/services') as List)
          .map((row) => Map<String, dynamic>.from(row as Map))
          .toList();
  Future<void> create(Map<String, dynamic> body) =>
      api.post('/api/admin/platform/services', body);
  Future<void> update(String code, Map<String, dynamic> body) =>
      api.put('/api/admin/platform/services/$code', body);
}
