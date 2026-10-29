import 'package:flutter/material.dart';

class SocietyEmailField extends StatelessWidget {
  const SocietyEmailField({
    super.key,
    required this.controller,
    this.onSubmitted,
    this.enabled = true,
  });

  final TextEditingController controller;
  final ValueChanged<String>? onSubmitted;
  final bool enabled;

  @override
  Widget build(BuildContext context) => TextField(
    controller: controller,
    enabled: enabled,
    keyboardType: TextInputType.emailAddress,
    autofillHints: const [AutofillHints.email],
    textInputAction: TextInputAction.next,
    onSubmitted: onSubmitted,
    decoration: InputDecoration(
      labelText: 'Email address',
      prefixIcon: Icon(
        Icons.mail_outline_rounded,
        color: Theme.of(context).colorScheme.primary,
        semanticLabel: 'Email',
      ),
    ),
  );
}

class SocietyPasswordField extends StatefulWidget {
  const SocietyPasswordField({
    super.key,
    required this.controller,
    this.onSubmitted,
    this.enabled = true,
    this.label = 'Password',
  });

  final TextEditingController controller;
  final ValueChanged<String>? onSubmitted;
  final bool enabled;
  final String label;

  @override
  State<SocietyPasswordField> createState() => _SocietyPasswordFieldState();
}

class _SocietyPasswordFieldState extends State<SocietyPasswordField> {
  bool _hidden = true;

  @override
  Widget build(BuildContext context) => TextField(
    controller: widget.controller,
    enabled: widget.enabled,
    obscureText: _hidden,
    enableSuggestions: false,
    autocorrect: false,
    autofillHints: const [AutofillHints.password],
    textInputAction: TextInputAction.done,
    onSubmitted: widget.onSubmitted,
    decoration: InputDecoration(
      labelText: widget.label,
      prefixIcon: Icon(
        Icons.lock_outline_rounded,
        color: Theme.of(context).colorScheme.primary,
        semanticLabel: 'Password',
      ),
      suffixIcon: IconButton(
        tooltip: _hidden ? 'Show password' : 'Hide password',
        onPressed: () => setState(() => _hidden = !_hidden),
        icon: Icon(
          _hidden ? Icons.visibility_outlined : Icons.visibility_off_outlined,
        ),
      ),
    ),
  );
}
