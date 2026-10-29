import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'package:society_admin_app/core/presentation/widgets/common.dart';
import '../bloc/operations_bloc.dart';

class OperationsView extends StatefulWidget {
  const OperationsView({super.key});
  @override
  State<OperationsView> createState() => _OperationsState();
}

class _OperationsState extends State<OperationsView> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => load());
  }

  OperationsBloc get bloc => context.read<OperationsBloc>();
  void load() => bloc.loadVisitors();

  Future<void> add() async {
    try {
      await _addVisitor();
      load();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(e.toString())));
      }
    }
  }

  Future<void> _addVisitor() async {
    final hosts = await bloc.loadVisitorHosts();
    final targets = await bloc.loadVisitorTargets();
    final name = TextEditingController();
    final mobile = TextEditingController();
    final purpose = TextEditingController();
    final vehicle = TextEditingController();
    final accompanying = TextEditingController(text: '0');
    final otherTarget = TextEditingController();
    var targetType = 'RESIDENT';
    Map<String, dynamic>? target = hosts.firstOrNull;
    final photos = <VisitorPhotoCapture>[];
    if (!mounted) return;
    final submit = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: const Text('Register visitor'),
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
                    onChanged: (value) => setDialogState(() {
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
                            leading: Icon(Icons.warning_amber),
                            title: Text('No resident is assigned to a space'),
                          )
                        : DropdownButtonFormField<String>(
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
                            onChanged: (id) => setDialogState(
                              () => target = hosts.firstWhere(
                                (item) =>
                                    '${item['resident_id']}|${item['space_id']}' ==
                                    id,
                              ),
                            ),
                          ),
                  if (targetType == 'STAFF')
                    DropdownButtonFormField<String>(
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
                      onChanged: (id) => setDialogState(
                        () => target = targets.firstWhere(
                          (item) => item['user_id'].toString() == id,
                        ),
                      ),
                    ),
                  if (targetType == 'OTHER')
                    Padding(
                      padding: const EdgeInsets.only(bottom: 10),
                      child: TextField(
                        controller: otherTarget,
                        decoration: const InputDecoration(
                          labelText: 'Person, office, or destination',
                        ),
                      ),
                    ),
                  const SizedBox(height: 10),
                  for (final entry in [
                    (name, 'Visitor name', TextInputType.name),
                    (mobile, 'Mobile', TextInputType.phone),
                    (purpose, 'Purpose', TextInputType.text),
                    (vehicle, 'Vehicle number (optional)', TextInputType.text),
                    (
                      accompanying,
                      'Members accompanying visitor',
                      TextInputType.number,
                    ),
                  ])
                    Padding(
                      padding: const EdgeInsets.only(bottom: 10),
                      child: TextField(
                        controller: entry.$1,
                        keyboardType: entry.$3,
                        decoration: InputDecoration(labelText: entry.$2),
                      ),
                    ),
                  FilledButton.tonalIcon(
                    onPressed: () async {
                      final captured = await captureVisitorPhoto(context);
                      if (captured != null) {
                        setDialogState(() => photos.add(captured));
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
                        onPressed: () =>
                            setDialogState(() => photos.remove(photo)),
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
    if (submit != true) return;
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
    final count = int.tryParse(accompanying.text.trim()) ?? -1;
    if (count < 0) throw Exception('Accompanying members must be 0 or more');
    final created = await bloc.createVisitor({
      'targetType': targetType,
      if (targetType == 'RESIDENT') 'spaceId': target!['space_id'],
      if (targetType == 'RESIDENT') 'hostResidentId': target!['resident_id'],
      if (targetType == 'STAFF') 'targetUserId': target!['user_id'],
      if (targetType == 'OTHER') 'targetName': otherTarget.text.trim(),
      'visitorName': name.text.trim(),
      'mobile': mobile.text.trim(),
      'purpose': purpose.text.trim(),
      'vehicleNumber': vehicle.text.trim(),
      'expectedAt': DateTime.now().toUtc().toIso8601String(),
      'accompanyingMembers': count,
    });
    for (final photo in photos) {
      await bloc.uploadVisitorPhoto(created['id'], photo.bytes, photo.name);
    }
  }

  Future<void> _phoneApprove(Map<String, dynamic> visitor) async {
    final note = TextEditingController(
      text: 'Approved after phone verification',
    );
    final approved = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Approve after phone call?'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Call ${visitor['target_name'] ?? visitor['host_resident_name'] ?? 'the visit contact'} at ${visitor['target_mobile'] ?? 'their registered phone number'} first.',
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
    await bloc.approveByPhone(visitor['id'], note.text.trim());
    load();
  }

  String title(Map r) =>
      (r['full_name'] ??
              r['title'] ??
              r['visitor_name'] ??
              r['name'] ??
              r['invoice_number'] ??
              r['space_number'] ??
              r['unit_number'] ??
              r['flat_number'] ??
              'Record')
          .toString();
  @override
  Widget build(c) => PageFrame(
    title: 'Visitors',
    subtitle: 'Register, approve, check in, and check out society visitors',
    action: FilledButton.icon(
      onPressed: add,
      icon: const Icon(Icons.add),
      label: const Text('Add'),
    ),
    child: BlocBuilder<OperationsBloc, AdminResourceState>(
      builder: (c, state) {
        final s = state.resource('visitors');
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
        if (rows.isEmpty) {
          return const Empty('No visitors found.');
        }
        return ListView.separated(
          itemCount: rows.length,
          separatorBuilder: (_, _) => const SizedBox(height: 8),
          itemBuilder: (c, i) {
            final r = rows[i];
            Widget? action;
            if (r['status'] == 'APPROVED') {
              action = FilledButton.tonal(
                onPressed: () async {
                  await bloc.updateVisitorStatus(r['id'], 'CHECKED_IN');
                  load();
                },
                child: const Text('Check in'),
              );
            }
            if (r['status'] == 'CHECKED_IN') {
              action = FilledButton.tonal(
                onPressed: () async {
                  await bloc.updateVisitorStatus(r['id'], 'CHECKED_OUT');
                  load();
                },
                child: const Text('Check out'),
              );
            }
            if ([
              'PENDING_RESIDENT_APPROVAL',
              'PENDING_STAFF_APPROVAL',
            ].contains(r['status'])) {
              action = FilledButton.tonalIcon(
                onPressed: () => _phoneApprove(r),
                icon: const Icon(Icons.phone),
                label: const Text('Phone approve'),
              );
            }
            return Card(
              child: ListTile(
                title: Text(title(r)),
                subtitle: Text(
                  [
                    r['email'],
                    r['description'],
                    r['purpose'],
                    r['host_resident_name'],
                    r['target_name'],
                    '${r['total_people'] ?? 1} people',
                    r['body'],
                    r['space_number'] ?? r['unit_number'] ?? r['flat_number'],
                    r['assigned_to_name'],
                  ].where((v) => v != null).join(' • '),
                ),
                trailing:
                    action ??
                    (r['status'] == null
                        ? null
                        : StatusChip(r['status'].toString())),
              ),
            );
          },
        );
      },
    ),
  );
}
