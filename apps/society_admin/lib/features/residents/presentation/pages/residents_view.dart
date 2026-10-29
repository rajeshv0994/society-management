// ignore_for_file: use_build_context_synchronously

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'package:society_admin_app/core/presentation/widgets/common.dart';
import '../../domain/entities/resident_create_input.dart';
import '../bloc/residents_bloc.dart';

class ResidentsView extends StatefulWidget {
  const ResidentsView({super.key});
  @override
  State<ResidentsView> createState() => _ResidentsState();
}

class _ResidentsState extends State<ResidentsView>
    with SingleTickerProviderStateMixin {
  late TabController tabs;
  @override
  void initState() {
    super.initState();
    tabs = TabController(length: 2, vsync: this);
    WidgetsBinding.instance.addPostFrameCallback((_) => load());
  }

  void load() => context.read<ResidentsBloc>().loadResidents();

  Future<void> addResident() async {
    try {
      final spaces = await context.read<ResidentsBloc>().loadAvailableSpaces();
      if (spaces.isEmpty) {
        throw Exception('Create property spaces before adding a resident.');
      }
      if (!mounted) return;
      final input = await showDialog<ResidentCreateInput>(
        context: context,
        builder: (_) => _AddResidentDialog(spaces: spaces),
      );
      if (input == null) return;
      await context.read<ResidentsBloc>().createResident(input);
      load();
      tabs.animateTo(1);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Resident added successfully')),
        );
      }
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(error.toString())));
      }
    }
  }

  Future<void> review(Map r, bool approve) async {
    String? value;
    if (approve) {
      final c = TextEditingController(text: 'Resident@123');
      value = await showDialog(
        context: context,
        builder: (d) => AlertDialog(
          title: const Text('Approve resident'),
          content: TextField(
            controller: c,
            decoration: const InputDecoration(labelText: 'Temporary password'),
          ),
          actions: [
            FilledButton(
              onPressed: () => Navigator.pop(d, c.text),
              child: const Text('Approve'),
            ),
          ],
        ),
      );
    } else {
      final c = TextEditingController();
      value = await showDialog(
        context: context,
        builder: (d) => AlertDialog(
          title: const Text('Reject resident'),
          content: TextField(
            controller: c,
            decoration: const InputDecoration(labelText: 'Reason'),
          ),
          actions: [
            FilledButton(
              onPressed: () => Navigator.pop(d, c.text),
              child: const Text('Reject'),
            ),
          ],
        ),
      );
    }
    if (value != null) {
      await context.read<ResidentsBloc>().reviewResident(
        r['id'],
        approve: approve,
        value: value,
      );
      load();
    }
  }

  Future<void> details(Map<String, dynamic> resident) async {
    final bloc = context.read<ResidentsBloc>();
    await showDialog(
      context: context,
      builder: (_) => BlocProvider.value(
        value: bloc,
        child: _ResidentDetailDialog(residentId: resident['id'].toString()),
      ),
    );
    if (mounted) load();
  }

  Widget list(
    String keyName,
    bool approvals,
  ) => BlocBuilder<ResidentsBloc, AdminResourceState>(
    builder: (c, state) {
      final s = state.resource(keyName);
      if (s.status == ResourceStatus.initial ||
          s.status == ResourceStatus.loading) {
        return const Center(child: CircularProgressIndicator());
      }
      if (s.error != null) {
        return ErrorState(message: s.error!, onRetry: load);
      }
      final rows =
          (s.value as ResourceCollection?)?.items ??
          const <Map<String, dynamic>>[];
      if (rows.isEmpty) return const Empty('No residents found.');
      return ListView.builder(
        itemCount: rows.length,
        itemBuilder: (c, i) {
          final r = rows[i];
          return Card(
            child: ListTile(
              onTap: approvals ? null : () => details(r),
              title: Text(r['full_name'].toString()),
              subtitle: Text(
                '${r['block'] ?? ''} ${r['space_number'] ?? r['unit_number'] ?? r['flat_number'] ?? ''} • ${r['resident_type'] ?? ''} • ${r['email']}',
              ),
              trailing: approvals
                  ? Wrap(
                      children: [
                        IconButton(
                          onPressed: () => review(r, true),
                          icon: const Icon(Icons.check, color: Colors.green),
                        ),
                        IconButton(
                          onPressed: () => review(r, false),
                          icon: const Icon(Icons.close, color: Colors.red),
                        ),
                      ],
                    )
                  : Wrap(
                      crossAxisAlignment: WrapCrossAlignment.center,
                      children: [
                        StatusChip(r['status'].toString()),
                        const Icon(Icons.chevron_right),
                      ],
                    ),
            ),
          );
        },
      );
    },
  );
  @override
  Widget build(c) => PageFrame(
    title: 'Residents',
    subtitle: 'Review registrations and manage active residents',
    action: FilledButton.icon(
      onPressed: addResident,
      icon: const Icon(Icons.person_add_alt_1),
      label: const Text('Add resident'),
    ),
    child: Column(
      children: [
        TabBar(
          controller: tabs,
          isScrollable: true,
          tabs: const [
            Tab(text: 'Pending approvals'),
            Tab(text: 'Active residents'),
          ],
        ),
        Expanded(
          child: TabBarView(
            controller: tabs,
            children: [list('pending', true), list('active', false)],
          ),
        ),
      ],
    ),
  );
}

class _AddResidentDialog extends StatefulWidget {
  const _AddResidentDialog({required this.spaces});

  final List<Map<String, dynamic>> spaces;

  @override
  State<_AddResidentDialog> createState() => _AddResidentDialogState();
}

class _AddResidentDialogState extends State<_AddResidentDialog> {
  final form = GlobalKey<FormState>();
  final fullName = TextEditingController();
  final mobile = TextEditingController();
  final email = TextEditingController();
  final password = TextEditingController(text: 'Resident@123');
  final moveInDate = TextEditingController();
  late Map<String, dynamic> space = widget.spaces.first;
  String residentType = 'OWNER';

  @override
  void dispose() {
    fullName.dispose();
    mobile.dispose();
    email.dispose();
    password.dispose();
    moveInDate.dispose();
    super.dispose();
  }

  String? required(String? value) =>
      value == null || value.trim().isEmpty ? 'This field is required' : null;

  void submit() {
    if (!form.currentState!.validate()) return;
    Navigator.pop(
      context,
      ResidentCreateInput(
        spaceId: space['id'].toString(),
        residentType: residentType,
        fullName: fullName.text.trim(),
        mobile: mobile.text.trim(),
        email: email.text.trim(),
        temporaryPassword: password.text,
        moveInDate: moveInDate.text.trim().isEmpty
            ? null
            : moveInDate.text.trim(),
      ),
    );
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Add resident'),
    content: SizedBox(
      width: 520,
      child: Form(
        key: form,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              DropdownButtonFormField<Map<String, dynamic>>(
                initialValue: space,
                isExpanded: true,
                decoration: const InputDecoration(labelText: 'Property space'),
                items: [
                  for (final item in widget.spaces)
                    DropdownMenuItem(
                      value: item,
                      child: Text(
                        '${item['label'] ?? item['space_number']} · ${item['occupancy_status']}',
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                ],
                onChanged: (value) => setState(() => space = value!),
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<String>(
                initialValue: residentType,
                decoration: const InputDecoration(labelText: 'Resident type'),
                items: const [
                  DropdownMenuItem(value: 'OWNER', child: Text('Owner')),
                  DropdownMenuItem(value: 'TENANT', child: Text('Tenant')),
                ],
                onChanged: (value) => setState(() => residentType = value!),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: fullName,
                validator: required,
                decoration: const InputDecoration(labelText: 'Full name'),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: mobile,
                validator: required,
                keyboardType: TextInputType.phone,
                decoration: const InputDecoration(labelText: 'Mobile'),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: email,
                validator: (value) {
                  final message = required(value);
                  if (message != null) return message;
                  return value!.contains('@') ? null : 'Enter a valid email';
                },
                keyboardType: TextInputType.emailAddress,
                decoration: const InputDecoration(labelText: 'Email'),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: password,
                obscureText: true,
                validator: (value) => (value?.length ?? 0) < 8
                    ? 'Use at least 8 characters'
                    : null,
                decoration: const InputDecoration(
                  labelText: 'Temporary password',
                ),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: moveInDate,
                decoration: const InputDecoration(
                  labelText: 'Move-in date (optional)',
                  hintText: 'YYYY-MM-DD',
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
      FilledButton(onPressed: submit, child: const Text('Add resident')),
    ],
  );
}

class _ResidentDetailDialog extends StatefulWidget {
  const _ResidentDetailDialog({required this.residentId});
  final String residentId;

  @override
  State<_ResidentDetailDialog> createState() => _ResidentDetailDialogState();
}

class _ResidentDetailDialogState extends State<_ResidentDetailDialog>
    with SingleTickerProviderStateMixin {
  late final TabController tabs = TabController(length: 6, vsync: this);

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => load());
  }

  String get keyName =>
      context.read<ResidentsBloc>().residentKey(widget.residentId);
  void load() => context.read<ResidentsBloc>().loadResident(widget.residentId);

  Future<void> addHousehold() async {
    final name = TextEditingController();
    final relationship = TextEditingController();
    final mobile = TextEditingController();
    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: const Text('Add household member'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: name,
              decoration: const InputDecoration(labelText: 'Full name'),
            ),
            TextField(
              controller: relationship,
              decoration: const InputDecoration(labelText: 'Relationship'),
            ),
            TextField(
              controller: mobile,
              decoration: const InputDecoration(labelText: 'Mobile'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialog),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialog, {
              'fullName': name.text,
              'relationship': relationship.text,
              'mobile': mobile.text,
            }),
            child: const Text('Add'),
          ),
        ],
      ),
    );
    if (result == null) return;
    await context.read<ResidentsBloc>().addHouseholdMember(
      widget.residentId,
      result,
    );
    load();
  }

  Future<void> removeHousehold(String memberId) async {
    await context.read<ResidentsBloc>().removeHouseholdMember(
      widget.residentId,
      memberId,
    );
    load();
  }

  Future<void> resolveComplaint(
    String complaintId,
    String currentStatus,
  ) async {
    await context.read<ResidentsBloc>().resolveComplaint(
      complaintId,
      currentStatus,
    );
    load();
  }

  Future<void> recordPayment(Map invoice) async {
    final total = _number(invoice['subtotal']) + _number(invoice['late_fee']);
    final outstanding = total - _number(invoice['paid_amount']);
    final amount = TextEditingController(text: outstanding.toStringAsFixed(2));
    final reference = TextEditingController();
    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: Text('Record payment • ${invoice['invoice_number']}'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: amount,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(labelText: 'Amount'),
            ),
            TextField(
              controller: reference,
              decoration: const InputDecoration(labelText: 'Reference'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialog),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialog, {
              'amount': double.tryParse(amount.text),
              'paymentMethod': 'CASH',
              if (reference.text.trim().isNotEmpty)
                'providerReference': reference.text.trim(),
            }),
            child: const Text('Record'),
          ),
        ],
      ),
    );
    if (result == null || result['amount'] == null) return;
    await context.read<ResidentsBloc>().recordInvoicePayment(
      invoice['id'],
      result,
    );
    load();
  }

  Future<void> visitorStatus(String visitorId, String status) async {
    await context.read<ResidentsBloc>().updateVisitorStatus(visitorId, status);
    load();
  }

  double _number(Object? value) =>
      value is num ? value.toDouble() : double.tryParse('$value') ?? 0;

  @override
  Widget build(BuildContext context) => Dialog.fullscreen(
    child: BlocBuilder<ResidentsBloc, AdminResourceState>(
      builder: (context, state) {
        final snapshot = state.resource(keyName);
        if (snapshot.status == ResourceStatus.initial ||
            snapshot.status == ResourceStatus.loading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.error != null) {
          return Scaffold(
            appBar: AppBar(
              leading: IconButton(
                onPressed: () => Navigator.pop(context),
                icon: const Icon(Icons.close),
              ),
            ),
            body: ErrorState(message: snapshot.error!, onRetry: load),
          );
        }
        final data = (snapshot.value as ResourceDocument).data;
        final resident = data['resident'] as Map;
        return Scaffold(
          appBar: AppBar(
            leading: IconButton(
              onPressed: () => Navigator.pop(context),
              icon: const Icon(Icons.close),
            ),
            title: Text(resident['full_name'].toString()),
            bottom: TabBar(
              controller: tabs,
              isScrollable: true,
              tabs: const [
                Tab(text: 'Overview'),
                Tab(text: 'Complaints'),
                Tab(text: 'Billing & payments'),
                Tab(text: 'Visitors'),
                Tab(text: 'Household'),
                Tab(text: 'Amenity bookings'),
              ],
            ),
          ),
          body: TabBarView(
            controller: tabs,
            children: [
              _overview(data),
              _complaints(data['complaints'] as List),
              _billing(data['invoices'] as List, data['payments'] as List),
              _visitors(data['visitors'] as List),
              _household(data['household'] as List),
              _records(
                data['amenityBookings'] as List,
                (row) => row['amenity_name'].toString(),
                (row) => '${row['starts_at']} • ₹${row['amount']}',
              ),
            ],
          ),
        );
      },
    ),
  );

  Widget _overview(Map<String, dynamic> data) {
    final resident = data['resident'] as Map;
    final registration = data['registration'] as Map;
    final space = data['space'] as Map;
    return ListView(
      padding: const EdgeInsets.all(24),
      children: [
        Wrap(
          spacing: 12,
          runSpacing: 12,
          children: [
            _metric('Status', resident['status']),
            _metric('Resident type', resident['resident_type']),
            _metric('Email', resident['email']),
            _metric('Mobile', resident['mobile']),
            _metric('Property group', space['block']),
            _metric('Section', space['section']),
            _metric('Space', space['space_number']),
            _metric('Occupancy', space['occupancy_status']),
            _metric('Move in', registration['move_in_date']),
            _metric('Move out', registration['move_out_date']),
            _metric('Emergency contact', registration['emergency_contact']),
          ],
        ),
      ],
    );
  }

  Widget _complaints(List rows) => _records(
    rows,
    (row) => row['title'].toString(),
    (row) => '${row['priority']} • ${row['description']}',
    action: (row) => ['RESOLVED', 'CLOSED'].contains(row['status'])
        ? StatusChip(row['status'].toString())
        : FilledButton.tonal(
            onPressed: () => resolveComplaint(
              row['id'].toString(),
              row['status'].toString(),
            ),
            child: const Text('Resolve'),
          ),
  );

  Widget _billing(List invoices, List payments) => ListView(
    padding: const EdgeInsets.all(24),
    children: [
      Text('Invoices', style: Theme.of(context).textTheme.titleLarge),
      if (invoices.isEmpty) const Text('No invoices.'),
      for (final invoice in invoices)
        Card(
          child: ListTile(
            title: Text(
              '${invoice['invoice_number']} • ₹${invoice['subtotal']}',
            ),
            subtitle: Text(
              'Due ${invoice['due_date']} • Paid ₹${invoice['paid_amount']}',
            ),
            trailing: invoice['status'] == 'PAID'
                ? StatusChip(invoice['status'].toString())
                : FilledButton.tonal(
                    onPressed: () => recordPayment(invoice as Map),
                    child: const Text('Record payment'),
                  ),
          ),
        ),
      const SizedBox(height: 20),
      Text('Payments', style: Theme.of(context).textTheme.titleLarge),
      if (payments.isEmpty) const Text('No payments.'),
      for (final payment in payments)
        Card(
          child: ListTile(
            title: Text('₹${payment['amount']} • ${payment['payment_method']}'),
            subtitle: Text(
              '${payment['paid_at']} • ${payment['provider_reference'] ?? ''}',
            ),
            trailing: StatusChip(payment['status'].toString()),
          ),
        ),
    ],
  );

  Widget _visitors(List rows) => _records(
    rows,
    (row) => row['visitor_name'].toString(),
    (row) => '${row['purpose'] ?? ''} • ${row['expected_at'] ?? ''}',
    action: (row) => switch (row['status']) {
      'APPROVED' => FilledButton.tonal(
        onPressed: () => visitorStatus(row['id'].toString(), 'CHECKED_IN'),
        child: const Text('Check in'),
      ),
      'CHECKED_IN' => FilledButton.tonal(
        onPressed: () => visitorStatus(row['id'].toString(), 'CHECKED_OUT'),
        child: const Text('Check out'),
      ),
      _ => StatusChip(row['status'].toString()),
    },
  );

  Widget _household(List rows) => Column(
    children: [
      Padding(
        padding: const EdgeInsets.all(16),
        child: Align(
          alignment: Alignment.centerRight,
          child: FilledButton.icon(
            onPressed: addHousehold,
            icon: const Icon(Icons.person_add),
            label: const Text('Add household member'),
          ),
        ),
      ),
      Expanded(
        child: _records(
          rows,
          (row) => row['full_name'].toString(),
          (row) => '${row['relationship']} • ${row['mobile'] ?? ''}',
          action: (row) => IconButton(
            tooltip: 'Remove',
            onPressed: () => removeHousehold(row['id'].toString()),
            icon: const Icon(Icons.delete_outline),
          ),
          padding: EdgeInsets.zero,
        ),
      ),
    ],
  );

  Widget _records(
    List rows,
    String Function(Map row) title,
    String Function(Map row) subtitle, {
    Widget Function(Map row)? action,
    EdgeInsets padding = const EdgeInsets.all(24),
  }) => rows.isEmpty
      ? const Empty('No related records.')
      : ListView.separated(
          padding: padding,
          itemCount: rows.length,
          separatorBuilder: (_, _) => const SizedBox(height: 8),
          itemBuilder: (context, index) {
            final row = rows[index] as Map;
            return Card(
              child: ListTile(
                title: Text(title(row)),
                subtitle: Text(subtitle(row)),
                trailing: action?.call(row),
              ),
            );
          },
        );

  Widget _metric(String label, Object? value) => SizedBox(
    width: 260,
    child: Card(
      child: ListTile(
        title: Text(label),
        subtitle: Text(value?.toString() ?? '-'),
      ),
    ),
  );
}
