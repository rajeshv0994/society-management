import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';
import 'async_data_bloc.dart';
import 'file_download.dart';
import 'theme.dart';
import 'workspace_record.dart';
import 'razorpay_checkout.dart';

typedef RazorpayLauncher =
    Future<RazorpayCheckoutResult> Function(Map<String, dynamic> options);

class PlatformMembershipWorkspace extends StatefulWidget {
  const PlatformMembershipWorkspace({
    super.key,
    required this.client,
    this.razorpayLauncher = openRazorpayCheckout,
  });
  final BlocManagedApiGateway client;
  final RazorpayLauncher razorpayLauncher;

  @override
  State<PlatformMembershipWorkspace> createState() =>
      _PlatformMembershipWorkspaceState();
}

class _PlatformMembershipWorkspaceState
    extends State<PlatformMembershipWorkspace> {
  late final AsyncDataBloc<_MembershipData> _bloc;
  Map<String, dynamic> get _subscription =>
      _bloc.state.data?.subscription ?? const {};
  List<Map<String, dynamic>> get _orders =>
      _bloc.state.data?.orders ?? const [];

  @override
  void initState() {
    super.initState();
    _bloc = AsyncDataBloc(() => _MembershipData.load(widget.client));
    _load();
  }

  Future<void> _load() => _bloc.refresh();

  @override
  void dispose() {
    _bloc.close();
    super.dispose();
  }

  Future<void> _checkout(Map<String, dynamic> order) async {
    final method = await showDialog<String>(
      context: context,
      builder: (_) => _RazorpayDialog(order: order),
    );
    if (method == null) return;
    try {
      final checkout = Map<String, dynamic>.from(
        await widget.client.post(
              '/api/society/subscription/orders/${order['id']}/checkout',
              {'paymentMethod': method},
            )
            as Map,
      );
      final result = await widget.razorpayLauncher(checkout);
      final paid = Map<String, dynamic>.from(
        await widget.client.post(
              '/api/society/subscription/orders/${order['id']}/verify',
              {
                'razorpayOrderId': result.orderId,
                'razorpayPaymentId': result.paymentId,
                'razorpaySignature': result.signature,
              },
            )
            as Map,
      );
      await _load();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              'Payment successful. ID: ${paid['provider_reference']}',
            ),
          ),
        );
      }
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(error.toString())));
      }
    }
  }

  Future<void> _downloadInvoice(Map<String, dynamic> order) async {
    try {
      final bytes = await widget.client.getBytes(
        '/api/society/subscription/orders/${order['id']}/invoice',
      );
      await savePdfFile(order['invoice_number'].toString(), bytes);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Invoice downloaded as PDF.')),
        );
      }
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(error.toString())));
      }
    }
  }

  Future<void> _invoiceAction(String action, Map<String, dynamic> order) async {
    switch (action) {
      case 'PAY':
        await _checkout(order);
        return;
      case 'DOWNLOAD':
        await _downloadInvoice(order);
        return;
    }
  }

  @override
  Widget build(
    BuildContext context,
  ) => BlocBuilder<AsyncDataBloc<_MembershipData>, AsyncDataState<_MembershipData>>(
    bloc: _bloc,
    builder: (context, state) {
      if (state.status == AsyncDataStatus.initial ||
          state.status == AsyncDataStatus.loading) {
        return const Center(child: CircularProgressIndicator());
      }
      if (state.status == AsyncDataStatus.failure)
        return Center(
          child: FilledButton.icon(
            onPressed: _load,
            icon: const Icon(Icons.refresh),
            label: Text(state.error.toString()),
          ),
        );
      final due = _orders
          .where(
            (item) =>
                ['DUE', 'REJECTED', 'PAYMENT_PENDING'].contains(item['status']),
          )
          .fold<double>(0, (sum, item) => sum + _number(item['amount']));
      final paid = _orders
          .where((item) => item['status'] == 'PAID')
          .fold<double>(0, (sum, item) => sum + _number(item['amount']));
      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Text(
              'Platform membership',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            const SizedBox(height: 4),
            const Text(
              'Manage your SocietyHub subscription invoices and payments to the platform.',
              style: TextStyle(color: SocietyColors.textMuted),
            ),
            const SizedBox(height: 18),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                _MembershipMetric(
                  '${_label(_subscription['billing_cycle'])} invoice',
                  _money(_number(_subscription['billing_cycle_amount'])),
                  Icons.calculate_outlined,
                  SocietyColors.blue,
                ),
                _MembershipMetric(
                  'Outstanding',
                  _money(due),
                  Icons.pending_actions,
                  SocietyColors.warning,
                ),
                _MembershipMetric(
                  'Total verified paid',
                  _money(paid),
                  Icons.verified_outlined,
                  SocietyColors.success,
                ),
                _MembershipMetric(
                  'Subscription status',
                  _label(_subscription['subscription_status']),
                  Icons.workspace_premium_outlined,
                  SocietyColors.blue,
                ),
              ],
            ),
            const SizedBox(height: 18),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'How to pay the platform',
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    const SizedBox(height: 12),
                    const _PaymentStep(
                      1,
                      'Choose requirements',
                      'In Society Profile, select spaces, staff seats, and monthly, quarterly, or yearly billing.',
                    ),
                    const _PaymentStep(
                      2,
                      'Receive the invoice',
                      'The platform admin issues an invoice using your selected requirements and billing cycle.',
                    ),
                    const _PaymentStep(
                      3,
                      'Pay securely online',
                      'Open the invoice actions, choose Pay now, and complete Razorpay checkout by UPI, card, or net banking.',
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 18),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Wrap(
                  spacing: 24,
                  runSpacing: 12,
                  children: [
                    _Fact('Plan', _subscription['plan_code']),
                    _Fact('Selected spaces', _subscription['selected_spaces']),
                    _Fact('Selected staff', _subscription['selected_staff']),
                    _Fact(
                      'Billing cycle',
                      _label(_subscription['billing_cycle']),
                    ),
                    _Fact(
                      'Amount per cycle',
                      _money(_number(_subscription['billing_cycle_amount'])),
                    ),
                    _Fact(
                      'Space rate',
                      _money(_number(_subscription['price_per_space'])),
                    ),
                    _Fact(
                      'Staff rate',
                      _money(_number(_subscription['price_per_staff'])),
                    ),
                    _Fact(
                      'Base monthly',
                      _money(_number(_subscription['base_monthly_price'])),
                    ),
                    _Fact(
                      'Paid services monthly',
                      _money(_number(_subscription['service_monthly_price'])),
                    ),
                    _Fact(
                      'Membership valid until',
                      _date(_subscription['subscription_expires_at']),
                    ),
                  ],
                ),
              ),
            ),
            if ((_subscription['service_catalog'] as List? ?? const []).any(
              (row) => (row as Map)['selected'] == true,
            )) ...[
              const SizedBox(height: 18),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(18),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Selected platform services',
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      const SizedBox(height: 10),
                      Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        children: [
                          for (final raw
                              in (_subscription['service_catalog'] as List))
                            if ((raw as Map)['selected'] == true)
                              Chip(
                                label: Text(
                                  raw['availability_type'] == 'PAID_PER_SPACE'
                                      ? '${raw['name']} · ${_money(_number(raw['monthly_amount']))}/month'
                                      : '${raw['name']} · Free',
                                ),
                              ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ],
            const SizedBox(height: 18),
            Text(
              'Subscription invoices',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 10),
            if (_orders.isEmpty)
              const Card(
                child: Padding(
                  padding: EdgeInsets.all(32),
                  child: Center(
                    child: Text(
                      'No platform subscription invoices issued yet.',
                    ),
                  ),
                ),
              )
            else
              for (final order in _orders)
                Card(
                  child: ListTile(
                    leading: Icon(
                      order['status'] == 'PAID'
                          ? Icons.verified
                          : Icons.receipt_long,
                      color: order['status'] == 'PAID'
                          ? SocietyColors.success
                          : SocietyColors.blue,
                    ),
                    title: Text(
                      '${order['invoice_number']} · ${_money(_number(order['amount']))}',
                    ),
                    subtitle: Text(
                      '${_label(order['billing_cycle'])} · ${_date(order['period_start'])} to ${_date(order['period_end'])} · Due ${_date(order['due_date'])}${order['provider_reference'] == null ? '' : '\n${_label(order['payment_method'])} · ${order['provider_reference']}'}${order['rejection_reason'] == null ? '' : '\nRejected: ${order['rejection_reason']}'}',
                    ),
                    isThreeLine:
                        order['provider_reference'] != null ||
                        order['rejection_reason'] != null,
                    trailing: PopupMenuButton<String>(
                      tooltip: 'Invoice actions',
                      onSelected: (value) => _invoiceAction(value, order),
                      itemBuilder: (_) => [
                        if ([
                          'DUE',
                          'REJECTED',
                          'PAYMENT_PENDING',
                        ].contains(order['status'])) ...[
                          const PopupMenuItem(
                            value: 'PAY',
                            child: ListTile(
                              leading: Icon(Icons.payment),
                              title: Text('Pay now with Razorpay'),
                            ),
                          ),
                        ],
                        const PopupMenuItem(
                          value: 'DOWNLOAD',
                          child: ListTile(
                            leading: Icon(Icons.download),
                            title: Text('Download invoice PDF'),
                          ),
                        ),
                      ],
                      child: Chip(
                        avatar: const Icon(Icons.more_horiz, size: 18),
                        label: Text(_label(order['status'])),
                      ),
                    ),
                  ),
                ),
          ],
        ),
      );
    },
  );
}

class _MembershipData {
  const _MembershipData({required this.subscription, required this.orders});

  final WorkspaceRecord subscription;
  final List<WorkspaceRecord> orders;

  static Future<_MembershipData> load(BlocManagedApiGateway client) async {
    final values = await Future.wait([
      client.get('/api/society/subscription'),
      client.get('/api/society/subscription/orders'),
    ]);
    return _MembershipData(
      subscription: WorkspaceRecord.from(values[0]),
      orders: workspaceRecords(values[1]),
    );
  }
}

class _RazorpayDialog extends StatefulWidget {
  const _RazorpayDialog({required this.order});
  final Map<String, dynamic> order;

  @override
  State<_RazorpayDialog> createState() => _RazorpayDialogState();
}

class _RazorpayDialogState extends State<_RazorpayDialog> {
  String _method = 'UPI';

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Row(
      children: [
        Icon(Icons.verified_user_outlined, color: SocietyColors.blue),
        SizedBox(width: 8),
        Expanded(child: Text('Razorpay checkout')),
      ],
    ),
    content: SizedBox(
      width: 480,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            _money(_number(widget.order['amount'])),
            style: Theme.of(context).textTheme.headlineMedium,
          ),
          Text(
            '${widget.order['invoice_number']} • ${_label(widget.order['billing_cycle'])}',
            style: const TextStyle(color: SocietyColors.textMuted),
          ),
          const SizedBox(height: 18),
          DropdownButtonFormField<String>(
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
          const SizedBox(height: 12),
          const Text(
            'Razorpay opens a secure checkout. SocietyHub verifies the signed response and captured payment before activating membership.',
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
        icon: const Icon(Icons.lock_outline),
        label: Text('Pay ${_money(_number(widget.order['amount']))}'),
      ),
    ],
  );
}

class _PaymentStep extends StatelessWidget {
  const _PaymentStep(this.number, this.title, this.detail);
  final int number;
  final String title;
  final String detail;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(bottom: 12),
    child: Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        CircleAvatar(radius: 15, child: Text('$number')),
        const SizedBox(width: 10),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title, style: const TextStyle(fontWeight: FontWeight.w600)),
              Text(
                detail,
                style: const TextStyle(color: SocietyColors.textMuted),
              ),
            ],
          ),
        ),
      ],
    ),
  );
}

class _MembershipMetric extends StatelessWidget {
  const _MembershipMetric(this.label, this.value, this.icon, this.color);
  final String label, value;
  final IconData icon;
  final Color color;
  @override
  Widget build(BuildContext context) => SizedBox(
    width: 220,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Icon(icon, color: color),
            const SizedBox(width: 10),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    label,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(color: SocietyColors.textMuted),
                  ),
                  FittedBox(
                    fit: BoxFit.scaleDown,
                    child: Text(
                      value,
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
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

double _number(Object? v) => double.tryParse(v?.toString() ?? '') ?? 0;
String _money(double v) => '₹${v.toStringAsFixed(2)}';
String _label(Object? v) => (v?.toString() ?? '')
    .toLowerCase()
    .split('_')
    .map((p) => p.isEmpty ? '' : '${p[0].toUpperCase()}${p.substring(1)}')
    .join(' ');
String _date(Object? v) {
  final d = DateTime.tryParse(v?.toString() ?? '');
  return d == null
      ? '—'
      : '${d.day.toString().padLeft(2, '0')}/${d.month.toString().padLeft(2, '0')}/${d.year}';
}
