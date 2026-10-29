import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';
import 'async_data_bloc.dart';
import 'theme.dart';
import 'workspace_record.dart';

enum ComplaintWorkspaceRole { societyAdmin, staff, resident }

Widget buildStaffComplaintWorkspace(BlocManagedApiGateway client) =>
    ComplaintWorkspace(client: client, role: ComplaintWorkspaceRole.staff);

Widget buildResidentComplaintWorkspace(BlocManagedApiGateway client) =>
    ComplaintWorkspace(client: client, role: ComplaintWorkspaceRole.resident);

class ComplaintWorkspace extends StatefulWidget {
  const ComplaintWorkspace({
    super.key,
    required this.client,
    required this.role,
  });

  final BlocManagedApiGateway client;
  final ComplaintWorkspaceRole role;

  @override
  State<ComplaintWorkspace> createState() => _ComplaintWorkspaceState();
}

class _ComplaintWorkspaceState extends State<ComplaintWorkspace> {
  late final AsyncDataBloc<_ComplaintData> _bloc;
  List<Map<String, dynamic>> get _complaints =>
      _bloc.state.data?.complaints ?? const [];
  Map<String, dynamic> get _options => _bloc.state.data?.options ?? const {};
  String _status = 'ALL';
  String _priority = 'ALL';

  @override
  void initState() {
    super.initState();
    _bloc = AsyncDataBloc(() => _ComplaintData.load(widget.client));
    _load();
  }

  Future<void> _load() => _bloc.refresh();

  @override
  void dispose() {
    _bloc.close();
    super.dispose();
  }

  Future<void> _create() async {
    final created = await showDialog<bool>(
      context: context,
      builder: (_) => _CreateComplaintDialog(
        client: widget.client,
        role: widget.role,
        options: _options,
      ),
    );
    if (created == true) await _load();
  }

  Future<void> _open(Map<String, dynamic> complaint) async {
    await showDialog<void>(
      context: context,
      builder: (_) => _ComplaintDetailDialog(
        client: widget.client,
        role: widget.role,
        complaintId: complaint['id'].toString(),
        options: _options,
      ),
    );
    await _load();
  }

  @override
  Widget build(
    BuildContext context,
  ) => BlocBuilder<AsyncDataBloc<_ComplaintData>, AsyncDataState<_ComplaintData>>(
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
              const Icon(
                Icons.error_outline,
                size: 42,
                color: SocietyColors.danger,
              ),
              const SizedBox(height: 12),
              Text(state.error.toString(), textAlign: TextAlign.center),
              const SizedBox(height: 12),
              FilledButton.icon(
                onPressed: _load,
                icon: const Icon(Icons.refresh),
                label: const Text('Try again'),
              ),
            ],
          ),
        );
      }
      final filtered = _complaints.where((item) {
        return (_status == 'ALL' || item['status'] == _status) &&
            (_priority == 'ALL' || item['priority'] == _priority);
      }).toList();
      final open = _complaints
          .where(
            (item) => !const {'RESOLVED', 'CLOSED'}.contains(item['status']),
          )
          .length;
      final overdue = _complaints
          .where((item) => item['overdue'] == true)
          .length;

      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Wrap(
              spacing: 16,
              runSpacing: 12,
              alignment: WrapAlignment.spaceBetween,
              crossAxisAlignment: WrapCrossAlignment.center,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Complaints',
                      style: Theme.of(context).textTheme.headlineMedium,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      widget.role == ComplaintWorkspaceRole.resident
                          ? 'Raise a concern and follow every progress update.'
                          : 'Create, assign, and track resident complaints to closure.',
                      style: const TextStyle(color: SocietyColors.textMuted),
                    ),
                  ],
                ),
                FilledButton.icon(
                  onPressed: _create,
                  icon: const Icon(Icons.add),
                  label: const Text('Raise complaint'),
                ),
              ],
            ),
            const SizedBox(height: 20),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                _SummaryCard(
                  label: 'Total',
                  value: _complaints.length,
                  icon: Icons.inbox_outlined,
                ),
                _SummaryCard(
                  label: 'Open',
                  value: open,
                  icon: Icons.pending_actions,
                ),
                _SummaryCard(
                  label: 'Overdue',
                  value: overdue,
                  icon: Icons.warning_amber_rounded,
                  danger: overdue > 0,
                ),
              ],
            ),
            const SizedBox(height: 20),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                SizedBox(
                  width: 210,
                  child: DropdownButtonFormField<String>(
                    initialValue: _status,
                    isExpanded: true,
                    decoration: const InputDecoration(labelText: 'Status'),
                    items:
                        const [
                              'ALL',
                              'OPEN',
                              'ASSIGNED',
                              'IN_PROGRESS',
                              'RESOLVED',
                              'CLOSED',
                            ]
                            .map(
                              (value) => DropdownMenuItem(
                                value: value,
                                child: Text(
                                  _label(value),
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                            )
                            .toList(),
                    onChanged: (value) => setState(() => _status = value!),
                  ),
                ),
                SizedBox(
                  width: 190,
                  child: DropdownButtonFormField<String>(
                    initialValue: _priority,
                    isExpanded: true,
                    decoration: const InputDecoration(labelText: 'Priority'),
                    items: const ['ALL', 'LOW', 'NORMAL', 'HIGH', 'URGENT']
                        .map(
                          (value) => DropdownMenuItem(
                            value: value,
                            child: Text(
                              _label(value),
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                        )
                        .toList(),
                    onChanged: (value) => setState(() => _priority = value!),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            if (filtered.isEmpty)
              const Card(
                child: Padding(
                  padding: EdgeInsets.all(36),
                  child: Column(
                    children: [
                      Icon(
                        Icons.task_alt,
                        size: 42,
                        color: SocietyColors.textMuted,
                      ),
                      SizedBox(height: 10),
                      Text('No complaints match these filters.'),
                    ],
                  ),
                ),
              )
            else
              for (final item in filtered) ...[
                _ComplaintCard(item: item, onTap: () => _open(item)),
                const SizedBox(height: 10),
              ],
          ],
        ),
      );
    },
  );
}

class _ComplaintData {
  const _ComplaintData({required this.complaints, required this.options});
  final List<WorkspaceRecord> complaints;
  final WorkspaceRecord options;

  static Future<_ComplaintData> load(BlocManagedApiGateway client) async {
    final values = await Future.wait([
      client.get('/api/complaints'),
      client.get('/api/complaints/options'),
    ]);
    return _ComplaintData(
      complaints: workspaceRecords(values[0]),
      options: WorkspaceRecord.from(values[1]),
    );
  }
}

class _CreateComplaintDialog extends StatefulWidget {
  const _CreateComplaintDialog({
    required this.client,
    required this.role,
    required this.options,
  });
  final BlocManagedApiGateway client;
  final ComplaintWorkspaceRole role;
  final Map<String, dynamic> options;

  @override
  State<_CreateComplaintDialog> createState() => _CreateComplaintDialogState();
}

class _CreateComplaintDialogState extends State<_CreateComplaintDialog> {
  final _formKey = GlobalKey<FormState>();
  final _title = TextEditingController();
  final _description = TextEditingController();
  String? _spaceId;
  String? _categoryId;
  String? _assignedTo;
  String _priority = 'NORMAL';
  bool _saving = false;
  String? _error;

  List<Map<String, dynamic>> get _spaces {
    final key = widget.role == ComplaintWorkspaceRole.resident
        ? 'spaces'
        : 'residents';
    return (widget.options[key] as List? ?? const [])
        .map((item) => Map<String, dynamic>.from(item as Map))
        .toList();
  }

  List<Map<String, dynamic>> _list(String key) =>
      (widget.options[key] as List? ?? const [])
          .map((item) => Map<String, dynamic>.from(item as Map))
          .toList();

  @override
  void dispose() {
    _title.dispose();
    _description.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    if (_spaceId == null) {
      setState(
        () => _error = widget.role == ComplaintWorkspaceRole.resident
            ? 'No space is linked to your account.'
            : 'Select a resident and space.',
      );
      return;
    }
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      final space = _spaces.firstWhere(
        (item) => item['space_id'].toString() == _spaceId,
      );
      final staffId = widget.role == ComplaintWorkspaceRole.staff
          ? widget.options['current_user_id']?.toString()
          : _assignedTo;
      await widget.client.post('/api/complaints', {
        if (widget.role != ComplaintWorkspaceRole.resident)
          'residentId': space['resident_id']?.toString(),
        'flatId': _spaceId,
        if (_categoryId != null) 'categoryId': _categoryId,
        if (staffId != null) 'assignedTo': staffId,
        'title': _title.text.trim(),
        'description': _description.text.trim(),
        'priority': _priority,
      });
      if (mounted) Navigator.pop(context, true);
    } catch (error) {
      if (mounted) setState(() => _error = error.toString());
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final categories = _list('categories');
    final staff = _list('staff');
    return AlertDialog(
      title: const Text('Raise complaint'),
      content: SizedBox(
        width: 600,
        child: SingleChildScrollView(
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                DropdownButtonFormField<String>(
                  initialValue: _spaceId,
                  isExpanded: true,
                  decoration: InputDecoration(
                    labelText: widget.role == ComplaintWorkspaceRole.resident
                        ? 'Your space'
                        : 'Resident and space',
                  ),
                  items: _spaces.map((item) {
                    final resident = item['full_name'] == null
                        ? ''
                        : '${item['full_name']} · ';
                    final block = item['block'] == null
                        ? ''
                        : '${item['block']} / ';
                    return DropdownMenuItem(
                      value: item['space_id'].toString(),
                      child: Text(
                        '$resident$block${item['space_number']}',
                        overflow: TextOverflow.ellipsis,
                      ),
                    );
                  }).toList(),
                  onChanged: (value) => setState(() => _spaceId = value),
                  validator: (value) =>
                      value == null ? 'Please select a space' : null,
                ),
                const SizedBox(height: 12),
                DropdownButtonFormField<String?>(
                  initialValue: _categoryId,
                  isExpanded: true,
                  decoration: const InputDecoration(labelText: 'Category'),
                  items: [
                    const DropdownMenuItem<String?>(
                      value: null,
                      child: Text('General'),
                    ),
                    ...categories.map(
                      (item) => DropdownMenuItem<String?>(
                        value: item['id'].toString(),
                        child: Text(
                          '${item['name']} · ${item['sla_hours']}h SLA',
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ),
                  ],
                  onChanged: (value) => setState(() => _categoryId = value),
                ),
                const SizedBox(height: 12),
                DropdownButtonFormField<String>(
                  initialValue: _priority,
                  isExpanded: true,
                  decoration: const InputDecoration(labelText: 'Priority'),
                  items: const ['LOW', 'NORMAL', 'HIGH', 'URGENT']
                      .map(
                        (value) => DropdownMenuItem(
                          value: value,
                          child: Text(_label(value)),
                        ),
                      )
                      .toList(),
                  onChanged: (value) => setState(() => _priority = value!),
                ),
                if (widget.role == ComplaintWorkspaceRole.societyAdmin) ...[
                  const SizedBox(height: 12),
                  DropdownButtonFormField<String?>(
                    initialValue: _assignedTo,
                    isExpanded: true,
                    decoration: const InputDecoration(
                      labelText: 'Assign to staff (optional)',
                    ),
                    items: [
                      const DropdownMenuItem<String?>(
                        value: null,
                        child: Text('Leave unassigned'),
                      ),
                      ...staff.map(
                        (item) => DropdownMenuItem<String?>(
                          value: item['id'].toString(),
                          child: Text(
                            item['full_name']?.toString() ?? 'Staff',
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      ),
                    ],
                    onChanged: (value) => setState(() => _assignedTo = value),
                  ),
                ],
                const SizedBox(height: 12),
                TextFormField(
                  controller: _title,
                  decoration: const InputDecoration(
                    labelText: 'Complaint title',
                  ),
                  validator: (value) => value == null || value.trim().isEmpty
                      ? 'Title is required'
                      : null,
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _description,
                  minLines: 4,
                  maxLines: 7,
                  decoration: const InputDecoration(
                    labelText: 'Describe the problem',
                  ),
                  validator: (value) => value == null || value.trim().isEmpty
                      ? 'Description is required'
                      : null,
                ),
                if (_error != null) ...[
                  const SizedBox(height: 12),
                  Text(
                    _error!,
                    style: const TextStyle(color: SocietyColors.danger),
                  ),
                ],
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
          onPressed: _saving ? null : _save,
          icon: _saving
              ? const SizedBox(
                  width: 18,
                  height: 18,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: Colors.white,
                  ),
                )
              : const Icon(Icons.send),
          label: const Text('Submit'),
        ),
      ],
    );
  }
}

class _ComplaintDetailDialog extends StatefulWidget {
  const _ComplaintDetailDialog({
    required this.client,
    required this.role,
    required this.complaintId,
    required this.options,
  });
  final BlocManagedApiGateway client;
  final ComplaintWorkspaceRole role;
  final String complaintId;
  final Map<String, dynamic> options;

  @override
  State<_ComplaintDetailDialog> createState() => _ComplaintDetailDialogState();
}

class _ComplaintDetailDialogState extends State<_ComplaintDetailDialog> {
  late final AsyncDataBloc<Map<String, dynamic>> _complaintBloc;

  Map<String, dynamic>? get _item => _complaintBloc.state.data;

  @override
  void initState() {
    super.initState();
    _complaintBloc = AsyncDataBloc(
      () async => Map<String, dynamic>.from(
        await widget.client.get('/api/complaints/${widget.complaintId}') as Map,
      ),
    )..refresh();
  }

  @override
  void dispose() {
    _complaintBloc.close();
    super.dispose();
  }

  Future<void> _load() => _complaintBloc.refresh();

  Future<void> _comment() async {
    final controller = TextEditingController();
    final note = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Add comment'),
        content: TextField(
          controller: controller,
          autofocus: true,
          minLines: 3,
          maxLines: 6,
          decoration: const InputDecoration(
            labelText: 'Comment or additional information',
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, controller.text.trim()),
            child: const Text('Add comment'),
          ),
        ],
      ),
    );
    controller.dispose();
    if (note == null || note.isEmpty) return;
    try {
      await widget.client.post(
        '/api/complaints/${widget.complaintId}/comments',
        {'note': note},
      );
      await _load();
    } catch (error) {
      if (mounted) _message(error.toString());
    }
  }

  Future<void> _progress() async {
    final changed = await showDialog<bool>(
      context: context,
      builder: (_) => _ProgressDialog(
        client: widget.client,
        role: widget.role,
        complaint: _item!,
        options: widget.options,
      ),
    );
    if (changed == true) await _load();
  }

  void _message(String value) => ScaffoldMessenger.of(
    context,
  ).showSnackBar(SnackBar(content: Text(value)));

  @override
  Widget build(BuildContext context) =>
      BlocBuilder<
        AsyncDataBloc<Map<String, dynamic>>,
        AsyncDataState<Map<String, dynamic>>
      >(
        bloc: _complaintBloc,
        builder: (context, state) {
          final _error = state.status == AsyncDataStatus.failure
              ? state.error.toString()
              : null;
          if (_error != null) {
            return AlertDialog(
              title: const Text('Complaint'),
              content: Text(_error),
              actions: [
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Close'),
                ),
              ],
            );
          }
          if (_item == null) {
            return const AlertDialog(
              content: SizedBox(
                width: 520,
                height: 180,
                child: Center(child: CircularProgressIndicator()),
              ),
            );
          }
          final item = _item!;
          final timeline = (item['timeline'] as List? ?? const [])
              .map((entry) => Map<String, dynamic>.from(entry as Map))
              .toList()
              .reversed;
          final canProgress =
              widget.role != ComplaintWorkspaceRole.resident &&
              item['status'] != 'CLOSED';
          return AlertDialog(
            title: Row(
              children: [
                Expanded(
                  child: Text(item['reference']?.toString() ?? 'Complaint'),
                ),
                _StatusChip(value: item['status']?.toString() ?? 'OPEN'),
              ],
            ),
            content: SizedBox(
              width: 720,
              child: SingleChildScrollView(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      item['title']?.toString() ?? '',
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    const SizedBox(height: 8),
                    Text(item['description']?.toString() ?? ''),
                    const SizedBox(height: 18),
                    Wrap(
                      spacing: 18,
                      runSpacing: 12,
                      children: [
                        _Fact('Resident', item['resident_name']),
                        _Fact('Space', _space(item)),
                        _Fact('Category', item['category_name']),
                        _Fact(
                          'Priority',
                          _label(item['priority']?.toString() ?? 'NORMAL'),
                        ),
                        _Fact(
                          'Assigned to',
                          item['assigned_to_name'] ?? 'Unassigned',
                        ),
                        _Fact('Raised by', item['created_by_name']),
                        _Fact('Due', _date(item['due_at'])),
                      ],
                    ),
                    if (item['overdue'] == true) ...[
                      const SizedBox(height: 14),
                      const Text(
                        'This complaint is overdue.',
                        style: TextStyle(
                          color: SocietyColors.danger,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ],
                    if (item['resolution'] != null) ...[
                      const SizedBox(height: 18),
                      Container(
                        width: double.infinity,
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: SocietyColors.success.withValues(alpha: .08),
                          borderRadius: BorderRadius.circular(14),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Resolution',
                              style: TextStyle(
                                fontWeight: FontWeight.w700,
                                color: SocietyColors.success,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(item['resolution'].toString()),
                          ],
                        ),
                      ),
                    ],
                    const SizedBox(height: 24),
                    Text(
                      'Progress timeline',
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    const SizedBox(height: 10),
                    if (timeline.isEmpty)
                      const Text(
                        'No progress recorded yet.',
                        style: TextStyle(color: SocietyColors.textMuted),
                      )
                    else
                      for (final update in timeline)
                        _TimelineItem(update: update),
                  ],
                ),
              ),
            ),
            actions: [
              TextButton.icon(
                onPressed: _comment,
                icon: const Icon(Icons.add_comment_outlined),
                label: const Text('Add comment'),
              ),
              if (canProgress)
                FilledButton.icon(
                  onPressed: _progress,
                  icon: const Icon(Icons.update),
                  label: const Text('Update progress'),
                ),
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('Close'),
              ),
            ],
          );
        },
      );
}

class _ProgressDialog extends StatefulWidget {
  const _ProgressDialog({
    required this.client,
    required this.role,
    required this.complaint,
    required this.options,
  });
  final BlocManagedApiGateway client;
  final ComplaintWorkspaceRole role;
  final Map<String, dynamic> complaint;
  final Map<String, dynamic> options;

  @override
  State<_ProgressDialog> createState() => _ProgressDialogState();
}

class _ProgressDialogState extends State<_ProgressDialog> {
  final _note = TextEditingController();
  final _resolution = TextEditingController();
  late String _status = widget.complaint['status']?.toString() ?? 'OPEN';
  late String? _assignedTo = widget.complaint['assigned_to']?.toString();
  bool _saving = false;
  String? _error;

  @override
  void dispose() {
    _note.dispose();
    _resolution.dispose();
    super.dispose();
  }

  List<String> get _statuses {
    final current = widget.complaint['status']?.toString() ?? 'OPEN';
    final transitions = <String, List<String>>{
      'OPEN': ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'CLOSED'],
      'ASSIGNED': ['ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'],
      'IN_PROGRESS': ['IN_PROGRESS', 'RESOLVED', 'CLOSED'],
      'RESOLVED': ['RESOLVED', 'IN_PROGRESS', 'CLOSED'],
      'CLOSED': ['CLOSED'],
    };
    return transitions[current] ?? [current];
  }

  Future<void> _save() async {
    if (const {'RESOLVED', 'CLOSED'}.contains(_status) &&
        _resolution.text.trim().isEmpty &&
        widget.complaint['resolution'] == null) {
      setState(
        () => _error = 'Add the resolution before resolving or closing.',
      );
      return;
    }
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await widget.client.patch('/api/complaints/${widget.complaint['id']}', {
        'status': _status,
        if (_assignedTo != null) 'assignedTo': _assignedTo,
        if (_note.text.trim().isNotEmpty) 'note': _note.text.trim(),
        if (_resolution.text.trim().isNotEmpty)
          'resolution': _resolution.text.trim(),
      });
      if (mounted) Navigator.pop(context, true);
    } catch (error) {
      if (mounted) setState(() => _error = error.toString());
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final staff = (widget.options['staff'] as List? ?? const [])
        .map((item) => Map<String, dynamic>.from(item as Map))
        .toList();
    return AlertDialog(
      title: const Text('Update complaint progress'),
      content: SizedBox(
        width: 520,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (widget.role == ComplaintWorkspaceRole.societyAdmin) ...[
                DropdownButtonFormField<String?>(
                  initialValue: _assignedTo,
                  isExpanded: true,
                  decoration: const InputDecoration(
                    labelText: 'Assigned staff',
                  ),
                  hint: const Text('Choose staff (optional)'),
                  items: [
                    ...staff.map(
                      (item) => DropdownMenuItem<String?>(
                        value: item['id'].toString(),
                        child: Text(
                          item['full_name']?.toString() ?? 'Staff',
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ),
                  ],
                  onChanged: (value) => setState(() {
                    _assignedTo = value;
                    if (value != null && _status == 'OPEN') {
                      _status = 'ASSIGNED';
                    }
                  }),
                ),
                const SizedBox(height: 12),
              ],
              DropdownButtonFormField<String>(
                initialValue: _status,
                isExpanded: true,
                decoration: const InputDecoration(labelText: 'Status'),
                items: _statuses
                    .map(
                      (value) => DropdownMenuItem(
                        value: value,
                        child: Text(_label(value)),
                      ),
                    )
                    .toList(),
                onChanged: (value) => setState(() => _status = value!),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: _note,
                minLines: 3,
                maxLines: 5,
                decoration: const InputDecoration(labelText: 'Progress note'),
              ),
              if (const {'RESOLVED', 'CLOSED'}.contains(_status)) ...[
                const SizedBox(height: 12),
                TextField(
                  controller: _resolution,
                  minLines: 3,
                  maxLines: 5,
                  decoration: InputDecoration(
                    labelText: 'Resolution *',
                    hintText: widget.complaint['resolution']?.toString(),
                  ),
                ),
              ],
              if (_error != null) ...[
                const SizedBox(height: 12),
                Text(
                  _error!,
                  style: const TextStyle(color: SocietyColors.danger),
                ),
              ],
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: _saving ? null : () => Navigator.pop(context),
          child: const Text('Cancel'),
        ),
        FilledButton(
          onPressed: _saving ? null : _save,
          child: Text(_saving ? 'Saving…' : 'Save update'),
        ),
      ],
    );
  }
}

class _ComplaintCard extends StatelessWidget {
  const _ComplaintCard({required this.item, required this.onTap});
  final Map<String, dynamic> item;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => Card(
    child: InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(20),
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Row(
          children: [
            Container(
              width: 46,
              height: 46,
              decoration: BoxDecoration(
                color: _priorityColor(
                  item['priority']?.toString(),
                ).withValues(alpha: .1),
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(
                Icons.support_agent,
                color: _priorityColor(item['priority']?.toString()),
              ),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          item['title']?.toString() ?? '',
                          style: const TextStyle(
                            fontWeight: FontWeight.w700,
                            fontSize: 16,
                          ),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      const SizedBox(width: 8),
                      _StatusChip(value: item['status']?.toString() ?? 'OPEN'),
                    ],
                  ),
                  const SizedBox(height: 5),
                  Text(
                    '${item['reference']} · ${item['resident_name'] ?? ''} · ${_space(item)}',
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(color: SocietyColors.textMuted),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${_label(item['priority']?.toString() ?? 'NORMAL')} · ${item['category_name'] ?? 'General'} · ${item['assigned_to_name'] ?? 'Unassigned'}',
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      color: item['overdue'] == true
                          ? SocietyColors.danger
                          : SocietyColors.textMuted,
                      fontSize: 12,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 8),
            const Icon(Icons.chevron_right),
          ],
        ),
      ),
    ),
  );
}

class _StatusChip extends StatelessWidget {
  const _StatusChip({required this.value});
  final String value;
  @override
  Widget build(BuildContext context) {
    final color = switch (value) {
      'RESOLVED' || 'CLOSED' => SocietyColors.success,
      'IN_PROGRESS' || 'ASSIGNED' => SocietyColors.blue,
      _ => SocietyColors.warning,
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
        color: color.withValues(alpha: .1),
        borderRadius: BorderRadius.circular(100),
      ),
      child: Text(
        _label(value),
        style: TextStyle(
          color: color,
          fontSize: 11,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class _SummaryCard extends StatelessWidget {
  const _SummaryCard({
    required this.label,
    required this.value,
    required this.icon,
    this.danger = false,
  });
  final String label;
  final int value;
  final IconData icon;
  final bool danger;
  @override
  Widget build(BuildContext context) => SizedBox(
    width: 175,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Icon(
              icon,
              color: danger ? SocietyColors.danger : SocietyColors.blue,
            ),
            const SizedBox(width: 12),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('$value', style: Theme.of(context).textTheme.titleLarge),
                Text(
                  label,
                  style: const TextStyle(color: SocietyColors.textMuted),
                ),
              ],
            ),
          ],
        ),
      ),
    ),
  );
}

class _Fact extends StatelessWidget {
  const _Fact(this.label, this.value);
  final String label;
  final Object? value;
  @override
  Widget build(BuildContext context) => SizedBox(
    width: 200,
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(color: SocietyColors.textMuted, fontSize: 12),
        ),
        const SizedBox(height: 2),
        Text(
          value?.toString() ?? '—',
          style: const TextStyle(fontWeight: FontWeight.w600),
        ),
      ],
    ),
  );
}

class _TimelineItem extends StatelessWidget {
  const _TimelineItem({required this.update});
  final Map<String, dynamic> update;
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(bottom: 14),
    child: Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          width: 10,
          height: 10,
          margin: const EdgeInsets.only(top: 5),
          decoration: const BoxDecoration(
            color: SocietyColors.blue,
            shape: BoxShape.circle,
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                _eventLabel(update),
                style: const TextStyle(fontWeight: FontWeight.w700),
              ),
              Text(
                '${update['actor_name'] ?? 'User'} · ${_date(update['created_at'])}',
                style: const TextStyle(
                  color: SocietyColors.textMuted,
                  fontSize: 12,
                ),
              ),
              if (update['note'] != null &&
                  update['note'].toString().trim().isNotEmpty) ...[
                const SizedBox(height: 4),
                Text(update['note'].toString()),
              ],
            ],
          ),
        ),
      ],
    ),
  );
}

String _eventLabel(Map<String, dynamic> update) {
  if (update['event_type'] == 'STATUS_CHANGED') {
    return 'Status changed from ${_label(update['from_status']?.toString() ?? '')} to ${_label(update['to_status']?.toString() ?? '')}';
  }
  return switch (update['event_type']) {
    'CREATED' => 'Complaint raised',
    'COMMENT' => 'Comment added',
    'ASSIGNMENT_CHANGED' => 'Assignment updated',
    'PROGRESS_NOTE' => 'Progress updated',
    _ => _label(update['event_type']?.toString() ?? 'Update'),
  };
}

String _space(Map<String, dynamic> item) {
  final block = item['block']?.toString();
  final number = item['space_number']?.toString() ?? 'No space';
  return block == null || block.isEmpty ? number : '$block / $number';
}

String _label(String value) => value
    .toLowerCase()
    .split('_')
    .where((part) => part.isNotEmpty)
    .map((part) => '${part[0].toUpperCase()}${part.substring(1)}')
    .join(' ');

String _date(Object? value) {
  if (value == null) return '—';
  final parsed = DateTime.tryParse(value.toString());
  if (parsed == null) return value.toString();
  final local = parsed.toLocal();
  String two(int number) => number.toString().padLeft(2, '0');
  return '${two(local.day)}/${two(local.month)}/${local.year} ${two(local.hour)}:${two(local.minute)}';
}

Color _priorityColor(String? value) => switch (value) {
  'URGENT' => SocietyColors.danger,
  'HIGH' => SocietyColors.warning,
  'LOW' => SocietyColors.teal,
  _ => SocietyColors.blue,
};
