import '../entities/storage_dashboard.dart';

abstract interface class StorageRepository {
  Future<StorageDashboard> loadStorage();
  Future<void> updatePolicy(UpdateStoragePolicyCommand command);
}
