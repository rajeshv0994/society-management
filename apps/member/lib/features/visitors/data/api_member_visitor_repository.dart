import 'package:society_ui/society_ui.dart';

import '../domain/visitor_models.dart';
import '../domain/visitor_repository.dart';

class ApiMemberVisitorRepository implements MemberVisitorRepository {
  const ApiMemberVisitorRepository(this.api);

  final ApiGateway api;

  @override
  Future<MemberVisitorWorkspaceData> load() async {
    final values = await Future.wait([
      api.get('/api/visitors'),
      api.get('/api/visitors/member-options'),
    ]);
    final options = Map<String, dynamic>.from(values[1] as Map);
    return MemberVisitorWorkspaceData(
      visitors: List.unmodifiable(
        (values[0] as List).map(
          (item) => _record(Map<String, dynamic>.from(item as Map)),
        ),
      ),
      spaces: List.unmodifiable(
        (options['spaces'] as List? ?? const []).map(
          (item) => _space(Map<String, dynamic>.from(item as Map)),
        ),
      ),
    );
  }

  @override
  Future<void> decide(String visitorId, String decision, String? note) async {
    await api.patch('/api/visitors/$visitorId/approval', {
      'decision': decision,
      if (note != null && note.isNotEmpty) 'note': note,
    });
  }

  @override
  Future<void> cancel(String visitorId, String? note) async {
    await api.patch('/api/visitors/$visitorId/cancel', {
      if (note != null && note.isNotEmpty) 'note': note,
    });
  }

  @override
  Future<void> register(VisitorRegistrationInput input) async {
    await api.post('/api/visitors', _registrationJson(input));
  }

  @override
  Future<List<int>> loadImage(String url) => api.getBytes(url);

  static VisitorRecord _record(Map<String, dynamic> json) =>
      VisitorRecord(VisitorFields.from(json));

  static VisitorSpace _space(Map<String, dynamic> json) => VisitorSpace(
    id: json['space_id']?.toString() ?? '',
    number: json['space_number']?.toString() ?? '',
    block: json['block']?.toString(),
  );

  static Map<String, dynamic> _registrationJson(
    VisitorRegistrationInput input,
  ) => {
    'spaceId': input.spaceId,
    'targetType': 'RESIDENT',
    'visitorName': input.visitorName,
    'mobile': input.mobile,
    'purpose': input.purpose,
    'vehicleNumber': input.vehicleNumber,
    'expectedAt': input.expectedAt.toUtc().toIso8601String(),
    'accompanyingMembers': input.accompanyingMembers,
  };
}
