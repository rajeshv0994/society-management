import '../entities/storage_dashboard.dart';
import '../repositories/storage_repository.dart';

class StorageUseCases {
  const StorageUseCases(this.repository);
  final StorageRepository repository;
  Future<StorageDashboard> load() => repository.loadStorage();
  Future<void> updatePolicy(UpdateStoragePolicyCommand command) =>
      repository.updatePolicy(command);
}
