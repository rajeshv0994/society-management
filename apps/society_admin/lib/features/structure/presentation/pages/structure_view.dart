import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'package:society_admin_app/core/presentation/widgets/common.dart';
import '../bloc/structure_bloc.dart';

class StructureView extends StatefulWidget {
  const StructureView({super.key});

  @override
  State<StructureView> createState() => _StructureViewState();
}

class _StructureViewState extends State<StructureView> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  void _load() => context.read<StructureBloc>().loadStructure();

  Future<void> _run(Future<void> Function() action, String success) async {
    try {
      await action();
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(success), behavior: SnackBarBehavior.floating),
      );
      _load();
    } catch (error) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(error.toString()),
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  Future<void> _blockDialog([Map<String, dynamic>? block]) async {
    final name = TextEditingController(text: block?['name']?.toString());
    final key = GlobalKey<FormState>();
    final confirmed = await _dialog(
      title: block == null ? 'Add property group' : 'Rename property group',
      formKey: key,
      children: [_field(name, 'Property group name', required: true)],
      action: block == null ? 'Add group' : 'Save changes',
    );
    if (!confirmed) return;
    await _run(() async {
      await context.read<StructureBloc>().saveBlock(
        id: block?['id'],
        name: name.text.trim(),
      );
    }, block == null ? 'Property group added' : 'Property group updated');
  }

  Future<void> _floorDialog(
    Map<String, dynamic> block, [
    Map<String, dynamic>? floor,
  ]) async {
    final name = TextEditingController(text: floor?['name']?.toString());
    final number = TextEditingController(
      text: (floor?['section_order'] ?? floor?['floor_number'])?.toString(),
    );
    final key = GlobalKey<FormState>();
    final confirmed = await _dialog(
      title: floor == null
          ? 'Add section to ${block['name']}'
          : 'Edit ${floor['name']}',
      formKey: key,
      children: [
        _field(name, 'Section name', required: true),
        _field(number, 'Display order', required: true, number: true),
      ],
      action: floor == null ? 'Add section' : 'Save changes',
    );
    if (!confirmed) return;
    await _run(() async {
      await context.read<StructureBloc>().saveSection(
        blockId: block['id']!,
        sectionId: floor?['id'],
        name: name.text.trim(),
        order: int.parse(number.text),
      );
    }, floor == null ? 'Section added' : 'Section updated');
  }

  Future<void> _flatDialog([
    Map<String, dynamic>? floor,
    Map<String, dynamic>? flat,
  ]) async {
    final number = TextEditingController(
      text:
          (flat?['space_number'] ??
                  flat?['unit_number'] ??
                  flat?['flat_number'])
              ?.toString(),
    );
    final area = TextEditingController(text: flat?['area_sqft']?.toString());
    final key = GlobalKey<FormState>();
    final confirmed = await _dialog(
      title: flat == null
          ? floor == null
                ? 'Add space'
                : 'Add space to ${floor['name']}'
          : 'Edit space ${flat['space_number'] ?? flat['unit_number'] ?? flat['flat_number']}',
      formKey: key,
      children: [
        _field(number, 'Space number / name', required: true),
        _field(area, 'Area in sq. ft.', decimal: true),
      ],
      action: flat == null ? 'Add space' : 'Save changes',
    );
    if (!confirmed) return;
    await _run(() async {
      await context.read<StructureBloc>().saveSpace(
        sectionId: floor?['id'],
        spaceId: flat?['id'],
        number: number.text.trim(),
        areaSqft: area.text.trim().isEmpty
            ? null
            : double.parse(area.text.trim()),
      );
    }, flat == null ? 'Space added' : 'Space updated');
  }

  Widget _field(
    TextEditingController controller,
    String label, {
    bool required = false,
    bool number = false,
    bool decimal = false,
  }) => Padding(
    padding: const EdgeInsets.only(bottom: 12),
    child: TextFormField(
      controller: controller,
      keyboardType: number || decimal
          ? const TextInputType.numberWithOptions(decimal: true, signed: true)
          : null,
      decoration: InputDecoration(labelText: label),
      validator: (value) {
        if (required && (value == null || value.trim().isEmpty)) {
          return '$label is required';
        }
        if (number && int.tryParse(value ?? '') == null) {
          return 'Enter a valid whole number';
        }
        if (decimal &&
            value != null &&
            value.trim().isNotEmpty &&
            double.tryParse(value) == null) {
          return 'Enter a valid number';
        }
        return null;
      },
    ),
  );

  Future<bool> _dialog({
    required String title,
    required GlobalKey<FormState> formKey,
    required List<Widget> children,
    required String action,
  }) async =>
      await showDialog<bool>(
        context: context,
        builder: (dialogContext) => AlertDialog(
          title: Text(title),
          content: SizedBox(
            width: 440,
            child: Form(
              key: formKey,
              child: Column(mainAxisSize: MainAxisSize.min, children: children),
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext, false),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () {
                if (formKey.currentState!.validate()) {
                  Navigator.pop(dialogContext, true);
                }
              },
              child: Text(action),
            ),
          ],
        ),
      ) ??
      false;

  Future<void> _delete(String type, Map<String, dynamic> item) async {
    final label =
        item['name']?.toString() ??
        'Space ${(item['space_number'] ?? item['unit_number'] ?? item['flat_number']).toString()}';
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: Text('Delete $label?'),
        content: Text(
          type == 'block'
              ? 'A property group can only be deleted after all of its sections are removed.'
              : type == 'section'
              ? 'A section can only be deleted after all of its spaces are removed.'
              : 'Only a vacant, unassigned space can be deleted.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            style: FilledButton.styleFrom(
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
            onPressed: () => Navigator.pop(dialogContext, true),
            child: const Text('Delete'),
          ),
        ],
      ),
    );
    if (confirmed != true) return;
    await _run(() async {
      await context.read<StructureBloc>().deleteStructureItem(
        type,
        item['id']!,
      );
    }, '$label deleted');
  }

  @override
  Widget build(BuildContext context) => PageFrame(
    title: 'Property spaces',
    subtitle: 'Manage houses, apartments, shops, offices, levels, or rentals',
    action: Wrap(
      spacing: 8,
      children: [
        OutlinedButton.icon(
          onPressed: _blockDialog,
          icon: const Icon(Icons.create_new_folder_outlined),
          label: const Text('Add group'),
        ),
        FilledButton.icon(
          onPressed: _flatDialog,
          icon: const Icon(Icons.add_home_work_outlined),
          label: const Text('Add space'),
        ),
      ],
    ),
    child: BlocBuilder<StructureBloc, AdminResourceState>(
      builder: (context, state) {
        final snapshot = state.resource('structure');
        if (snapshot.status == ResourceStatus.initial ||
            snapshot.status == ResourceStatus.loading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.error != null) {
          return ErrorState(message: snapshot.error!, onRetry: _load);
        }
        final data =
            (snapshot.value as ResourceDocument?)?.data ??
            const <String, dynamic>{};
        final blocks = _maps(data['blocks']);
        final floors = _maps(data['sections'] ?? data['floors']);
        final flats = _maps(data['spaces'] ?? data['units'] ?? data['flats']);
        final occupied = flats
            .where((flat) => flat['occupancy_status'] != 'VACANT')
            .length;
        return Column(
          children: [
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                _StructureMetric(
                  label: 'Property groups',
                  value: '${blocks.length}',
                  icon: Icons.apartment,
                  color: SocietyColors.blue,
                ),
                _StructureMetric(
                  label: 'Sections',
                  value: '${floors.length}',
                  icon: Icons.layers_outlined,
                  color: const Color(0xFF6E42BD),
                ),
                _StructureMetric(
                  label: 'Total spaces',
                  value: '${flats.length}',
                  icon: Icons.home_work_outlined,
                  color: SocietyColors.success,
                ),
                _StructureMetric(
                  label: 'Occupied',
                  value: '$occupied',
                  icon: Icons.people_alt_outlined,
                  color: SocietyColors.warning,
                ),
              ],
            ),
            const SizedBox(height: 16),
            Expanded(
              child: blocks.isEmpty
                  ? _EmptyStructure(onAdd: _flatDialog)
                  : ListView.separated(
                      itemCount: blocks.length,
                      separatorBuilder: (_, _) => const SizedBox(height: 12),
                      itemBuilder: (_, index) {
                        final block = blocks[index];
                        final blockFloors = floors
                            .where((floor) => floor['block_id'] == block['id'])
                            .toList();
                        return _BlockCard(
                          block: block,
                          floors: blockFloors,
                          flats: flats,
                          onAddFloor: () => _floorDialog(block),
                          onEditBlock: () => _blockDialog(block),
                          onDeleteBlock: () => _delete('block', block),
                          onEditFloor: (floor) => _floorDialog(block, floor),
                          onDeleteFloor: (floor) => _delete('section', floor),
                          onAddFlat: (floor) => _flatDialog(floor),
                          onEditFlat: (floor, flat) => _flatDialog(floor, flat),
                          onDeleteFlat: (flat) => _delete('space', flat),
                        );
                      },
                    ),
            ),
          ],
        );
      },
    ),
  );

  List<Map<String, dynamic>> _maps(dynamic value) => (value as List? ?? [])
      .map((item) => Map<String, dynamic>.from(item))
      .toList();
}

class _StructureMetric extends StatelessWidget {
  const _StructureMetric({
    required this.label,
    required this.value,
    required this.icon,
    required this.color,
  });

  final String label;
  final String value;
  final IconData icon;
  final Color color;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: 190,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(15),
        child: Row(
          children: [
            CircleAvatar(
              backgroundColor: color.withValues(alpha: .12),
              foregroundColor: color,
              child: Icon(icon),
            ),
            const SizedBox(width: 11),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(value, style: Theme.of(context).textTheme.headlineSmall),
                  Text(
                    label,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(color: SocietyColors.textMuted),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    ),
  );
}

class _BlockCard extends StatelessWidget {
  const _BlockCard({
    required this.block,
    required this.floors,
    required this.flats,
    required this.onAddFloor,
    required this.onEditBlock,
    required this.onDeleteBlock,
    required this.onEditFloor,
    required this.onDeleteFloor,
    required this.onAddFlat,
    required this.onEditFlat,
    required this.onDeleteFlat,
  });

  final Map<String, dynamic> block;
  final List<Map<String, dynamic>> floors;
  final List<Map<String, dynamic>> flats;
  final VoidCallback onAddFloor;
  final VoidCallback onEditBlock;
  final VoidCallback onDeleteBlock;
  final void Function(Map<String, dynamic>) onEditFloor;
  final void Function(Map<String, dynamic>) onDeleteFloor;
  final void Function(Map<String, dynamic>) onAddFlat;
  final void Function(Map<String, dynamic>, Map<String, dynamic>) onEditFlat;
  final void Function(Map<String, dynamic>) onDeleteFlat;

  @override
  Widget build(BuildContext context) {
    final compact = MediaQuery.sizeOf(context).width < 760;
    return Card(
      clipBehavior: Clip.antiAlias,
      child: ExpansionTile(
        initiallyExpanded: true,
        leading: const CircleAvatar(child: Icon(Icons.apartment)),
        title: Text(
          block['name'].toString(),
          style: Theme.of(context).textTheme.titleLarge,
        ),
        subtitle: Text(
          '${block['section_count'] ?? block['floor_count'] ?? floors.length} sections  •  '
          '${block['space_count'] ?? block['unit_count'] ?? block['flat_count'] ?? _flatCount} spaces  •  '
          '${block['occupied_count'] ?? _occupiedCount} occupied',
        ),
        trailing: Wrap(
          crossAxisAlignment: WrapCrossAlignment.center,
          children: [
            if (compact)
              IconButton(
                tooltip: 'Add section',
                onPressed: onAddFloor,
                icon: const Icon(Icons.add),
              )
            else
              TextButton.icon(
                onPressed: onAddFloor,
                icon: const Icon(Icons.add),
                label: const Text('Section'),
              ),
            PopupMenuButton<String>(
              onSelected: (value) =>
                  value == 'edit' ? onEditBlock() : onDeleteBlock(),
              itemBuilder: (_) => const [
                PopupMenuItem(value: 'edit', child: Text('Rename group')),
                PopupMenuItem(value: 'delete', child: Text('Delete group')),
              ],
            ),
          ],
        ),
        children: [
          if (floors.isEmpty)
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 6, 20, 20),
              child: Align(
                alignment: Alignment.centerLeft,
                child: OutlinedButton.icon(
                  onPressed: onAddFloor,
                  icon: const Icon(Icons.add),
                  label: const Text('Add the first section'),
                ),
              ),
            ),
          for (final floor in floors)
            _FloorSection(
              floor: floor,
              flats: flats
                  .where(
                    (flat) =>
                        (flat['section_id'] ?? flat['floor_id']) == floor['id'],
                  )
                  .toList(),
              onAddFlat: () => onAddFlat(floor),
              onEdit: () => onEditFloor(floor),
              onDelete: () => onDeleteFloor(floor),
              onEditFlat: (flat) => onEditFlat(floor, flat),
              onDeleteFlat: onDeleteFlat,
            ),
        ],
      ),
    );
  }

  int get _flatCount => floors.fold(
    0,
    (total, floor) =>
        total +
        flats
            .where(
              (flat) => (flat['section_id'] ?? flat['floor_id']) == floor['id'],
            )
            .length,
  );

  int get _occupiedCount => floors.fold(
    0,
    (total, floor) =>
        total +
        flats
            .where(
              (flat) =>
                  (flat['section_id'] ?? flat['floor_id']) == floor['id'] &&
                  flat['occupancy_status'] != 'VACANT',
            )
            .length,
  );
}

class _FloorSection extends StatelessWidget {
  const _FloorSection({
    required this.floor,
    required this.flats,
    required this.onAddFlat,
    required this.onEdit,
    required this.onDelete,
    required this.onEditFlat,
    required this.onDeleteFlat,
  });

  final Map<String, dynamic> floor;
  final List<Map<String, dynamic>> flats;
  final VoidCallback onAddFlat;
  final VoidCallback onEdit;
  final VoidCallback onDelete;
  final void Function(Map<String, dynamic>) onEditFlat;
  final void Function(Map<String, dynamic>) onDeleteFlat;

  @override
  Widget build(BuildContext context) => Container(
    margin: const EdgeInsets.fromLTRB(16, 0, 16, 14),
    padding: const EdgeInsets.all(14),
    decoration: BoxDecoration(
      color: SocietyColors.canvas,
      border: Border.all(color: SocietyColors.border),
      borderRadius: BorderRadius.circular(14),
    ),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            const Icon(Icons.layers_outlined, size: 20),
            const SizedBox(width: 8),
            Expanded(
              child: Text(
                '${floor['name']} · ${flats.length} spaces',
                style: Theme.of(context).textTheme.titleMedium,
              ),
            ),
            IconButton(
              tooltip: 'Add space',
              onPressed: onAddFlat,
              icon: const Icon(Icons.add_home_work_outlined),
            ),
            PopupMenuButton<String>(
              onSelected: (value) => value == 'edit' ? onEdit() : onDelete(),
              itemBuilder: (_) => const [
                PopupMenuItem(value: 'edit', child: Text('Edit section')),
                PopupMenuItem(value: 'delete', child: Text('Delete section')),
              ],
            ),
          ],
        ),
        const SizedBox(height: 10),
        if (flats.isEmpty)
          OutlinedButton.icon(
            onPressed: onAddFlat,
            icon: const Icon(Icons.add),
            label: const Text('Add the first space'),
          )
        else
          Wrap(
            spacing: 9,
            runSpacing: 9,
            children: [
              for (final flat in flats)
                _FlatCard(
                  flat: flat,
                  onEdit: () => onEditFlat(flat),
                  onDelete: () => onDeleteFlat(flat),
                ),
            ],
          ),
      ],
    ),
  );
}

class _FlatCard extends StatelessWidget {
  const _FlatCard({
    required this.flat,
    required this.onEdit,
    required this.onDelete,
  });

  final Map<String, dynamic> flat;
  final VoidCallback onEdit;
  final VoidCallback onDelete;

  @override
  Widget build(BuildContext context) {
    final occupied = flat['occupancy_status'] != 'VACANT';
    final color = occupied ? SocietyColors.warning : SocietyColors.success;
    return Container(
      width: 178,
      padding: const EdgeInsets.fromLTRB(12, 10, 4, 10),
      decoration: BoxDecoration(
        color: color.withValues(alpha: .07),
        border: Border.all(color: color.withValues(alpha: .3)),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        children: [
          Icon(
            occupied ? Icons.home : Icons.home_outlined,
            color: color,
            size: 21,
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  (flat['space_number'] ??
                          flat['unit_number'] ??
                          flat['flat_number'])
                      .toString(),
                  style: const TextStyle(fontWeight: FontWeight.w700),
                ),
                Text(
                  flat['area_sqft'] == null
                      ? flat['occupancy_status'].toString().replaceAll('_', ' ')
                      : '${flat['area_sqft']} sq. ft.',
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(fontSize: 11, color: color),
                ),
              ],
            ),
          ),
          PopupMenuButton<String>(
            iconSize: 19,
            padding: EdgeInsets.zero,
            onSelected: (value) => value == 'edit' ? onEdit() : onDelete(),
            itemBuilder: (_) => const [
              PopupMenuItem(value: 'edit', child: Text('Edit space')),
              PopupMenuItem(value: 'delete', child: Text('Delete space')),
            ],
          ),
        ],
      ),
    );
  }
}

class _EmptyStructure extends StatelessWidget {
  const _EmptyStructure({required this.onAdd});

  final VoidCallback onAdd;

  @override
  Widget build(BuildContext context) => Center(
    child: Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        const Icon(
          Icons.apartment_outlined,
          size: 54,
          color: SocietyColors.textMuted,
        ),
        const SizedBox(height: 12),
        Text(
          'Start with your first space',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 6),
        const Text(
          'Use any name such as House 12, First level, Shop 4, or Office A.',
          style: TextStyle(color: SocietyColors.textMuted),
        ),
        const SizedBox(height: 16),
        FilledButton.icon(
          onPressed: onAdd,
          icon: const Icon(Icons.add),
          label: const Text('Add space'),
        ),
      ],
    ),
  );
}
