import '../entities/management_record.dart';
import '../repositories/management_repository.dart';

class ManagementUseCases {
  const ManagementUseCases(this.repository);
  final ManagementRepository repository;
  Future<List<ManagementRecord>> load(ManagementResource resource) =>
      repository.loadRecords(resource);
  Future<void> updatePlan(UpdatePlanCommand command) =>
      repository.updatePlan(command);
  Future<void> retryNotification(String id) => repository.retryNotification(id);
  Future<void> resolveTicket(String id, String resolution) =>
      repository.resolveTicket(id, resolution);
}
