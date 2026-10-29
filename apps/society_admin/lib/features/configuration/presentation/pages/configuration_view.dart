import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'package:society_admin_app/core/presentation/widgets/common.dart';
import '../bloc/configuration_bloc.dart';

class ConfigurationView extends StatefulWidget {
  const ConfigurationView({super.key, required this.enabledModules});

  final Set<String> enabledModules;

  @override
  State<ConfigurationView> createState() => _ConfigurationViewState();
}

class _ConfigurationViewState extends State<ConfigurationView> {
  Map<String, dynamic> _data = const {};
  bool _busy = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  void _load() => context.read<ConfigurationBloc>().loadConfiguration();

  Future<void> _run(Future<dynamic> Function() action, String success) async {
    if (_busy) return;
    setState(() => _busy = true);
    try {
      await action();
      _load();
      _message(success);
    } catch (error) {
      _message(error.toString());
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _completeSetup() async {
    final confirmed = await _confirm(
      'Complete society setup',
      'Confirm that the required property structure is ready. Optional configurations can still be changed later.',
      'Complete setup',
    );
    if (!confirmed || !mounted) return;
    await _run(
      context.read<ConfigurationBloc>().completeSetup,
      'Society setup completed.',
    );
  }

  Future<void> _parkingForm([Map<String, dynamic>? item]) async {
    final body = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => _ParkingForm(item: item),
    );
    if (body == null) return;
    await _run(
      () => context.read<ConfigurationBloc>().saveResource(
        ConfigurationResource.parking,
        body,
        id: item?['id'],
      ),
      item == null ? 'Parking slot added.' : 'Parking slot updated.',
    );
  }

  Future<void> _parkingAction(Map<String, dynamic> item, String action) async {
    Map<String, dynamic> body = {'action': action};
    if (action == 'ALLOCATE') {
      final result = await showDialog<Map<String, dynamic>>(
        context: context,
        builder: (_) =>
            _ParkingAllocationForm(slot: item, spaces: _maps(_data['spaces'])),
      );
      if (result == null) return;
      body = result;
    } else {
      final label = _label(action);
      if (!await _confirm(
        '$label parking slot',
        action == 'RELEASE'
            ? 'Release this slot and clear its current space and vehicle allocation?'
            : 'Change ${item['slot_number']} to ${label.toLowerCase()}?',
        label,
      )) {
        return;
      }
    }
    await _run(
      () => context.read<ConfigurationBloc>().updateResource(
        ConfigurationResource.parking,
        item['id'],
        body,
      ),
      'Parking status updated.',
    );
  }

  Future<void> _vendorForm([Map<String, dynamic>? item]) async {
    final body = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => _VendorForm(item: item),
    );
    if (body == null) return;
    await _run(
      () => context.read<ConfigurationBloc>().saveResource(
        ConfigurationResource.vendor,
        body,
        id: item?['id'],
      ),
      item == null ? 'Vendor added.' : 'Vendor updated.',
    );
  }

  Future<void> _vendorStatus(Map<String, dynamic> item, bool active) async {
    await _run(
      () => context.read<ConfigurationBloc>().updateResource(
        ConfigurationResource.vendor,
        item['id'],
        {'status': active ? 'ACTIVE' : 'INACTIVE'},
      ),
      active ? 'Vendor activated.' : 'Vendor deactivated.',
    );
  }

  Future<void> _categoryForm([Map<String, dynamic>? item]) async {
    final body = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => _CategoryForm(item: item),
    );
    if (body == null) return;
    await _run(
      () => context.read<ConfigurationBloc>().saveResource(
        ConfigurationResource.complaintCategory,
        body,
        id: item?['id'],
      ),
      item == null ? 'Complaint category added.' : 'Category updated.',
    );
  }

  Future<void> _categoryStatus(Map<String, dynamic> item, bool active) async {
    await _run(
      () => context.read<ConfigurationBloc>().updateResource(
        ConfigurationResource.complaintCategory,
        item['id'],
        {'active': active},
      ),
      active ? 'Category activated.' : 'Category deactivated.',
    );
  }

  Future<void> _chargeForm([Map<String, dynamic>? item]) async {
    final body = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => _ChargeForm(item: item),
    );
    if (body == null) return;
    await _run(
      () => context.read<ConfigurationBloc>().saveResource(
        ConfigurationResource.chargeRule,
        body,
        id: item?['id'],
      ),
      item == null ? 'Charge rule added.' : 'Charge rule updated.',
    );
  }

  Future<void> _chargeStatus(Map<String, dynamic> item, bool active) async {
    await _run(
      () => context.read<ConfigurationBloc>().updateResource(
        ConfigurationResource.chargeRule,
        item['id'],
        {'active': active},
      ),
      active ? 'Charge rule activated.' : 'Charge rule deactivated.',
    );
  }

  Future<bool> _confirm(String title, String message, String action) async =>
      await showDialog<bool>(
        context: context,
        builder: (dialog) => AlertDialog(
          title: Text(title),
          content: Text(message),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialog, false),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(dialog, true),
              child: Text(action),
            ),
          ],
        ),
      ) ??
      false;

  void _message(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), behavior: SnackBarBehavior.floating),
    );
  }

  @override
  Widget build(BuildContext context) => PageFrame(
    title: 'Configuration',
    subtitle:
        'Society-wide parking, vendors, complaint SLAs, charge rules, and setup readiness.',
    child: BlocBuilder<ConfigurationBloc, AdminResourceState>(
      builder: (context, state) => _body(state),
    ),
  );

  Widget _body(AdminResourceState state) {
    final snapshot = state.resource('configuration');
    if (snapshot.status == ResourceStatus.initial ||
        snapshot.status == ResourceStatus.loading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (snapshot.error != null) {
      return Center(
        child: FilledButton.icon(
          onPressed: _load,
          icon: const Icon(Icons.refresh),
          label: Text(snapshot.error!),
        ),
      );
    }
    _data = (snapshot.value as ResourceDocument).data;
    final summary = _map(_data['summary']);
    final setup = _map(_data['setup']);
    return RefreshIndicator(
      onRefresh: () async => _load(),
      child: ListView(
        padding: const EdgeInsets.only(bottom: 28),
        children: [
          _SetupCard(setup: setup, busy: _busy, onComplete: _completeSetup),
          const SizedBox(height: 14),
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: [
              if (_enabled('PARKING'))
                _Metric(
                  'Parking slots',
                  '${summary['parking_total'] ?? 0}',
                  '${summary['parking_available'] ?? 0} available · ${summary['parking_allocated'] ?? 0} allocated',
                  Icons.local_parking,
                ),
              if (_enabled('VENDORS'))
                _Metric(
                  'Active vendors',
                  '${summary['active_vendors'] ?? 0}',
                  'Approved service providers',
                  Icons.handyman_outlined,
                ),
              if (_enabled('COMPLAINTS'))
                _Metric(
                  'Complaint categories',
                  '${summary['active_categories'] ?? 0}',
                  'Active SLA definitions',
                  Icons.rule_folder_outlined,
                ),
              if (_enabled('BILLING'))
                _Metric(
                  'Charge rules',
                  '${summary['active_charge_rules'] ?? 0}',
                  'Active default calculations',
                  Icons.calculate_outlined,
                ),
            ],
          ),
          if (_enabled('PARKING')) ...[
            const SizedBox(height: 20),
            _SectionHeader(
              title: 'Parking management',
              subtitle:
                  'Create slots, allocate spaces and vehicles, reserve, release, or mark maintenance.',
              button: 'Add slot',
              icon: Icons.add_road,
              onPressed: _busy ? null : _parkingForm,
            ),
            const SizedBox(height: 8),
            _ParkingGrid(
              rows: _maps(_data['parking']),
              busy: _busy,
              onEdit: _parkingForm,
              onAction: _parkingAction,
            ),
          ],
          if (_enabled('VENDORS')) ...[
            const SizedBox(height: 20),
            _SectionHeader(
              title: 'Vendor directory',
              subtitle:
                  'Maintain real service-provider contacts, GST details, and active status.',
              button: 'Add vendor',
              icon: Icons.person_add_alt,
              onPressed: _busy ? null : _vendorForm,
            ),
            const SizedBox(height: 8),
            _VendorGrid(
              rows: _maps(_data['vendors']),
              busy: _busy,
              onEdit: _vendorForm,
              onStatus: _vendorStatus,
            ),
          ],
          if (_enabled('COMPLAINTS')) ...[
            const SizedBox(height: 20),
            _SectionHeader(
              title: 'Complaint categories and SLAs',
              subtitle:
                  'Categories immediately appear in Member, Staff, and Admin complaint forms.',
              button: 'Add category',
              icon: Icons.create_new_folder_outlined,
              onPressed: _busy ? null : _categoryForm,
            ),
            const SizedBox(height: 8),
            _CategoryGrid(
              rows: _maps(_data['complaintCategories']),
              busy: _busy,
              onEdit: _categoryForm,
              onStatus: _categoryStatus,
            ),
          ],
          if (_enabled('BILLING')) ...[
            const SizedBox(height: 20),
            _SectionHeader(
              title: 'Default charge rules',
              subtitle:
                  'Configure fixed, per-square-foot, or percentage calculations with effective dates.',
              button: 'Add charge rule',
              icon: Icons.add_card,
              onPressed: _busy ? null : _chargeForm,
            ),
            const SizedBox(height: 8),
            _ChargeGrid(
              rows: _maps(_data['chargeRules']),
              busy: _busy,
              onEdit: _chargeForm,
              onStatus: _chargeStatus,
            ),
          ],
        ],
      ),
    );
  }

  bool _enabled(String module) => widget.enabledModules.contains(module);
}

class _SetupCard extends StatelessWidget {
  const _SetupCard({
    required this.setup,
    required this.busy,
    required this.onComplete,
  });

  final Map<String, dynamic> setup;
  final bool busy;
  final VoidCallback onComplete;

  @override
  Widget build(BuildContext context) {
    final completed = setup['setup_completed'] == true;
    final ready = setup['ready'] == true;
    return Card(
      color: completed
          ? SocietyColors.success.withValues(alpha: .07)
          : SocietyColors.warning.withValues(alpha: .08),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Wrap(
          spacing: 18,
          runSpacing: 14,
          alignment: WrapAlignment.spaceBetween,
          crossAxisAlignment: WrapCrossAlignment.center,
          children: [
            ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 760),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    completed ? 'Society setup complete' : 'Setup readiness',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  Text(
                    '${setup['property_groups'] ?? 0} property groups · ${setup['spaces'] ?? 0} spaces',
                    style: const TextStyle(color: SocietyColors.textMuted),
                  ),
                  const SizedBox(height: 10),
                  Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: [
                      for (final row in _maps(setup['checklist']))
                        Chip(
                          avatar: Icon(
                            row['complete'] == true
                                ? Icons.check_circle
                                : row['required'] == true
                                ? Icons.error_outline
                                : Icons.info_outline,
                            size: 18,
                            color: row['complete'] == true
                                ? SocietyColors.success
                                : SocietyColors.warning,
                          ),
                          label: Text(
                            '${row['label']}${row['required'] == true ? ' · required' : ' · optional'}',
                          ),
                        ),
                    ],
                  ),
                ],
              ),
            ),
            if (!completed)
              FilledButton.icon(
                onPressed: ready && !busy ? onComplete : null,
                icon: const Icon(Icons.task_alt),
                label: Text(ready ? 'Complete setup' : 'Structure required'),
              ),
          ],
        ),
      ),
    );
  }
}

class _Metric extends StatelessWidget {
  const _Metric(this.label, this.value, this.note, this.icon);

  final String label, value, note;
  final IconData icon;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: 245,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Row(
          children: [
            CircleAvatar(child: Icon(icon)),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    label,
                    style: const TextStyle(color: SocietyColors.textMuted),
                  ),
                  Text(value, style: Theme.of(context).textTheme.headlineSmall),
                  Text(note, style: Theme.of(context).textTheme.bodySmall),
                ],
              ),
            ),
          ],
        ),
      ),
    ),
  );
}

class _SectionHeader extends StatelessWidget {
  const _SectionHeader({
    required this.title,
    required this.subtitle,
    required this.button,
    required this.icon,
    required this.onPressed,
  });

  final String title, subtitle, button;
  final IconData icon;
  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) => Wrap(
    spacing: 12,
    runSpacing: 8,
    alignment: WrapAlignment.spaceBetween,
    crossAxisAlignment: WrapCrossAlignment.center,
    children: [
      ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 760),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: Theme.of(context).textTheme.titleLarge),
            Text(
              subtitle,
              style: const TextStyle(color: SocietyColors.textMuted),
            ),
          ],
        ),
      ),
      FilledButton.icon(
        onPressed: onPressed,
        icon: Icon(icon),
        label: Text(button),
      ),
    ],
  );
}

class _ParkingGrid extends StatelessWidget {
  const _ParkingGrid({
    required this.rows,
    required this.busy,
    required this.onEdit,
    required this.onAction,
  });

  final List<Map<String, dynamic>> rows;
  final bool busy;
  final void Function(Map<String, dynamic>) onEdit;
  final void Function(Map<String, dynamic>, String) onAction;

  @override
  Widget build(BuildContext context) {
    if (rows.isEmpty) return const _Empty('No parking slots configured.');
    return Wrap(
      spacing: 10,
      runSpacing: 10,
      children: [
        for (final item in rows)
          SizedBox(
            width: 330,
            child: Card(
              child: Padding(
                padding: const EdgeInsets.all(14),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            item['slot_number']?.toString() ?? 'Parking slot',
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                        ),
                        _Status(item['status']),
                        PopupMenuButton<String>(
                          enabled: !busy,
                          onSelected: (value) => value == 'EDIT'
                              ? onEdit(item)
                              : onAction(item, value),
                          itemBuilder: (_) => [
                            const PopupMenuItem(
                              value: 'EDIT',
                              child: Text('Edit details'),
                            ),
                            if (item['status'] != 'ALLOCATED')
                              const PopupMenuItem(
                                value: 'ALLOCATE',
                                child: Text('Allocate'),
                              ),
                            if (item['status'] == 'ALLOCATED')
                              const PopupMenuItem(
                                value: 'RELEASE',
                                child: Text('Release'),
                              ),
                            const PopupMenuItem(
                              value: 'RESERVE',
                              child: Text('Reserve'),
                            ),
                            const PopupMenuItem(
                              value: 'MAINTENANCE',
                              child: Text('Maintenance'),
                            ),
                            if (item['status'] == 'INACTIVE')
                              const PopupMenuItem(
                                value: 'ACTIVATE',
                                child: Text('Activate'),
                              )
                            else
                              const PopupMenuItem(
                                value: 'DEACTIVATE',
                                child: Text('Deactivate'),
                              ),
                          ],
                        ),
                      ],
                    ),
                    Text(_label(item['slot_type'])),
                    if (item['vehicle_number'] != null)
                      Text('Vehicle: ${item['vehicle_number']}'),
                    if (item['flat_id'] != null)
                      Text('Space allocation: ${item['flat_id']}'),
                  ],
                ),
              ),
            ),
          ),
      ],
    );
  }
}

class _VendorGrid extends StatelessWidget {
  const _VendorGrid({
    required this.rows,
    required this.busy,
    required this.onEdit,
    required this.onStatus,
  });

  final List<Map<String, dynamic>> rows;
  final bool busy;
  final void Function(Map<String, dynamic>) onEdit;
  final void Function(Map<String, dynamic>, bool) onStatus;

  @override
  Widget build(BuildContext context) {
    if (rows.isEmpty) return const _Empty('No vendors configured.');
    return Wrap(
      spacing: 10,
      runSpacing: 10,
      children: [
        for (final item in rows)
          SizedBox(
            width: 360,
            child: Card(
              child: Padding(
                padding: const EdgeInsets.all(14),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            item['name']?.toString() ?? 'Vendor',
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                        ),
                        _Status(item['status']),
                        PopupMenuButton<String>(
                          enabled: !busy,
                          onSelected: (value) => value == 'EDIT'
                              ? onEdit(item)
                              : onStatus(item, value == 'ACTIVATE'),
                          itemBuilder: (_) => [
                            const PopupMenuItem(
                              value: 'EDIT',
                              child: Text('Edit vendor'),
                            ),
                            PopupMenuItem(
                              value: item['status'] == 'ACTIVE'
                                  ? 'DEACTIVATE'
                                  : 'ACTIVATE',
                              child: Text(
                                item['status'] == 'ACTIVE'
                                    ? 'Deactivate'
                                    : 'Activate',
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                    Text(_label(item['service_type'])),
                    Text(
                      '${item['contact_person'] ?? 'No contact person'} · ${item['mobile']}',
                    ),
                    if (item['email'] != null) Text(item['email'].toString()),
                    if (item['gst_number'] != null)
                      Text('GST: ${item['gst_number']}'),
                  ],
                ),
              ),
            ),
          ),
      ],
    );
  }
}

class _CategoryGrid extends StatelessWidget {
  const _CategoryGrid({
    required this.rows,
    required this.busy,
    required this.onEdit,
    required this.onStatus,
  });

  final List<Map<String, dynamic>> rows;
  final bool busy;
  final void Function(Map<String, dynamic>) onEdit;
  final void Function(Map<String, dynamic>, bool) onStatus;

  @override
  Widget build(BuildContext context) {
    if (rows.isEmpty) {
      return const _Empty('No complaint categories configured.');
    }
    return Wrap(
      spacing: 10,
      runSpacing: 10,
      children: [
        for (final item in rows)
          SizedBox(
            width: 300,
            child: Card(
              child: ListTile(
                title: Text(item['name']?.toString() ?? 'Category'),
                subtitle: Text('${item['sla_hours']} hour SLA'),
                leading: Icon(
                  item['active'] == true
                      ? Icons.timer_outlined
                      : Icons.timer_off_outlined,
                ),
                trailing: PopupMenuButton<String>(
                  enabled: !busy,
                  onSelected: (value) => value == 'EDIT'
                      ? onEdit(item)
                      : onStatus(item, value == 'ACTIVATE'),
                  itemBuilder: (_) => [
                    const PopupMenuItem(
                      value: 'EDIT',
                      child: Text('Edit category'),
                    ),
                    PopupMenuItem(
                      value: item['active'] == true ? 'DEACTIVATE' : 'ACTIVATE',
                      child: Text(
                        item['active'] == true ? 'Deactivate' : 'Activate',
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
      ],
    );
  }
}

class _ChargeGrid extends StatelessWidget {
  const _ChargeGrid({
    required this.rows,
    required this.busy,
    required this.onEdit,
    required this.onStatus,
  });

  final List<Map<String, dynamic>> rows;
  final bool busy;
  final void Function(Map<String, dynamic>) onEdit;
  final void Function(Map<String, dynamic>, bool) onStatus;

  @override
  Widget build(BuildContext context) {
    if (rows.isEmpty) {
      return const _Empty('No default charge rules configured.');
    }
    return Wrap(
      spacing: 10,
      runSpacing: 10,
      children: [
        for (final item in rows)
          SizedBox(
            width: 350,
            child: Card(
              child: Padding(
                padding: const EdgeInsets.all(14),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            item['name']?.toString() ?? 'Charge rule',
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                        ),
                        _Status(item['active'] == true ? 'ACTIVE' : 'INACTIVE'),
                        PopupMenuButton<String>(
                          enabled: !busy,
                          onSelected: (value) => value == 'EDIT'
                              ? onEdit(item)
                              : onStatus(item, value == 'ACTIVATE'),
                          itemBuilder: (_) => [
                            const PopupMenuItem(
                              value: 'EDIT',
                              child: Text('Edit charge rule'),
                            ),
                            PopupMenuItem(
                              value: item['active'] == true
                                  ? 'DEACTIVATE'
                                  : 'ACTIVATE',
                              child: Text(
                                item['active'] == true
                                    ? 'Deactivate'
                                    : 'Activate',
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                    Text(
                      '${_label(item['calculation_type'])} · ${item['amount']}',
                    ),
                    Text(
                      '${_label(item['frequency'])} · from ${item['effective_from']}',
                    ),
                    if (item['effective_to'] != null)
                      Text('Ends ${item['effective_to']}'),
                  ],
                ),
              ),
            ),
          ),
      ],
    );
  }
}

class _Status extends StatelessWidget {
  const _Status(this.value);

  final Object? value;

  @override
  Widget build(BuildContext context) =>
      Chip(visualDensity: VisualDensity.compact, label: Text(_label(value)));
}

class _Empty extends StatelessWidget {
  const _Empty(this.message);

  final String message;

  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(20),
      child: Text(
        message,
        style: const TextStyle(color: SocietyColors.textMuted),
      ),
    ),
  );
}

class _ParkingForm extends StatefulWidget {
  const _ParkingForm({this.item});

  final Map<String, dynamic>? item;

  @override
  State<_ParkingForm> createState() => _ParkingFormState();
}

class _ParkingFormState extends State<_ParkingForm> {
  late final TextEditingController _number = TextEditingController(
    text: widget.item?['slot_number']?.toString() ?? '',
  );
  late String _type = widget.item?['slot_type']?.toString() ?? 'CAR';
  String? _error;

  @override
  void dispose() {
    _number.dispose();
    super.dispose();
  }

  void _save() {
    if (_number.text.trim().isEmpty) {
      setState(() => _error = 'Slot number is required.');
      return;
    }
    Navigator.pop(context, {
      'slotNumber': _number.text.trim().toUpperCase(),
      'slotType': _type,
    });
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: Text(widget.item == null ? 'Add parking slot' : 'Edit parking slot'),
    content: SizedBox(
      width: 440,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextField(
            key: const Key('parking-number'),
            controller: _number,
            maxLength: 40,
            decoration: const InputDecoration(labelText: 'Slot number'),
          ),
          DropdownButtonFormField<String>(
            initialValue: _type,
            decoration: const InputDecoration(labelText: 'Slot type'),
            items: const ['CAR', 'BIKE', 'VISITOR', 'ACCESSIBLE']
                .map(
                  (value) => DropdownMenuItem(
                    value: value,
                    child: Text(_label(value)),
                  ),
                )
                .toList(),
            onChanged: (value) => setState(() => _type = value!),
          ),
          if (_error != null) ...[
            const SizedBox(height: 8),
            Text(_error!, style: const TextStyle(color: SocietyColors.danger)),
          ],
        ],
      ),
    ),
    actions: [
      TextButton(
        onPressed: () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(
        key: const Key('save-parking'),
        onPressed: _save,
        child: const Text('Save slot'),
      ),
    ],
  );
}

class _ParkingAllocationForm extends StatefulWidget {
  const _ParkingAllocationForm({required this.slot, required this.spaces});

  final Map<String, dynamic> slot;
  final List<Map<String, dynamic>> spaces;

  @override
  State<_ParkingAllocationForm> createState() => _ParkingAllocationFormState();
}

class _ParkingAllocationFormState extends State<_ParkingAllocationForm> {
  String? _spaceId;
  final _vehicle = TextEditingController();
  String? _error;

  @override
  void dispose() {
    _vehicle.dispose();
    super.dispose();
  }

  void _save() {
    if (_spaceId == null || _vehicle.text.trim().isEmpty) {
      setState(() => _error = 'Space and vehicle number are required.');
      return;
    }
    Navigator.pop(context, {
      'action': 'ALLOCATE',
      'flatId': _spaceId,
      'vehicleNumber': _vehicle.text.trim().toUpperCase(),
    });
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: Text('Allocate ${widget.slot['slot_number']}'),
    content: SizedBox(
      width: 500,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          DropdownButtonFormField<String>(
            isExpanded: true,
            initialValue: _spaceId,
            decoration: const InputDecoration(labelText: 'Society space'),
            items: [
              for (final space in widget.spaces)
                DropdownMenuItem(
                  value: space['id'].toString(),
                  child: Text(
                    space['label'].toString(),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
            ],
            onChanged: (value) => setState(() => _spaceId = value),
          ),
          const SizedBox(height: 10),
          TextField(
            controller: _vehicle,
            maxLength: 30,
            decoration: const InputDecoration(labelText: 'Vehicle number'),
          ),
          if (widget.spaces.isEmpty)
            const Text('Create a property space before allocating parking.'),
          if (_error != null)
            Text(_error!, style: const TextStyle(color: SocietyColors.danger)),
        ],
      ),
    ),
    actions: [
      TextButton(
        onPressed: () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(
        onPressed: widget.spaces.isEmpty ? null : _save,
        child: const Text('Allocate'),
      ),
    ],
  );
}

class _VendorForm extends StatefulWidget {
  const _VendorForm({this.item});

  final Map<String, dynamic>? item;

  @override
  State<_VendorForm> createState() => _VendorFormState();
}

class _VendorFormState extends State<_VendorForm> {
  late final _name = _controller('name');
  late final _service = _controller('service_type');
  late final _contact = _controller('contact_person');
  late final _mobile = _controller('mobile');
  late final _email = _controller('email');
  late final _gst = _controller('gst_number');
  String? _error;

  TextEditingController _controller(String key) =>
      TextEditingController(text: widget.item?[key]?.toString() ?? '');

  @override
  void dispose() {
    for (final controller in [
      _name,
      _service,
      _contact,
      _mobile,
      _email,
      _gst,
    ]) {
      controller.dispose();
    }
    super.dispose();
  }

  void _save() {
    if (_name.text.trim().isEmpty ||
        _service.text.trim().isEmpty ||
        _mobile.text.trim().length < 7) {
      setState(
        () => _error = 'Name, service type, and a valid mobile are required.',
      );
      return;
    }
    if (_email.text.trim().isNotEmpty && !_email.text.contains('@')) {
      setState(() => _error = 'Enter a valid email address.');
      return;
    }
    Navigator.pop(context, {
      'name': _name.text.trim(),
      'serviceType': _service.text.trim(),
      'contactPerson': _contact.text.trim(),
      'mobile': _mobile.text.trim(),
      'email': _email.text.trim(),
      'gstNumber': _gst.text.trim().toUpperCase(),
    });
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: Text(widget.item == null ? 'Add vendor' : 'Edit vendor'),
    content: SizedBox(
      width: 620,
      child: SingleChildScrollView(
        child: Wrap(
          spacing: 10,
          runSpacing: 10,
          children: [
            _formField(_name, 'Vendor name', key: const Key('vendor-name')),
            _formField(_service, 'Service type'),
            _formField(_contact, 'Contact person'),
            _formField(_mobile, 'Mobile', keyboard: TextInputType.phone),
            _formField(_email, 'Email', keyboard: TextInputType.emailAddress),
            _formField(_gst, 'GST number'),
            if (_error != null)
              SizedBox(
                width: 600,
                child: Text(
                  _error!,
                  style: const TextStyle(color: SocietyColors.danger),
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
      FilledButton(
        key: const Key('save-vendor'),
        onPressed: _save,
        child: const Text('Save vendor'),
      ),
    ],
  );
}

class _CategoryForm extends StatefulWidget {
  const _CategoryForm({this.item});

  final Map<String, dynamic>? item;

  @override
  State<_CategoryForm> createState() => _CategoryFormState();
}

class _CategoryFormState extends State<_CategoryForm> {
  late final _name = TextEditingController(
    text: widget.item?['name']?.toString() ?? '',
  );
  late final _sla = TextEditingController(
    text: widget.item?['sla_hours']?.toString() ?? '48',
  );
  String? _error;

  @override
  void dispose() {
    _name.dispose();
    _sla.dispose();
    super.dispose();
  }

  void _save() {
    final hours = int.tryParse(_sla.text.trim());
    if (_name.text.trim().isEmpty ||
        hours == null ||
        hours < 1 ||
        hours > 8760) {
      setState(
        () => _error = 'Name and an SLA between 1 and 8760 hours are required.',
      );
      return;
    }
    Navigator.pop(context, {'name': _name.text.trim(), 'slaHours': hours});
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: Text(
      widget.item == null
          ? 'Add complaint category'
          : 'Edit complaint category',
    ),
    content: SizedBox(
      width: 460,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextField(
            controller: _name,
            decoration: const InputDecoration(labelText: 'Category name'),
          ),
          const SizedBox(height: 10),
          TextField(
            controller: _sla,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(
              labelText: 'Resolution SLA in hours',
            ),
          ),
          if (_error != null) ...[
            const SizedBox(height: 8),
            Text(_error!, style: const TextStyle(color: SocietyColors.danger)),
          ],
        ],
      ),
    ),
    actions: [
      TextButton(
        onPressed: () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(onPressed: _save, child: const Text('Save category')),
    ],
  );
}

class _ChargeForm extends StatefulWidget {
  const _ChargeForm({this.item});

  final Map<String, dynamic>? item;

  @override
  State<_ChargeForm> createState() => _ChargeFormState();
}

class _ChargeFormState extends State<_ChargeForm> {
  late final _name = TextEditingController(
    text: widget.item?['name']?.toString() ?? '',
  );
  late final _amount = TextEditingController(
    text: widget.item?['amount']?.toString() ?? '',
  );
  late final _from = TextEditingController(
    text: widget.item?['effective_from']?.toString() ?? _date(DateTime.now()),
  );
  late final _to = TextEditingController(
    text: widget.item?['effective_to']?.toString() ?? '',
  );
  late String _calculation =
      widget.item?['calculation_type']?.toString() ?? 'FIXED';
  late String _frequency = widget.item?['frequency']?.toString() ?? 'MONTHLY';
  String? _error;

  @override
  void dispose() {
    for (final controller in [_name, _amount, _from, _to]) {
      controller.dispose();
    }
    super.dispose();
  }

  void _save() {
    final amount = double.tryParse(_amount.text.trim());
    final from = DateTime.tryParse(_from.text.trim());
    final to = _to.text.trim().isEmpty
        ? null
        : DateTime.tryParse(_to.text.trim());
    if (_name.text.trim().isEmpty ||
        amount == null ||
        amount < 0 ||
        from == null) {
      setState(
        () => _error =
            'Name, non-negative amount, and valid start date are required.',
      );
      return;
    }
    if (_calculation == 'PERCENTAGE' && amount > 100) {
      setState(() => _error = 'Percentage cannot exceed 100.');
      return;
    }
    if (_to.text.trim().isNotEmpty && (to == null || to.isBefore(from))) {
      setState(
        () => _error = 'End date must be valid and not before the start date.',
      );
      return;
    }
    Navigator.pop(context, {
      'name': _name.text.trim(),
      'calculationType': _calculation,
      'amount': amount,
      'frequency': _frequency,
      'effectiveFrom': _date(from),
      if (to != null) 'effectiveTo': _date(to),
    });
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: Text(widget.item == null ? 'Add charge rule' : 'Edit charge rule'),
    content: SizedBox(
      width: 650,
      child: SingleChildScrollView(
        child: Wrap(
          spacing: 10,
          runSpacing: 10,
          children: [
            _formField(_name, 'Rule name', key: const Key('charge-name')),
            SizedBox(
              width: 315,
              child: DropdownButtonFormField<String>(
                initialValue: _calculation,
                decoration: const InputDecoration(
                  labelText: 'Calculation type',
                ),
                items: const ['FIXED', 'PER_SQFT', 'PERCENTAGE']
                    .map(
                      (value) => DropdownMenuItem(
                        value: value,
                        child: Text(_label(value)),
                      ),
                    )
                    .toList(),
                onChanged: (value) => setState(() => _calculation = value!),
              ),
            ),
            _formField(
              _amount,
              'Amount / rate',
              keyboard: TextInputType.number,
            ),
            SizedBox(
              width: 315,
              child: DropdownButtonFormField<String>(
                initialValue: _frequency,
                decoration: const InputDecoration(labelText: 'Frequency'),
                items: const ['MONTHLY', 'QUARTERLY', 'YEARLY', 'ONE_TIME']
                    .map(
                      (value) => DropdownMenuItem(
                        value: value,
                        child: Text(_label(value)),
                      ),
                    )
                    .toList(),
                onChanged: (value) => setState(() => _frequency = value!),
              ),
            ),
            _formField(_from, 'Effective from · YYYY-MM-DD'),
            _formField(_to, 'Effective to · optional'),
            if (_error != null)
              SizedBox(
                width: 630,
                child: Text(
                  _error!,
                  style: const TextStyle(color: SocietyColors.danger),
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
      FilledButton(
        key: const Key('save-charge'),
        onPressed: _save,
        child: const Text('Save rule'),
      ),
    ],
  );
}

Widget _formField(
  TextEditingController controller,
  String label, {
  Key? key,
  TextInputType? keyboard,
}) => SizedBox(
  width: 315,
  child: TextField(
    key: key,
    controller: controller,
    keyboardType: keyboard,
    decoration: InputDecoration(labelText: label),
  ),
);

Map<String, dynamic> _map(Object? value) =>
    value is Map ? Map<String, dynamic>.from(value) : <String, dynamic>{};

List<Map<String, dynamic>> _maps(Object? value) => (value as List? ?? const [])
    .map((item) => Map<String, dynamic>.from(item as Map))
    .toList();

String _label(Object? value) => (value?.toString() ?? '')
    .toLowerCase()
    .split('_')
    .map(
      (part) =>
          part.isEmpty ? '' : '${part[0].toUpperCase()}${part.substring(1)}',
    )
    .join(' ');

String _date(DateTime value) =>
    '${value.year.toString().padLeft(4, '0')}-${value.month.toString().padLeft(2, '0')}-${value.day.toString().padLeft(2, '0')}';
