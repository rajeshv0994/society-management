import 'public_models.dart';

abstract interface class PublicRepository {
  Future<List<PublicRecord>> loadPlans();
  Future<List<PublicRecord>> loadSocieties();
  Future<List<PublicRecord>> loadServices();
  Future<PublicRecord> login(LoginCommand command);
  Future<PublicRecord> register(SocietyRegistrationCommand command);
  Future<void> activate(SocietyActivationCommand command);
  Future<PublicRecord> submitRequirement(RequirementCommand command);
}
