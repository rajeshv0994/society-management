import '../datasources/management_remote_data_source.dart';
import '../../domain/entities/management_record.dart';
import '../../domain/repositories/management_repository.dart';
import '../models/management_record_model.dart';

class ManagementRepositoryImpl implements ManagementRepository {
  const ManagementRepositoryImpl(this.remote);
  final ManagementRemoteDataSource remote;
  @override
  Future<List<ManagementRecord>> loadRecords(
    ManagementResource resource,
  ) async => (await remote.records(
    resource,
  )).map(ManagementRecordModel.fromJson).toList(growable: false);
  @override
  Future<void> updatePlan(UpdatePlanCommand command) =>
      remote.updatePlan(command);
  @override
  Future<void> retryNotification(String id) => remote.retryNotification(id);
  @override
  Future<void> resolveTicket(String id, String resolution) =>
      remote.resolveTicket(id, resolution);
}
