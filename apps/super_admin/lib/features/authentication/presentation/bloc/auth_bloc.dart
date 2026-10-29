import 'dart:async';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/app_failure.dart' show userFacingError;
import '../../domain/entities/admin_session.dart';
import '../../domain/usecases/authentication_use_cases.dart';

sealed class AuthEvent {
  const AuthEvent();
}

class AuthRestored extends AuthEvent {
  const AuthRestored();
}

class LoginSubmitted extends AuthEvent {
  const LoginSubmitted(this.email, this.password);
  final String email, password;
}

class LogoutRequested extends AuthEvent {
  const LogoutRequested();
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

class Authenticated extends AuthState {
  const Authenticated(this.session);
  final AdminSession session;
}

class Unauthenticated extends AuthState {
  const Unauthenticated({this.error});
  final String? error;
}

class AuthBloc extends Bloc<AuthEvent, AuthState> {
  AuthBloc(this.useCases) : super(const AuthLoading()) {
    _subscription = useCases.changes.listen((authenticated) {
      if (!authenticated && state is Authenticated && !isClosed) {
        add(const SessionExpired());
      }
    });
    on<AuthRestored>((event, emit) async {
      final session = await useCases.restore();
      emit(session == null ? const Unauthenticated() : Authenticated(session));
    });
    on<LoginSubmitted>((event, emit) async {
      emit(const AuthLoading());
      try {
        emit(Authenticated(await useCases.login(event.email, event.password)));
      } catch (error) {
        emit(Unauthenticated(error: userFacingError(error)));
      }
    });
    on<LogoutRequested>((event, emit) async {
      await useCases.logout();
      emit(const Unauthenticated());
    });
    on<SessionExpired>((event, emit) {
      emit(const Unauthenticated());
    });
  }

  final AuthenticationUseCases useCases;
  late final StreamSubscription<bool> _subscription;

  @override
  Future<void> close() async {
    await _subscription.cancel();
    return super.close();
  }
}
