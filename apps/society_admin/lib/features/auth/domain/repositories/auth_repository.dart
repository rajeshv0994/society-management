import '../entities/admin_session.dart';

abstract interface class AuthRepository {
  Future<AdminSession?> restore();
  Future<AdminSession> login(String email, String password, String societyCode);
  Future<void> logout();
  Future<List<PublicSociety>> loadPublicSocieties();
  bool get isAuthenticated;
  void addSessionListener(void Function() listener);
  void removeSessionListener(void Function() listener);
  void dispose();
}
