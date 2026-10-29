import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'package:society_admin_app/core/presentation/widgets/common.dart';
import '../bloc/audit_bloc.dart';

class AuditView extends StatefulWidget {
  const AuditView({super.key});

  @override
  State<AuditView> createState() => _AuditViewState();
}

class _AuditViewState extends State<AuditView> {
  final search = TextEditingController();
  bool sensitiveOnly = true;
  int page = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => load());
  }

  @override
  void dispose() {
    search.dispose();
    super.dispose();
  }

  void load({int? targetPage}) {
    page = targetPage ?? 0;
    context.read<AuditBloc>().loadAudit(
      page: page,
      sensitiveOnly: sensitiveOnly,
      search: search.text,
    );
  }

  @override
  Widget build(BuildContext context) => PageFrame(
    title: 'Sensitive activity',
    subtitle: 'See who performed security-sensitive changes in your society',
    action: IconButton(
      tooltip: 'Refresh activity',
      onPressed: () => load(targetPage: page),
      icon: const Icon(Icons.refresh),
    ),
    child: BlocBuilder<AuditBloc, AdminResourceState>(
      builder: (context, state) {
        final snapshot = state.resource('audit');
        return ListView(
          children: [
            _filters(),
            const SizedBox(height: 12),
            if (snapshot.status == ResourceStatus.initial ||
                snapshot.status == ResourceStatus.loading)
              const SizedBox(
                height: 180,
                child: Center(child: CircularProgressIndicator()),
              )
            else if (snapshot.error != null)
              SizedBox(
                height: 180,
                child: ErrorState(message: snapshot.error!, onRetry: load),
              )
            else
              ..._content((snapshot.value as ResourceDocument).data),
          ],
        );
      },
    ),
  );

  Widget _filters() => Card(
    child: Padding(
      padding: const EdgeInsets.all(14),
      child: Wrap(
        spacing: 12,
        runSpacing: 10,
        crossAxisAlignment: WrapCrossAlignment.center,
        children: [
          SizedBox(
            width: 340,
            child: TextField(
              key: const ValueKey('society-audit-search'),
              controller: search,
              onSubmitted: (_) => load(),
              decoration: const InputDecoration(
                labelText: 'Search action, target or detail',
                prefixIcon: Icon(Icons.search),
              ),
            ),
          ),
          FilterChip(
            key: const ValueKey('sensitive-only'),
            selected: sensitiveOnly,
            avatar: const Icon(Icons.shield_outlined, size: 18),
            label: const Text('Sensitive only'),
            onSelected: (value) {
              setState(() => sensitiveOnly = value);
              load();
            },
          ),
          FilledButton.icon(
            key: const ValueKey('society-audit-apply'),
            onPressed: load,
            icon: const Icon(Icons.filter_alt),
            label: const Text('Apply'),
          ),
          TextButton(
            onPressed: () {
              search.clear();
              setState(() => sensitiveOnly = true);
              load();
            },
            child: const Text('Clear'),
          ),
        ],
      ),
    ),
  );

  List<Widget> _content(Map<String, dynamic> data) {
    final items = (data['items'] as List? ?? const [])
        .map((item) => Map<String, dynamic>.from(item as Map))
        .toList();
    if (items.isEmpty) {
      return const [
        SizedBox(
          height: 180,
          child: Empty('No activity matches these filters.'),
        ),
      ];
    }
    final current = (data['page'] as num?)?.toInt() ?? page;
    final total = (data['total'] as num?)?.toInt() ?? items.length;
    final pages = (data['total_pages'] as num?)?.toInt() ?? 1;
    return [
      for (final event in items) ...[_event(event), const SizedBox(height: 8)],
      Wrap(
        alignment: WrapAlignment.end,
        crossAxisAlignment: WrapCrossAlignment.center,
        children: [
          Text('$total events • page ${current + 1} of $pages'),
          IconButton(
            tooltip: 'Previous page',
            onPressed: current > 0 ? () => load(targetPage: current - 1) : null,
            icon: const Icon(Icons.chevron_left),
          ),
          IconButton(
            tooltip: 'Next page',
            onPressed: current + 1 < pages
                ? () => load(targetPage: current + 1)
                : null,
            icon: const Icon(Icons.chevron_right),
          ),
        ],
      ),
    ];
  }

  Widget _event(Map<String, dynamic> event) {
    final occurred = DateTime.tryParse(
      event['occurred_at']?.toString() ?? '',
    )?.toLocal();
    final actor = event['actor_name']?.toString() ?? 'System';
    final area = event['entity_type'] == null
        ? null
        : _readable(event['entity_type'].toString());
    return Card(
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: SocietyColors.warning.withValues(alpha: .12),
          child: const Icon(
            Icons.shield_outlined,
            color: SocietyColors.warning,
          ),
        ),
        title: Text(_readable(event['action']?.toString() ?? 'Activity')),
        subtitle: Text(
          '$actor${area == null ? '' : ' • $area'}'
          '${occurred == null ? '' : '\n${_timestamp(occurred)}'}',
        ),
        trailing: event['sensitive'] == true
            ? const Tooltip(
                message: 'Sensitive action',
                child: Icon(Icons.lock_outline),
              )
            : null,
        onTap: () => _details(event),
      ),
    );
  }

  Future<void> _details(Map<String, dynamic> event) {
    final details = event['details'] is Map
        ? Map<String, dynamic>.from(event['details'] as Map)
        : <String, dynamic>{};
    final occurred = DateTime.tryParse(
      event['occurred_at']?.toString() ?? '',
    )?.toLocal();
    final extraDetails = details.entries
        .where((entry) => !_hiddenDetailKeys.contains(entry.key))
        .toList();
    return showDialog<void>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: Row(
          children: [
            const Icon(Icons.policy_outlined),
            const SizedBox(width: 10),
            Expanded(
              child: Text(_readable(event['action']?.toString() ?? 'Activity')),
            ),
          ],
        ),
        content: SizedBox(
          width: 600,
          child: SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                _detailRow(
                  'Performed by',
                  event['actor_name']?.toString() ?? 'System',
                ),
                if (details['actor_email_snapshot'] != null)
                  _detailRow(
                    'Performer contact',
                    details['actor_email_snapshot'].toString(),
                  ),
                _detailRow(
                  'Date and time',
                  occurred == null ? 'Not available' : _timestamp(occurred),
                ),
                _detailRow(
                  'Area',
                  _readable(event['entity_type']?.toString() ?? 'General'),
                ),
                if (event['entity_id'] != null)
                  _detailRow('Target record', event['entity_id'].toString()),
                if (details['http_method'] != null)
                  _detailRow(
                    'Change type',
                    _operation(details['http_method'].toString()),
                  ),
                _detailRow(
                  'Result',
                  _readable(details['outcome']?.toString() ?? 'Succeeded'),
                  valueColor: SocietyColors.success,
                ),
                if (extraDetails.isNotEmpty) ...[
                  const Padding(
                    padding: EdgeInsets.fromLTRB(4, 18, 4, 6),
                    child: Text(
                      'Change details',
                      style: TextStyle(fontWeight: FontWeight.w700),
                    ),
                  ),
                  for (final detail in extraDetails)
                    _detailRow(
                      _detailLabel(detail.key),
                      _friendlyValue(detail.value),
                    ),
                ],
              ],
            ),
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
  }

  static const _hiddenDetailKeys = {
    'http_method',
    'outcome',
    'route',
    'actor_name_snapshot',
    'actor_email_snapshot',
  };

  Widget _detailRow(String label, String value, {Color? valueColor}) =>
      Container(
        padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 10),
        decoration: const BoxDecoration(
          border: Border(bottom: BorderSide(color: SocietyColors.border)),
        ),
        child: LayoutBuilder(
          builder: (context, constraints) {
            final labelWidget = Text(
              label,
              style: const TextStyle(color: SocietyColors.textMuted),
            );
            final valueWidget = SelectableText(
              value,
              style: TextStyle(
                color: valueColor ?? SocietyColors.navy,
                fontWeight: FontWeight.w600,
              ),
            );
            if (constraints.maxWidth < 440) {
              return Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [labelWidget, const SizedBox(height: 4), valueWidget],
              );
            }
            return Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                SizedBox(width: 155, child: labelWidget),
                Expanded(child: valueWidget),
              ],
            );
          },
        ),
      );

  String _operation(String method) => switch (method.toUpperCase()) {
    'POST' => 'Created',
    'PUT' => 'Updated',
    'PATCH' => 'Changed',
    'DELETE' => 'Removed or deactivated',
    _ => _readable(method),
  };

  String _detailLabel(String key) => switch (key) {
    'flat_id' || 'unit_id' || 'space_id' => 'Space',
    'staff_id' || 'user_id' => 'Person',
    _ => _readable(key),
  };

  String _friendlyValue(Object? value) {
    if (value == null) return 'Not available';
    if (value is bool) return value ? 'Yes' : 'No';
    if (value is Iterable) {
      return value.map(_friendlyValue).join(', ');
    }
    if (value is Map) {
      return value.entries
          .map(
            (entry) =>
                '${_detailLabel(entry.key.toString())}: ${_friendlyValue(entry.value)}',
          )
          .join('\n');
    }
    final text = value.toString();
    return RegExp(r'^[A-Z][A-Z0-9_]*$').hasMatch(text) ? _readable(text) : text;
  }

  String _readable(String value) => value
      .replaceFirst(RegExp(r'^API_'), '')
      .replaceAll('_', ' ')
      .toLowerCase()
      .split(' ')
      .map(
        (word) => word.isEmpty
            ? word
            : '${word[0].toUpperCase()}${word.substring(1)}',
      )
      .join(' ');

  String _timestamp(DateTime value) {
    const months = [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec',
    ];
    final hour = value.hour % 12 == 0 ? 12 : value.hour % 12;
    final period = value.hour < 12 ? 'AM' : 'PM';
    return '${value.day} ${months[value.month - 1]} ${value.year}, '
        '$hour:${value.minute.toString().padLeft(2, '0')} $period';
  }
}
