import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:super_admin_app/core/presentation/widgets/common_widgets.dart';
import 'package:super_admin_app/features/societies/domain/entities/society_summary.dart';
import '../../domain/entities/requirements_workspace.dart';
import '../bloc/requirements_bloc.dart';

class RequirementsView extends StatefulWidget {
  const RequirementsView({super.key});
  @override
  State<RequirementsView> createState() => _RequirementsViewState();
}

class _RequirementsViewState extends State<RequirementsView>
    with SingleTickerProviderStateMixin {
  late TabController tabs;
  @override
  void initState() {
    super.initState();
    tabs = TabController(length: 2, vsync: this);
    WidgetsBinding.instance.addPostFrameCallback((_) => load());
  }

  void load() =>
      context.read<RequirementsBloc>().add(const RequirementsLoaded());

  Future<void> mark(Map data, String status) async {
    final notes = TextEditingController(text: data['admin_notes']?.toString());
    final ok = await showDialog<bool>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: Text('Mark as ${status.replaceAll('_', ' ')}'),
        content: TextField(
          controller: notes,
          maxLines: 3,
          decoration: const InputDecoration(labelText: 'Admin notes'),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialog, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialog, true),
            child: const Text('Update'),
          ),
        ],
      ),
    );
    if (ok == true) {
      if (!mounted) return;
      context.read<RequirementsBloc>().add(
        RequirementReviewed(data['id'].toString(), status, notes.text),
      );
    }
  }

  Future<void> buildPlan(Map requirement) async {
    final state = context.read<RequirementsBloc>().state;
    final societies = state.data?.societies ?? const <SocietySummary>[];
    final body = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) =>
          _CustomPlanDialog(requirement: requirement, societies: societies),
    );
    if (body == null) return;
    if (!mounted) return;
    context.read<RequirementsBloc>().add(CustomPlanCreated(body));
  }

  Future<void> editPlan(CustomPlanRecord plan) async {
    final state = context.read<RequirementsBloc>().state;
    final societies = state.data?.societies ?? const <SocietySummary>[];
    final body = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => _CustomPlanDialog(plan: plan.data, societies: societies),
    );
    if (body == null || !mounted) return;
    context.read<RequirementsBloc>().add(
      CustomPlanUpdated(plan.data['id'].toString(), body),
    );
  }

  Future<void> cancelPlan(CustomPlanRecord plan) async {
    var reason = '';
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: const Text('Cancel custom plan?'),
        content: TextField(
          onChanged: (value) => reason = value,
          maxLength: 300,
          maxLines: 3,
          decoration: const InputDecoration(
            labelText: 'Cancellation reason',
            helperText:
                'The plan remains in history. Cancelling an Active custom plan returns future pricing to the platform plan; invoices are cancelled separately in Billing.',
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialog, false),
            child: const Text('Keep plan'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialog, true),
            child: const Text('Cancel plan'),
          ),
        ],
      ),
    );
    final value = reason.trim();
    if (confirmed != true || !mounted) return;
    if (value.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Cancellation reason is required')),
      );
      return;
    }
    context.read<RequirementsBloc>().add(
      CustomPlanCancelled(plan.data['id'].toString(), value),
    );
  }

  @override
  Widget build(c) => BlocConsumer<RequirementsBloc, RequirementsState>(
    listener: (context, state) {
      if (state.error != null) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(state.error!)));
      }
      if (state.completedMutation == RequirementsMutation.createPlan) {
        tabs.animateTo(1);
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('Custom plan created')));
      }
      if (state.completedMutation == RequirementsMutation.updatePlan) {
        tabs.animateTo(1);
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('Custom plan updated')));
      }
      if (state.completedMutation == RequirementsMutation.cancelPlan) {
        tabs.animateTo(1);
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('Custom plan cancelled')));
      }
    },
    builder: (context, state) {
      if (state.loading) {
        return const Center(child: CircularProgressIndicator());
      }
      if (state.error != null && state.data == null) {
        return FailureState(state.error!, retry: load);
      }
      final requirements = state.data!.requirements;
      final plans = state.data!.plans;
      return PageFrame(
        title: 'Custom requirements',
        subtitle: 'Review enquiries and build society-specific module plans',
        onRefresh: load,
        toolbar: TabBar(
          controller: tabs,
          tabs: const [
            Tab(text: 'Requirement inbox'),
            Tab(text: 'Custom plans'),
          ],
        ),
        child: TabBarView(
          controller: tabs,
          children: [_requirements(requirements), _plans(plans)],
        ),
      );
    },
  );
  Widget _requirements(List<RequirementRecord> rows) {
    if (rows.isEmpty) {
      return const EmptyState('No custom requirements submitted.');
    }
    return ListView.separated(
      itemCount: rows.length,
      separatorBuilder: (_, _) => const SizedBox(height: 10),
      itemBuilder: (context, index) {
        final data = rows[index].data;
        return Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        data['society_name'].toString(),
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                    ),
                    StatusChip(data['status'].toString()),
                  ],
                ),
                Text(
                  '${data['contact_person']} • ${data['email']} • ${data['mobile']}',
                ),
                if (data['city'] != null)
                  Text(
                    '${data['city']} • ${data['estimated_spaces'] ?? data['estimated_units'] ?? data['estimated_flats'] ?? '-'} spaces • ${data['estimated_staff'] ?? '-'} staff • Budget ₹${data['budget_monthly'] ?? '-'}/month',
                  ),
                const Divider(),
                Text(data['requirement_details'].toString()),
                const SizedBox(height: 6),
                Text(
                  'Requested modules: ${data['requested_modules_text']}',
                  style: Theme.of(context).textTheme.labelLarge,
                ),
                const SizedBox(height: 12),
                Wrap(
                  spacing: 8,
                  children: [
                    OutlinedButton.icon(
                      onPressed: () => mark(data, 'CONTACTED'),
                      icon: const Icon(Icons.call),
                      label: const Text('Mark contacted'),
                    ),
                    FilledButton.icon(
                      onPressed: () => buildPlan(data),
                      icon: const Icon(Icons.tune),
                      label: const Text('Build custom plan'),
                    ),
                    TextButton(
                      onPressed: () => mark(data, 'REJECTED'),
                      child: const Text('Reject'),
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _plans(List<CustomPlanRecord> rows) {
    if (rows.isEmpty) return const EmptyState('No custom plans created yet.');
    return ListView.separated(
      itemCount: rows.length,
      separatorBuilder: (_, _) => const SizedBox(height: 8),
      itemBuilder: (context, index) {
        final plan = rows[index];
        final data = plan.data;
        final fixedMonthly =
            double.tryParse(data['price_monthly']?.toString() ?? '') ?? 0;
        final fixedPricing = fixedMonthly > 0;
        final historical = const {
          'REPLACED',
          'CANCELLED',
        }.contains(data['status']);
        final cancellable = const {'DRAFT', 'ACTIVE'}.contains(data['status']);
        return Card(
          child: ListTile(
            leading: const Icon(Icons.description),
            title: Text('Custom rates • ${data['society_name']}'),
            subtitle: Text(
              '${fixedPricing ? '₹${data['price_monthly']} fixed monthly' : '₹${data['price_per_space'] ?? 0} / space + ₹${data['price_per_staff'] ?? 0} / staff monthly'} • ${data['max_spaces'] ?? data['max_units'] ?? data['max_flats'] ?? 'Unlimited'} spaces • ${data['enabled_modules_text']}${data['cancellation_reason'] == null ? '' : '\nCancelled: ${data['cancellation_reason']}'}',
            ),
            trailing: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                StatusChip(data['status'].toString()),
                const SizedBox(width: 8),
                IconButton(
                  tooltip: historical
                      ? 'Historical plans cannot be edited'
                      : 'Edit custom plan',
                  onPressed: historical ? null : () => editPlan(plan),
                  icon: const Icon(Icons.edit_outlined),
                ),
                if (cancellable)
                  IconButton(
                    tooltip: 'Cancel custom plan',
                    onPressed: () => cancelPlan(plan),
                    icon: const Icon(Icons.cancel_outlined),
                  ),
              ],
            ),
          ),
        );
      },
    );
  }
}

class _CustomPlanDialog extends StatefulWidget {
  const _CustomPlanDialog({
    this.requirement,
    this.plan,
    required this.societies,
  }) : assert(requirement != null || plan != null);
  final Map? requirement;
  final Map? plan;
  final List<SocietySummary> societies;
  @override
  State<_CustomPlanDialog> createState() => _CustomPlanDialogState();
}

class _CustomPlanDialogState extends State<_CustomPlanDialog> {
  final name = TextEditingController(),
      description = TextEditingController(),
      price = TextEditingController(),
      staffPrice = TextEditingController(),
      fixedPrice = TextEditingController(),
      setup = TextEditingController(text: '0'),
      flats = TextEditingController(),
      staff = TextEditingController(text: '20'),
      storage = TextEditingController(text: '50');
  final modules = <String>{};
  String? societyId;
  String cycle = 'MONTHLY', status = 'DRAFT', pricingModel = 'USAGE_BASED';
  final available = [
    'RESIDENTS',
    'STAFF',
    'VISITORS',
    'COMPLAINTS',
    'BILLING',
    'PAYMENTS',
    'AMENITIES',
    'NOTICES',
    'DOCUMENTS',
    'PARKING',
    'VENDORS',
    'CUSTOM_REPORTS',
    'API_ACCESS',
  ];
  @override
  void initState() {
    super.initState();
    final source = widget.plan ?? widget.requirement!;
    final editing = widget.plan != null;
    societyId = source[editing ? 'society_id' : 'society_id']?.toString();
    name.text = source['plan_name']?.toString() ?? 'Custom rates';
    description.text = source['description']?.toString() ?? '';
    flats.text =
        source[editing ? 'max_flats' : 'estimated_flats']?.toString() ?? '';
    staff.text =
        source[editing ? 'max_staff' : 'estimated_staff']?.toString() ?? '20';
    storage.text = source['max_storage_gb']?.toString() ?? '50';
    setup.text = source['setup_fee']?.toString() ?? '0';
    cycle = source['billing_cycle']?.toString() ?? 'MONTHLY';
    status = editing ? source['status']?.toString() ?? 'DRAFT' : 'DRAFT';
    price.text = source['price_per_space']?.toString() ?? '';
    staffPrice.text = source['price_per_staff']?.toString() ?? '0';
    final legacyFixed =
        double.tryParse(source['price_monthly']?.toString() ?? '') ?? 0;
    if (editing && legacyFixed > 0) {
      pricingModel = 'FIXED';
      fixedPrice.text = source['price_monthly'].toString();
    } else if (!editing && source['budget_monthly'] != null) {
      pricingModel = 'FIXED';
      fixedPrice.text = source['budget_monthly'].toString();
    }
    final requested =
        source[editing ? 'enabled_modules_text' : 'requested_modules_text']
            ?.toString() ??
        '';
    modules.addAll(available.where(requested.contains));
  }

  void submit(BuildContext context) {
    if (societyId == null ||
        name.text.isEmpty ||
        (pricingModel == 'FIXED'
            ? fixedPrice.text.isEmpty
            : price.text.isEmpty) ||
        modules.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Society, rates and modules are required'),
        ),
      );
      return;
    }
    final body = <String, dynamic>{
      'planName': name.text,
      'description': description.text,
      'enabledModules': modules.toList(),
      'maxSpaces': int.tryParse(flats.text),
      'maxStaff': int.tryParse(staff.text),
      'maxStorageGb': int.tryParse(storage.text),
      'pricePerSpace': pricingModel == 'FIXED'
          ? 0
          : double.tryParse(price.text) ?? 0,
      'pricePerStaff': pricingModel == 'FIXED'
          ? 0
          : double.tryParse(staffPrice.text) ?? 0,
      'fixedMonthlyPrice': pricingModel == 'FIXED'
          ? double.tryParse(fixedPrice.text) ?? 0
          : 0,
      'setupFee': double.tryParse(setup.text) ?? 0,
      'billingCycle': cycle,
      'validFrom':
          widget.plan?['valid_from']?.toString() ??
          DateTime.now().toIso8601String().substring(0, 10),
      'validUntil': widget.plan?['valid_until'],
      'status': status,
    };
    if (widget.plan == null) {
      body['societyId'] = societyId;
      body['requirementId'] = widget.requirement!['id'];
    }
    Navigator.pop(context, body);
  }

  @override
  Widget build(context) => AlertDialog(
    title: Text(widget.plan == null ? 'Build custom plan' : 'Edit custom plan'),
    content: SizedBox(
      width: 700,
      child: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            DropdownButtonFormField<String>(
              initialValue: societyId,
              decoration: const InputDecoration(labelText: 'Assign to society'),
              items: widget.societies
                  .map(
                    (s) => DropdownMenuItem(value: s.id, child: Text(s.name)),
                  )
                  .toList(),
              onChanged: widget.plan == null
                  ? (value) => setState(() => societyId = value)
                  : null,
            ),
            const SizedBox(height: 10),
            TextField(
              controller: name,
              decoration: const InputDecoration(labelText: 'Plan name'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: description,
              maxLines: 2,
              decoration: const InputDecoration(labelText: 'Description'),
            ),
            const SizedBox(height: 12),
            Text(
              'Enabled modules',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            Wrap(
              spacing: 4,
              children: [
                for (final module in available)
                  FilterChip(
                    label: Text(module.replaceAll('_', ' ')),
                    selected: modules.contains(module),
                    onSelected: (selected) => setState(
                      () => selected
                          ? modules.add(module)
                          : modules.remove(module),
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 12),
            DropdownButtonFormField<String>(
              initialValue: pricingModel,
              decoration: const InputDecoration(labelText: 'Pricing model'),
              items: const [
                DropdownMenuItem(
                  value: 'USAGE_BASED',
                  child: Text('Per space and staff'),
                ),
                DropdownMenuItem(
                  value: 'FIXED',
                  child: Text('Fixed monthly amount'),
                ),
              ],
              onChanged: (value) => setState(() => pricingModel = value!),
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: [
                if (pricingModel == 'USAGE_BASED') ...[
                  _field(price, 'Price per space / month'),
                  _field(staffPrice, 'Price per staff / month'),
                ] else
                  _field(fixedPrice, 'Fixed monthly amount'),
                _field(setup, 'Setup fee'),
                _field(flats, 'Maximum spaces'),
                _field(staff, 'Max staff'),
                _field(storage, 'Storage GB'),
                SizedBox(
                  width: 210,
                  child: DropdownButtonFormField<String>(
                    initialValue: cycle,
                    decoration: const InputDecoration(
                      labelText: 'Billing cycle',
                    ),
                    items: ['MONTHLY', 'QUARTERLY', 'YEARLY']
                        .map((v) => DropdownMenuItem(value: v, child: Text(v)))
                        .toList(),
                    onChanged: (v) => cycle = v!,
                  ),
                ),
                SizedBox(
                  width: 210,
                  child: DropdownButtonFormField<String>(
                    initialValue: status,
                    decoration: const InputDecoration(labelText: 'Status'),
                    items:
                        (widget.plan?['status'] == 'ACTIVE'
                                ? ['ACTIVE']
                                : ['DRAFT', 'ACTIVE'])
                            .map(
                              (v) => DropdownMenuItem(value: v, child: Text(v)),
                            )
                            .toList(),
                    onChanged: (v) => status = v!,
                  ),
                ),
              ],
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
        onPressed: () => submit(context),
        child: Text(
          widget.plan == null ? 'Create custom plan' : 'Save changes',
        ),
      ),
    ],
  );
  Widget _field(TextEditingController controller, String label) => SizedBox(
    width: 210,
    child: TextField(
      controller: controller,
      keyboardType: TextInputType.number,
      decoration: InputDecoration(labelText: label),
    ),
  );

  @override
  void dispose() {
    name.dispose();
    description.dispose();
    price.dispose();
    staffPrice.dispose();
    fixedPrice.dispose();
    setup.dispose();
    flats.dispose();
    staff.dispose();
    storage.dispose();
    super.dispose();
  }
}
