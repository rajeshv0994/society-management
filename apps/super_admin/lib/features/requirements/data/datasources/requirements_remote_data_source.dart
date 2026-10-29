import 'package:society_ui/society_ui.dart';

class RequirementsRemoteDataSource {
  const RequirementsRemoteDataSource(this.api);
  final ApiClient api;
  Future<List<Map<String, dynamic>>> records(String endpoint) async =>
      (await api.get(endpoint) as List)
          .map((row) => Map<String, dynamic>.from(row as Map))
          .toList();
  Future<List<Map<String, dynamic>>> societies() async =>
      records('/api/admin/platform/societies');
  Future<void> review(String id, String status, String notes) => api.patch(
    '/api/admin/platform/requirements/$id',
    {'status': status, 'adminNotes': notes},
  );
  Future<void> createPlan(Map<String, dynamic> body) =>
      api.post('/api/admin/platform/custom-plans', body);
  Future<void> updatePlan(String id, Map<String, dynamic> body) =>
      api.put('/api/admin/platform/custom-plans/$id', body);
  Future<void> cancelPlan(String id, String reason) => api.post(
    '/api/admin/platform/custom-plans/$id/cancel',
    {'reason': reason},
  );
}
