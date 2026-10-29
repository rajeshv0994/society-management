// ignore_for_file: use_build_context_synchronously

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'package:society_admin_app/core/presentation/widgets/common.dart';
import '../bloc/profile_bloc.dart';

class ProfileView extends StatefulWidget {
  const ProfileView({super.key, this.onEntitlementsChanged});
  final VoidCallback? onEntitlementsChanged;

  @override
  State<ProfileView> createState() => _ProfileViewState();
}

class _ProfileViewState extends State<ProfileView> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => reload());
  }

  void reload() {
    final bloc = context.read<ProfileBloc>();
    bloc.loadProfile();
    bloc.loadSubscription();
  }

  Future<void> editSubscription(Map<String, dynamic> subscription) async {
    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => _SubscriptionForm(subscription: subscription),
    );
    if (result == null) return;
    try {
      await context.read<ProfileBloc>().updateSubscription(result);
      reload();
      widget.onEntitlementsChanged?.call();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text(
              'Subscription requirements and billing cycle updated.',
            ),
          ),
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

  Future<void> edit(Map<String, dynamic> profile) async {
    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => _ProfileForm(profile: profile),
    );
    if (result == null) return;
    try {
      await context.read<ProfileBloc>().updateProfile(result);
      reload();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Society profile updated.')),
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

  Future<void> uploadLogo() async {
    final picked = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: const ['png', 'jpg', 'jpeg'],
      withData: true,
    );
    final file = picked?.files.single;
    if (file?.bytes == null) return;
    try {
      await context.read<ProfileBloc>().uploadLogo(file!.bytes!, file.name);
      reload();
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(error.toString())));
      }
    }
  }

  @override
  Widget build(BuildContext context) => PageFrame(
    title: 'Society profile',
    subtitle:
        'Manage the identity and contact details visible across the platform',
    child: BlocBuilder<ProfileBloc, AdminResourceState>(
      builder: (context, state) {
        final snapshot = state.resource('profile');
        final subscriptionSnapshot = state.resource('subscription');
        if (snapshot.status == ResourceStatus.initial ||
            snapshot.status == ResourceStatus.loading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.error != null) {
          return ErrorState(message: snapshot.error!, onRetry: reload);
        }
        final profile = (snapshot.value as ResourceDocument).data;
        final logoPath = profile['logo_url']?.toString();
        return ListView(
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Wrap(
                  spacing: 24,
                  runSpacing: 20,
                  crossAxisAlignment: WrapCrossAlignment.center,
                  children: [
                    CircleAvatar(
                      radius: 52,
                      backgroundImage: logoPath == null
                          ? null
                          : NetworkImage(
                              context.read<ProfileBloc>().absoluteUrl(logoPath),
                            ),
                      child: logoPath == null
                          ? const Icon(Icons.apartment, size: 45)
                          : null,
                    ),
                    SizedBox(
                      width: 500,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            profile['name'].toString(),
                            style: Theme.of(context).textTheme.headlineSmall,
                          ),
                          const SizedBox(height: 6),
                          Text(
                            profile['description']?.toString() ??
                                'No description added.',
                          ),
                          const SizedBox(height: 16),
                          Wrap(
                            spacing: 10,
                            runSpacing: 10,
                            children: [
                              FilledButton.tonalIcon(
                                onPressed: uploadLogo,
                                icon: const Icon(Icons.upload),
                                label: const Text('Upload logo'),
                              ),
                              FilledButton.icon(
                                onPressed: () => edit(profile),
                                icon: const Icon(Icons.edit),
                                label: const Text('Edit profile'),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            Builder(
              builder: (context) {
                if (subscriptionSnapshot.status == ResourceStatus.initial ||
                    subscriptionSnapshot.status == ResourceStatus.loading) {
                  return const Card(
                    child: Padding(
                      padding: EdgeInsets.all(24),
                      child: LinearProgressIndicator(),
                    ),
                  );
                }
                if (subscriptionSnapshot.error != null) {
                  return Card(
                    child: ListTile(
                      title: const Text('Subscription quantities'),
                      subtitle: Text(subscriptionSnapshot.error!),
                    ),
                  );
                }
                final subscription =
                    (subscriptionSnapshot.value as ResourceDocument).data;
                final fixedMonthly =
                    double.tryParse(
                      subscription['fixed_monthly_price']?.toString() ?? '',
                    ) ??
                    0;
                final fixedCustomPricing = fixedMonthly > 0;
                return Card(
                  child: Padding(
                    padding: const EdgeInsets.all(20),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Expanded(
                              child: Text(
                                'Platform subscription requirements',
                                style: Theme.of(context).textTheme.titleLarge,
                              ),
                            ),
                            FilledButton.icon(
                              onPressed: () => editSubscription(subscription),
                              icon: const Icon(Icons.tune),
                              label: const Text('Change requirements'),
                            ),
                          ],
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'Choose spaces, staff seats, optional services, and payment cycle here. Pay issued invoices under Platform Membership.',
                        ),
                        const SizedBox(height: 12),
                        Wrap(
                          spacing: 28,
                          runSpacing: 12,
                          children: [
                            _metric(
                              'Selected spaces',
                              subscription['selected_spaces'],
                              'Configured: ${subscription['configured_spaces']}',
                            ),
                            _metric(
                              'Selected staff',
                              subscription['selected_staff'],
                              'Active: ${subscription['active_staff']}',
                            ),
                            _metric(
                              'Space rate',
                              fixedCustomPricing
                                  ? 'Included'
                                  : '₹${subscription['price_per_space']}',
                              fixedCustomPricing
                                  ? 'Fixed custom plan'
                                  : 'per month',
                            ),
                            _metric(
                              'Staff rate',
                              fixedCustomPricing
                                  ? 'Included'
                                  : '₹${subscription['price_per_staff']}',
                              fixedCustomPricing
                                  ? 'Fixed custom plan'
                                  : 'per month',
                            ),
                            _metric(
                              'Base monthly',
                              '₹${subscription['base_monthly_price'] ?? subscription['estimated_monthly_price']}',
                              fixedCustomPricing
                                  ? 'Fixed custom plan'
                                  : 'Spaces and staff',
                            ),
                            _metric(
                              'Paid services',
                              '₹${subscription['service_monthly_price'] ?? 0}',
                              'Per-space add-ons',
                            ),
                            _metric(
                              'Estimated monthly',
                              '₹${subscription['estimated_monthly_price']}',
                              'Complete monthly total',
                            ),
                            _metric(
                              'Billing cycle',
                              _subscriptionLabel(subscription['billing_cycle']),
                              '${subscription['billing_cycle_months']} month(s)',
                            ),
                            _metric(
                              'Amount per cycle',
                              '₹${subscription['billing_cycle_amount']}',
                              'Invoiced by the platform',
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
            const SizedBox(height: 16),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Wrap(
                  spacing: 30,
                  runSpacing: 20,
                  children: [
                    for (final entry in {
                      'Registration number': profile['registration_number'],
                      'Society type': profile['society_type'],
                      'Contact person': profile['contact_person'],
                      'Email': profile['email'],
                      'Mobile': profile['mobile'],
                      'Website': profile['website'],
                      'Location':
                          '${profile['city']}, ${profile['state']} ${profile['pin_code']}',
                    }.entries)
                      SizedBox(
                        width: 260,
                        child: ListTile(
                          contentPadding: EdgeInsets.zero,
                          title: Text(entry.key),
                          subtitle: Text(entry.value?.toString() ?? '-'),
                        ),
                      ),
                  ],
                ),
              ),
            ),
          ],
        );
      },
    ),
  );

  Widget _metric(String label, Object? value, String detail) => SizedBox(
    width: 190,
    child: ListTile(
      contentPadding: EdgeInsets.zero,
      title: Text(label),
      subtitle: Text('$value\n$detail'),
    ),
  );
}

class _SubscriptionForm extends StatefulWidget {
  const _SubscriptionForm({required this.subscription});
  final Map<String, dynamic> subscription;

  @override
  State<_SubscriptionForm> createState() => _SubscriptionFormState();
}

class _SubscriptionFormState extends State<_SubscriptionForm> {
  late final spaces = TextEditingController(
    text: widget.subscription['selected_spaces'].toString(),
  );
  late final staff = TextEditingController(
    text: widget.subscription['selected_staff'].toString(),
  );
  late String billingCycle =
      widget.subscription['billing_cycle']?.toString() ?? 'MONTHLY';
  late final List<Map<String, dynamic>> catalog =
      (widget.subscription['service_catalog'] as List? ?? const [])
          .map((row) => Map<String, dynamic>.from(row as Map))
          .toList();
  late final Set<String> selectedServices = catalog
      .where((service) => service['selected'] == true)
      .map((service) => service['code'].toString())
      .toSet();

  double get _cycleAmount {
    final spaceCount = int.tryParse(spaces.text) ?? 0;
    final staffCount = int.tryParse(staff.text) ?? 0;
    final spaceRate =
        double.tryParse(
          widget.subscription['price_per_space']?.toString() ?? '',
        ) ??
        0;
    final staffRate =
        double.tryParse(
          widget.subscription['price_per_staff']?.toString() ?? '',
        ) ??
        0;
    final fixedMonthly =
        double.tryParse(
          widget.subscription['fixed_monthly_price']?.toString() ?? '',
        ) ??
        0;
    final months = switch (billingCycle) {
      'QUARTERLY' => 3,
      'YEARLY' => 12,
      _ => 1,
    };
    final serviceRate = catalog
        .where(
          (service) =>
              selectedServices.contains(service['code']) &&
              service['status'] == 'ACTIVE' &&
              service['availability_type'] == 'PAID_PER_SPACE',
        )
        .fold<double>(
          0,
          (total, service) =>
              total +
              (double.tryParse(service['price_per_space'].toString()) ?? 0),
        );
    return (fixedMonthly +
            spaceCount * (spaceRate + serviceRate) +
            staffCount * staffRate) *
        months;
  }

  @override
  void dispose() {
    spaces.dispose();
    staff.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Choose subscription requirements'),
    content: SizedBox(
      width: 620,
      child: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'You can increase or reduce quantities. A reduction cannot go below spaces already configured or active staff accounts.',
            ),
            const SizedBox(height: 16),
            TextField(
              controller: spaces,
              onChanged: (_) => setState(() {}),
              keyboardType: TextInputType.number,
              decoration: InputDecoration(
                labelText: 'Total property spaces',
                helperText:
                    'In use: ${widget.subscription['configured_spaces']} • Maximum: ${widget.subscription['max_spaces'] ?? 'Unlimited'}',
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: staff,
              onChanged: (_) => setState(() {}),
              keyboardType: TextInputType.number,
              decoration: InputDecoration(
                labelText: 'Total staff seats',
                helperText:
                    'Active: ${widget.subscription['active_staff']} • Maximum: ${widget.subscription['max_staff'] ?? 'Unlimited'}',
              ),
            ),
            const SizedBox(height: 12),
            DropdownButtonFormField<String>(
              initialValue: billingCycle,
              decoration: const InputDecoration(labelText: 'Payment cycle'),
              items: const [
                DropdownMenuItem(value: 'MONTHLY', child: Text('Monthly')),
                DropdownMenuItem(value: 'QUARTERLY', child: Text('Quarterly')),
                DropdownMenuItem(value: 'YEARLY', child: Text('Yearly')),
              ],
              onChanged: (value) => setState(() => billingCycle = value!),
            ),
            const SizedBox(height: 16),
            Text(
              'Platform services',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 4),
            const Text(
              'Included services cannot be removed. Paid services are charged per selected property space.',
            ),
            const SizedBox(height: 10),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                for (final service in catalog.where(
                  (service) => service['status'] == 'ACTIVE',
                ))
                  FilterChip(
                    key: Key('subscription-service-${service['code']}'),
                    selected: selectedServices.contains(service['code']),
                    onSelected: service['required'] == true
                        ? null
                        : (selected) => setState(
                            () => selected
                                ? selectedServices.add(
                                    service['code'].toString(),
                                  )
                                : selectedServices.remove(service['code']),
                          ),
                    label: Text(_serviceSubscriptionLabel(service)),
                  ),
              ],
            ),
            const SizedBox(height: 14),
            Text(
              'Calculated amount for this cycle: ₹${_cycleAmount.toStringAsFixed(2)}',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 4),
            const Text(
              'After saving, the platform admin issues an invoice. Pay it from Platform Membership and submit the transaction reference.',
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
        onPressed: () {
          final totalSpaces = int.tryParse(spaces.text);
          final totalStaff = int.tryParse(staff.text);
          if (totalSpaces == null ||
              totalSpaces < 1 ||
              totalStaff == null ||
              totalStaff < 0) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Enter valid quantities.')),
            );
            return;
          }
          Navigator.pop(context, {
            'totalSpaces': totalSpaces,
            'totalStaff': totalStaff,
            'billingCycle': billingCycle,
            'enabledModules': selectedServices.toList(),
          });
        },
        child: const Text('Update subscription'),
      ),
    ],
  );
}

String _subscriptionLabel(Object? value) => (value?.toString() ?? '')
    .toLowerCase()
    .split('_')
    .map(
      (part) =>
          part.isEmpty ? '' : '${part[0].toUpperCase()}${part.substring(1)}',
    )
    .join(' ');

String _serviceSubscriptionLabel(Map<String, dynamic> service) =>
    switch (service['availability_type']?.toString()) {
      'DEFAULT_FREE' => '${service['name']} · Included free',
      'PAID_PER_SPACE' =>
        '${service['name']} · ₹${service['price_per_space'] ?? 0}/space/month',
      _ => '${service['name']} · Free',
    };

class _ProfileForm extends StatefulWidget {
  const _ProfileForm({required this.profile});
  final Map<String, dynamic> profile;

  @override
  State<_ProfileForm> createState() => _ProfileFormState();
}

class _ProfileFormState extends State<_ProfileForm> {
  late final fields = {
    for (final key in [
      'name',
      'description',
      'website',
      'contact_person',
      'email',
      'mobile',
      'address',
      'city',
      'state',
      'pin_code',
    ])
      key: TextEditingController(text: widget.profile[key]?.toString() ?? ''),
  };

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Edit society profile'),
    content: SizedBox(
      width: 650,
      child: SingleChildScrollView(
        child: Wrap(
          spacing: 12,
          runSpacing: 12,
          children: [
            for (final entry in {
              'name': 'Society name',
              'description': 'Description',
              'website': 'Website',
              'contact_person': 'Contact person',
              'email': 'Email',
              'mobile': 'Mobile',
              'address': 'Address',
              'city': 'City',
              'state': 'State',
              'pin_code': 'PIN code',
            }.entries)
              SizedBox(
                width: entry.key == 'description' || entry.key == 'address'
                    ? 612
                    : 300,
                child: TextField(
                  controller: fields[entry.key],
                  maxLines: entry.key == 'description' || entry.key == 'address'
                      ? 3
                      : 1,
                  decoration: InputDecoration(labelText: entry.value),
                ),
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
        onPressed: () => Navigator.pop(context, {
          'name': fields['name']!.text,
          'description': fields['description']!.text,
          'website': fields['website']!.text,
          'contactPerson': fields['contact_person']!.text,
          'email': fields['email']!.text,
          'mobile': fields['mobile']!.text,
          'address': fields['address']!.text,
          'city': fields['city']!.text,
          'state': fields['state']!.text,
          'pinCode': fields['pin_code']!.text,
        }),
        child: const Text('Save changes'),
      ),
    ],
  );
}
