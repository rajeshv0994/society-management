import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';
import 'package:super_admin_app/app/dependency_injection.dart';
import 'package:super_admin_app/features/audit/presentation/bloc/audit_bloc.dart';
import 'package:super_admin_app/features/audit/presentation/pages/audit_view.dart';
import 'package:super_admin_app/features/authentication/presentation/bloc/auth_bloc.dart';
import 'package:super_admin_app/features/billing/presentation/bloc/billing_bloc.dart';
import 'package:super_admin_app/features/billing/presentation/pages/subscription_billing_view.dart';
import 'package:super_admin_app/features/dashboard/presentation/bloc/dashboard_bloc.dart';
import 'package:super_admin_app/features/dashboard/presentation/pages/dashboard_view.dart';
import 'package:super_admin_app/features/management/presentation/bloc/management_bloc.dart';
import 'package:super_admin_app/features/management/presentation/pages/management_view.dart';
import 'package:super_admin_app/features/management/domain/entities/management_record.dart';
import 'package:super_admin_app/features/requirements/presentation/bloc/requirements_bloc.dart';
import 'package:super_admin_app/features/requirements/presentation/pages/requirements_view.dart';
import 'package:super_admin_app/features/services/presentation/bloc/service_catalog_bloc.dart';
import 'package:super_admin_app/features/services/presentation/pages/services_view.dart';
import 'package:super_admin_app/features/societies/presentation/bloc/societies_bloc.dart';
import 'package:super_admin_app/features/societies/presentation/bloc/society_operations_bloc.dart';
import 'package:super_admin_app/features/societies/presentation/pages/societies_view.dart';
import 'package:super_admin_app/features/storage/presentation/bloc/storage_bloc.dart';
import 'package:super_admin_app/features/storage/presentation/pages/storage_view.dart';
import 'package:super_admin_app/features/support/presentation/bloc/support_bloc.dart';
import 'package:super_admin_app/features/support/presentation/pages/support_view.dart';
import '../bloc/navigation_bloc.dart';
import '../../domain/entities/admin_section.dart';

extension AdminSectionPresentation on AdminSection {
  String get label => switch (this) {
    AdminSection.dashboard => 'Dashboard',
    AdminSection.societies => 'Societies',
    AdminSection.services => 'Service catalog',
    AdminSection.plans => 'Subscription rates',
    AdminSection.requirements => 'Custom requirements',
    AdminSection.billing => 'Billing',
    AdminSection.notifications => 'Notifications',
    AdminSection.support => 'Support',
    AdminSection.audit => 'Audit log',
    AdminSection.storage => 'Storage management',
  };
  IconData get icon => switch (this) {
    AdminSection.dashboard => Icons.dashboard,
    AdminSection.societies => Icons.apartment,
    AdminSection.services => Icons.extension,
    AdminSection.plans => Icons.price_change,
    AdminSection.requirements => Icons.contact_support,
    AdminSection.billing => Icons.payments,
    AdminSection.notifications => Icons.notifications_active,
    AdminSection.support => Icons.support_agent,
    AdminSection.audit => Icons.manage_search,
    AdminSection.storage => Icons.cloud_outlined,
  };
}

class AdminShellView extends StatelessWidget {
  const AdminShellView({super.key, required this.dependencies});
  final SuperAdminDependencies dependencies;
  @override
  Widget build(c) => MultiBlocProvider(
    providers: [
      BlocProvider(create: (_) => NavigationBloc()),
      BlocProvider(
        create: (_) =>
            DashboardBloc(dependencies.dashboard)..add(const DashboardLoaded()),
      ),
      BlocProvider(
        create: (_) =>
            SocietiesBloc(dependencies.societies)..add(const SocietiesLoaded()),
      ),
      BlocProvider(
        create: (_) =>
            SupportBloc(dependencies.support)..add(const SupportLoaded()),
      ),
      BlocProvider(
        create: (_) => SocietyOperationsBloc(dependencies.societies),
      ),
      BlocProvider(create: (_) => ServiceCatalogBloc(dependencies.services)),
      BlocProvider(create: (_) => StorageBloc(dependencies.storage)),
      BlocProvider(create: (_) => RequirementsBloc(dependencies.requirements)),
      BlocProvider(create: (_) => BillingBloc(dependencies.billing)),
      BlocProvider(create: (_) => ManagementBloc(dependencies.management)),
      BlocProvider(create: (_) => AuditBloc(dependencies.audit)),
    ],
    child: const _Shell(),
  );
}

class _Shell extends StatelessWidget {
  const _Shell();
  @override
  Widget build(c) {
    final state = c.watch<NavigationBloc>().state,
        wide = MediaQuery.sizeOf(c).width >= 900;
    void select(AdminSection s) {
      c.read<NavigationBloc>().add(NavigationRequested(s));
      if (!wide) Navigator.pop(c);
    }

    final nav = ListView(
      padding: const EdgeInsets.all(14),
      children: [
        const Padding(
          padding: EdgeInsets.fromLTRB(8, 6, 8, 22),
          child: SocietyBrandMark(),
        ),
        for (final s in AdminSection.values)
          ListTile(
            selected: s == state.section,
            leading: Icon(s.icon),
            title: Text(s.label),
            onTap: () => select(s),
          ),
      ],
    );
    final content = switch (state.section) {
      AdminSection.dashboard => const DashboardView(),
      AdminSection.societies => SocietiesView(
        initialStatus: state.societyStatus,
      ),
      AdminSection.services => const ServicesView(),
      AdminSection.plans => ManagementView(
        key: const ValueKey('plans'),
        title: 'Subscription rates',
        resource: ManagementResource.plans,
        titleKeys: const ['name'],
        subtitleKeys: const [
          'approval_mode',
          'price_per_space',
          'max_spaces',
          'price_per_staff',
          'max_staff',
          'trial_duration_value',
          'trial_duration_unit',
          'society_count',
        ],
        action: 'editPlan',
      ),
      AdminSection.requirements => const RequirementsView(),
      AdminSection.billing => const SubscriptionBillingView(),
      AdminSection.notifications => ManagementView(
        key: const ValueKey('notifications'),
        title: 'Notification outbox',
        resource: ManagementResource.notifications,
        titleKeys: const ['template_code', 'destination'],
        subtitleKeys: const ['channel', 'status', 'society_name'],
        action: 'retry',
      ),
      AdminSection.support => const SupportView(),
      AdminSection.audit => const AuditView(key: ValueKey('audit')),
      AdminSection.storage => const StorageView(),
    };
    return Scaffold(
      appBar: AppBar(
        title: Text(state.section.label),
        actions: [
          IconButton(
            tooltip: 'Sign out',
            onPressed: () => c.read<AuthBloc>().add(const LogoutRequested()),
            icon: const Icon(Icons.logout_rounded),
          ),
        ],
      ),
      drawer: wide ? null : Drawer(child: SafeArea(child: nav)),
      body: Row(
        children: [
          if (wide)
            SizedBox(
              width: 270,
              child: Material(
                color: Colors.white,
                shape: const Border(
                  right: BorderSide(color: SocietyColors.border),
                ),
                child: nav,
              ),
            ),
          Expanded(child: content),
        ],
      ),
    );
  }
}
