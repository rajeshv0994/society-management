import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../app/dependency_injection.dart';
import '../../../shell/presentation/pages/shell_view.dart';
import '../bloc/auth_bloc.dart';
import 'login_view.dart';

class AuthGate extends StatelessWidget {
  const AuthGate({super.key, required this.dependencies});
  final SocietyAdminDependencies dependencies;
  @override
  Widget build(c) => BlocBuilder<AuthBloc, AuthState>(
    builder: (context, state) => switch (state) {
      AuthLoading() => const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      ),
      SignedOut(:final error, :final societies) => LoginView(
        error: error,
        societies: societies,
      ),
      SignedIn(:final profile) => ShellView(
        dependencies: dependencies,
        profile: profile,
      ),
    },
  );
}
