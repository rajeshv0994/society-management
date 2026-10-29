import 'package:society_ui/society_ui.dart';
import '../../domain/entities/admin_session.dart';
import '../../domain/repositories/authentication_repository.dart';
import '../datasources/session_data_source.dart';
import '../models/admin_session_model.dart';

class SessionAuthenticationRepository implements AuthenticationRepository {
  SessionAuthenticationRepository(this.dataSource);
  final SessionDataSource dataSource;
  SessionManager get sessions => dataSource.sessions;
  @override
  Stream<bool> get authenticationChanges => dataSource.changes;
  @override
  Future<AdminSession?> restore() async {
    final value = await dataSource.restore();
    return value == null ? null : AdminSessionModel.fromSession(value);
  }

  @override
  Future<AdminSession> login(String email, String password) async =>
      AdminSessionModel.fromSession(await dataSource.login(email, password));
  @override
  Future<void> logout() => dataSource.logout();
  void dispose() => dataSource.dispose();
}
