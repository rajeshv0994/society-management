import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';
import 'package:super_admin_app/core/presentation/widgets/common_widgets.dart';
import '../bloc/storage_bloc.dart';

class StorageView extends StatefulWidget {
  const StorageView({super.key});
  @override
  State<StorageView> createState() => _StorageViewState();
}

class _StorageViewState extends State<StorageView> {
  @override
  void initState() {
    super.initState();
    load();
  }

  void load() => context.read<StorageBloc>().add(const StorageLoaded());

  Future<void> edit(Map<String, dynamic> policy) async {
    final free = TextEditingController(
      text: '${policy['default_free_gb'] ?? 5}',
    );
    final block = TextEditingController(
      text: '${policy['purchase_block_gb'] ?? 5}',
    );
    final price = TextEditingController(
      text: '${policy['price_per_block_monthly'] ?? 0}',
    );
    final accepted = await showDialog<bool>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: const Text('Storage pricing policy'),
        content: SizedBox(
          width: 440,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: free,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'Free storage (GB)',
                ),
              ),
              const SizedBox(height: 10),
              TextField(
                controller: block,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'Purchase block (GB)',
                ),
              ),
              const SizedBox(height: 10),
              TextField(
                controller: price,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'Price per block / month (₹)',
                ),
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
            child: const Text('Save policy'),
          ),
        ],
      ),
    );
    if (accepted != true) return;
    if (!mounted) return;
    context.read<StorageBloc>().add(
      StoragePolicySaved({
        'defaultFreeGb': int.parse(free.text),
        'purchaseBlockGb': int.parse(block.text),
        'pricePerBlockMonthly': double.parse(price.text),
      }),
    );
  }

  @override
  Widget build(BuildContext context) => PageFrame(
    title: 'Storage management',
    subtitle: 'Set the free allowance and paid monthly storage blocks',
    onRefresh: load,
    child: BlocConsumer<StorageBloc, StorageState>(
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
        if (state.error != null && state.data == null) {
          return FailureState(state.error!, retry: load);
        }
        final policy = state.data!.policy.data;
        final societies = state.data!.societies
            .map((item) => item.data)
            .toList();
        return ListView(
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Wrap(
                  spacing: 28,
                  runSpacing: 14,
                  crossAxisAlignment: WrapCrossAlignment.center,
                  children: [
                    _metric(
                      'Free allowance',
                      '${policy['default_free_gb']} GB',
                    ),
                    _metric(
                      'Purchase block',
                      '${policy['purchase_block_gb']} GB',
                    ),
                    _metric(
                      'Monthly block price',
                      '₹${policy['price_per_block_monthly']}',
                    ),
                    FilledButton.icon(
                      onPressed: () => edit(policy),
                      icon: const Icon(Icons.edit),
                      label: const Text('Edit policy'),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 14),
            Text(
              'Society usage',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 8),
            for (final society in societies)
              Card(
                child: ListTile(
                  leading: const CircleAvatar(child: Icon(Icons.apartment)),
                  title: Text(society['society_name'].toString()),
                  subtitle: Text(
                    '${_size(society['used_bytes'])} used • '
                    '${_size(society['purchased_bytes'])} purchased',
                  ),
                  trailing: Text('₹${society['monthly_addon_amount']}/month'),
                ),
              ),
          ],
        );
      },
    ),
  );

  Widget _metric(String label, String value) => SizedBox(
    width: 170,
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(color: SocietyColors.textMuted)),
        Text(
          value,
          style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w700),
        ),
      ],
    ),
  );

  static String _size(Object? raw) {
    final bytes = (raw as num?)?.toDouble() ?? 0;
    if (bytes >= 1073741824) {
      return '${(bytes / 1073741824).toStringAsFixed(2)} GB';
    }
    if (bytes >= 1048576) return '${(bytes / 1048576).toStringAsFixed(1)} MB';
    if (bytes >= 1024) return '${(bytes / 1024).toStringAsFixed(1)} KB';
    return '${bytes.toInt()} B';
  }
}
