// ignore_for_file: use_build_context_synchronously

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'package:society_admin_app/core/presentation/widgets/common.dart';
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
    WidgetsBinding.instance.addPostFrameCallback((_) => load());
  }

  void load() => context.read<StorageBloc>().loadStorage();

  Future<void> purchase(Map<String, dynamic> policy) async {
    int blocks = 1;
    final accepted = await showDialog<bool>(
      context: context,
      builder: (dialog) => StatefulBuilder(
        builder: (context, setDialog) => AlertDialog(
          title: const Text('Purchase more storage'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                '$blocks × ${policy['purchase_block_gb']} GB = '
                '${blocks * (policy['purchase_block_gb'] as num).toInt()} GB',
                style: Theme.of(context).textTheme.titleMedium,
              ),
              const SizedBox(height: 8),
              Text(
                '₹${blocks * (policy['price_per_block_monthly'] as num).toDouble()} per month. '
                'This add-on will be included in platform billing.',
              ),
              Slider(
                value: blocks.toDouble(),
                min: 1,
                max: 10,
                divisions: 9,
                label: '$blocks blocks',
                onChanged: (value) => setDialog(() => blocks = value.round()),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialog, false),
              child: const Text('Cancel'),
            ),
            FilledButton(
              key: const ValueKey('confirm-storage-purchase'),
              onPressed: () => Navigator.pop(dialog, true),
              child: const Text('Confirm purchase'),
            ),
          ],
        ),
      ),
    );
    if (accepted != true) return;
    await context.read<StorageBloc>().purchaseStorage(blocks);
    load();
  }

  Future<void> remove(Map<String, dynamic> file) async {
    final accepted = await showDialog<bool>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: const Text('Delete stored file?'),
        content: Text(
          '${file['name'] ?? 'This file'} and its related record will be permanently removed. '
          'This cannot be undone.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialog, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialog, true),
            child: const Text('Delete and free space'),
          ),
        ],
      ),
    );
    if (accepted != true) return;
    await context.read<StorageBloc>().deleteFile(file['id']!);
    load();
  }

  @override
  Widget build(BuildContext context) => PageFrame(
    title: 'Storage',
    subtitle: 'Monitor usage, clean files and purchase additional capacity',
    action: IconButton(onPressed: load, icon: const Icon(Icons.refresh)),
    child: BlocBuilder<StorageBloc, AdminResourceState>(
      builder: (context, state) {
        final snapshot = state.resource('storage');
        if (snapshot.status == ResourceStatus.initial ||
            snapshot.status == ResourceStatus.loading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.error != null) {
          return ErrorState(message: snapshot.error!, onRetry: load);
        }
        final data = (snapshot.value as ResourceDocument).data;
        final policy = Map<String, dynamic>.from(data['policy'] as Map);
        final files = (data['files'] as List? ?? const [])
            .map((item) => Map<String, dynamic>.from(item as Map))
            .toList();
        final used = (data['used_bytes'] as num).toDouble();
        final total = (data['total_bytes'] as num).toDouble();
        final percent = (data['usage_percent'] as num).toDouble();
        return ListView(
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            '${_size(used)} of ${_size(total)} used',
                            style: Theme.of(context).textTheme.titleLarge,
                          ),
                        ),
                        FilledButton.icon(
                          key: const ValueKey('purchase-storage'),
                          onPressed: () => purchase(policy),
                          icon: const Icon(Icons.add),
                          label: const Text('Buy storage'),
                        ),
                      ],
                    ),
                    const SizedBox(height: 14),
                    LinearProgressIndicator(value: (percent / 100).clamp(0, 1)),
                    const SizedBox(height: 10),
                    Text(
                      '${_size(data['free_bytes'])} free • '
                      '${_size(data['purchased_bytes'])} purchased • '
                      '${_size(data['available_bytes'])} available',
                    ),
                    Text(
                      'Current add-on: ₹${data['monthly_addon_amount']}/month',
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 14),
            Text('Stored files', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 8),
            if (files.isEmpty)
              const SizedBox(height: 140, child: Empty('No metered files yet.'))
            else
              for (final file in files)
                Card(
                  child: ListTile(
                    leading: Icon(_icon(file['category']?.toString())),
                    title: Text(file['name']?.toString() ?? 'Stored file'),
                    subtitle: Text(
                      '${_label(file['category'])} • ${_size(file['size_bytes'])}',
                    ),
                    trailing: file['cleanup_eligible'] == true
                        ? IconButton(
                            tooltip: 'Delete and free space',
                            onPressed: () => remove(file),
                            icon: const Icon(Icons.delete_outline),
                          )
                        : const Tooltip(
                            message:
                                'Legacy file: remove it from its original module',
                            child: Icon(Icons.lock_outline),
                          ),
                  ),
                ),
          ],
        );
      },
    ),
  );

  static String _size(Object? raw) {
    final bytes = raw is num ? raw.toDouble() : double.tryParse('$raw') ?? 0;
    if (bytes >= 1073741824) {
      return '${(bytes / 1073741824).toStringAsFixed(2)} GB';
    }
    if (bytes >= 1048576) return '${(bytes / 1048576).toStringAsFixed(1)} MB';
    if (bytes >= 1024) return '${(bytes / 1024).toStringAsFixed(1)} KB';
    return '${bytes.toInt()} B';
  }

  static String _label(Object? value) =>
      (value?.toString() ?? 'FILE').replaceAll('_', ' ').toLowerCase();
  static IconData _icon(String? category) => switch (category) {
    'IMAGES' => Icons.image_outlined,
    'VIDEOS' => Icons.video_file_outlined,
    _ => Icons.description_outlined,
  };
}
