// ignore_for_file: use_super_parameters
import '../../../../core/domain/usecases/admin_resource_use_cases.dart';
import '../../../../core/domain/entities/admin_resource.dart';
import '../../../../core/presentation/bloc/admin_resource_bloc.dart';

class OperationsBloc extends AdminResourceBloc {
  OperationsBloc(AdminResourceUseCases useCases) : super(useCases);

  void loadVisitors() => load(
    'visitors',
    const ResourceRequest(AdminTargets.visitors, ResourceKind.collection),
  );

  Future<List<Map<String, dynamic>>> loadVisitorHosts() async =>
      (await queryCollection(AdminTargets.visitorHosts)).items;

  Future<List<Map<String, dynamic>>> loadVisitorTargets() async =>
      (await queryCollection(AdminTargets.visitorTargets)).items;

  Future<Map<String, dynamic>> createVisitor(Map<String, dynamic> body) async =>
      Map<String, dynamic>.from(
        (await execute(
              ResourceCommand(
                ResourceMethod.post,
                AdminTargets.visitors,
                body: body,
              ),
            ))
            as Map,
      );

  Future<Object?> uploadVisitorPhoto(
    Object visitorId,
    List<int> bytes,
    String filename,
  ) => execute(
    ResourceCommand(
      ResourceMethod.uploadVisitorPhoto,
      AdminTarget(AdminTargetType.visitors, [visitorId]),
      bytes: bytes,
      filename: filename,
    ),
  );

  Future<Object?> approveByPhone(Object visitorId, String note) => execute(
    ResourceCommand(
      ResourceMethod.patch,
      AdminTargets.visitorStaffApproval(visitorId),
      body: {'note': note},
    ),
  );

  Future<Object?> updateVisitorStatus(Object visitorId, String status) =>
      execute(
        ResourceCommand(
          ResourceMethod.patch,
          AdminTargets.visitorStatus(visitorId),
          body: {'status': status},
        ),
      );
}
