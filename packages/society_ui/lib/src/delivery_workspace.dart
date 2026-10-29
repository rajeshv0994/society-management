import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';
import 'async_data_bloc.dart';
import 'theme.dart';
import 'workspace_record.dart';

enum DeliveryWorkspaceRole { societyAdmin, staff, resident }

Widget buildResidentDeliveryWorkspace(BlocManagedApiGateway client) =>
    DeliveryWorkspace(client: client, role: DeliveryWorkspaceRole.resident);

Widget buildStaffDeliveryWorkspace(BlocManagedApiGateway client) =>
    DeliveryWorkspace(client: client, role: DeliveryWorkspaceRole.staff);

class DeliveryWorkspace extends StatefulWidget {
  const DeliveryWorkspace({
    super.key,
    required this.client,
    required this.role,
  });
  final BlocManagedApiGateway client;
  final DeliveryWorkspaceRole role;

  @override
  State<DeliveryWorkspace> createState() => _DeliveryWorkspaceState();
}

class _DeliveryWorkspaceState extends State<DeliveryWorkspace> {
  late final AsyncDataBloc<Map<String, dynamic>> _bloc;
  Map<String, dynamic> get _data => _bloc.state.data ?? const {};
  String _status = 'ALL';

  bool get _resident => widget.role == DeliveryWorkspaceRole.resident;
  bool get _manager => !_resident;

  @override
  void initState() {
    super.initState();
    _bloc = AsyncDataBloc(
      () async => Map<String, dynamic>.unmodifiable(
        Map<String, dynamic>.from(
          await widget.client.get('/api/deliveries') as Map,
        ),
      ),
    );
    _load();
  }

  Future<void> _load() => _bloc.refresh();

  @override
  void dispose() {
    _bloc.close();
    super.dispose();
  }

  Future<void> _create() async {
    final changed = await showDialog<bool>(
      context: context,
      builder: (_) => _DeliveryForm(
        client: widget.client,
        options: _maps(_data['options']),
        manager: _manager,
      ),
    );
    if (changed == true) await _load();
  }

  Future<void> _action(Map<String, dynamic> item, String action) async {
    String? note;
    String? collector;
    String? code;
    if (const {'REJECT', 'RETURN', 'DAMAGED', 'LOST'}.contains(action)) {
      note = await _textDialog(
        '${_label(action)} delivery',
        'Reason or details',
      );
      if (note == null) return;
    } else if (action == 'AUTHORIZE') {
      collector = await _textDialog(
        'Authorize collection',
        'Collector full name',
      );
      if (collector == null) return;
    } else if (action == 'HANDOVER') {
      final value = await showDialog<List<String>>(
        context: context,
        builder: (_) => const _HandoverDialog(),
      );
      if (value == null) return;
      code = value.first;
      collector = value.last;
    } else if (!await _confirm(
      '${_label(action)} delivery',
      '${_label(action)} ${item['delivery_number']} from ${item['provider']}?',
      _label(action),
    )) {
      return;
    }
    try {
      await widget.client.patch('/api/deliveries/${item['id']}', {
        'action': action,
        if (note != null) 'note': note,
        if (collector != null) 'collectorName': collector,
        if (code != null) 'pickupCode': code,
      });
      await _load();
      _message('Delivery updated.');
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _checkout(Map<String, dynamic> item) async {
    final method = await showDialog<String>(
      context: context,
      builder: (_) => _DeliveryCheckoutDialog(item: item),
    );
    if (method == null) return;
    try {
      final paid = Map<String, dynamic>.from(
        await widget.client.post('/api/deliveries/${item['id']}/checkout', {
              'paymentMethod': method,
            })
            as Map,
      );
      await _load();
      _message('COD paid: ${paid['cod_payment_reference']}');
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _recordCod(Map<String, dynamic> item) async {
    final method = await showDialog<String>(
      context: context,
      builder: (_) => const _CodDialog(),
    );
    if (method == null) return;
    try {
      final paid = Map<String, dynamic>.from(
        await widget.client.post('/api/deliveries/${item['id']}/cod', {
              'paymentMethod': method,
            })
            as Map,
      );
      await _load();
      _message('COD recorded: ${paid['cod_payment_reference']}');
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _history(Map<String, dynamic> item) async {
    try {
      final rows = _maps(
        await widget.client.get('/api/deliveries/${item['id']}/history'),
      );
      if (!mounted) return;
      await showDialog<void>(
        context: context,
        builder: (_) => AlertDialog(
          title: Text('History · ${item['delivery_number']}'),
          content: SizedBox(
            width: 620,
            child: rows.isEmpty
                ? const Text('No events recorded.')
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
                          '${_label(row['from_status'])} → ${_label(row['to_status'])}\n${row['actor_name'] ?? _label(row['actor_role'])} · ${_dateTime(row['occurred_at'])}${row['note'] == null ? '' : '\n${row['note']}'}',
                        ),
                      );
                    },
                  ),
          ),
          actions: [
            FilledButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Close'),
            ),
          ],
        ),
      );
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<bool> _confirm(String title, String message, String action) async =>
      await showDialog<bool>(
        context: context,
        builder: (dialog) => AlertDialog(
          title: Text(title),
          content: Text(message),
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

  Future<String?> _textDialog(String title, String label) async {
    final controller = TextEditingController();
    final result = await showDialog<String>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: Text(title),
        content: TextField(
          controller: controller,
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

  void _message(String text) {
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
                onPressed: _load,
                icon: const Icon(Icons.refresh),
                label: Text(state.error.toString()),
              ),
            );
          }
          final deliveries = _maps(_data['deliveries']);
          final visible = deliveries
              .where((item) => _status == 'ALL' || item['status'] == _status)
              .toList();
          return RefreshIndicator(
            onRefresh: _load,
            child: ListView(
              padding: const EdgeInsets.all(20),
              children: [
                Wrap(
                  spacing: 12,
                  runSpacing: 10,
                  alignment: WrapAlignment.spaceBetween,
                  crossAxisAlignment: WrapCrossAlignment.center,
                  children: [
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          _resident ? 'My deliveries' : 'Delivery management',
                          style: Theme.of(context).textTheme.headlineMedium,
                        ),
                        Text(
                          _resident
                              ? 'Pre-register parcels, respond to arrivals, pay COD, and collect securely.'
                              : 'Receive, notify, store, hand over, deliver, return, and audit parcels.',
                          style: const TextStyle(
                            color: SocietyColors.textMuted,
                          ),
                        ),
                      ],
                    ),
                    FilledButton.icon(
                      onPressed: _create,
                      icon: const Icon(Icons.add_box_outlined),
                      label: Text(
                        _resident ? 'Expect delivery' : 'Log delivery',
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                Wrap(
                  spacing: 12,
                  runSpacing: 12,
                  children: [
                    _DeliveryMetric(
                      'Total',
                      '${_data['total'] ?? 0}',
                      Icons.inventory_2_outlined,
                    ),
                    _DeliveryMetric(
                      'Awaiting action',
                      '${_data['awaiting_action'] ?? 0}',
                      Icons.pending_actions,
                    ),
                    _DeliveryMetric(
                      'Ready for pickup',
                      '${_data['ready_for_pickup'] ?? 0}',
                      Icons.local_shipping_outlined,
                    ),
                    _DeliveryMetric(
                      'Completed',
                      '${_data['completed'] ?? 0}',
                      Icons.task_alt,
                    ),
                    if ((_data['pending_cod'] ?? 0) != 0)
                      _DeliveryMetric(
                        'Pending COD',
                        '${_data['pending_cod']}',
                        Icons.payments_outlined,
                      ),
                  ],
                ),
                const SizedBox(height: 18),
                Align(
                  alignment: Alignment.centerRight,
                  child: SizedBox(
                    width: 240,
                    child: DropdownButtonFormField<String>(
                      isExpanded: true,
                      initialValue: _status,
                      decoration: const InputDecoration(labelText: 'Status'),
                      items: [
                        for (final value in const [
                          'ALL',
                          'EXPECTED',
                          'RECEIVED',
                          'READY_FOR_PICKUP',
                          'OUT_FOR_DELIVERY',
                          'COLLECTED',
                          'DELIVERED',
                          'REJECTED',
                          'RETURNED',
                          'CANCELLED',
                          'DAMAGED',
                          'LOST',
                        ])
                          DropdownMenuItem(
                            value: value,
                            child: Text(_label(value)),
                          ),
                      ],
                      onChanged: (value) => setState(() => _status = value!),
                    ),
                  ),
                ),
                const SizedBox(height: 10),
                if (visible.isEmpty)
                  const _DeliveryEmpty('No deliveries match this status.')
                else
                  for (final item in visible) ...[
                    _DeliveryCard(
                      item: item,
                      resident: _resident,
                      onAction: (action) => _action(item, action),
                      onCheckout: () => _checkout(item),
                      onRecordCod: () => _recordCod(item),
                      onHistory: () => _history(item),
                    ),
                    const SizedBox(height: 8),
                  ],
              ],
            ),
          );
        },
      );
}

class _DeliveryCard extends StatelessWidget {
  const _DeliveryCard({
    required this.item,
    required this.resident,
    required this.onAction,
    required this.onCheckout,
    required this.onRecordCod,
    required this.onHistory,
  });
  final Map<String, dynamic> item;
  final bool resident;
  final ValueChanged<String> onAction;
  final VoidCallback onCheckout, onRecordCod, onHistory;

  @override
  Widget build(BuildContext context) {
    final status = item['status']?.toString() ?? '';
    final codPending = item['cod_payment_status'] == 'PENDING';
    final details = Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Wrap(
          spacing: 8,
          runSpacing: 6,
          crossAxisAlignment: WrapCrossAlignment.center,
          children: [
            Text(
              '${item['provider']} · ${item['delivery_number']}',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            Chip(label: Text(_label(status))),
            if (item['fragile'] == true) const Chip(label: Text('Fragile')),
          ],
        ),
        Text(
          '${_label(item['parcel_type'])} · ${item['package_count']} package(s) · ${_label(item['parcel_size'])}${item['tracking_number'] == null ? '' : ' · Tracking ${item['tracking_number']}'}',
        ),
        if (!resident)
          Text(
            '${item['resident_name'] ?? 'Resident'} · ${item['block'] ?? ''} ${item['space_number'] ?? ''} · ${item['resident_mobile'] ?? ''}',
          ),
        Text(
          'Sender ${item['sender_name'] ?? '—'} · Storage ${item['storage_location'] ?? '—'} · ${_dateTime(item['expected_at'] ?? item['received_at'] ?? item['created_at'])}',
          style: const TextStyle(color: SocietyColors.textMuted),
        ),
        if (_number(item['cod_amount']) > 0)
          Text(
            'COD ${_money(item['cod_amount'])} · ${_label(item['cod_payment_status'])}${item['cod_payment_method'] == null ? '' : ' · ${_label(item['cod_payment_method'])}'}',
          ),
        if (resident && item['pickup_code'] != null)
          SelectableText(
            'Pickup code: ${item['pickup_code']}',
            style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 17),
          ),
        if (item['authorized_collector'] != null)
          Text('Authorized collector: ${item['authorized_collector']}'),
        if (item['closed_reason'] != null)
          Text(
            'Note: ${item['closed_reason']}',
            style: const TextStyle(color: SocietyColors.danger),
          ),
      ],
    );
    final actions = Wrap(
      spacing: 6,
      runSpacing: 6,
      children: [
        if (resident && status == 'EXPECTED')
          TextButton(
            onPressed: () => onAction('CANCEL'),
            child: const Text('Cancel'),
          ),
        if (resident && status == 'RECEIVED') ...[
          FilledButton.tonal(
            onPressed: () => onAction('ACCEPT'),
            child: const Text('Accept'),
          ),
          TextButton(
            onPressed: () => onAction('REJECT'),
            child: const Text('Reject'),
          ),
          TextButton(
            onPressed: () => onAction('AUTHORIZE'),
            child: const Text('Authorize pickup'),
          ),
        ],
        if (resident && codPending)
          FilledButton.tonal(
            onPressed: onCheckout,
            child: const Text('Pay COD online'),
          ),
        if (!resident && status == 'EXPECTED')
          FilledButton.tonal(
            onPressed: () => onAction('RECEIVE'),
            child: const Text('Receive'),
          ),
        if (!resident && status == 'RECEIVED')
          FilledButton.tonal(
            onPressed: () => onAction('MARK_READY'),
            child: const Text('Mark ready'),
          ),
        if (!resident && codPending)
          OutlinedButton(
            onPressed: onRecordCod,
            child: const Text('Record COD'),
          ),
        if (!resident &&
            const {'RECEIVED', 'READY_FOR_PICKUP'}.contains(status)) ...[
          FilledButton.tonal(
            onPressed: () => onAction('HANDOVER'),
            child: const Text('Handover'),
          ),
          TextButton(
            onPressed: () => onAction('OUT_FOR_DELIVERY'),
            child: const Text('Send to unit'),
          ),
          TextButton(
            onPressed: () => onAction('DELIVER'),
            child: const Text('Deliver now'),
          ),
          TextButton(
            onPressed: () => onAction('DAMAGED'),
            child: const Text('Damaged'),
          ),
          TextButton(
            onPressed: () => onAction('LOST'),
            child: const Text('Lost'),
          ),
        ],
        if (!resident && status == 'OUT_FOR_DELIVERY') ...[
          FilledButton.tonal(
            onPressed: () => onAction('DELIVER'),
            child: const Text('Confirm delivered'),
          ),
          TextButton(
            onPressed: () => onAction('LOST'),
            child: const Text('Lost'),
          ),
        ],
        if (!resident &&
            const {
              'RECEIVED',
              'READY_FOR_PICKUP',
              'REJECTED',
              'DAMAGED',
            }.contains(status))
          TextButton(
            onPressed: () => onAction('RETURN'),
            child: const Text('Return'),
          ),
        TextButton.icon(
          onPressed: onHistory,
          icon: const Icon(Icons.history, size: 18),
          label: const Text('History'),
        ),
      ],
    );
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(15),
        child: LayoutBuilder(
          builder: (context, constraints) {
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
                Flexible(child: actions),
              ],
            );
          },
        ),
      ),
    );
  }
}

class _DeliveryForm extends StatefulWidget {
  const _DeliveryForm({
    required this.client,
    required this.options,
    required this.manager,
  });
  final BlocManagedApiGateway client;
  final List<Map<String, dynamic>> options;
  final bool manager;

  @override
  State<_DeliveryForm> createState() => _DeliveryFormState();
}

class _DeliveryFormState extends State<_DeliveryForm> {
  late String? _option = widget.options.firstOrNull?['flat_id']?.toString();
  final _provider = TextEditingController();
  final _tracking = TextEditingController();
  final _sender = TextEditingController();
  final _person = TextEditingController();
  final _mobile = TextEditingController();
  final _count = TextEditingController(text: '1');
  final _storage = TextEditingController(text: 'Security desk');
  final _cod = TextEditingController(text: '0');
  final _notes = TextEditingController();
  late String _parcelType = 'PACKAGE';
  late String _size = 'MEDIUM';
  bool _fragile = false;
  bool _receivedNow = false;
  bool _saving = false;
  String? _error;

  @override
  void dispose() {
    for (final controller in [
      _provider,
      _tracking,
      _sender,
      _person,
      _mobile,
      _count,
      _storage,
      _cod,
      _notes,
    ]) {
      controller.dispose();
    }
    super.dispose();
  }

  Map<String, dynamic>? get _selected =>
      widget.options.cast<Map<String, dynamic>?>().firstWhere(
        (row) => row?['flat_id']?.toString() == _option,
        orElse: () => null,
      );

  Future<void> _save() async {
    final count = int.tryParse(_count.text);
    final cod = double.tryParse(_cod.text);
    if (_option == null ||
        _provider.text.trim().isEmpty ||
        count == null ||
        count < 1 ||
        cod == null ||
        cod < 0) {
      setState(
        () => _error =
            'Unit, provider, package count, and COD amount must be valid.',
      );
      return;
    }
    setState(() {
      _saving = true;
      _error = null;
    });
    final body = {
      if (widget.manager) 'residentId': _selected?['resident_id'],
      'flatId': _option,
      'trackingNumber': _tracking.text.trim(),
      'provider': _provider.text.trim(),
      'senderName': _sender.text.trim(),
      'deliveryPersonName': _person.text.trim(),
      'deliveryPersonMobile': _mobile.text.trim(),
      'parcelType': _parcelType,
      'packageCount': count,
      'parcelSize': _size,
      'fragile': _fragile,
      'storageLocation': _storage.text.trim(),
      if (!_receivedNow)
        'expectedAt': DateTime.now()
            .add(const Duration(days: 1))
            .toUtc()
            .toIso8601String(),
      'notes': _notes.text.trim(),
      'codAmount': cod,
      'receivedNow': widget.manager && _receivedNow,
    };
    try {
      await widget.client.post('/api/deliveries', body);
      if (mounted) Navigator.pop(context, true);
    } catch (error) {
      if (mounted) setState(() => _error = error.toString());
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: Text(widget.manager ? 'Log delivery' : 'Expect delivery'),
    content: SizedBox(
      width: 700,
      child: SingleChildScrollView(
        child: Wrap(
          spacing: 10,
          runSpacing: 10,
          children: [
            SizedBox(
              width: 335,
              child: DropdownButtonFormField<String>(
                isExpanded: true,
                initialValue: _option,
                decoration: const InputDecoration(
                  labelText: 'Resident and unit',
                ),
                items: [
                  for (final row in widget.options)
                    DropdownMenuItem(
                      value: row['flat_id'].toString(),
                      child: Text(
                        '${row['resident_name']} · ${row['block'] ?? ''} ${row['space_number']}',
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                ],
                onChanged: (value) => setState(() => _option = value),
              ),
            ),
            _field(_provider, 'Courier/provider'),
            _field(_tracking, 'Tracking number'),
            _field(_sender, 'Sender/store'),
            _field(_person, 'Delivery person'),
            _field(_mobile, 'Delivery person mobile'),
            _dropdown('Parcel type', _parcelType, const [
              'DOCUMENT',
              'PACKAGE',
              'FOOD',
              'GROCERY',
              'MEDICINE',
              'OTHER',
            ], (value) => _parcelType = value),
            _dropdown('Parcel size', _size, const [
              'SMALL',
              'MEDIUM',
              'LARGE',
              'OVERSIZED',
            ], (value) => _size = value),
            _field(_count, 'Package count', number: true),
            _field(_cod, 'COD amount', number: true),
            _field(_storage, 'Storage location'),
            SizedBox(
              width: 335,
              child: SwitchListTile.adaptive(
                value: _fragile,
                onChanged: (value) => setState(() => _fragile = value),
                title: const Text('Fragile package'),
              ),
            ),
            if (widget.manager)
              SizedBox(
                width: 335,
                child: SwitchListTile.adaptive(
                  value: _receivedNow,
                  onChanged: (value) => setState(() => _receivedNow = value),
                  title: const Text('Already received at gate'),
                ),
              ),
            SizedBox(
              width: 680,
              child: TextField(
                controller: _notes,
                maxLines: 2,
                maxLength: 500,
                decoration: const InputDecoration(labelText: 'Notes'),
              ),
            ),
            if (_error != null)
              SizedBox(
                width: 680,
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
        child: Text(_saving ? 'Saving…' : 'Save delivery'),
      ),
    ],
  );

  Widget _field(
    TextEditingController controller,
    String label, {
    bool number = false,
  }) => SizedBox(
    width: 335,
    child: TextField(
      controller: controller,
      keyboardType: number ? TextInputType.number : null,
      decoration: InputDecoration(labelText: label),
    ),
  );

  Widget _dropdown(
    String label,
    String value,
    List<String> values,
    ValueChanged<String> assign,
  ) => SizedBox(
    width: 335,
    child: DropdownButtonFormField<String>(
      isExpanded: true,
      initialValue: value,
      decoration: InputDecoration(labelText: label),
      items: [
        for (final item in values)
          DropdownMenuItem(value: item, child: Text(_label(item))),
      ],
      onChanged: (newValue) => setState(() => assign(newValue!)),
    ),
  );
}

class _HandoverDialog extends StatefulWidget {
  const _HandoverDialog();
  @override
  State<_HandoverDialog> createState() => _HandoverDialogState();
}

class _HandoverDialogState extends State<_HandoverDialog> {
  final code = TextEditingController();
  final collector = TextEditingController();
  String? error;

  @override
  void dispose() {
    code.dispose();
    collector.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Secure parcel handover'),
    content: Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        TextField(
          controller: code,
          maxLength: 6,
          keyboardType: TextInputType.number,
          decoration: const InputDecoration(labelText: 'Resident pickup code'),
        ),
        TextField(
          controller: collector,
          decoration: const InputDecoration(labelText: 'Collector name'),
        ),
        if (error != null)
          Text(error!, style: const TextStyle(color: SocietyColors.danger)),
      ],
    ),
    actions: [
      TextButton(
        onPressed: () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(
        onPressed: () {
          if (!RegExp(r'^[0-9]{6}$').hasMatch(code.text) ||
              collector.text.trim().isEmpty) {
            setState(
              () => error = 'Enter the 6-digit code and collector name.',
            );
            return;
          }
          Navigator.pop(context, [code.text, collector.text.trim()]);
        },
        child: const Text('Confirm handover'),
      ),
    ],
  );
}

class _DeliveryCheckoutDialog extends StatefulWidget {
  const _DeliveryCheckoutDialog({required this.item});
  final Map<String, dynamic> item;
  @override
  State<_DeliveryCheckoutDialog> createState() =>
      _DeliveryCheckoutDialogState();
}

class _DeliveryCheckoutDialogState extends State<_DeliveryCheckoutDialog> {
  String method = 'UPI';
  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Razorpay COD payment'),
    content: Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Chip(label: Text('TEST MODE · No real money charged')),
        Text(
          _money(widget.item['cod_amount']),
          style: Theme.of(context).textTheme.headlineMedium,
        ),
        Text('${widget.item['provider']} · ${widget.item['delivery_number']}'),
        const SizedBox(height: 12),
        DropdownButtonFormField<String>(
          isExpanded: true,
          initialValue: method,
          decoration: const InputDecoration(labelText: 'Payment method'),
          items: const [
            DropdownMenuItem(value: 'UPI', child: Text('UPI')),
            DropdownMenuItem(value: 'CARD', child: Text('Credit / debit card')),
            DropdownMenuItem(value: 'NET_BANKING', child: Text('Net banking')),
          ],
          onChanged: (value) => setState(() => method = value!),
        ),
      ],
    ),
    actions: [
      TextButton(
        onPressed: () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(
        onPressed: () => Navigator.pop(context, method),
        child: Text('Pay ${_money(widget.item['cod_amount'])}'),
      ),
    ],
  );
}

class _CodDialog extends StatefulWidget {
  const _CodDialog();
  @override
  State<_CodDialog> createState() => _CodDialogState();
}

class _CodDialogState extends State<_CodDialog> {
  String method = 'CASH';
  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Record COD collection'),
    content: DropdownButtonFormField<String>(
      isExpanded: true,
      initialValue: method,
      decoration: const InputDecoration(labelText: 'Collection method'),
      items: const [
        DropdownMenuItem(value: 'CASH', child: Text('Cash')),
        DropdownMenuItem(value: 'UPI', child: Text('UPI collected at desk')),
        DropdownMenuItem(value: 'CARD', child: Text('Card collected at desk')),
      ],
      onChanged: (value) => setState(() => method = value!),
    ),
    actions: [
      TextButton(
        onPressed: () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(
        onPressed: () => Navigator.pop(context, method),
        child: const Text('Record paid'),
      ),
    ],
  );
}

class _DeliveryMetric extends StatelessWidget {
  const _DeliveryMetric(this.label, this.value, this.icon);
  final String label, value;
  final IconData icon;
  @override
  Widget build(BuildContext context) => SizedBox(
    width: 200,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
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

class _DeliveryEmpty extends StatelessWidget {
  const _DeliveryEmpty(this.message);
  final String message;
  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(26),
      child: Center(child: Text(message)),
    ),
  );
}

List<WorkspaceRecord> _maps(Object? value) => workspaceRecords(value);
double _number(Object? value) => double.tryParse(value?.toString() ?? '') ?? 0;
String _money(Object? value) => '₹${_number(value).toStringAsFixed(2)}';
String _label(Object? value) => (value?.toString() ?? '')
    .toLowerCase()
    .split('_')
    .map(
      (part) =>
          part.isEmpty ? '' : '${part[0].toUpperCase()}${part.substring(1)}',
    )
    .join(' ');
String _dateTime(Object? value) {
  final date = DateTime.tryParse(value?.toString() ?? '')?.toLocal();
  if (date == null) return '—';
  return '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year} ${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
}
