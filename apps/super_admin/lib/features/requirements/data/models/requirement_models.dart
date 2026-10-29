import '../../domain/entities/requirements_workspace.dart';

class RequirementRecordModel extends RequirementRecord {
  RequirementRecordModel(super.data);
  factory RequirementRecordModel.fromJson(Map<String, dynamic> json) =>
      RequirementRecordModel(Map.unmodifiable(json));
}

class CustomPlanRecordModel extends CustomPlanRecord {
  CustomPlanRecordModel(super.data);
  factory CustomPlanRecordModel.fromJson(Map<String, dynamic> json) =>
      CustomPlanRecordModel(Map.unmodifiable(json));
}
