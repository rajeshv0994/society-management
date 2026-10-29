import '../entities/admin_session.dart';
import '../repositories/auth_repository.dart';

class AuthUseCases {
  const AuthUseCases(this.repository);

  final AuthRepository repository;

  Future<AdminSession?> restore() => repository.restore();
  Future<AdminSession> login(String email, String password, String code) =>
      repository.login(email, password, code);
  Future<void> logout() => repository.logout();
  Future<List<PublicSociety>> loadPublicSocieties() =>
      repository.loadPublicSocieties();
}
