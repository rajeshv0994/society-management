import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';
import 'async_data_bloc.dart';
import 'theme.dart';
import 'workspace_record.dart';

enum _BillingView { expenses, salaries }

class SocietyExpenseWorkspace extends StatefulWidget {
  const SocietyExpenseWorkspace({super.key, required this.client});
  final BlocManagedApiGateway client;

  @override
  State<SocietyExpenseWorkspace> createState() =>
      _SocietyExpenseWorkspaceState();
}

class _SocietyExpenseWorkspaceState extends State<SocietyExpenseWorkspace> {
  late final AsyncDataBloc<_ExpenseData> _bloc;
  List<Map<String, dynamic>> get _rows => _bloc.state.data?.rows ?? const [];
  List<Map<String, dynamic>> get _staff => _bloc.state.data?.staff ?? const [];
  Map<String, dynamic> get _summary => _bloc.state.data?.summary ?? const {};
  _BillingView _view = _BillingView.expenses;
  String _query = '';

  @override
  void initState() {
    super.initState();
    _bloc = AsyncDataBloc(() => _ExpenseData.load(widget.client));
    _load();
  }

  Future<void> _load() => _bloc.refresh();

  @override
  void dispose() {
    _bloc.close();
    super.dispose();
  }

  Future<void> _add() async {
    final created = await showDialog<bool>(
      context: context,
      builder: (_) => _ExpenseDialog(client: widget.client),
    );
    if (created == true) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Society expense recorded.')),
        );
      }
      await _load();
    }
  }

  Future<void> _setBudget() async {
    final changed = await showDialog<bool>(
      context: context,
      builder: (_) => _BudgetDialog(client: widget.client),
    );
    if (changed == true) await _load();
  }

  Future<void> _recordSalary(Map<String, dynamic> staff) async {
    final changed = await showDialog<bool>(
      context: context,
      builder: (_) => _SalaryDialog(client: widget.client, staff: staff),
    );
    if (changed == true) await _load();
  }

  @override
  Widget build(
    BuildContext context,
  ) => BlocBuilder<AsyncDataBloc<_ExpenseData>, AsyncDataState<_ExpenseData>>(
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
      final rows = _rows.where((row) {
        final q = _query.trim().toLowerCase();
        if (q.isEmpty) return true;
        return [
          row['category'],
          row['vendor'],
          row['description'],
          row['payment_reference'],
        ].any((value) => value?.toString().toLowerCase().contains(q) == true);
      }).toList();
      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Wrap(
              alignment: WrapAlignment.spaceBetween,
              crossAxisAlignment: WrapCrossAlignment.center,
              spacing: 16,
              runSpacing: 12,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Society billing & expenses',
                      style: Theme.of(context).textTheme.headlineMedium,
                    ),
                    const SizedBox(height: 4),
                    const Text(
                      'Available budget combines received member payments and manual additions, less society expenses and staff salaries.',
                      style: TextStyle(color: SocietyColors.textMuted),
                    ),
                  ],
                ),
                Wrap(
                  spacing: 8,
                  children: [
                    OutlinedButton.icon(
                      onPressed: _setBudget,
                      icon: const Icon(Icons.account_balance),
                      label: const Text('Add budget'),
                    ),
                    if (_view == _BillingView.expenses)
                      FilledButton.icon(
                        onPressed: _add,
                        icon: const Icon(Icons.add),
                        label: const Text('Record expense'),
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
                _ExpenseMetric(
                  'Available budget (${_summary['year'] ?? ''})',
                  _money(_number(_summary['total_budget'])),
                  Icons.account_balance,
                  SocietyColors.blue,
                ),
                _ExpenseMetric(
                  'Member payments received',
                  _money(_number(_summary['received_payments'])),
                  Icons.payments_outlined,
                  SocietyColors.success,
                ),
                _ExpenseMetric(
                  'Manual additions',
                  _money(_number(_summary['manual_budget'])),
                  Icons.add_card,
                  SocietyColors.blue,
                ),
                _ExpenseMetric(
                  'Total spent',
                  _money(_number(_summary['total_spent'])),
                  Icons.account_balance_wallet_outlined,
                  SocietyColors.danger,
                ),
                _ExpenseMetric(
                  'Remaining',
                  _money(_number(_summary['remaining'])),
                  Icons.savings_outlined,
                  SocietyColors.success,
                ),
                if (_number(_summary['over_budget']) > 0)
                  _ExpenseMetric(
                    'Over budget',
                    _money(_number(_summary['over_budget'])),
                    Icons.warning_amber_rounded,
                    SocietyColors.danger,
                  ),
              ],
            ),
            const SizedBox(height: 18),
            SegmentedButton<_BillingView>(
              segments: const [
                ButtonSegment(
                  value: _BillingView.expenses,
                  icon: Icon(Icons.receipt_long),
                  label: Text('Society expenses'),
                ),
                ButtonSegment(
                  value: _BillingView.salaries,
                  icon: Icon(Icons.badge_outlined),
                  label: Text('Staff salaries'),
                ),
              ],
              selected: {_view},
              onSelectionChanged: (values) =>
                  setState(() => _view = values.first),
            ),
            const SizedBox(height: 14),
            TextField(
              onChanged: (value) => setState(() => _query = value),
              decoration: InputDecoration(
                prefixIcon: const Icon(Icons.search),
                labelText: _view == _BillingView.expenses
                    ? 'Search category, vendor, description, or reference'
                    : 'Search staff name or role',
              ),
            ),
            const SizedBox(height: 14),
            if (_view == _BillingView.salaries)
              _salaryList()
            else if (rows.isEmpty)
              const Card(
                child: Padding(
                  padding: EdgeInsets.all(36),
                  child: Center(child: Text('No society expenses recorded.')),
                ),
              )
            else
              for (final row in rows)
                Card(
                  child: ListTile(
                    leading: const CircleAvatar(
                      child: Icon(Icons.receipt_long_outlined),
                    ),
                    title: Text(
                      '${_label(row['category'])} · ${_money(_number(row['amount']))}',
                    ),
                    subtitle: Text(
                      '${row['description']}${row['vendor'] == null ? '' : ' · ${row['vendor']}'}\n${_date(row['expense_date'])} · ${_label(row['payment_method'])}${row['payment_reference'] == null ? '' : ' · ${row['payment_reference']}'}',
                    ),
                    isThreeLine: true,
                  ),
                ),
          ],
        ),
      );
    },
  );

  Widget _salaryList() {
    final query = _query.trim().toLowerCase();
    final rows = _staff.where((staff) {
      if (query.isEmpty) return true;
      return [
        staff['staff_name'],
        staff['staff_type'],
      ].any((value) => value?.toString().toLowerCase().contains(query) == true);
    }).toList();
    if (rows.isEmpty) {
      return const Card(
        child: Padding(
          padding: EdgeInsets.all(36),
          child: Center(child: Text('No staff salary records found.')),
        ),
      );
    }
    return Column(
      children: [
        for (final staff in rows)
          Card(
            child: ListTile(
              leading: const CircleAvatar(child: Icon(Icons.badge_outlined)),
              title: Text(staff['staff_name']?.toString() ?? 'Staff member'),
              subtitle: Text(
                '${_label(staff['staff_type'])} · Monthly salary ${_money(_number(staff['monthly_salary']))}\n${(staff['payments'] as List? ?? const []).isEmpty ? 'No salary payment recorded' : 'Last paid ${_date((staff['payments'] as List).first['paid_on'])} · ${_money(_number((staff['payments'] as List).first['amount']))}'}',
              ),
              isThreeLine: true,
              trailing: FilledButton.tonal(
                onPressed: () => _recordSalary(staff),
                child: const Text('Record salary'),
              ),
            ),
          ),
      ],
    );
  }
}

class _ExpenseData {
  const _ExpenseData({
    required this.rows,
    required this.staff,
    required this.summary,
  });
  final List<WorkspaceRecord> rows;
  final List<WorkspaceRecord> staff;
  final WorkspaceRecord summary;

  static Future<_ExpenseData> load(BlocManagedApiGateway client) async {
    final values = await Future.wait([
      client.get('/api/billing/expenses'),
      client.get('/api/billing/finance-summary'),
      client.get('/api/billing/staff-salaries'),
    ]);
    return _ExpenseData(
      rows: workspaceRecords(values[0]),
      summary: WorkspaceRecord.from(values[1]),
      staff: workspaceRecords(values[2]),
    );
  }
}

class _BudgetDialog extends StatefulWidget {
  const _BudgetDialog({required this.client});
  final BlocManagedApiGateway client;

  @override
  State<_BudgetDialog> createState() => _BudgetDialogState();
}

class _BudgetDialogState extends State<_BudgetDialog> {
  final _amount = TextEditingController();
  final _note = TextEditingController();
  bool _saving = false;
  String? _error;

  @override
  void dispose() {
    _amount.dispose();
    _note.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (_number(_amount.text) < 0) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await widget.client.post('/api/billing/budget/additions', {
        'amount': _number(_amount.text),
        if (_note.text.trim().isNotEmpty) 'note': _note.text.trim(),
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
    title: Text('Add to ${DateTime.now().year} society budget'),
    content: SizedBox(
      width: 430,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextFormField(
            controller: _amount,
            autofocus: true,
            keyboardType: const TextInputType.numberWithOptions(decimal: true),
            decoration: const InputDecoration(
              labelText: 'Amount to add',
              helperText: 'This amount is added to received member payments',
              prefixText: '₹ ',
            ),
          ),
          const SizedBox(height: 12),
          TextFormField(
            controller: _note,
            maxLength: 240,
            decoration: const InputDecoration(
              labelText: 'Source / note (optional)',
              hintText: 'Reserve fund, donation, opening balance, etc.',
            ),
          ),
          if (_error != null)
            Padding(
              padding: const EdgeInsets.only(top: 12),
              child: Text(
                _error!,
                style: const TextStyle(color: SocietyColors.danger),
              ),
            ),
        ],
      ),
    ),
    actions: [
      TextButton(
        onPressed: _saving ? null : () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(
        onPressed: _saving ? null : _save,
        child: Text(_saving ? 'Adding…' : 'Add to budget'),
      ),
    ],
  );
}

class _SalaryDialog extends StatefulWidget {
  const _SalaryDialog({required this.client, required this.staff});
  final BlocManagedApiGateway client;
  final Map<String, dynamic> staff;

  @override
  State<_SalaryDialog> createState() => _SalaryDialogState();
}

class _SalaryDialogState extends State<_SalaryDialog> {
  final _form = GlobalKey<FormState>();
  late final _amount = TextEditingController(
    text: _number(widget.staff['monthly_salary']).toStringAsFixed(2),
  );
  final _reference = TextEditingController();
  final _note = TextEditingController();
  DateTime _month = DateTime(DateTime.now().year, DateTime.now().month);
  DateTime _paidOn = DateTime.now();
  String _method = 'BANK_TRANSFER';
  bool _saving = false;
  String? _error;

  @override
  void dispose() {
    _amount.dispose();
    _reference.dispose();
    _note.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_form.currentState!.validate()) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await widget.client.post(
        '/api/society/staff/${widget.staff['staff_id']}/salary-payments',
        {
          'salaryMonth': _isoDate(_month),
          'amount': double.parse(_amount.text.trim()),
          'paidOn': _isoDate(_paidOn),
          'paymentMethod': _method,
          if (_reference.text.trim().isNotEmpty)
            'referenceNumber': _reference.text.trim(),
          if (_note.text.trim().isNotEmpty) 'note': _note.text.trim(),
        },
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
    title: Text('Record salary · ${widget.staff['staff_name']}'),
    content: SizedBox(
      width: 520,
      child: Form(
        key: _form,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextFormField(
              controller: _amount,
              keyboardType: const TextInputType.numberWithOptions(
                decimal: true,
              ),
              decoration: const InputDecoration(
                labelText: 'Salary paid',
                prefixText: '₹ ',
              ),
              validator: (value) =>
                  _number(value) <= 0 ? 'Enter a valid salary amount' : null,
            ),
            const SizedBox(height: 12),
            DropdownButtonFormField<String>(
              initialValue: _method,
              decoration: const InputDecoration(labelText: 'Payment method'),
              items: const ['BANK_TRANSFER', 'UPI', 'CHEQUE', 'CASH']
                  .map(
                    (value) => DropdownMenuItem(
                      value: value,
                      child: Text(_label(value)),
                    ),
                  )
                  .toList(),
              onChanged: (value) => setState(() => _method = value!),
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _reference,
              decoration: const InputDecoration(labelText: 'Reference number'),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () async {
                      final picked = await showDatePicker(
                        context: context,
                        initialDate: _month,
                        firstDate: DateTime(2020),
                        lastDate: DateTime.now(),
                      );
                      if (picked != null) {
                        setState(
                          () => _month = DateTime(picked.year, picked.month),
                        );
                      }
                    },
                    icon: const Icon(Icons.calendar_month),
                    label: Text('Salary month: ${_month.month}/${_month.year}'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () async {
                      final picked = await showDatePicker(
                        context: context,
                        initialDate: _paidOn,
                        firstDate: DateTime(2020),
                        lastDate: DateTime.now(),
                      );
                      if (picked != null) setState(() => _paidOn = picked);
                    },
                    icon: const Icon(Icons.event),
                    label: Text('Paid: ${_date(_paidOn)}'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _note,
              maxLength: 250,
              decoration: const InputDecoration(labelText: 'Note (optional)'),
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
        child: Text(_saving ? 'Saving…' : 'Record salary'),
      ),
    ],
  );
}

class _ExpenseDialog extends StatefulWidget {
  const _ExpenseDialog({required this.client});
  final BlocManagedApiGateway client;
  @override
  State<_ExpenseDialog> createState() => _ExpenseDialogState();
}

class _ExpenseDialogState extends State<_ExpenseDialog> {
  final _form = GlobalKey<FormState>();
  final _vendor = TextEditingController();
  final _description = TextEditingController();
  final _amount = TextEditingController();
  final _reference = TextEditingController();
  String _category = 'UTILITIES';
  String _method = 'BANK_TRANSFER';
  DateTime _dateValue = DateTime.now();
  bool _saving = false;
  String? _error;

  @override
  void dispose() {
    _vendor.dispose();
    _description.dispose();
    _amount.dispose();
    _reference.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_form.currentState!.validate()) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await widget.client.post('/api/billing/expenses', {
        'category': _category,
        if (_vendor.text.trim().isNotEmpty) 'vendor': _vendor.text.trim(),
        'description': _description.text.trim(),
        'amount': double.parse(_amount.text.trim()),
        'expenseDate': _isoDate(_dateValue),
        'paymentMethod': _method,
        if (_reference.text.trim().isNotEmpty)
          'paymentReference': _reference.text.trim(),
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
    title: const Text('Record society expense'),
    content: SizedBox(
      width: 560,
      child: SingleChildScrollView(
        child: Form(
          key: _form,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              DropdownButtonFormField<String>(
                initialValue: _category,
                decoration: const InputDecoration(
                  labelText: 'Expense category',
                ),
                items:
                    const [
                          'UTILITIES',
                          'REPAIRS',
                          'SECURITY',
                          'HOUSEKEEPING',
                          'VENDOR',
                          'PURCHASE',
                          'OTHER',
                        ]
                        .map((v) => DropdownMenuItem(value: v, child: Text(v)))
                        .toList(),
                onChanged: (v) => setState(() => _category = v!),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _description,
                decoration: const InputDecoration(
                  labelText: 'What was this expense for?',
                ),
                maxLength: 300,
                validator: (v) => v == null || v.trim().isEmpty
                    ? 'Enter a description'
                    : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _vendor,
                decoration: const InputDecoration(
                  labelText: 'Vendor / paid to (optional)',
                ),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _amount,
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                ),
                decoration: const InputDecoration(
                  labelText: 'Amount',
                  prefixText: '₹ ',
                ),
                validator: (v) =>
                    _number(v) <= 0 ? 'Enter a valid amount' : null,
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<String>(
                initialValue: _method,
                decoration: const InputDecoration(labelText: 'Payment method'),
                items: const ['BANK_TRANSFER', 'UPI', 'CHEQUE', 'CASH', 'CARD']
                    .map(
                      (v) => DropdownMenuItem(value: v, child: Text(_label(v))),
                    )
                    .toList(),
                onChanged: (v) => setState(() => _method = v!),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _reference,
                decoration: const InputDecoration(
                  labelText: 'Payment / cheque reference',
                ),
                validator: (v) =>
                    const {'BANK_TRANSFER', 'CHEQUE'}.contains(_method) &&
                        (v == null || v.trim().isEmpty)
                    ? 'Reference is required'
                    : null,
              ),
              const SizedBox(height: 12),
              OutlinedButton.icon(
                onPressed: () async {
                  final picked = await showDatePicker(
                    context: context,
                    initialDate: _dateValue,
                    firstDate: DateTime(2020),
                    lastDate: DateTime.now(),
                  );
                  if (picked != null) setState(() => _dateValue = picked);
                },
                icon: const Icon(Icons.event),
                label: Text('Expense date: ${_date(_dateValue)}'),
              ),
              if (_error != null)
                Padding(
                  padding: const EdgeInsets.only(top: 12),
                  child: Text(
                    _error!,
                    style: const TextStyle(color: SocietyColors.danger),
                  ),
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
        child: Text(_saving ? 'Saving…' : 'Record expense'),
      ),
    ],
  );
}

class _ExpenseMetric extends StatelessWidget {
  const _ExpenseMetric(this.label, this.value, this.icon, this.color);
  final String label;
  final String value;
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
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    label,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(color: SocietyColors.textMuted),
                  ),
                  FittedBox(
                    fit: BoxFit.scaleDown,
                    alignment: Alignment.centerLeft,
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

double _number(Object? value) => double.tryParse(value?.toString() ?? '') ?? 0;
String _money(double value) => '₹${value.toStringAsFixed(2)}';
String _label(Object? value) => (value?.toString() ?? '')
    .toLowerCase()
    .split('_')
    .map((p) => p.isEmpty ? '' : '${p[0].toUpperCase()}${p.substring(1)}')
    .join(' ');
String _isoDate(DateTime value) =>
    '${value.year}-${value.month.toString().padLeft(2, '0')}-${value.day.toString().padLeft(2, '0')}';
String _date(Object? value) {
  final date = value is DateTime
      ? value
      : DateTime.tryParse(value?.toString() ?? '');
  return date == null
      ? '—'
      : '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year}';
}
