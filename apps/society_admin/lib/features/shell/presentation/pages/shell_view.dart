import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';
import 'package:society_admin_app/app/dependency_injection.dart';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'package:society_admin_app/core/presentation/adapters/bloc_api_gateway.dart';
import 'package:society_admin_app/core/presentation/widgets/common.dart';
import 'package:society_admin_app/features/audit/presentation/bloc/audit_bloc.dart';
import 'package:society_admin_app/features/audit/presentation/pages/audit_view.dart';
import 'package:society_admin_app/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:society_admin_app/features/configuration/presentation/bloc/configuration_bloc.dart';
import 'package:society_admin_app/features/configuration/presentation/pages/configuration_view.dart';
import 'package:society_admin_app/features/dashboard/presentation/bloc/dashboard_bloc.dart';
import 'package:society_admin_app/features/dashboard/presentation/pages/dashboard_view.dart';
import 'package:society_admin_app/features/documents/presentation/bloc/documents_bloc.dart';
import 'package:society_admin_app/features/documents/presentation/pages/documents_view.dart';
import 'package:society_admin_app/features/operations/presentation/bloc/operations_bloc.dart';
import 'package:society_admin_app/features/operations/presentation/pages/operations_view.dart';
import 'package:society_admin_app/features/payments/presentation/bloc/payments_bloc.dart';
import 'package:society_admin_app/features/profile/presentation/bloc/profile_bloc.dart';
import 'package:society_admin_app/features/profile/presentation/pages/profile_view.dart';
import 'package:society_admin_app/features/residents/presentation/bloc/residents_bloc.dart';
import 'package:society_admin_app/features/residents/presentation/pages/residents_view.dart';
import 'package:society_admin_app/features/staff/presentation/bloc/staff_bloc.dart';
import 'package:society_admin_app/features/staff/presentation/pages/staff_view.dart';
import 'package:society_admin_app/features/storage/presentation/bloc/storage_bloc.dart';
import 'package:society_admin_app/features/storage/presentation/pages/storage_view.dart';
import 'package:society_admin_app/features/structure/presentation/bloc/structure_bloc.dart';
import 'package:society_admin_app/features/structure/presentation/pages/structure_view.dart';
import 'package:society_admin_app/features/support/presentation/bloc/support_bloc.dart';
import 'package:society_admin_app/features/support/presentation/pages/support_view.dart';
import 'package:society_admin_app/features/shell/domain/entities/admin_section.dart';
import '../bloc/navigation_bloc.dart';
import '../bloc/workspace_blocs.dart';

class ShellView extends StatelessWidget {
  const ShellView({
    super.key,
    required this.dependencies,
    required this.profile,
  });
  final SocietyAdminDependencies dependencies;
  final Map<String, dynamic> profile;
  @override
  Widget build(BuildContext context) => MultiBlocProvider(
    providers: [
      BlocProvider(create: (_) => NavigationBloc()),
      BlocProvider(create: (_) => DashboardBloc(dependencies.resources)),
      BlocProvider(create: (_) => ProfileBloc(dependencies.resources)),
      BlocProvider(create: (_) => StructureBloc(dependencies.resources)),
      BlocProvider(create: (_) => ResidentsBloc(dependencies.resources)),
      BlocProvider(create: (_) => StaffBloc(dependencies.resources)),
      BlocProvider(create: (_) => ConfigurationBloc(dependencies.resources)),
      BlocProvider(create: (_) => OperationsBloc(dependencies.resources)),
      BlocProvider(create: (_) => PaymentsBloc(dependencies.resources)),
      BlocProvider(create: (_) => DocumentsBloc(dependencies.resources)),
      BlocProvider(create: (_) => StorageBloc(dependencies.resources)),
      BlocProvider(create: (_) => SupportBloc(dependencies.resources)),
      BlocProvider(create: (_) => AuditBloc(dependencies.resources)),
      BlocProvider(
        create: (_) => PlatformMembershipBloc(dependencies.resources),
      ),
      BlocProvider(create: (_) => HouseholdsBloc(dependencies.resources)),
      BlocProvider(create: (_) => BillingBloc(dependencies.resources)),
      BlocProvider(create: (_) => ComplaintsBloc(dependencies.resources)),
      BlocProvider(create: (_) => AmenitiesBloc(dependencies.resources)),
      BlocProvider(create: (_) => ActivitiesBloc(dependencies.resources)),
      BlocProvider(create: (_) => DeliveriesBloc(dependencies.resources)),
      BlocProvider(create: (_) => EmergenciesBloc(dependencies.resources)),
      BlocProvider(create: (_) => NoticesBloc(dependencies.resources)),
      BlocProvider(create: (_) => TasksBloc(dependencies.resources)),
    ],
    child: _ShellLoader(dependencies: dependencies, profile: profile),
  );
}

class _ShellLoader extends StatefulWidget {
  const _ShellLoader({required this.dependencies, required this.profile});
  final SocietyAdminDependencies dependencies;
  final Map<String, dynamic> profile;
  @override
  State<_ShellLoader> createState() => _ShellLoaderState();
}

class _ShellLoaderState extends State<_ShellLoader> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => load());
  }

  void load() => context.read<ProfileBloc>().loadShellProfile();
  @override
  Widget build(
    BuildContext context,
  ) => BlocBuilder<ProfileBloc, AdminResourceState>(
    builder: (context, state) {
      final snapshot = state.resource('shell-profile');
      if (snapshot.status == ResourceStatus.initial ||
          snapshot.status == ResourceStatus.loading) {
        return const Scaffold(body: Center(child: CircularProgressIndicator()));
      }
      if (snapshot.error != null) {
        return Scaffold(
          body: ErrorState(message: snapshot.error!, onRetry: load),
        );
      }
      final profileDocument = snapshot.value as ResourceDocument;
      final raw = profileDocument.data['enabled_modules'];
      final modules = <String>{};
      try {
        final decoded = raw is List ? raw : jsonDecode(raw?.toString() ?? '[]');
        if (decoded is List) modules.addAll(decoded.map((e) => e.toString()));
      } catch (_) {}
      return _Shell(
        dependencies: widget.dependencies,
        profile: widget.profile,
        enabledModules: modules,
        onEntitlementsChanged: load,
        paymentRecovery:
            profileDocument.data['membership_access_allowed'] == false,
      );
    },
  );
}

class _Shell extends StatelessWidget {
  const _Shell({
    required this.dependencies,
    required this.profile,
    required this.enabledModules,
    required this.onEntitlementsChanged,
    required this.paymentRecovery,
  });
  final SocietyAdminDependencies dependencies;
  final Map<String, dynamic> profile;
  final Set<String> enabledModules;
  final VoidCallback onEntitlementsChanged;
  final bool paymentRecovery;
  @override
  Widget build(BuildContext c) {
    final section = paymentRecovery
            ? AdminSection.platformMembership
            : c.watch<NavigationBloc>().state,
        wide = MediaQuery.sizeOf(c).width > 900;
    void select(AdminSection s) {
      c.read<NavigationBloc>().add(Navigate(s));
      if (!wide) Navigator.pop(c);
    }

    final nav = ListView(
      padding: const EdgeInsets.all(12),
      children: [
        const Padding(
          padding: EdgeInsets.fromLTRB(8, 8, 8, 18),
          child: SocietyBrandMark(),
        ),
        ListTile(
          leading: const CircleAvatar(
            backgroundColor: SocietyColors.surfaceMuted,
            child: Icon(Icons.apartment_rounded, color: SocietyColors.blue),
          ),
          title: Text(profile['fullName']?.toString() ?? 'Society Admin'),
          subtitle: const Text(
            'SOCIETY ADMIN',
            style: TextStyle(
              color: SocietyColors.textMuted,
              fontSize: 10,
              fontWeight: FontWeight.w700,
              letterSpacing: 1,
            ),
          ),
        ),
        const SizedBox(height: 8),
        if (paymentRecovery)
          const Card(
            color: Color(0xfffff4e5),
            child: Padding(
              padding: EdgeInsets.all(12),
              child: Text(
                'Membership payment is required. Other society services are temporarily unavailable.',
              ),
            ),
          ),
        for (final s in AdminSection.values.where(
          (value) => paymentRecovery
              ? value == AdminSection.platformMembership
              : value.isAvailable(enabledModules),
        ))
          ListTile(
            selected: s == section,
            leading: Icon(s.icon),
            title: Text(s.label),
            onTap: () => select(s),
          ),
      ],
    );
    return Scaffold(
      appBar: AppBar(
        title: Text(section.label),
        actions: [
          IconButton(
            tooltip: 'Sign out',
            onPressed: () => c.read<AuthBloc>().add(const LogoutAuth()),
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
              child: DecoratedBox(
                decoration: const BoxDecoration(
                  color: Colors.white,
                  border: Border(
                    right: BorderSide(color: SocietyColors.border),
                  ),
                ),
                child: nav,
              ),
            ),
          Expanded(child: _content(c, section)),
        ],
      ),
    );
  }

  Widget _content(BuildContext context, AdminSection s) => switch (s) {
    AdminSection.dashboard => DashboardView(onNavigate: (_) => {}),
    AdminSection.profile => ProfileView(
      onEntitlementsChanged: onEntitlementsChanged,
    ),
    AdminSection.platformMembership => PlatformMembershipWorkspace(
      client: BlocApiGateway(context.read<PlatformMembershipBloc>()),
    ),
    AdminSection.structure => const StructureView(),
    AdminSection.residents => const ResidentsView(),
    AdminSection.households => HouseholdWorkspace(
      client: BlocApiGateway(context.read<HouseholdsBloc>()),
      role: HouseholdWorkspaceRole.societyAdmin,
    ),
    AdminSection.configuration => ConfigurationView(
      enabledModules: enabledModules,
    ),
    AdminSection.staff => const StaffView(),
    AdminSection.billing => SocietyExpenseWorkspace(
      client: BlocApiGateway(context.read<BillingBloc>()),
    ),
    AdminSection.payments => MaintenanceWorkspace(
      client: BlocApiGateway(context.read<PaymentsBloc>()),
      role: MaintenanceRole.societyAdmin,
    ),
    AdminSection.complaints => ComplaintWorkspace(
      client: BlocApiGateway(context.read<ComplaintsBloc>()),
      role: ComplaintWorkspaceRole.societyAdmin,
    ),
    AdminSection.visitors => const OperationsView(key: ValueKey('visitors')),
    AdminSection.amenities => AmenityWorkspace(
      client: BlocApiGateway(context.read<AmenitiesBloc>()),
      role: AmenityWorkspaceRole.societyAdmin,
    ),
    AdminSection.activities => ActivityWorkspace(
      client: BlocApiGateway(context.read<ActivitiesBloc>()),
      role: ActivityWorkspaceRole.societyAdmin,
    ),
    AdminSection.deliveries => DeliveryWorkspace(
      client: BlocApiGateway(context.read<DeliveriesBloc>()),
      role: DeliveryWorkspaceRole.societyAdmin,
    ),
    AdminSection.emergencies => EmergencyWorkspace(
      client: BlocApiGateway(context.read<EmergenciesBloc>()),
      role: EmergencyWorkspaceRole.societyAdmin,
    ),
    AdminSection.notices => NoticeWorkspace(
      client: BlocApiGateway(context.read<NoticesBloc>()),
      role: NoticeWorkspaceRole.societyAdmin,
    ),
    AdminSection.documents => const DocumentsView(),
    AdminSection.tasks => TaskWorkspace(
      client: BlocApiGateway(context.read<TasksBloc>()),
      role: TaskWorkspaceRole.societyAdmin,
    ),
    AdminSection.audit => const AuditView(),
    AdminSection.storage => const StorageView(),
    AdminSection.support => const SupportView(),
  };
}
