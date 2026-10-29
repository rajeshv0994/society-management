import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';
import 'async_data_bloc.dart';
import 'theme.dart';
import 'workspace_record.dart';

enum TaskWorkspaceRole { societyAdmin, staff }

Widget buildStaffTaskWorkspace(BlocManagedApiGateway client) =>
    TaskWorkspace(client: client, role: TaskWorkspaceRole.staff);

class TaskWorkspace extends StatefulWidget {
  const TaskWorkspace({super.key, required this.client, required this.role});
  final BlocManagedApiGateway client;
  final TaskWorkspaceRole role;

  @override
  State<TaskWorkspace> createState() => _TaskWorkspaceState();
}

class _TaskWorkspaceState extends State<TaskWorkspace> {
  late final AsyncDataBloc<_TaskData> _bloc;
  List<Map<String, dynamic>> get _tasks => _bloc.state.data?.tasks ?? const [];
  Map<String, dynamic> get _options => _bloc.state.data?.options ?? const {};
  String _filter = 'ALL';
  String _query = '';

  bool get _admin => widget.role == TaskWorkspaceRole.societyAdmin;

  @override
  void initState() {
    super.initState();
    _bloc = AsyncDataBloc(() => _TaskData.load(widget.client));
    _load();
  }

  Future<void> _load() => _bloc.refresh();

  @override
  void dispose() {
    _bloc.close();
    super.dispose();
  }

  Future<void> _edit([Map<String, dynamic>? task]) async {
    final saved = await showDialog<bool>(
      context: context,
      builder: (_) =>
          _TaskEditor(client: widget.client, options: _options, existing: task),
    );
    if (saved == true) {
      _message(task == null ? 'Task assigned.' : 'Task updated.');
      await _load();
    }
  }

  Future<void> _open(Map<String, dynamic> task) async {
    await showDialog<void>(
      context: context,
      builder: (_) => _TaskDetailDialog(
        client: widget.client,
        role: widget.role,
        taskId: task['id'].toString(),
        options: _options,
        onEdit: _admin ? _edit : null,
      ),
    );
    await _load();
  }

  void _message(String text) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
    }
  }

  @override
  Widget build(
    BuildContext context,
  ) => BlocBuilder<AsyncDataBloc<_TaskData>, AsyncDataState<_TaskData>>(
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
                size: 44,
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
      final open = _tasks.where((item) => item['status'] == 'OPEN').length;
      final active = _tasks
          .where((item) => item['status'] == 'IN_PROGRESS')
          .length;
      final overdue = _tasks.where((item) => item['overdue'] == true).length;
      final completed = _tasks
          .where((item) => item['status'] == 'COMPLETED')
          .length;
      final query = _query.trim().toLowerCase();
      final rows = _tasks.where((task) {
        final statusMatch =
            _filter == 'ALL' ||
            (_filter == 'OVERDUE'
                ? task['overdue'] == true
                : task['status'] == _filter);
        if (!statusMatch) return false;
        if (query.isEmpty) return true;
        return [
          task['reference'],
          task['title'],
          task['description'],
          task['assigned_to_name'],
        ].any(
          (value) => value?.toString().toLowerCase().contains(query) == true,
        );
      }).toList();

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
                      _admin ? 'Staff tasks' : 'My duties',
                      style: Theme.of(context).textTheme.headlineMedium,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      _admin
                          ? 'Assign work and track staff progress to completion.'
                          : 'Start assigned work, report blockers, and record completion.',
                      style: const TextStyle(color: SocietyColors.textMuted),
                    ),
                  ],
                ),
                if (_admin)
                  FilledButton.icon(
                    onPressed: _edit,
                    icon: const Icon(Icons.add_task),
                    label: const Text('Assign task'),
                  ),
              ],
            ),
            const SizedBox(height: 18),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                _TaskMetric(
                  'Open',
                  open,
                  Icons.inbox_outlined,
                  SocietyColors.warning,
                ),
                _TaskMetric(
                  'In progress',
                  active,
                  Icons.play_circle_outline,
                  SocietyColors.blue,
                ),
                _TaskMetric(
                  'Overdue',
                  overdue,
                  Icons.warning_amber,
                  overdue > 0 ? SocietyColors.danger : SocietyColors.blue,
                ),
                _TaskMetric(
                  'Completed',
                  completed,
                  Icons.task_alt,
                  SocietyColors.success,
                ),
              ],
            ),
            const SizedBox(height: 18),
            TextField(
              onChanged: (value) => setState(() => _query = value),
              decoration: InputDecoration(
                prefixIcon: const Icon(Icons.search),
                labelText: _admin
                    ? 'Search task or assigned staff'
                    : 'Search my tasks',
              ),
            ),
            const SizedBox(height: 12),
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Wrap(
                spacing: 8,
                children:
                    [
                          'ALL',
                          'OPEN',
                          'IN_PROGRESS',
                          'BLOCKED',
                          'OVERDUE',
                          'COMPLETED',
                          if (_admin) 'CANCELLED',
                        ]
                        .map(
                          (value) => ChoiceChip(
                            label: Text(_taskLabel(value)),
                            selected: _filter == value,
                            onSelected: (_) => setState(() => _filter = value),
                          ),
                        )
                        .toList(),
              ),
            ),
            const SizedBox(height: 14),
            if (rows.isEmpty)
              const Card(
                child: Padding(
                  padding: EdgeInsets.all(36),
                  child: Column(
                    children: [
                      Icon(
                        Icons.task_alt,
                        size: 44,
                        color: SocietyColors.textMuted,
                      ),
                      SizedBox(height: 10),
                      Text('No tasks match this view.'),
                    ],
                  ),
                ),
              )
            else
              for (final task in rows) ...[
                _TaskCard(
                  task: task,
                  admin: _admin,
                  onOpen: () => _open(task),
                  onEdit: () => _edit(task),
                ),
                const SizedBox(height: 10),
              ],
          ],
        ),
      );
    },
  );
}

class _TaskData {
  const _TaskData({required this.tasks, required this.options});
  final List<WorkspaceRecord> tasks;
  final WorkspaceRecord options;

  static Future<_TaskData> load(BlocManagedApiGateway client) async {
    final values = await Future.wait([
      client.get('/api/tasks'),
      client.get('/api/tasks/options'),
    ]);
    return _TaskData(
      tasks: workspaceRecords(values[0]),
      options: WorkspaceRecord.from(values[1]),
    );
  }
}

class _TaskEditor extends StatefulWidget {
  const _TaskEditor({
    required this.client,
    required this.options,
    this.existing,
  });
  final BlocManagedApiGateway client;
  final Map<String, dynamic> options;
  final Map<String, dynamic>? existing;

  @override
  State<_TaskEditor> createState() => _TaskEditorState();
}

class _TaskEditorState extends State<_TaskEditor> {
  final _form = GlobalKey<FormState>();
  late final TextEditingController _title = TextEditingController(
    text: widget.existing?['title']?.toString(),
  );
  late final TextEditingController _description = TextEditingController(
    text: widget.existing?['description']?.toString(),
  );
  String? _assignedTo;
  late String _priority = widget.existing?['priority']?.toString() ?? 'NORMAL';
  late DateTime? _due = DateTime.tryParse(
    widget.existing?['due_at']?.toString() ?? '',
  )?.toLocal();
  bool _saving = false;
  String? _error;

  List<Map<String, dynamic>> get _staff =>
      (widget.options['staff'] as List? ?? const [])
          .map((item) => Map<String, dynamic>.from(item as Map))
          .toList();

  @override
  void initState() {
    super.initState();
    final existingAssignee = widget.existing?['assigned_to']?.toString();
    _assignedTo =
        _staff.any((staff) => staff['id']?.toString() == existingAssignee)
        ? existingAssignee
        : _staff.firstOrNull?['id']?.toString();
  }

  @override
  void dispose() {
    _title.dispose();
    _description.dispose();
    super.dispose();
  }

  Future<void> _pickDue() async {
    final date = await showDatePicker(
      context: context,
      initialDate: _due ?? DateTime.now().add(const Duration(days: 1)),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 730)),
    );
    if (date == null || !mounted) return;
    final time = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(
        _due ?? DateTime(date.year, date.month, date.day, 18),
      ),
    );
    if (time == null) return;
    setState(
      () => _due = DateTime(
        date.year,
        date.month,
        date.day,
        time.hour,
        time.minute,
      ),
    );
  }

  Future<void> _save() async {
    if (!_form.currentState!.validate()) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    final body = {
      'title': _title.text.trim(),
      'description': _description.text.trim(),
      'assignedTo': _assignedTo,
      if (_due != null) 'dueAt': _due!.toUtc().toIso8601String(),
      'priority': _priority,
    };
    try {
      if (widget.existing == null) {
        await widget.client.post('/api/tasks', body);
      } else {
        await widget.client.put('/api/tasks/${widget.existing!['id']}', body);
      }
      if (mounted) Navigator.pop(context, true);
    } catch (error) {
      if (mounted) setState(() => _error = error.toString());
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: Text(
      widget.existing == null ? 'Assign staff task' : 'Edit staff task',
    ),
    content: SizedBox(
      width: 580,
      child: SingleChildScrollView(
        child: Form(
          key: _form,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              DropdownButtonFormField<String>(
                initialValue: _assignedTo,
                isExpanded: true,
                decoration: const InputDecoration(labelText: 'Assigned staff'),
                items: _staff
                    .map(
                      (staff) => DropdownMenuItem(
                        value: staff['id'].toString(),
                        child: Text(
                          staff['full_name'].toString(),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    )
                    .toList(),
                onChanged: (value) => setState(() => _assignedTo = value),
                validator: (value) => value == null ? 'Select staff' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _title,
                decoration: const InputDecoration(labelText: 'Task title *'),
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
                  labelText: 'Task instructions',
                ),
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
                        child: Text(_taskLabel(value)),
                      ),
                    )
                    .toList(),
                onChanged: (value) => setState(() => _priority = value!),
              ),
              const SizedBox(height: 12),
              ListTile(
                contentPadding: EdgeInsets.zero,
                leading: const Icon(Icons.event),
                title: Text(
                  _due == null ? 'No due date' : 'Due ${_taskDateTime(_due)}',
                ),
                trailing: Wrap(
                  children: [
                    if (_due != null)
                      IconButton(
                        onPressed: () => setState(() => _due = null),
                        icon: const Icon(Icons.clear),
                      ),
                    IconButton(
                      onPressed: _pickDue,
                      icon: const Icon(Icons.calendar_today),
                    ),
                  ],
                ),
              ),
              if (_error != null)
                Text(
                  _error!,
                  style: const TextStyle(color: SocietyColors.danger),
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
      FilledButton(
        onPressed: _saving ? null : _save,
        child: Text(
          _saving
              ? 'Saving…'
              : widget.existing == null
              ? 'Assign task'
              : 'Save changes',
        ),
      ),
    ],
  );
}

class _TaskDetailDialog extends StatefulWidget {
  const _TaskDetailDialog({
    required this.client,
    required this.role,
    required this.taskId,
    required this.options,
    this.onEdit,
  });
  final BlocManagedApiGateway client;
  final TaskWorkspaceRole role;
  final String taskId;
  final Map<String, dynamic> options;
  final Future<void> Function(Map<String, dynamic> task)? onEdit;

  @override
  State<_TaskDetailDialog> createState() => _TaskDetailDialogState();
}

class _TaskDetailDialogState extends State<_TaskDetailDialog> {
  late final AsyncDataBloc<Map<String, dynamic>> _taskBloc;

  Map<String, dynamic>? get _task => _taskBloc.state.data;

  @override
  void initState() {
    super.initState();
    _taskBloc = AsyncDataBloc(
      () async => Map<String, dynamic>.from(
        await widget.client.get('/api/tasks/${widget.taskId}') as Map,
      ),
    )..refresh();
  }

  @override
  void dispose() {
    _taskBloc.close();
    super.dispose();
  }

  Future<void> _load() => _taskBloc.refresh();

  Future<void> _progress() async {
    final changed = await showDialog<bool>(
      context: context,
      builder: (_) => _TaskProgressDialog(
        client: widget.client,
        role: widget.role,
        task: _task!,
      ),
    );
    if (changed == true) await _load();
  }

  Future<void> _comment() async {
    final note = TextEditingController();
    final value = await showDialog<String>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Add task note'),
        content: TextField(
          controller: note,
          minLines: 3,
          maxLines: 6,
          decoration: const InputDecoration(
            labelText: 'Progress note or comment',
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialogContext, note.text.trim()),
            child: const Text('Add note'),
          ),
        ],
      ),
    );
    note.dispose();
    if (value == null || value.isEmpty) return;
    try {
      await widget.client.post('/api/tasks/${widget.taskId}/comments', {
        'note': value,
      });
      await _load();
    } catch (error) {
      if (mounted)
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(error.toString())));
    }
  }

  @override
  Widget build(BuildContext context) =>
      BlocBuilder<
        AsyncDataBloc<Map<String, dynamic>>,
        AsyncDataState<Map<String, dynamic>>
      >(
        bloc: _taskBloc,
        builder: (context, state) {
          final _error = state.status == AsyncDataStatus.failure
              ? state.error.toString()
              : null;
          if (_error != null)
            return AlertDialog(
              title: const Text('Task'),
              content: Text(_error),
              actions: [
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Close'),
                ),
              ],
            );
          if (_task == null)
            return const AlertDialog(
              content: SizedBox(
                width: 560,
                height: 220,
                child: Center(child: CircularProgressIndicator()),
              ),
            );
          final task = _task!;
          final timeline = (task['timeline'] as List? ?? const [])
              .map((item) => Map<String, dynamic>.from(item as Map))
              .toList()
              .reversed;
          return AlertDialog(
            title: Row(
              children: [
                Expanded(child: Text(task['reference']?.toString() ?? 'Task')),
                _TaskStatus(task['status'].toString()),
              ],
            ),
            content: SizedBox(
              width: 700,
              child: ConstrainedBox(
                constraints: BoxConstraints(
                  maxHeight: MediaQuery.sizeOf(context).height * .66,
                ),
                child: SingleChildScrollView(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        task['title'].toString(),
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      const SizedBox(height: 8),
                      Text(
                        task['description']?.toString().isNotEmpty == true
                            ? task['description'].toString()
                            : 'No additional instructions.',
                      ),
                      const SizedBox(height: 18),
                      Wrap(
                        spacing: 18,
                        runSpacing: 12,
                        children: [
                          _TaskFact('Assigned to', task['assigned_to_name']),
                          _TaskFact('Assigned by', task['assigned_by_name']),
                          _TaskFact(
                            'Priority',
                            _taskLabel(task['priority'].toString()),
                          ),
                          _TaskFact('Due', _taskDateTime(task['due_at'])),
                          _TaskFact(
                            'Started',
                            _taskDateTime(task['started_at']),
                          ),
                          _TaskFact(
                            'Completed',
                            _taskDateTime(task['completed_at']),
                          ),
                        ],
                      ),
                      if (task['overdue'] == true) ...[
                        const SizedBox(height: 12),
                        const Text(
                          'This task is overdue.',
                          style: TextStyle(
                            color: SocietyColors.danger,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ],
                      if (task['completion_note'] != null) ...[
                        const SizedBox(height: 16),
                        Container(
                          width: double.infinity,
                          padding: const EdgeInsets.all(14),
                          decoration: BoxDecoration(
                            color: SocietyColors.success.withValues(alpha: .08),
                            borderRadius: BorderRadius.circular(14),
                          ),
                          child: Text('Completion: ${task['completion_note']}'),
                        ),
                      ],
                      const SizedBox(height: 22),
                      Text(
                        'Activity timeline',
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      const SizedBox(height: 10),
                      if (timeline.isEmpty)
                        const Text('No task activity recorded.')
                      else
                        for (final update in timeline) _TaskTimeline(update),
                    ],
                  ),
                ),
              ),
            ),
            actions: [
              TextButton.icon(
                onPressed: _comment,
                icon: const Icon(Icons.add_comment_outlined),
                label: const Text('Add note'),
              ),
              if (widget.onEdit != null)
                TextButton.icon(
                  onPressed: () async {
                    Navigator.pop(context);
                    await widget.onEdit!(task);
                  },
                  icon: const Icon(Icons.edit),
                  label: const Text('Edit'),
                ),
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

class _TaskProgressDialog extends StatefulWidget {
  const _TaskProgressDialog({
    required this.client,
    required this.role,
    required this.task,
  });
  final BlocManagedApiGateway client;
  final TaskWorkspaceRole role;
  final Map<String, dynamic> task;

  @override
  State<_TaskProgressDialog> createState() => _TaskProgressDialogState();
}

class _TaskProgressDialogState extends State<_TaskProgressDialog> {
  final _note = TextEditingController();
  late String _status = widget.task['status'].toString();
  bool _saving = false;
  String? _error;

  @override
  void dispose() {
    _note.dispose();
    super.dispose();
  }

  List<String> get _statuses {
    final current = widget.task['status'].toString();
    final admin = widget.role == TaskWorkspaceRole.societyAdmin;
    return switch (current) {
      'OPEN' => [
        'OPEN',
        'IN_PROGRESS',
        'BLOCKED',
        'COMPLETED',
        if (admin) 'CANCELLED',
      ],
      'IN_PROGRESS' => [
        'IN_PROGRESS',
        'BLOCKED',
        'COMPLETED',
        if (admin) 'CANCELLED',
      ],
      'BLOCKED' => ['BLOCKED', 'IN_PROGRESS', if (admin) 'CANCELLED'],
      'COMPLETED' => ['COMPLETED', if (admin) 'IN_PROGRESS'],
      'CANCELLED' => ['CANCELLED', if (admin) 'OPEN'],
      _ => [current],
    };
  }

  Future<void> _save() async {
    if (const {'BLOCKED', 'COMPLETED'}.contains(_status) &&
        _note.text.trim().isEmpty) {
      setState(() => _error = 'Add a note for blocked or completed work.');
      return;
    }
    if (_status == widget.task['status'] && _note.text.trim().isEmpty) {
      setState(() => _error = 'Add a progress note or change the status.');
      return;
    }
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await widget.client.patch('/api/tasks/${widget.task['id']}/progress', {
        'status': _status,
        if (_note.text.trim().isNotEmpty) 'note': _note.text.trim(),
      });
      if (mounted) Navigator.pop(context, true);
    } catch (error) {
      if (mounted) setState(() => _error = error.toString());
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Update task progress'),
    content: SizedBox(
      width: 500,
      child: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            DropdownButtonFormField<String>(
              initialValue: _status,
              isExpanded: true,
              decoration: const InputDecoration(labelText: 'Status'),
              items: _statuses
                  .map(
                    (value) => DropdownMenuItem(
                      value: value,
                      child: Text(_taskLabel(value)),
                    ),
                  )
                  .toList(),
              onChanged: (value) => setState(() => _status = value!),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _note,
              minLines: 3,
              maxLines: 6,
              decoration: InputDecoration(
                labelText: const {'BLOCKED', 'COMPLETED'}.contains(_status)
                    ? 'Required note *'
                    : 'Progress note',
              ),
            ),
            if (_error != null) ...[
              const SizedBox(height: 10),
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

class _TaskCard extends StatelessWidget {
  const _TaskCard({
    required this.task,
    required this.admin,
    required this.onOpen,
    required this.onEdit,
  });
  final Map<String, dynamic> task;
  final bool admin;
  final VoidCallback onOpen;
  final VoidCallback onEdit;

  @override
  Widget build(BuildContext context) => Card(
    child: InkWell(
      onTap: onOpen,
      borderRadius: BorderRadius.circular(20),
      child: LayoutBuilder(
        builder: (context, constraints) {
          final compact = constraints.maxWidth < 520;
          final details = Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                task['title'].toString(),
                style: const TextStyle(
                  fontWeight: FontWeight.w700,
                  fontSize: 16,
                ),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
              const SizedBox(height: 5),
              Text(
                '${task['reference']} · ${task['assigned_to_name'] ?? ''}',
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
              Text(
                '${_taskLabel(task['priority'].toString())} · Due ${_taskDateTime(task['due_at'])}',
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  color: task['overdue'] == true
                      ? SocietyColors.danger
                      : SocietyColors.textMuted,
                  fontSize: 12,
                ),
              ),
            ],
          );
          final actions = Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (admin)
                IconButton(
                  onPressed: onEdit,
                  tooltip: 'Edit task',
                  icon: const Icon(Icons.edit_outlined),
                ),
              IconButton(
                onPressed: onOpen,
                tooltip: 'Task details',
                icon: const Icon(Icons.chevron_right),
              ),
            ],
          );
          return Padding(
            padding: const EdgeInsets.all(17),
            child: compact
                ? Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          _TaskIcon(
                            priority: task['priority'].toString(),
                            overdue: task['overdue'] == true,
                          ),
                          const SizedBox(width: 12),
                          _TaskStatus(task['status'].toString()),
                          const Spacer(),
                          actions,
                        ],
                      ),
                      const SizedBox(height: 12),
                      details,
                    ],
                  )
                : Row(
                    children: [
                      _TaskIcon(
                        priority: task['priority'].toString(),
                        overdue: task['overdue'] == true,
                      ),
                      const SizedBox(width: 14),
                      Expanded(child: details),
                      const SizedBox(width: 8),
                      _TaskStatus(task['status'].toString()),
                      actions,
                    ],
                  ),
          );
        },
      ),
    ),
  );
}

class _TaskMetric extends StatelessWidget {
  const _TaskMetric(this.label, this.value, this.icon, this.color);
  final String label;
  final int value;
  final IconData icon;
  final Color color;
  @override
  Widget build(BuildContext context) => SizedBox(
    width: 175,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(15),
        child: Row(
          children: [
            Icon(icon, color: color),
            const SizedBox(width: 11),
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

class _TaskIcon extends StatelessWidget {
  const _TaskIcon({required this.priority, required this.overdue});
  final String priority;
  final bool overdue;
  @override
  Widget build(BuildContext context) {
    final color = overdue || priority == 'URGENT'
        ? SocietyColors.danger
        : priority == 'HIGH'
        ? SocietyColors.warning
        : SocietyColors.blue;
    return Container(
      width: 46,
      height: 46,
      decoration: BoxDecoration(
        color: color.withValues(alpha: .1),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Icon(Icons.task_alt, color: color),
    );
  }
}

class _TaskStatus extends StatelessWidget {
  const _TaskStatus(this.status);
  final String status;
  @override
  Widget build(BuildContext context) {
    final color = status == 'COMPLETED'
        ? SocietyColors.success
        : status == 'BLOCKED' || status == 'CANCELLED'
        ? SocietyColors.danger
        : status == 'IN_PROGRESS'
        ? SocietyColors.blue
        : SocietyColors.warning;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 5),
      decoration: BoxDecoration(
        color: color.withValues(alpha: .1),
        borderRadius: BorderRadius.circular(100),
      ),
      child: Text(
        _taskLabel(status),
        style: TextStyle(
          color: color,
          fontWeight: FontWeight.w700,
          fontSize: 11,
        ),
      ),
    );
  }
}

class _TaskFact extends StatelessWidget {
  const _TaskFact(this.label, this.value);
  final String label;
  final Object? value;
  @override
  Widget build(BuildContext context) => SizedBox(
    width: 195,
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(color: SocietyColors.textMuted, fontSize: 12),
        ),
        Text(
          value?.toString() ?? '—',
          style: const TextStyle(fontWeight: FontWeight.w600),
        ),
      ],
    ),
  );
}

class _TaskTimeline extends StatelessWidget {
  const _TaskTimeline(this.update);
  final Map<String, dynamic> update;
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(bottom: 13),
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
        const SizedBox(width: 11),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                _taskEvent(update),
                style: const TextStyle(fontWeight: FontWeight.w700),
              ),
              Text(
                '${update['actor_name']} · ${_taskDateTime(update['created_at'])}',
                style: const TextStyle(
                  color: SocietyColors.textMuted,
                  fontSize: 12,
                ),
              ),
              if (update['note']?.toString().isNotEmpty == true)
                Text(update['note'].toString()),
            ],
          ),
        ),
      ],
    ),
  );
}

String _taskEvent(
  Map<String, dynamic> update,
) => switch (update['event_type']) {
  'CREATED' => 'Task assigned',
  'REASSIGNED' => 'Task reassigned',
  'EDITED' => 'Task details updated',
  'COMMENT' => 'Comment added',
  'PROGRESS_NOTE' => 'Progress note added',
  'STATUS_CHANGED' =>
    'Status changed to ${_taskLabel(update['to_status']?.toString() ?? '')}',
  _ => _taskLabel(update['event_type']?.toString() ?? 'Update'),
};

String _taskLabel(String value) => value
    .toLowerCase()
    .split('_')
    .map(
      (part) =>
          part.isEmpty ? '' : '${part[0].toUpperCase()}${part.substring(1)}',
    )
    .join(' ');

String _taskDateTime(Object? value) {
  if (value == null) return '—';
  final parsed = value is DateTime
      ? value
      : DateTime.tryParse(value.toString())?.toLocal();
  if (parsed == null) return value.toString();
  return '${parsed.day.toString().padLeft(2, '0')}/${parsed.month.toString().padLeft(2, '0')}/${parsed.year} ${parsed.hour.toString().padLeft(2, '0')}:${parsed.minute.toString().padLeft(2, '0')}';
}
