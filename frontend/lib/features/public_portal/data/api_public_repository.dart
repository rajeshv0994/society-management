import '../../../src/api.dart';
import '../domain/public_models.dart';
import '../domain/public_repository.dart';

class ApiPublicRepository implements PublicRepository {
  const ApiPublicRepository(this.api);
  final ApiClient api;

  Future<List<PublicRecord>> _list(String path) async => List.unmodifiable(
    (await api.get(path) as List).map((item) => PublicRecord.from(item as Map)),
  );

  @override
  Future<List<PublicRecord>> loadPlans() => _list('/api/public/plans');
  @override
  Future<List<PublicRecord>> loadSocieties() => _list('/api/public/societies');
  @override
  Future<List<PublicRecord>> loadServices() => _list('/api/public/services');

  @override
  Future<PublicRecord> login(LoginCommand command) async => PublicRecord.from(
    await api.post('/api/auth/login', {
          'email': command.email,
          'password': command.password,
          if (command.societyCode?.trim().isNotEmpty == true)
            'societyCode': command.societyCode!.trim().toUpperCase(),
        })
        as Map,
  );

  @override
  Future<PublicRecord> register(SocietyRegistrationCommand command) async =>
      PublicRecord.from(
        await api.post('/api/public/societies/register', {
              'name': command.name,
              'societyType': command.societyType,
              'totalBlocks': command.totalBlocks,
              'totalSpaces': command.totalSpaces,
              'plannedStaff': command.plannedStaff,
              'approximateResidents': command.approximateResidents,
              'address': command.address,
              'city': command.city,
              'state': command.state,
              'pinCode': command.pinCode,
              'contactPerson': command.contactPerson,
              'email': command.email,
              'mobile': '0000000000',
              'planCode': command.planCode,
              'enabledModules': command.enabledModules,
            })
            as Map,
      );

  @override
  Future<void> activate(SocietyActivationCommand command) async {
    await api.post('/api/public/societies/activate', {
      'setupCode': command.setupCode,
      'fullName': command.fullName,
      'email': command.email,
      'mobile': command.mobile,
      'password': command.password,
    });
  }

  @override
  Future<PublicRecord> submitRequirement(RequirementCommand command) async =>
      PublicRecord.from(
        await api.post('/api/public/requirements', {
              'societyName': command.societyName,
              'contactPerson': command.contactPerson,
              'email': command.email,
              'mobile': command.mobile,
              'city': command.city,
              'estimatedSpaces': command.estimatedSpaces,
              'estimatedStaff': command.estimatedStaff,
              'requestedModules': command.requestedModules,
              'requirementDetails': command.requirementDetails,
              'budgetMonthly': command.budgetMonthly,
            })
            as Map,
      );
}
