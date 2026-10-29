import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';
import 'async_data_bloc.dart';
import 'theme.dart';
import 'workspace_record.dart';

enum EmergencyWorkspaceRole { societyAdmin, staff, resident }

Widget buildResidentEmergencyWorkspace(BlocManagedApiGateway client) =>
    EmergencyWorkspace(client: client, role: EmergencyWorkspaceRole.resident);

Widget buildStaffEmergencyWorkspace(BlocManagedApiGateway client) =>
    EmergencyWorkspace(client: client, role: EmergencyWorkspaceRole.staff);

class EmergencyWorkspace extends StatefulWidget {
  const EmergencyWorkspace({
    super.key,
    required this.client,
    required this.role,
  });

  final BlocManagedApiGateway client;
  final EmergencyWorkspaceRole role;

  @override
  State<EmergencyWorkspace> createState() => _EmergencyWorkspaceState();
}

class _EmergencyWorkspaceState extends State<EmergencyWorkspace> {
  late final AsyncDataBloc<Map<String, dynamic>> _bloc;
  Map<String, dynamic> get _data => _bloc.state.data ?? const {};
  String _status = 'ALL';

  bool get _resident => widget.role == EmergencyWorkspaceRole.resident;
  bool get _admin => widget.role == EmergencyWorkspaceRole.societyAdmin;

  @override
  void initState() {
    super.initState();
    _bloc = AsyncDataBloc(
      () async => Map<String, dynamic>.unmodifiable(
        Map<String, dynamic>.from(
          await widget.client.get('/api/emergencies') as Map,
        ),
      ),
    );
    _load();
  }

  Future<void> _load() => _bloc.refresh();

  @override
  void dispose() {
    _bloc.close();
    super.dispose();
  }

  Future<void> _create() async {
    final changed = await showDialog<bool>(
      context: context,
      barrierDismissible: false,
      builder: (_) => _IncidentForm(
        client: widget.client,
        resident: _resident,
        options: _maps(_data['resident_options']),
      ),
    );
    if (changed == true) {
      await _load();
      _message(
        'Emergency incident reported. Society responders were notified.',
      );
    }
  }

  Future<void> _action(Map<String, dynamic> item, String action) async {
    final body = <String, dynamic>{'action': action};
    if (action == 'ASSIGN') {
      final staffId = await _assignDialog();
      if (staffId == null) return;
      body['assignedStaffId'] = staffId;
    } else if (action == 'ESCALATE') {
      final severity = await _severityDialog(item['severity']?.toString());
      if (severity == null) return;
      body['severity'] = severity;
    } else if (const {
      'REQUEST_AMBULANCE',
      'REQUEST_POLICE',
      'REQUEST_FIRE',
    }.contains(action)) {
      final dispatch = await showDialog<Map<String, String>>(
        context: context,
        builder: (_) => _DispatchDialog(action: action),
      );
      if (dispatch == null) return;
      body.addAll(dispatch);
    } else if (const {
      'ADD_UPDATE',
      'NOTIFY_CONTACT',
      'STABILIZE',
      'RESOLVE',
      'CANCEL',
      'FALSE_ALARM',
      'REOPEN',
    }.contains(action)) {
      final note = await _noteDialog(_label(action), _noteLabel(action));
      if (note == null) return;
      body['note'] = note;
    } else {
      final confirmed = await _confirm(
        '${_label(action)} incident',
        '${_label(action)} ${item['incident_number']}?',
      );
      if (!confirmed) return;
    }
    try {
      await widget.client.patch('/api/emergencies/${item['id']}', body);
      await _load();
      _message('Incident updated.');
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<String?> _assignDialog() async {
    final responders = _maps(_data['responder_options']);
    if (responders.isEmpty) {
      _message('No staff member has Emergency Incidents permission.');
      return null;
    }
    String selected = responders.first['id'].toString();
    return showDialog<String>(
      context: context,
      builder: (dialog) => StatefulBuilder(
        builder: (_, setDialogState) => AlertDialog(
          title: const Text('Assign emergency responder'),
          content: DropdownButtonFormField<String>(
            initialValue: selected,
            decoration: const InputDecoration(labelText: 'Responder'),
            items: [
              for (final responder in responders)
                DropdownMenuItem(
                  value: responder['id'].toString(),
                  child: Text(
                    '${responder['name']} · ${responder['mobile'] ?? ''}',
                  ),
                ),
            ],
            onChanged: (value) {
              if (value != null) setDialogState(() => selected = value);
            },
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialog),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(dialog, selected),
              child: const Text('Assign'),
            ),
          ],
        ),
      ),
    );
  }

  Future<String?> _severityDialog(String? current) => showDialog<String>(
    context: context,
    builder: (dialog) => SimpleDialog(
      title: Text('Escalate from ${_label(current)}'),
      children: [
        for (final severity in const ['MEDIUM', 'HIGH', 'CRITICAL'])
          if (_severityRank(severity) > _severityRank(current))
            SimpleDialogOption(
              onPressed: () => Navigator.pop(dialog, severity),
              child: Text(_label(severity)),
            ),
      ],
    ),
  );

  Future<void> _history(Map<String, dynamic> item) async {
    try {
      final rows = _maps(
        await widget.client.get('/api/emergencies/${item['id']}/history'),
      );
      if (!mounted) return;
      await showDialog<void>(
        context: context,
        builder: (dialog) => AlertDialog(
          title: Text('Incident timeline · ${item['incident_number']}'),
          content: SizedBox(
            width: 680,
            child: rows.isEmpty
                ? const Text('No timeline entries.')
                : ListView.separated(
                    shrinkWrap: true,
                    itemCount: rows.length,
                    separatorBuilder: (_, _) => const Divider(),
                    itemBuilder: (_, index) {
                      final row = rows[index];
                      return ListTile(
                        leading: const Icon(Icons.history_toggle_off),
                        title: Text(_label(row['event_type'])),
                        subtitle: Text(
                          '${row['actor_name'] ?? _label(row['actor_role'])} · ${_dateTime(row['occurred_at'])}'
                          '${row['from_status'] == null ? '' : '\n${_label(row['from_status'])} → ${_label(row['to_status'])}'}'
                          '${row['note'] == null ? '' : '\n${row['note']}'}',
                        ),
                      );
                    },
                  ),
          ),
          actions: [
            FilledButton(
              onPressed: () => Navigator.pop(dialog),
              child: const Text('Close'),
            ),
          ],
        ),
      );
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _directory([Map<String, dynamic>? item]) async {
    final changed = await showDialog<bool>(
      context: context,
      builder: (_) => _DirectoryForm(client: widget.client, item: item),
    );
    if (changed == true) await _load();
  }

  Future<void> _removeDirectory(Map<String, dynamic> item) async {
    if (!await _confirm(
      'Remove contact',
      'Remove ${item['name']} from the directory?',
    )) {
      return;
    }
    try {
      await widget.client.delete('/api/emergencies/directory/${item['id']}');
      await _load();
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<String?> _noteDialog(String title, String label) async {
    final controller = TextEditingController();
    final result = await showDialog<String>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: Text(title),
        content: TextField(
          controller: controller,
          maxLines: 4,
          maxLength: 1000,
          decoration: InputDecoration(
            labelText: label,
            alignLabelWithHint: true,
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialog),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              if (controller.text.trim().isNotEmpty) {
                Navigator.pop(dialog, controller.text.trim());
              }
            },
            child: const Text('Submit'),
          ),
        ],
      ),
    );
    controller.dispose();
    return result;
  }

  Future<bool> _confirm(String title, String text) async =>
      await showDialog<bool>(
        context: context,
        builder: (dialog) => AlertDialog(
          title: Text(title),
          content: Text(text),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialog, false),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(dialog, true),
              child: const Text('Confirm'),
            ),
          ],
        ),
      ) ??
      false;

  void _message(String text) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
    }
  }

  @override
  Widget build(BuildContext context) =>
      BlocBuilder<
        AsyncDataBloc<Map<String, dynamic>>,
        AsyncDataState<Map<String, dynamic>>
      >(
        bloc: _bloc,
        builder: (context, state) {
          if (state.status == AsyncDataStatus.initial ||
              state.status == AsyncDataStatus.loading) {
            return const Center(child: CircularProgressIndicator());
          }
          if (state.status == AsyncDataStatus.failure) {
            return Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(state.error.toString()),
                  const SizedBox(height: 12),
                  FilledButton(onPressed: _load, child: const Text('Retry')),
                ],
              ),
            );
          }
          final incidents = _maps(_data['incidents']);
          final filtered = _status == 'ALL'
              ? incidents
              : incidents.where((row) => row['status'] == _status).toList();
          return RefreshIndicator(
            onRefresh: _load,
            child: ListView(
              padding: const EdgeInsets.all(20),
              children: [
                _EmergencyBanner(onReport: _create),
                const SizedBox(height: 16),
                Wrap(
                  spacing: 12,
                  runSpacing: 12,
                  children: [
                    _Metric('Total', _data['total'], Icons.sos_outlined),
                    _Metric('Open', _data['open'], Icons.warning_amber_rounded),
                    _Metric('Critical', _data['critical'], Icons.crisis_alert),
                    _Metric(
                      'Responding',
                      _data['responding'],
                      Icons.health_and_safety,
                    ),
                    _Metric(
                      'Resolved',
                      _data['resolved'],
                      Icons.verified_outlined,
                    ),
                  ],
                ),
                const SizedBox(height: 24),
                _SectionHeader(
                  title: 'Emergency directory',
                  subtitle:
                      'Verified society contacts. Use your phone to call the displayed number.',
                  action: _admin
                      ? FilledButton.tonalIcon(
                          onPressed: _directory,
                          icon: const Icon(Icons.add_call),
                          label: const Text('Add contact'),
                        )
                      : null,
                ),
                const SizedBox(height: 10),
                if (_maps(_data['directory']).isEmpty)
                  const _Empty('No society emergency contacts configured yet.')
                else
                  Wrap(
                    spacing: 12,
                    runSpacing: 12,
                    children: [
                      for (final contact in _maps(_data['directory']))
                        _DirectoryCard(
                          item: contact,
                          admin: _admin,
                          onEdit: () => _directory(contact),
                          onRemove: () => _removeDirectory(contact),
                        ),
                    ],
                  ),
                const SizedBox(height: 26),
                _SectionHeader(
                  title: _resident
                      ? 'My emergency incidents'
                      : 'Emergency command queue',
                  subtitle: _resident
                      ? 'Track response progress and add important updates.'
                      : 'Acknowledge, assign, respond, escalate, and close incidents.',
                  action: DropdownButton<String>(
                    value: _status,
                    items: [
                      for (final value in const [
                        'ALL',
                        'REPORTED',
                        'ACKNOWLEDGED',
                        'RESPONDING',
                        'ON_SCENE',
                        'STABILIZED',
                        'RESOLVED',
                        'CLOSED',
                        'CANCELLED',
                        'FALSE_ALARM',
                      ])
                        DropdownMenuItem(
                          value: value,
                          child: Text(_label(value)),
                        ),
                    ],
                    onChanged: (value) =>
                        setState(() => _status = value ?? 'ALL'),
                  ),
                ),
                const SizedBox(height: 10),
                if (filtered.isEmpty)
                  const _Empty('No incidents match this filter.')
                else
                  for (final item in filtered)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 12),
                      child: _IncidentCard(
                        item: item,
                        resident: _resident,
                        admin: _admin,
                        onAction: (action) => _action(item, action),
                        onHistory: () => _history(item),
                      ),
                    ),
              ],
            ),
          );
        },
      );
}

class _EmergencyBanner extends StatelessWidget {
  const _EmergencyBanner({required this.onReport});
  final VoidCallback onReport;

  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.all(20),
    decoration: BoxDecoration(
      color: const Color(0xff7f1d1d),
      borderRadius: BorderRadius.circular(18),
    ),
    child: LayoutBuilder(
      builder: (_, constraints) {
        final text = Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Emergency & accident response',
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                color: Colors.white,
                fontWeight: FontWeight.w800,
              ),
            ),
            const SizedBox(height: 6),
            const Text(
              'For immediate danger, call local emergency services first. This module alerts and coordinates your society response team.',
              style: TextStyle(color: Color(0xffffdddd), height: 1.4),
            ),
          ],
        );
        final button = FilledButton.icon(
          style: FilledButton.styleFrom(
            backgroundColor: Colors.white,
            foregroundColor: const Color(0xff991b1b),
          ),
          onPressed: onReport,
          icon: const Icon(Icons.sos),
          label: const Text('Report emergency'),
        );
        if (constraints.maxWidth < 650) {
          return Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [text, const SizedBox(height: 16), button],
          );
        }
        return Row(
          children: [
            Expanded(child: text),
            const SizedBox(width: 20),
            button,
          ],
        );
      },
    ),
  );
}

class _IncidentCard extends StatelessWidget {
  const _IncidentCard({
    required this.item,
    required this.resident,
    required this.admin,
    required this.onAction,
    required this.onHistory,
  });

  final Map<String, dynamic> item;
  final bool resident;
  final bool admin;
  final ValueChanged<String> onAction;
  final VoidCallback onHistory;

  bool get open => const {
    'REPORTED',
    'ACKNOWLEDGED',
    'RESPONDING',
    'ON_SCENE',
    'STABILIZED',
  }.contains(item['status']);

  @override
  Widget build(BuildContext context) {
    final severity = item['severity']?.toString() ?? 'LOW';
    return Card(
      clipBehavior: Clip.antiAlias,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Wrap(
              spacing: 8,
              runSpacing: 8,
              crossAxisAlignment: WrapCrossAlignment.center,
              children: [
                _Chip(_label(severity), _severityColor(severity)),
                _Chip(_label(item['status']), _statusColor(item['status'])),
                _Chip(_label(item['incident_type']), SocietyColors.blue),
                Text(
                  item['incident_number']?.toString() ?? '',
                  style: const TextStyle(
                    color: SocietyColors.textMuted,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Text(
              item['title']?.toString() ?? 'Emergency incident',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 5),
            Text(item['description']?.toString() ?? ''),
            const SizedBox(height: 12),
            Wrap(
              spacing: 16,
              runSpacing: 8,
              children: [
                _Fact(Icons.location_on_outlined, item['location_details']),
                if (item['block'] != null || item['space_number'] != null)
                  _Fact(
                    Icons.apartment,
                    '${item['block'] ?? ''} ${item['space_number'] ?? ''}',
                  ),
                if (item['resident_name'] != null)
                  _Fact(Icons.person_outline, item['resident_name']),
                if (item['assigned_staff_name'] != null)
                  _Fact(
                    Icons.health_and_safety_outlined,
                    'Responder: ${item['assigned_staff_name']}',
                  ),
                _Fact(
                  Icons.groups_outlined,
                  '${item['people_affected'] ?? 0} affected',
                ),
                _Fact(Icons.schedule, _dateTime(item['created_at'])),
              ],
            ),
            if (item['injury_details'] != null ||
                item['injured_person_name'] != null) ...[
              const Divider(height: 24),
              Text(
                'Injury: ${item['injured_person_name'] ?? 'Person not identified'} · ${item['injury_details'] ?? 'No details'}',
              ),
            ],
            if (item['ambulance_requested'] == true ||
                item['police_requested'] == true ||
                item['fire_service_requested'] == true ||
                item['emergency_contact_notified'] == true) ...[
              const SizedBox(height: 10),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: [
                  if (item['ambulance_requested'] == true)
                    const Chip(label: Text('Ambulance recorded')),
                  if (item['police_requested'] == true)
                    const Chip(label: Text('Police recorded')),
                  if (item['fire_service_requested'] == true)
                    const Chip(label: Text('Fire service recorded')),
                  if (item['emergency_contact_notified'] == true)
                    const Chip(label: Text('Emergency contact notified')),
                ],
              ),
            ],
            if (item['resolution_summary'] != null) ...[
              const Divider(height: 24),
              Text('Resolution: ${item['resolution_summary']}'),
            ],
            const SizedBox(height: 14),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                OutlinedButton.icon(
                  onPressed: onHistory,
                  icon: const Icon(Icons.history),
                  label: const Text('Timeline'),
                ),
                if (open)
                  OutlinedButton(
                    onPressed: () => onAction('ADD_UPDATE'),
                    child: const Text('Add update'),
                  ),
                if (resident &&
                    const {'REPORTED', 'ACKNOWLEDGED'}.contains(item['status']))
                  TextButton(
                    onPressed: () => onAction('CANCEL'),
                    child: const Text('Cancel report'),
                  ),
                if (!resident) ..._managerActions(),
              ],
            ),
          ],
        ),
      ),
    );
  }

  List<Widget> _managerActions() {
    final status = item['status'];
    return [
      if (status == 'REPORTED')
        FilledButton.tonal(
          onPressed: () => onAction('ACKNOWLEDGE'),
          child: const Text('Acknowledge'),
        ),
      if (const {'REPORTED', 'ACKNOWLEDGED'}.contains(status))
        FilledButton(
          onPressed: () => onAction('START_RESPONSE'),
          child: const Text('Start response'),
        ),
      if (open)
        OutlinedButton(
          onPressed: () => onAction('ASSIGN'),
          child: const Text('Assign responder'),
        ),
      if (status == 'RESPONDING')
        FilledButton(
          onPressed: () => onAction('ARRIVE'),
          child: const Text('Arrived on scene'),
        ),
      if (const {'RESPONDING', 'ON_SCENE'}.contains(status))
        FilledButton.tonal(
          onPressed: () => onAction('STABILIZE'),
          child: const Text('Stabilized'),
        ),
      if (open) ...[
        OutlinedButton(
          onPressed: () => onAction('REQUEST_AMBULANCE'),
          child: const Text('Record ambulance'),
        ),
        OutlinedButton(
          onPressed: () => onAction('REQUEST_POLICE'),
          child: const Text('Record police'),
        ),
        OutlinedButton(
          onPressed: () => onAction('REQUEST_FIRE'),
          child: const Text('Record fire service'),
        ),
        OutlinedButton(
          onPressed: () => onAction('NOTIFY_CONTACT'),
          child: const Text('Contact notified'),
        ),
        if (item['severity'] != 'CRITICAL')
          OutlinedButton(
            onPressed: () => onAction('ESCALATE'),
            child: const Text('Escalate'),
          ),
        FilledButton(
          onPressed: () => onAction('RESOLVE'),
          child: const Text('Resolve'),
        ),
        TextButton(
          onPressed: () => onAction('FALSE_ALARM'),
          child: const Text('False alarm'),
        ),
      ],
      if (admin && status == 'RESOLVED')
        FilledButton(
          onPressed: () => onAction('CLOSE'),
          child: const Text('Close'),
        ),
      if (admin &&
          const {
            'RESOLVED',
            'CLOSED',
            'CANCELLED',
            'FALSE_ALARM',
          }.contains(status))
        OutlinedButton(
          onPressed: () => onAction('REOPEN'),
          child: const Text('Reopen'),
        ),
    ];
  }
}

class _IncidentForm extends StatefulWidget {
  const _IncidentForm({
    required this.client,
    required this.resident,
    required this.options,
  });
  final BlocManagedApiGateway client;
  final bool resident;
  final List<Map<String, dynamic>> options;

  @override
  State<_IncidentForm> createState() => _IncidentFormState();
}

class _IncidentFormState extends State<_IncidentForm> {
  final _form = GlobalKey<FormState>();
  final _title = TextEditingController();
  final _description = TextEditingController();
  final _location = TextEditingController();
  final _affected = TextEditingController(text: '1');
  final _injuredName = TextEditingController();
  final _injuredMobile = TextEditingController();
  final _injury = TextEditingController();
  String _type = 'MEDICAL';
  String _severity = 'HIGH';
  String? _optionKey;
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    if (widget.options.isNotEmpty) _optionKey = _key(widget.options.first);
  }

  @override
  void dispose() {
    for (final controller in [
      _title,
      _description,
      _location,
      _affected,
      _injuredName,
      _injuredMobile,
      _injury,
    ]) {
      controller.dispose();
    }
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_form.currentState!.validate()) return;
    if (widget.resident && _optionKey == null) {
      _error('No occupied society space is linked to your profile.');
      return;
    }
    final option = widget.options
        .where((row) => _key(row) == _optionKey)
        .firstOrNull;
    setState(() => _saving = true);
    try {
      await widget.client.post('/api/emergencies', {
        if (option != null) 'residentId': option['resident_id'],
        if (option != null) 'flatId': option['flat_id'],
        'incidentType': _type,
        'severity': _severity,
        'title': _title.text.trim(),
        'description': _description.text.trim(),
        'locationDetails': _location.text.trim(),
        'peopleAffected': int.tryParse(_affected.text) ?? 0,
        'injuredPersonName': _injuredName.text.trim(),
        'injuredPersonMobile': _injuredMobile.text.trim(),
        'injuryDetails': _injury.text.trim(),
      });
      if (mounted) Navigator.pop(context, true);
    } catch (error) {
      _error(error.toString());
      if (mounted) setState(() => _saving = false);
    }
  }

  void _error(String text) {
    if (mounted)
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Report emergency or accident'),
    content: SizedBox(
      width: 720,
      child: Form(
        key: _form,
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Card(
                color: Color(0xfffff1f2),
                child: Padding(
                  padding: EdgeInsets.all(12),
                  child: Text(
                    'If anyone is in immediate danger, call the appropriate emergency service before submitting this report.',
                  ),
                ),
              ),
              if (widget.options.isNotEmpty)
                DropdownButtonFormField<String>(
                  initialValue: _optionKey,
                  isExpanded: true,
                  decoration: InputDecoration(
                    labelText: widget.resident
                        ? 'Your space'
                        : 'Resident / space (optional)',
                  ),
                  items: [
                    if (!widget.resident)
                      const DropdownMenuItem(
                        value: null,
                        child: Text('Common area / no resident'),
                      ),
                    for (final option in widget.options)
                      DropdownMenuItem(
                        value: _key(option),
                        child: Text(
                          '${option['resident_name']} · ${option['block'] ?? ''} ${option['space_number']}',
                        ),
                      ),
                  ],
                  onChanged: (value) => setState(() => _optionKey = value),
                ),
              const SizedBox(height: 12),
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: [
                  SizedBox(
                    width: 280,
                    child: DropdownButtonFormField<String>(
                      initialValue: _type,
                      isExpanded: true,
                      decoration: const InputDecoration(
                        labelText: 'Incident type',
                      ),
                      items: [
                        for (final value in const [
                          'MEDICAL',
                          'ACCIDENT',
                          'FIRE',
                          'SECURITY',
                          'GAS_LEAK',
                          'ELECTRICAL',
                          'ELEVATOR',
                          'NATURAL_DISASTER',
                          'OTHER',
                        ])
                          DropdownMenuItem(
                            value: value,
                            child: Text(_label(value)),
                          ),
                      ],
                      onChanged: (value) =>
                          setState(() => _type = value ?? _type),
                    ),
                  ),
                  SizedBox(
                    width: 240,
                    child: DropdownButtonFormField<String>(
                      initialValue: _severity,
                      decoration: const InputDecoration(labelText: 'Severity'),
                      items: [
                        for (final value in const [
                          'LOW',
                          'MEDIUM',
                          'HIGH',
                          'CRITICAL',
                        ])
                          DropdownMenuItem(
                            value: value,
                            child: Text(_label(value)),
                          ),
                      ],
                      onChanged: (value) =>
                          setState(() => _severity = value ?? _severity),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              _requiredField(_title, 'Short incident title', maxLength: 180),
              const SizedBox(height: 12),
              _requiredField(
                _description,
                'What happened?',
                maxLines: 4,
                maxLength: 1500,
              ),
              const SizedBox(height: 12),
              _requiredField(
                _location,
                'Exact location / landmark',
                maxLength: 300,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _affected,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(labelText: 'People affected'),
                validator: (value) {
                  final parsed = int.tryParse(value ?? '');
                  return parsed == null || parsed < 0 || parsed > 999
                      ? 'Enter 0 to 999'
                      : null;
                },
              ),
              const SizedBox(height: 18),
              Text(
                'Injury details (if applicable)',
                style: Theme.of(context).textTheme.titleMedium,
              ),
              const SizedBox(height: 10),
              TextFormField(
                controller: _injuredName,
                decoration: const InputDecoration(
                  labelText: 'Injured person name',
                ),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _injuredMobile,
                decoration: const InputDecoration(
                  labelText: 'Injured person mobile',
                ),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _injury,
                maxLines: 3,
                decoration: const InputDecoration(
                  labelText: 'Known injuries / condition',
                ),
              ),
            ],
          ),
        ),
      ),
    ),
    actions: [
      TextButton(
        onPressed: _saving ? null : () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton.icon(
        onPressed: _saving ? null : _submit,
        icon: const Icon(Icons.sos),
        label: Text(_saving ? 'Reporting…' : 'Report incident'),
      ),
    ],
  );
}

class _DispatchDialog extends StatefulWidget {
  const _DispatchDialog({required this.action});
  final String action;

  @override
  State<_DispatchDialog> createState() => _DispatchDialogState();
}

class _DispatchDialogState extends State<_DispatchDialog> {
  final note = TextEditingController();
  final reference = TextEditingController();
  final hospital = TextEditingController();

  @override
  void dispose() {
    note.dispose();
    reference.dispose();
    hospital.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: Text(_label(widget.action)),
    content: SizedBox(
      width: 500,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Text(
            'This records a call already made; it does not contact the service automatically.',
          ),
          const SizedBox(height: 12),
          TextField(
            controller: note,
            decoration: const InputDecoration(
              labelText: 'Dispatch / call details *',
            ),
            maxLines: 3,
          ),
          const SizedBox(height: 12),
          TextField(
            controller: reference,
            decoration: const InputDecoration(
              labelText: 'Call or dispatch reference',
            ),
          ),
          if (widget.action == 'REQUEST_AMBULANCE') ...[
            const SizedBox(height: 12),
            TextField(
              controller: hospital,
              decoration: const InputDecoration(
                labelText: 'Hospital (if known)',
              ),
            ),
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
        onPressed: () {
          if (note.text.trim().isEmpty) return;
          Navigator.pop(context, {
            'note': note.text.trim(),
            if (reference.text.trim().isNotEmpty)
              'externalReference': reference.text.trim(),
            if (hospital.text.trim().isNotEmpty)
              'hospitalName': hospital.text.trim(),
          });
        },
        child: const Text('Record call'),
      ),
    ],
  );
}

class _DirectoryForm extends StatefulWidget {
  const _DirectoryForm({required this.client, this.item});
  final BlocManagedApiGateway client;
  final Map<String, dynamic>? item;

  @override
  State<_DirectoryForm> createState() => _DirectoryFormState();
}

class _DirectoryFormState extends State<_DirectoryForm> {
  final form = GlobalKey<FormState>();
  late final name = TextEditingController(
    text: widget.item?['name']?.toString(),
  );
  late final phone = TextEditingController(
    text: widget.item?['phone']?.toString(),
  );
  late final alternate = TextEditingController(
    text: widget.item?['alternate_phone']?.toString(),
  );
  late final address = TextEditingController(
    text: widget.item?['address']?.toString(),
  );
  late final priority = TextEditingController(
    text: '${widget.item?['priority'] ?? 1}',
  );
  late String type = widget.item?['contact_type']?.toString() ?? 'AMBULANCE';
  late bool allDay = widget.item?['available_24x7'] == true;
  bool saving = false;

  @override
  void dispose() {
    name.dispose();
    phone.dispose();
    alternate.dispose();
    address.dispose();
    priority.dispose();
    super.dispose();
  }

  Future<void> submit() async {
    if (!form.currentState!.validate()) return;
    setState(() => saving = true);
    final body = {
      'contactType': type,
      'name': name.text.trim(),
      'phone': phone.text.trim(),
      'alternatePhone': alternate.text.trim(),
      'address': address.text.trim(),
      'available24x7': allDay,
      'priority': int.parse(priority.text),
    };
    try {
      if (widget.item == null) {
        await widget.client.post('/api/emergencies/directory', body);
      } else {
        await widget.client.put(
          '/api/emergencies/directory/${widget.item!['id']}',
          body,
        );
      }
      if (mounted) Navigator.pop(context, true);
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(error.toString())));
        setState(() => saving = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: Text(
      widget.item == null ? 'Add emergency contact' : 'Edit emergency contact',
    ),
    content: SizedBox(
      width: 560,
      child: Form(
        key: form,
        child: SingleChildScrollView(
          child: Column(
            children: [
              DropdownButtonFormField<String>(
                initialValue: type,
                decoration: const InputDecoration(labelText: 'Contact type'),
                items: [
                  for (final value in const [
                    'AMBULANCE',
                    'HOSPITAL',
                    'POLICE',
                    'FIRE',
                    'SOCIETY_SECURITY',
                    'DISASTER_CONTROL',
                    'OTHER',
                  ])
                    DropdownMenuItem(value: value, child: Text(_label(value))),
                ],
                onChanged: (value) => setState(() => type = value ?? type),
              ),
              const SizedBox(height: 12),
              _requiredField(name, 'Service / contact name'),
              const SizedBox(height: 12),
              _requiredField(phone, 'Phone number'),
              const SizedBox(height: 12),
              TextFormField(
                controller: alternate,
                decoration: const InputDecoration(labelText: 'Alternate phone'),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: address,
                decoration: const InputDecoration(labelText: 'Address'),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: priority,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'Display priority',
                ),
                validator: (value) {
                  final parsed = int.tryParse(value ?? '');
                  return parsed == null || parsed < 1 || parsed > 99
                      ? 'Enter 1 to 99'
                      : null;
                },
              ),
              SwitchListTile(
                value: allDay,
                contentPadding: EdgeInsets.zero,
                title: const Text('Available 24×7'),
                onChanged: (value) => setState(() => allDay = value),
              ),
            ],
          ),
        ),
      ),
    ),
    actions: [
      TextButton(
        onPressed: saving ? null : () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(
        onPressed: saving ? null : submit,
        child: Text(saving ? 'Saving…' : 'Save contact'),
      ),
    ],
  );
}

class _DirectoryCard extends StatelessWidget {
  const _DirectoryCard({
    required this.item,
    required this.admin,
    required this.onEdit,
    required this.onRemove,
  });
  final Map<String, dynamic> item;
  final bool admin;
  final VoidCallback onEdit;
  final VoidCallback onRemove;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: 310,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.call_outlined, color: Color(0xffb91c1c)),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    item['name']?.toString() ?? '',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                ),
                if (admin)
                  PopupMenuButton<String>(
                    onSelected: (value) =>
                        value == 'edit' ? onEdit() : onRemove(),
                    itemBuilder: (_) => const [
                      PopupMenuItem(value: 'edit', child: Text('Edit')),
                      PopupMenuItem(value: 'remove', child: Text('Remove')),
                    ],
                  ),
              ],
            ),
            Text(
              _label(item['contact_type']),
              style: const TextStyle(color: SocietyColors.textMuted),
            ),
            const SizedBox(height: 8),
            SelectableText(
              item['phone']?.toString() ?? '',
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w800),
            ),
            if (item['alternate_phone'] != null)
              SelectableText('Alternate: ${item['alternate_phone']}'),
            if (item['address'] != null)
              Padding(
                padding: const EdgeInsets.only(top: 6),
                child: Text(item['address'].toString()),
              ),
            if (item['available_24x7'] == true)
              const Padding(
                padding: EdgeInsets.only(top: 6),
                child: Text('Available 24×7'),
              ),
          ],
        ),
      ),
    ),
  );
}

class _Metric extends StatelessWidget {
  const _Metric(this.label, this.value, this.icon);
  final String label;
  final Object? value;
  final IconData icon;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: 170,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Row(
          children: [
            Icon(icon, color: const Color(0xffb91c1c)),
            const SizedBox(width: 10),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '${value ?? 0}',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  Text(
                    label,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(color: SocietyColors.textMuted),
                  ),
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
    this.action,
  });
  final String title;
  final String subtitle;
  final Widget? action;

  @override
  Widget build(BuildContext context) => LayoutBuilder(
    builder: (_, constraints) {
      final text = Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: Theme.of(context).textTheme.titleLarge),
          Text(
            subtitle,
            style: const TextStyle(color: SocietyColors.textMuted),
          ),
        ],
      );
      if (constraints.maxWidth < 650) {
        return Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            text,
            if (action != null) ...[
              const SizedBox(height: 8),
              Align(alignment: Alignment.centerLeft, child: action!),
            ],
          ],
        );
      }
      return Row(
        children: [
          Expanded(child: text),
          if (action != null) ...[const SizedBox(width: 12), action!],
        ],
      );
    },
  );
}

class _Fact extends StatelessWidget {
  const _Fact(this.icon, this.value);
  final IconData icon;
  final Object? value;

  @override
  Widget build(BuildContext context) => ConstrainedBox(
    constraints: const BoxConstraints(maxWidth: 320),
    child: Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 17, color: SocietyColors.textMuted),
        const SizedBox(width: 5),
        Flexible(
          child: Text(
            value?.toString() ?? '—',
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
        ),
      ],
    ),
  );
}

class _Chip extends StatelessWidget {
  const _Chip(this.label, this.color);
  final String label;
  final Color color;

  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 5),
    decoration: BoxDecoration(
      color: color.withValues(alpha: 0.12),
      borderRadius: BorderRadius.circular(30),
    ),
    child: Text(
      label,
      style: TextStyle(color: color, fontSize: 12, fontWeight: FontWeight.w800),
    ),
  );
}

class _Empty extends StatelessWidget {
  const _Empty(this.text);
  final String text;

  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(20),
      child: Text(text, style: const TextStyle(color: SocietyColors.textMuted)),
    ),
  );
}

TextFormField _requiredField(
  TextEditingController controller,
  String label, {
  int maxLines = 1,
  int? maxLength,
}) => TextFormField(
  controller: controller,
  maxLines: maxLines,
  maxLength: maxLength,
  decoration: InputDecoration(
    labelText: label,
    alignLabelWithHint: maxLines > 1,
  ),
  validator: (value) =>
      value == null || value.trim().isEmpty ? '$label is required' : null,
);

String _key(Map<String, dynamic> row) =>
    '${row['resident_id']}|${row['flat_id']}';

List<WorkspaceRecord> _maps(Object? value) => workspaceRecords(value);

String _label(Object? value) {
  if (value == null) return '—';
  final words = value.toString().replaceAll('_', ' ').toLowerCase();
  return words.isEmpty ? '—' : '${words[0].toUpperCase()}${words.substring(1)}';
}

String _dateTime(Object? value) {
  final parsed = DateTime.tryParse(value?.toString() ?? '')?.toLocal();
  if (parsed == null) return '—';
  String two(int number) => number.toString().padLeft(2, '0');
  return '${two(parsed.day)}/${two(parsed.month)}/${parsed.year} ${two(parsed.hour)}:${two(parsed.minute)}';
}

String _noteLabel(String action) => switch (action) {
  'ADD_UPDATE' => 'Situation update',
  'NOTIFY_CONTACT' => 'Who was contacted and when?',
  'STABILIZE' => 'Condition and stabilization details',
  'RESOLVE' => 'Resolution summary',
  'CANCEL' => 'Cancellation reason',
  'FALSE_ALARM' => 'False alarm reason',
  'REOPEN' => 'Reason for reopening',
  _ => 'Details',
};

int _severityRank(Object? severity) => switch (severity) {
  'LOW' => 1,
  'MEDIUM' => 2,
  'HIGH' => 3,
  'CRITICAL' => 4,
  _ => 0,
};

Color _severityColor(String severity) => switch (severity) {
  'CRITICAL' => const Color(0xff991b1b),
  'HIGH' => const Color(0xffdc2626),
  'MEDIUM' => const Color(0xffd97706),
  _ => const Color(0xff2563eb),
};

Color _statusColor(Object? status) => switch (status) {
  'RESOLVED' || 'CLOSED' => const Color(0xff15803d),
  'CANCELLED' || 'FALSE_ALARM' => SocietyColors.textMuted,
  'RESPONDING' || 'ON_SCENE' || 'STABILIZED' => const Color(0xffc2410c),
  _ => const Color(0xffb91c1c),
};
