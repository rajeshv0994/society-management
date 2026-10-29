import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';
import 'async_data_bloc.dart';
import 'theme.dart';
import 'workspace_record.dart';

enum NoticeWorkspaceRole { societyAdmin, staff, resident }

Widget buildStaffNoticeWorkspace(BlocManagedApiGateway client) =>
    NoticeWorkspace(client: client, role: NoticeWorkspaceRole.staff);

Widget buildResidentNoticeWorkspace(BlocManagedApiGateway client) =>
    NoticeWorkspace(client: client, role: NoticeWorkspaceRole.resident);

class NoticeWorkspace extends StatefulWidget {
  const NoticeWorkspace({super.key, required this.client, required this.role});

  final BlocManagedApiGateway client;
  final NoticeWorkspaceRole role;

  @override
  State<NoticeWorkspace> createState() => _NoticeWorkspaceState();
}

class _NoticeWorkspaceState extends State<NoticeWorkspace> {
  late final AsyncDataBloc<_NoticeData> _bloc;
  List<Map<String, dynamic>> get _notices =>
      _bloc.state.data?.notices ?? const [];
  bool get _canManage => _bloc.state.data?.canManage ?? false;
  String _filter = 'ALL';
  String _query = '';

  @override
  void initState() {
    super.initState();
    _bloc = AsyncDataBloc(() => _NoticeData.load(widget.client));
    _load();
  }

  Future<void> _load() => _bloc.refresh();

  @override
  void dispose() {
    _bloc.close();
    super.dispose();
  }

  Future<void> _compose([Map<String, dynamic>? notice]) async {
    final saved = await showDialog<bool>(
      context: context,
      builder: (_) => _NoticeEditor(client: widget.client, existing: notice),
    );
    if (saved == true) {
      _message(notice == null ? 'Notice saved.' : 'Notice updated.');
      await _load();
    }
  }

  Future<void> _open(Map<String, dynamic> notice) async {
    if (notice['status'] == 'PUBLISHED' && notice['read'] != true) {
      try {
        await widget.client.post('/api/notices/${notice['id']}/read');
      } catch (_) {}
    }
    if (!mounted) return;
    final action = await showDialog<String>(
      context: context,
      builder: (_) =>
          _NoticeDetailDialog(notice: notice, canManage: _canManage),
    );
    if (action == 'ACKNOWLEDGE') {
      await _acknowledge(notice);
    } else if (action == 'EDIT') {
      await _compose(notice);
    } else if (action == 'ARCHIVE') {
      await _archive(notice);
    } else {
      await _load();
    }
  }

  Future<void> _acknowledge(Map<String, dynamic> notice) async {
    try {
      await widget.client.post('/api/notices/${notice['id']}/acknowledge');
      _message('Notice acknowledged.');
      await _load();
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _archive(Map<String, dynamic> notice) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Archive notice?'),
        content: const Text(
          'Archived notices are removed from Member and regular Staff views.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialogContext, true),
            child: const Text('Archive'),
          ),
        ],
      ),
    );
    if (confirmed != true) return;
    try {
      await widget.client.put('/api/notices/${notice['id']}', {
        'title': notice['title'],
        'body': notice['body'],
        'audience': notice['audience'],
        'priority': notice['priority'],
        'pinned': notice['pinned'] == true,
        'requiresAcknowledgement': notice['requires_acknowledgement'] == true,
        'status': 'ARCHIVED',
        if (notice['expires_at'] != null) 'expiresAt': notice['expires_at'],
      });
      _message('Notice archived.');
      await _load();
    } catch (error) {
      _message(error.toString());
    }
  }

  void _message(String text) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
    }
  }

  bool _matches(Map<String, dynamic> notice) {
    final filterMatches = switch (_filter) {
      'UNREAD' => notice['read'] != true && notice['status'] == 'PUBLISHED',
      'PINNED' => notice['pinned'] == true,
      'IMPORTANT' => const {'IMPORTANT', 'URGENT'}.contains(notice['priority']),
      'ACK_REQUIRED' =>
        notice['requires_acknowledgement'] == true &&
            notice['acknowledged'] != true,
      'DRAFT' => notice['status'] == 'DRAFT',
      'ARCHIVED' => notice['status'] == 'ARCHIVED' || notice['expired'] == true,
      _ => true,
    };
    if (!filterMatches) return false;
    final query = _query.trim().toLowerCase();
    if (query.isEmpty) return true;
    return [
      notice['title'],
      notice['body'],
      notice['published_by_name'],
    ].any((value) => value?.toString().toLowerCase().contains(query) == true);
  }

  @override
  Widget build(
    BuildContext context,
  ) => BlocBuilder<AsyncDataBloc<_NoticeData>, AsyncDataState<_NoticeData>>(
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
      final unread = _notices
          .where(
            (item) => item['status'] == 'PUBLISHED' && item['read'] != true,
          )
          .length;
      final important = _notices
          .where(
            (item) => const {'IMPORTANT', 'URGENT'}.contains(item['priority']),
          )
          .length;
      final acknowledgement = _notices
          .where(
            (item) =>
                item['requires_acknowledgement'] == true &&
                item['acknowledged'] != true &&
                item['status'] == 'PUBLISHED',
          )
          .length;
      final rows = _notices.where(_matches).toList();

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
                      'Notices',
                      style: Theme.of(context).textTheme.headlineMedium,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      _canManage
                          ? 'Create, publish, and track society announcements.'
                          : 'Important announcements from your society.',
                      style: const TextStyle(color: SocietyColors.textMuted),
                    ),
                  ],
                ),
                if (_canManage)
                  FilledButton.icon(
                    onPressed: _compose,
                    icon: const Icon(Icons.campaign_outlined),
                    label: const Text('Create notice'),
                  ),
              ],
            ),
            const SizedBox(height: 18),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                _NoticeMetric(
                  'Unread',
                  unread,
                  Icons.mark_email_unread_outlined,
                  SocietyColors.blue,
                ),
                _NoticeMetric(
                  'Important',
                  important,
                  Icons.priority_high,
                  SocietyColors.warning,
                ),
                _NoticeMetric(
                  'Need acknowledgement',
                  acknowledgement,
                  Icons.fact_check_outlined,
                  acknowledgement > 0
                      ? SocietyColors.danger
                      : SocietyColors.success,
                ),
              ],
            ),
            const SizedBox(height: 18),
            TextField(
              onChanged: (value) => setState(() => _query = value),
              decoration: const InputDecoration(
                prefixIcon: Icon(Icons.search),
                labelText: 'Search notices',
              ),
            ),
            const SizedBox(height: 12),
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Wrap(
                spacing: 8,
                children: [
                  for (final value in [
                    'ALL',
                    'UNREAD',
                    'PINNED',
                    'IMPORTANT',
                    'ACK_REQUIRED',
                    if (_canManage) 'DRAFT',
                    if (_canManage) 'ARCHIVED',
                  ])
                    ChoiceChip(
                      label: Text(_noticeLabel(value)),
                      selected: _filter == value,
                      onSelected: (_) => setState(() => _filter = value),
                    ),
                ],
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
                        Icons.campaign_outlined,
                        size: 44,
                        color: SocietyColors.textMuted,
                      ),
                      SizedBox(height: 10),
                      Text('No notices match this view.'),
                    ],
                  ),
                ),
              )
            else
              for (final notice in rows) ...[
                _NoticeCard(
                  notice: notice,
                  canManage: _canManage,
                  onOpen: () => _open(notice),
                  onEdit: () => _compose(notice),
                  onAcknowledge: () => _acknowledge(notice),
                ),
                const SizedBox(height: 10),
              ],
          ],
        ),
      );
    },
  );
}

class _NoticeData {
  const _NoticeData({required this.notices, required this.canManage});
  final List<WorkspaceRecord> notices;
  final bool canManage;

  static Future<_NoticeData> load(BlocManagedApiGateway client) async {
    final values = await Future.wait([
      client.get('/api/notices'),
      client.get('/api/notices/options'),
    ]);
    return _NoticeData(
      notices: workspaceRecords(values[0]),
      canManage: (values[1] as Map)['can_manage'] == true,
    );
  }
}

class _NoticeEditor extends StatefulWidget {
  const _NoticeEditor({required this.client, this.existing});
  final BlocManagedApiGateway client;
  final Map<String, dynamic>? existing;

  @override
  State<_NoticeEditor> createState() => _NoticeEditorState();
}

class _NoticeEditorState extends State<_NoticeEditor> {
  final _form = GlobalKey<FormState>();
  late final TextEditingController _title = TextEditingController(
    text: widget.existing?['title']?.toString(),
  );
  late final TextEditingController _body = TextEditingController(
    text: widget.existing?['body']?.toString(),
  );
  late String _audience = widget.existing?['audience']?.toString() ?? 'ALL';
  late String _priority = widget.existing?['priority']?.toString() ?? 'NORMAL';
  late String _status = widget.existing?['status']?.toString() ?? 'PUBLISHED';
  late bool _pinned = widget.existing?['pinned'] == true;
  late bool _acknowledgement =
      widget.existing?['requires_acknowledgement'] == true;
  late DateTime? _expires = DateTime.tryParse(
    widget.existing?['expires_at']?.toString() ?? '',
  )?.toLocal();
  bool _saving = false;
  String? _error;

  @override
  void dispose() {
    _title.dispose();
    _body.dispose();
    super.dispose();
  }

  Future<void> _pickExpiry() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: _expires ?? DateTime.now().add(const Duration(days: 7)),
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 730)),
    );
    if (picked != null) {
      setState(
        () =>
            _expires = DateTime(picked.year, picked.month, picked.day, 23, 59),
      );
    }
  }

  Future<void> _save() async {
    if (!_form.currentState!.validate()) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    final body = {
      'title': _title.text.trim(),
      'body': _body.text.trim(),
      'audience': _audience,
      'priority': _priority,
      'pinned': _pinned,
      'requiresAcknowledgement': _acknowledgement,
      'status': _status,
      if (_expires != null) 'expiresAt': _expires!.toUtc().toIso8601String(),
    };
    try {
      if (widget.existing == null) {
        await widget.client.post('/api/notices', body);
      } else {
        await widget.client.put('/api/notices/${widget.existing!['id']}', body);
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
    title: Text(widget.existing == null ? 'Create notice' : 'Edit notice'),
    content: SizedBox(
      width: 620,
      child: ConstrainedBox(
        constraints: BoxConstraints(
          maxHeight: MediaQuery.sizeOf(context).height * .68,
        ),
        child: SingleChildScrollView(
          child: Form(
            key: _form,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: _title,
                  decoration: const InputDecoration(
                    labelText: 'Notice title *',
                  ),
                  validator: _required,
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _body,
                  minLines: 6,
                  maxLines: 12,
                  decoration: const InputDecoration(
                    labelText: 'Announcement *',
                  ),
                  validator: _required,
                ),
                const SizedBox(height: 12),
                DropdownButtonFormField<String>(
                  initialValue: _audience,
                  isExpanded: true,
                  decoration: const InputDecoration(labelText: 'Audience'),
                  items: const ['ALL', 'RESIDENTS', 'STAFF']
                      .map(
                        (value) => DropdownMenuItem(
                          value: value,
                          child: Text(_audienceLabel(value)),
                        ),
                      )
                      .toList(),
                  onChanged: (value) => setState(() => _audience = value!),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: DropdownButtonFormField<String>(
                        initialValue: _priority,
                        isExpanded: true,
                        decoration: const InputDecoration(
                          labelText: 'Priority',
                        ),
                        items: const ['NORMAL', 'IMPORTANT', 'URGENT']
                            .map(
                              (value) => DropdownMenuItem(
                                value: value,
                                child: Text(_noticeLabel(value)),
                              ),
                            )
                            .toList(),
                        onChanged: (value) =>
                            setState(() => _priority = value!),
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: DropdownButtonFormField<String>(
                        initialValue: _status,
                        isExpanded: true,
                        decoration: const InputDecoration(labelText: 'Status'),
                        items: const ['DRAFT', 'PUBLISHED', 'ARCHIVED']
                            .map(
                              (value) => DropdownMenuItem(
                                value: value,
                                child: Text(_noticeLabel(value)),
                              ),
                            )
                            .toList(),
                        onChanged: (value) => setState(() => _status = value!),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('Pin this notice'),
                  subtitle: const Text('Pinned notices remain at the top.'),
                  value: _pinned,
                  onChanged: (value) => setState(() => _pinned = value),
                ),
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('Require acknowledgement'),
                  subtitle: const Text(
                    'Recipients must confirm they have read it.',
                  ),
                  value: _acknowledgement,
                  onChanged: (value) =>
                      setState(() => _acknowledgement = value),
                ),
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: const Icon(Icons.event_busy_outlined),
                  title: Text(
                    _expires == null
                        ? 'No expiry'
                        : 'Expires ${_noticeDate(_expires)}',
                  ),
                  subtitle: const Text(
                    'Expired notices move out of recipient views.',
                  ),
                  trailing: Wrap(
                    children: [
                      if (_expires != null)
                        IconButton(
                          onPressed: () => setState(() => _expires = null),
                          icon: const Icon(Icons.clear),
                        ),
                      IconButton(
                        onPressed: _pickExpiry,
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
              : _status == 'DRAFT'
              ? 'Save draft'
              : 'Save notice',
        ),
      ),
    ],
  );

  String? _required(String? value) =>
      value == null || value.trim().isEmpty ? 'This field is required' : null;
}

class _NoticeCard extends StatelessWidget {
  const _NoticeCard({
    required this.notice,
    required this.canManage,
    required this.onOpen,
    required this.onEdit,
    required this.onAcknowledge,
  });
  final Map<String, dynamic> notice;
  final bool canManage;
  final VoidCallback onOpen;
  final VoidCallback onEdit;
  final VoidCallback onAcknowledge;

  @override
  Widget build(BuildContext context) {
    final unread = notice['read'] != true && notice['status'] == 'PUBLISHED';
    return Card(
      child: InkWell(
        onTap: onOpen,
        borderRadius: BorderRadius.circular(20),
        child: Padding(
          padding: const EdgeInsets.all(17),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _NoticeIcon(
                    priority: notice['priority']?.toString() ?? 'NORMAL',
                  ),
                  const SizedBox(width: 13),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            if (notice['pinned'] == true) ...[
                              const Icon(
                                Icons.push_pin,
                                size: 16,
                                color: SocietyColors.blue,
                              ),
                              const SizedBox(width: 5),
                            ],
                            Expanded(
                              child: Text(
                                notice['title']?.toString() ?? '',
                                style: TextStyle(
                                  fontSize: 16,
                                  fontWeight: unread
                                      ? FontWeight.w800
                                      : FontWeight.w700,
                                ),
                              ),
                            ),
                            if (unread)
                              Container(
                                width: 9,
                                height: 9,
                                decoration: const BoxDecoration(
                                  color: SocietyColors.blue,
                                  shape: BoxShape.circle,
                                ),
                              ),
                          ],
                        ),
                        const SizedBox(height: 5),
                        Text(
                          notice['body']?.toString() ?? '',
                          maxLines: 3,
                          overflow: TextOverflow.ellipsis,
                        ),
                        const SizedBox(height: 8),
                        Wrap(
                          spacing: 7,
                          runSpacing: 6,
                          children: [
                            _NoticeChip(
                              _audienceLabel(
                                notice['audience']?.toString() ?? 'ALL',
                              ),
                            ),
                            _NoticeChip(
                              _noticeLabel(
                                notice['priority']?.toString() ?? 'NORMAL',
                              ),
                            ),
                            if (canManage)
                              _NoticeChip(
                                _noticeLabel(
                                  notice['status']?.toString() ?? 'PUBLISHED',
                                ),
                              ),
                            if (notice['requires_acknowledgement'] == true)
                              _NoticeChip(
                                notice['acknowledged'] == true
                                    ? 'Acknowledged'
                                    : 'Acknowledgement required',
                              ),
                          ],
                        ),
                        const SizedBox(height: 7),
                        Text(
                          '${notice['published_by_name'] ?? 'Society'} · ${_noticeDateTime(notice['published_at'] ?? notice['updated_at'])}',
                          style: const TextStyle(
                            color: SocietyColors.textMuted,
                            fontSize: 12,
                          ),
                        ),
                        if (canManage)
                          Text(
                            '${notice['read_count'] ?? 0} read · ${notice['acknowledgement_count'] ?? 0} acknowledged',
                            style: const TextStyle(
                              color: SocietyColors.textMuted,
                              fontSize: 12,
                            ),
                          ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 10),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                alignment: WrapAlignment.end,
                children: [
                  if (notice['requires_acknowledgement'] == true &&
                      notice['acknowledged'] != true &&
                      notice['status'] == 'PUBLISHED')
                    FilledButton.tonalIcon(
                      onPressed: onAcknowledge,
                      icon: const Icon(Icons.fact_check_outlined),
                      label: const Text('Acknowledge'),
                    ),
                  if (canManage)
                    OutlinedButton.icon(
                      onPressed: onEdit,
                      icon: const Icon(Icons.edit_outlined),
                      label: const Text('Edit'),
                    ),
                  TextButton(
                    onPressed: onOpen,
                    child: const Text('Read notice'),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _NoticeDetailDialog extends StatelessWidget {
  const _NoticeDetailDialog({required this.notice, required this.canManage});
  final Map<String, dynamic> notice;
  final bool canManage;

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: Row(
      children: [
        if (notice['pinned'] == true)
          const Padding(
            padding: EdgeInsets.only(right: 8),
            child: Icon(Icons.push_pin, color: SocietyColors.blue),
          ),
        Expanded(child: Text(notice['title']?.toString() ?? 'Notice')),
      ],
    ),
    content: SizedBox(
      width: 680,
      child: ConstrainedBox(
        constraints: BoxConstraints(
          maxHeight: MediaQuery.sizeOf(context).height * .65,
        ),
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Wrap(
                spacing: 8,
                runSpacing: 6,
                children: [
                  _NoticeChip(_audienceLabel(notice['audience'].toString())),
                  _NoticeChip(_noticeLabel(notice['priority'].toString())),
                  if (canManage)
                    _NoticeChip(_noticeLabel(notice['status'].toString())),
                ],
              ),
              const SizedBox(height: 18),
              SelectableText(
                notice['body']?.toString() ?? '',
                style: const TextStyle(fontSize: 16, height: 1.55),
              ),
              const SizedBox(height: 22),
              const Divider(),
              Text('Published by ${notice['published_by_name'] ?? 'Society'}'),
              Text(
                'Published ${_noticeDateTime(notice['published_at'] ?? notice['updated_at'])}',
                style: const TextStyle(color: SocietyColors.textMuted),
              ),
              if (notice['expires_at'] != null)
                Text(
                  'Expires ${_noticeDateTime(notice['expires_at'])}',
                  style: const TextStyle(color: SocietyColors.textMuted),
                ),
              if (canManage)
                Padding(
                  padding: const EdgeInsets.only(top: 10),
                  child: Text(
                    '${notice['read_count'] ?? 0} recipients read · ${notice['acknowledgement_count'] ?? 0} acknowledged',
                    style: const TextStyle(fontWeight: FontWeight.w700),
                  ),
                ),
            ],
          ),
        ),
      ),
    ),
    actions: [
      if (notice['requires_acknowledgement'] == true &&
          notice['acknowledged'] != true &&
          notice['status'] == 'PUBLISHED')
        FilledButton.icon(
          onPressed: () => Navigator.pop(context, 'ACKNOWLEDGE'),
          icon: const Icon(Icons.fact_check_outlined),
          label: const Text('Acknowledge'),
        ),
      if (canManage)
        TextButton.icon(
          onPressed: () => Navigator.pop(context, 'EDIT'),
          icon: const Icon(Icons.edit),
          label: const Text('Edit'),
        ),
      if (canManage && notice['status'] != 'ARCHIVED')
        TextButton.icon(
          onPressed: () => Navigator.pop(context, 'ARCHIVE'),
          icon: const Icon(Icons.archive_outlined),
          label: const Text('Archive'),
        ),
      TextButton(
        onPressed: () => Navigator.pop(context),
        child: const Text('Close'),
      ),
    ],
  );
}

class _NoticeMetric extends StatelessWidget {
  const _NoticeMetric(this.label, this.value, this.icon, this.color);
  final String label;
  final int value;
  final IconData icon;
  final Color color;
  @override
  Widget build(BuildContext context) => SizedBox(
    width: 215,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(15),
        child: Row(
          children: [
            Icon(icon, color: color),
            const SizedBox(width: 11),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('$value', style: Theme.of(context).textTheme.titleLarge),
                  Text(
                    label,
                    style: const TextStyle(color: SocietyColors.textMuted),
                    overflow: TextOverflow.ellipsis,
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

class _NoticeIcon extends StatelessWidget {
  const _NoticeIcon({required this.priority});
  final String priority;
  @override
  Widget build(BuildContext context) {
    final color = priority == 'URGENT'
        ? SocietyColors.danger
        : priority == 'IMPORTANT'
        ? SocietyColors.warning
        : SocietyColors.blue;
    return Container(
      width: 46,
      height: 46,
      decoration: BoxDecoration(
        color: color.withValues(alpha: .1),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Icon(Icons.campaign_outlined, color: color),
    );
  }
}

class _NoticeChip extends StatelessWidget {
  const _NoticeChip(this.label);
  final String label;
  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 4),
    decoration: BoxDecoration(
      color: SocietyColors.surfaceMuted,
      borderRadius: BorderRadius.circular(100),
    ),
    child: Text(
      label,
      style: const TextStyle(fontSize: 11, fontWeight: FontWeight.w700),
    ),
  );
}

String _noticeLabel(String value) => value
    .toLowerCase()
    .split('_')
    .map(
      (part) =>
          part.isEmpty ? '' : '${part[0].toUpperCase()}${part.substring(1)}',
    )
    .join(' ');

String _audienceLabel(String value) => switch (value) {
  'ALL' => 'All members & staff',
  'RESIDENTS' => 'Members only',
  'STAFF' => 'Staff only',
  _ => value,
};

String _noticeDate(Object? value) {
  if (value == null) return '—';
  final parsed = value is DateTime
      ? value
      : DateTime.tryParse(value.toString())?.toLocal();
  if (parsed == null) return value.toString();
  return '${parsed.day.toString().padLeft(2, '0')}/${parsed.month.toString().padLeft(2, '0')}/${parsed.year}';
}

String _noticeDateTime(Object? value) {
  if (value == null) return '—';
  final parsed = DateTime.tryParse(value.toString())?.toLocal();
  if (parsed == null) return value.toString();
  return '${_noticeDate(parsed)} ${parsed.hour.toString().padLeft(2, '0')}:${parsed.minute.toString().padLeft(2, '0')}';
}
