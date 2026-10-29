import '../entities/management_record.dart';

abstract interface class ManagementRepository {
  Future<List<ManagementRecord>> loadRecords(ManagementResource resource);
  Future<void> updatePlan(UpdatePlanCommand command);
  Future<void> retryNotification(String id);
  Future<void> resolveTicket(String id, String resolution);
}
