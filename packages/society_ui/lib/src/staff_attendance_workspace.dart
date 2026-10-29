import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';
import 'async_data_bloc.dart';
import 'theme.dart';
import 'workspace_record.dart';

class StaffAttendanceWorkspace extends StatefulWidget {
  const StaffAttendanceWorkspace({super.key, required this.client});
  final BlocManagedApiGateway client;

  @override
  State<StaffAttendanceWorkspace> createState() =>
      _StaffAttendanceWorkspaceState();
}

class _StaffAttendanceWorkspaceState extends State<StaffAttendanceWorkspace> {
  late final AsyncDataBloc<WorkspaceRecord> _attendanceBloc;
  bool _actionBusy = false;
  String _status = 'ALL';

  @override
  void initState() {
    super.initState();
    _attendanceBloc = AsyncDataBloc(
      () async => WorkspaceRecord.from(
        await widget.client.get('/api/society/staff/me/attendance'),
      ),
    )..refresh();
  }

  Future<void> _load() => _attendanceBloc.refresh();

  @override
  void dispose() {
    _attendanceBloc.close();
    super.dispose();
  }

  Future<void> _clock(String action) async {
    setState(() => _actionBusy = true);
    try {
      await widget.client.post('/api/society/staff/me/attendance/$action');
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(action == 'clock-in' ? 'Checked in.' : 'Checked out.'),
        ),
      );
      await _load();
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(error.toString())));
      }
    } finally {
      if (mounted) setState(() => _actionBusy = false);
    }
  }

  @override
  Widget build(
    BuildContext context,
  ) => BlocBuilder<AsyncDataBloc<WorkspaceRecord>, AsyncDataState<WorkspaceRecord>>(
    bloc: _attendanceBloc,
    builder: (context, state) {
      if (state.data == null && state.status != AsyncDataStatus.failure) {
        return const Center(child: CircularProgressIndicator());
      }
      if (state.data == null) {
        return Center(
          child: FilledButton.icon(
            onPressed: _load,
            icon: const Icon(Icons.refresh),
            label: Text(state.error.toString()),
          ),
        );
      }
      final data = state.data!;
      final today = data['today'] == null
          ? null
          : WorkspaceRecord.from(data['today']);
      final summary = data['summary'] is WorkspaceRecord
          ? data['summary'] as WorkspaceRecord
          : WorkspaceRecord.empty;
      final history = (data['history'] as List? ?? const [])
          .whereType<WorkspaceRecord>()
          .where((item) => _status == 'ALL' || item['status'] == _status)
          .toList();
      final checkedIn = today?['check_in'] != null;
      final checkedOut = today?['check_out'] != null;

      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Text(
              'My attendance',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            const SizedBox(height: 4),
            const Text(
              'Check in and out, review daily records, and see admin corrections.',
              style: TextStyle(color: SocietyColors.textMuted),
            ),
            const SizedBox(height: 16),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Wrap(
                  alignment: WrapAlignment.spaceBetween,
                  crossAxisAlignment: WrapCrossAlignment.center,
                  spacing: 18,
                  runSpacing: 14,
                  children: [
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Today',
                          style: TextStyle(color: SocietyColors.textMuted),
                        ),
                        Text(
                          today == null
                              ? 'Not marked'
                              : _label(today['status']),
                          style: Theme.of(context).textTheme.headlineSmall,
                        ),
                        Text(
                          'Check-in ${_time(today?['check_in'])} · Check-out ${_time(today?['check_out'])}',
                        ),
                        if (today?['note'] != null)
                          Text('Note: ${today!['note']}'),
                      ],
                    ),
                    Wrap(
                      spacing: 8,
                      children: [
                        FilledButton.icon(
                          onPressed: _actionBusy || checkedIn
                              ? null
                              : () => _clock('clock-in'),
                          icon: const Icon(Icons.login),
                          label: const Text('Check in'),
                        ),
                        OutlinedButton.icon(
                          onPressed: _actionBusy || !checkedIn || checkedOut
                              ? null
                              : () => _clock('clock-out'),
                          icon: const Icon(Icons.logout),
                          label: const Text('Check out'),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 14),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                _Metric(
                  'Present',
                  summary['present'] ?? 0,
                  SocietyColors.success,
                ),
                _Metric('Absent', summary['absent'] ?? 0, SocietyColors.danger),
                _Metric(
                  'Half day',
                  summary['half_day'] ?? 0,
                  SocietyColors.warning,
                ),
                _Metric('Leave', summary['leave'] ?? 0, SocietyColors.blue),
              ],
            ),
            const SizedBox(height: 18),
            DropdownButtonFormField<String>(
              initialValue: _status,
              decoration: const InputDecoration(labelText: 'Filter history'),
              items: const ['ALL', 'PRESENT', 'ABSENT', 'HALF_DAY', 'LEAVE']
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
            if (history.isEmpty)
              const Card(
                child: Padding(
                  padding: EdgeInsets.all(32),
                  child: Center(child: Text('No attendance records found.')),
                ),
              )
            else
              for (final item in history)
                Card(
                  child: ListTile(
                    leading: Icon(
                      item['status'] == 'PRESENT'
                          ? Icons.check_circle_outline
                          : Icons.event_note,
                      color: _statusColor(item['status']?.toString()),
                    ),
                    title: Text(
                      '${_date(item['attendance_date'])} · ${_label(item['status'])}',
                    ),
                    subtitle: Text(
                      'Check-in ${_time(item['check_in'])} · Check-out ${_time(item['check_out'])}${item['note'] == null ? '' : '\n${item['note']}'}',
                    ),
                    isThreeLine: item['note'] != null,
                  ),
                ),
          ],
        ),
      );
    },
  );
}

class _Metric extends StatelessWidget {
  const _Metric(this.label, this.value, this.color);
  final String label;
  final Object value;
  final Color color;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: 160,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              value.toString(),
              style: Theme.of(
                context,
              ).textTheme.titleLarge?.copyWith(color: color),
            ),
            Text(label, style: const TextStyle(color: SocietyColors.textMuted)),
          ],
        ),
      ),
    ),
  );
}

Color _statusColor(String? status) => switch (status) {
  'PRESENT' => SocietyColors.success,
  'ABSENT' => SocietyColors.danger,
  'HALF_DAY' => SocietyColors.warning,
  _ => SocietyColors.blue,
};

String _label(Object? value) => (value?.toString() ?? '')
    .toLowerCase()
    .split('_')
    .map(
      (part) =>
          part.isEmpty ? '' : '${part[0].toUpperCase()}${part.substring(1)}',
    )
    .join(' ');

String _time(Object? value) {
  if (value == null || value.toString().isEmpty) return '—';
  final text = value.toString();
  return text.length >= 5 ? text.substring(0, 5) : text;
}

String _date(Object? value) {
  final date = DateTime.tryParse(value?.toString() ?? '');
  if (date == null) return value?.toString() ?? '—';
  return '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year}';
}
