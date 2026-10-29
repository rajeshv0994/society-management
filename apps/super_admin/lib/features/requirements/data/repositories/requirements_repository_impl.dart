import '../datasources/requirements_remote_data_source.dart';
import '../../../societies/data/models/society_models.dart';
import '../../domain/entities/requirements_workspace.dart';
import '../../domain/repositories/requirements_repository.dart';
import '../models/requirement_models.dart';

class RequirementsRepositoryImpl implements RequirementsRepository {
  const RequirementsRepositoryImpl(this.remote);
  final RequirementsRemoteDataSource remote;
  @override
  Future<RequirementsWorkspace> loadWorkspace() async {
    final values = await Future.wait([
      remote.records('/api/admin/platform/requirements'),
      remote.records('/api/admin/platform/custom-plans'),
      remote.societies(),
    ]);
    return RequirementsWorkspace(
      requirements: values[0]
          .map(RequirementRecordModel.fromJson)
          .toList(growable: false),
      plans: values[1]
          .map(CustomPlanRecordModel.fromJson)
          .toList(growable: false),
      societies: values[2]
          .map(SocietySummaryModel.fromJson)
          .toList(growable: false),
    );
  }

  @override
  Future<void> review(ReviewRequirementCommand command) =>
      remote.review(command.id, command.status, command.notes);
  @override
  Future<void> createPlan(CreateCustomPlanCommand command) async {
    await remote.createPlan(command.values);
  }

  @override
  Future<void> updateCustomPlan(UpdateCustomPlanCommand command) async {
    await remote.updatePlan(command.id, command.values);
  }

  @override
  Future<void> cancelCustomPlan(String id, String reason) =>
      remote.cancelPlan(id, reason);
}
