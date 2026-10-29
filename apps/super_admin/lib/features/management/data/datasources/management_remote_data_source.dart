import 'package:society_ui/society_ui.dart';
import '../../domain/entities/management_record.dart';

class ManagementRemoteDataSource {
  const ManagementRemoteDataSource(this.api);
  final ApiClient api;
  Future<List<Map<String, dynamic>>> records(
    ManagementResource resource,
  ) async =>
      (await api.get(switch (resource) {
                ManagementResource.plans => '/api/admin/platform/plans',
                ManagementResource.notifications =>
                  '/api/admin/platform/notifications',
                ManagementResource.orders => '/api/admin/platform/orders',
                ManagementResource.support => '/api/admin/platform/support',
              })
              as List)
          .map((row) => Map<String, dynamic>.from(row as Map))
          .toList();
  Future<void> updatePlan(UpdatePlanCommand command) =>
      api.put('/api/admin/platform/plans/${command.code}', {
        'approvalMode': command.approvalMode,
        'maxSpaces': command.maxSpaces,
        'pricePerSpace': command.pricePerSpace,
        'maxStaff': command.maxStaff,
        'pricePerStaff': command.pricePerStaff,
        'trialDurationValue': command.trialDurationValue,
        'trialDurationUnit': command.trialDurationUnit,
      });
  Future<void> retryNotification(String id) =>
      api.post('/api/admin/platform/notifications/$id/retry');
  Future<void> resolveTicket(String id, String resolution) => api.patch(
    '/api/admin/platform/support/$id',
    {'status': 'RESOLVED', 'resolution': resolution},
  );
}
