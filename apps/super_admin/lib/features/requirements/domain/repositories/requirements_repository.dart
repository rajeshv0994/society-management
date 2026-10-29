import '../entities/requirements_workspace.dart';

abstract interface class RequirementsRepository {
  Future<RequirementsWorkspace> loadWorkspace();
  Future<void> review(ReviewRequirementCommand command);
  Future<void> createPlan(CreateCustomPlanCommand command);
  Future<void> updateCustomPlan(UpdateCustomPlanCommand command);
  Future<void> cancelCustomPlan(String id, String reason);
}
