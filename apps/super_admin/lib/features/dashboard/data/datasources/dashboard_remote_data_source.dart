import 'package:society_ui/society_ui.dart';

class DashboardRemoteDataSource {
  const DashboardRemoteDataSource(this.api);
  final ApiClient api;
  Future<Map<String, dynamic>> load() async =>
      Map<String, dynamic>.from(await api.get('/api/admin/platform/dashboard'));
}
