import 'package:society_ui/society_ui.dart';

class SupportRemoteDataSource {
  const SupportRemoteDataSource(this.api);
  final ApiClient api;
  Future<List<Map<String, dynamic>>> records(String endpoint) async =>
      (await api.get(endpoint) as List)
          .map((row) => Map<String, dynamic>.from(row as Map))
          .toList();
  Future<void> sendMessage(String id, String message) =>
      api.post('/api/support/$id/messages', {'message': message});
  Future<void> resolveTicket(String id, String resolution) => api.patch(
    '/api/admin/platform/support/$id',
    {'status': 'RESOLVED', 'resolution': resolution},
  );
}
