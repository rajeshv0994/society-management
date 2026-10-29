import 'package:flutter/material.dart';
import 'package:society_ui/society_ui.dart';

class PageFrame extends StatelessWidget {
  const PageFrame({
    super.key,
    required this.title,
    required this.subtitle,
    required this.child,
    this.onRefresh,
    this.toolbar,
  });
  final String title, subtitle;
  final Widget child;
  final VoidCallback? onRefresh;
  final Widget? toolbar;
  @override
  Widget build(c) => Padding(
    padding: EdgeInsets.all(MediaQuery.sizeOf(c).width < 600 ? 16 : 24),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: Theme.of(c).textTheme.headlineMedium),
                  Text(
                    subtitle,
                    style: const TextStyle(color: SocietyColors.textMuted),
                  ),
                ],
              ),
            ),
            if (onRefresh != null)
              IconButton(onPressed: onRefresh, icon: const Icon(Icons.refresh)),
          ],
        ),
        if (toolbar != null) ...[const SizedBox(height: 14), toolbar!],
        const SizedBox(height: 20),
        Expanded(child: child),
      ],
    ),
  );
}

class StatusChip extends StatelessWidget {
  const StatusChip(this.status, {super.key});
  final String status;
  @override
  Widget build(c) {
    final good = [
          'ACTIVE',
          'APPROVED',
          'PAID',
          'SUCCESS',
          'VERIFIED',
          'SENT',
          'RESOLVED',
          'CLOSED',
        ].contains(status),
        bad = ['REJECTED', 'FAILED', 'CANCELLED'].contains(status);
    return Chip(
      label: Text(status.replaceAll('_', ' ')),
      backgroundColor:
          (good
                  ? SocietyColors.success
                  : bad
                  ? SocietyColors.danger
                  : SocietyColors.warning)
              .withValues(alpha: .12),
      side: BorderSide.none,
    );
  }
}

class EmptyState extends StatelessWidget {
  const EmptyState(this.text, {super.key});
  final String text;
  @override
  Widget build(c) => Center(
    child: Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        const Icon(
          Icons.inbox_outlined,
          size: 50,
          color: SocietyColors.textMuted,
        ),
        const SizedBox(height: 10),
        Text(text, style: const TextStyle(color: SocietyColors.textMuted)),
      ],
    ),
  );
}

class FailureState extends StatelessWidget {
  const FailureState(this.text, {super.key, this.retry});
  final String text;
  final VoidCallback? retry;
  @override
  Widget build(c) => Center(
    child: Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(
          Icons.error_outline,
          size: 50,
          color: Theme.of(c).colorScheme.error,
        ),
        Text(text),
        if (retry != null)
          TextButton(onPressed: retry, child: const Text('Retry')),
      ],
    ),
  );
}
