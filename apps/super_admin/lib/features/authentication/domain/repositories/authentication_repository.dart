import '../entities/admin_session.dart';

abstract interface class AuthenticationRepository {
  Stream<bool> get authenticationChanges;
  Future<AdminSession?> restore();
  Future<AdminSession> login(String email, String password);
  Future<void> logout();
}
