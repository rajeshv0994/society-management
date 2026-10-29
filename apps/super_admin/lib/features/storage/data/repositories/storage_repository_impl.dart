import '../datasources/storage_remote_data_source.dart';
import '../../domain/entities/storage_dashboard.dart';
import '../../domain/repositories/storage_repository.dart';
import '../models/storage_dashboard_model.dart';

class StorageRepositoryImpl implements StorageRepository {
  const StorageRepositoryImpl(this.remote);
  final StorageRemoteDataSource remote;
  @override
  Future<StorageDashboard> loadStorage() async =>
      StorageDashboardModel.fromJson(await remote.load());
  @override
  Future<void> updatePolicy(UpdateStoragePolicyCommand command) =>
      remote.updatePolicy(command.values);
}
