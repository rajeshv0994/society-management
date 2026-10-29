// ignore_for_file: use_super_parameters
import '../../../../core/domain/usecases/admin_resource_use_cases.dart';
import '../../../../core/domain/entities/admin_resource.dart';
import '../../../../core/presentation/bloc/admin_resource_bloc.dart';

class StaffBloc extends AdminResourceBloc {
  StaffBloc(AdminResourceUseCases useCases) : super(useCases);

  void loadDirectory() {
    load(
      'staff',
      const ResourceRequest(AdminTargets.staff, ResourceKind.collection),
    );
    load(
      'staff-profile-options',
      const ResourceRequest(AdminTargets.profile, ResourceKind.document),
    );
    load(
      'staff-configuration-options',
      const ResourceRequest(AdminTargets.configuration, ResourceKind.document),
    );
  }

  String staffKey(Object id) => 'staff-$id';
  void loadStaff(Object id) => load(
    staffKey(id),
    ResourceRequest(AdminTargets.staffMember(id), ResourceKind.document),
  );

  Future<Object?> saveStaff(
    Map<String, dynamic> body, {
    Object? id,
    String? temporaryPassword,
  }) => execute(
    ResourceCommand(
      id == null ? ResourceMethod.post : ResourceMethod.put,
      id == null ? AdminTargets.staff : AdminTargets.staffMember(id),
      body: {...body, if (id == null) 'temporaryPassword': temporaryPassword},
    ),
  );

  Future<Object?> resetPassword(Object id, String password) => execute(
    ResourceCommand(
      ResourceMethod.post,
      AdminTargets.staffResetPassword(id),
      body: {'temporaryPassword': password},
    ),
  );

  Future<Object?> updatePermissions(Object id, Iterable<String> permissions) =>
      execute(
        ResourceCommand(
          ResourceMethod.put,
          AdminTargets.staffPermissions(id),
          body: {'permissions': permissions.toList(growable: false)},
        ),
      );

  Future<Object?> setActive(Object id, bool active) => execute(
    ResourceCommand(
      ResourceMethod.patch,
      AdminTargets.staffStatus(id),
      body: {'status': active ? 'ACTIVE' : 'INACTIVE'},
    ),
  );

  Future<Object?> updateEmployment(Object id, Map<String, dynamic> body) =>
      _putStaffChild(id, 'employment', body);

  Future<Object?> saveAttendance(Object id, Map<String, dynamic> body) =>
      _putStaffChild(id, 'attendance', body);

  Future<Object?> recordSalary(Object id, Map<String, dynamic> body) => execute(
    ResourceCommand(
      ResourceMethod.post,
      AdminTargets.staffSalaryPayments(id),
      body: body,
    ),
  );

  Future<Object?> assignTask(Object staffId, Map<String, dynamic> body) =>
      execute(
        ResourceCommand(
          ResourceMethod.post,
          AdminTargets.tasks,
          body: {...body, 'assignedTo': staffId},
        ),
      );

  Future<Object?> completeTask(Object taskId, String note) => execute(
    ResourceCommand(
      ResourceMethod.patch,
      AdminTargets.taskComplete(taskId),
      body: {'completionNote': note},
    ),
  );

  Future<Object?> _putStaffChild(
    Object id,
    String child,
    Map<String, dynamic> body,
  ) => execute(
    ResourceCommand(
      ResourceMethod.put,
      child == 'employment'
          ? AdminTargets.staffEmployment(id)
          : AdminTargets.staffAttendance(id),
      body: body,
    ),
  );
}
