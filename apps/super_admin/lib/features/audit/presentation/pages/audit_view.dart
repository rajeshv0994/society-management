import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:super_admin_app/core/presentation/widgets/common_widgets.dart';
import '../../domain/entities/audit_page.dart';
import '../bloc/audit_bloc.dart';

class AuditView extends StatefulWidget {
  const AuditView({super.key});

  @override
  State<AuditView> createState() => _AuditViewState();
}

class _AuditViewState extends State<AuditView> {
  final search = TextEditingController();
  final action = TextEditingController();
  final entityType = TextEditingController();
  int page = 0;
  DateTime? from, to;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => load());
  }

  @override
  void dispose() {
    search.dispose();
    action.dispose();
    entityType.dispose();
    super.dispose();
  }

  void load({int? targetPage}) {
    page = targetPage ?? 0;
    context.read<AuditBloc>().add(
      AuditLoaded(
        page: page,
        search: search.text,
        action: action.text,
        entityType: entityType.text,
        from: from,
        to: to == null
            ? null
            : DateTime(to!.year, to!.month, to!.day, 23, 59, 59, 999),
      ),
    );
  }

  void clear() {
    search.clear();
    action.clear();
    entityType.clear();
    from = null;
    to = null;
    load();
  }

  Future<void> pickDate(bool start) async {
    final value = await showDatePicker(
      context: context,
      initialDate: (start ? from : to) ?? DateTime.now(),
      firstDate: DateTime(2020),
      lastDate: DateTime.now(),
    );
    if (value != null) {
      setState(() => start ? from = value : to = value);
    }
  }

  @override
  Widget build(BuildContext context) => PageFrame(
    title: 'Audit log',
    subtitle: 'Searchable, immutable history of successful platform changes',
    onRefresh: () => load(targetPage: page),
    child: BlocBuilder<AuditBloc, AuditState>(
      builder: (context, state) => ListView(
        children: [
          _filters(),
          const SizedBox(height: 12),
          if (state.loading)
            const SizedBox(
              height: 180,
              child: Center(child: CircularProgressIndicator()),
            )
          else if (state.error != null && state.data == null)
            SizedBox(
              height: 180,
              child: FailureState(state.error!, retry: load),
            )
          else if (state.data!.items.isEmpty)
            const SizedBox(
              height: 180,
              child: EmptyState('No audit events match the selected filters.'),
            )
          else ...[
            _events(state.data!.items),
            _pagination(state.data!),
          ],
        ],
      ),
    ),
  );

  Widget _filters() => Card(
    child: Padding(
      padding: const EdgeInsets.all(14),
      child: Wrap(
        spacing: 10,
        runSpacing: 10,
        crossAxisAlignment: WrapCrossAlignment.center,
        children: [
          SizedBox(
            width: 320,
            child: TextField(
              key: const ValueKey('audit-search'),
              controller: search,
              onSubmitted: (_) => load(),
              decoration: const InputDecoration(
                labelText: 'Search',
                prefixIcon: Icon(Icons.search),
              ),
            ),
          ),
          OutlinedButton.icon(
            onPressed: _advancedFilters,
            icon: const Icon(Icons.tune),
            label: Text(
              action.text.isEmpty &&
                      entityType.text.isEmpty &&
                      from == null &&
                      to == null
                  ? 'More filters'
                  : 'Filters active',
            ),
          ),
          FilledButton.icon(
            key: const ValueKey('audit-apply'),
            onPressed: load,
            icon: const Icon(Icons.filter_alt),
            label: const Text('Apply'),
          ),
          TextButton(onPressed: clear, child: const Text('Clear')),
        ],
      ),
    ),
  );

  Future<void> _advancedFilters() async {
    final originalAction = action.text;
    final originalEntityType = entityType.text;
    final originalFrom = from;
    final originalTo = to;
    final accepted = await showDialog<bool>(
      context: context,
      builder: (dialog) => StatefulBuilder(
        builder: (context, setDialog) => AlertDialog(
          title: const Text('Audit filters'),
          content: SizedBox(
            width: 420,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                TextField(
                  controller: action,
                  decoration: const InputDecoration(labelText: 'Exact action'),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: entityType,
                  decoration: const InputDecoration(labelText: 'Entity type'),
                ),
                const SizedBox(height: 10),
                Wrap(
                  spacing: 10,
                  children: [
                    OutlinedButton.icon(
                      onPressed: () async {
                        await pickDate(true);
                        setDialog(() {});
                      },
                      icon: const Icon(Icons.date_range),
                      label: Text(from == null ? 'From' : _date(from!)),
                    ),
                    OutlinedButton.icon(
                      onPressed: () async {
                        await pickDate(false);
                        setDialog(() {});
                      },
                      icon: const Icon(Icons.event),
                      label: Text(to == null ? 'To' : _date(to!)),
                    ),
                  ],
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialog, false),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(dialog, true),
              child: const Text('Apply'),
            ),
          ],
        ),
      ),
    );
    if (accepted == true) {
      load();
    } else {
      action.text = originalAction;
      entityType.text = originalEntityType;
      setState(() {
        from = originalFrom;
        to = originalTo;
      });
    }
  }

  Widget _events(List<AuditEventRecord> items) => Column(
    children: [
      for (var index = 0; index < items.length; index++) ...[
        Builder(
          builder: (context) {
            final event = items[index];
            final scope = [
              event.actorName,
              if (event.societyName != null) event.societyName!,
              if (event.occurredAt != null) _timestamp(event.occurredAt!),
            ].join(' • ');
            return Card(
              child: ListTile(
                leading: const CircleAvatar(
                  child: Icon(Icons.history, size: 20),
                ),
                title: Text('${event.action} • ${event.entityType}'),
                subtitle: Text(
                  '${event.entityId == null ? '' : '${event.entityId}\n'}$scope',
                ),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _details(event),
              ),
            );
          },
        ),
        if (index + 1 < items.length) const SizedBox(height: 8),
      ],
    ],
  );

  Widget _pagination(AuditPage result) => Padding(
    padding: const EdgeInsets.only(top: 8),
    child: Wrap(
      alignment: WrapAlignment.end,
      crossAxisAlignment: WrapCrossAlignment.center,
      children: [
        Text(
          '${result.total} events • page ${result.page + 1} of ${result.totalPages}',
        ),
        IconButton(
          tooltip: 'Previous page',
          onPressed: result.page > 0 ? () => load(targetPage: page - 1) : null,
          icon: const Icon(Icons.chevron_left),
        ),
        IconButton(
          tooltip: 'Next page',
          onPressed: result.page + 1 < result.totalPages
              ? () => load(targetPage: page + 1)
              : null,
          icon: const Icon(Icons.chevron_right),
        ),
      ],
    ),
  );

  Future<void> _details(AuditEventRecord event) => showDialog<void>(
    context: context,
    builder: (dialog) => AlertDialog(
      title: Text(event.action),
      content: SizedBox(
        width: 560,
        child: SingleChildScrollView(
          child: SelectableText(
            const JsonEncoder.withIndent('  ').convert({
              'entity_type': event.entityType,
              'entity_id': event.entityId,
              'actor': event.actorName,
              'society': event.societyName,
              'occurred_at': event.data['occurred_at'],
              'details': event.details,
            }),
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

  String _date(DateTime value) =>
      '${value.year.toString().padLeft(4, '0')}-${value.month.toString().padLeft(2, '0')}-${value.day.toString().padLeft(2, '0')}';
  String _timestamp(DateTime value) =>
      '${_date(value)} ${value.hour.toString().padLeft(2, '0')}:${value.minute.toString().padLeft(2, '0')}';
}
