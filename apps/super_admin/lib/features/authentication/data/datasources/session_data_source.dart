import 'dart:async';
import 'package:society_ui/society_ui.dart';

class SessionDataSource {
  SessionDataSource(this.sessions) {
    sessions.addListener(_changed);
  }
  final SessionManager sessions;
  final _changes = StreamController<bool>.broadcast();
  Stream<bool> get changes => _changes.stream;
  void _changed() => _changes.add(sessions.isAuthenticated);
  Future<SessionData?> restore() => sessions.restore();
  Future<SessionData> login(String email, String password) =>
      sessions.login(email, password);
  Future<void> logout() => sessions.logout();
  void dispose() {
    sessions.removeListener(_changed);
    sessions.dispose();
    _changes.close();
  }
}
