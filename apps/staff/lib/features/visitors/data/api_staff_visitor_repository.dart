import 'package:society_ui/society_ui.dart';

import '../domain/visitor_models.dart';
import '../domain/visitor_repository.dart';

class ApiStaffVisitorRepository implements StaffVisitorRepository {
  const ApiStaffVisitorRepository(this.api);
  final ApiGateway api;

  @override
  Future<StaffVisitorWorkspaceData> load() async {
    final permissions = Map<String, dynamic>.from(
      await api.get('/api/visitors/permissions') as Map,
    );
    final visitors = await _list('/api/visitors');
    return StaffVisitorWorkspaceData(
      visitors: List.unmodifiable(visitors.map(_record)),
      canManage: permissions['can_manage_gate'] == true,
      currentStaffId: permissions['user_id']?.toString() ?? '',
    );
  }

  Future<List<Map<String, dynamic>>> _list(String path) async =>
      (await api.get(path) as List)
          .map((item) => Map<String, dynamic>.from(item as Map))
          .toList();

  @override
  Future<List<VisitorTarget>> loadHosts() async =>
      List.unmodifiable((await _list('/api/visitors/hosts')).map(_target));

  @override
  Future<List<VisitorTarget>> loadTargets() async =>
      List.unmodifiable((await _list('/api/visitors/targets')).map(_target));

  @override
  Future<void> updateStatus(String visitorId, String status) async {
    await api.patch('/api/visitors/$visitorId/status', {'status': status});
  }

  @override
  Future<void> uploadPhoto(
    String visitorId,
    List<int> bytes,
    String filename,
  ) async {
    await api.upload(
      '/api/visitors/$visitorId/photo',
      bytes: bytes,
      filename: filename,
    );
  }

  @override
  Future<StaffVisitorRecord> create(StaffVisitorRegistration input) async =>
      _record(
        Map<String, dynamic>.from(
          await api.post('/api/visitors', _registrationJson(input)) as Map,
        ),
      );

  @override
  Future<void> recordPhoneApproval(String visitorId, String note) async {
    await api.patch('/api/visitors/$visitorId/staff-approval', {'note': note});
  }

  @override
  Future<void> recordDecision(String visitorId, String decision) async {
    await api.patch('/api/visitors/$visitorId/staff-decision', {
      'decision': decision,
    });
  }

  static StaffVisitorRecord _record(Map<String, dynamic> json) =>
      StaffVisitorRecord(StaffVisitorFields.from(json));

  static VisitorTarget _target(Map<String, dynamic> json) =>
      VisitorTarget(StaffVisitorFields.from(json));

  static Map<String, dynamic> _registrationJson(
    StaffVisitorRegistration input,
  ) => {
    'targetType': input.targetType,
    if (input.spaceId != null) 'spaceId': input.spaceId,
    if (input.hostResidentId != null) 'hostResidentId': input.hostResidentId,
    if (input.targetUserId != null) 'targetUserId': input.targetUserId,
    if (input.targetName != null) 'targetName': input.targetName,
    'visitorName': input.visitorName,
    'mobile': input.mobile,
    'purpose': input.purpose,
    'vehicleNumber': input.vehicleNumber,
    'expectedAt': input.expectedAt.toUtc().toIso8601String(),
    'accompanyingMembers': input.accompanyingMembers,
  };
}
