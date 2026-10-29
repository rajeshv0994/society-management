// ignore_for_file: use_build_context_synchronously

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'package:society_admin_app/core/presentation/widgets/common.dart';
import '../bloc/staff_bloc.dart';

class StaffDetailDialog extends StatefulWidget {
  const StaffDetailDialog({
    super.key,
    required this.staffId,
    required this.onManagePermissions,
  });

  final String staffId;
  final Future<void> Function() onManagePermissions;

  @override
  State<StaffDetailDialog> createState() => _StaffDetailDialogState();
}

class _StaffDetailDialogState extends State<StaffDetailDialog> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  String get keyName => context.read<StaffBloc>().staffKey(widget.staffId);
  void _load() => context.read<StaffBloc>().loadStaff(widget.staffId);

  void _message(String text) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
    }
  }

  Future<void> _employment(Map<String, dynamic> data) async {
    final joined = TextEditingController(
      text:
          data['joined_on']?.toString() ??
          DateTime.now().toIso8601String().substring(0, 10),
    );
    final dateOfBirth = TextEditingController(
      text: data['date_of_birth']?.toString(),
    );
    final alternateMobile = TextEditingController(
      text: data['alternate_mobile']?.toString(),
    );
    final address = TextEditingController(text: data['address']?.toString());
    final emergencyName = TextEditingController(
      text: data['emergency_contact_name']?.toString(),
    );
    final emergencyMobile = TextEditingController(
      text: data['emergency_contact_mobile']?.toString(),
    );
    final idType = TextEditingController(text: data['id_type']?.toString());
    final idNumber = TextEditingController(text: data['id_number']?.toString());
    final salary = TextEditingController(
      text: data['monthly_salary']?.toString(),
    );
    final bankAccountName = TextEditingController(
      text: data['bank_account_name']?.toString(),
    );
    final bankAccountNumber = TextEditingController(
      text: data['bank_account_number']?.toString(),
    );
    final bankIfsc = TextEditingController(text: data['bank_ifsc']?.toString());
    final upiId = TextEditingController(text: data['upi_id']?.toString());
    var gender = data['gender']?.toString();
    final saved = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Staff profile & employment'),
        content: SizedBox(
          width: 500,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                _heading('Personal identity'),
                _field(dateOfBirth, 'Date of birth (YYYY-MM-DD)'),
                DropdownButtonFormField<String>(
                  initialValue: gender,
                  decoration: const InputDecoration(labelText: 'Gender'),
                  items: const [
                    DropdownMenuItem(value: 'MALE', child: Text('Male')),
                    DropdownMenuItem(value: 'FEMALE', child: Text('Female')),
                    DropdownMenuItem(value: 'OTHER', child: Text('Other')),
                    DropdownMenuItem(
                      value: 'PREFER_NOT_TO_SAY',
                      child: Text('Prefer not to say'),
                    ),
                  ],
                  onChanged: (value) => gender = value,
                ),
                const SizedBox(height: 10),
                _field(alternateMobile, 'Alternate mobile'),
                _field(address, 'Residential address'),
                _field(idType, 'Government ID type'),
                _field(idNumber, 'Government ID number'),
                _heading('Emergency contact'),
                _field(emergencyName, 'Contact name'),
                _field(emergencyMobile, 'Contact mobile'),
                _heading('Employment'),
                _field(joined, 'Joined on (YYYY-MM-DD)'),
                _field(salary, 'Monthly salary', TextInputType.number),
                _heading('Payroll account'),
                _field(bankAccountName, 'Bank account holder name'),
                _field(bankAccountNumber, 'Bank account number'),
                _field(bankIfsc, 'Bank IFSC code'),
                _field(upiId, 'UPI ID'),
              ],
            ),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialogContext, true),
            child: const Text('Save'),
          ),
        ],
      ),
    );
    if (saved != true) return;
    try {
      DateTime.parse(joined.text.trim());
      final dob = dateOfBirth.text.trim().isEmpty
          ? null
          : DateTime.parse(dateOfBirth.text.trim());
      final monthlySalary = salary.text.trim().isEmpty
          ? null
          : double.parse(salary.text.trim());
      await context.read<StaffBloc>().updateEmployment(widget.staffId, {
        'joinedOn': joined.text.trim(),
        'dateOfBirth': dob?.toIso8601String().substring(0, 10),
        'gender': gender,
        'alternateMobile': alternateMobile.text.trim(),
        'address': address.text.trim(),
        'emergencyContactName': emergencyName.text.trim(),
        'emergencyContactMobile': emergencyMobile.text.trim(),
        'idType': idType.text.trim(),
        'idNumber': idNumber.text.trim(),
        'monthlySalary': monthlySalary,
        'bankAccountName': bankAccountName.text.trim(),
        'bankAccountNumber': bankAccountNumber.text.trim(),
        'bankIfsc': bankIfsc.text.trim().toUpperCase(),
        'upiId': upiId.text.trim(),
      });
      _load();
      _message('Employment details updated');
    } catch (error) {
      _message('Check the date and salary values: $error');
    }
  }

  Future<void> _markAttendance([Map? existing]) async {
    final date = TextEditingController(
      text:
          existing?['attendance_date']?.toString() ??
          DateTime.now().toIso8601String().substring(0, 10),
    );
    final checkIn = TextEditingController(
      text: existing?['check_in']?.toString().substring(0, 5) ?? '09:00',
    );
    final checkOut = TextEditingController(
      text: existing?['check_out']?.toString().substring(0, 5) ?? '18:00',
    );
    final note = TextEditingController(text: existing?['note']?.toString());
    var status = existing?['status']?.toString() ?? 'PRESENT';
    final saved = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => StatefulBuilder(
        builder: (context, update) => AlertDialog(
          title: Text(
            existing == null ? 'Mark attendance' : 'Correct attendance',
          ),
          content: SizedBox(
            width: 460,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                _field(date, 'Attendance date (YYYY-MM-DD)'),
                DropdownButtonFormField<String>(
                  initialValue: status,
                  decoration: const InputDecoration(labelText: 'Status'),
                  items: const [
                    DropdownMenuItem(value: 'PRESENT', child: Text('Present')),
                    DropdownMenuItem(value: 'ABSENT', child: Text('Absent')),
                    DropdownMenuItem(
                      value: 'HALF_DAY',
                      child: Text('Half day'),
                    ),
                    DropdownMenuItem(value: 'LEAVE', child: Text('Leave')),
                  ],
                  onChanged: (value) => update(() => status = value!),
                ),
                const SizedBox(height: 10),
                if (status == 'PRESENT' || status == 'HALF_DAY') ...[
                  _field(checkIn, 'Check-in (HH:mm)'),
                  _field(checkOut, 'Check-out (HH:mm)'),
                ],
                _field(note, 'Note'),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext, false),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(dialogContext, true),
              child: const Text('Save attendance'),
            ),
          ],
        ),
      ),
    );
    if (saved != true) return;
    try {
      await context.read<StaffBloc>().saveAttendance(widget.staffId, {
        'attendanceDate': date.text.trim(),
        'status': status,
        'checkIn': status == 'PRESENT' || status == 'HALF_DAY'
            ? '${checkIn.text.trim()}:00'
            : null,
        'checkOut': status == 'PRESENT' || status == 'HALF_DAY'
            ? '${checkOut.text.trim()}:00'
            : null,
        'note': note.text.trim(),
      });
      _load();
      _message('Attendance saved');
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _assignTask() async {
    final title = TextEditingController();
    final description = TextEditingController();
    final due = TextEditingController(
      text: DateTime.now().add(const Duration(days: 1)).toIso8601String(),
    );
    var priority = 'NORMAL';
    final saved = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => StatefulBuilder(
        builder: (context, update) => AlertDialog(
          title: const Text('Assign task'),
          content: SizedBox(
            width: 480,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                _field(title, 'Task title'),
                _field(description, 'Description'),
                _field(due, 'Due at (ISO date/time)'),
                DropdownButtonFormField<String>(
                  initialValue: priority,
                  decoration: const InputDecoration(labelText: 'Priority'),
                  items: const [
                    DropdownMenuItem(value: 'LOW', child: Text('Low')),
                    DropdownMenuItem(value: 'NORMAL', child: Text('Normal')),
                    DropdownMenuItem(value: 'HIGH', child: Text('High')),
                    DropdownMenuItem(value: 'URGENT', child: Text('Urgent')),
                  ],
                  onChanged: (value) => update(() => priority = value!),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext, false),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(dialogContext, true),
              child: const Text('Assign'),
            ),
          ],
        ),
      ),
    );
    if (saved != true) return;
    if (title.text.trim().isEmpty) return _message('Task title is required');
    try {
      final dueAt = due.text.trim();
      await context.read<StaffBloc>().assignTask(widget.staffId, {
        'title': title.text.trim(),
        'description': description.text.trim(),
        if (dueAt.isNotEmpty) 'dueAt': dueAt,
        'priority': priority,
      });
      _load();
      _message('Task assigned');
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _completeTask(Map task) async {
    final note = TextEditingController();
    final saved = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Complete task'),
        content: _field(note, 'Completion note'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialogContext, true),
            child: const Text('Complete'),
          ),
        ],
      ),
    );
    if (saved != true || note.text.trim().isEmpty) return;
    await context.read<StaffBloc>().completeTask(task['id'], note.text.trim());
    _load();
  }

  Future<void> _salaryPayment(Map<String, dynamic> data) async {
    final now = DateTime.now();
    final month = TextEditingController(
      text: '${now.year}-${now.month.toString().padLeft(2, '0')}-01',
    );
    final amount = TextEditingController(
      text: data['monthly_salary']?.toString(),
    );
    final paidOn = TextEditingController(
      text: now.toIso8601String().substring(0, 10),
    );
    final reference = TextEditingController();
    final note = TextEditingController();
    var method = 'BANK_TRANSFER';
    final saved = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => StatefulBuilder(
        builder: (context, update) => AlertDialog(
          title: const Text('Record salary payment'),
          content: SizedBox(
            width: 480,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                _field(month, 'Salary month (YYYY-MM-01)'),
                _field(amount, 'Amount', TextInputType.number),
                _field(paidOn, 'Paid on (YYYY-MM-DD)'),
                DropdownButtonFormField<String>(
                  initialValue: method,
                  decoration: const InputDecoration(
                    labelText: 'Payment method',
                  ),
                  items: const [
                    DropdownMenuItem(
                      value: 'BANK_TRANSFER',
                      child: Text('Bank transfer'),
                    ),
                    DropdownMenuItem(value: 'UPI', child: Text('UPI')),
                    DropdownMenuItem(value: 'CASH', child: Text('Cash')),
                    DropdownMenuItem(value: 'CHEQUE', child: Text('Cheque')),
                  ],
                  onChanged: (value) => update(() => method = value!),
                ),
                const SizedBox(height: 10),
                _field(reference, 'Reference number'),
                _field(note, 'Note'),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext, false),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(dialogContext, true),
              child: const Text('Record payment'),
            ),
          ],
        ),
      ),
    );
    if (saved != true) return;
    try {
      await context.read<StaffBloc>().recordSalary(widget.staffId, {
        'salaryMonth': month.text.trim(),
        'amount': double.parse(amount.text.trim()),
        'paidOn': paidOn.text.trim(),
        'paymentMethod': method,
        'referenceNumber': reference.text.trim(),
        'note': note.text.trim(),
      });
      _load();
      _message('Salary payment recorded');
    } catch (error) {
      _message(error.toString());
    }
  }

  Widget _field(
    TextEditingController controller,
    String label, [
    TextInputType keyboard = TextInputType.text,
  ]) => Padding(
    padding: const EdgeInsets.only(bottom: 10),
    child: TextField(
      controller: controller,
      keyboardType: keyboard,
      decoration: InputDecoration(labelText: label),
    ),
  );

  Widget _heading(String text) => Padding(
    padding: const EdgeInsets.fromLTRB(0, 10, 0, 8),
    child: Align(
      alignment: Alignment.centerLeft,
      child: Text(text, style: const TextStyle(fontWeight: FontWeight.w700)),
    ),
  );

  @override
  Widget build(BuildContext context) => Dialog(
    child: ConstrainedBox(
      constraints: const BoxConstraints(maxWidth: 1050, maxHeight: 760),
      child: BlocBuilder<StaffBloc, AdminResourceState>(
        builder: (context, state) {
          final snapshot = state.resource(keyName);
          if (snapshot.status == ResourceStatus.initial ||
              snapshot.status == ResourceStatus.loading) {
            return const SizedBox(
              height: 500,
              child: Center(child: CircularProgressIndicator()),
            );
          }
          if (snapshot.error != null) {
            return SizedBox(
              height: 400,
              child: ErrorState(message: snapshot.error!, onRetry: _load),
            );
          }
          final data = (snapshot.value as ResourceDocument).data;
          return DefaultTabController(
            length: 5,
            child: Column(
              children: [
                ListTile(
                  leading: const CircleAvatar(
                    child: Icon(Icons.badge_outlined),
                  ),
                  title: Text(data['full_name'].toString()),
                  subtitle: Text(
                    '${data['staff_type'] ?? 'STAFF'} • ${data['status']}',
                  ),
                  trailing: IconButton(
                    onPressed: () => Navigator.pop(context),
                    icon: const Icon(Icons.close),
                  ),
                ),
                const TabBar(
                  isScrollable: true,
                  tabs: [
                    Tab(text: 'Overview'),
                    Tab(text: 'Attendance'),
                    Tab(text: 'Tasks'),
                    Tab(text: 'Payroll'),
                    Tab(text: 'Access'),
                  ],
                ),
                Expanded(
                  child: TabBarView(
                    children: [
                      _overview(data),
                      _attendance(data),
                      _tasks(data),
                      _payroll(data),
                      _access(data),
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    ),
  );

  Widget _overview(Map<String, dynamic> data) => ListView(
    padding: const EdgeInsets.all(20),
    children: [
      Align(
        alignment: Alignment.centerRight,
        child: FilledButton.tonalIcon(
          onPressed: () => _employment(data),
          icon: const Icon(Icons.edit),
          label: const Text('Edit full profile'),
        ),
      ),
      Wrap(
        spacing: 12,
        runSpacing: 12,
        children: [
          _info('Email', data['email']),
          _info('Mobile', data['mobile']),
          _info('Alternate mobile', data['alternate_mobile']),
          _info('Date of birth', data['date_of_birth']),
          _info('Gender', data['gender']?.toString().replaceAll('_', ' ')),
          _info('Employee code', data['employee_code']),
          _info('Duty area', data['duty_area']),
          _info('Shift', data['shift_name']),
          _info('Joined on', data['joined_on']),
          _info(
            'Monthly salary',
            data['monthly_salary'] == null
                ? 'Not set'
                : '₹${data['monthly_salary']}',
          ),
          _info(
            'Government ID',
            '${data['id_type'] ?? ''} ${data['id_number'] ?? ''}',
          ),
          _info(
            'Emergency contact',
            '${data['emergency_contact_name'] ?? ''} ${data['emergency_contact_mobile'] ?? ''}',
          ),
          _info('Address', data['address']),
          _info('Bank account holder', data['bank_account_name']),
          _info('Bank account number', data['bank_account_number']),
          _info('IFSC code', data['bank_ifsc']),
          _info('UPI ID', data['upi_id']),
        ],
      ),
    ],
  );

  Widget _attendance(Map<String, dynamic> data) {
    final rows = data['attendance'] as List? ?? const [];
    final summary = data['attendance_summary'] as Map? ?? const {};
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Expanded(
                child: Text(
                  'Present ${summary['present'] ?? 0} • Absent ${summary['absent'] ?? 0} • Half day ${summary['half_day'] ?? 0} • Leave ${summary['leave'] ?? 0}',
                ),
              ),
              FilledButton.icon(
                onPressed: () => _markAttendance(),
                icon: const Icon(Icons.event_available),
                label: const Text('Mark attendance'),
              ),
            ],
          ),
        ),
        Expanded(
          child: rows.isEmpty
              ? const Center(child: Text('No attendance records.'))
              : ListView.builder(
                  itemCount: rows.length,
                  itemBuilder: (_, index) {
                    final row = rows[index] as Map;
                    return ListTile(
                      onTap: () => _markAttendance(row),
                      leading: const Icon(Icons.calendar_today),
                      title: Text(row['attendance_date'].toString()),
                      subtitle: Text(
                        '${row['check_in'] ?? '—'} to ${row['check_out'] ?? '—'} • ${row['note'] ?? ''}',
                      ),
                      trailing: Wrap(
                        crossAxisAlignment: WrapCrossAlignment.center,
                        children: [
                          StatusChip(row['status'].toString()),
                          IconButton(
                            tooltip: 'Correct attendance',
                            onPressed: () => _markAttendance(row),
                            icon: const Icon(Icons.edit_outlined),
                          ),
                        ],
                      ),
                    );
                  },
                ),
        ),
      ],
    );
  }

  Widget _tasks(Map<String, dynamic> data) {
    final rows = data['tasks'] as List? ?? const [];
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.all(16),
          child: Align(
            alignment: Alignment.centerRight,
            child: FilledButton.icon(
              onPressed: _assignTask,
              icon: const Icon(Icons.add_task),
              label: const Text('Assign task'),
            ),
          ),
        ),
        Expanded(
          child: rows.isEmpty
              ? const Center(child: Text('No tasks assigned.'))
              : ListView.builder(
                  itemCount: rows.length,
                  itemBuilder: (_, index) {
                    final task = rows[index] as Map;
                    return ListTile(
                      title: Text(task['title'].toString()),
                      subtitle: Text(
                        '${task['priority']} • Due ${task['due_at'] ?? 'Not set'}\n${task['description'] ?? ''}',
                      ),
                      isThreeLine: true,
                      trailing: task['status'] == 'COMPLETED'
                          ? const StatusChip('COMPLETED')
                          : FilledButton.tonal(
                              onPressed: () => _completeTask(task),
                              child: const Text('Complete'),
                            ),
                    );
                  },
                ),
        ),
      ],
    );
  }

  Widget _payroll(Map<String, dynamic> data) {
    final rows = data['salary_payments'] as List? ?? const [];
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Expanded(
                child: Text(
                  'Monthly salary: ${data['monthly_salary'] == null ? 'Not set' : '₹${data['monthly_salary']}'}',
                ),
              ),
              FilledButton.icon(
                onPressed: () => _salaryPayment(data),
                icon: const Icon(Icons.payments_outlined),
                label: const Text('Record salary payment'),
              ),
            ],
          ),
        ),
        Expanded(
          child: rows.isEmpty
              ? const Center(child: Text('No salary payments recorded.'))
              : ListView.builder(
                  itemCount: rows.length,
                  itemBuilder: (_, index) {
                    final row = rows[index] as Map;
                    return ListTile(
                      leading: const Icon(Icons.receipt_long),
                      title: Text('₹${row['amount']} • ${row['salary_month']}'),
                      subtitle: Text(
                        '${row['payment_method']} • Paid ${row['paid_on']} • ${row['reference_number'] ?? ''}',
                      ),
                    );
                  },
                ),
        ),
      ],
    );
  }

  Widget _access(Map<String, dynamic> data) {
    final permissions = data['permissions'] as List? ?? const [];
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        ListTile(
          leading: const Icon(Icons.admin_panel_settings_outlined),
          title: const Text('Module permissions'),
          subtitle: const Text(
            'Control which operational modules this staff member can use.',
          ),
          trailing: FilledButton.tonal(
            onPressed: () async {
              await widget.onManagePermissions();
              _load();
            },
            child: const Text('Manage'),
          ),
        ),
        const Divider(),
        if (permissions.isEmpty) const Text('No module permissions assigned.'),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            for (final permission in permissions)
              Chip(
                avatar: const Icon(Icons.check, size: 16),
                label: Text(permission.toString().replaceAll('_', ' ')),
              ),
          ],
        ),
      ],
    );
  }

  Widget _info(String label, dynamic value) => SizedBox(
    width: 290,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(label, style: const TextStyle(color: SocietyColors.textMuted)),
            const SizedBox(height: 4),
            Text(
              value?.toString().trim().isNotEmpty == true
                  ? value.toString()
                  : 'Not set',
            ),
          ],
        ),
      ),
    ),
  );
}
