// ignore_for_file: use_super_parameters
import '../../../../core/domain/usecases/admin_resource_use_cases.dart';
import '../../../../core/domain/entities/admin_resource.dart';
import '../../../../core/presentation/bloc/admin_resource_bloc.dart';

class DashboardBloc extends AdminResourceBloc {
  DashboardBloc(AdminResourceUseCases useCases) : super(useCases);

  void loadDashboard() => load(
    'dashboard',
    const ResourceRequest(AdminTargets.dashboard, ResourceKind.document),
  );
}
