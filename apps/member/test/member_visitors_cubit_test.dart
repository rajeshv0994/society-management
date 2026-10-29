import 'package:flutter_test/flutter_test.dart';
import 'package:member_app/features/visitors/domain/visitor_models.dart';
import 'package:member_app/features/visitors/domain/visitor_repository.dart';
import 'package:member_app/features/visitors/presentation/bloc/member_visitors_cubit.dart';

void main() {
  test('member visitors cubit owns loading and mutation refresh', () async {
    final repository = _FakeRepository();
    final cubit = MemberVisitorsCubit(repository);
    addTearDown(cubit.close);

    await cubit.load();
    await cubit.decide(cubit.state.data!.visitors.single, 'APPROVE', null);

    expect(cubit.state.status, MemberVisitorsStatus.ready);
    expect(repository.loadCalls, 2);
    expect(repository.decisions, 1);
  });
}

class _FakeRepository implements MemberVisitorRepository {
  int loadCalls = 0;
  int decisions = 0;

  @override
  Future<MemberVisitorWorkspaceData> load() async {
    loadCalls++;
    return MemberVisitorWorkspaceData(
      visitors: [
        VisitorRecord(VisitorFields.from({'id': 'visitor-1'})),
      ],
      spaces: const [VisitorSpace(id: 'space-1', number: '101')],
    );
  }

  @override
  Future<void> decide(String visitorId, String decision, String? note) async {
    decisions++;
  }

  @override
  Future<void> cancel(String visitorId, String? note) async {}
  @override
  Future<void> register(VisitorRegistrationInput input) async {}
  @override
  Future<List<int>> loadImage(String url) async => const [];
}
