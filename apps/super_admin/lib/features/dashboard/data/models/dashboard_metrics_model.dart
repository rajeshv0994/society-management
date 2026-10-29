import '../../domain/entities/dashboard_metrics.dart';

class DashboardMetricsModel extends DashboardMetrics {
  DashboardMetricsModel(super.values);
  factory DashboardMetricsModel.fromJson(Map<String, dynamic> json) =>
      DashboardMetricsModel(Map.unmodifiable(json));
}
