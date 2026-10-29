import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:super_admin_app/core/presentation/widgets/common_widgets.dart';
import '../bloc/service_catalog_bloc.dart';

class ServicesView extends StatefulWidget {
  const ServicesView({super.key});

  @override
  State<ServicesView> createState() => _ServicesViewState();
}

class _ServicesViewState extends State<ServicesView> {
  @override
  void initState() {
    super.initState();
    load();
  }

  void load() =>
      context.read<ServiceCatalogBloc>().add(const ServiceCatalogLoaded());

  Future<void> edit([Map<String, dynamic>? service]) async {
    final result = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => _ServiceDialog(service: service),
    );
    if (result == null) return;
    if (!mounted) return;
    context.read<ServiceCatalogBloc>().add(
      ServiceCatalogSaved(result, code: service?['code']?.toString()),
    );
  }

  @override
  Widget build(BuildContext context) => PageFrame(
    title: 'Service catalog',
    subtitle: 'Configure default, optional-free, and paid per-space services',
    onRefresh: load,
    toolbar: FilledButton.icon(
      onPressed: () => edit(),
      icon: const Icon(Icons.add),
      label: const Text('Add service'),
    ),
    child: BlocConsumer<ServiceCatalogBloc, ServiceCatalogState>(
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
        final services = state.data.map((item) => item.data).toList();
        if (services.isEmpty) {
          return const EmptyState('No services configured.');
        }
        return ListView.separated(
          itemCount: services.length,
          separatorBuilder: (_, _) => const SizedBox(height: 8),
          itemBuilder: (context, index) {
            final service = services[index];
            return Card(
              child: ListTile(
                leading: const Icon(Icons.extension_outlined),
                title: Text(service['name'].toString()),
                subtitle: Text(
                  '${service['code']} • ${_commercialLabel(service)}\n'
                  '${service['description'] ?? 'No description'}\n'
                  '${service['selected_society_count'] ?? 0} societies selected • projected ₹${service['projected_monthly_revenue'] ?? 0}/month',
                ),
                isThreeLine: true,
                trailing: Wrap(
                  crossAxisAlignment: WrapCrossAlignment.center,
                  children: [
                    StatusChip(service['status'].toString()),
                    IconButton(
                      tooltip: 'Edit service',
                      onPressed: () => edit(service),
                      icon: const Icon(Icons.edit),
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    ),
  );
}

class _ServiceDialog extends StatefulWidget {
  const _ServiceDialog({this.service});

  final Map<String, dynamic>? service;

  @override
  State<_ServiceDialog> createState() => _ServiceDialogState();
}

class _ServiceDialogState extends State<_ServiceDialog> {
  final form = GlobalKey<FormState>();
  late final code = TextEditingController(
    text: widget.service?['code']?.toString(),
  );
  late final name = TextEditingController(
    text: widget.service?['name']?.toString(),
  );
  late final description = TextEditingController(
    text: widget.service?['description']?.toString(),
  );
  late final sortOrder = TextEditingController(
    text: widget.service?['sort_order']?.toString() ?? '0',
  );
  late String status = widget.service?['status']?.toString() ?? 'COMING_SOON';
  late String availability =
      widget.service?['availability_type']?.toString() ?? 'OPTIONAL_FREE';
  late final pricePerSpace = TextEditingController(
    text: widget.service?['price_per_space']?.toString() ?? '0',
  );
  @override
  void dispose() {
    code.dispose();
    name.dispose();
    description.dispose();
    sortOrder.dispose();
    pricePerSpace.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: Text(widget.service == null ? 'Add service' : 'Manage service'),
    content: SizedBox(
      width: 480,
      child: Form(
        key: form,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextFormField(
                controller: code,
                enabled: widget.service == null,
                decoration: const InputDecoration(labelText: 'Service code'),
                validator: (value) =>
                    value == null ||
                        !RegExp(r'^[A-Z][A-Z0-9_]{1,39}$').hasMatch(value)
                    ? 'Use uppercase letters, numbers and underscores'
                    : null,
              ),
              const SizedBox(height: 10),
              TextFormField(
                controller: name,
                decoration: const InputDecoration(labelText: 'Service name'),
                validator: (value) =>
                    value == null || value.trim().isEmpty ? 'Required' : null,
              ),
              const SizedBox(height: 10),
              TextFormField(
                controller: description,
                maxLines: 3,
                decoration: const InputDecoration(labelText: 'Description'),
              ),
              const SizedBox(height: 10),
              DropdownButtonFormField<String>(
                initialValue: status,
                decoration: const InputDecoration(labelText: 'Availability'),
                items: const ['ACTIVE', 'COMING_SOON', 'RETIRED']
                    .map(
                      (value) => DropdownMenuItem(
                        value: value,
                        child: Text(value.replaceAll('_', ' ')),
                      ),
                    )
                    .toList(),
                onChanged: (value) => status = value!,
              ),
              const SizedBox(height: 10),
              DropdownButtonFormField<String>(
                key: const Key('service-availability'),
                isExpanded: true,
                initialValue: availability,
                decoration: const InputDecoration(labelText: 'Society access'),
                items: const [
                  DropdownMenuItem(
                    value: 'DEFAULT_FREE',
                    child: Text(
                      'Default for every society · free',
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  DropdownMenuItem(
                    value: 'OPTIONAL_FREE',
                    child: Text(
                      'Optional · free',
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  DropdownMenuItem(
                    value: 'PAID_PER_SPACE',
                    child: Text(
                      'Optional · paid per space',
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
                onChanged: (value) => setState(() {
                  availability = value!;
                  if (availability != 'PAID_PER_SPACE') {
                    pricePerSpace.text = '0';
                  }
                  if (availability == 'DEFAULT_FREE') status = 'ACTIVE';
                }),
              ),
              const SizedBox(height: 10),
              TextFormField(
                key: const Key('service-price-per-space'),
                controller: pricePerSpace,
                enabled: availability == 'PAID_PER_SPACE',
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                ),
                decoration: const InputDecoration(
                  labelText: 'Monthly price per society space',
                  prefixText: '₹ ',
                  helperText:
                      'Added for each selected space before the billing-cycle multiplier.',
                ),
                validator: (value) {
                  final amount = double.tryParse(value ?? '');
                  if (amount == null || amount < 0) return 'Enter a valid rate';
                  if (availability == 'PAID_PER_SPACE' && amount <= 0) {
                    return 'Paid services require a positive rate';
                  }
                  if (availability != 'PAID_PER_SPACE' && amount != 0) {
                    return 'Free services must have a zero rate';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 10),
              TextFormField(
                controller: sortOrder,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(labelText: 'Display order'),
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
        onPressed: () {
          if (!form.currentState!.validate()) return;
          Navigator.pop(context, {
            'code': code.text,
            'name': name.text,
            'description': description.text,
            'status': status,
            'sortOrder': int.tryParse(sortOrder.text) ?? 0,
            'availabilityType': availability,
            'pricePerSpace': double.tryParse(pricePerSpace.text) ?? 0,
          });
        },
        child: const Text('Save'),
      ),
    ],
  );
}

String _commercialLabel(Map<String, dynamic> service) =>
    switch (service['availability_type']?.toString()) {
      'DEFAULT_FREE' => 'Default for all societies · Free',
      'PAID_PER_SPACE' =>
        'Optional · ₹${service['price_per_space'] ?? 0} per space/month',
      _ => 'Optional · Free',
    };
