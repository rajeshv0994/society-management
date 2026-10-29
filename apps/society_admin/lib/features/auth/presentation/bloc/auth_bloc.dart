import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';
import '../../domain/entities/admin_session.dart';
import '../../domain/usecases/auth_use_cases.dart';

sealed class AuthEvent {
  const AuthEvent();
}

class RestoreAuth extends AuthEvent {
  const RestoreAuth();
}

class LoginAuth extends AuthEvent {
  const LoginAuth(this.email, this.password, this.societyCode);
  final String email, password, societyCode;
}

class LogoutAuth extends AuthEvent {
  const LogoutAuth();
}

class SessionExpired extends AuthEvent {
  const SessionExpired();
}

sealed class AuthState {
  const AuthState();
}

class AuthLoading extends AuthState {
  const AuthLoading();
}

class SignedOut extends AuthState {
  const SignedOut({this.error, this.societies = const []});
  final String? error;
  final List<PublicSociety> societies;
}

class SignedIn extends AuthState {
  const SignedIn(this.session);
  final AdminSession session;
  Map<String, dynamic> get profile => session.profile;
}

class AuthBloc extends Bloc<AuthEvent, AuthState> {
  AuthBloc(this.useCases) : super(const AuthLoading()) {
    useCases.repository.addSessionListener(_sessionChanged);
    on<AuthEvent>(_handle, transformer: _sequential());
  }

  final AuthUseCases useCases;

  static EventTransformer<AuthEvent> _sequential() =>
      (events, mapper) => events.asyncExpand(mapper);

  Future<void> _handle(AuthEvent event, Emitter<AuthState> emit) =>
      switch (event) {
        RestoreAuth value => _restore(value, emit),
        LoginAuth value => _login(value, emit),
        LogoutAuth value => _logout(value, emit),
        SessionExpired _ => Future.sync(() => emit(const SignedOut())),
      };

  Future<({List<PublicSociety> societies, String? error})> _societies() async {
    try {
      return (societies: await useCases.loadPublicSocieties(), error: null);
    } catch (error) {
      return (
        societies: const <PublicSociety>[],
        error: userFacingError(error),
      );
    }
  }

  Future<void> _restore(RestoreAuth event, Emitter<AuthState> emit) async {
    final session = await useCases.restore();
    final discovery = session == null ? await _societies() : null;
    emit(
      session == null
          ? SignedOut(societies: discovery!.societies, error: discovery.error)
          : SignedIn(session),
    );
  }

  Future<void> _login(LoginAuth event, Emitter<AuthState> emit) async {
    final societies = switch (state) {
      SignedOut(:final societies) => societies,
      _ => const <PublicSociety>[],
    };
    emit(const AuthLoading());
    try {
      emit(
        SignedIn(
          await useCases.login(event.email, event.password, event.societyCode),
        ),
      );
    } catch (error) {
      emit(SignedOut(error: userFacingError(error), societies: societies));
    }
  }

  Future<void> _logout(LogoutAuth event, Emitter<AuthState> emit) async {
    await useCases.logout();
    final discovery = await _societies();
    emit(SignedOut(societies: discovery.societies, error: discovery.error));
  }

  void _sessionChanged() {
    if (!useCases.repository.isAuthenticated &&
        state is SignedIn &&
        !isClosed) {
      add(const SessionExpired());
    }
  }

  @override
  Future<void> close() {
    useCases.repository
      ..removeSessionListener(_sessionChanged)
      ..dispose();
    return super.close();
  }
}
