import '../entities/dashboard_metrics.dart';
import '../repositories/dashboard_repository.dart';

class DashboardUseCases {
  const DashboardUseCases(this.repository);
  final DashboardRepository repository;
  Future<DashboardMetrics> load() => repository.loadDashboard();
}
