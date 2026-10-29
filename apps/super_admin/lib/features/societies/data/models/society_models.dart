import '../../domain/entities/society_summary.dart';

class SocietySummaryModel extends SocietySummary {
  SocietySummaryModel(super.data);
  factory SocietySummaryModel.fromJson(Map<String, dynamic> json) {
    final society = json['society'];
    return SocietySummaryModel(
      Map.unmodifiable({
        ...json,
        if (society is Map) ...Map<String, dynamic>.from(society),
      }),
    );
  }
}

class SocietyDetailModel extends SocietyDetail {
  SocietyDetailModel(super.data);
  factory SocietyDetailModel.fromJson(Map<String, dynamic> json) =>
      SocietyDetailModel(Map.unmodifiable(json));
}

class SocietyCreateResultModel extends SocietyCreateResult {
  SocietyCreateResultModel(super.data);
  factory SocietyCreateResultModel.fromJson(Map<String, dynamic> json) =>
      SocietyCreateResultModel(Map.unmodifiable(json));
}
