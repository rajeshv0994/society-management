import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';

import 'features/visitors/data/api_member_visitor_repository.dart';
import 'features/visitors/domain/visitor_models.dart';
import 'features/visitors/presentation/bloc/member_visitors_cubit.dart';

Widget buildVisitorApprovalPage(ApiGateway client) => BlocProvider(
  create: (_) =>
      MemberVisitorsCubit(ApiMemberVisitorRepository(client))..load(),
  child: const VisitorApprovalPage(),
);

class VisitorApprovalPage extends StatefulWidget {
  const VisitorApprovalPage({super.key});

  @override
  State<VisitorApprovalPage> createState() => _VisitorApprovalPageState();
}

class _VisitorApprovalPageState extends State<VisitorApprovalPage> {
  String _filter = 'ALL';
  String _query = '';

  Future<void> _load() => context.read<MemberVisitorsCubit>().load();

  Future<void> _register() async {
    final created = await showDialog<bool>(
      context: context,
      builder: (_) => _RegisterVisitorDialog(
        spaces: context.read<MemberVisitorsCubit>().state.data!.spaces,
        onRegister: context.read<MemberVisitorsCubit>().register,
      ),
    );
    if (created == true) {
      _message(
        'Visitor pre-approved. Gate staff will capture photos on arrival.',
      );
    }
  }

  Future<void> _decide(VisitorRecord visitor, String decision) async {
    final visitorsCubit = context.read<MemberVisitorsCubit>();
    final approving = decision == 'APPROVE';
    if (approving && (visitor['photo_count'] as num? ?? 0) == 0) {
      _message('Gate staff must capture a visitor photo before approval.');
      return;
    }
    final note = TextEditingController();
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: Text(approving ? 'Approve visitor?' : 'Deny visitor?'),
        content: SizedBox(
          width: 440,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                '${visitor['visitor_name']} is requesting entry with ${visitor['accompanying_members'] ?? 0} accompanying member(s).',
              ),
              const SizedBox(height: 12),
              TextField(
                controller: note,
                minLines: 2,
                maxLines: 4,
                decoration: InputDecoration(
                  labelText: approving
                      ? 'Message to gate (optional)'
                      : 'Reason for denial (optional)',
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, false),
            child: const Text('Go back'),
          ),
          FilledButton(
            style: approving
                ? null
                : FilledButton.styleFrom(backgroundColor: SocietyColors.danger),
            onPressed: () => Navigator.pop(dialogContext, true),
            child: Text(approving ? 'Approve entry' : 'Deny entry'),
          ),
        ],
      ),
    );
    if (confirmed != true) {
      note.dispose();
      return;
    }
    try {
      await visitorsCubit.decide(
        visitor,
        decision,
        note.text.trim().isEmpty ? null : note.text.trim(),
      );
      _message(approving ? 'Visitor approved for entry.' : 'Visitor denied.');
    } catch (error) {
      _message(error.toString());
    } finally {
      note.dispose();
    }
  }

  Future<void> _cancel(VisitorRecord visitor) async {
    final visitorsCubit = context.read<MemberVisitorsCubit>();
    final note = TextEditingController();
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Cancel expected visitor?'),
        content: SizedBox(
          width: 420,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                '${visitor['visitor_name']} will no longer be allowed to check in.',
              ),
              const SizedBox(height: 12),
              TextField(
                controller: note,
                decoration: const InputDecoration(
                  labelText: 'Cancellation note (optional)',
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, false),
            child: const Text('Keep visitor'),
          ),
          FilledButton(
            style: FilledButton.styleFrom(
              backgroundColor: SocietyColors.danger,
            ),
            onPressed: () => Navigator.pop(dialogContext, true),
            child: const Text('Cancel visitor'),
          ),
        ],
      ),
    );
    if (confirmed != true) {
      note.dispose();
      return;
    }
    try {
      await visitorsCubit.cancel(
        visitor,
        note.text.trim().isEmpty ? null : note.text.trim(),
      );
      _message('Expected visitor cancelled.');
    } catch (error) {
      _message(error.toString());
    } finally {
      note.dispose();
    }
  }

  Future<void> _details(VisitorRecord visitor) => showDialog<void>(
    context: context,
    builder: (_) => _VisitorDetailDialog(
      loadImage: context.read<MemberVisitorsCubit>().loadImage,
      visitor: visitor,
    ),
  );

  void _message(String text) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
    }
  }

  bool _matchesFilter(VisitorRecord visitor) {
    final status = visitor['status']?.toString() ?? '';
    final category = switch (status) {
      'PENDING_RESIDENT_APPROVAL' => 'PENDING',
      'APPROVED' => 'UPCOMING',
      'CHECKED_IN' => 'ACTIVE',
      _ => 'HISTORY',
    };
    if (_filter != 'ALL' && category != _filter) return false;
    final query = _query.trim().toLowerCase();
    if (query.isEmpty) return true;
    return [
      visitor['visitor_name'],
      visitor['mobile'],
      visitor['purpose'],
      visitor['vehicle_number'],
    ].any((value) => value?.toString().toLowerCase().contains(query) == true);
  }

  @override
  Widget build(
    BuildContext context,
  ) => BlocBuilder<MemberVisitorsCubit, MemberVisitorsState>(
    builder: (context, state) {
      if (state.data == null && state.status != MemberVisitorsStatus.failure) {
        return const Center(child: CircularProgressIndicator());
      }
      if (state.data == null) {
        return Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(
                Icons.error_outline,
                size: 44,
                color: SocietyColors.danger,
              ),
              const SizedBox(height: 12),
              Text(state.error.toString(), textAlign: TextAlign.center),
              const SizedBox(height: 12),
              FilledButton.icon(
                onPressed: _load,
                icon: const Icon(Icons.refresh),
                label: const Text('Try again'),
              ),
            ],
          ),
        );
      }
      final visitors = state.data!.visitors;
      final pending = visitors
          .where((item) => item['status'] == 'PENDING_RESIDENT_APPROVAL')
          .length;
      final upcoming = visitors
          .where((item) => item['status'] == 'APPROVED')
          .length;
      final inside = visitors
          .where((item) => item['status'] == 'CHECKED_IN')
          .length;
      final rows = visitors.where(_matchesFilter).toList();

      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Wrap(
              spacing: 16,
              runSpacing: 12,
              alignment: WrapAlignment.spaceBetween,
              crossAxisAlignment: WrapCrossAlignment.center,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'My visitors',
                      style: Theme.of(context).textTheme.headlineMedium,
                    ),
                    const SizedBox(height: 4),
                    const Text(
                      'Pre-register visitors, review gate requests, and track entry.',
                      style: TextStyle(color: SocietyColors.textMuted),
                    ),
                  ],
                ),
                FilledButton.icon(
                  onPressed: _register,
                  icon: const Icon(Icons.person_add_alt_1),
                  label: const Text('Pre-register visitor'),
                ),
              ],
            ),
            const SizedBox(height: 18),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                _VisitorMetric(
                  label: 'Need approval',
                  value: pending,
                  icon: Icons.notification_important_outlined,
                  color: pending > 0
                      ? SocietyColors.warning
                      : SocietyColors.blue,
                ),
                _VisitorMetric(
                  label: 'Expected',
                  value: upcoming,
                  icon: Icons.event_available,
                  color: SocietyColors.blue,
                ),
                _VisitorMetric(
                  label: 'Inside society',
                  value: inside,
                  icon: Icons.login,
                  color: SocietyColors.success,
                ),
              ],
            ),
            const SizedBox(height: 18),
            TextField(
              onChanged: (value) => setState(() => _query = value),
              decoration: const InputDecoration(
                prefixIcon: Icon(Icons.search),
                labelText: 'Search visitor, phone, purpose, or vehicle',
              ),
            ),
            const SizedBox(height: 12),
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: SegmentedButton<String>(
                segments: const [
                  ButtonSegment(value: 'ALL', label: Text('All')),
                  ButtonSegment(value: 'PENDING', label: Text('Need approval')),
                  ButtonSegment(value: 'UPCOMING', label: Text('Expected')),
                  ButtonSegment(value: 'ACTIVE', label: Text('Inside')),
                  ButtonSegment(value: 'HISTORY', label: Text('History')),
                ],
                selected: {_filter},
                onSelectionChanged: (values) =>
                    setState(() => _filter = values.first),
              ),
            ),
            const SizedBox(height: 16),
            if (rows.isEmpty)
              const Card(
                child: Padding(
                  padding: EdgeInsets.all(36),
                  child: Column(
                    children: [
                      Icon(
                        Icons.people_outline,
                        size: 44,
                        color: SocietyColors.textMuted,
                      ),
                      SizedBox(height: 10),
                      Text('No visitors match this view.'),
                    ],
                  ),
                ),
              )
            else
              for (final visitor in rows) ...[
                _VisitorCard(
                  loadImage: context.read<MemberVisitorsCubit>().loadImage,
                  visitor: visitor,
                  onDetails: () => _details(visitor),
                  onApprove: () => _decide(visitor, 'APPROVE'),
                  onDeny: () => _decide(visitor, 'DENY'),
                  onCancel: () => _cancel(visitor),
                ),
                const SizedBox(height: 10),
              ],
          ],
        ),
      );
    },
  );
}

class _RegisterVisitorDialog extends StatefulWidget {
  const _RegisterVisitorDialog({
    required this.spaces,
    required this.onRegister,
  });
  final List<VisitorSpace> spaces;
  final Future<void> Function(VisitorRegistrationInput input) onRegister;

  @override
  State<_RegisterVisitorDialog> createState() => _RegisterVisitorDialogState();
}

class _RegisterVisitorDialogState extends State<_RegisterVisitorDialog> {
  final _form = GlobalKey<FormState>();
  final _name = TextEditingController();
  final _mobile = TextEditingController();
  final _purpose = TextEditingController();
  final _vehicle = TextEditingController();
  String? _spaceId;
  int _accompanying = 0;
  DateTime _expected = DateTime.now().add(const Duration(hours: 1));
  bool _saving = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _spaceId = widget.spaces.firstOrNull?.id;
  }

  @override
  void dispose() {
    _name.dispose();
    _mobile.dispose();
    _purpose.dispose();
    _vehicle.dispose();
    super.dispose();
  }

  Future<void> _pickDate() async {
    final date = await showDatePicker(
      context: context,
      initialDate: _expected,
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );
    if (date == null || !mounted) return;
    setState(
      () => _expected = DateTime(
        date.year,
        date.month,
        date.day,
        _expected.hour,
        _expected.minute,
      ),
    );
  }

  Future<void> _pickTime() async {
    final time = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(_expected),
    );
    if (time == null) return;
    setState(
      () => _expected = DateTime(
        _expected.year,
        _expected.month,
        _expected.day,
        time.hour,
        time.minute,
      ),
    );
  }

  Future<void> _save() async {
    if (!_form.currentState!.validate()) return;
    if (_spaceId == null) {
      setState(() => _error = 'No society space is linked to your account.');
      return;
    }
    if (_expected.isBefore(DateTime.now())) {
      setState(() => _error = 'Expected arrival must be in the future.');
      return;
    }
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await widget.onRegister(
        VisitorRegistrationInput(
          spaceId: _spaceId!,
          visitorName: _name.text.trim(),
          mobile: _mobile.text.trim(),
          purpose: _purpose.text.trim(),
          vehicleNumber: _vehicle.text.trim(),
          expectedAt: _expected,
          accompanyingMembers: _accompanying,
        ),
      );
      if (mounted) Navigator.pop(context, true);
    } catch (error) {
      if (mounted) setState(() => _error = error.toString());
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Pre-register visitor'),
    content: SizedBox(
      width: 560,
      child: SingleChildScrollView(
        child: Form(
          key: _form,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              DropdownButtonFormField<String>(
                initialValue: _spaceId,
                isExpanded: true,
                decoration: const InputDecoration(
                  labelText: 'Visiting your space',
                ),
                items: widget.spaces
                    .map(
                      (space) => DropdownMenuItem(
                        value: space.id,
                        child: Text(
                          '${space.block ?? ''} / ${space.number}',
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    )
                    .toList(),
                onChanged: (value) => setState(() => _spaceId = value),
                validator: (value) => value == null ? 'Select a space' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _name,
                decoration: const InputDecoration(labelText: 'Visitor name *'),
                validator: _required,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _mobile,
                keyboardType: TextInputType.phone,
                decoration: const InputDecoration(labelText: 'Mobile number'),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _purpose,
                decoration: const InputDecoration(
                  labelText: 'Purpose of visit *',
                ),
                validator: _required,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _vehicle,
                textCapitalization: TextCapitalization.characters,
                decoration: const InputDecoration(
                  labelText: 'Vehicle number (optional)',
                ),
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<int>(
                initialValue: _accompanying,
                isExpanded: true,
                decoration: const InputDecoration(
                  labelText: 'Accompanying members',
                ),
                items: List.generate(
                  11,
                  (count) => DropdownMenuItem(
                    value: count,
                    child: Text(
                      count == 0
                          ? 'No accompanying members'
                          : '$count member${count == 1 ? '' : 's'}',
                    ),
                  ),
                ),
                onChanged: (value) => setState(() => _accompanying = value!),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: _pickDate,
                      icon: const Icon(Icons.calendar_today),
                      label: Text(_dateOnly(_expected)),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: _pickTime,
                      icon: const Icon(Icons.schedule),
                      label: Text(
                        TimeOfDay.fromDateTime(_expected).format(context),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              const ListTile(
                contentPadding: EdgeInsets.zero,
                leading: Icon(Icons.info_outline, color: SocietyColors.blue),
                title: Text('This visitor will be pre-approved.'),
                subtitle: Text(
                  'Gate staff must still capture a photo before check-in.',
                ),
              ),
              if (_error != null)
                Text(
                  _error!,
                  style: const TextStyle(color: SocietyColors.danger),
                ),
            ],
          ),
        ),
      ),
    ),
    actions: [
      TextButton(
        onPressed: _saving ? null : () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton.icon(
        onPressed: _saving ? null : _save,
        icon: _saving
            ? const SizedBox(
                width: 18,
                height: 18,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  color: Colors.white,
                ),
              )
            : const Icon(Icons.event_available),
        label: const Text('Pre-approve visitor'),
      ),
    ],
  );

  String? _required(String? value) =>
      value == null || value.trim().isEmpty ? 'This field is required' : null;
}

class _VisitorCard extends StatelessWidget {
  const _VisitorCard({
    required this.loadImage,
    required this.visitor,
    required this.onDetails,
    required this.onApprove,
    required this.onDeny,
    required this.onCancel,
  });
  final Future<List<int>> Function(String url) loadImage;
  final VisitorRecord visitor;
  final VoidCallback onDetails;
  final VoidCallback onApprove;
  final VoidCallback onDeny;
  final VoidCallback onCancel;

  @override
  Widget build(BuildContext context) {
    final status = visitor['status']?.toString() ?? '';
    final pending = status == 'PENDING_RESIDENT_APPROVAL';
    final approved = status == 'APPROVED';
    return Card(
      child: InkWell(
        onTap: onDetails,
        borderRadius: BorderRadius.circular(20),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: LayoutBuilder(
            builder: (context, constraints) {
              final details = Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _VisitorThumbnail(loadImage: loadImage, visitor: visitor),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Expanded(
                              child: Text(
                                visitor['visitor_name']?.toString() ??
                                    'Visitor',
                                style: Theme.of(context).textTheme.titleMedium,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                            _VisitorStatus(status: status),
                          ],
                        ),
                        const SizedBox(height: 5),
                        Text(
                          '${visitor['total_people'] ?? 1} people · ${visitor['purpose'] ?? 'Visit'}',
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                        const SizedBox(height: 3),
                        Text(
                          '${visitor['mobile']?.toString().isEmpty == false ? visitor['mobile'] : 'No phone'} · ${visitor['vehicle_number']?.toString().isEmpty == false ? visitor['vehicle_number'] : 'No vehicle'}',
                          style: const TextStyle(
                            color: SocietyColors.textMuted,
                          ),
                        ),
                        const SizedBox(height: 3),
                        Text(
                          'Expected ${_dateTime(visitor['expected_at'] ?? visitor['created_at'])}',
                          style: const TextStyle(
                            color: SocietyColors.textMuted,
                            fontSize: 12,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              );
              final actions = Wrap(
                spacing: 8,
                runSpacing: 8,
                alignment: WrapAlignment.end,
                children: [
                  if (pending)
                    OutlinedButton.icon(
                      onPressed: onDeny,
                      icon: const Icon(Icons.close),
                      label: const Text('Deny'),
                    ),
                  if (pending)
                    FilledButton.icon(
                      onPressed: onApprove,
                      icon: const Icon(Icons.check),
                      label: const Text('Approve'),
                    ),
                  if (approved)
                    OutlinedButton.icon(
                      onPressed: onCancel,
                      icon: const Icon(Icons.event_busy),
                      label: const Text('Cancel'),
                    ),
                  TextButton.icon(
                    onPressed: onDetails,
                    icon: const Icon(Icons.visibility_outlined),
                    label: const Text('Details'),
                  ),
                ],
              );
              if (constraints.maxWidth < 680) {
                return Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [details, const SizedBox(height: 12), actions],
                );
              }
              return Row(
                children: [
                  Expanded(child: details),
                  const SizedBox(width: 14),
                  actions,
                ],
              );
            },
          ),
        ),
      ),
    );
  }
}

class _VisitorDetailDialog extends StatelessWidget {
  const _VisitorDetailDialog({required this.loadImage, required this.visitor});
  final Future<List<int>> Function(String url) loadImage;
  final VisitorRecord visitor;

  @override
  Widget build(BuildContext context) {
    final urls = (visitor['photo_urls'] as List? ?? const [])
        .map((url) => url.toString())
        .toList();
    return AlertDialog(
      title: Row(
        children: [
          Expanded(
            child: Text(visitor['visitor_name']?.toString() ?? 'Visitor'),
          ),
          _VisitorStatus(status: visitor['status']?.toString() ?? ''),
        ],
      ),
      content: SizedBox(
        width: 680,
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (urls.isEmpty)
                Container(
                  width: double.infinity,
                  height: 180,
                  decoration: BoxDecoration(
                    color: SocietyColors.surfaceMuted,
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: const Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.photo_camera_outlined, size: 46),
                      SizedBox(height: 8),
                      Text('Photo will be captured at the gate'),
                    ],
                  ),
                )
              else
                SizedBox(
                  height: 280,
                  child: PageView(
                    children: urls
                        .map(
                          (url) => _AuthorizedImage(
                            loadImage: loadImage,
                            url: url,
                            borderRadius: 16,
                          ),
                        )
                        .toList(),
                  ),
                ),
              if (urls.length > 1) ...[
                const SizedBox(height: 6),
                Center(child: Text('Swipe through ${urls.length} photos')),
              ],
              const SizedBox(height: 20),
              Wrap(
                spacing: 18,
                runSpacing: 14,
                children: [
                  _VisitorFact('Purpose', visitor['purpose']),
                  _VisitorFact('Mobile', visitor['mobile']),
                  _VisitorFact('Total people', visitor['total_people'] ?? 1),
                  _VisitorFact(
                    'Accompanying',
                    visitor['accompanying_members'] ?? 0,
                  ),
                  _VisitorFact('Vehicle', visitor['vehicle_number']),
                  _VisitorFact('Your space', _space(visitor)),
                  _VisitorFact('Expected', _dateTime(visitor['expected_at'])),
                  _VisitorFact('Requested', _dateTime(visitor['created_at'])),
                  if (visitor['checked_in_at'] != null)
                    _VisitorFact(
                      'Checked in',
                      _dateTime(visitor['checked_in_at']),
                    ),
                  if (visitor['checked_out_at'] != null)
                    _VisitorFact(
                      'Checked out',
                      _dateTime(visitor['checked_out_at']),
                    ),
                ],
              ),
              if (visitor['approval_note']?.toString().isNotEmpty == true) ...[
                const SizedBox(height: 18),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: SocietyColors.surfaceMuted,
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: Text('Approval note: ${visitor['approval_note']}'),
                ),
              ],
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Close'),
        ),
      ],
    );
  }
}

class _VisitorThumbnail extends StatelessWidget {
  const _VisitorThumbnail({required this.loadImage, required this.visitor});
  final Future<List<int>> Function(String url) loadImage;
  final VisitorRecord visitor;

  @override
  Widget build(BuildContext context) {
    final url = visitor['photo_url']?.toString();
    return SizedBox(
      width: 76,
      height: 76,
      child: url == null
          ? DecoratedBox(
              decoration: BoxDecoration(
                color: SocietyColors.surfaceMuted,
                borderRadius: BorderRadius.circular(14),
              ),
              child: const Icon(Icons.person_outline, size: 38),
            )
          : _AuthorizedImage(loadImage: loadImage, url: url, borderRadius: 14),
    );
  }
}

class _AuthorizedImage extends StatelessWidget {
  const _AuthorizedImage({
    required this.loadImage,
    required this.url,
    required this.borderRadius,
  });
  final Future<List<int>> Function(String url) loadImage;
  final String url;
  final double borderRadius;

  @override
  Widget build(BuildContext context) =>
      _BlocImage(loadImage: loadImage, url: url, borderRadius: borderRadius);
}

class _BlocImage extends StatefulWidget {
  const _BlocImage({
    required this.loadImage,
    required this.url,
    required this.borderRadius,
  });
  final Future<List<int>> Function(String url) loadImage;
  final String url;
  final double borderRadius;

  @override
  State<_BlocImage> createState() => _BlocImageState();
}

class _BlocImageState extends State<_BlocImage> {
  late final AsyncDataBloc<List<int>> _bloc;

  @override
  void initState() {
    super.initState();
    _bloc = AsyncDataBloc(() => widget.loadImage(widget.url))..refresh();
  }

  @override
  void dispose() {
    _bloc.close();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) =>
      BlocBuilder<AsyncDataBloc<List<int>>, AsyncDataState<List<int>>>(
        bloc: _bloc,
        builder: (context, state) {
          if (state.status == AsyncDataStatus.failure) {
            return const Center(child: Icon(Icons.broken_image_outlined));
          }
          if (state.data == null) {
            return const Center(child: CircularProgressIndicator());
          }
          return ClipRRect(
            borderRadius: BorderRadius.circular(widget.borderRadius),
            child: Image.memory(
              Uint8List.fromList(state.data!),
              fit: BoxFit.cover,
              width: double.infinity,
              height: double.infinity,
            ),
          );
        },
      );
}

class _VisitorStatus extends StatelessWidget {
  const _VisitorStatus({required this.status});
  final String status;

  @override
  Widget build(BuildContext context) {
    final color = switch (status) {
      'APPROVED' || 'CHECKED_IN' || 'CHECKED_OUT' => SocietyColors.success,
      'DENIED' || 'CANCELLED' => SocietyColors.danger,
      _ => SocietyColors.warning,
    };
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 5),
      decoration: BoxDecoration(
        color: color.withValues(alpha: .1),
        borderRadius: BorderRadius.circular(100),
      ),
      child: Text(
        _statusLabel(status),
        style: TextStyle(
          color: color,
          fontSize: 11,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class _VisitorMetric extends StatelessWidget {
  const _VisitorMetric({
    required this.label,
    required this.value,
    required this.icon,
    required this.color,
  });
  final String label;
  final int value;
  final IconData icon;
  final Color color;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: 185,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(15),
        child: Row(
          children: [
            Icon(icon, color: color),
            const SizedBox(width: 11),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('$value', style: Theme.of(context).textTheme.titleLarge),
                Text(
                  label,
                  style: const TextStyle(color: SocietyColors.textMuted),
                ),
              ],
            ),
          ],
        ),
      ),
    ),
  );
}

class _VisitorFact extends StatelessWidget {
  const _VisitorFact(this.label, this.value);
  final String label;
  final Object? value;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: 195,
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(color: SocietyColors.textMuted, fontSize: 12),
        ),
        const SizedBox(height: 2),
        Text(
          value == null || value.toString().isEmpty ? '—' : value.toString(),
          style: const TextStyle(fontWeight: FontWeight.w600),
        ),
      ],
    ),
  );
}

String _statusLabel(String status) => switch (status) {
  'PENDING_RESIDENT_APPROVAL' => 'Needs your approval',
  'PENDING_STAFF_APPROVAL' => 'Awaiting staff',
  'CHECKED_IN' => 'Inside society',
  'CHECKED_OUT' => 'Checked out',
  'APPROVED' => 'Expected / approved',
  'DENIED' => 'Denied',
  'CANCELLED' => 'Cancelled',
  _ => status.replaceAll('_', ' '),
};

String _space(VisitorRecord visitor) {
  final block = visitor['block']?.toString();
  final number = visitor['space_number']?.toString() ?? '—';
  return block == null || block.isEmpty ? number : '$block / $number';
}

String _dateOnly(DateTime value) =>
    '${value.day.toString().padLeft(2, '0')}/${value.month.toString().padLeft(2, '0')}/${value.year}';

String _dateTime(Object? value) {
  if (value == null) return '—';
  final parsed = DateTime.tryParse(value.toString());
  if (parsed == null) return value.toString();
  final local = parsed.toLocal();
  String two(int number) => number.toString().padLeft(2, '0');
  return '${two(local.day)}/${two(local.month)}/${local.year} ${two(local.hour)}:${two(local.minute)}';
}
