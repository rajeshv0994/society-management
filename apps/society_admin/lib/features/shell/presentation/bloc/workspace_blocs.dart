import '../../../../core/presentation/bloc/admin_resource_bloc.dart';

abstract class WorkspaceBloc extends AdminResourceBloc {
  WorkspaceBloc(super.useCases);
}

class PlatformMembershipBloc extends WorkspaceBloc {
  PlatformMembershipBloc(super.useCases);
}

class HouseholdsBloc extends WorkspaceBloc {
  HouseholdsBloc(super.useCases);
}

class BillingBloc extends WorkspaceBloc {
  BillingBloc(super.useCases);
}

class ComplaintsBloc extends WorkspaceBloc {
  ComplaintsBloc(super.useCases);
}

class AmenitiesBloc extends WorkspaceBloc {
  AmenitiesBloc(super.useCases);
}

class ActivitiesBloc extends WorkspaceBloc {
  ActivitiesBloc(super.useCases);
}

class DeliveriesBloc extends WorkspaceBloc {
  DeliveriesBloc(super.useCases);
}

class EmergenciesBloc extends WorkspaceBloc {
  EmergenciesBloc(super.useCases);
}

class NoticesBloc extends WorkspaceBloc {
  NoticesBloc(super.useCases);
}

class TasksBloc extends WorkspaceBloc {
  TasksBloc(super.useCases);
}
