import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';
import 'async_data_bloc.dart';
import 'theme.dart';
import 'workspace_record.dart';

enum AmenityWorkspaceRole { societyAdmin, staff, resident }

Widget buildResidentAmenityWorkspace(BlocManagedApiGateway client) =>
    AmenityWorkspace(client: client, role: AmenityWorkspaceRole.resident);

Widget buildStaffAmenityWorkspace(BlocManagedApiGateway client) =>
    AmenityWorkspace(client: client, role: AmenityWorkspaceRole.staff);

class AmenityWorkspace extends StatefulWidget {
  const AmenityWorkspace({super.key, required this.client, required this.role});
  final BlocManagedApiGateway client;
  final AmenityWorkspaceRole role;

  @override
  State<AmenityWorkspace> createState() => _AmenityWorkspaceState();
}

class _AmenityWorkspaceState extends State<AmenityWorkspace> {
  late final AsyncDataBloc<_AmenityData> _bloc;
  List<Map<String, dynamic>> get _amenities =>
      _bloc.state.data?.amenities ?? const [];
  List<Map<String, dynamic>> get _bookings =>
      _bloc.state.data?.bookings ?? const [];
  String _status = 'ALL';
  String _type = 'ALL';

  bool get _resident => widget.role == AmenityWorkspaceRole.resident;
  bool get _admin => widget.role == AmenityWorkspaceRole.societyAdmin;
  bool get _manager => !_resident;

  @override
  void initState() {
    super.initState();
    _bloc = AsyncDataBloc(() => _AmenityData.load(widget.client));
    _load();
  }

  Future<void> _load() => _bloc.refresh();

  @override
  void dispose() {
    _bloc.close();
    super.dispose();
  }

  Future<void> _editAmenity([Map<String, dynamic>? amenity]) async {
    final changed = await showDialog<bool>(
      context: context,
      builder: (_) => _AmenityForm(client: widget.client, amenity: amenity),
    );
    if (changed == true) await _load();
  }

  Future<void> _book(Map<String, dynamic> amenity) async {
    final booking = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => _AmenityBookingForm(amenity: amenity),
    );
    if (booking == null) return;
    try {
      final created = Map<String, dynamic>.from(
        await widget.client.post(
              '/api/amenities/${amenity['id']}/book',
              booking,
            )
            as Map,
      );
      await _load();
      _message('Booking ${created['booking_number']} created.');
      if (created['status'] == 'PENDING_PAYMENT') await _pay(created);
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _pay(Map<String, dynamic> booking) async {
    final method = await showDialog<String>(
      context: context,
      builder: (_) => _AmenityCheckoutDialog(booking: booking),
    );
    if (method == null) return;
    try {
      final paid = Map<String, dynamic>.from(
        await widget.client.post(
              '/api/amenities/bookings/${booking['id']}/checkout',
              {'paymentMethod': method},
            )
            as Map,
      );
      _message('Payment successful: ${paid['payment_reference']}');
      await _load();
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _cash(Map<String, dynamic> booking) async {
    final ok = await _confirm(
      'Record cash payment',
      'Confirm cash receipt of ${_money(booking['amount'])} for ${booking['booking_number']}?',
      'Record cash',
    );
    if (!ok) return;
    try {
      final paid = Map<String, dynamic>.from(
        await widget.client.post(
              '/api/amenities/bookings/${booking['id']}/cash',
            )
            as Map,
      );
      _message('Cash recorded: ${paid['payment_reference']}');
      await _load();
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _action(Map<String, dynamic> booking, String action) async {
    String? reason;
    if (action == 'REJECT') {
      reason = await _reason();
      if (reason == null) return;
    } else {
      final ok = await _confirm(
        '${_label(action)} booking',
        '${_label(action)} ${booking['booking_number']} for ${booking['amenity_name']}?',
        _label(action),
      );
      if (!ok) return;
    }
    try {
      await widget.client.patch('/api/amenities/bookings/${booking['id']}', {
        'action': action,
        if (reason != null) 'reason': reason,
      });
      await _load();
      _message('Booking ${_label(action).toLowerCase()} completed.');
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<bool> _confirm(String title, String text, String action) async =>
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
              child: Text(action),
            ),
          ],
        ),
      ) ??
      false;

  Future<String?> _reason() async {
    final controller = TextEditingController();
    final value = await showDialog<String>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: const Text('Reject amenity booking'),
        content: TextField(
          controller: controller,
          maxLength: 300,
          decoration: const InputDecoration(labelText: 'Rejection reason'),
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
            child: const Text('Reject'),
          ),
        ],
      ),
    );
    controller.dispose();
    return value;
  }

  void _message(String text) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
    }
  }

  @override
  Widget build(
    BuildContext context,
  ) => BlocBuilder<AsyncDataBloc<_AmenityData>, AsyncDataState<_AmenityData>>(
    bloc: _bloc,
    builder: (context, state) {
      if (state.status == AsyncDataStatus.initial ||
          state.status == AsyncDataStatus.loading) {
        return const Center(child: CircularProgressIndicator());
      }
      if (state.status == AsyncDataStatus.failure) {
        return Center(
          child: FilledButton.icon(
            onPressed: _load,
            icon: const Icon(Icons.refresh),
            label: Text(state.error.toString()),
          ),
        );
      }
      final active = _amenities.where((item) => item['active'] != false).length;
      final visibleAmenities = _amenities
          .where((item) => _type == 'ALL' || item['amenity_type'] == _type)
          .toList();
      final pending = _bookings
          .where(
            (item) => const {
              'PENDING_PAYMENT',
              'PENDING_APPROVAL',
            }.contains(item['status']),
          )
          .length;
      final upcoming = _bookings
          .where(
            (item) =>
                const {'CONFIRMED', 'CHECKED_IN'}.contains(item['status']) &&
                (DateTime.tryParse(
                      item['ends_at']?.toString() ?? '',
                    )?.isAfter(DateTime.now()) ??
                    false),
          )
          .length;
      final revenue = _bookings
          .where((item) => item['payment_status'] == 'PAID')
          .fold<double>(0, (sum, item) => sum + _number(item['amount']));
      final rows = _bookings
          .where((item) => _status == 'ALL' || item['status'] == _status)
          .toList();
      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        _resident ? 'Society amenities' : 'Amenity management',
                        style: Theme.of(context).textTheme.headlineMedium,
                      ),
                      Text(
                        _resident
                            ? 'Reserve facilities, pay charges, and track your bookings.'
                            : 'Manage facilities, booking payments, approvals, and usage.',
                        style: const TextStyle(color: SocietyColors.textMuted),
                      ),
                    ],
                  ),
                ),
                if (_admin)
                  FilledButton.icon(
                    onPressed: _editAmenity,
                    icon: const Icon(Icons.add),
                    label: const Text('Add amenity'),
                  ),
              ],
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                _AmenityMetric('Active amenities', '$active', Icons.pool),
                _AmenityMetric(
                  'Pending action',
                  '$pending',
                  Icons.pending_actions,
                ),
                _AmenityMetric('Upcoming', '$upcoming', Icons.event_available),
                if (_manager)
                  _AmenityMetric('Collected', _money(revenue), Icons.payments),
              ],
            ),
            const SizedBox(height: 20),
            Row(
              children: [
                Expanded(
                  child: Text(
                    'Available facility instances',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                ),
                SizedBox(
                  width: 220,
                  child: DropdownButtonFormField<String>(
                    isExpanded: true,
                    initialValue: _type,
                    decoration: const InputDecoration(
                      labelText: 'Facility type',
                    ),
                    items:
                        const [
                              'ALL',
                              'COMMUNITY_CENTER',
                              'GYM',
                              'SWIMMING_POOL',
                              'SPORTS_COURT',
                              'GUEST_ROOM',
                              'PLAY_AREA',
                              'OTHER',
                            ]
                            .map(
                              (value) => DropdownMenuItem(
                                value: value,
                                child: Text(_label(value)),
                              ),
                            )
                            .toList(),
                    onChanged: (value) => setState(() => _type = value!),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            if (visibleAmenities.isEmpty)
              const _AmenityEmpty('No facility instances match this type.')
            else
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: [
                  for (final amenity in visibleAmenities)
                    _AmenityCard(
                      amenity: amenity,
                      resident: _resident,
                      admin: _admin,
                      onBook: () => _book(amenity),
                      onEdit: () => _editAmenity(amenity),
                    ),
                ],
              ),
            const SizedBox(height: 24),
            Row(
              children: [
                Expanded(
                  child: Text(
                    _resident ? 'My bookings' : 'All bookings',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                ),
                SizedBox(
                  width: 210,
                  child: DropdownButtonFormField<String>(
                    isExpanded: true,
                    initialValue: _status,
                    decoration: const InputDecoration(labelText: 'Status'),
                    items:
                        const [
                              'ALL',
                              'PENDING_PAYMENT',
                              'PENDING_APPROVAL',
                              'CONFIRMED',
                              'CHECKED_IN',
                              'COMPLETED',
                              'CANCELLED',
                              'REJECTED',
                            ]
                            .map(
                              (value) => DropdownMenuItem(
                                value: value,
                                child: Text(_label(value)),
                              ),
                            )
                            .toList(),
                    onChanged: (value) => setState(() => _status = value!),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            if (rows.isEmpty)
              const _AmenityEmpty('No amenity bookings match this status.')
            else
              for (final booking in rows) ...[
                _BookingCard(
                  booking: booking,
                  resident: _resident,
                  onPay: () => _pay(booking),
                  onCash: () => _cash(booking),
                  onAction: (action) => _action(booking, action),
                ),
                const SizedBox(height: 8),
              ],
          ],
        ),
      );
    },
  );
}

class _AmenityData {
  const _AmenityData({required this.amenities, required this.bookings});
  final List<Map<String, dynamic>> amenities;
  final List<Map<String, dynamic>> bookings;

  static Future<_AmenityData> load(BlocManagedApiGateway client) async {
    final values = await Future.wait([
      client.get('/api/amenities'),
      client.get('/api/amenities/bookings'),
    ]);
    return _AmenityData(
      amenities: List.unmodifiable(_maps(values[0])),
      bookings: List.unmodifiable(_maps(values[1])),
    );
  }
}

class _AmenityMetric extends StatelessWidget {
  const _AmenityMetric(this.label, this.value, this.icon);
  final String label, value;
  final IconData icon;
  @override
  Widget build(BuildContext context) => SizedBox(
    width: 210,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(15),
        child: Row(
          children: [
            Icon(icon, color: SocietyColors.blue),
            const SizedBox(width: 10),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(value, style: Theme.of(context).textTheme.titleLarge),
                  Text(
                    label,
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

class _AmenityCard extends StatelessWidget {
  const _AmenityCard({
    required this.amenity,
    required this.resident,
    required this.admin,
    required this.onBook,
    required this.onEdit,
  });
  final Map<String, dynamic> amenity;
  final bool resident, admin;
  final VoidCallback onBook, onEdit;
  @override
  Widget build(BuildContext context) => SizedBox(
    width: 350,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const CircleAvatar(child: Icon(Icons.pool)),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    amenity['name']?.toString() ?? 'Amenity',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                ),
                if (amenity['active'] == false)
                  const Chip(label: Text('Inactive')),
              ],
            ),
            const SizedBox(height: 10),
            Text(
              '${_label(amenity['amenity_type'])} · ${amenity['amenity_code'] ?? '—'}',
              style: const TextStyle(
                color: SocietyColors.blue,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 5),
            Text(amenity['description']?.toString() ?? 'No description'),
            const SizedBox(height: 8),
            Text(
              '${amenity['location'] ?? 'Society premises'} · Capacity ${amenity['capacity'] ?? '—'}\n${_time(amenity['opens_at'])}–${_time(amenity['closes_at'])} · ${amenity['slot_minutes']} minute slots\n${_pricingSummary(amenity)}${_number(amenity['refundable_deposit']) > 0 ? ' + ${_money(amenity['refundable_deposit'])} refundable deposit' : ''}\n${amenity['booking_approval_required'] == true ? 'Approval required' : 'Instant confirmation'}',
              style: const TextStyle(color: SocietyColors.textMuted),
            ),
            if (amenity['instructions'] != null) ...[
              const SizedBox(height: 7),
              Text('Rules: ${amenity['instructions']}'),
            ],
            const SizedBox(height: 12),
            Align(
              alignment: Alignment.centerRight,
              child: resident
                  ? FilledButton.tonal(
                      onPressed: onBook,
                      child: const Text('Book now'),
                    )
                  : admin
                  ? TextButton.icon(
                      onPressed: onEdit,
                      icon: const Icon(Icons.edit),
                      label: const Text('Edit'),
                    )
                  : const SizedBox.shrink(),
            ),
          ],
        ),
      ),
    ),
  );
}

class _BookingCard extends StatelessWidget {
  const _BookingCard({
    required this.booking,
    required this.resident,
    required this.onPay,
    required this.onCash,
    required this.onAction,
  });
  final Map<String, dynamic> booking;
  final bool resident;
  final VoidCallback onPay, onCash;
  final ValueChanged<String> onAction;

  @override
  Widget build(BuildContext context) {
    final status = booking['status']?.toString() ?? '';
    final pendingPayment = status == 'PENDING_PAYMENT';
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(15),
        child: LayoutBuilder(
          builder: (context, constraints) {
            final details = Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        '${booking['amenity_name'] ?? 'Amenity'} · ${booking['booking_number']}',
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                    ),
                    Chip(label: Text(_label(status))),
                  ],
                ),
                Text(
                  '${_label(booking['amenity_type'])} · ${booking['amenity_code'] ?? '—'}\n${_dateTime(booking['starts_at'])} to ${_dateTime(booking['ends_at'])}\n${resident ? '' : '${booking['member_name'] ?? 'Member'} · '}${booking['attendees']} attendee(s) · ${_money(booking['amount'])} total · ${_label(booking['payment_status'])}',
                  style: const TextStyle(color: SocietyColors.textMuted),
                ),
                if (_number(booking['deposit_amount']) > 0)
                  Text(
                    'Charge ${_money(booking['base_charge'])} + refundable deposit ${_money(booking['deposit_amount'])}',
                  ),
                if (booking['purpose'] != null)
                  Text('Purpose: ${booking['purpose']}'),
                if (booking['payment_reference'] != null)
                  Text('Payment: ${booking['payment_reference']}'),
                if (booking['rejection_reason'] != null)
                  Text(
                    'Rejected: ${booking['rejection_reason']}',
                    style: const TextStyle(color: SocietyColors.danger),
                  ),
              ],
            );
            final actions = Wrap(
              spacing: 7,
              runSpacing: 7,
              children: [
                if (pendingPayment)
                  FilledButton.tonal(
                    onPressed: onPay,
                    child: const Text('Pay online'),
                  ),
                if (pendingPayment && !resident)
                  OutlinedButton(
                    onPressed: onCash,
                    child: const Text('Record cash'),
                  ),
                if (!resident && status == 'PENDING_APPROVAL') ...[
                  FilledButton.tonal(
                    onPressed: () => onAction('APPROVE'),
                    child: const Text('Approve'),
                  ),
                  TextButton(
                    onPressed: () => onAction('REJECT'),
                    child: const Text('Reject'),
                  ),
                ],
                if (!resident && status == 'CONFIRMED')
                  FilledButton.tonal(
                    onPressed: () => onAction('CHECK_IN'),
                    child: const Text('Check in'),
                  ),
                if (!resident && status == 'CHECKED_IN')
                  FilledButton.tonal(
                    onPressed: () => onAction('CHECK_OUT'),
                    child: const Text('Complete'),
                  ),
                if (!resident && booking['payment_status'] == 'REFUND_PENDING')
                  OutlinedButton(
                    onPressed: () => onAction('REFUND'),
                    child: const Text('Mark refunded'),
                  ),
                if (const {
                  'PENDING_PAYMENT',
                  'PENDING_APPROVAL',
                  'CONFIRMED',
                }.contains(status))
                  TextButton(
                    onPressed: () => onAction('CANCEL'),
                    child: const Text('Cancel'),
                  ),
              ],
            );
            if (constraints.maxWidth < 760) {
              return Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [details, const SizedBox(height: 12), actions],
              );
            }
            return Row(
              children: [
                Expanded(child: details),
                const SizedBox(width: 12),
                actions,
              ],
            );
          },
        ),
      ),
    );
  }
}

class _AmenityForm extends StatefulWidget {
  const _AmenityForm({required this.client, this.amenity});
  final BlocManagedApiGateway client;
  final Map<String, dynamic>? amenity;
  @override
  State<_AmenityForm> createState() => _AmenityFormState();
}

class _AmenityFormState extends State<_AmenityForm> {
  late final _name = _controller('name');
  late final _code = _controller('amenity_code');
  late final _description = _controller('description');
  late final _location = _controller('location');
  late final _capacity = _controller('capacity', fallback: '20');
  late final _charge = _controller('hourly_charge', fallback: '0');
  late final _bookingCharge = _controller('booking_charge', fallback: '0');
  late final _deposit = _controller('refundable_deposit', fallback: '0');
  late final _opens = _controller('opens_at', fallback: '06:00');
  late final _closes = _controller('closes_at', fallback: '22:00');
  late final _slot = _controller('slot_minutes', fallback: '60');
  late final _advance = _controller('advance_booking_days', fallback: '30');
  late final _cancel = _controller('cancellation_hours', fallback: '4');
  late final _instructions = _controller('instructions');
  late bool _approval = widget.amenity?['booking_approval_required'] == true;
  late bool _active = widget.amenity?['active'] != false;
  late String _type = widget.amenity?['amenity_type']?.toString() ?? 'OTHER';
  late String _pricing =
      widget.amenity?['pricing_type']?.toString() ?? 'HOURLY';
  bool _saving = false;
  String? _error;

  TextEditingController _controller(String key, {String fallback = ''}) =>
      TextEditingController(text: widget.amenity?[key]?.toString() ?? fallback);

  Future<void> _save() async {
    final capacity = int.tryParse(_capacity.text);
    final charge = double.tryParse(_charge.text);
    final bookingCharge = double.tryParse(_bookingCharge.text);
    final deposit = double.tryParse(_deposit.text);
    final slot = int.tryParse(_slot.text);
    final advance = int.tryParse(_advance.text);
    final cancel = int.tryParse(_cancel.text);
    if (_name.text.trim().isEmpty ||
        !RegExp(
          r'^[A-Z0-9][A-Z0-9_-]{1,39}$',
        ).hasMatch(_code.text.trim().toUpperCase()) ||
        capacity == null ||
        capacity < 1 ||
        charge == null ||
        charge < 0 ||
        bookingCharge == null ||
        bookingCharge < 0 ||
        deposit == null ||
        deposit < 0 ||
        slot == null ||
        slot < 15 ||
        advance == null ||
        advance < 1 ||
        cancel == null ||
        cancel < 0) {
      setState(
        () => _error = 'Complete all required fields with valid values.',
      );
      return;
    }
    setState(() {
      _saving = true;
      _error = null;
    });
    final body = {
      'name': _name.text.trim(),
      'amenityType': _type,
      'amenityCode': _code.text.trim().toUpperCase(),
      'description': _description.text.trim(),
      'location': _location.text.trim(),
      'capacity': capacity,
      'hourlyCharge': charge,
      'pricingType': _pricing,
      'bookingCharge': bookingCharge,
      'refundableDeposit': deposit,
      'opensAt': _opens.text.trim(),
      'closesAt': _closes.text.trim(),
      'bookingApprovalRequired': _approval,
      'slotMinutes': slot,
      'advanceBookingDays': advance,
      'cancellationHours': cancel,
      'instructions': _instructions.text.trim(),
      'active': _active,
    };
    try {
      if (widget.amenity == null) {
        await widget.client.post('/api/amenities', body);
      } else {
        await widget.client.put(
          '/api/amenities/${widget.amenity!['id']}',
          body,
        );
      }
      if (mounted) Navigator.pop(context, true);
    } catch (error) {
      if (mounted) setState(() => _error = error.toString());
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: Text(widget.amenity == null ? 'Add amenity' : 'Edit amenity'),
    content: SizedBox(
      width: 680,
      child: SingleChildScrollView(
        child: Wrap(
          spacing: 10,
          runSpacing: 10,
          children: [
            SizedBox(
              width: 310,
              child: DropdownButtonFormField<String>(
                isExpanded: true,
                initialValue: _type,
                decoration: const InputDecoration(labelText: 'Facility type'),
                items:
                    const [
                          'COMMUNITY_CENTER',
                          'GYM',
                          'SWIMMING_POOL',
                          'SPORTS_COURT',
                          'GUEST_ROOM',
                          'PLAY_AREA',
                          'OTHER',
                        ]
                        .map(
                          (value) => DropdownMenuItem(
                            value: value,
                            child: Text(_label(value)),
                          ),
                        )
                        .toList(),
                onChanged: (value) => setState(() => _type = value!),
              ),
            ),
            _field(_code, 'Unique instance code'),
            _field(_name, 'Amenity name'),
            _field(_location, 'Location'),
            _field(_capacity, 'Capacity', number: true),
            SizedBox(
              width: 310,
              child: DropdownButtonFormField<String>(
                isExpanded: true,
                initialValue: _pricing,
                decoration: const InputDecoration(labelText: 'Pricing method'),
                items: const ['FREE', 'HOURLY', 'PER_SLOT', 'FIXED']
                    .map(
                      (value) => DropdownMenuItem(
                        value: value,
                        child: Text(_label(value)),
                      ),
                    )
                    .toList(),
                onChanged: (value) => setState(() => _pricing = value!),
              ),
            ),
            if (_pricing == 'HOURLY')
              _field(_charge, 'Charge per hour', number: true),
            if (_pricing == 'PER_SLOT')
              _field(_bookingCharge, 'Charge per slot', number: true),
            if (_pricing == 'FIXED')
              _field(_bookingCharge, 'Fixed booking charge', number: true),
            _field(_deposit, 'Refundable deposit', number: true),
            _field(_opens, 'Opening time (HH:mm)'),
            _field(_closes, 'Closing time (HH:mm)'),
            _field(_slot, 'Slot duration (minutes)', number: true),
            _field(_advance, 'Advance booking days', number: true),
            _field(_cancel, 'Cancellation notice hours', number: true),
            SizedBox(
              width: 650,
              child: TextField(
                controller: _description,
                maxLines: 2,
                decoration: const InputDecoration(labelText: 'Description'),
              ),
            ),
            SizedBox(
              width: 650,
              child: TextField(
                controller: _instructions,
                maxLines: 2,
                maxLength: 500,
                decoration: const InputDecoration(
                  labelText: 'Rules and instructions',
                ),
              ),
            ),
            SizedBox(
              width: 310,
              child: SwitchListTile.adaptive(
                value: _approval,
                onChanged: (value) => setState(() => _approval = value),
                title: const Text('Admin approval required'),
              ),
            ),
            if (widget.amenity != null)
              SizedBox(
                width: 310,
                child: SwitchListTile.adaptive(
                  value: _active,
                  onChanged: (value) => setState(() => _active = value),
                  title: const Text('Active'),
                ),
              ),
            if (_error != null)
              SizedBox(
                width: 650,
                child: Text(
                  _error!,
                  style: const TextStyle(color: SocietyColors.danger),
                ),
              ),
          ],
        ),
      ),
    ),
    actions: [
      TextButton(
        onPressed: _saving ? null : () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(
        onPressed: _saving ? null : _save,
        child: Text(_saving ? 'Saving…' : 'Save amenity'),
      ),
    ],
  );

  Widget _field(
    TextEditingController controller,
    String label, {
    bool number = false,
  }) => SizedBox(
    width: 310,
    child: TextField(
      controller: controller,
      keyboardType: number ? TextInputType.number : null,
      decoration: InputDecoration(labelText: label),
    ),
  );
}

class _AmenityBookingForm extends StatefulWidget {
  const _AmenityBookingForm({required this.amenity});
  final Map<String, dynamic> amenity;
  @override
  State<_AmenityBookingForm> createState() => _AmenityBookingFormState();
}

class _AmenityBookingFormState extends State<_AmenityBookingForm> {
  DateTime _date = DateTime.now().add(const Duration(days: 1));
  late TimeOfDay _start =
      _parseTime(widget.amenity['opens_at']) ??
      const TimeOfDay(hour: 9, minute: 0);
  late TimeOfDay _end = _start.replacing(hour: (_start.hour + 1).clamp(0, 23));
  final _attendees = TextEditingController(text: '1');
  final _purpose = TextEditingController();
  String? _error;

  @override
  void dispose() {
    _attendees.dispose();
    _purpose.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final start = DateTime(
      _date.year,
      _date.month,
      _date.day,
      _start.hour,
      _start.minute,
    );
    final end = DateTime(
      _date.year,
      _date.month,
      _date.day,
      _end.hour,
      _end.minute,
    );
    final minutes = end.difference(start).inMinutes;
    final baseCharge = minutes > 0
        ? _amenityBaseCharge(widget.amenity, minutes)
        : 0;
    final deposit = _number(widget.amenity['refundable_deposit']);
    final amount = baseCharge + deposit;
    return AlertDialog(
      title: Text('Book ${widget.amenity['name']}'),
      content: SizedBox(
        width: 540,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              title: Text('${_money(amount)} estimated charge'),
              subtitle: Text(
                '${_pricingSummary(widget.amenity)} · ${widget.amenity['slot_minutes']} minute slots\nBase ${_money(baseCharge)}${deposit > 0 ? ' + refundable deposit ${_money(deposit)}' : ''} · Book up to ${widget.amenity['advance_booking_days']} days ahead',
              ),
            ),
            OutlinedButton.icon(
              onPressed: () async {
                final value = await showDatePicker(
                  context: context,
                  initialDate: _date,
                  firstDate: DateTime.now().add(const Duration(days: 1)),
                  lastDate: DateTime.now().add(
                    Duration(
                      days:
                          int.tryParse(
                            widget.amenity['advance_booking_days'].toString(),
                          ) ??
                          30,
                    ),
                  ),
                );
                if (value != null) setState(() => _date = value);
              },
              icon: const Icon(Icons.event),
              label: Text(_dateOnly(_date)),
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: _timeButton(
                    'Start',
                    _start,
                    (value) => _start = value,
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: _timeButton('End', _end, (value) => _end = value),
                ),
              ],
            ),
            const SizedBox(height: 10),
            TextField(
              controller: _attendees,
              keyboardType: TextInputType.number,
              decoration: InputDecoration(
                labelText: 'Number of attendees',
                helperText: 'Capacity: ${widget.amenity['capacity'] ?? '—'}',
              ),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: _purpose,
              maxLength: 240,
              decoration: const InputDecoration(
                labelText: 'Purpose (optional)',
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
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Cancel'),
        ),
        FilledButton(
          onPressed: () {
            final attendees = int.tryParse(_attendees.text);
            if (!end.isAfter(start) || attendees == null || attendees < 1) {
              setState(
                () => _error = 'Choose a valid time and attendee count.',
              );
              return;
            }
            Navigator.pop(context, {
              'startsAt': start.toUtc().toIso8601String(),
              'endsAt': end.toUtc().toIso8601String(),
              'attendees': attendees,
              if (_purpose.text.trim().isNotEmpty)
                'purpose': _purpose.text.trim(),
            });
          },
          child: const Text('Create booking'),
        ),
      ],
    );
  }

  Widget _timeButton(
    String label,
    TimeOfDay value,
    ValueChanged<TimeOfDay> assign,
  ) => OutlinedButton.icon(
    onPressed: () async {
      final picked = await showTimePicker(context: context, initialTime: value);
      if (picked != null) setState(() => assign(picked));
    },
    icon: const Icon(Icons.schedule),
    label: Text('$label ${value.format(context)}'),
  );
}

class _AmenityCheckoutDialog extends StatefulWidget {
  const _AmenityCheckoutDialog({required this.booking});
  final Map<String, dynamic> booking;
  @override
  State<_AmenityCheckoutDialog> createState() => _AmenityCheckoutDialogState();
}

class _AmenityCheckoutDialogState extends State<_AmenityCheckoutDialog> {
  String _method = 'UPI';
  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Razorpay amenity payment'),
    content: SizedBox(
      width: 480,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Chip(label: Text('TEST MODE · No real money charged')),
          const SizedBox(height: 10),
          Text(
            _money(widget.booking['amount']),
            style: Theme.of(context).textTheme.headlineMedium,
          ),
          Text(
            '${widget.booking['amenity_name']} · ${widget.booking['booking_number']}',
          ),
          const SizedBox(height: 14),
          DropdownButtonFormField<String>(
            isExpanded: true,
            initialValue: _method,
            decoration: const InputDecoration(labelText: 'Payment method'),
            items: const [
              DropdownMenuItem(value: 'UPI', child: Text('UPI')),
              DropdownMenuItem(
                value: 'CARD',
                child: Text('Credit / debit card'),
              ),
              DropdownMenuItem(
                value: 'NET_BANKING',
                child: Text('Net banking'),
              ),
            ],
            onChanged: (value) => setState(() => _method = value!),
          ),
        ],
      ),
    ),
    actions: [
      TextButton(
        onPressed: () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton.icon(
        onPressed: () => Navigator.pop(context, _method),
        icon: const Icon(Icons.lock),
        label: Text('Pay ${_money(widget.booking['amount'])}'),
      ),
    ],
  );
}

class _AmenityEmpty extends StatelessWidget {
  const _AmenityEmpty(this.text);
  final String text;
  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(28),
      child: Center(child: Text(text)),
    ),
  );
}

List<WorkspaceRecord> _maps(Object? value) => workspaceRecords(value);
double _number(Object? value) => double.tryParse(value?.toString() ?? '') ?? 0;
String _money(Object? value) => '₹${_number(value).toStringAsFixed(2)}';
String _pricingSummary(Map<String, dynamic> amenity) =>
    switch (amenity['pricing_type']?.toString()) {
      'FREE' => 'Free booking',
      'FIXED' => '${_money(amenity['booking_charge'])} per booking',
      'PER_SLOT' => '${_money(amenity['booking_charge'])} per slot',
      _ => '${_money(amenity['hourly_charge'])} per hour',
    };
double _amenityBaseCharge(Map<String, dynamic> amenity, int minutes) =>
    switch (amenity['pricing_type']?.toString()) {
      'FREE' => 0,
      'FIXED' => _number(amenity['booking_charge']),
      'PER_SLOT' =>
        _number(amenity['booking_charge']) *
            (minutes /
                (_number(amenity['slot_minutes']) == 0
                    ? 60
                    : _number(amenity['slot_minutes']))),
      _ => _number(amenity['hourly_charge']) * minutes / 60,
    };
String _label(Object? value) => (value?.toString() ?? '')
    .toLowerCase()
    .split('_')
    .map(
      (part) =>
          part.isEmpty ? '' : '${part[0].toUpperCase()}${part.substring(1)}',
    )
    .join(' ');
String _time(Object? value) {
  final text = value?.toString();
  return text == null || text.isEmpty
      ? '—'
      : text.substring(0, text.length.clamp(0, 5));
}

TimeOfDay? _parseTime(Object? value) {
  final parts = value?.toString().split(':');
  if (parts == null || parts.length < 2) return null;
  final hour = int.tryParse(parts[0]);
  final minute = int.tryParse(parts[1]);
  return hour == null || minute == null
      ? null
      : TimeOfDay(hour: hour, minute: minute);
}

String _dateOnly(DateTime value) =>
    '${value.day.toString().padLeft(2, '0')}/${value.month.toString().padLeft(2, '0')}/${value.year}';
String _dateTime(Object? value) {
  final date = DateTime.tryParse(value?.toString() ?? '')?.toLocal();
  if (date == null) return '—';
  final hour = date.hour % 12 == 0 ? 12 : date.hour % 12;
  final period = date.hour >= 12 ? 'PM' : 'AM';
  return '${_dateOnly(date)} $hour:${date.minute.toString().padLeft(2, '0')} $period';
}
