import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:super_admin_app/core/presentation/widgets/common_widgets.dart';
import '../../domain/entities/management_record.dart';
import '../bloc/management_bloc.dart';

class ManagementView extends StatefulWidget {
  const ManagementView({
    super.key,
    required this.title,
    required this.resource,
    required this.titleKeys,
    required this.subtitleKeys,
    this.action,
  });
  final String title;
  final ManagementResource resource;
  final List<String> titleKeys, subtitleKeys;
  final String? action;
  @override
  State<ManagementView> createState() => _ManagementViewState();
}

class _ManagementViewState extends State<ManagementView> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => load());
  }

  @override
  void didUpdateWidget(covariant ManagementView oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.resource != widget.resource) {
      WidgetsBinding.instance.addPostFrameCallback((_) => load());
    }
  }

  void load() =>
      context.read<ManagementBloc>().add(ManagementLoaded(widget.resource));

  String values(Map data, List<String> keys) => keys
      .map((key) => data[key])
      .where((value) => value != null)
      .map((value) => value.toString())
      .join(' • ');
  Future<void> runAction(Map data) async {
    if (widget.action == 'retry') {
      context.read<ManagementBloc>().add(
        NotificationRetried(data['id'].toString()),
      );
    } else if (widget.action == 'resolve') {
      await _resolve(data);
    } else if (widget.action == 'editPlan') {
      await _editPlan(data);
    }
  }

  Future<void> _resolve(Map data) async {
    final note = TextEditingController();
    final ok = await showDialog<bool>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: Text(data['subject']?.toString() ?? 'Support ticket'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(data['description']?.toString() ?? ''),
            const SizedBox(height: 12),
            TextField(
              controller: note,
              maxLines: 4,
              decoration: const InputDecoration(labelText: 'Resolution'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialog, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialog, true),
            child: const Text('Resolve'),
          ),
        ],
      ),
    );
    if (ok == true && mounted) {
      context.read<ManagementBloc>().add(
        ManagementTicketResolved(data['id'].toString(), note.text),
      );
    }
  }

  Future<void> _editPlan(Map data) async {
    final price = TextEditingController(
          text:
              (data['price_per_space'] ?? data['price_monthly'])?.toString() ??
              '0',
        ),
        maximum = TextEditingController(
          text: (data['max_spaces'] ?? data['max_flats'])?.toString() ?? '',
        ),
        staffPrice = TextEditingController(
          text: data['price_per_staff']?.toString() ?? '0',
        ),
        maximumStaff = TextEditingController(
          text: data['max_staff']?.toString() ?? '',
        ),
        trialDuration = TextEditingController(
          text:
              (data['trial_duration_value'] ??
                      data['trial_duration_months'] ??
                      1)
                  .toString(),
        );
    String approval = data['approval_mode']?.toString() ?? 'MANUAL';
    String trialUnit = data['trial_duration_unit']?.toString() ?? 'MONTHS';
    final ok = await showDialog<bool>(
      context: context,
      builder: (dialog) => StatefulBuilder(
        builder: (context, setDialog) => AlertDialog(
          title: Text('Edit ${data['code']}'),
          content: SizedBox(
            width: 420,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: price,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Price per space / month',
                  ),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: maximum,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Maximum spaces',
                  ),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: staffPrice,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Price per staff / month',
                  ),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: maximumStaff,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(labelText: 'Maximum staff'),
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: trialDuration,
                        keyboardType: TextInputType.number,
                        decoration: const InputDecoration(
                          labelText: 'Trial duration',
                        ),
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: DropdownButtonFormField<String>(
                        initialValue: trialUnit,
                        decoration: const InputDecoration(
                          labelText: 'Trial unit',
                        ),
                        items: ['DAYS', 'MONTHS']
                            .map(
                              (value) => DropdownMenuItem(
                                value: value,
                                child: Text(value),
                              ),
                            )
                            .toList(),
                        onChanged: (value) =>
                            setDialog(() => trialUnit = value!),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                DropdownButtonFormField<String>(
                  initialValue: approval,
                  decoration: const InputDecoration(labelText: 'Approval mode'),
                  items: ['AUTO', 'MANUAL']
                      .map(
                        (value) =>
                            DropdownMenuItem(value: value, child: Text(value)),
                      )
                      .toList(),
                  onChanged: (value) => setDialog(() => approval = value!),
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
              child: const Text('Save'),
            ),
          ],
        ),
      ),
    );
    if (ok == true && mounted) {
      context.read<ManagementBloc>().add(
        PlanSaved(
          UpdatePlanCommand(
            code: data['code'].toString(),
            approvalMode: approval,
            maxSpaces: int.tryParse(maximum.text),
            pricePerSpace: double.tryParse(price.text) ?? 0,
            maxStaff: int.tryParse(maximumStaff.text),
            pricePerStaff: double.tryParse(staffPrice.text) ?? 0,
            trialDurationValue: int.tryParse(trialDuration.text) ?? 1,
            trialDurationUnit: trialUnit,
          ),
        ),
      );
    }
  }

  Widget? trailing(Map data) {
    if (widget.action == 'editPlan') {
      return IconButton(
        tooltip: 'Edit plan',
        onPressed: () => runAction(data),
        icon: const Icon(Icons.edit),
      );
    }
    if (widget.action == 'retry' && data['status'] == 'FAILED') {
      return FilledButton.tonalIcon(
        onPressed: () => runAction(data),
        icon: const Icon(Icons.refresh),
        label: const Text('Retry'),
      );
    }
    if (widget.action == 'resolve' &&
        !['RESOLVED', 'CLOSED'].contains(data['status'])) {
      return FilledButton.tonal(
        onPressed: () => runAction(data),
        child: const Text('Resolve'),
      );
    }
    return data['status'] == null
        ? null
        : StatusChip(data['status'].toString());
  }

  String emptyMessage() => switch (widget.action) {
    'editPlan' => 'No subscription rates configured.',
    'retry' => 'No notifications have been queued.',
    'resolve' => 'No support tickets require attention.',
    _ =>
      widget.resource == ManagementResource.orders
          ? 'No subscription orders have been created.'
          : 'No audit events recorded yet.',
  };
  @override
  Widget build(BuildContext context) => PageFrame(
    title: widget.title,
    subtitle: _subtitle(),
    onRefresh: load,
    child: BlocConsumer<ManagementBloc, ManagementState>(
      listener: (context, state) {
        if (state.error != null) {
          ScaffoldMessenger.of(
            context,
          ).showSnackBar(SnackBar(content: Text(state.error!)));
        }
      },
      builder: (context, state) {
        if (state.loading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (state.error != null && state.data.isEmpty) {
          return FailureState(state.error!, retry: load);
        }
        final rows = state.data;
        if (rows.isEmpty) return EmptyState(emptyMessage());
        return ListView.separated(
          itemCount: rows.length,
          separatorBuilder: (_, _) => const SizedBox(height: 8),
          itemBuilder: (context, index) {
            final data = rows[index].data;
            final title = values(data, widget.titleKeys);
            return Card(
              child: ListTile(
                isThreeLine: widget.action == 'resolve',
                leading: Icon(_icon()),
                title: Text(title.isEmpty ? 'Record' : title),
                subtitle: Text(
                  values(data, widget.subtitleKeys) +
                      (widget.action == 'resolve' && data['description'] != null
                          ? '\n${data['description']}'
                          : ''),
                ),
                trailing: trailing(data),
              ),
            );
          },
        );
      },
    ),
  );
  String _subtitle() => switch (widget.action) {
    'editPlan' => 'Configure usage pricing, capacity and approval policy',
    'retry' => 'Monitor delivery and retry failed notifications',
    'resolve' => 'Review and resolve society support requests',
    _ =>
      widget.resource == ManagementResource.orders
          ? 'Subscription order and payment history'
          : 'Immutable platform activity history',
  };
  IconData _icon() => switch (widget.action) {
    'editPlan' => Icons.price_change,
    'retry' => Icons.notifications,
    'resolve' => Icons.support_agent,
    _ =>
      widget.resource == ManagementResource.orders
          ? Icons.receipt_long
          : Icons.history,
  };
}
