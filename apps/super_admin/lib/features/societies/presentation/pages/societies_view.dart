import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:super_admin_app/core/presentation/widgets/common_widgets.dart';
import '../../domain/entities/society_summary.dart';
import '../bloc/societies_bloc.dart';
import '../bloc/society_operations_bloc.dart';
import 'society_detail_dialog.dart';

class SocietiesView extends StatefulWidget {
  const SocietiesView({super.key, this.initialStatus});
  final String? initialStatus;
  @override
  State<SocietiesView> createState() => _SocietiesViewState();
}

class _SocietiesViewState extends State<SocietiesView> {
  String? status;
  final search = TextEditingController();
  @override
  void initState() {
    super.initState();
    status = widget.initialStatus;
    WidgetsBinding.instance.addPostFrameCallback(
      (_) => context.read<SocietyOperationsBloc>().add(
        const SocietyCreatePrepared(),
      ),
    );
    if (status != null) {
      WidgetsBinding.instance.addPostFrameCallback((_) => load());
    }
  }

  void load() => context.read<SocietiesBloc>().add(
    SocietiesLoaded(status: status, search: search.text),
  );
  Future<void> reject(SocietySummary society) async {
    final reason = TextEditingController();
    final ok = await showDialog<bool>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: const Text('Reject society'),
        content: TextField(
          controller: reason,
          decoration: const InputDecoration(labelText: 'Reason'),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialog, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialog, true),
            child: const Text('Reject'),
          ),
        ],
      ),
    );
    if (ok == true && mounted) {
      context.read<SocietiesBloc>().add(
        SocietyRejected(society.id, reason.text),
      );
    }
  }

  Future<void> create() async {
    final operations = context.read<SocietyOperationsBloc>().state;
    if (operations.feature != SocietyOperationFeature.create ||
        operations.data == null) {
      context.read<SocietyOperationsBloc>().add(const SocietyCreatePrepared());
      return;
    }
    final services = (operations.data as SocietyCreateWorkspace).services
        .map((item) => item.data)
        .where((service) => service['status'] == 'ACTIVE')
        .toList();
    if (!mounted) return;
    final data = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => _CreateSocietyDialog(services: services),
    );
    if (data == null) return;
    if (!mounted) return;
    context.read<SocietyOperationsBloc>().add(SocietyCreated(data));
  }

  Future<void> details(SocietySummary society) async {
    final operations = context.read<SocietyOperationsBloc>();
    operations.add(SocietyDetailLoaded(society.id));
    await showDialog(
      context: context,
      builder: (_) => BlocProvider.value(
        value: operations,
        child: SocietyDetailDialog(societyId: society.id),
      ),
    );
    if (mounted) load();
  }

  @override
  Widget build(BuildContext context) => MultiBlocListener(
    listeners: [
      BlocListener<SocietiesBloc, SocietiesState>(
        listener: (context, state) {
          if (state is SocietiesReady && state.message != null) {
            ScaffoldMessenger.of(
              context,
            ).showSnackBar(SnackBar(content: Text(state.message!)));
          }
        },
      ),
      BlocListener<SocietyOperationsBloc, SocietyOperationsState>(
        listener: (context, state) async {
          if (state.error != null) {
            ScaffoldMessenger.of(
              context,
            ).showSnackBar(SnackBar(content: Text(state.error!)));
          }
          if (state.completedMutation == SocietyOperationMutation.create &&
              state.result is Map) {
            final result = (state.result as SocietyCreateResult).data;
            await showDialog<void>(
              context: context,
              builder: (dialog) => AlertDialog(
                title: const Text('Society created'),
                content: SelectableText(
                  'Registration number: ${result['registrationNumber']}\n'
                  'Setup code: ${result['setupCode']}\n'
                  'Expires: ${result['expiresAt']}',
                ),
                actions: [
                  TextButton(
                    onPressed: () => Navigator.pop(dialog),
                    child: const Text('Done'),
                  ),
                ],
              ),
            );
            if (context.mounted) load();
          }
        },
      ),
    ],
    child: PageFrame(
      title: 'Societies',
      subtitle: 'Create, approve, reject and inspect societies',
      onRefresh: load,
      toolbar: Wrap(
        spacing: 10,
        runSpacing: 10,
        children: [
          FilledButton.icon(
            onPressed: create,
            icon: const Icon(Icons.add_business),
            label: const Text('Add society'),
          ),
          SizedBox(
            width: 250,
            child: TextField(
              controller: search,
              onSubmitted: (_) => load(),
              decoration: InputDecoration(
                labelText: 'Search',
                suffixIcon: IconButton(
                  onPressed: load,
                  icon: const Icon(Icons.search),
                ),
              ),
            ),
          ),
          DropdownButton<String?>(
            value: status,
            hint: const Text('All statuses'),
            items: [
              const DropdownMenuItem(value: null, child: Text('All statuses')),
              for (final value in [
                'PENDING_VERIFICATION',
                'APPROVED',
                'ACTIVE',
                'REJECTED',
              ])
                DropdownMenuItem(
                  value: value,
                  child: Text(value.replaceAll('_', ' ')),
                ),
            ],
            onChanged: (value) {
              setState(() => status = value);
              load();
            },
          ),
        ],
      ),
      child: BlocBuilder<SocietiesBloc, SocietiesState>(
        builder: (context, state) {
          if (state is SocietiesLoading) {
            return const Center(child: CircularProgressIndicator());
          }
          if (state is SocietiesFailure) {
            return FailureState(state.message, retry: load);
          }
          final items = (state as SocietiesReady).items;
          if (items.isEmpty) return const EmptyState('No societies found.');
          return ListView.separated(
            itemCount: items.length,
            separatorBuilder: (_, _) => const SizedBox(height: 8),
            itemBuilder: (context, index) {
              final society = items[index];
              return Card(
                child: ListTile(
                  onTap: () => details(society),
                  leading: CircleAvatar(child: Text(society.name[0])),
                  title: Text(society.name),
                  subtitle: Text(society.subtitle),
                  trailing: Wrap(
                    crossAxisAlignment: WrapCrossAlignment.center,
                    children: [
                      StatusChip(society.status),
                      if (society.status == 'PENDING_VERIFICATION')
                        IconButton(
                          onPressed: () => context.read<SocietiesBloc>().add(
                            SocietyApproved(society.id),
                          ),
                          icon: const Icon(
                            Icons.check_circle,
                            color: Colors.green,
                          ),
                        ),
                      const Icon(Icons.chevron_right),
                      if (society.status == 'PENDING_VERIFICATION')
                        IconButton(
                          onPressed: () => reject(society),
                          icon: const Icon(Icons.cancel, color: Colors.red),
                        ),
                    ],
                  ),
                ),
              );
            },
          );
        },
      ),
    ),
  );
}

class _CreateSocietyDialog extends StatefulWidget {
  const _CreateSocietyDialog({required this.services});

  final List<Map<String, dynamic>> services;
  @override
  State<_CreateSocietyDialog> createState() => _CreateSocietyDialogState();
}

class _CreateSocietyDialogState extends State<_CreateSocietyDialog> {
  final form = GlobalKey<FormState>();
  final selectedServices = <String>{};

  @override
  void initState() {
    super.initState();
    selectedServices.addAll(
      widget.services
          .where((service) => service['availability_type'] == 'DEFAULT_FREE')
          .map((service) => service['code'].toString()),
    );
  }

  final controls = {
    for (final key in [
      'name',
      'address',
      'city',
      'state',
      'pinCode',
      'contactPerson',
      'email',
      'mobile',
      'totalSpaces',
      'plannedStaff',
    ])
      key: TextEditingController(),
  };
  String plan = 'STANDARD';
  bool issueInvoice = true;
  Widget input(String key, String label, {int? minimum}) => SizedBox(
    width: 280,
    child: TextFormField(
      controller: controls[key],
      decoration: InputDecoration(labelText: label),
      keyboardType: minimum == null ? null : TextInputType.number,
      validator: (value) {
        if (value == null || value.trim().isEmpty) return 'Required';
        if (minimum != null &&
            (int.tryParse(value) ?? (minimum - 1)) < minimum) {
          return minimum == 0
              ? 'Enter 0 or a greater number'
              : 'Enter a number greater than 0';
        }
        return null;
      },
    ),
  );
  void submit(BuildContext context) {
    if (!form.currentState!.validate()) return;
    if (selectedServices.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Select at least one service')),
      );
      return;
    }
    Navigator.pop(context, {
      for (final entry in controls.entries) entry.key: entry.value.text,
      'societyType': 'COOPERATIVE',
      'totalBlocks': 1,
      'totalSpaces': int.parse(controls['totalSpaces']!.text),
      'plannedStaff': int.tryParse(controls['plannedStaff']!.text) ?? 0,
      'planCode': plan,
      'enabledModules': selectedServices.toList(),
      'issueInvoice': issueInvoice && plan != 'TRIAL',
    });
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Add society'),
    content: SizedBox(
      width: 600,
      child: Form(
        key: form,
        child: SingleChildScrollView(
          child: Wrap(
            spacing: 10,
            runSpacing: 10,
            children: [
              for (final entry in {
                'name': 'Society name',
                'address': 'Address',
                'city': 'City',
                'state': 'State',
                'pinCode': 'PIN code',
                'contactPerson': 'Contact person',
                'email': 'Email',
                'mobile': 'Mobile',
              }.entries)
                input(entry.key, entry.value),
              input('totalSpaces', 'Number of unit spaces', minimum: 1),
              input('plannedStaff', 'Expected staff count', minimum: 0),
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
                            label: Text(_catalogSelectionLabel(service)),
                            selected: selectedServices.contains(
                              service['code'],
                            ),
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
                  onChanged: (value) => setState(() => plan = value!),
                ),
              ),
              SizedBox(
                width: 570,
                child: SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('Issue first membership invoice now'),
                  subtitle: const Text(
                    'The society can pay it in Society Admin; captured funds appear in Platform Billing.',
                  ),
                  value: issueInvoice && plan != 'TRIAL',
                  onChanged: plan == 'TRIAL'
                      ? null
                      : (value) => setState(() => issueInvoice = value),
                ),
              ),
            ],
          ),
        ),
      ),
    ),
    actions: [
      TextButton(
        onPressed: () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(
        onPressed: () => submit(context),
        child: const Text('Create'),
      ),
    ],
  );
}

String _catalogSelectionLabel(Map<String, dynamic> service) =>
    switch (service['availability_type']?.toString()) {
      'DEFAULT_FREE' => '${service['name']} · Included',
      'PAID_PER_SPACE' =>
        '${service['name']} · ₹${service['price_per_space'] ?? 0}/space',
      _ => '${service['name']} · Free',
    };
