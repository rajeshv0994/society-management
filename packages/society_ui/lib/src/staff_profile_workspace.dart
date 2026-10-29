import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';
import 'async_data_bloc.dart';
import 'theme.dart';
import 'workspace_record.dart';

class StaffProfileWorkspace extends StatefulWidget {
  const StaffProfileWorkspace({super.key, required this.client});
  final BlocManagedApiGateway client;

  @override
  State<StaffProfileWorkspace> createState() => _StaffProfileWorkspaceState();
}

class _StaffProfileWorkspaceState extends State<StaffProfileWorkspace> {
  late final AsyncDataBloc<WorkspaceRecord> _profileBloc;

  @override
  void initState() {
    super.initState();
    _profileBloc = AsyncDataBloc(
      () async => WorkspaceRecord.from(
        await widget.client.get('/api/society/staff/me'),
      ),
    )..refresh();
  }

  Future<void> _load() => _profileBloc.refresh();

  @override
  void dispose() {
    _profileBloc.close();
    super.dispose();
  }

  @override
  Widget build(
    BuildContext context,
  ) => BlocBuilder<AsyncDataBloc<WorkspaceRecord>, AsyncDataState<WorkspaceRecord>>(
    bloc: _profileBloc,
    builder: (context, state) {
      if (state.data == null && state.status != AsyncDataStatus.failure) {
        return const Center(child: CircularProgressIndicator());
      }
      if (state.data == null) {
        return Center(
          child: FilledButton.icon(
            onPressed: _load,
            icon: const Icon(Icons.refresh),
            label: Text(state.error.toString()),
          ),
        );
      }
      final profile = state.data!;
      final attendance = profile['attendance_summary'] is WorkspaceRecord
          ? profile['attendance_summary'] as WorkspaceRecord
          : WorkspaceRecord.empty;
      final permissions = (profile['permissions'] as List? ?? const [])
          .map((value) => value.toString())
          .toList();
      final salaries = (profile['salary_payments'] as List? ?? const [])
          .whereType<WorkspaceRecord>()
          .toList();

      return RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Wrap(
                  spacing: 18,
                  runSpacing: 14,
                  crossAxisAlignment: WrapCrossAlignment.center,
                  children: [
                    const CircleAvatar(
                      radius: 34,
                      backgroundColor: SocietyColors.surfaceMuted,
                      child: Icon(
                        Icons.badge_outlined,
                        size: 34,
                        color: SocietyColors.blue,
                      ),
                    ),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          profile['full_name']?.toString() ?? 'Staff member',
                          style: Theme.of(context).textTheme.headlineSmall,
                        ),
                        Text(
                          '${_label(profile['staff_type'])} · ${profile['employee_code'] ?? 'No employee code'}',
                          style: const TextStyle(
                            color: SocietyColors.textMuted,
                          ),
                        ),
                        const SizedBox(height: 5),
                        _StatusChip(profile['status']?.toString() ?? 'ACTIVE'),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 14),
            _Section(
              title: 'Personal details',
              icon: Icons.person_outline,
              children: [
                _Fact('Email', profile['email']),
                _Fact('Mobile', profile['mobile']),
                _Fact('Alternate mobile', profile['alternate_mobile']),
                _Fact('Date of birth', _date(profile['date_of_birth'])),
                _Fact('Gender', _label(profile['gender'])),
                _Fact('Address', profile['address']),
                _Fact('Emergency contact', profile['emergency_contact_name']),
                _Fact('Emergency mobile', profile['emergency_contact_mobile']),
                _Fact(
                  'Identity',
                  [profile['id_type'], profile['id_number']]
                      .where(
                        (value) => value != null && value.toString().isNotEmpty,
                      )
                      .join(' · '),
                ),
              ],
            ),
            const SizedBox(height: 14),
            _Section(
              title: 'Employment',
              icon: Icons.work_outline,
              children: [
                _Fact('Staff type', _label(profile['staff_type'])),
                _Fact('Duty area', profile['duty_area']),
                _Fact('Shift', profile['shift_name']),
                _Fact('Joined on', _date(profile['joined_on'])),
                _Fact('Monthly salary', _money(profile['monthly_salary'])),
                _Fact('Bank account holder', profile['bank_account_name']),
                _Fact('Bank account number', profile['bank_account_number']),
                _Fact('IFSC code', profile['bank_ifsc']),
                _Fact('UPI ID', profile['upi_id']),
              ],
            ),
            const SizedBox(height: 14),
            _Section(
              title: 'Attendance summary',
              icon: Icons.fact_check_outlined,
              children: [
                _Fact('Present', attendance['present'] ?? 0),
                _Fact('Absent', attendance['absent'] ?? 0),
                _Fact('Half day', attendance['half_day'] ?? 0),
                _Fact('Leave', attendance['leave'] ?? 0),
              ],
            ),
            const SizedBox(height: 14),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Permissions',
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    const SizedBox(height: 10),
                    if (permissions.isEmpty)
                      const Text('No operational permissions assigned.')
                    else
                      Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        children: permissions
                            .map(
                              (permission) =>
                                  Chip(label: Text(_label(permission))),
                            )
                            .toList(),
                      ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 14),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Salary payment history',
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    const SizedBox(height: 8),
                    if (salaries.isEmpty)
                      const Text('No salary payment recorded yet.')
                    else
                      for (final salary in salaries)
                        ListTile(
                          contentPadding: EdgeInsets.zero,
                          leading: const Icon(Icons.payments_outlined),
                          title: Text(
                            '${_month(salary['salary_month'])} · ${_money(salary['amount'])}',
                          ),
                          subtitle: Text(
                            'Paid ${_date(salary['paid_on'])} · ${_label(salary['payment_method'])}${salary['reference_number'] == null ? '' : ' · ${salary['reference_number']}'}',
                          ),
                        ),
                  ],
                ),
              ),
            ),
          ],
        ),
      );
    },
  );
}

class _Section extends StatelessWidget {
  const _Section({
    required this.title,
    required this.icon,
    required this.children,
  });
  final String title;
  final IconData icon;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(18),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: SocietyColors.blue),
              const SizedBox(width: 8),
              Text(title, style: Theme.of(context).textTheme.titleLarge),
            ],
          ),
          const SizedBox(height: 14),
          Wrap(spacing: 18, runSpacing: 14, children: children),
        ],
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
    width: 210,
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(color: SocietyColors.textMuted, fontSize: 12),
        ),
        Text(
          value == null || value.toString().isEmpty ? '—' : value.toString(),
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
          style: const TextStyle(fontWeight: FontWeight.w600),
        ),
      ],
    ),
  );
}

class _StatusChip extends StatelessWidget {
  const _StatusChip(this.status);
  final String status;

  @override
  Widget build(BuildContext context) => Chip(
    avatar: Icon(
      status == 'ACTIVE' ? Icons.check_circle : Icons.pause_circle,
      size: 17,
      color: status == 'ACTIVE' ? SocietyColors.success : SocietyColors.warning,
    ),
    label: Text(_label(status)),
  );
}

String _label(Object? value) => (value?.toString() ?? '')
    .toLowerCase()
    .split('_')
    .map(
      (part) =>
          part.isEmpty ? '' : '${part[0].toUpperCase()}${part.substring(1)}',
    )
    .join(' ');

String _date(Object? value) {
  final date = DateTime.tryParse(value?.toString() ?? '');
  if (date == null) return value?.toString() ?? '—';
  return '${date.day.toString().padLeft(2, '0')}/${date.month.toString().padLeft(2, '0')}/${date.year}';
}

String _month(Object? value) {
  final date = DateTime.tryParse(value?.toString() ?? '');
  if (date == null) return value?.toString() ?? '—';
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

String _money(Object? value) {
  final amount = double.tryParse(value?.toString() ?? '');
  return amount == null ? '—' : '₹${amount.toStringAsFixed(2)}';
}
