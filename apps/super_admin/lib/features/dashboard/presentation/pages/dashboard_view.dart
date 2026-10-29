import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:super_admin_app/core/presentation/widgets/common_widgets.dart';
import 'package:super_admin_app/features/shell/presentation/bloc/navigation_bloc.dart';
import 'package:super_admin_app/features/shell/domain/entities/admin_section.dart';
import '../bloc/dashboard_bloc.dart';

class DashboardView extends StatelessWidget {
  const DashboardView({super.key});
  NavigationRequested target(String key) => switch (key) {
    'pending_approvals' => const NavigationRequested(
      AdminSection.societies,
      societyStatus: 'PENDING_VERIFICATION',
    ),
    'total_societies' ||
    'active_societies' ||
    'active_members' => const NavigationRequested(AdminSection.societies),
    'open_tickets' => const NavigationRequested(AdminSection.support),
    'open_requirements' => const NavigationRequested(AdminSection.requirements),
    'failed_notifications' => const NavigationRequested(
      AdminSection.notifications,
    ),
    'monthly_plan_value' => const NavigationRequested(AdminSection.billing),
    _ => const NavigationRequested(AdminSection.societies),
  };
  @override
  Widget build(c) => PageFrame(
    title: 'Platform overview',
    subtitle: 'Click any metric to open its live records',
    onRefresh: () => c.read<DashboardBloc>().add(const DashboardLoaded()),
    child: BlocBuilder<DashboardBloc, DashboardState>(
      builder: (context, state) => switch (state) {
        DashboardLoading() => const Center(child: CircularProgressIndicator()),
        DashboardFailure(:final message) => FailureState(
          message,
          retry: () =>
              context.read<DashboardBloc>().add(const DashboardLoaded()),
        ),
        DashboardReady(:final metrics) => GridView.extent(
          maxCrossAxisExtent: 280,
          mainAxisSpacing: 14,
          crossAxisSpacing: 14,
          childAspectRatio: 1.6,
          children: [
            for (final e in metrics.values.entries)
              _MetricCard(
                label: e.key,
                value: e.value.toString(),
                onTap: () => context.read<NavigationBloc>().add(target(e.key)),
              ),
          ],
        ),
      },
    ),
  );
}

class _MetricCard extends StatelessWidget {
  const _MetricCard({
    required this.label,
    required this.value,
    required this.onTap,
  });
  final String label, value;
  final VoidCallback onTap;
  @override
  Widget build(c) => Card(
    clipBehavior: Clip.antiAlias,
    child: InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    label.replaceAll('_', ' ').toUpperCase(),
                    style: Theme.of(c).textTheme.labelMedium,
                  ),
                ),
                const Icon(Icons.arrow_forward),
              ],
            ),
            const Spacer(),
            Text(
              value,
              style: Theme.of(
                c,
              ).textTheme.headlineMedium?.copyWith(fontWeight: FontWeight.bold),
            ),
          ],
        ),
      ),
    ),
  );
}
