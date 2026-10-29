// ignore_for_file: use_super_parameters
import '../../../../core/domain/usecases/admin_resource_use_cases.dart';
import '../../../../core/domain/entities/admin_resource.dart';
import '../../../../core/presentation/bloc/admin_resource_bloc.dart';
import '../../domain/entities/resident_create_input.dart';

class ResidentsBloc extends AdminResourceBloc {
  ResidentsBloc(AdminResourceUseCases useCases) : super(useCases);

  void loadResidents() {
    load(
      'pending',
      const ResourceRequest(
        AdminTargets.residentsPending,
        ResourceKind.collection,
      ),
    );
    load(
      'active',
      const ResourceRequest(AdminTargets.residents, ResourceKind.collection),
    );
  }

  Future<List<Map<String, dynamic>>> loadAvailableSpaces() async {
    final configuration = await queryDocument(AdminTargets.configuration);
    return (configuration.data['spaces'] as List? ?? const [])
        .map((item) => Map<String, dynamic>.from(item as Map))
        .toList(growable: false);
  }

  Future<Object?> createResident(ResidentCreateInput input) => execute(
    ResourceCommand(
      ResourceMethod.post,
      AdminTargets.residents,
      body: {
        'flatId': input.spaceId,
        'residentType': input.residentType,
        'fullName': input.fullName,
        'mobile': input.mobile,
        'email': input.email,
        'temporaryPassword': input.temporaryPassword,
        if (input.moveInDate != null) 'moveInDate': input.moveInDate,
      },
    ),
  );

  Future<Object?> reviewResident(
    Object id, {
    required bool approve,
    required String value,
  }) => execute(
    ResourceCommand(
      ResourceMethod.post,
      AdminTargets.residentReview(id),
      body: {
        'decision': approve ? 'APPROVE' : 'REJECT',
        'reason': approve ? null : value,
        'temporaryPassword': approve ? value : null,
      },
    ),
  );

  String residentKey(Object id) => 'resident-$id';

  void loadResident(Object id) => load(
    residentKey(id),
    ResourceRequest(AdminTargets.resident(id), ResourceKind.document),
  );

  Future<Object?> addHouseholdMember(
    Object residentId,
    Map<String, dynamic> data,
  ) => execute(
    ResourceCommand(
      ResourceMethod.post,
      AdminTargets.residentHousehold(residentId),
      body: data,
    ),
  );

  Future<Object?> removeHouseholdMember(Object residentId, Object memberId) =>
      execute(
        ResourceCommand(
          ResourceMethod.delete,
          AdminTargets.residentHouseholdMember(residentId, memberId),
        ),
      );

  Future<void> resolveComplaint(
    Object complaintId,
    String currentStatus,
  ) async {
    if (currentStatus == 'OPEN') {
      await execute(
        ResourceCommand(
          ResourceMethod.patch,
          AdminTargets.complaint(complaintId),
          body: {
            'status': 'IN_PROGRESS',
            'note': 'Society Admin started work from the resident record',
          },
        ),
      );
    }
    await execute(
      ResourceCommand(
        ResourceMethod.patch,
        AdminTargets.complaint(complaintId),
        body: {
          'status': 'RESOLVED',
          'note': 'Complaint resolved from the resident record',
          'resolution': 'Resolved by Society Admin from resident record',
        },
      ),
    );
  }

  Future<Object?> recordInvoicePayment(
    Object invoiceId,
    Map<String, dynamic> data,
  ) => execute(
    ResourceCommand(
      ResourceMethod.post,
      AdminTargets.invoicePayments(invoiceId),
      body: data,
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
