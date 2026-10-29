import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';
import '../bloc/auth_bloc.dart';

class LoginView extends StatefulWidget {
  const LoginView({super.key, this.error});
  final String? error;
  @override
  State<LoginView> createState() => _LoginViewState();
}

class _LoginViewState extends State<LoginView> {
  final email = TextEditingController(text: 'admin@society.local'),
      password = TextEditingController();

  @override
  void dispose() {
    email.dispose();
    password.dispose();
    super.dispose();
  }

  void submit() => context.read<AuthBloc>().add(
    LoginSubmitted(email.text.trim(), password.text),
  );
  @override
  Widget build(c) => Scaffold(
    body: SocietyLoginBackground(
      child: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 440),
            child: Card(
              child: Padding(
                padding: const EdgeInsets.all(32),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const Center(child: SocietyBrandMark()),
                    const SizedBox(height: 28),
                    Container(
                      width: 60,
                      height: 60,
                      decoration: BoxDecoration(
                        color: const Color(0xff6e42bd).withValues(alpha: .1),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.admin_panel_settings_rounded,
                        size: 31,
                        color: Color(0xff6e42bd),
                      ),
                    ),
                    const SizedBox(height: 18),
                    Text(
                      'Platform Control',
                      style: Theme.of(c).textTheme.headlineSmall,
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 7),
                    const Text(
                      'Manage the SocietyHub platform and every connected community',
                      textAlign: TextAlign.center,
                      style: TextStyle(color: SocietyColors.textMuted),
                    ),
                    const SizedBox(height: 28),
                    SocietyEmailField(controller: email),
                    const SizedBox(height: 14),
                    SocietyPasswordField(
                      controller: password,
                      onSubmitted: (_) => submit(),
                    ),
                    const SizedBox(height: 20),
                    FilledButton(
                      onPressed: submit,
                      child: const Text('Sign in securely'),
                    ),
                    if (widget.error != null)
                      Padding(
                        padding: const EdgeInsets.only(top: 14),
                        child: Text(
                          widget.error!,
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            color: Theme.of(c).colorScheme.error,
                          ),
                        ),
                      ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    ),
  );
}
