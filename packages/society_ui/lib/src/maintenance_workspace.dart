import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';
import 'async_data_bloc.dart';
import 'file_download.dart';
import 'theme.dart';
import 'workspace_record.dart';

enum MaintenanceRole { societyAdmin, staff, resident }

enum MaintenanceView { invoices, payments }

Widget buildStaffMaintenanceWorkspace(BlocManagedApiGateway client) =>
    MaintenanceWorkspace(client: client, role: MaintenanceRole.staff);

Widget buildResidentMaintenanceWorkspace(BlocManagedApiGateway client) =>
    MaintenanceWorkspace(client: client, role: MaintenanceRole.resident);

class MaintenanceWorkspace extends StatefulWidget {
  const MaintenanceWorkspace({
    super.key,
    required this.client,
    required this.role,
    this.initialView = MaintenanceView.invoices,
    this.fixedView,
  });

  final BlocManagedApiGateway client;
  final MaintenanceRole role;
  final MaintenanceView initialView;
  final MaintenanceView? fixedView;

  @override
  State<MaintenanceWorkspace> createState() => _MaintenanceWorkspaceState();
}

class _MaintenanceWorkspaceState extends State<MaintenanceWorkspace> {
  late final AsyncDataBloc<_MaintenanceData> _bloc;
  List<Map<String, dynamic>> get _invoices =>
      _bloc.state.data?.invoices ?? const [];
  List<Map<String, dynamic>> get _payments =>
      _bloc.state.data?.payments ?? const [];
  List<Map<String, dynamic>> get _chargeSettings =>
      _bloc.state.data?.chargeSettings ?? const [];
  Map<String, dynamic> get _options => _bloc.state.data?.options ?? const {};
  late MaintenanceView _view = widget.fixedView ?? widget.initialView;
  String _query = '';
  String _status = 'ALL';

  bool get _isResident => widget.role == MaintenanceRole.resident;
  bool get _isAdmin => widget.role == MaintenanceRole.societyAdmin;
  bool get _isBillingPage =>
      widget.fixedView == MaintenanceView.invoices && !_isResident;
  bool get _isPaymentsPage =>
      widget.fixedView == MaintenanceView.payments && !_isResident;

  @override
  void initState() {
    super.initState();
    _bloc = AsyncDataBloc(() => _MaintenanceData.load(widget.client));
    _load();
  }

  Future<void> _load() => _bloc.refresh();

  @override
  void dispose() {
    _bloc.close();
    super.dispose();
  }

  Future<void> _createInvoice() async {
    final created = await showDialog<bool>(
      context: context,
      builder: (_) => _InvoiceDialog(
        client: widget.client,
        options: _options,
        chargeSettings: _chargeSettings,
      ),
    );
    if (created == true) {
      _message('Society bill created.');
      await _load();
    }
  }

  Future<void> _manageChargeSettings() async {
    final changed = await showDialog<bool>(
      context: context,
      builder: (_) => _ChargeSettingsDialog(
        client: widget.client,
        settings: _chargeSettings,
        options: _options,
      ),
    );
    if (changed == true) await _load();
  }

  Future<void> _configureOnlinePayment() async {
    final changed = await showDialog<bool>(
      context: context,
      builder: (_) => _SocietyPaymentSettingsDialog(
        client: widget.client,
        settings: Map<String, dynamic>.from(
          _options['online_payment'] as Map? ?? const {},
        ),
      ),
    );
    if (changed == true) await _load();
  }

  Future<void> _pay(
    Map<String, dynamic> invoice, {
    String? initialMethod,
  }) async {
    final payment = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => _PaymentDialog(
        invoice: invoice,
        resident: _isResident,
        initialMethod: initialMethod,
        onlinePayment: Map<String, dynamic>.from(
          _options['online_payment'] as Map? ?? const {},
        ),
      ),
    );
    if (payment == null) return;
    try {
      final online = payment.remove('onlineCheckout') == true;
      final result = Map<String, dynamic>.from(
        await widget.client.post(
              '/api/billing/invoices/${invoice['id']}/${online ? 'checkout' : 'payments'}',
              payment,
            )
            as Map,
      );
      if (!mounted) return;
      _message(
        'Payment successful. Receipt ${result['receiptNumber']} created.',
      );
      await _load();
      await _receipt(result['paymentId'].toString());
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _choosePayment([String? initialMethod]) async {
    final unpaid = _invoices
        .where((invoice) => _number(invoice['outstanding']) > 0)
        .toList();
    if (unpaid.isEmpty) {
      _message(
        _isResident
            ? 'You have no outstanding society bill to pay.'
            : 'There are no outstanding invoices available for collection.',
      );
      return;
    }
    var selectedId = unpaid.first['id'].toString();
    final invoice = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (dialogContext) => StatefulBuilder(
        builder: (context, update) => AlertDialog(
          title: Text(_isResident ? 'Select invoice to pay' : 'Select invoice'),
          content: SizedBox(
            width: 540,
            child: DropdownButtonFormField<String>(
              initialValue: selectedId,
              isExpanded: true,
              decoration: InputDecoration(
                labelText: _isResident
                    ? 'Your outstanding invoice'
                    : 'Resident invoice',
              ),
              items: unpaid
                  .map(
                    (item) => DropdownMenuItem(
                      value: item['id'].toString(),
                      child: Text(
                        _isResident
                            ? '${item['invoice_number']} · ${_money(_number(item['outstanding']))}'
                            : '${item['resident_name'] ?? 'Unassigned'} · ${_space(item)} · ${_money(_number(item['outstanding']))}',
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  )
                  .toList(),
              onChanged: (value) => update(() => selectedId = value!),
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(
                dialogContext,
                unpaid.firstWhere(
                  (item) => item['id'].toString() == selectedId,
                ),
              ),
              child: Text(_isResident ? 'Continue online' : 'Continue'),
            ),
          ],
        ),
      ),
    );
    if (invoice != null) {
      await _pay(invoice, initialMethod: initialMethod);
    }
  }

  Future<void> _receipt(String paymentId) async {
    await showDialog<void>(
      context: context,
      builder: (_) =>
          _ReceiptDialog(client: widget.client, paymentId: paymentId),
    );
  }

  Future<void> _invoiceDetails(Map<String, dynamic> invoice) async {
    await showDialog<void>(
      context: context,
      builder: (_) => _InvoiceDetailDialog(
        client: widget.client,
        invoice: invoice,
        onReceipt: _receipt,
      ),
    );
  }

  void _message(String text) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
    }
  }

  @override
  Widget build(
    BuildContext context,
  ) => BlocBuilder<AsyncDataBloc<_MaintenanceData>, AsyncDataState<_MaintenanceData>>(
    bloc: _bloc,
    builder: (context, state) {
      if (state.status == AsyncDataStatus.initial ||
          state.status == AsyncDataStatus.loading) {
        return const Center(child: CircularProgressIndicator());
      }
      if (state.status == AsyncDataStatus.failure) {
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
      final totalDue = _invoices.fold<double>(
        0,
        (sum, item) => sum + _number(item['outstanding']),
      );
      final totalPaid = _payments.fold<double>(
        0,
        (sum, item) => sum + _number(item['amount']),
      );
      final totalBilled = _invoices.fold<double>(
        0,
        (sum, item) => sum + _number(item['total']),
      );
      final membershipBilled = _invoices.fold<double>(
        0,
        (sum, item) => sum + _number(item['membership_fee']),
      );
      final otherChargesBilled = _invoices.fold<double>(
        0,
        (sum, item) => sum + _number(item['other_charges']),
      );
      final overdue = _invoices.where((item) => item['overdue'] == true).length;
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
                      _isResident
                          ? 'Society dues & payments'
                          : _isPaymentsPage
                          ? 'Payments & receipts'
                          : 'Member charges & payments',
                      style: Theme.of(context).textTheme.headlineMedium,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      _isResident
                          ? 'Pay society dues online and access every receipt.'
                          : _isPaymentsPage
                          ? 'Record collections, reconcile transactions, and issue receipts.'
                          : 'Set member charges, create invoices, receive payments, and issue receipts.',
                      style: const TextStyle(color: SocietyColors.textMuted),
                    ),
                  ],
                ),
                Wrap(
                  spacing: 10,
                  runSpacing: 8,
                  children: [
                    if (_isAdmin && widget.fixedView == null)
                      OutlinedButton.icon(
                        onPressed: _manageChargeSettings,
                        icon: const Icon(Icons.tune),
                        label: const Text('Charge settings'),
                      ),
                    if (_isAdmin && !_isBillingPage)
                      OutlinedButton.icon(
                        onPressed: _configureOnlinePayment,
                        icon: const Icon(Icons.account_balance),
                        label: const Text('Online payment account'),
                      ),
                    if (_isAdmin && !_isPaymentsPage)
                      OutlinedButton.icon(
                        onPressed: _createInvoice,
                        icon: const Icon(Icons.note_add_outlined),
                        label: const Text('Create society bill'),
                      ),
                    if (!_isBillingPage)
                      FilledButton.icon(
                        onPressed: () => _choosePayment(),
                        icon: Icon(
                          _isResident ? Icons.lock_outline : Icons.add_card,
                        ),
                        label: Text(
                          _isResident ? 'Pay online' : 'Receive member payment',
                        ),
                      ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 18),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                if (_isBillingPage) ...[
                  _Metric(
                    'Total billed',
                    _money(totalBilled),
                    Icons.receipt_long,
                    SocietyColors.blue,
                  ),
                  _Metric(
                    'Membership fees',
                    _money(membershipBilled),
                    Icons.home_work_outlined,
                    SocietyColors.success,
                  ),
                  _Metric(
                    'Other charges',
                    _money(otherChargesBilled),
                    Icons.playlist_add,
                    SocietyColors.warning,
                  ),
                ] else ...[
                  _Metric(
                    'Outstanding',
                    _money(totalDue),
                    Icons.pending_actions,
                    SocietyColors.warning,
                  ),
                  _Metric(
                    _isResident ? 'Total paid by you' : 'Collected',
                    _money(totalPaid),
                    Icons.account_balance_wallet,
                    SocietyColors.success,
                  ),
                  if (_isPaymentsPage)
                    _Metric(
                      'Receipts issued',
                      '${_payments.length}',
                      Icons.receipt_outlined,
                      SocietyColors.blue,
                    ),
                ],
                _Metric(
                  _isBillingPage ? 'Overdue bills' : 'Overdue invoices',
                  '$overdue',
                  Icons.warning_amber_rounded,
                  overdue > 0 ? SocietyColors.danger : SocietyColors.blue,
                ),
              ],
            ),
            const SizedBox(height: 18),
            if (_isResident)
              _PaymentChannelCard(
                icon: Icons.language,
                color: SocietyColors.blue,
                title: 'Online society dues payment',
                methods: 'UPI · Debit/Credit card · Net banking',
                description:
                    'Select an outstanding invoice, confirm the amount, and receive a receipt immediately.',
                actionLabel: 'Pay online now',
                onAction: () => _choosePayment('UPI'),
              )
            else if (_isPaymentsPage || widget.fixedView == null)
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: [
                  _PaymentChannelCard(
                    icon: Icons.payments_outlined,
                    color: SocietyColors.success,
                    title: 'Receive cash / offline payment',
                    methods: 'Cash',
                    description:
                        'Record cash collected from a member and issue a numbered receipt.',
                    actionLabel: 'Receive cash payment',
                    onAction: () => _choosePayment('CASH'),
                  ),
                  _PaymentChannelCard(
                    icon: Icons.qr_code_2,
                    color: SocietyColors.blue,
                    title: 'Receive digital member payment',
                    methods: 'Razorpay · UPI · Card · Net banking',
                    description:
                        'Open secure online checkout for the member. Funds are recorded against the society settlement account.',
                    actionLabel: 'Open online checkout',
                    onAction: () => _choosePayment('UPI'),
                  ),
                ],
              ),
            if (widget.fixedView == null) ...[
              const SizedBox(height: 18),
              SegmentedButton<MaintenanceView>(
                segments: const [
                  ButtonSegment(
                    value: MaintenanceView.invoices,
                    icon: Icon(Icons.receipt_long),
                    label: Text('Invoices'),
                  ),
                  ButtonSegment(
                    value: MaintenanceView.payments,
                    icon: Icon(Icons.payments_outlined),
                    label: Text('Payments & receipts'),
                  ),
                ],
                selected: {_view},
                onSelectionChanged: (values) =>
                    setState(() => _view = values.first),
              ),
            ],
            const SizedBox(height: 14),
            TextField(
              onChanged: (value) => setState(() => _query = value),
              decoration: InputDecoration(
                prefixIcon: const Icon(Icons.search),
                labelText: _view == MaintenanceView.invoices
                    ? _isResident
                          ? 'Search invoice number or billing month'
                          : 'Search invoice, resident, or space'
                    : _isResident
                    ? 'Search receipt, invoice, or transaction reference'
                    : 'Search receipt, invoice, reference, or resident',
              ),
            ),
            if (_view == MaintenanceView.invoices) ...[
              const SizedBox(height: 12),
              Wrap(
                spacing: 8,
                children: ['ALL', 'DUE', 'PART_PAID', 'PAID', 'OVERDUE']
                    .map(
                      (value) => ChoiceChip(
                        label: Text(_label(value)),
                        selected: _status == value,
                        onSelected: (_) => setState(() => _status = value),
                      ),
                    )
                    .toList(),
              ),
            ],
            const SizedBox(height: 14),
            if (_view == MaintenanceView.invoices)
              _invoiceList()
            else
              _paymentList(),
          ],
        ),
      );
    },
  );

  Widget _invoiceList() {
    final query = _query.trim().toLowerCase();
    final rows = _invoices.where((invoice) {
      final statusMatch =
          _status == 'ALL' ||
          (_status == 'OVERDUE'
              ? invoice['overdue'] == true
              : invoice['status'] == _status);
      if (!statusMatch) return false;
      if (query.isEmpty) return true;
      final searchable = _isResident
          ? [invoice['invoice_number'], invoice['billing_month']]
          : [
              invoice['invoice_number'],
              invoice['resident_name'],
              invoice['space_number'],
              invoice['block'],
            ];
      return searchable.any(
        (value) => value?.toString().toLowerCase().contains(query) == true,
      );
    }).toList();
    if (rows.isEmpty) return const _Empty('No society bills match this view.');
    return Column(
      children: [
        for (final invoice in rows) ...[
          Card(
            child: InkWell(
              onTap: () => _invoiceDetails(invoice),
              borderRadius: BorderRadius.circular(20),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: LayoutBuilder(
                  builder: (context, constraints) {
                    final details = Row(
                      children: [
                        _IconBox(
                          icon: Icons.receipt_long,
                          color: invoice['overdue'] == true
                              ? SocietyColors.danger
                              : SocietyColors.blue,
                        ),
                        const SizedBox(width: 14),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Expanded(
                                    child: Text(
                                      invoice['invoice_number']?.toString() ??
                                          'Invoice',
                                      style: const TextStyle(
                                        fontWeight: FontWeight.w700,
                                        fontSize: 16,
                                      ),
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                  ),
                                  _Status(
                                    value:
                                        invoice['status']?.toString() ?? 'DUE',
                                  ),
                                ],
                              ),
                              const SizedBox(height: 5),
                              Text(
                                '${invoice['resident_name'] ?? 'Unassigned unit'} · ${_space(invoice)}',
                              ),
                              Text(
                                '${_month(invoice['billing_month'])} · ${_chargeSummary(invoice)} · Due ${_date(invoice['due_date'])} · Outstanding ${_money(_number(invoice['outstanding']))}',
                                style: TextStyle(
                                  color: invoice['overdue'] == true
                                      ? SocietyColors.danger
                                      : SocietyColors.textMuted,
                                  fontSize: 12,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    );
                    final outstanding = _number(invoice['outstanding']);
                    final actions = Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: [
                        if (outstanding > 0 && !_isBillingPage)
                          FilledButton.tonalIcon(
                            onPressed: () => _pay(invoice),
                            icon: Icon(
                              _isResident ? Icons.lock_outline : Icons.add_card,
                            ),
                            label: Text(
                              _isResident ? 'Pay online' : 'Receive payment',
                            ),
                          ),
                        TextButton(
                          onPressed: () => _invoiceDetails(invoice),
                          child: const Text('Details'),
                        ),
                      ],
                    );
                    if (constraints.maxWidth < 720) {
                      return Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          details,
                          const SizedBox(height: 12),
                          actions,
                        ],
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
            ),
          ),
          const SizedBox(height: 10),
        ],
      ],
    );
  }

  Widget _paymentList() {
    final query = _query.trim().toLowerCase();
    final rows = _payments.where((payment) {
      if (query.isEmpty) return true;
      final searchable = _isResident
          ? [
              payment['receipt_number'],
              payment['invoice_number'],
              payment['provider_reference'],
            ]
          : [
              payment['receipt_number'],
              payment['invoice_number'],
              payment['provider_reference'],
              payment['payer_name'],
              payment['space_number'],
            ];
      return searchable.any(
        (value) => value?.toString().toLowerCase().contains(query) == true,
      );
    }).toList();
    if (rows.isEmpty) return const _Empty('No society payments recorded.');
    return Column(
      children: [
        for (final payment in rows) ...[
          Card(
            child: ListTile(
              onTap: () => _receipt(payment['id'].toString()),
              leading: _IconBox(
                icon: payment['payment_channel'] == 'ONLINE'
                    ? Icons.language
                    : Icons.payments,
                color: SocietyColors.success,
              ),
              title: Text(
                '${payment['receipt_number']} · ${_money(_number(payment['amount']))}',
                overflow: TextOverflow.ellipsis,
              ),
              subtitle: Text(
                '${payment['invoice_number']} · ${payment['payer_name'] ?? ''} · ${_label(payment['payment_method']?.toString() ?? '')}\n${_dateTime(payment['paid_at'])} · ${payment['provider_reference'] ?? ''}',
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
              isThreeLine: true,
              trailing: IconButton(
                tooltip: 'View receipt',
                onPressed: () => _receipt(payment['id'].toString()),
                icon: const Icon(Icons.receipt_outlined),
              ),
            ),
          ),
          const SizedBox(height: 8),
        ],
      ],
    );
  }
}

class _MaintenanceData {
  const _MaintenanceData({
    required this.invoices,
    required this.payments,
    required this.chargeSettings,
    required this.options,
  });
  final List<Map<String, dynamic>> invoices;
  final List<Map<String, dynamic>> payments;
  final List<Map<String, dynamic>> chargeSettings;
  final Map<String, dynamic> options;

  static Future<_MaintenanceData> load(BlocManagedApiGateway client) async {
    final values = await Future.wait([
      client.get('/api/billing/invoices'),
      client.get('/api/billing/payments'),
      client.get('/api/billing/options'),
      client.get('/api/billing/charge-settings'),
    ]);
    return _MaintenanceData(
      invoices: List.unmodifiable(_maps(values[0])),
      payments: List.unmodifiable(_maps(values[1])),
      options: Map<String, dynamic>.unmodifiable(
        Map<String, dynamic>.from(values[2] as Map),
      ),
      chargeSettings: List.unmodifiable(_maps(values[3])),
    );
  }
}

class _ChargeSettingsDialog extends StatelessWidget {
  const _ChargeSettingsDialog({
    required this.client,
    required this.settings,
    required this.options,
  });
  final BlocManagedApiGateway client;
  final List<Map<String, dynamic>> settings;
  final Map<String, dynamic> options;

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Member charge settings'),
    content: SizedBox(
      width: 620,
      child: settings.isEmpty
          ? const Padding(
              padding: EdgeInsets.all(24),
              child: Text(
                'No charge settings yet. Add a membership, maintenance, or other reusable member charge.',
              ),
            )
          : ListView.separated(
              shrinkWrap: true,
              itemCount: settings.length,
              separatorBuilder: (_, _) => const Divider(),
              itemBuilder: (_, index) {
                final item = settings[index];
                return ListTile(
                  leading: Icon(
                    item['charge_type'] == 'MEMBERSHIP'
                        ? Icons.home_work_outlined
                        : Icons.playlist_add,
                    color: SocietyColors.blue,
                  ),
                  title: Text(
                    '${item['name']} · ${_money(_number(item['amount']))}',
                  ),
                  subtitle: Text(
                    '${item['flat_id'] == null ? 'Default for all members' : '${item['resident_name'] ?? 'Member'} · ${item['space_number'] ?? 'Space'}'} · ${_label(item['charge_type'].toString())} · ${_label(item['frequency'].toString())}${item['description'] == null ? '' : '\n${item['description']}'}',
                  ),
                );
              },
            ),
    ),
    actions: [
      TextButton(
        onPressed: () => Navigator.pop(context, false),
        child: const Text('Close'),
      ),
      FilledButton.icon(
        onPressed: () async {
          final created = await showDialog<bool>(
            context: context,
            builder: (_) =>
                _ChargeSettingForm(client: client, options: options),
          );
          if (created == true && context.mounted) Navigator.pop(context, true);
        },
        icon: const Icon(Icons.add),
        label: const Text('Add charge setting'),
      ),
    ],
  );
}

class _ChargeSettingForm extends StatefulWidget {
  const _ChargeSettingForm({required this.client, required this.options});
  final BlocManagedApiGateway client;
  final Map<String, dynamic> options;

  @override
  State<_ChargeSettingForm> createState() => _ChargeSettingFormState();
}

class _ChargeSettingFormState extends State<_ChargeSettingForm> {
  final _form = GlobalKey<FormState>();
  final _name = TextEditingController();
  final _amount = TextEditingController();
  final _description = TextEditingController();
  String _type = 'MEMBERSHIP';
  String _frequency = 'MONTHLY';
  String? _flatId;
  bool _saving = false;
  String? _error;

  @override
  void dispose() {
    _name.dispose();
    _amount.dispose();
    _description.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_form.currentState!.validate()) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await widget.client.post('/api/billing/charge-settings', {
        'name': _name.text.trim(),
        'chargeType': _type,
        'amount': double.parse(_amount.text.trim()),
        'frequency': _frequency,
        if (_flatId != null) 'flatId': _flatId,
        if (_description.text.trim().isNotEmpty)
          'description': _description.text.trim(),
      });
      if (mounted) Navigator.pop(context, true);
    } catch (error) {
      if (mounted) setState(() => _error = error.toString());
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Add member charge setting'),
    content: SizedBox(
      width: 520,
      child: Form(
        key: _form,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextFormField(
              controller: _name,
              decoration: const InputDecoration(
                labelText: 'Charge name',
                hintText: 'Monthly membership fee',
              ),
              validator: (value) => value == null || value.trim().isEmpty
                  ? 'Enter a charge name'
                  : null,
            ),
            const SizedBox(height: 12),
            DropdownButtonFormField<String>(
              initialValue: _type,
              decoration: const InputDecoration(labelText: 'Charge type'),
              items: const [
                DropdownMenuItem(
                  value: 'MEMBERSHIP',
                  child: Text('Membership / maintenance'),
                ),
                DropdownMenuItem(
                  value: 'OTHER',
                  child: Text('Other member charge'),
                ),
              ],
              onChanged: (value) => setState(() => _type = value!),
            ),
            const SizedBox(height: 12),
            DropdownButtonFormField<String>(
              initialValue: _flatId,
              isExpanded: true,
              decoration: const InputDecoration(
                labelText: 'Applies to',
                helperText: 'Leave as default to use for every member',
              ),
              items: [
                const DropdownMenuItem<String>(
                  value: null,
                  child: Text('Default for all members'),
                ),
                ..._maps(widget.options['spaces']).map(
                  (space) => DropdownMenuItem<String>(
                    value: space['space_id'].toString(),
                    child: Text(
                      '${space['resident_name'] ?? 'Vacant'} · ${space['block'] ?? ''} / ${space['space_number']}',
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ),
              ],
              onChanged: (value) => setState(() => _flatId = value),
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _amount,
              keyboardType: const TextInputType.numberWithOptions(
                decimal: true,
              ),
              decoration: const InputDecoration(
                labelText: 'Default amount',
                prefixText: '₹ ',
              ),
              validator: (value) =>
                  _number(value) <= 0 ? 'Enter a valid amount' : null,
            ),
            const SizedBox(height: 12),
            DropdownButtonFormField<String>(
              initialValue: _frequency,
              decoration: const InputDecoration(labelText: 'Frequency'),
              items: const ['MONTHLY', 'QUARTERLY', 'ANNUAL', 'ONE_TIME']
                  .map(
                    (value) =>
                        DropdownMenuItem(value: value, child: Text(value)),
                  )
                  .toList(),
              onChanged: (value) => setState(() => _frequency = value!),
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _description,
              maxLength: 240,
              decoration: const InputDecoration(
                labelText: 'Description (optional)',
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
    actions: [
      TextButton(
        onPressed: _saving ? null : () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(
        onPressed: _saving ? null : _save,
        child: Text(_saving ? 'Saving…' : 'Save charge'),
      ),
    ],
  );
}

class _InvoiceDialog extends StatefulWidget {
  const _InvoiceDialog({
    required this.client,
    required this.options,
    required this.chargeSettings,
  });
  final BlocManagedApiGateway client;
  final Map<String, dynamic> options;
  final List<Map<String, dynamic>> chargeSettings;

  @override
  State<_InvoiceDialog> createState() => _InvoiceDialogState();
}

class _InvoiceDialogState extends State<_InvoiceDialog> {
  final _form = GlobalKey<FormState>();
  final _membershipFee = TextEditingController();
  final _otherCharges = TextEditingController(text: '0');
  final _otherChargeNote = TextEditingController();
  String? _spaceId;
  String? _chargeSettingId;
  DateTime _month = DateTime(DateTime.now().year, DateTime.now().month);
  DateTime _due = DateTime.now().add(const Duration(days: 15));
  bool _saving = false;
  String? _error;

  List<Map<String, dynamic>> get _spaces => _maps(widget.options['spaces']);
  List<Map<String, dynamic>> get _applicableSettings => widget.chargeSettings
      .where(
        (setting) =>
            setting['flat_id'] == null ||
            setting['flat_id']?.toString() == _spaceId,
      )
      .toList();

  void _applyChargeSetting(String? id) {
    setState(() {
      _chargeSettingId = id;
      if (id == null) return;
      final setting = widget.chargeSettings.firstWhere(
        (item) => item['id'].toString() == id,
      );
      final amount = _number(setting['amount']).toStringAsFixed(2);
      if (setting['charge_type'] == 'MEMBERSHIP') {
        _membershipFee.text = amount;
      } else {
        _otherCharges.text = amount;
        _otherChargeNote.text =
            setting['description']?.toString().isNotEmpty == true
            ? setting['description'].toString()
            : setting['name'].toString();
      }
    });
  }

  @override
  void initState() {
    super.initState();
    _spaceId = _spaces.firstOrNull?['space_id']?.toString();
  }

  @override
  void dispose() {
    _membershipFee.dispose();
    _otherCharges.dispose();
    _otherChargeNote.dispose();
    super.dispose();
  }

  Future<void> _pickMonth() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: _month,
      firstDate: DateTime(DateTime.now().year - 2),
      lastDate: DateTime(DateTime.now().year + 2, 12, 31),
      helpText: 'Select any date in the billing month',
    );
    if (picked != null)
      setState(() => _month = DateTime(picked.year, picked.month));
  }

  Future<void> _pickDue() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: _due,
      firstDate: _month,
      lastDate: DateTime(_month.year + 1, 12, 31),
    );
    if (picked != null) setState(() => _due = picked);
  }

  Future<void> _save() async {
    if (!_form.currentState!.validate()) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await widget.client.post('/api/billing/invoices', {
        'spaceId': _spaceId,
        'billingMonth': _isoDate(_month),
        'dueDate': _isoDate(_due),
        'membershipFee': _number(_membershipFee.text.trim()),
        'otherCharges': _number(_otherCharges.text.trim()),
        if (_otherChargeNote.text.trim().isNotEmpty)
          'otherChargeNote': _otherChargeNote.text.trim(),
      });
      if (mounted) Navigator.pop(context, true);
    } catch (error) {
      if (mounted) setState(() => _error = error.toString());
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Create society bill'),
    content: SizedBox(
      width: 560,
      child: SingleChildScrollView(
        child: Form(
          key: _form,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (_applicableSettings.isNotEmpty) ...[
                DropdownButtonFormField<String>(
                  initialValue: _chargeSettingId,
                  isExpanded: true,
                  decoration: const InputDecoration(
                    labelText: 'Use a saved member charge',
                  ),
                  items: _applicableSettings
                      .map(
                        (setting) => DropdownMenuItem(
                          value: setting['id'].toString(),
                          child: Text(
                            '${setting['name']} · ${_money(_number(setting['amount']))} · ${_label(setting['frequency'].toString())}',
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      )
                      .toList(),
                  onChanged: _applyChargeSetting,
                ),
                const SizedBox(height: 12),
              ],
              DropdownButtonFormField<String>(
                initialValue: _spaceId,
                isExpanded: true,
                decoration: const InputDecoration(
                  labelText: 'Resident and space',
                ),
                items: _spaces
                    .map(
                      (space) => DropdownMenuItem(
                        value: space['space_id'].toString(),
                        child: Text(
                          '${space['resident_name'] ?? 'Vacant'} · ${space['block'] ?? ''} / ${space['space_number']}',
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    )
                    .toList(),
                onChanged: (value) => setState(() {
                  _spaceId = value;
                  _chargeSettingId = null;
                }),
                validator: (value) => value == null ? 'Select a space' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _membershipFee,
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                ),
                decoration: const InputDecoration(
                  labelText: 'Membership / maintenance fee',
                  prefixText: '₹ ',
                ),
                validator: (value) {
                  if (_number(value) < 0) return 'Amount cannot be negative';
                  if (_number(value) == 0 && _number(_otherCharges.text) == 0) {
                    return 'Add at least one society charge';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _otherCharges,
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                ),
                decoration: const InputDecoration(
                  labelText: 'Other society charges',
                  hintText: 'Amenity, parking, repair, event, etc.',
                  prefixText: '₹ ',
                ),
                validator: (value) =>
                    _number(value) < 0 ? 'Amount cannot be negative' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _otherChargeNote,
                maxLength: 240,
                decoration: const InputDecoration(
                  labelText: 'Other charge details',
                  hintText: 'Required when other charges are added',
                ),
                validator: (value) =>
                    _number(_otherCharges.text) > 0 &&
                        (value == null || value.trim().isEmpty)
                    ? 'Describe the other charge'
                    : null,
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: _pickMonth,
                      icon: const Icon(Icons.calendar_month),
                      label: Text('Month: ${_monthLabel(_month)}'),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: _pickDue,
                      icon: const Icon(Icons.event),
                      label: Text('Due: ${_date(_due)}'),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              const ListTile(
                contentPadding: EdgeInsets.zero,
                leading: Icon(Icons.auto_awesome, color: SocietyColors.blue),
                title: Text('Invoice number will be generated automatically.'),
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
      FilledButton(
        onPressed: _saving ? null : _save,
        child: Text(_saving ? 'Creating…' : 'Create bill'),
      ),
    ],
  );
}

class _SocietyPaymentSettingsDialog extends StatefulWidget {
  const _SocietyPaymentSettingsDialog({
    required this.client,
    required this.settings,
  });
  final BlocManagedApiGateway client;
  final Map<String, dynamic> settings;

  @override
  State<_SocietyPaymentSettingsDialog> createState() =>
      _SocietyPaymentSettingsDialogState();
}

class _SocietyPaymentSettingsDialogState
    extends State<_SocietyPaymentSettingsDialog> {
  late final _beneficiary = TextEditingController(
    text: widget.settings['beneficiary_name']?.toString() ?? '',
  );
  late final _bank = TextEditingController(
    text: widget.settings['bank_name']?.toString() ?? '',
  );
  late final _account = TextEditingController(
    text: widget.settings['account_number']?.toString() ?? '',
  );
  late final _ifsc = TextEditingController(
    text: widget.settings['ifsc_code']?.toString() ?? '',
  );
  late final _upi = TextEditingController(
    text: widget.settings['upi_id']?.toString() ?? '',
  );
  late final _razorpayKey = TextEditingController(
    text: widget.settings['razorpay_key_id']?.toString() ?? '',
  );
  bool _saving = false;
  String? _error;

  @override
  void dispose() {
    _beneficiary.dispose();
    _bank.dispose();
    _account.dispose();
    _ifsc.dispose();
    _upi.dispose();
    _razorpayKey.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (_beneficiary.text.trim().isEmpty || _razorpayKey.text.trim().isEmpty) {
      setState(() => _error = 'Beneficiary and Razorpay key ID are required.');
      return;
    }
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await widget.client.put('/api/billing/payment-settings', {
        'beneficiaryName': _beneficiary.text.trim(),
        'bankName': _bank.text.trim(),
        'accountNumber': _account.text.trim(),
        'ifscCode': _ifsc.text.trim(),
        'upiId': _upi.text.trim(),
        'razorpayKeyId': _razorpayKey.text.trim(),
        'testMode': true,
      });
      if (mounted) Navigator.pop(context, true);
    } catch (error) {
      if (mounted) setState(() => _error = error.toString());
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Society online payment account'),
    content: SizedBox(
      width: 620,
      child: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Member Razorpay payments are recorded against this society settlement account. The platform membership account is separate.',
            ),
            const SizedBox(height: 14),
            TextField(
              controller: _beneficiary,
              decoration: const InputDecoration(labelText: 'Beneficiary name'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: _bank,
              decoration: const InputDecoration(labelText: 'Settlement bank'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: _account,
              decoration: const InputDecoration(
                labelText: 'Settlement account number',
              ),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: _ifsc,
              decoration: const InputDecoration(labelText: 'IFSC code'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: _upi,
              decoration: const InputDecoration(labelText: 'Society UPI ID'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: _razorpayKey,
              decoration: const InputDecoration(labelText: 'Razorpay key ID'),
            ),
            const ListTile(
              contentPadding: EdgeInsets.zero,
              leading: Icon(Icons.science_outlined),
              title: Text('Razorpay test mode'),
              subtitle: Text(
                'No real money is charged. Live mode requires production credentials and webhook verification.',
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
    actions: [
      TextButton(
        onPressed: _saving ? null : () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(
        onPressed: _saving ? null : _save,
        child: Text(_saving ? 'Saving…' : 'Save online payment account'),
      ),
    ],
  );
}

class _PaymentDialog extends StatefulWidget {
  const _PaymentDialog({
    required this.invoice,
    required this.resident,
    required this.onlinePayment,
    this.initialMethod,
  });
  final Map<String, dynamic> invoice;
  final bool resident;
  final Map<String, dynamic> onlinePayment;
  final String? initialMethod;

  @override
  State<_PaymentDialog> createState() => _PaymentDialogState();
}

class _PaymentDialogState extends State<_PaymentDialog> {
  late final TextEditingController _amount = TextEditingController(
    text: _number(widget.invoice['outstanding']).toStringAsFixed(2),
  );
  final _note = TextEditingController();
  late final bool _online =
      widget.resident ||
      const {
        'UPI',
        'CARD',
        'NET_BANKING',
        'ONLINE_UPI',
      }.contains(widget.initialMethod);
  late String _method = widget.initialMethod == 'ONLINE_UPI'
      ? 'UPI'
      : widget.initialMethod ?? (widget.resident ? 'UPI' : 'CASH');
  String? _error;

  @override
  void dispose() {
    _amount.dispose();
    _note.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final methods = _online
        ? const ['UPI', 'CARD', 'NET_BANKING']
        : const ['CASH'];
    final configured = widget.onlinePayment['configured'] == true;
    return AlertDialog(
      title: Text(
        _online ? 'Razorpay society payment' : 'Receive cash payment',
      ),
      content: SizedBox(
        width: 520,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                '${widget.invoice['invoice_number']} · ${_space(widget.invoice)}',
                style: const TextStyle(fontWeight: FontWeight.w700),
              ),
              Text(
                'Outstanding ${_money(_number(widget.invoice['outstanding']))}',
                style: const TextStyle(color: SocietyColors.textMuted),
              ),
              const SizedBox(height: 14),
              TextField(
                controller: _amount,
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                ),
                decoration: const InputDecoration(labelText: 'Payment amount'),
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<String>(
                initialValue: _method,
                isExpanded: true,
                decoration: InputDecoration(
                  labelText: _online
                      ? 'Online payment method'
                      : 'Payment method',
                ),
                items: methods
                    .map(
                      (method) => DropdownMenuItem(
                        value: method,
                        child: Text(_methodLabel(method)),
                      ),
                    )
                    .toList(),
                onChanged: (value) => setState(() => _method = value!),
              ),
              if (_online) ...[
                const SizedBox(height: 12),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: SocietyColors.blue.withValues(alpha: .06),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: SocietyColors.border),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        widget.onlinePayment['beneficiary_name']?.toString() ??
                            'Society collection account',
                        style: const TextStyle(fontWeight: FontWeight.w700),
                      ),
                      Text(
                        '${widget.onlinePayment['bank_name'] ?? 'Settlement account'} · ${widget.onlinePayment['account_number_masked'] ?? 'Account configured in Razorpay'}',
                        style: const TextStyle(color: SocietyColors.textMuted),
                      ),
                      if (widget.onlinePayment['test_mode'] == true)
                        const Padding(
                          padding: EdgeInsets.only(top: 6),
                          child: Chip(
                            label: Text('TEST MODE · No real money charged'),
                          ),
                        ),
                    ],
                  ),
                ),
              ],
              if (!_online) ...[
                const SizedBox(height: 12),
                TextField(
                  controller: _note,
                  minLines: 2,
                  maxLines: 4,
                  decoration: const InputDecoration(
                    labelText: 'Cash collection note (optional)',
                  ),
                ),
              ],
              if (_online) ...[
                const SizedBox(height: 12),
                Text(
                  configured
                      ? 'Razorpay will generate a transaction reference and the society will issue a receipt immediately.'
                      : 'Online payments are unavailable until the Society Admin configures the Razorpay settlement account.',
                  style: TextStyle(
                    color: configured
                        ? SocietyColors.textMuted
                        : SocietyColors.danger,
                  ),
                ),
              ],
              if (_error != null) ...[
                const SizedBox(height: 10),
                Text(
                  _error!,
                  style: const TextStyle(color: SocietyColors.danger),
                ),
              ],
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Cancel'),
        ),
        FilledButton.icon(
          onPressed: _online && !configured
              ? null
              : () {
                  final amount = double.tryParse(_amount.text.trim());
                  final outstanding = _number(widget.invoice['outstanding']);
                  if (amount == null || amount <= 0 || amount > outstanding) {
                    setState(
                      () => _error =
                          'Enter an amount up to ${_money(outstanding)}.',
                    );
                    return;
                  }
                  Navigator.pop(context, {
                    'amount': amount,
                    'paymentMethod': _method,
                    'onlineCheckout': _online,
                    if (!_online && _note.text.trim().isNotEmpty)
                      'note': _note.text.trim(),
                  });
                },
          icon: Icon(_online ? Icons.lock : Icons.payments),
          label: Text(
            _online ? 'Pay securely online' : 'Record cash and issue receipt',
          ),
        ),
      ],
    );
  }
}

class _InvoiceDetailDialog extends StatefulWidget {
  const _InvoiceDetailDialog({
    required this.client,
    required this.invoice,
    required this.onReceipt,
  });
  final BlocManagedApiGateway client;
  final Map<String, dynamic> invoice;
  final Future<void> Function(String paymentId) onReceipt;

  @override
  State<_InvoiceDetailDialog> createState() => _InvoiceDetailDialogState();
}

class _InvoiceDetailDialogState extends State<_InvoiceDetailDialog> {
  late final AsyncDataBloc<List<Map<String, dynamic>>> _paymentsBloc;

  @override
  void initState() {
    super.initState();
    _paymentsBloc = AsyncDataBloc(
      () async => _maps(
        await widget.client.get(
          '/api/billing/invoices/${widget.invoice['id']}/payments',
        ),
      ),
    )..refresh();
  }

  @override
  void dispose() {
    _paymentsBloc.close();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: Row(
      children: [
        Expanded(child: Text(widget.invoice['invoice_number'].toString())),
        _Status(value: widget.invoice['status'].toString()),
      ],
    ),
    content: SizedBox(
      width: 650,
      child: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Wrap(
              spacing: 18,
              runSpacing: 12,
              children: [
                _Fact('Resident', widget.invoice['resident_name']),
                _Fact('Space', _space(widget.invoice)),
                _Fact('Billing month', _month(widget.invoice['billing_month'])),
                _Fact('Due date', _date(widget.invoice['due_date'])),
                _Fact(
                  'Membership / maintenance',
                  _money(_number(widget.invoice['membership_fee'])),
                ),
                if (_number(widget.invoice['other_charges']) > 0)
                  _Fact(
                    'Other charges',
                    '${_money(_number(widget.invoice['other_charges']))} · ${widget.invoice['other_charge_note'] ?? ''}',
                  ),
                _Fact(
                  'Invoice total',
                  _money(_number(widget.invoice['total'])),
                ),
                _Fact('Paid', _money(_number(widget.invoice['paid_amount']))),
                _Fact(
                  'Outstanding',
                  _money(_number(widget.invoice['outstanding'])),
                ),
              ],
            ),
            const SizedBox(height: 22),
            Text(
              'Payment history',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 8),
            BlocBuilder<
              AsyncDataBloc<List<Map<String, dynamic>>>,
              AsyncDataState<List<Map<String, dynamic>>>
            >(
              bloc: _paymentsBloc,
              builder: (context, state) {
                if (state.status == AsyncDataStatus.failure &&
                    state.data == null) {
                  return Text(state.error.toString());
                }
                if (state.data == null)
                  return const Center(child: CircularProgressIndicator());
                if (state.data!.isEmpty)
                  return const Text('No payment received for this invoice.');
                return Column(
                  children: state.data!
                      .map(
                        (payment) => ListTile(
                          contentPadding: EdgeInsets.zero,
                          leading: const Icon(Icons.receipt_outlined),
                          title: Text(
                            '${payment['receipt_number']} · ${_money(_number(payment['amount']))}',
                          ),
                          subtitle: Text(
                            '${_methodLabel(payment['payment_method'].toString())} · ${_dateTime(payment['paid_at'])}',
                          ),
                          trailing: IconButton(
                            onPressed: () =>
                                widget.onReceipt(payment['id'].toString()),
                            icon: const Icon(Icons.open_in_new),
                          ),
                        ),
                      )
                      .toList(),
                );
              },
            ),
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

class _ReceiptDialog extends StatefulWidget {
  const _ReceiptDialog({required this.client, required this.paymentId});
  final BlocManagedApiGateway client;
  final String paymentId;

  @override
  State<_ReceiptDialog> createState() => _ReceiptDialogState();
}

class _ReceiptDialogState extends State<_ReceiptDialog> {
  late final AsyncDataBloc<Map<String, dynamic>> _receiptBloc;
  bool _downloading = false;

  @override
  void initState() {
    super.initState();
    _receiptBloc = AsyncDataBloc(
      () async => Map<String, dynamic>.from(
        await widget.client.get(
              '/api/billing/payments/${widget.paymentId}/receipt',
            )
            as Map,
      ),
    )..refresh();
  }

  @override
  void dispose() {
    _receiptBloc.close();
    super.dispose();
  }

  Future<void> _download(Map<String, dynamic> receipt) async {
    setState(() => _downloading = true);
    try {
      final bytes = await widget.client.getBytes(
        '/api/billing/payments/${widget.paymentId}/receipt.pdf',
      );
      await savePdfFile(
        'payment_receipt_${receipt['receipt_number'] ?? widget.paymentId}',
        bytes,
      );
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Payment receipt downloaded as PDF.')),
        );
      }
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(error.toString())));
      }
    } finally {
      if (mounted) setState(() => _downloading = false);
    }
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Payment receipt'),
    content: SizedBox(
      width: 580,
      child: ConstrainedBox(
        constraints: BoxConstraints(
          maxHeight: MediaQuery.sizeOf(context).height * .65,
        ),
        child:
            BlocBuilder<
              AsyncDataBloc<Map<String, dynamic>>,
              AsyncDataState<Map<String, dynamic>>
            >(
              bloc: _receiptBloc,
              builder: (context, state) {
                if (state.status == AsyncDataStatus.failure &&
                    state.data == null) {
                  return SingleChildScrollView(
                    child: Text(state.error.toString()),
                  );
                }
                if (state.data == null) {
                  return const SizedBox(
                    height: 220,
                    child: Center(child: CircularProgressIndicator()),
                  );
                }
                final receipt = state.data!;
                return SingleChildScrollView(
                  child: SelectionArea(
                    child: Container(
                      padding: const EdgeInsets.all(24),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        border: Border.all(color: SocietyColors.border),
                        borderRadius: BorderRadius.circular(18),
                      ),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const SocietyBrandMark(),
                          const SizedBox(height: 12),
                          Text(
                            receipt['society_name']?.toString() ?? 'Society',
                            style: Theme.of(context).textTheme.titleLarge,
                          ),
                          if (receipt['society_registration'] != null)
                            Text(
                              'Registration: ${receipt['society_registration']}',
                              style: const TextStyle(
                                color: SocietyColors.textMuted,
                              ),
                            ),
                          const Divider(height: 30),
                          const Icon(
                            Icons.check_circle,
                            color: SocietyColors.success,
                            size: 42,
                          ),
                          const SizedBox(height: 6),
                          Text(
                            _money(_number(receipt['amount'])),
                            style: Theme.of(context).textTheme.headlineMedium,
                          ),
                          const Text(
                            'Payment received',
                            style: TextStyle(
                              color: SocietyColors.success,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                          const SizedBox(height: 18),
                          _ReceiptRow(
                            'Receipt number',
                            receipt['receipt_number'],
                          ),
                          _ReceiptRow('Invoice', receipt['invoice_number']),
                          _ReceiptRow('Resident', receipt['payer_name']),
                          _ReceiptRow('Space', _space(receipt)),
                          _ReceiptRow(
                            'Billing month',
                            _month(receipt['billing_month']),
                          ),
                          _ReceiptRow(
                            'Membership / maintenance',
                            _money(_number(receipt['membership_fee'])),
                          ),
                          if (_number(receipt['other_charges']) > 0)
                            _ReceiptRow(
                              'Other charges',
                              '${_money(_number(receipt['other_charges']))} · ${receipt['other_charge_note'] ?? ''}',
                            ),
                          _ReceiptRow(
                            'Payment method',
                            _methodLabel(receipt['payment_method'].toString()),
                          ),
                          _ReceiptRow(
                            'Channel',
                            _label(receipt['payment_channel'].toString()),
                          ),
                          _ReceiptRow(
                            'Transaction reference',
                            receipt['provider_reference'],
                          ),
                          _ReceiptRow('Paid on', _dateTime(receipt['paid_at'])),
                          _ReceiptRow(
                            'Recorded by',
                            receipt['recorded_by_name'],
                          ),
                          if (receipt['note'] != null)
                            _ReceiptRow('Note', receipt['note']),
                          const Divider(height: 26),
                          const Text(
                            'This is a system-generated payment receipt.',
                            style: TextStyle(
                              color: SocietyColors.textMuted,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                );
              },
            ),
      ),
    ),
    actions: [
      BlocBuilder<
        AsyncDataBloc<Map<String, dynamic>>,
        AsyncDataState<Map<String, dynamic>>
      >(
        bloc: _receiptBloc,
        builder: (context, state) => OutlinedButton.icon(
          onPressed: state.data == null || _downloading
              ? null
              : () => _download(state.data!),
          icon: _downloading
              ? const SizedBox.square(
                  dimension: 16,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              : const Icon(Icons.download_outlined),
          label: const Text('Download PDF'),
        ),
      ),
      BlocBuilder<
        AsyncDataBloc<Map<String, dynamic>>,
        AsyncDataState<Map<String, dynamic>>
      >(
        bloc: _receiptBloc,
        builder: (context, state) => TextButton.icon(
          onPressed: state.data == null
              ? null
              : () async {
                  await Clipboard.setData(
                    ClipboardData(
                      text: state.data!['receipt_number'].toString(),
                    ),
                  );
                  if (context.mounted)
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Receipt number copied.')),
                    );
                },
          icon: const Icon(Icons.copy),
          label: const Text('Copy receipt number'),
        ),
      ),
      TextButton(
        onPressed: () => Navigator.pop(context),
        child: const Text('Close'),
      ),
    ],
  );
}

class _ReceiptRow extends StatelessWidget {
  const _ReceiptRow(this.label, this.value);
  final String label;
  final Object? value;
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 4),
    child: Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          width: 155,
          child: Text(
            label,
            style: const TextStyle(color: SocietyColors.textMuted),
          ),
        ),
        Expanded(
          child: Text(
            value?.toString() ?? '—',
            textAlign: TextAlign.right,
            style: const TextStyle(fontWeight: FontWeight.w600),
          ),
        ),
      ],
    ),
  );
}

class _PaymentChannelCard extends StatelessWidget {
  const _PaymentChannelCard({
    required this.icon,
    required this.color,
    required this.title,
    required this.methods,
    required this.description,
    required this.actionLabel,
    required this.onAction,
  });

  final IconData icon;
  final Color color;
  final String title;
  final String methods;
  final String description;
  final String actionLabel;
  final VoidCallback onAction;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: 380,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                _IconBox(icon: icon, color: color),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        title,
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                      Text(
                        methods,
                        style: TextStyle(
                          color: color,
                          fontWeight: FontWeight.w700,
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Text(
              description,
              style: const TextStyle(color: SocietyColors.textMuted),
            ),
            const SizedBox(height: 12),
            Align(
              alignment: Alignment.centerRight,
              child: FilledButton.tonal(
                onPressed: onAction,
                child: Text(actionLabel),
              ),
            ),
          ],
        ),
      ),
    ),
  );
}

class _Metric extends StatelessWidget {
  const _Metric(this.label, this.value, this.icon, this.color);
  final String label;
  final String value;
  final IconData icon;
  final Color color;
  @override
  Widget build(BuildContext context) => SizedBox(
    width: 210,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(15),
        child: Row(
          children: [
            Icon(icon, color: color),
            const SizedBox(width: 11),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    value,
                    style: Theme.of(context).textTheme.titleLarge,
                    overflow: TextOverflow.ellipsis,
                  ),
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

class _IconBox extends StatelessWidget {
  const _IconBox({required this.icon, required this.color});
  final IconData icon;
  final Color color;
  @override
  Widget build(BuildContext context) => Container(
    width: 46,
    height: 46,
    decoration: BoxDecoration(
      color: color.withValues(alpha: .1),
      borderRadius: BorderRadius.circular(14),
    ),
    child: Icon(icon, color: color),
  );
}

class _Status extends StatelessWidget {
  const _Status({required this.value});
  final String value;
  @override
  Widget build(BuildContext context) {
    final color = value == 'PAID'
        ? SocietyColors.success
        : value == 'PART_PAID'
        ? SocietyColors.blue
        : SocietyColors.warning;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 5),
      decoration: BoxDecoration(
        color: color.withValues(alpha: .1),
        borderRadius: BorderRadius.circular(100),
      ),
      child: Text(
        _label(value),
        style: TextStyle(
          color: color,
          fontWeight: FontWeight.w700,
          fontSize: 11,
        ),
      ),
    );
  }
}

class _Fact extends StatelessWidget {
  const _Fact(this.label, this.value);
  final String label;
  final Object? value;
  @override
  Widget build(BuildContext context) => SizedBox(
    width: 190,
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(color: SocietyColors.textMuted, fontSize: 12),
        ),
        Text(
          value?.toString() ?? '—',
          style: const TextStyle(fontWeight: FontWeight.w600),
        ),
      ],
    ),
  );
}

class _Empty extends StatelessWidget {
  const _Empty(this.message);
  final String message;
  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(36),
      child: Column(
        children: [
          const Icon(
            Icons.receipt_long_outlined,
            size: 44,
            color: SocietyColors.textMuted,
          ),
          const SizedBox(height: 10),
          Text(message),
        ],
      ),
    ),
  );
}

List<WorkspaceRecord> _maps(Object? value) => workspaceRecords(value);

double _number(Object? value) => double.tryParse(value?.toString() ?? '') ?? 0;

String _money(double value) => '₹${value.toStringAsFixed(2)}';

String _space(Map<String, dynamic> item) {
  final block = item['block']?.toString();
  final number = item['space_number']?.toString() ?? '—';
  return block == null || block.isEmpty ? number : '$block / $number';
}

String _chargeSummary(Map<String, dynamic> invoice) {
  final membership = _number(invoice['membership_fee']);
  final other = _number(invoice['other_charges']);
  if (membership > 0 && other > 0) return 'Membership + other charges';
  if (other > 0)
    return invoice['other_charge_note']?.toString() ?? 'Other charge';
  return 'Membership / maintenance';
}

String _label(String value) => value
    .toLowerCase()
    .split('_')
    .map(
      (part) =>
          part.isEmpty ? '' : '${part[0].toUpperCase()}${part.substring(1)}',
    )
    .join(' ');

String _methodLabel(String method) => switch (method) {
  'ONLINE_UPI' => 'UPI (online)',
  'ONLINE_CARD' => 'Card (online)',
  'RAZORPAY_UPI' => 'Razorpay UPI',
  'RAZORPAY_CARD' => 'Razorpay card',
  'RAZORPAY_NET_BANKING' => 'Razorpay net banking',
  'NET_BANKING' => 'Net banking',
  'BANK_TRANSFER' => 'Bank transfer',
  _ => _label(method),
};

String _isoDate(DateTime value) =>
    '${value.year}-${value.month.toString().padLeft(2, '0')}-${value.day.toString().padLeft(2, '0')}';

String _date(Object? value) {
  if (value is DateTime)
    return '${value.day.toString().padLeft(2, '0')}/${value.month.toString().padLeft(2, '0')}/${value.year}';
  if (value == null) return '—';
  final date = DateTime.tryParse(value.toString());
  return date == null
      ? value.toString()
      : '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year}';
}

String _dateTime(Object? value) {
  if (value == null) return '—';
  final date = DateTime.tryParse(value.toString())?.toLocal();
  if (date == null) return value.toString();
  return '${_date(date)} ${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
}

String _month(Object? value) {
  if (value == null) return '—';
  final date = DateTime.tryParse(value.toString());
  return date == null ? value.toString() : _monthLabel(date);
}

String _monthLabel(DateTime date) {
  const months = [
    'Jan',
    'Feb',
    'Mar',
    'Apr',
    'May',
    'Jun',
    'Jul',
    'Aug',
    'Sep',
    'Oct',
    'Nov',
    'Dec',
  ];
  return '${months[date.month - 1]} ${date.year}';
}
