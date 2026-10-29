import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:super_admin_app/app/dependency_injection.dart';
import '../bloc/auth_bloc.dart';
import '../../../shell/presentation/pages/admin_shell_view.dart';
import 'login_view.dart';

class AuthGate extends StatelessWidget {
  const AuthGate({super.key, required this.dependencies});
  final AppDependencies dependencies;
  @override
  Widget build(c) => BlocBuilder<AuthBloc, AuthState>(
    builder: (context, state) => switch (state) {
      AuthLoading() => const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      ),
      Unauthenticated(:final error) => LoginView(error: error),
      Authenticated() => AdminShellView(dependencies: dependencies.platform()),
    },
  );
}
