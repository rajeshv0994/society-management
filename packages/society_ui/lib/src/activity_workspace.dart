import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';
import 'async_data_bloc.dart';
import 'camera_capture.dart';
import 'theme.dart';
import 'workspace_record.dart';

enum ActivityWorkspaceRole { societyAdmin, staff, resident }

Widget buildResidentActivityWorkspace(BlocManagedApiGateway client) =>
    ActivityWorkspace(client: client, role: ActivityWorkspaceRole.resident);

Widget buildStaffActivityWorkspace(BlocManagedApiGateway client) =>
    ActivityWorkspace(client: client, role: ActivityWorkspaceRole.staff);

class ActivityWorkspace extends StatefulWidget {
  const ActivityWorkspace({
    super.key,
    required this.client,
    required this.role,
  });

  final BlocManagedApiGateway client;
  final ActivityWorkspaceRole role;

  @override
  State<ActivityWorkspace> createState() => _ActivityWorkspaceState();
}

class _ActivityWorkspaceState extends State<ActivityWorkspace> {
  late final AsyncDataBloc<Map<String, dynamic>> _bloc;
  Map<String, dynamic> get data => _bloc.state.data ?? const {};
  String status = 'ALL';

  bool get resident => widget.role == ActivityWorkspaceRole.resident;
  bool get admin => widget.role == ActivityWorkspaceRole.societyAdmin;
  bool get manager => !resident;

  @override
  void initState() {
    super.initState();
    _bloc = AsyncDataBloc(
      () async => Map<String, dynamic>.unmodifiable(
        Map<String, dynamic>.from(
          await widget.client.get('/api/activities') as Map,
        ),
      ),
    );
    load();
  }

  Future<void> load() => _bloc.refresh();

  @override
  void dispose() {
    _bloc.close();
    super.dispose();
  }

  Future<void> editActivity([Map<String, dynamic>? item]) async {
    final changed = await showDialog<bool>(
      context: context,
      barrierDismissible: false,
      builder: (_) => _ActivityForm(client: widget.client, item: item),
    );
    if (changed == true) await load();
  }

  Future<void> activityAction(Map<String, dynamic> item, String action) async {
    String? reason;
    if (action == 'CANCEL') {
      reason = await noteDialog('Cancel activity', 'Cancellation reason');
      if (reason == null) return;
    } else if (!await confirm(
      '${_label(action)} activity',
      '${_label(action)} ${item['title']}?',
    )) {
      return;
    }
    try {
      await widget.client.patch('/api/activities/${item['id']}', {
        'action': action,
        if (reason != null) 'reason': reason,
      });
      await load();
      message('Activity updated.');
    } catch (failure) {
      message(failure.toString());
    }
  }

  Future<void> join(Map<String, dynamic> item) async {
    final body = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => _JoinDialog(item: item),
    );
    if (body == null) return;
    try {
      await widget.client.post('/api/activities/${item['id']}/join', body);
      await load();
      message(
        (num.tryParse(item['fee_per_person']?.toString() ?? '') ?? 0) > 0
            ? 'Registration created. Complete payment to confirm your place.'
            : 'Activity registration created.',
      );
    } catch (failure) {
      message(failure.toString());
    }
  }

  Future<void> checkout(Map<String, dynamic> row) async {
    final method = await showDialog<String>(
      context: context,
      builder: (dialog) => SimpleDialog(
        title: const Text('Pay activity fee · Razorpay test mode'),
        children: [
          const Padding(
            padding: EdgeInsets.fromLTRB(24, 0, 24, 8),
            child: Text('TEST MODE · No real money charged'),
          ),
          for (final method in const ['UPI', 'CARD', 'NET_BANKING'])
            SimpleDialogOption(
              onPressed: () => Navigator.pop(dialog, method),
              child: Text(_label(method)),
            ),
        ],
      ),
    );
    if (method == null) return;
    try {
      final paid = Map<String, dynamic>.from(
        await widget.client.post(
              '/api/activities/participations/${row['id']}/checkout',
              {'paymentMethod': method},
            )
            as Map,
      );
      await load();
      message('Payment completed: ${paid['payment_reference']}');
    } catch (failure) {
      message(failure.toString());
    }
  }

  Future<void> participationAction(
    Map<String, dynamic> row,
    String action,
  ) async {
    String? reason;
    if (const {'REJECT', 'CANCEL'}.contains(action)) {
      reason = await noteDialog(_label(action), '${_label(action)} reason');
      if (reason == null) return;
    } else if (!await confirm(
      _label(action),
      '${_label(action)} ${row['participant_name'] ?? 'this registration'}?',
    )) {
      return;
    }
    try {
      if (action == 'CASH') {
        await widget.client.post(
          '/api/activities/participations/${row['id']}/cash',
        );
      } else {
        await widget.client.patch(
          '/api/activities/participations/${row['id']}',
          {'action': action, if (reason != null) 'reason': reason},
        );
      }
      await load();
      message('Registration updated.');
    } catch (failure) {
      message(failure.toString());
    }
  }

  Future<void> uploadPhoto(Map<String, dynamic> activity) async {
    final caption = await noteDialog(
      'Photo caption',
      'Caption (enter a short description)',
    );
    if (caption == null) return;
    final capture = await captureVisitorPhoto(context);
    if (capture == null) return;
    try {
      final encoded = Uri.encodeQueryComponent(caption);
      await widget.client.upload(
        '/api/activities/${activity['id']}/photos?caption=$encoded',
        bytes: capture.bytes,
        filename: capture.name,
      );
      await load();
      message(
        admin
            ? 'Photo published.'
            : 'Photo submitted for Society Admin approval.',
      );
    } catch (failure) {
      message(failure.toString());
    }
  }

  Future<void> photoAction(Map<String, dynamic> photo, String action) async {
    String? reason;
    if (const {'REJECT', 'REMOVE'}.contains(action)) {
      reason = await noteDialog(_label(action), '${_label(action)} reason');
      if (reason == null) return;
    }
    try {
      await widget.client.patch('/api/activities/photos/${photo['id']}', {
        'action': action,
        if (reason != null) 'reason': reason,
      });
      await load();
    } catch (failure) {
      message(failure.toString());
    }
  }

  Future<void> history(Map<String, dynamic> item) async {
    try {
      final rows = _maps(
        await widget.client.get('/api/activities/${item['id']}/history'),
      );
      if (!mounted) return;
      await showDialog<void>(
        context: context,
        builder: (dialog) => AlertDialog(
          title: Text('Activity timeline · ${item['title']}'),
          content: SizedBox(
            width: 650,
            child: rows.isEmpty
                ? const Text('No activity events.')
                : ListView.separated(
                    shrinkWrap: true,
                    itemCount: rows.length,
                    separatorBuilder: (_, _) => const Divider(),
                    itemBuilder: (_, index) {
                      final row = rows[index];
                      return ListTile(
                        leading: const Icon(Icons.history),
                        title: Text(_label(row['event_type'])),
                        subtitle: Text(
                          '${row['actor_name'] ?? _label(row['actor_role'])} · ${_dateTime(row['occurred_at'])}'
                          '${row['note'] == null ? '' : '\n${row['note']}'}',
                        ),
                      );
                    },
                  ),
          ),
          actions: [
            FilledButton(
              onPressed: () => Navigator.pop(dialog),
              child: const Text('Close'),
            ),
          ],
        ),
      );
    } catch (failure) {
      message(failure.toString());
    }
  }

  Future<String?> noteDialog(String title, String label) async {
    final controller = TextEditingController();
    final result = await showDialog<String>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: Text(title),
        content: TextField(
          controller: controller,
          maxLines: 3,
          maxLength: 500,
          decoration: InputDecoration(labelText: label),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialog),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              if (controller.text.trim().isNotEmpty) {
                Navigator.pop(dialog, controller.text.trim());
              }
            },
            child: const Text('Submit'),
          ),
        ],
      ),
    );
    controller.dispose();
    return result;
  }

  Future<bool> confirm(String title, String text) async =>
      await showDialog<bool>(
        context: context,
        builder: (dialog) => AlertDialog(
          title: Text(title),
          content: Text(text),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialog, false),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(dialog, true),
              child: const Text('Confirm'),
            ),
          ],
        ),
      ) ??
      false;

  void message(String text) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
    }
  }

  @override
  Widget build(BuildContext context) =>
      BlocBuilder<
        AsyncDataBloc<Map<String, dynamic>>,
        AsyncDataState<Map<String, dynamic>>
      >(
        bloc: _bloc,
        builder: (context, state) {
          if (state.status == AsyncDataStatus.initial ||
              state.status == AsyncDataStatus.loading) {
            return const Center(child: CircularProgressIndicator());
          }
          if (state.status == AsyncDataStatus.failure) {
            return Center(
              child: FilledButton.icon(
                onPressed: load,
                icon: const Icon(Icons.refresh),
                label: Text(state.error.toString()),
              ),
            );
          }
          final activities = _maps(data['activities']);
          final filtered = status == 'ALL'
              ? activities
              : activities.where((item) => item['status'] == status).toList();
          return DefaultTabController(
            length: 3,
            child: Column(
              children: [
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 18, 20, 8),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      _Header(
                        admin: admin,
                        onCreate: editActivity,
                        total: data['total_activities'],
                        upcoming: data['upcoming'],
                        registrations: data['my_registrations'],
                        payments: data['pending_payments'],
                        photos: data['pending_photos'],
                      ),
                      const SizedBox(height: 12),
                      const TabBar(
                        isScrollable: true,
                        tabs: [
                          Tab(text: 'Activities'),
                          Tab(text: 'Participations'),
                          Tab(text: 'Galleries'),
                        ],
                      ),
                    ],
                  ),
                ),
                Expanded(
                  child: TabBarView(
                    children: [
                      _activityList(filtered, activities),
                      _participationList(),
                      _gallery(activities),
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      );

  Widget _activityList(
    List<Map<String, dynamic>> filtered,
    List<Map<String, dynamic>> all,
  ) => RefreshIndicator(
    onRefresh: load,
    child: ListView(
      padding: const EdgeInsets.all(20),
      children: [
        Align(
          alignment: Alignment.centerRight,
          child: DropdownButton<String>(
            value: status,
            items: [
              for (final value in const [
                'ALL',
                'DRAFT',
                'PUBLISHED',
                'COMPLETED',
                'CANCELLED',
                'ARCHIVED',
              ])
                DropdownMenuItem(value: value, child: Text(_label(value))),
            ],
            onChanged: (value) => setState(() => status = value ?? 'ALL'),
          ),
        ),
        if (filtered.isEmpty)
          const _Empty('No activities match this filter.')
        else
          for (final item in filtered)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: _ActivityCard(
                item: item,
                resident: resident,
                admin: admin,
                onJoin: () => join(item),
                onEdit: () => editActivity(item),
                onAction: (action) => activityAction(item, action),
                onHistory: () => history(item),
                onPhoto: () => uploadPhoto(item),
              ),
            ),
      ],
    ),
  );

  Widget _participationList() {
    final rows = _maps(data['participations']);
    return RefreshIndicator(
      onRefresh: load,
      child: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Text(
            resident ? 'My registrations' : 'Participation management',
            style: Theme.of(context).textTheme.titleLarge,
          ),
          const SizedBox(height: 10),
          if (rows.isEmpty)
            const _Empty('No activity registrations yet.')
          else
            for (final row in rows)
              Padding(
                padding: const EdgeInsets.only(bottom: 10),
                child: _ParticipationCard(
                  row: row,
                  resident: resident,
                  onCheckout: () => checkout(row),
                  onAction: (action) => participationAction(row, action),
                ),
              ),
        ],
      ),
    );
  }

  Widget _gallery(List<Map<String, dynamic>> activities) {
    final rows = _maps(data['photos']);
    return RefreshIndicator(
      onRefresh: load,
      child: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Text(
            'Activity galleries',
            style: Theme.of(context).textTheme.titleLarge,
          ),
          const Text(
            'Member submissions are visible to others only after Society Admin approval.',
            style: TextStyle(color: SocietyColors.textMuted),
          ),
          const SizedBox(height: 12),
          if (rows.isEmpty)
            const _Empty('No activity photos have been published yet.')
          else
            LayoutBuilder(
              builder: (_, constraints) {
                final width = constraints.maxWidth < 600
                    ? constraints.maxWidth
                    : constraints.maxWidth < 1000
                    ? (constraints.maxWidth - 12) / 2
                    : (constraints.maxWidth - 24) / 3;
                return Wrap(
                  spacing: 12,
                  runSpacing: 12,
                  children: [
                    for (final photo in rows)
                      SizedBox(
                        width: width,
                        child: _PhotoCard(
                          client: widget.client,
                          photo: photo,
                          admin: admin,
                          onAction: (action) => photoAction(photo, action),
                        ),
                      ),
                  ],
                );
              },
            ),
        ],
      ),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header({
    required this.admin,
    required this.onCreate,
    required this.total,
    required this.upcoming,
    required this.registrations,
    required this.payments,
    required this.photos,
  });
  final bool admin;
  final VoidCallback onCreate;
  final Object? total;
  final Object? upcoming;
  final Object? registrations;
  final Object? payments;
  final Object? photos;

  @override
  Widget build(BuildContext context) => Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      LayoutBuilder(
        builder: (_, constraints) {
          final text = Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Activities & galleries',
                style: Theme.of(context).textTheme.headlineSmall,
              ),
              const Text(
                'Join free or paid society events, manage attendance, and share moderated memories.',
                style: TextStyle(color: SocietyColors.textMuted),
              ),
            ],
          );
          final button = admin
              ? FilledButton.icon(
                  onPressed: onCreate,
                  icon: const Icon(Icons.add),
                  label: const Text('Create activity'),
                )
              : null;
          if (constraints.maxWidth < 650) {
            return Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                text,
                if (button != null) ...[const SizedBox(height: 10), button],
              ],
            );
          }
          return Row(
            children: [
              Expanded(child: text),
              if (button != null) button,
            ],
          );
        },
      ),
      const SizedBox(height: 12),
      Wrap(
        spacing: 10,
        runSpacing: 10,
        children: [
          _Metric('Activities', total),
          _Metric('Upcoming', upcoming),
          _Metric('Registrations', registrations),
          _Metric('Payments due', payments),
          _Metric('Photos to review', photos),
        ],
      ),
    ],
  );
}

class _ActivityCard extends StatelessWidget {
  const _ActivityCard({
    required this.item,
    required this.resident,
    required this.admin,
    required this.onJoin,
    required this.onEdit,
    required this.onAction,
    required this.onHistory,
    required this.onPhoto,
  });
  final Map<String, dynamic> item;
  final bool resident;
  final bool admin;
  final VoidCallback onJoin;
  final VoidCallback onEdit;
  final ValueChanged<String> onAction;
  final VoidCallback onHistory;
  final VoidCallback onPhoto;

  @override
  Widget build(BuildContext context) {
    final mine = item['my_participation'] is Map
        ? Map<String, dynamic>.from(item['my_participation'] as Map)
        : null;
    final fee = num.tryParse(item['fee_per_person']?.toString() ?? '') ?? 0;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                _Chip(_label(item['status']), SocietyColors.blue),
                _Chip(_label(item['category']), const Color(0xff7c3aed)),
                _Chip(
                  fee > 0 ? '₹${fee.toStringAsFixed(2)} / person' : 'Free',
                  fee > 0 ? const Color(0xffb45309) : const Color(0xff15803d),
                ),
                if (item['approval_required'] == true)
                  const _Chip('Approval required', Color(0xffc2410c)),
              ],
            ),
            const SizedBox(height: 10),
            Text(
              item['title']?.toString() ?? '',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 5),
            Text(item['description']?.toString() ?? ''),
            const SizedBox(height: 12),
            Wrap(
              spacing: 16,
              runSpacing: 8,
              children: [
                _Fact(Icons.calendar_month, _dateTime(item['starts_at'])),
                _Fact(Icons.location_on_outlined, item['venue']),
                _Fact(
                  Icons.people_outline,
                  '${item['registered_count'] ?? 0} registered',
                ),
                if (item['capacity'] != null)
                  _Fact(
                    Icons.event_seat_outlined,
                    '${item['available_places']} places left',
                  ),
                _Fact(Icons.groups_outlined, _label(item['audience'])),
              ],
            ),
            if (mine != null) ...[
              const Divider(height: 24),
              Text(
                'Your registration: ${_label(mine['status'])} · ${_label(mine['payment_status'])}',
                style: const TextStyle(fontWeight: FontWeight.w700),
              ),
            ],
            const SizedBox(height: 14),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                OutlinedButton.icon(
                  onPressed: onHistory,
                  icon: const Icon(Icons.history),
                  label: const Text('Timeline'),
                ),
                if (resident && item['status'] == 'PUBLISHED' && mine == null)
                  FilledButton(
                    onPressed: onJoin,
                    child: const Text('Participate'),
                  ),
                if (resident &&
                    mine != null &&
                    !(_parse(item['starts_at']) ??
                            DateTime.now().add(const Duration(days: 1)))
                        .isAfter(DateTime.now()) &&
                    const {
                      'CONFIRMED',
                      'CHECKED_IN',
                      'ATTENDED',
                    }.contains(mine['status']))
                  OutlinedButton.icon(
                    onPressed: onPhoto,
                    icon: const Icon(Icons.add_a_photo_outlined),
                    label: const Text('Submit photo'),
                  ),
                if (admin &&
                    const {'DRAFT', 'PUBLISHED'}.contains(item['status']))
                  OutlinedButton(onPressed: onEdit, child: const Text('Edit')),
                if (admin && item['status'] == 'DRAFT')
                  FilledButton(
                    onPressed: () => onAction('PUBLISH'),
                    child: const Text('Publish'),
                  ),
                if (admin &&
                    const {'DRAFT', 'PUBLISHED'}.contains(item['status']))
                  TextButton(
                    onPressed: () => onAction('CANCEL'),
                    child: const Text('Cancel activity'),
                  ),
                if (admin && item['status'] == 'PUBLISHED')
                  FilledButton.tonal(
                    onPressed: () => onAction('COMPLETE'),
                    child: const Text('Mark completed'),
                  ),
                if (admin &&
                    const {'COMPLETED', 'CANCELLED'}.contains(item['status']))
                  OutlinedButton(
                    onPressed: () => onAction('ARCHIVE'),
                    child: const Text('Archive'),
                  ),
                if (admin)
                  OutlinedButton.icon(
                    onPressed: onPhoto,
                    icon: const Icon(Icons.add_a_photo_outlined),
                    label: const Text('Add gallery photo'),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _ParticipationCard extends StatelessWidget {
  const _ParticipationCard({
    required this.row,
    required this.resident,
    required this.onCheckout,
    required this.onAction,
  });
  final Map<String, dynamic> row;
  final bool resident;
  final VoidCallback onCheckout;
  final ValueChanged<String> onAction;

  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(15),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            row['activity_title']?.toString() ?? 'Activity',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 7),
          Wrap(
            spacing: 12,
            runSpacing: 8,
            children: [
              if (!resident)
                _Fact(Icons.person_outline, row['participant_name']),
              _Fact(
                Icons.group_outlined,
                '${row['participant_count']} participant(s)',
              ),
              _Fact(Icons.event_available, _label(row['status'])),
              _Fact(
                Icons.payments_outlined,
                '₹${row['amount']} · ${_label(row['payment_status'])}',
              ),
              _Fact(Icons.schedule, _dateTime(row['starts_at'])),
            ],
          ),
          const SizedBox(height: 10),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              if (resident && row['payment_status'] == 'PENDING')
                FilledButton(
                  onPressed: onCheckout,
                  child: const Text('Pay online'),
                ),
              if (resident &&
                  const {
                    'PENDING_PAYMENT',
                    'PENDING_APPROVAL',
                    'CONFIRMED',
                  }.contains(row['status']))
                TextButton(
                  onPressed: () => onAction('CANCEL'),
                  child: const Text('Cancel registration'),
                ),
              if (!resident && row['payment_status'] == 'PENDING')
                FilledButton.tonal(
                  onPressed: () => onAction('CASH'),
                  child: const Text('Record cash'),
                ),
              if (!resident && row['status'] == 'PENDING_APPROVAL') ...[
                FilledButton(
                  onPressed: () => onAction('APPROVE'),
                  child: const Text('Approve'),
                ),
                TextButton(
                  onPressed: () => onAction('REJECT'),
                  child: const Text('Reject'),
                ),
              ],
              if (!resident && row['status'] == 'CONFIRMED') ...[
                FilledButton(
                  onPressed: () => onAction('CHECK_IN'),
                  child: const Text('Check in'),
                ),
                OutlinedButton(
                  onPressed: () => onAction('ATTENDED'),
                  child: const Text('Mark attended'),
                ),
                TextButton(
                  onPressed: () => onAction('NO_SHOW'),
                  child: const Text('No show'),
                ),
              ],
              if (!resident && row['status'] == 'CHECKED_IN')
                FilledButton(
                  onPressed: () => onAction('ATTENDED'),
                  child: const Text('Mark attended'),
                ),
              if (!resident && row['payment_status'] == 'REFUND_PENDING')
                FilledButton.tonal(
                  onPressed: () => onAction('REFUND'),
                  child: const Text('Record refund'),
                ),
            ],
          ),
        ],
      ),
    ),
  );
}

class _PhotoCard extends StatefulWidget {
  const _PhotoCard({
    required this.client,
    required this.photo,
    required this.admin,
    required this.onAction,
  });
  final BlocManagedApiGateway client;
  final Map<String, dynamic> photo;
  final bool admin;
  final ValueChanged<String> onAction;

  @override
  State<_PhotoCard> createState() => _PhotoCardState();
}

class _PhotoCardState extends State<_PhotoCard> {
  late final AsyncDataBloc<List<int>> _imageBloc;

  @override
  void initState() {
    super.initState();
    _imageBloc = AsyncDataBloc(
      () => widget.client.getBytes(widget.photo['photo_url'].toString()),
    )..refresh();
  }

  @override
  void dispose() {
    _imageBloc.close();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Card(
    clipBehavior: Clip.antiAlias,
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        AspectRatio(
          aspectRatio: 16 / 10,
          child:
              BlocBuilder<AsyncDataBloc<List<int>>, AsyncDataState<List<int>>>(
                bloc: _imageBloc,
                builder: (_, state) {
                  if (state.data != null)
                    return Image.memory(
                      Uint8List.fromList(state.data!),
                      fit: BoxFit.cover,
                    );
                  if (state.status == AsyncDataStatus.failure)
                    return const Center(
                      child: Icon(Icons.broken_image_outlined),
                    );
                  return const Center(child: CircularProgressIndicator());
                },
              ),
        ),
        Padding(
          padding: const EdgeInsets.all(12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                widget.photo['activity_title']?.toString() ??
                    'Activity gallery',
                style: Theme.of(context).textTheme.titleMedium,
              ),
              if (widget.photo['caption'] != null)
                Text(widget.photo['caption'].toString()),
              const SizedBox(height: 6),
              Wrap(
                spacing: 7,
                runSpacing: 7,
                children: [
                  _Chip(
                    _label(widget.photo['moderation_status']),
                    SocietyColors.blue,
                  ),
                  if (widget.photo['featured'] == true)
                    const _Chip('Featured', Color(0xffb45309)),
                ],
              ),
              if (widget.admin) ...[
                const SizedBox(height: 8),
                Wrap(
                  spacing: 6,
                  runSpacing: 6,
                  children: [
                    if (widget.photo['moderation_status'] == 'PENDING') ...[
                      FilledButton.tonal(
                        onPressed: () => widget.onAction('APPROVE'),
                        child: const Text('Approve'),
                      ),
                      TextButton(
                        onPressed: () => widget.onAction('REJECT'),
                        child: const Text('Reject'),
                      ),
                    ],
                    if (widget.photo['moderation_status'] == 'PUBLISHED')
                      OutlinedButton(
                        onPressed: () => widget.onAction(
                          widget.photo['featured'] == true
                              ? 'UNFEATURE'
                              : 'FEATURE',
                        ),
                        child: Text(
                          widget.photo['featured'] == true
                              ? 'Unfeature'
                              : 'Feature',
                        ),
                      ),
                    if (widget.photo['moderation_status'] != 'REMOVED')
                      TextButton(
                        onPressed: () => widget.onAction('REMOVE'),
                        child: const Text('Remove'),
                      ),
                  ],
                ),
              ],
            ],
          ),
        ),
      ],
    ),
  );
}

class _ActivityForm extends StatefulWidget {
  const _ActivityForm({required this.client, this.item});
  final BlocManagedApiGateway client;
  final Map<String, dynamic>? item;

  @override
  State<_ActivityForm> createState() => _ActivityFormState();
}

class _ActivityFormState extends State<_ActivityForm> {
  final form = GlobalKey<FormState>();
  late final title = TextEditingController(
    text: widget.item?['title']?.toString(),
  );
  late final description = TextEditingController(
    text: widget.item?['description']?.toString(),
  );
  late final venue = TextEditingController(
    text: widget.item?['venue']?.toString(),
  );
  late final capacity = TextEditingController(
    text: widget.item?['capacity']?.toString(),
  );
  late final fee = TextEditingController(
    text: '${widget.item?['fee_per_person'] ?? 0}',
  );
  late final cancellation = TextEditingController(
    text: '${widget.item?['cancellation_hours'] ?? 24}',
  );
  late final organizer = TextEditingController(
    text: widget.item?['organizer_name']?.toString(),
  );
  late final mobile = TextEditingController(
    text: widget.item?['organizer_mobile']?.toString(),
  );
  late final instructions = TextEditingController(
    text: widget.item?['instructions']?.toString(),
  );
  late String category = widget.item?['category']?.toString() ?? 'SOCIAL';
  late String audience = widget.item?['audience']?.toString() ?? 'ALL_MEMBERS';
  late bool approval = widget.item?['approval_required'] == true;
  bool publish = true;
  bool saving = false;
  late DateTime starts =
      _parse(widget.item?['starts_at']) ??
      DateTime.now().add(const Duration(days: 7));
  late DateTime ends =
      _parse(widget.item?['ends_at']) ?? starts.add(const Duration(hours: 2));
  late DateTime registrationOpen =
      _parse(widget.item?['registration_opens_at']) ??
      DateTime.now().subtract(const Duration(minutes: 5));
  late DateTime registrationClose =
      _parse(widget.item?['registration_closes_at']) ??
      starts.subtract(const Duration(hours: 1));

  @override
  void dispose() {
    for (final controller in [
      title,
      description,
      venue,
      capacity,
      fee,
      cancellation,
      organizer,
      mobile,
      instructions,
    ]) {
      controller.dispose();
    }
    super.dispose();
  }

  Future<void> submit() async {
    if (!form.currentState!.validate()) return;
    setState(() => saving = true);
    final body = {
      'title': title.text.trim(),
      'category': category,
      'description': description.text.trim(),
      'venue': venue.text.trim(),
      'startsAt': starts.toUtc().toIso8601String(),
      'endsAt': ends.toUtc().toIso8601String(),
      'registrationOpensAt': registrationOpen.toUtc().toIso8601String(),
      'registrationClosesAt': registrationClose.toUtc().toIso8601String(),
      if (capacity.text.trim().isNotEmpty)
        'capacity': int.tryParse(capacity.text),
      'feePerPerson': num.tryParse(fee.text) ?? 0,
      'approvalRequired': approval,
      'cancellationHours': int.tryParse(cancellation.text) ?? 0,
      'audience': audience,
      'organizerName': organizer.text.trim(),
      'organizerMobile': mobile.text.trim(),
      'instructions': instructions.text.trim(),
      'publishNow': publish,
    };
    try {
      if (widget.item == null) {
        await widget.client.post('/api/activities', body);
      } else {
        await widget.client.put('/api/activities/${widget.item!['id']}', body);
      }
      if (mounted) Navigator.pop(context, true);
    } catch (failure) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(failure.toString())));
        setState(() => saving = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: Text(widget.item == null ? 'Create activity' : 'Edit activity'),
    content: SizedBox(
      width: 760,
      child: Form(
        key: form,
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              _required(title, 'Activity title'),
              const SizedBox(height: 12),
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: [
                  SizedBox(
                    width: 280,
                    child: _dropdown(
                      'Category',
                      category,
                      const [
                        'SPORTS',
                        'CULTURAL',
                        'EDUCATIONAL',
                        'HEALTH',
                        'SOCIAL',
                        'RELIGIOUS',
                        'CHILDREN',
                        'SENIOR_CITIZEN',
                        'VOLUNTEERING',
                        'OTHER',
                      ],
                      (value) => setState(() => category = value),
                    ),
                  ),
                  SizedBox(
                    width: 280,
                    child: _dropdown(
                      'Audience',
                      audience,
                      const [
                        'ALL_MEMBERS',
                        'ADULTS',
                        'CHILDREN',
                        'SENIOR_CITIZENS',
                        'WOMEN',
                        'MEN',
                        'OTHER',
                      ],
                      (value) => setState(() => audience = value),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              _required(description, 'Description', maxLines: 4),
              const SizedBox(height: 12),
              _required(venue, 'Venue'),
              const SizedBox(height: 14),
              _DateField(
                label: 'Starts',
                value: starts,
                onChanged: (value) => setState(() => starts = value),
              ),
              _DateField(
                label: 'Ends',
                value: ends,
                onChanged: (value) => setState(() => ends = value),
              ),
              _DateField(
                label: 'Registration opens',
                value: registrationOpen,
                onChanged: (value) => setState(() => registrationOpen = value),
              ),
              _DateField(
                label: 'Registration closes',
                value: registrationClose,
                onChanged: (value) => setState(() => registrationClose = value),
              ),
              const SizedBox(height: 12),
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: [
                  SizedBox(
                    width: 210,
                    child: TextFormField(
                      controller: capacity,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(
                        labelText: 'Capacity (blank = unlimited)',
                      ),
                    ),
                  ),
                  SizedBox(
                    width: 210,
                    child: TextFormField(
                      controller: fee,
                      keyboardType: const TextInputType.numberWithOptions(
                        decimal: true,
                      ),
                      decoration: const InputDecoration(
                        labelText: 'Fee per person ₹',
                      ),
                      validator: _nonNegative,
                    ),
                  ),
                  SizedBox(
                    width: 210,
                    child: TextFormField(
                      controller: cancellation,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(
                        labelText: 'Cancellation notice hours',
                      ),
                      validator: _nonNegative,
                    ),
                  ),
                ],
              ),
              SwitchListTile(
                value: approval,
                contentPadding: EdgeInsets.zero,
                title: const Text('Registration approval required'),
                onChanged: (value) => setState(() => approval = value),
              ),
              _required(organizer, 'Organizer name'),
              const SizedBox(height: 12),
              TextFormField(
                controller: mobile,
                decoration: const InputDecoration(
                  labelText: 'Organizer mobile',
                ),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: instructions,
                maxLines: 3,
                decoration: const InputDecoration(
                  labelText: 'Participant instructions',
                ),
              ),
              if (widget.item == null)
                SwitchListTile(
                  value: publish,
                  contentPadding: EdgeInsets.zero,
                  title: const Text('Publish immediately'),
                  subtitle: const Text('Turn off to save as draft.'),
                  onChanged: (value) => setState(() => publish = value),
                ),
            ],
          ),
        ),
      ),
    ),
    actions: [
      TextButton(
        onPressed: saving ? null : () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(
        onPressed: saving ? null : submit,
        child: Text(saving ? 'Saving…' : 'Save activity'),
      ),
    ],
  );

  DropdownButtonFormField<String> _dropdown(
    String label,
    String value,
    List<String> values,
    ValueChanged<String> changed,
  ) => DropdownButtonFormField<String>(
    initialValue: value,
    isExpanded: true,
    decoration: InputDecoration(labelText: label),
    items: [
      for (final item in values)
        DropdownMenuItem(value: item, child: Text(_label(item))),
    ],
    onChanged: (next) {
      if (next != null) changed(next);
    },
  );
}

class _JoinDialog extends StatefulWidget {
  const _JoinDialog({required this.item});
  final Map<String, dynamic> item;

  @override
  State<_JoinDialog> createState() => _JoinDialogState();
}

class _JoinDialogState extends State<_JoinDialog> {
  final count = TextEditingController(text: '1');
  final names = TextEditingController();
  final mobile = TextEditingController();
  final notes = TextEditingController();

  @override
  void dispose() {
    count.dispose();
    names.dispose();
    mobile.dispose();
    notes.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final fee =
        num.tryParse(widget.item['fee_per_person']?.toString() ?? '') ?? 0;
    final people = int.tryParse(count.text) ?? 1;
    return AlertDialog(
      title: Text('Participate · ${widget.item['title']}'),
      content: SizedBox(
        width: 520,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextFormField(
              controller: count,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(
                labelText: 'Number of participants',
              ),
              onChanged: (_) => setState(() {}),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: names,
              decoration: const InputDecoration(labelText: 'Participant names'),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: mobile,
              decoration: const InputDecoration(labelText: 'Contact mobile'),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: notes,
              decoration: const InputDecoration(labelText: 'Notes'),
            ),
            const SizedBox(height: 12),
            Align(
              alignment: Alignment.centerLeft,
              child: Text(
                fee == 0
                    ? 'Free activity'
                    : 'Total fee: ₹${(fee * people).toStringAsFixed(2)}',
                style: Theme.of(context).textTheme.titleMedium,
              ),
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Cancel'),
        ),
        FilledButton(
          onPressed: () {
            final parsed = int.tryParse(count.text);
            if (parsed == null || parsed < 1 || parsed > 50) return;
            Navigator.pop(context, {
              'participantCount': parsed,
              'participantNames': names.text.trim(),
              'contactMobile': mobile.text.trim(),
              'notes': notes.text.trim(),
            });
          },
          child: Text(
            fee == 0 ? 'Confirm participation' : 'Continue to payment',
          ),
        ),
      ],
    );
  }
}

class _DateField extends StatelessWidget {
  const _DateField({
    required this.label,
    required this.value,
    required this.onChanged,
  });
  final String label;
  final DateTime value;
  final ValueChanged<DateTime> onChanged;

  Future<void> pick(BuildContext context) async {
    final date = await showDatePicker(
      context: context,
      firstDate: DateTime.now().subtract(const Duration(days: 1)),
      lastDate: DateTime.now().add(const Duration(days: 730)),
      initialDate: value,
    );
    if (date == null || !context.mounted) return;
    final time = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(value),
    );
    if (time == null) return;
    onChanged(
      DateTime(date.year, date.month, date.day, time.hour, time.minute),
    );
  }

  @override
  Widget build(BuildContext context) => ListTile(
    contentPadding: EdgeInsets.zero,
    title: Text(label),
    subtitle: Text(_dateTime(value.toIso8601String())),
    trailing: OutlinedButton(
      onPressed: () => pick(context),
      child: const Text('Change'),
    ),
  );
}

class _Metric extends StatelessWidget {
  const _Metric(this.label, this.value);
  final String label;
  final Object? value;
  @override
  Widget build(BuildContext context) => Container(
    width: 145,
    padding: const EdgeInsets.all(12),
    decoration: BoxDecoration(
      color: SocietyColors.surfaceMuted,
      borderRadius: BorderRadius.circular(12),
    ),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('${value ?? 0}', style: Theme.of(context).textTheme.titleLarge),
        Text(label, maxLines: 2, overflow: TextOverflow.ellipsis),
      ],
    ),
  );
}

class _Fact extends StatelessWidget {
  const _Fact(this.icon, this.value);
  final IconData icon;
  final Object? value;
  @override
  Widget build(BuildContext context) => ConstrainedBox(
    constraints: const BoxConstraints(maxWidth: 300),
    child: Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 17, color: SocietyColors.textMuted),
        const SizedBox(width: 5),
        Flexible(
          child: Text(
            value?.toString() ?? '—',
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
        ),
      ],
    ),
  );
}

class _Chip extends StatelessWidget {
  const _Chip(this.label, this.color);
  final String label;
  final Color color;
  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 5),
    decoration: BoxDecoration(
      color: color.withValues(alpha: .12),
      borderRadius: BorderRadius.circular(30),
    ),
    child: Text(
      label,
      style: TextStyle(color: color, fontSize: 12, fontWeight: FontWeight.w800),
    ),
  );
}

class _Empty extends StatelessWidget {
  const _Empty(this.text);
  final String text;
  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(20),
      child: Text(text, style: const TextStyle(color: SocietyColors.textMuted)),
    ),
  );
}

TextFormField _required(
  TextEditingController controller,
  String label, {
  int maxLines = 1,
}) => TextFormField(
  controller: controller,
  maxLines: maxLines,
  decoration: InputDecoration(labelText: label),
  validator: (value) =>
      value == null || value.trim().isEmpty ? '$label is required' : null,
);

String? _nonNegative(String? value) {
  final parsed = num.tryParse(value ?? '');
  return parsed == null || parsed < 0
      ? 'Enter zero or a positive number'
      : null;
}

DateTime? _parse(Object? value) =>
    DateTime.tryParse(value?.toString() ?? '')?.toLocal();

List<WorkspaceRecord> _maps(Object? value) => workspaceRecords(value);

String _label(Object? value) {
  if (value == null) return '—';
  final words = value.toString().replaceAll('_', ' ').toLowerCase();
  return words.isEmpty ? '—' : '${words[0].toUpperCase()}${words.substring(1)}';
}

String _dateTime(Object? value) {
  final parsed = DateTime.tryParse(value?.toString() ?? '')?.toLocal();
  if (parsed == null) return '—';
  String two(int value) => value.toString().padLeft(2, '0');
  return '${two(parsed.day)}/${two(parsed.month)}/${parsed.year} ${two(parsed.hour)}:${two(parsed.minute)}';
}
