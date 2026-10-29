import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';

import 'features/visitors/data/api_staff_visitor_repository.dart';
import 'features/visitors/domain/visitor_models.dart';
import 'features/visitors/presentation/bloc/staff_visitors_cubit.dart';

Widget buildStaffVisitorDesk(ApiGateway client) => BlocProvider(
  create: (_) => StaffVisitorsCubit(ApiStaffVisitorRepository(client))..load(),
  child: const VisitorDeskPage(),
);

class VisitorDeskPage extends StatefulWidget {
  const VisitorDeskPage({super.key});

  @override
  State<VisitorDeskPage> createState() => _VisitorDeskPageState();
}

class _VisitorDeskPageState extends State<VisitorDeskPage> {
  Future<void> _status(StaffVisitorRecord visitor, String status) async {
    try {
      await context.read<StaffVisitorsCubit>().updateStatus(visitor, status);
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _captureArrivalPhotos(StaffVisitorRecord visitor) async {
    final visitorsCubit = context.read<StaffVisitorsCubit>();
    final photos = <VisitorPhotoCapture>[];
    final save = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => StatefulBuilder(
        builder: (context, update) => AlertDialog(
          title: Text('Arrival photos • ${visitor['visitor_name']}'),
          content: SizedBox(
            width: 460,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Text(
                  'Capture one or more current photos before allowing check-in.',
                ),
                const SizedBox(height: 14),
                FilledButton.tonalIcon(
                  onPressed: () async {
                    final captured = await captureVisitorPhoto(context);
                    if (captured != null) {
                      update(() => photos.add(captured));
                    }
                  },
                  icon: const Icon(Icons.photo_camera),
                  label: Text(
                    photos.isEmpty
                        ? 'Capture visitor photo'
                        : 'Capture another (${photos.length} captured)',
                  ),
                ),
                for (final photo in photos)
                  ListTile(
                    dense: true,
                    leading: const Icon(Icons.photo),
                    title: Text(photo.name),
                    trailing: IconButton(
                      onPressed: () => update(() => photos.remove(photo)),
                      icon: const Icon(Icons.close),
                    ),
                  ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext, false),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: photos.isEmpty
                  ? null
                  : () => Navigator.pop(dialogContext, true),
              child: const Text('Upload photos'),
            ),
          ],
        ),
      ),
    );
    if (save != true) return;
    try {
      for (final photo in photos) {
        await visitorsCubit.uploadPhoto(visitor, photo.bytes, photo.name);
      }
      _message(
        '${photos.length} arrival photo(s) saved. Visitor can check in.',
      );
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _add() async {
    final visitorsCubit = context.read<StaffVisitorsCubit>();
    try {
      final hosts = await visitorsCubit.loadHosts();
      final targets = await visitorsCubit.loadTargets();
      final name = TextEditingController();
      final mobile = TextEditingController();
      final purpose = TextEditingController();
      final vehicle = TextEditingController();
      final group = TextEditingController(text: '0');
      final otherTarget = TextEditingController();
      var targetType = 'RESIDENT';
      VisitorTarget? target = hosts.firstOrNull;
      final photos = <VisitorPhotoCapture>[];
      if (!mounted) return;
      final ok = await showDialog<bool>(
        context: context,
        builder: (dialogContext) => StatefulBuilder(
          builder: (context, update) => AlertDialog(
            title: const Text('New gate visitor'),
            content: SizedBox(
              width: 480,
              child: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    DropdownButtonFormField<String>(
                      initialValue: targetType,
                      decoration: const InputDecoration(
                        labelText: 'Visiting whom?',
                      ),
                      items: const [
                        DropdownMenuItem(
                          value: 'RESIDENT',
                          child: Text('Resident'),
                        ),
                        DropdownMenuItem(
                          value: 'STAFF',
                          child: Text('Society staff'),
                        ),
                        DropdownMenuItem(
                          value: 'OTHER',
                          child: Text('Other / office'),
                        ),
                      ],
                      onChanged: (value) => update(() {
                        targetType = value!;
                        final choices = targetType == 'RESIDENT'
                            ? hosts
                            : targets
                                  .where((item) => item['role'] == targetType)
                                  .toList();
                        target = choices.firstOrNull;
                      }),
                    ),
                    const SizedBox(height: 10),
                    if (targetType == 'RESIDENT')
                      hosts.isEmpty
                          ? const ListTile(
                              title: Text('No resident is assigned to a space'),
                            )
                          : DropdownButtonFormField<String>(
                              key: ValueKey(targetType),
                              initialValue:
                                  '${target?['resident_id']}|${target?['space_id']}',
                              decoration: const InputDecoration(
                                labelText: 'Resident and space',
                              ),
                              items: hosts
                                  .map(
                                    (item) => DropdownMenuItem(
                                      value:
                                          '${item['resident_id']}|${item['space_id']}',
                                      child: Text(
                                        '${item['full_name']} • ${item['block'] ?? ''} ${item['space_number']} • ${item['mobile'] ?? 'No phone'}',
                                      ),
                                    ),
                                  )
                                  .toList(),
                              onChanged: (id) => update(
                                () => target = hosts.firstWhere(
                                  (item) =>
                                      '${item['resident_id']}|${item['space_id']}' ==
                                      id,
                                ),
                              ),
                            ),
                    if (targetType == 'STAFF')
                      DropdownButtonFormField<String>(
                        key: ValueKey(targetType),
                        initialValue: target?['user_id']?.toString(),
                        decoration: const InputDecoration(
                          labelText: 'Society staff',
                        ),
                        items: targets
                            .where((item) => item['role'] == targetType)
                            .map(
                              (item) => DropdownMenuItem(
                                value: item['user_id'].toString(),
                                child: Text(
                                  '${item['full_name']} • ${item['mobile'] ?? 'No phone'}',
                                ),
                              ),
                            )
                            .toList(),
                        onChanged: (id) => update(
                          () => target = targets.firstWhere(
                            (item) => item['user_id'].toString() == id,
                          ),
                        ),
                      ),
                    if (targetType == 'OTHER')
                      _field(otherTarget, 'Person, office, or destination'),
                    const SizedBox(height: 10),
                    _field(name, 'Visitor name'),
                    _field(mobile, 'Mobile', TextInputType.phone),
                    _field(purpose, 'Purpose'),
                    _field(vehicle, 'Vehicle number (optional)'),
                    _field(
                      group,
                      'Members accompanying visitor',
                      TextInputType.number,
                    ),
                    FilledButton.tonalIcon(
                      onPressed: () async {
                        final captured = await captureVisitorPhoto(context);
                        if (captured != null) {
                          update(() => photos.add(captured));
                        }
                      },
                      icon: const Icon(Icons.photo_camera),
                      label: Text(
                        photos.isEmpty
                            ? 'Capture visitor photo'
                            : 'Capture another photo (${photos.length} captured)',
                      ),
                    ),
                    for (final photo in photos)
                      ListTile(
                        dense: true,
                        leading: const Icon(Icons.photo),
                        title: Text(photo.name),
                        trailing: IconButton(
                          icon: const Icon(Icons.close),
                          onPressed: () => update(() => photos.remove(photo)),
                        ),
                      ),
                  ],
                ),
              ),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(dialogContext, false),
                child: const Text('Cancel'),
              ),
              FilledButton(
                onPressed: () => Navigator.pop(dialogContext, true),
                child: const Text('Send for approval'),
              ),
            ],
          ),
        ),
      );
      if (ok != true) return;
      final count = int.tryParse(group.text.trim()) ?? -1;
      if (name.text.trim().isEmpty || purpose.text.trim().isEmpty) {
        throw Exception('Visitor name and purpose are required');
      }
      if (targetType != 'OTHER' && target == null) {
        throw Exception('Select who the visitor is meeting');
      }
      if (targetType == 'OTHER' && otherTarget.text.trim().isEmpty) {
        throw Exception('Enter the person or destination being visited');
      }
      if (photos.isEmpty) throw Exception('Add at least one visitor photo');
      if (count < 0) throw Exception('Accompanying members must be 0 or more');
      final created = await visitorsCubit.create(
        StaffVisitorRegistration(
          targetType: targetType,
          spaceId: targetType == 'RESIDENT'
              ? target!['space_id']?.toString()
              : null,
          hostResidentId: targetType == 'RESIDENT'
              ? target!['resident_id']?.toString()
              : null,
          targetUserId: targetType == 'STAFF'
              ? target!['user_id']?.toString()
              : null,
          targetName: targetType == 'OTHER' ? otherTarget.text.trim() : null,
          visitorName: name.text.trim(),
          mobile: mobile.text.trim(),
          purpose: purpose.text.trim(),
          vehicleNumber: vehicle.text.trim(),
          expectedAt: DateTime.now(),
          accompanyingMembers: count,
        ),
      );
      for (final photo in photos) {
        await visitorsCubit.uploadPhoto(created, photo.bytes, photo.name);
      }
      _message('Visitor registered and sent for approval');
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _phoneApprove(StaffVisitorRecord visitor) async {
    final visitorsCubit = context.read<StaffVisitorsCubit>();
    final note = TextEditingController(
      text: 'Approved after phone verification',
    );
    final approved = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Phone verification'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Call ${visitor['target_name'] ?? visitor['host_resident_name'] ?? 'the visit contact'} at ${visitor['target_mobile'] ?? 'their registered phone number'}.',
            ),
            const SizedBox(height: 12),
            TextField(
              controller: note,
              decoration: const InputDecoration(labelText: 'Verification note'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialogContext, true),
            child: const Text('Phone verified — approve'),
          ),
        ],
      ),
    );
    if (approved != true) return;
    try {
      await visitorsCubit.recordPhoneApproval(visitor, note.text.trim());
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _staffDecision(
    StaffVisitorRecord visitor,
    String decision,
  ) async {
    try {
      await context.read<StaffVisitorsCubit>().recordDecision(
        visitor,
        decision,
      );
    } catch (error) {
      _message(error.toString());
    }
  }

  Widget _field(
    TextEditingController controller,
    String label, [
    TextInputType keyboard = TextInputType.text,
  ]) => Padding(
    padding: const EdgeInsets.only(bottom: 10),
    child: TextField(
      controller: controller,
      keyboardType: keyboard,
      decoration: InputDecoration(labelText: label),
    ),
  );

  void _message(String text) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
    }
  }

  @override
  Widget build(
    BuildContext context,
  ) => BlocBuilder<StaffVisitorsCubit, StaffVisitorsState>(
    builder: (context, state) {
      if (state.data == null && state.status != StaffVisitorsStatus.failure) {
        return const Center(child: CircularProgressIndicator());
      }
      if (state.data == null) {
        return Center(child: Text(state.error.toString()));
      }
      final data = state.data!;
      final canManageGate = data.canManage;
      final currentStaffId = data.currentStaffId;
      final rows = data.visitors;
      return Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Visitor desk',
                        style: Theme.of(context).textTheme.headlineMedium,
                      ),
                      Text(
                        canManageGate
                            ? 'Manage gate entry using resident/staff approval or phone verification.'
                            : 'Approve or deny visitors who are coming to meet you.',
                      ),
                    ],
                  ),
                ),
                if (canManageGate)
                  FilledButton.icon(
                    onPressed: _add,
                    icon: const Icon(Icons.add_a_photo),
                    label: const Text('Register visitor'),
                  ),
              ],
            ),
            const SizedBox(height: 20),
            Expanded(
              child: rows.isEmpty
                  ? const Center(child: Text('No visitors registered.'))
                  : ListView.separated(
                      itemCount: rows.length,
                      separatorBuilder: (_, _) => const SizedBox(height: 8),
                      itemBuilder: (context, index) {
                        final row = rows[index];
                        final status = row['status'].toString();
                        final awaitingThisStaff =
                            status == 'PENDING_STAFF_APPROVAL' &&
                            row['target_user_id'].toString() == currentStaffId;
                        return Card(
                          child: ListTile(
                            leading: Icon(
                              row['photo_url'] == null
                                  ? Icons.person
                                  : Icons.photo,
                            ),
                            title: Text(
                              '${row['visitor_name']} • ${row['total_people'] ?? 1} people',
                            ),
                            subtitle: Text(
                              '${row['target_name'] ?? row['host_resident_name'] ?? ''} • ${row['block'] ?? ''} ${row['space_number'] ?? ''}\n${row['purpose'] ?? ''} • ${row['photo_count'] ?? 0} photos • ${status.replaceAll('_', ' ')}',
                            ),
                            isThreeLine: true,
                            trailing: awaitingThisStaff
                                ? PopupMenuButton<String>(
                                    tooltip: 'Approve or deny visitor',
                                    icon: const Icon(Icons.fact_check_outlined),
                                    onSelected: (decision) =>
                                        _staffDecision(row, decision),
                                    itemBuilder: (_) => const [
                                      PopupMenuItem(
                                        value: 'APPROVE',
                                        child: ListTile(
                                          leading: Icon(Icons.check),
                                          title: Text('Approve visitor'),
                                        ),
                                      ),
                                      PopupMenuItem(
                                        value: 'DENY',
                                        child: ListTile(
                                          leading: Icon(Icons.close),
                                          title: Text('Deny visitor'),
                                        ),
                                      ),
                                    ],
                                  )
                                : canManageGate &&
                                      status == 'APPROVED' &&
                                      (row['photo_count'] as num? ?? 0) == 0
                                ? FilledButton.tonalIcon(
                                    onPressed: () => _captureArrivalPhotos(row),
                                    icon: const Icon(Icons.photo_camera),
                                    label: const Text('Capture photo'),
                                  )
                                : canManageGate && status == 'APPROVED'
                                ? FilledButton.tonal(
                                    onPressed: () => _status(row, 'CHECKED_IN'),
                                    child: const Text('Check in'),
                                  )
                                : canManageGate && status == 'CHECKED_IN'
                                ? FilledButton.tonal(
                                    onPressed: () =>
                                        _status(row, 'CHECKED_OUT'),
                                    child: const Text('Check out'),
                                  )
                                : canManageGate &&
                                      [
                                        'PENDING_RESIDENT_APPROVAL',
                                        'PENDING_STAFF_APPROVAL',
                                      ].contains(status)
                                ? FilledButton.tonalIcon(
                                    onPressed: () => _phoneApprove(row),
                                    icon: const Icon(Icons.phone),
                                    label: const Text('Phone approve'),
                                  )
                                : Chip(
                                    label: Text(status.replaceAll('_', ' ')),
                                  ),
                          ),
                        );
                      },
                    ),
            ),
          ],
        ),
      );
    },
  );
}
