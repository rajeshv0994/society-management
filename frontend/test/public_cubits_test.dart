import 'package:flutter_test/flutter_test.dart';
import 'package:society_app/features/public_portal/domain/public_models.dart';
import 'package:society_app/features/public_portal/domain/public_repository.dart';
import 'package:society_app/features/public_portal/presentation/bloc/public_cubits.dart';

void main() {
  test('plans cubit owns loading and immutable results', () async {
    final cubit = PlansCubit(_Repository());
    addTearDown(cubit.close);
    await cubit.load();
    expect(cubit.state.status, PublicStatus.ready);
    expect(cubit.state.data!.single['code'], 'TRIAL');
    expect(
      () => cubit.state.data!.single['code'] = 'changed',
      throwsUnsupportedError,
    );
  });

  test(
    'society directory failure is visible instead of looking empty',
    () async {
      final cubit = LoginCubit(_Repository(failSocieties: true));
      addTearDown(cubit.close);

      await cubit.load();

      expect(cubit.state.status, PublicStatus.failure);
      expect(cubit.state.message, contains('directory unavailable'));
    },
  );
}

class _Repository implements PublicRepository {
  _Repository({this.failSocieties = false});

  final bool failSocieties;

  @override
  Future<List<PublicRecord>> loadPlans() async => [
    PublicRecord.from({'code': 'TRIAL'}),
  ];
  @override
  Future<void> activate(SocietyActivationCommand command) async {}
  @override
  Future<PublicRecord> login(LoginCommand command) async =>
      PublicRecord.from({});
  @override
  Future<List<PublicRecord>> loadServices() async => const [];
  @override
  Future<List<PublicRecord>> loadSocieties() async {
    if (failSocieties) throw StateError('directory unavailable');
    return const [];
  }

  @override
  Future<PublicRecord> register(SocietyRegistrationCommand command) async =>
      PublicRecord.from({});
  @override
  Future<PublicRecord> submitRequirement(RequirementCommand command) async =>
      PublicRecord.from({});
}
