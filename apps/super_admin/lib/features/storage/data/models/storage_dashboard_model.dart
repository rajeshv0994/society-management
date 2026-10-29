import '../../domain/entities/storage_dashboard.dart';

class StorageDashboardModel extends StorageDashboard {
  StorageDashboardModel.fromJson(Map<String, dynamic> json)
    : super(
        policy: StoragePolicy(
          Map.unmodifiable(
            Map<String, dynamic>.from(json['policy'] as Map? ?? const {}),
          ),
        ),
        societies: (json['societies'] as List? ?? const [])
            .map(
              (item) => SocietyStorageUsage(
                Map.unmodifiable(Map<String, dynamic>.from(item as Map)),
              ),
            )
            .toList(growable: false),
      );
}
