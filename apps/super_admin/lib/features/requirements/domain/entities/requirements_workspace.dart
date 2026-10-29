import '../../../../core/immutable_fields.dart';

import '../../../societies/domain/entities/society_summary.dart';

class RequirementRecord {
  RequirementRecord(Map<String, dynamic> data)
    : data = ImmutableFields.from(data);
  final ImmutableFields data;
}

class CustomPlanRecord {
  CustomPlanRecord(Map<String, dynamic> data)
    : data = ImmutableFields.from(data);
  final ImmutableFields data;
}

class RequirementsWorkspace {
  const RequirementsWorkspace({
    required this.requirements,
    required this.plans,
    required this.societies,
  });
  final List<RequirementRecord> requirements;
  final List<CustomPlanRecord> plans;
  final List<SocietySummary> societies;
}

class ReviewRequirementCommand {
  const ReviewRequirementCommand(this.id, this.status, this.notes);
  final String id, status, notes;
}

class CreateCustomPlanCommand {
  CreateCustomPlanCommand(Map<String, dynamic> values)
    : values = ImmutableFields.from(values);
  final ImmutableFields values;
}

class UpdateCustomPlanCommand {
  UpdateCustomPlanCommand(this.id, Map<String, dynamic> values)
    : values = ImmutableFields.from(values);
  final String id;
  final ImmutableFields values;
}
