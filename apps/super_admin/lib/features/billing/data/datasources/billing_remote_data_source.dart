import 'package:society_ui/society_ui.dart';

class BillingRemoteDataSource {
  const BillingRemoteDataSource(this.api);
  final ApiClient api;
  Future<Map<String, dynamic>> load() async {
    final values = await Future.wait([
      api.get('/api/admin/platform/orders'),
      api.get('/api/admin/platform/societies'),
      api.get('/api/admin/platform/payment-gateway'),
    ]);
    return {
      'orders': (values[0] as List)
          .map((row) => Map<String, dynamic>.from(row as Map))
          .toList(),
      'societies': (values[1] as List)
          .map((row) => Map<String, dynamic>.from(row as Map))
          .toList(),
      'gateway': Map<String, dynamic>.from(values[2] as Map),
    };
  }

  Future<void> createOrder(Map<String, dynamic> body) =>
      api.post('/api/admin/platform/orders', body);
  Future<List<int>> invoice(String id) =>
      api.getBytes('/api/admin/platform/orders/$id/invoice');
  Future<void> reconcile(String id) =>
      api.post('/api/admin/platform/orders/$id/reconcile', const {});
  Future<void> cancel(String id, String reason) =>
      api.post('/api/admin/platform/orders/$id/cancel', {'reason': reason});
}
