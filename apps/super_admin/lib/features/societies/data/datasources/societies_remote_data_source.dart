import 'package:society_ui/society_ui.dart';

class SocietiesRemoteDataSource {
  const SocietiesRemoteDataSource(this.api);
  final ApiClient api;
  Future<List<Map<String, dynamic>>> societies({
    String? status,
    String? search,
  }) async {
    final query = Uri(
      queryParameters: {
        'status': ?status,
        if (search?.isNotEmpty == true) 'search': search,
      },
    ).query;
    return (await api.get(
              '/api/admin/platform/societies${query.isEmpty ? '' : '?$query'}',
            )
            as List)
        .map((row) => Map<String, dynamic>.from(row as Map))
        .toList();
  }

  Future<Map<String, dynamic>> society(String id) async =>
      Map<String, dynamic>.from(
        await api.get('/api/admin/platform/societies/$id'),
      );
  Future<List<Map<String, dynamic>>> services() async =>
      (await api.get('/api/admin/platform/services') as List)
          .map((row) => Map<String, dynamic>.from(row as Map))
          .toList();
  Future<Map<String, dynamic>> create(Map<String, dynamic> body) async =>
      Map<String, dynamic>.from(
        await api.post('/api/admin/platform/societies', body),
      );
  Future<void> update(String id, Map<String, dynamic> body) =>
      api.put('/api/admin/platform/societies/$id', body);
  Future<void> uploadLogo(String id, List<int> bytes, String filename) =>
      api.upload(
        '/api/admin/platform/societies/$id/logo',
        bytes: bytes,
        filename: filename,
      );
  Future<Map<String, dynamic>> approve(String id) async =>
      Map<String, dynamic>.from(
        await api.post('/api/admin/societies/$id/approve'),
      );
  Future<void> reject(String id, String reason) =>
      api.post('/api/admin/societies/$id/reject', {'reason': reason});
  Future<void> verifyDocument(
    String societyId,
    String documentId,
    String status,
  ) => api.patch('/api/admin/societies/$societyId/documents/$documentId', {
    'status': status,
  });
  String absoluteUrl(String path) => '${api.baseUrl}$path';
}
