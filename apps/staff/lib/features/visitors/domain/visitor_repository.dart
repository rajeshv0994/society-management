import 'visitor_models.dart';

abstract interface class StaffVisitorRepository {
  Future<StaffVisitorWorkspaceData> load();
  Future<List<VisitorTarget>> loadHosts();
  Future<List<VisitorTarget>> loadTargets();
  Future<void> updateStatus(String visitorId, String status);
  Future<void> uploadPhoto(String visitorId, List<int> bytes, String filename);
  Future<StaffVisitorRecord> create(StaffVisitorRegistration input);
  Future<void> recordPhoneApproval(String visitorId, String note);
  Future<void> recordDecision(String visitorId, String decision);
}
