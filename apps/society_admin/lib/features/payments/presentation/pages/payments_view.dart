// ignore_for_file: use_build_context_synchronously

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'package:society_admin_app/core/presentation/widgets/common.dart';
import '../bloc/payments_bloc.dart';

class PaymentsView extends StatefulWidget {
  const PaymentsView({super.key});

  @override
  State<PaymentsView> createState() => _PaymentsViewState();
}

class _PaymentsViewState extends State<PaymentsView> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => load());
  }

  void load() => context.read<PaymentsBloc>().loadPayments();

  Future<void> recordPayment() async {
    try {
      final invoices = await context.read<PaymentsBloc>().loadUnpaidInvoices();
      if (invoices.isEmpty) throw Exception('No unpaid invoices available');
      if (!mounted) return;
      final result = await showDialog<Map<String, dynamic>>(
        context: context,
        builder: (_) => _PaymentDialog(invoices: invoices),
      );
      if (result == null) return;
      final invoiceId = result.remove('invoiceId');
      await context.read<PaymentsBloc>().recordPayment(invoiceId, result);
      load();
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(error.toString())));
      }
    }
  }

  @override
  Widget build(BuildContext context) => PageFrame(
    title: 'Payments',
    subtitle: 'Record and review maintenance payment transactions',
    action: FilledButton.icon(
      onPressed: recordPayment,
      icon: const Icon(Icons.add_card),
      label: const Text('Record payment'),
    ),
    child: BlocBuilder<PaymentsBloc, AdminResourceState>(
      builder: (context, state) {
        final snapshot = state.resource('payments');
        if (snapshot.status == ResourceStatus.initial ||
            snapshot.status == ResourceStatus.loading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.error != null) {
          return ErrorState(message: snapshot.error!, onRetry: load);
        }
        final rows =
            (snapshot.value as ResourceCollection?)?.items ??
            const <Map<String, dynamic>>[];
        if (rows.isEmpty) return const Empty('No payments recorded.');
        return ListView.separated(
          itemCount: rows.length,
          separatorBuilder: (_, _) => const SizedBox(height: 8),
          itemBuilder: (context, index) {
            final payment = rows[index];
            return Card(
              child: ListTile(
                leading: const Icon(Icons.payments_outlined),
                title: Text(
                  '${payment['invoice_number'] ?? 'Invoice'} • ₹${payment['amount']}',
                ),
                subtitle: Text(
                  '${payment['payment_method']} • ${payment['provider_reference'] ?? 'No reference'} • ${payment['paid_at']}',
                ),
                trailing: StatusChip(payment['status'].toString()),
              ),
            );
          },
        );
      },
    ),
  );
}

class _PaymentDialog extends StatefulWidget {
  const _PaymentDialog({required this.invoices});

  final List<Map<String, dynamic>> invoices;

  @override
  State<_PaymentDialog> createState() => _PaymentDialogState();
}

class _PaymentDialogState extends State<_PaymentDialog> {
  late String invoiceId = widget.invoices.first['id'].toString();
  final amount = TextEditingController();
  final reference = TextEditingController();
  String method = 'CASH';

  @override
  Widget build(BuildContext context) => AlertDialog(
    title: const Text('Record payment'),
    content: SizedBox(
      width: 460,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          DropdownButtonFormField<String>(
            initialValue: invoiceId,
            decoration: const InputDecoration(labelText: 'Invoice'),
            items: widget.invoices
                .map(
                  (invoice) => DropdownMenuItem(
                    value: invoice['id'].toString(),
                    child: Text(
                      '${invoice['invoice_number']} • Due ₹${(invoice['subtotal'] ?? 0) + (invoice['late_fee'] ?? 0) - (invoice['paid_amount'] ?? 0)}',
                    ),
                  ),
                )
                .toList(),
            onChanged: (value) => invoiceId = value!,
          ),
          const SizedBox(height: 10),
          TextField(
            controller: amount,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(labelText: 'Amount'),
          ),
          const SizedBox(height: 10),
          DropdownButtonFormField<String>(
            initialValue: method,
            decoration: const InputDecoration(labelText: 'Payment method'),
            items: const ['CASH', 'BANK_TRANSFER', 'CHEQUE', 'UPI', 'CARD']
                .map(
                  (value) => DropdownMenuItem(value: value, child: Text(value)),
                )
                .toList(),
            onChanged: (value) => method = value!,
          ),
          const SizedBox(height: 10),
          TextField(
            controller: reference,
            decoration: const InputDecoration(
              labelText: 'Reference (optional)',
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
          final value = double.tryParse(amount.text);
          if (value == null || value <= 0) return;
          Navigator.pop(context, {
            'invoiceId': invoiceId,
            'amount': value,
            'paymentMethod': method,
            if (reference.text.trim().isNotEmpty)
              'providerReference': reference.text.trim(),
          });
        },
        child: const Text('Record'),
      ),
    ],
  );
}
