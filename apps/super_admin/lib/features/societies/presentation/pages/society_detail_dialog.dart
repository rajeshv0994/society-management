import 'package:flutter/material.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:super_admin_app/core/presentation/widgets/common_widgets.dart';
import '../../domain/entities/society_services_parser.dart';
import '../../domain/entities/society_summary.dart';
import '../bloc/society_operations_bloc.dart';

class SocietyDetailDialog extends StatefulWidget {
  const SocietyDetailDialog({super.key, required this.societyId});
  final String societyId;
  @override
  State<SocietyDetailDialog> createState() => _SocietyDetailDialogState();
}

class _SocietyDetailDialogState extends State<SocietyDetailDialog>
    with SingleTickerProviderStateMixin {
  late TabController tabs;
  @override
  void initState() {
    super.initState();
    tabs = TabController(length: 7, vsync: this);
  }

  void load() => context.read<SocietyOperationsBloc>().add(
    SocietyDetailLoaded(widget.societyId),
  );
  Future<void> edit(Map society) async {
    final workspace = context.read<SocietyOperationsBloc>().state.data;
    if (workspace is! SocietyDetailWorkspace) return;
    final services = workspace.services
        .map<Map<String, dynamic>>((item) => item.data)
        .toList(growable: false);
    final activeServices = services
        .where((service) => service['status'] == 'ACTIVE')
        .toList();
    if (!mounted) return;
    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => _EditSocietyDialog(
        society: society,
        services: activeServices.toList(),
      ),
    );
    if (result != null) {
      if (!mounted) return;
      context.read<SocietyOperationsBloc>().add(
        SocietyUpdated(widget.societyId, result),
      );
    }
  }

  Future<void> uploadLogo() async {
    final picked = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: const ['png', 'jpg', 'jpeg'],
      withData: true,
    );
    final file = picked?.files.single;
    if (file?.bytes == null) return;
    if (!mounted) return;
    context.read<SocietyOperationsBloc>().add(
      SocietyLogoUploaded(widget.societyId, file!.bytes!, file.name),
    );
  }

  @override
  Widget build(c) => Dialog.fullscreen(
    child: BlocConsumer<SocietyOperationsBloc, SocietyOperationsState>(
      listener: (context, state) {
        if (state.feature == SocietyOperationFeature.detail &&
            state.error != null) {
          ScaffoldMessenger.of(
            context,
          ).showSnackBar(SnackBar(content: Text(state.error!)));
        }
      },
      builder: (context, state) {
        if (state.feature != SocietyOperationFeature.detail || state.loading) {
          return const Center(child: CircularProgressIndicator());
        }
        final operationsData = state.data;
        if (operationsData is! SocietyDetailWorkspace) {
          return FailureState(
            state.error ?? 'Society details are unavailable.',
            retry: load,
          );
        }
        final data = operationsData.detail.data;
        final society = data['society'] as Map;
        return Scaffold(
          appBar: AppBar(
            leading: IconButton(
              onPressed: () => Navigator.pop(context),
              icon: const Icon(Icons.close),
            ),
            title: Text(society['name'].toString()),
            actions: [
              StatusChip(society['status'].toString()),
              const SizedBox(width: 8),
              FilledButton.tonalIcon(
                onPressed: () => edit(society),
                icon: const Icon(Icons.edit),
                label: const Text('Manage'),
              ),
              const SizedBox(width: 8),
              FilledButton.tonalIcon(
                onPressed: uploadLogo,
                icon: const Icon(Icons.image),
                label: const Text('Change logo'),
              ),
              const SizedBox(width: 12),
            ],
            bottom: TabBar(
              controller: tabs,
              isScrollable: true,
              tabs: const [
                Tab(text: 'Overview'),
                Tab(text: 'Structure'),
                Tab(text: 'Users'),
                Tab(text: 'Documents'),
                Tab(text: 'Plans & billing'),
                Tab(text: 'Support'),
                Tab(text: 'Related activity'),
              ],
            ),
          ),
          body: TabBarView(
            controller: tabs,
            children: [
              _overview(
                society,
                data['household_summary'] as Map,
                data['delivery_summary'] as Map,
                data['emergency_summary'] as Map,
                data['activity_summary'] as Map,
                operationsData.baseUrl,
              ),
              _structure(data['structure'] as Map),
              _records(
                data['users'] as List,
                ['full_name', 'role'],
                ['email', 'mobile', 'status'],
              ),
              _records(
                data['documents'] as List,
                ['document_type'],
                ['verification_status', 'storage_key'],
              ),
              _plans(data),
              _records(
                data['support'] as List,
                ['subject'],
                ['priority', 'status', 'created_at'],
              ),
              _relatedActivity(data),
            ],
          ),
        );
      },
    ),
  );
  Widget _overview(
    Map s,
    Map household,
    Map deliveries,
    Map emergencies,
    Map activities,
    String baseUrl,
  ) => ListView(
    padding: const EdgeInsets.all(24),
    children: [
      Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          CircleAvatar(
            radius: 50,
            backgroundImage: s['logo_url'] == null
                ? null
                : NetworkImage('$baseUrl${s['logo_url']}'),
            child: s['logo_url'] == null
                ? const Icon(Icons.apartment, size: 42)
                : null,
          ),
          const SizedBox(width: 20),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  s['name'].toString(),
                  style: Theme.of(context).textTheme.headlineSmall,
                ),
                const SizedBox(height: 6),
                Text(s['description']?.toString() ?? 'No society description.'),
              ],
            ),
          ),
        ],
      ),
      const SizedBox(height: 20),
      Wrap(
        spacing: 12,
        runSpacing: 12,
        children: [
          _info('Registration', s['registration_number']),
          _info('Type', s['society_type']),
          _info('Subscription', s['subscription_status']),
          _info('Contact', s['contact_person']),
          _info('Email', s['email']),
          _info('Mobile', s['mobile']),
          _info('Website', s['website']),
          _info(
            'Services provided',
            societyServicesFrom(s['enabled_modules']).isEmpty
                ? 'None selected'
                : societyServicesFrom(
                    s['enabled_modules'],
                  ).map((value) => value.replaceAll('_', ' ')).join(', '),
          ),
          _info('Location', '${s['city']}, ${s['state']} ${s['pin_code']}'),
          _info('Setup complete', s['setup_completed']),
        ],
      ),
      const SizedBox(height: 20),
      Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Text(s['address'].toString()),
        ),
      ),
      const SizedBox(height: 20),
      Text(
        'Household operations',
        style: Theme.of(context).textTheme.titleLarge,
      ),
      const SizedBox(height: 8),
      Wrap(
        spacing: 12,
        runSpacing: 12,
        children: [
          _info('Households', household['households']),
          _info('Family members', household['family_members']),
          _info('Vehicles', household['vehicles']),
          _info('Emergency contacts', household['emergency_contacts']),
          _info('Pending verification', household['pending_verification']),
        ],
      ),
      const SizedBox(height: 20),
      Text(
        'Delivery operations',
        style: Theme.of(context).textTheme.titleLarge,
      ),
      const SizedBox(height: 8),
      Wrap(
        spacing: 12,
        runSpacing: 12,
        children: [
          _info('Total deliveries', deliveries['total']),
          _info('Active deliveries', deliveries['active']),
        ],
      ),
      const SizedBox(height: 20),
      Text(
        'Emergency operations',
        style: Theme.of(context).textTheme.titleLarge,
      ),
      const SizedBox(height: 8),
      Wrap(
        spacing: 12,
        runSpacing: 12,
        children: [
          _info('Total incidents', emergencies['total']),
          _info('Open incidents', emergencies['open']),
          _info('Critical incidents', emergencies['critical']),
        ],
      ),
      const SizedBox(height: 20),
      Text(
        'Activity operations',
        style: Theme.of(context).textTheme.titleLarge,
      ),
      const SizedBox(height: 8),
      Wrap(
        spacing: 12,
        runSpacing: 12,
        children: [
          _info('Total activities', activities['total']),
          _info('Published activities', activities['published']),
          _info('Participations', activities['participations']),
          _info('Published photos', activities['published_photos']),
          _info('Photos awaiting review', activities['pending_photos']),
        ],
      ),
    ],
  );
  Widget _info(String label, Object? value) => SizedBox(
    width: 260,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(label.toUpperCase(), style: const TextStyle(fontSize: 11)),
            const SizedBox(height: 5),
            Text(
              value?.toString() ?? '-',
              style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w600),
            ),
          ],
        ),
      ),
    ),
  );
  Widget _structure(Map values) {
    final hierarchy = (values['hierarchy'] as List?) ?? const [];
    final counts = Map.from(values)..remove('hierarchy');
    return ListView(
      padding: const EdgeInsets.all(24),
      children: [
        Wrap(
          spacing: 12,
          runSpacing: 12,
          children: [
            for (final entry in counts.entries)
              _info(entry.key.toString().replaceAll('_', ' '), entry.value),
          ],
        ),
        const SizedBox(height: 24),
        Text(
          'Configured hierarchy',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        if (hierarchy.isEmpty)
          const Text('No blocks, sections, or spaces configured.'),
        for (final rawBlock in hierarchy)
          Card(
            child: ExpansionTile(
              leading: const Icon(Icons.account_tree_outlined),
              title: Text((rawBlock as Map)['name']?.toString() ?? 'Block'),
              subtitle: Text(
                '${(rawBlock['sections'] as List?)?.length ?? 0} sections',
              ),
              children: [
                for (final rawSection
                    in (rawBlock['sections'] as List?) ?? const [])
                  Builder(
                    builder: (context) {
                      final section = rawSection as Map;
                      final spaces = (section['spaces'] as List?) ?? const [];
                      return ExpansionTile(
                        leading: const Icon(Icons.layers_outlined),
                        title: Text(section['name']?.toString() ?? 'Section'),
                        subtitle: Text('${spaces.length} spaces'),
                        children: [
                          for (final rawSpace in spaces)
                            Builder(
                              builder: (context) {
                                final space = rawSpace as Map;
                                return ListTile(
                                  leading: const Icon(
                                    Icons.meeting_room_outlined,
                                  ),
                                  title: Text(
                                    space['space_number']?.toString() ??
                                        space['flat_number']?.toString() ??
                                        'Space',
                                  ),
                                  subtitle: Text(
                                    [
                                      space['occupancy_status'],
                                      if (space['area_sqft'] != null)
                                        '${space['area_sqft']} sq ft',
                                    ].whereType<Object>().join(' • '),
                                  ),
                                );
                              },
                            ),
                        ],
                      );
                    },
                  ),
              ],
            ),
          ),
      ],
    );
  }

  Widget _relatedActivity(Map data) => ListView(
    padding: const EdgeInsets.all(24),
    children: [
      _recordSection(
        'Custom requirements',
        (data['requirements'] as List?) ?? const [],
        titleKeys: const ['requested_modules', 'requirement_details'],
        subtitleKeys: const ['status', 'created_at'],
      ),
      _recordSection(
        'Notification history',
        (data['notifications'] as List?) ?? const [],
        titleKeys: const ['template_code', 'channel'],
        subtitleKeys: const ['destination', 'attempts', 'created_at'],
      ),
      _recordSection(
        'Audit trail',
        (data['audit_events'] as List?) ?? const [],
        titleKeys: const ['action'],
        subtitleKeys: const ['entity_type', 'entity_id', 'occurred_at'],
      ),
    ],
  );

  Widget _recordSection(
    String heading,
    List rows, {
    required List<String> titleKeys,
    required List<String> subtitleKeys,
  }) {
    String join(Map row, List<String> keys) => keys
        .map((key) => row[key])
        .where((value) => value != null && value.toString().isNotEmpty)
        .join(' • ');
    return Padding(
      padding: const EdgeInsets.only(bottom: 24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(heading, style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 8),
          if (rows.isEmpty) Text('No ${heading.toLowerCase()} linked.'),
          for (final raw in rows)
            Card(
              child: ListTile(
                title: Text(join(raw as Map, titleKeys)),
                subtitle: Text(join(raw, subtitleKeys)),
                trailing: raw['status'] == null
                    ? null
                    : StatusChip(raw['status'].toString()),
              ),
            ),
        ],
      ),
    );
  }

  Widget _records(List rows, List<String> title, List<String> subtitle) =>
      rows.isEmpty
      ? const EmptyState('No linked records.')
      : ListView.separated(
          padding: const EdgeInsets.all(24),
          itemCount: rows.length,
          separatorBuilder: (_, _) => const SizedBox(height: 8),
          itemBuilder: (c, i) {
            final r = rows[i] as Map;
            String join(List<String> keys) =>
                keys.map((k) => r[k]).where((v) => v != null).join(' • ');
            return Card(
              child: ListTile(
                title: Text(join(title)),
                subtitle: Text(join(subtitle)),
                trailing: r['status'] == null
                    ? null
                    : StatusChip(r['status'].toString()),
              ),
            );
          },
        );
  Widget _plans(Map data) {
    final plans = data['customPlans'] as List, orders = data['orders'] as List;
    return ListView(
      padding: const EdgeInsets.all(24),
      children: [
        Text('Custom plans', style: Theme.of(context).textTheme.titleLarge),
        const SizedBox(height: 8),
        if (plans.isEmpty) const Text('No custom plan.'),
        for (final p in plans)
          Card(
            child: ListTile(
              title: Text(p['plan_name']?.toString() ?? 'Custom rates'),
              subtitle: Text(_customPlanSummary(p as Map)),
              trailing: StatusChip(p['status'].toString()),
            ),
          ),
        const SizedBox(height: 20),
        Text(
          'Subscription orders',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        if (orders.isEmpty) const Text('No subscription orders.'),
        for (final o in orders)
          Card(
            child: ListTile(
              title: Text('${o['plan_code']} • ₹${o['amount']}'),
              subtitle: Text(o['created_at'].toString()),
              trailing: StatusChip(o['status'].toString()),
            ),
          ),
      ],
    );
  }

  String _customPlanSummary(Map plan) {
    final fixedMonthly =
        double.tryParse(plan['price_monthly']?.toString() ?? '') ?? 0;
    final pricing = fixedMonthly > 0
        ? '₹${plan['price_monthly']} fixed monthly'
        : '₹${plan['price_per_space'] ?? 0} / space + '
              '₹${plan['price_per_staff'] ?? 0} / staff monthly';
    return '$pricing • ${plan['enabled_modules']}';
  }
}

class _EditSocietyDialog extends StatefulWidget {
  const _EditSocietyDialog({required this.society, required this.services});
  final Map society;
  final List<Map<String, dynamic>> services;
  @override
  State<_EditSocietyDialog> createState() => _EditSocietyDialogState();
}

class _EditSocietyDialogState extends State<_EditSocietyDialog> {
  late final controls = {
    for (final k in [
      'name',
      'description',
      'website',
      'contact_person',
      'email',
      'mobile',
      'address',
      'city',
      'state',
      'pin_code',
      'planned_staff',
    ])
      k: TextEditingController(text: widget.society[k]?.toString()),
  };
  late String plan = widget.society['plan_code'].toString(),
      status = widget.society['status'].toString();
  late final Set<String> selectedServices;

  @override
  void initState() {
    super.initState();
    selectedServices = societyServicesFrom(widget.society['enabled_modules']);
    selectedServices.addAll(
      widget.services
          .where((service) => service['availability_type'] == 'DEFAULT_FREE')
          .map((service) => service['code'].toString()),
    );
  }

  void submit() {
    if (selectedServices.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Select at least one service')),
      );
      return;
    }
    Navigator.pop(context, {
      'name': controls['name']!.text,
      'description': controls['description']!.text,
      'website': controls['website']!.text,
      'contactPerson': controls['contact_person']!.text,
      'email': controls['email']!.text,
      'mobile': controls['mobile']!.text,
      'address': controls['address']!.text,
      'city': controls['city']!.text,
      'state': controls['state']!.text,
      'pinCode': controls['pin_code']!.text,
      'planCode': plan,
      'plannedStaff': int.tryParse(controls['planned_staff']!.text) ?? 0,
      'status': status,
      'enabledModules': selectedServices.toList(),
    });
  }

  @override
  Widget build(context) => AlertDialog(
    title: const Text('Manage society'),
    content: SizedBox(
      width: 600,
      child: SingleChildScrollView(
        child: Wrap(
          spacing: 10,
          runSpacing: 10,
          children: [
            for (final e in {
              'name': 'Society name',
              'description': 'Description',
              'website': 'Website',
              'contact_person': 'Contact person',
              'email': 'Email',
              'mobile': 'Mobile',
              'address': 'Address',
              'city': 'City',
              'state': 'State',
              'pin_code': 'PIN code',
              'planned_staff': 'Purchased staff seats',
            }.entries)
              SizedBox(
                width: 280,
                child: TextField(
                  controller: controls[e.key],
                  decoration: InputDecoration(labelText: e.value),
                ),
              ),
            SizedBox(
              width: 280,
              child: DropdownButtonFormField<String>(
                initialValue: plan,
                decoration: const InputDecoration(labelText: 'Billing type'),
                items:
                    const {
                          'TRIAL': 'Trial settings',
                          'STANDARD': 'Paid usage rates',
                        }.entries
                        .map(
                          (entry) => DropdownMenuItem(
                            value: entry.key,
                            child: Text(entry.value),
                          ),
                        )
                        .toList(),
                onChanged: (v) => plan = v!,
              ),
            ),
            SizedBox(
              width: 280,
              child: DropdownButtonFormField<String>(
                initialValue: status,
                decoration: const InputDecoration(labelText: 'Platform status'),
                items: ['ACTIVE', 'SUSPENDED', 'APPROVED']
                    .map((v) => DropdownMenuItem(value: v, child: Text(v)))
                    .toList(),
                onChanged: (v) => status = v!,
              ),
            ),
            SizedBox(
              width: 570,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Services provided',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  const SizedBox(height: 6),
                  Wrap(
                    spacing: 6,
                    runSpacing: 6,
                    children: [
                      for (final service in widget.services)
                        FilterChip(
                          label: Text(_managedServiceLabel(service)),
                          selected: selectedServices.contains(service['code']),
                          onSelected:
                              service['availability_type'] == 'DEFAULT_FREE'
                              ? null
                              : (selected) => setState(
                                  () => selected
                                      ? selectedServices.add(
                                          service['code'].toString(),
                                        )
                                      : selectedServices.remove(
                                          service['code'],
                                        ),
                                ),
                        ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    ),
    actions: [
      TextButton(
        onPressed: () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(onPressed: submit, child: const Text('Save changes')),
    ],
  );
}

String _managedServiceLabel(Map<String, dynamic> service) =>
    switch (service['availability_type']?.toString()) {
      'DEFAULT_FREE' => '${service['name']} · Included',
      'PAID_PER_SPACE' =>
        '${service['name']} · ₹${service['price_per_space'] ?? 0}/space',
      _ => '${service['name']} · Free',
    };
