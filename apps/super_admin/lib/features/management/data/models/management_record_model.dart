import '../../domain/entities/management_record.dart';

class ManagementRecordModel extends ManagementRecord {
  ManagementRecordModel(super.data);
  factory ManagementRecordModel.fromJson(Map<String, dynamic> json) =>
      ManagementRecordModel(ManagementFields.from(json));
}
