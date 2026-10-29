import '../../../../core/immutable_fields.dart';

class DashboardMetrics {
  DashboardMetrics(Map<String, dynamic> values)
    : values = ImmutableFields.from(values);
  final ImmutableFields values;
}
