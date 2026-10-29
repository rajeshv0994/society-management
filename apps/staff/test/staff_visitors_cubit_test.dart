import 'package:flutter_test/flutter_test.dart';
import 'package:staff_app/features/visitors/domain/visitor_models.dart';
import 'package:staff_app/features/visitors/domain/visitor_repository.dart';
import 'package:staff_app/features/visitors/presentation/bloc/staff_visitors_cubit.dart';

void main() {
  test('staff visitors cubit owns loading and status mutation', () async {
    final repository = _FakeRepository();
    final cubit = StaffVisitorsCubit(repository);
    addTearDown(cubit.close);

    await cubit.load();
    await cubit.updateStatus(cubit.state.data!.visitors.single, 'CHECKED_IN');

    expect(cubit.state.status, StaffVisitorsStatus.ready);
    expect(repository.loadCalls, 2);
    expect(repository.statusCalls, 1);
  });
}

class _FakeRepository implements StaffVisitorRepository {
  int loadCalls = 0;
  int statusCalls = 0;

  @override
  Future<StaffVisitorWorkspaceData> load() async {
    loadCalls++;
    return StaffVisitorWorkspaceData(
      visitors: [
        StaffVisitorRecord(StaffVisitorFields.from({'id': 'visitor-1'})),
      ],
      canManage: true,
      currentStaffId: 'staff-1',
    );
  }

  @override
  Future<void> updateStatus(String visitorId, String status) async {
    statusCalls++;
  }

  @override
  Future<List<VisitorTarget>> loadHosts() async => const [];
  @override
  Future<List<VisitorTarget>> loadTargets() async => const [];
  @override
  Future<void> uploadPhoto(
    String visitorId,
    List<int> bytes,
    String filename,
  ) async {}
  @override
  Future<StaffVisitorRecord> create(StaffVisitorRegistration input) async =>
      StaffVisitorRecord(StaffVisitorFields.from({'id': 'created'}));
  @override
  Future<void> recordPhoneApproval(String visitorId, String note) async {}
  @override
  Future<void> recordDecision(String visitorId, String decision) async {}
}
