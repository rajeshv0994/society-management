import 'package:flutter/material.dart';
import 'package:society_ui/society_ui.dart';

class PageFrame extends StatelessWidget {
  const PageFrame({
    super.key,
    required this.title,
    required this.child,
    this.subtitle = '',
    this.action,
  });
  final String title, subtitle;
  final Widget child;
  final Widget? action;
  @override
  Widget build(c) => Padding(
    padding: EdgeInsets.all(MediaQuery.sizeOf(c).width < 600 ? 16 : 22),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        LayoutBuilder(
          builder: (context, constraints) {
            final heading = Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: Theme.of(c).textTheme.headlineMedium),
                Text(
                  subtitle,
                  style: const TextStyle(color: SocietyColors.textMuted),
                ),
              ],
            );
            if (constraints.maxWidth < 600 && action != null) {
              return Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  heading,
                  const SizedBox(height: 12),
                  Align(alignment: Alignment.centerLeft, child: action),
                ],
              );
            }
            return Row(
              children: [
                Expanded(child: heading),
                ?action,
              ],
            );
          },
        ),
        const SizedBox(height: 18),
        Expanded(child: child),
      ],
    ),
  );
}

class StatusChip extends StatelessWidget {
  const StatusChip(this.status, {super.key});
  final String status;
  @override
  Widget build(c) => Chip(
    label: Text(status.replaceAll('_', ' ')),
    side: BorderSide.none,
    backgroundColor:
        ([
                  'ACTIVE',
                  'PAID',
                  'RESOLVED',
                  'COMPLETED',
                  'CHECKED_IN',
                ].contains(status)
                ? SocietyColors.success
                : SocietyColors.warning)
            .withValues(alpha: .13),
  );
}

class Empty extends StatelessWidget {
  const Empty(this.text, {super.key});
  final String text;
  @override
  Widget build(c) => Center(
    child: Text(text, style: const TextStyle(color: SocietyColors.textMuted)),
  );
}

class ErrorState extends StatelessWidget {
  const ErrorState({super.key, required this.message, required this.onRetry});

  final String message;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) => Center(
    child: ConstrainedBox(
      constraints: const BoxConstraints(maxWidth: 520),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(
            Icons.cloud_off_outlined,
            size: 44,
            color: SocietyColors.warning,
          ),
          const SizedBox(height: 12),
          Text(
            message,
            textAlign: TextAlign.center,
            style: const TextStyle(color: SocietyColors.textMuted),
          ),
          const SizedBox(height: 14),
          OutlinedButton.icon(
            onPressed: onRetry,
            icon: const Icon(Icons.refresh),
            label: const Text('Try again'),
          ),
        ],
      ),
    ),
  );
}
