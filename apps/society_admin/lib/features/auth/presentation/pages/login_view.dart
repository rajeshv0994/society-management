import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';
import '../../domain/entities/admin_session.dart';
import '../bloc/auth_bloc.dart';

class LoginView extends StatefulWidget {
  const LoginView({super.key, this.error, this.societies = const []});
  final String? error;
  final List<PublicSociety> societies;
  @override
  State<LoginView> createState() => _LoginState();
}

class _LoginState extends State<LoginView> {
  final email = TextEditingController(text: 'green.admin@society.local'),
      password = TextEditingController(),
      societyCode = TextEditingController(text: 'GREEN-VALLEY');
  void submit() => context.read<AuthBloc>().add(
    LoginAuth(email.text, password.text, societyCode.text),
  );

  @override
  void dispose() {
    email.dispose();
    password.dispose();
    societyCode.dispose();
    super.dispose();
  }

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
                        color: SocietyColors.blue.withValues(alpha: .1),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.apartment_rounded,
                        size: 31,
                        color: SocietyColors.blue,
                      ),
                    ),
                    const SizedBox(height: 18),
                    Text(
                      'Society Administration',
                      style: Theme.of(c).textTheme.headlineSmall,
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 7),
                    const Text(
                      'Manage your community, residents, staff, and daily operations',
                      textAlign: TextAlign.center,
                      style: TextStyle(color: SocietyColors.textMuted),
                    ),
                    const SizedBox(height: 28),
                    if (widget.societies.isNotEmpty)
                      DropdownButtonFormField<String>(
                        isExpanded: true,
                        decoration: const InputDecoration(
                          labelText: 'Select society',
                          prefixIcon: Icon(Icons.apartment_outlined),
                        ),
                        items: widget.societies
                            .map(
                              (society) => DropdownMenuItem(
                                value: society.code,
                                child: Text(
                                  '${society.name} (${society.code})',
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                            )
                            .toList(),
                        onChanged: (value) {
                          if (value != null) societyCode.text = value;
                        },
                      ),
                    if (widget.societies.isNotEmpty) const SizedBox(height: 14),
                    TextField(
                      controller: societyCode,
                      textCapitalization: TextCapitalization.characters,
                      decoration: const InputDecoration(
                        labelText: 'Society code',
                        prefixIcon: Icon(Icons.tag),
                      ),
                    ),
                    const SizedBox(height: 14),
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
