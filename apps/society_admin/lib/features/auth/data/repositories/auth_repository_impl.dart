import 'package:society_ui/society_ui.dart';
import '../../domain/entities/admin_session.dart';
import '../../domain/repositories/auth_repository.dart';
import '../datasources/auth_remote_data_source.dart';
import '../models/public_society_model.dart';

class AuthRepositoryImpl implements AuthRepository {
  AuthRepositoryImpl(this.sessions, this.remote);

  final SessionManager sessions;
  final AuthRemoteDataSource remote;

  AdminSession _map(SessionData value) => AdminSession(
    userId: value.userId,
    fullName: value.fullName,
    role: value.role,
    expiresIn: value.expiresIn,
    societyId: value.societyId,
  );

  @override
  Future<AdminSession?> restore() async {
    final value = await sessions.restore();
    return value == null ? null : _map(value);
  }

  @override
  Future<AdminSession> login(
    String email,
    String password,
    String societyCode,
  ) async =>
      _map(await sessions.login(email, password, societyCode: societyCode));

  @override
  Future<void> logout() => sessions.logout();

  @override
  Future<List<PublicSociety>> loadPublicSocieties() async => List.unmodifiable(
    (await remote.loadPublicSocieties())
        .map(
          (item) => PublicSocietyModel.fromJson(
            Map<String, dynamic>.from(item as Map),
          ),
        )
        .where((society) => society.code.isNotEmpty),
  );

  @override
  bool get isAuthenticated => sessions.isAuthenticated;
  @override
  void addSessionListener(void Function() listener) =>
      sessions.addListener(listener);
  @override
  void removeSessionListener(void Function() listener) =>
      sessions.removeListener(listener);
  @override
  void dispose() => sessions.dispose();
}
