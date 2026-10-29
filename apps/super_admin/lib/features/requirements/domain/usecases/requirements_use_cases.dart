import '../entities/requirements_workspace.dart';
import '../repositories/requirements_repository.dart';

class RequirementsUseCases {
  const RequirementsUseCases(this.repository);
  final RequirementsRepository repository;
  Future<RequirementsWorkspace> load() => repository.loadWorkspace();
  Future<void> review(ReviewRequirementCommand command) =>
      repository.review(command);
  Future<void> createPlan(CreateCustomPlanCommand command) =>
      repository.createPlan(command);
  Future<void> updateCustomPlan(UpdateCustomPlanCommand command) =>
      repository.updateCustomPlan(command);
  Future<void> cancelCustomPlan(String id, String reason) =>
      repository.cancelCustomPlan(id, reason);
}
