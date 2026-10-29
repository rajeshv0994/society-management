import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';

import 'package:super_admin_app/core/presentation/widgets/common_widgets.dart';
import '../bloc/billing_bloc.dart';

class SubscriptionBillingView extends StatefulWidget {
  const SubscriptionBillingView({super.key});
  @override
  State<SubscriptionBillingView> createState() =>
      _SubscriptionBillingViewState();
}

class _SubscriptionBillingViewState extends State<SubscriptionBillingView> {
  List<Map<String, dynamic>> _orders = const [];
  List<Map<String, dynamic>> _societies = const [];
  String _status = 'ALL';
  String? _downloadName;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  void _load() => context.read<BillingBloc>().add(const BillingLoaded());

  Future<void> _issue() async {
    final body = await showDialog<Map<String, dynamic>>(
      context: context,
      builder: (_) => _IssueSubscriptionInvoice(societies: _societies),
    );
    if (body != null && mounted) {
      context.read<BillingBloc>().add(SubscriptionOrderCreated(body));
    }
  }

  Future<void> _downloadInvoice(Map<String, dynamic> order) async {
    _downloadName = order['invoice_number'].toString();
    context.read<BillingBloc>().add(
      SubscriptionInvoiceRequested(order['id'].toString()),
    );
  }

  Future<void> _cancelInvoice(Map<String, dynamic> order) async {
    var reason = '';
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: Text('Cancel ${order['invoice_number']}?'),
        content: TextField(
          onChanged: (value) => reason = value,
          maxLength: 300,
          maxLines: 3,
          decoration: const InputDecoration(
            labelText: 'Cancellation reason',
            helperText: 'The invoice remains visible as cancelled history.',
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialog, false),
            child: const Text('Keep invoice'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialog, true),
            child: const Text('Cancel invoice'),
          ),
        ],
      ),
    );
    final value = reason.trim();
    if (confirmed != true || !mounted) return;
    if (value.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Cancellation reason is required')),
      );
      return;
    }
    context.read<BillingBloc>().add(
      SubscriptionOrderCancelled(order['id'].toString(), value),
    );
  }

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<BillingBloc, BillingState>(
      listener: (context, state) async {
        if (state.error != null) {
          ScaffoldMessenger.of(
            context,
          ).showSnackBar(SnackBar(content: Text(state.error!)));
        }
        if (state.completedMutation == BillingMutation.downloadInvoice &&
            state.result is List<int>) {
          await savePdfFile(_downloadName ?? 'platform_invoice', state.result);
          if (context.mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Invoice downloaded as PDF.')),
            );
          }
        }
        if (state.completedMutation == BillingMutation.cancelOrder &&
            context.mounted) {
          ScaffoldMessenger.of(
            context,
          ).showSnackBar(const SnackBar(content: Text('Invoice cancelled')));
        }
      },
      builder: (context, state) {
        if (state.loading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (state.error != null && state.data == null) {
          return FailureState(state.error!, retry: _load);
        }
        _orders = state.data!.orders.map((item) => item.data).toList();
        _societies = state.data!.societies.map((item) => item.data).toList();
        final rows = _orders
            .where((o) => _status == 'ALL' || o['status'] == _status)
            .toList();
        final billed = _orders
            .where((o) => o['status'] != 'CANCELLED')
            .fold<double>(0, (s, o) => s + _number(o['amount']));
        final paid = _orders
            .where((o) => o['status'] == 'PAID')
            .fold<double>(0, (s, o) => s + _number(o['amount']));
        final pending = _orders
            .where(
              (o) => const {
                'DUE',
                'PAYMENT_PENDING',
                'SUBMITTED',
                'REJECTED',
              }.contains(o['status']),
            )
            .fold<double>(0, (s, o) => s + _number(o['amount']));
        final gateway = state.data!.gateway;
        return PageFrame(
          title: 'Platform membership billing',
          subtitle: 'Issue subscription invoices and track online payments',
          onRefresh: _load,
          toolbar: Wrap(
            spacing: 8,
            children: [
              FilledButton.icon(
                onPressed: _issue,
                icon: const Icon(Icons.note_add_outlined),
                label: const Text('Issue invoice'),
              ),
            ],
          ),
          child: Column(
            children: [
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: [
                  _Metric('Total billed', billed, SocietyColors.blue),
                  _Metric('Verified revenue', paid, SocietyColors.success),
                  _Metric('Outstanding', pending, SocietyColors.warning),
                  _Metric(
                    'Paid invoices',
                    _orders
                        .where((o) => o['status'] == 'PAID')
                        .length
                        .toDouble(),
                    SocietyColors.success,
                    money: false,
                  ),
                ],
              ),
              const SizedBox(height: 14),
              Card(
                child: ListTile(
                  leading: Icon(
                    gateway?['configured'] == true
                        ? Icons.verified_user_outlined
                        : Icons.warning_amber_outlined,
                    color: gateway?['configured'] == true
                        ? SocietyColors.success
                        : SocietyColors.warning,
                  ),
                  title: Text(
                    'Razorpay ${_label(gateway?['mode'] ?? 'UNCONFIGURED')}',
                  ),
                  subtitle: Text(
                    gateway?['configured'] == true
                        ? 'Server credentials and webhook secret are configured.'
                        : 'Payments cannot be received until server credentials and webhook secret are configured.',
                  ),
                ),
              ),
              const SizedBox(height: 14),
              DropdownButtonFormField<String>(
                initialValue: _status,
                decoration: const InputDecoration(labelText: 'Payment status'),
                items:
                    const [
                          'ALL',
                          'DUE',
                          'PAYMENT_PENDING',
                          'SUBMITTED',
                          'REJECTED',
                          'PAID',
                          'CANCELLED',
                        ]
                        .map(
                          (v) => DropdownMenuItem(
                            value: v,
                            child: Text(_label(v)),
                          ),
                        )
                        .toList(),
                onChanged: (v) => setState(() => _status = v!),
              ),
              const SizedBox(height: 14),
              Expanded(
                child: rows.isEmpty
                    ? const EmptyState(
                        'No subscription invoices match this status.',
                      )
                    : ListView.separated(
                        itemCount: rows.length,
                        separatorBuilder: (_, _) => const SizedBox(height: 8),
                        itemBuilder: (_, i) {
                          final o = rows[i];
                          return Card(
                            child: ListTile(
                              leading: IconButton(
                                tooltip: 'Download invoice PDF',
                                onPressed: () => _downloadInvoice(o),
                                icon: const Icon(Icons.download_outlined),
                              ),
                              title: Text(
                                '${o['society_name']} (${o['society_code'] ?? '—'}) · ${_money(o['amount'])}',
                              ),
                              subtitle: Text(
                                '${o['invoice_number']} · ${_label(o['billing_cycle'])} · ${_date(o['period_start'])} to ${_date(o['period_end'])} · Due ${_date(o['due_date'])}\nPayer: ${o['society_email'] ?? '—'} · ${o['society_mobile'] ?? '—'}${o['provider_reference'] == null ? '' : '\nOnline payment: ${_label(o['payment_method'])} · ${o['provider_reference']} · ${_dateTime(o['paid_at'])}'}${o['status'] == 'CANCELLED' ? '\nCancelled: ${o['rejection_reason'] ?? 'No reason recorded'}' : ''}',
                              ),
                              isThreeLine: true,
                              trailing: Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  if (o['status'] == 'PAYMENT_PENDING' &&
                                      (o['payment_attempts'] as List? ??
                                              const [])
                                          .any(
                                            (a) =>
                                                a['provider_payment_id'] !=
                                                null,
                                          ))
                                    IconButton(
                                      tooltip: 'Reconcile with Razorpay',
                                      onPressed: () =>
                                          context.read<BillingBloc>().add(
                                            SubscriptionPaymentReconciled(
                                              o['id'].toString(),
                                            ),
                                          ),
                                      icon: const Icon(Icons.sync),
                                    ),
                                  if (o['status'] == 'DUE')
                                    IconButton(
                                      tooltip: 'Cancel due invoice',
                                      onPressed: () => _cancelInvoice(o),
                                      icon: const Icon(Icons.cancel_outlined),
                                    ),
                                  Chip(label: Text(_label(o['status']))),
                                ],
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
}

class _IssueSubscriptionInvoice extends StatefulWidget {
  const _IssueSubscriptionInvoice({required this.societies});
  final List<Map<String, dynamic>> societies;
  @override
  State<_IssueSubscriptionInvoice> createState() =>
      _IssueSubscriptionInvoiceState();
}

class _IssueSubscriptionInvoiceState extends State<_IssueSubscriptionInvoice> {
  String? _societyId;
  DateTime _month = DateTime(DateTime.now().year, DateTime.now().month);
  DateTime _due = DateTime.now().add(const Duration(days: 15));

  Map<String, dynamic>? get _selectedSociety {
    for (final society in widget.societies) {
      if (society['id']?.toString() == _societyId) return society;
    }
    return null;
  }

  @override
  void initState() {
    super.initState();
    _societyId = widget.societies.firstOrNull?['id']?.toString();
  }

  void _save() {
    if (_societyId == null) return;
    Navigator.pop(context, {
      'societyId': _societyId,
      'billingMonth': _iso(_month),
      'dueDate': _iso(_due),
    });
  }

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Issue platform membership invoice'),
    content: SizedBox(
      width: 520,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          DropdownButtonFormField<String>(
            initialValue: _societyId,
            isExpanded: true,
            decoration: const InputDecoration(labelText: 'Society'),
            items: widget.societies
                .map(
                  (s) => DropdownMenuItem(
                    value: s['id'].toString(),
                    child: Text(
                      s['name'].toString(),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                )
                .toList(),
            onChanged: (v) => setState(() => _societyId = v),
          ),
          if (_selectedSociety != null) ...[
            const SizedBox(height: 10),
            Align(
              alignment: Alignment.centerLeft,
              child: Text(
                'Selected requirements: ${_selectedSociety!['total_flats'] ?? 0} spaces · ${_selectedSociety!['planned_staff'] ?? 0} staff · ${_label(_selectedSociety!['subscription_billing_cycle'] ?? 'MONTHLY')}\n'
                'Monthly total: ₹${_selectedSociety!['subscription_estimated_monthly'] ?? 0} · paid services: ₹${_selectedSociety!['subscription_service_monthly'] ?? 0}',
              ),
            ),
          ],
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: () async {
                    final p = await showDatePicker(
                      context: context,
                      initialDate: _month,
                      firstDate: DateTime(2024),
                      lastDate: DateTime(2035),
                    );
                    if (p != null) {
                      setState(() => _month = DateTime(p.year, p.month));
                    }
                  },
                  icon: const Icon(Icons.calendar_month),
                  label: Text('Month: ${_monthLabel(_month)}'),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: () async {
                    final p = await showDatePicker(
                      context: context,
                      initialDate: _due,
                      firstDate: _month,
                      lastDate: DateTime(2035),
                    );
                    if (p != null) setState(() => _due = p);
                  },
                  icon: const Icon(Icons.event),
                  label: Text('Due: ${_date(_due)}'),
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          const Text(
            'Amount and coverage period are calculated automatically from the society’s selected spaces, staff seats, billing cycle, and platform rates.',
          ),
        ],
      ),
    ),
    actions: [
      TextButton(
        onPressed: () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(onPressed: _save, child: const Text('Issue invoice')),
    ],
  );
}

class _Metric extends StatelessWidget {
  const _Metric(this.label, this.value, this.color, {this.money = true});
  final String label;
  final double value;
  final Color color;
  final bool money;
  @override
  Widget build(BuildContext context) => SizedBox(
    width: 210,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              money ? _money(value) : value.toInt().toString(),
              style: Theme.of(
                context,
              ).textTheme.titleLarge?.copyWith(color: color),
            ),
            Text(label, style: const TextStyle(color: SocietyColors.textMuted)),
          ],
        ),
      ),
    ),
  );
}

double _number(Object? v) => double.tryParse(v?.toString() ?? '') ?? 0;
String _money(Object? v) => '₹${_number(v).toStringAsFixed(2)}';
String _label(Object? v) => (v?.toString() ?? '')
    .toLowerCase()
    .split('_')
    .map((p) => p.isEmpty ? '' : '${p[0].toUpperCase()}${p.substring(1)}')
    .join(' ');
String _iso(DateTime d) =>
    '${d.year}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';
String _date(Object? v) {
  final d = v is DateTime ? v : DateTime.tryParse(v?.toString() ?? '');
  return d == null
      ? '—'
      : '${d.day.toString().padLeft(2, '0')}/${d.month.toString().padLeft(2, '0')}/${d.year}';
}

String _monthLabel(DateTime d) {
  const m = [
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
  return '${m[d.month - 1]} ${d.year}';
}

String _dateTime(Object? value) {
  final date = DateTime.tryParse(value?.toString() ?? '')?.toLocal();
  if (date == null) return '—';
  return '${_date(date)} ${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
}
