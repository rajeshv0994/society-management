import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'package:society_admin_app/core/presentation/widgets/common.dart';
import 'package:society_admin_app/features/shell/domain/entities/admin_section.dart';
import '../bloc/dashboard_bloc.dart';

class DashboardView extends StatefulWidget {
  const DashboardView({super.key, required this.onNavigate});
  final ValueChanged<AdminSection> onNavigate;
  @override
  State<DashboardView> createState() => _DashboardState();
}

class _DashboardState extends State<DashboardView> {
  @override
  void initState() {
    super.initState();
    context.read<DashboardBloc>().loadDashboard();
  }

  @override
  Widget build(c) => PageFrame(
    title: 'Society dashboard',
    subtitle: 'Live operational metrics',
    child: BlocBuilder<DashboardBloc, AdminResourceState>(
      builder: (c, state) {
        final snapshot = state.resource('dashboard');
        if (snapshot.status == ResourceStatus.initial ||
            snapshot.status == ResourceStatus.loading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.error != null) {
          return ErrorState(
            message: snapshot.error!,
            onRetry: context.read<DashboardBloc>().loadDashboard,
          );
        }
        final m = (snapshot.value as ResourceDocument).data;
        return GridView.extent(
          maxCrossAxisExtent: 260,
          mainAxisSpacing: 12,
          crossAxisSpacing: 12,
          childAspectRatio: 1.7,
          children: [
            for (final e in m.entries)
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(18),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(e.key.replaceAll('_', ' ').toUpperCase()),
                      const Spacer(),
                      Text(
                        e.value.toString(),
                        style: Theme.of(c).textTheme.headlineMedium,
                      ),
                    ],
                  ),
                ),
              ),
          ],
        );
      },
    ),
  );
}
