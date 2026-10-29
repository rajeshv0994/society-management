import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'api_client.dart';
import 'app_failure.dart';
import 'async_data_bloc.dart';
import 'auth_fields.dart';
import 'portal_bloc.dart';
import 'portal_models.dart';
import 'portal_repository.dart';
import 'session_manager.dart';
import 'theme.dart';

extension PortalRoleDetails on PortalRole {
  String get apiRole => switch (this) {
    PortalRole.superAdmin => 'SUPER_ADMIN',
    PortalRole.societyAdmin => 'SOCIETY_ADMIN',
    PortalRole.staff => 'STAFF',
    PortalRole.member => 'RESIDENT',
  };
  String get title => switch (this) {
    PortalRole.superAdmin => 'Platform Control',
    PortalRole.societyAdmin => 'Society Administration',
    PortalRole.staff => 'Staff Workspace',
    PortalRole.member => 'Member Home',
  };
  IconData get icon => switch (this) {
    PortalRole.superAdmin => Icons.admin_panel_settings,
    PortalRole.societyAdmin => Icons.apartment,
    PortalRole.staff => Icons.badge,
    PortalRole.member => Icons.home,
  };
}

class PortalModule {
  const PortalModule(
    this.label,
    this.subtitle,
    this.icon, {
    this.resource,
    this.pageBuilder,
    this.permissionCode,
    this.serviceCode,
  });
  final String label;
  final String subtitle;
  final IconData icon;
  final PortalModuleResource? resource;
  final Widget Function(BlocManagedApiGateway client)? pageBuilder;
  final String? permissionCode;
  final String? serviceCode;
}

List<PortalModule> portalModulesForAccess(
  List<PortalModule> modules, {
  required Set<String> permissions,
  Set<String>? enabledServices,
}) => modules
    .where(
      (module) =>
          (module.serviceCode == null ||
              enabledServices == null ||
              enabledServices.contains(module.serviceCode)) &&
          (module.permissionCode == null ||
              permissions.contains(module.permissionCode)),
    )
    .toList();

class SocietyPortalApp extends StatelessWidget {
  const SocietyPortalApp({
    super.key,
    required this.role,
    required this.modules,
    required this.brandColor,
  });
  final PortalRole role;
  final List<PortalModule> modules;
  final Color brandColor;

  @override
  Widget build(BuildContext context) => MaterialApp(
    title: 'SocietyHub ${role.title}',
    debugShowCheckedModeBanner: false,
    theme: SocietyTheme.light(accent: brandColor),
    home: _PortalGate(role: role, modules: modules),
  );
}

class _PortalGate extends StatelessWidget {
  const _PortalGate({required this.role, required this.modules});
  final PortalRole role;
  final List<PortalModule> modules;

  @override
  Widget build(BuildContext context) => BlocProvider(
    create: (_) {
      final sessions = SessionManager(
        storageKey: role.apiRole,
        expectedRole: role.apiRole,
      );
      return PortalAuthBloc(
        role: role,
        sessions: sessions,
        repository: PortalRepository(HttpApiClient()),
      )..add(const PortalAuthStarted());
    },
    child: _PortalGateContent(role: role, modules: modules),
  );
}

class _PortalGateContent extends StatelessWidget {
  const _PortalGateContent({required this.role, required this.modules});
  final PortalRole role;
  final List<PortalModule> modules;

  @override
  Widget build(BuildContext context) =>
      BlocBuilder<PortalAuthBloc, PortalAuthState>(
        builder: (context, state) {
          if (state.status == PortalAuthStatus.restoring) {
            return const Scaffold(
              body: Center(child: CircularProgressIndicator()),
            );
          }
          if (!state.authenticated) return _Login(role: role);
          final bloc = context.read<PortalAuthBloc>();
          return _Dashboard(
            role: role,
            modules: modules,
            client: bloc.authenticatedApi,
            profile: state.session?.toMap(),
            onLogout: () => bloc.add(const PortalLogoutRequested()),
          );
        },
      );
}

class _Login extends StatefulWidget {
  const _Login({required this.role});
  final PortalRole role;
  @override
  State<_Login> createState() => _LoginState();
}

class _LoginState extends State<_Login> {
  final email = TextEditingController();
  final password = TextEditingController();
  final societyCode = TextEditingController();

  void submit() => context.read<PortalAuthBloc>().add(
    PortalLoginSubmitted(
      email: email.text,
      password: password.text,
      societyCode: societyCode.text,
    ),
  );

  @override
  void dispose() {
    email.dispose();
    password.dispose();
    societyCode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) =>
      BlocBuilder<PortalAuthBloc, PortalAuthState>(
        builder: (context, state) => Scaffold(
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
                            width: 58,
                            height: 58,
                            decoration: BoxDecoration(
                              color: Theme.of(context)
                                  .extension<SocietyRoleTheme>()!
                                  .accent
                                  .withValues(alpha: .1),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              widget.role.icon,
                              size: 29,
                              color: Theme.of(
                                context,
                              ).extension<SocietyRoleTheme>()!.accent,
                            ),
                          ),
                          const SizedBox(height: 18),
                          Text(
                            widget.role.title,
                            style: Theme.of(context).textTheme.headlineSmall,
                            textAlign: TextAlign.center,
                          ),
                          const SizedBox(height: 7),
                          const Text(
                            'Sign in to your dedicated SocietyHub workspace',
                            textAlign: TextAlign.center,
                            style: TextStyle(color: SocietyColors.textMuted),
                          ),
                          const SizedBox(height: 28),
                          if (widget.role != PortalRole.superAdmin) ...[
                            if (state.societies.isNotEmpty)
                              DropdownButtonFormField<String>(
                                isExpanded: true,
                                decoration: const InputDecoration(
                                  labelText: 'Select society',
                                  prefixIcon: Icon(Icons.apartment_outlined),
                                ),
                                items: state.societies
                                    .map(
                                      (society) => DropdownMenuItem(
                                        value: society.code,
                                        child: Text(
                                          '${society.name} (${society.code})',
                                          maxLines: 1,
                                          overflow: TextOverflow.ellipsis,
                                        ),
                                      ),
                                    )
                                    .toList(),
                                onChanged: (value) {
                                  if (value != null) societyCode.text = value;
                                },
                              ),
                            if (state.societies.isNotEmpty)
                              const SizedBox(height: 10),
                            TextField(
                              controller: societyCode,
                              textCapitalization: TextCapitalization.characters,
                              decoration: const InputDecoration(
                                labelText: 'Society code',
                                hintText: 'Example: GREEN-VALLEY',
                                prefixIcon: Icon(Icons.tag),
                              ),
                            ),
                            const SizedBox(height: 14),
                          ],
                          SocietyEmailField(controller: email),
                          const SizedBox(height: 14),
                          SocietyPasswordField(
                            controller: password,
                            onSubmitted: (_) => submit(),
                          ),
                          const SizedBox(height: 20),
                          FilledButton(
                            onPressed:
                                state.status == PortalAuthStatus.submitting
                                ? null
                                : submit,
                            child: Text(
                              state.status == PortalAuthStatus.submitting
                                  ? 'Signing in…'
                                  : 'Sign in securely',
                            ),
                          ),
                          if (state.error != null)
                            Padding(
                              padding: const EdgeInsets.only(top: 14),
                              child: Text(
                                state.error!,
                                textAlign: TextAlign.center,
                                style: TextStyle(
                                  color: Theme.of(context).colorScheme.error,
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
        ),
      );
}

class _Dashboard extends StatefulWidget {
  const _Dashboard({
    required this.role,
    required this.modules,
    required this.client,
    required this.profile,
    required this.onLogout,
  });
  final PortalRole role;
  final List<PortalModule> modules;
  final BlocManagedApiGateway client;
  final Map<String, dynamic>? profile;
  final VoidCallback onLogout;
  @override
  State<_Dashboard> createState() => _DashboardState();
}

class _DashboardState extends State<_Dashboard> {
  int selected = 0;
  late final AsyncDataBloc<PortalAccess> _accessBloc;

  @override
  void initState() {
    super.initState();
    _accessBloc = AsyncDataBloc(
      () => context.read<PortalAuthBloc>().repository.loadAccess(
        widget.role,
        widget.client,
      ),
    )..refresh();
  }

  @override
  void dispose() {
    _accessBloc.close();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) =>
      BlocBuilder<AsyncDataBloc<PortalAccess>, AsyncDataState<PortalAccess>>(
        bloc: _accessBloc,
        builder: (context, state) {
          if (state.data == null && state.status != AsyncDataStatus.failure) {
            return const Scaffold(
              body: Center(child: CircularProgressIndicator()),
            );
          }
          if (state.status == AsyncDataStatus.failure && state.data == null) {
            return Scaffold(
              appBar: AppBar(
                title: Text(widget.role.title),
                actions: [
                  IconButton(
                    tooltip: 'Sign out',
                    onPressed: widget.onLogout,
                    icon: const Icon(Icons.logout),
                  ),
                ],
              ),
              body: _EmptyState(
                icon: Icons.cloud_off_outlined,
                text: userFacingError(state.error!),
                actionLabel: 'Try again',
                onAction: _accessBloc.refresh,
              ),
            );
          }
          final access = state.data ?? const PortalAccess();
          final wide = MediaQuery.sizeOf(context).width >= 760;
          final modules = portalModulesForAccess(
            widget.modules,
            permissions: access.permissions,
            enabledServices: access.enabledServices,
          );
          final selectedIndex = selected < modules.length ? selected : 0;
          final module = modules[selectedIndex];
          final content =
              module.pageBuilder?.call(widget.client) ??
              _ModulePage(module: module, client: widget.client);
          return Scaffold(
            appBar: AppBar(
              title: Text(widget.role.title),
              actions: [
                IconButton(
                  tooltip: 'Sign out',
                  onPressed: widget.onLogout,
                  icon: const Icon(Icons.logout),
                ),
              ],
            ),
            drawer: wide
                ? null
                : Drawer(
                    child: SafeArea(
                      child: _Navigation(
                        modules: modules,
                        selected: selectedIndex,
                        onSelect: (i) {
                          setState(() => selected = i);
                          Navigator.pop(context);
                        },
                      ),
                    ),
                  ),
            body: Row(
              children: [
                if (wide)
                  SizedBox(
                    width: 260,
                    child: Material(
                      color: Colors.white,
                      shape: const Border(
                        right: BorderSide(color: SocietyColors.border),
                      ),
                      child: _Navigation(
                        modules: modules,
                        selected: selectedIndex,
                        onSelect: (i) => setState(() => selected = i),
                      ),
                    ),
                  ),
                Expanded(child: content),
              ],
            ),
          );
        },
      );
}

class _Navigation extends StatelessWidget {
  const _Navigation({
    required this.modules,
    required this.selected,
    required this.onSelect,
  });
  final List<PortalModule> modules;
  final int selected;
  final ValueChanged<int> onSelect;
  @override
  Widget build(BuildContext context) => ListView(
    padding: const EdgeInsets.all(12),
    children: [
      Padding(
        padding: const EdgeInsets.fromLTRB(10, 8, 10, 20),
        child: const SocietyBrandMark(),
      ),
      for (var i = 0; i < modules.length; i++)
        ListTile(
          selected: i == selected,
          leading: Icon(modules[i].icon),
          title: Text(modules[i].label),
          onTap: () => onSelect(i),
        ),
    ],
  );
}

class _ModulePage extends StatefulWidget {
  const _ModulePage({required this.module, required this.client});
  final PortalModule module;
  final BlocManagedApiGateway client;

  @override
  State<_ModulePage> createState() => _ModulePageState();
}

class _ModulePageState extends State<_ModulePage> {
  late final AsyncDataBloc<Object?> _moduleBloc;

  @override
  void initState() {
    super.initState();
    _moduleBloc = AsyncDataBloc(
      () => context.read<PortalAuthBloc>().repository.loadModule(
        widget.client,
        widget.module.resource!,
      ),
    );
    if (widget.module.resource != null) _moduleBloc.refresh();
  }

  @override
  void dispose() {
    _moduleBloc.close();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Padding(
    padding: EdgeInsets.all(MediaQuery.sizeOf(context).width < 600 ? 16 : 24),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Text(
          widget.module.label,
          style: Theme.of(context).textTheme.headlineMedium,
        ),
        Text(widget.module.subtitle),
        const SizedBox(height: 24),
        Expanded(
          child: widget.module.resource == null
              ? _EmptyState(
                  icon: widget.module.icon,
                  text:
                      'This workspace is ready for the ${widget.module.label.toLowerCase()} workflow.',
                )
              : BlocBuilder<AsyncDataBloc<Object?>, AsyncDataState<Object?>>(
                  bloc: _moduleBloc,
                  builder: (context, state) {
                    if (state.data == null &&
                        state.status != AsyncDataStatus.failure) {
                      return const Center(child: CircularProgressIndicator());
                    }
                    if (state.status == AsyncDataStatus.failure &&
                        state.data == null) {
                      return _EmptyState(
                        icon: Icons.error_outline,
                        text: userFacingError(state.error!),
                        actionLabel: 'Try again',
                        onAction: _moduleBloc.refresh,
                      );
                    }
                    final data = state.data;
                    if (data is Map) {
                      return GridView.count(
                        crossAxisCount: MediaQuery.sizeOf(context).width > 800
                            ? 3
                            : 2,
                        mainAxisSpacing: 12,
                        crossAxisSpacing: 12,
                        childAspectRatio: 1.8,
                        children: data.entries.map((entry) {
                          final value = entry.value;
                          final display = value is List
                              ? '${value.length} records'
                              : value is Map
                              ? '${value.length} records'
                              : value.toString();
                          return Card(
                            child: Padding(
                              padding: const EdgeInsets.all(16),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    entry.key
                                        .toString()
                                        .replaceAll('_', ' ')
                                        .toUpperCase(),
                                    style: Theme.of(
                                      context,
                                    ).textTheme.labelMedium,
                                  ),
                                  const Spacer(),
                                  Text(
                                    display,
                                    style: Theme.of(
                                      context,
                                    ).textTheme.headlineSmall,
                                  ),
                                ],
                              ),
                            ),
                          );
                        }).toList(),
                      );
                    }
                    final items = data is List ? data : <dynamic>[];
                    if (items.isEmpty)
                      return const _EmptyState(
                        icon: Icons.inbox_outlined,
                        text: 'Nothing needs attention right now.',
                      );
                    return ListView.separated(
                      itemCount: items.length,
                      separatorBuilder: (_, _) => const SizedBox(height: 8),
                      itemBuilder: (context, index) {
                        final item = items[index] as Map;
                        return Card(
                          child: ListTile(
                            title: Text(
                              (item['name'] ??
                                      item['full_name'] ??
                                      item['registration_number'] ??
                                      'Record')
                                  .toString(),
                            ),
                            subtitle: Text(
                              (item['status'] ?? item['city'] ?? '').toString(),
                            ),
                            trailing: const Icon(Icons.chevron_right),
                          ),
                        );
                      },
                    );
                  },
                ),
        ),
      ],
    ),
  );
}

class _EmptyState extends StatelessWidget {
  const _EmptyState({
    required this.icon,
    required this.text,
    this.actionLabel,
    this.onAction,
  });
  final IconData icon;
  final String text;
  final String? actionLabel;
  final VoidCallback? onAction;
  @override
  Widget build(BuildContext context) => Center(
    child: Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 54, color: Theme.of(context).colorScheme.primary),
        const SizedBox(height: 12),
        Text(text, textAlign: TextAlign.center),
        if (onAction != null) ...[
          const SizedBox(height: 16),
          FilledButton.icon(
            onPressed: onAction,
            icon: const Icon(Icons.refresh),
            label: Text(actionLabel ?? 'Retry'),
          ),
        ],
      ],
    ),
  );
}
