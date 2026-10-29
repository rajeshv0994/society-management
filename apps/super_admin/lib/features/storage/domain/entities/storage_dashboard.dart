import '../../../../core/immutable_fields.dart';

class StoragePolicy {
  StoragePolicy(Map<String, dynamic> data) : data = ImmutableFields.from(data);
  final ImmutableFields data;
}

class SocietyStorageUsage {
  SocietyStorageUsage(Map<String, dynamic> data)
    : data = ImmutableFields.from(data);
  final ImmutableFields data;
}

class StorageDashboard {
  const StorageDashboard({required this.policy, required this.societies});
  final StoragePolicy policy;
  final List<SocietyStorageUsage> societies;
}

class UpdateStoragePolicyCommand {
  UpdateStoragePolicyCommand(Map<String, dynamic> values)
    : values = ImmutableFields.from(values);
  final ImmutableFields values;
}
