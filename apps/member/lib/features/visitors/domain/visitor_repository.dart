import 'visitor_models.dart';

abstract interface class MemberVisitorRepository {
  Future<MemberVisitorWorkspaceData> load();
  Future<void> decide(String visitorId, String decision, String? note);
  Future<void> cancel(String visitorId, String? note);
  Future<void> register(VisitorRegistrationInput input);
  Future<List<int>> loadImage(String url);
}
