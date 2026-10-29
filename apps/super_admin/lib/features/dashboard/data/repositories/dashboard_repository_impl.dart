import '../datasources/dashboard_remote_data_source.dart';
import '../../domain/entities/dashboard_metrics.dart';
import '../../domain/repositories/dashboard_repository.dart';
import '../models/dashboard_metrics_model.dart';

class DashboardRepositoryImpl implements DashboardRepository {
  const DashboardRepositoryImpl(this.remote);
  final DashboardRemoteDataSource remote;
  @override
  Future<DashboardMetrics> loadDashboard() async =>
      DashboardMetricsModel.fromJson(await remote.load());
}
