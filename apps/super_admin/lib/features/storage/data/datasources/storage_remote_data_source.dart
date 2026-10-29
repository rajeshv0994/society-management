import 'package:society_ui/society_ui.dart';

class StorageRemoteDataSource {
  const StorageRemoteDataSource(this.api);
  final ApiClient api;
  Future<Map<String, dynamic>> load() async =>
      Map<String, dynamic>.from(await api.get('/api/admin/platform/storage'));
  Future<void> updatePolicy(Map<String, dynamic> body) =>
      api.put('/api/admin/platform/storage/policy', body);
}
