import '../entities/admin_session.dart';
import '../repositories/authentication_repository.dart';

class AuthenticationUseCases {
  const AuthenticationUseCases(this.repository);
  final AuthenticationRepository repository;
  Stream<bool> get changes => repository.authenticationChanges;
  Future<AdminSession?> restore() => repository.restore();
  Future<AdminSession> login(String email, String password) =>
      repository.login(email, password);
  Future<void> logout() => repository.logout();
}
