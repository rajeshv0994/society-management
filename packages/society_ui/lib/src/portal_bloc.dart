import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';
import 'api_gateway_bloc.dart';
import 'app_failure.dart';
import 'portal_models.dart';
import 'portal_repository.dart';
import 'session_manager.dart';

sealed class PortalAuthEvent {
  const PortalAuthEvent();
}

final class PortalAuthStarted extends PortalAuthEvent {
  const PortalAuthStarted();
}

final class PortalLoginSubmitted extends PortalAuthEvent {
  const PortalLoginSubmitted({
    required this.email,
    required this.password,
    this.societyCode,
  });

  final String email;
  final String password;
  final String? societyCode;
}

final class PortalLogoutRequested extends PortalAuthEvent {
  const PortalLogoutRequested();
}

final class _PortalSessionChanged extends PortalAuthEvent {
  const _PortalSessionChanged();
}

class PortalAuthBloc extends Bloc<PortalAuthEvent, PortalAuthState> {
  factory PortalAuthBloc({
    required PortalRole role,
    required SessionManager sessions,
    required PortalRepository repository,
  }) {
    final gatewayBloc = ApiGatewayBloc(HttpApiClient(session: sessions));
    return PortalAuthBloc._(
      role: role,
      sessions: sessions,
      repository: repository,
      gatewayBloc: gatewayBloc,
    );
  }

  PortalAuthBloc._({
    required this.role,
    required this.sessions,
    required this.repository,
    required ApiGatewayBloc gatewayBloc,
  }) : _gatewayBloc = gatewayBloc,
       authenticatedApi = BlocBackedApiGateway(gatewayBloc),
       super(const PortalAuthState()) {
    on<PortalAuthStarted>(_started);
    on<PortalLoginSubmitted>(_login);
    on<PortalLogoutRequested>(_logout);
    on<_PortalSessionChanged>(_sessionChanged);
    sessions.addListener(_onSessionNotification);
  }

  final PortalRole role;
  final SessionManager sessions;
  final PortalRepository repository;
  final BlocManagedApiGateway authenticatedApi;
  final ApiGatewayBloc _gatewayBloc;

  void _onSessionNotification() => add(const _PortalSessionChanged());

  Future<void> _started(
    PortalAuthStarted event,
    Emitter<PortalAuthState> emit,
  ) async {
    var societies = const <SocietyOption>[];
    String? discoveryError;
    if (role != PortalRole.superAdmin) {
      try {
        societies = await repository.loadSocieties();
      } catch (error) {
        // Manual society-code entry remains available while discovery is down.
        discoveryError = userFacingError(error);
      }
    }
    await sessions.restore();
    emit(
      PortalAuthState(
        status: sessions.isAuthenticated
            ? PortalAuthStatus.signedIn
            : PortalAuthStatus.signedOut,
        session: sessions.current,
        societies: societies,
        error: discoveryError,
      ),
    );
  }

  Future<void> _login(
    PortalLoginSubmitted event,
    Emitter<PortalAuthState> emit,
  ) async {
    final societyCode = event.societyCode?.trim() ?? '';
    if (role != PortalRole.superAdmin && societyCode.isEmpty) {
      emit(
        PortalAuthState(
          status: PortalAuthStatus.signedOut,
          societies: state.societies,
          error: 'Select a society or enter its society code.',
        ),
      );
      return;
    }
    emit(
      PortalAuthState(
        status: PortalAuthStatus.submitting,
        societies: state.societies,
      ),
    );
    try {
      final session = await sessions.login(
        event.email.trim(),
        event.password,
        societyCode: societyCode,
      );
      emit(
        PortalAuthState(
          status: PortalAuthStatus.signedIn,
          session: session,
          societies: state.societies,
        ),
      );
    } catch (error) {
      emit(
        PortalAuthState(
          status: PortalAuthStatus.signedOut,
          societies: state.societies,
          error: userFacingError(error),
        ),
      );
    }
  }

  Future<void> _logout(
    PortalLogoutRequested event,
    Emitter<PortalAuthState> emit,
  ) async {
    await sessions.logout();
    emit(
      PortalAuthState(
        status: PortalAuthStatus.signedOut,
        societies: state.societies,
      ),
    );
  }

  void _sessionChanged(
    _PortalSessionChanged event,
    Emitter<PortalAuthState> emit,
  ) {
    if (state.status == PortalAuthStatus.restoring ||
        state.status == PortalAuthStatus.submitting) {
      return;
    }
    emit(
      PortalAuthState(
        status: sessions.isAuthenticated
            ? PortalAuthStatus.signedIn
            : PortalAuthStatus.signedOut,
        session: sessions.current,
        societies: state.societies,
      ),
    );
  }

  @override
  Future<void> close() async {
    sessions.removeListener(_onSessionNotification);
    sessions.dispose();
    await _gatewayBloc.close();
    await super.close();
  }
}
