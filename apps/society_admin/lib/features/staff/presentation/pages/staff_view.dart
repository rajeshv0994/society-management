import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_ui/society_ui.dart';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'package:society_admin_app/core/presentation/widgets/common.dart';
import '../bloc/staff_bloc.dart';
import 'staff_detail_dialog.dart';

class StaffView extends StatefulWidget {
  const StaffView({super.key});

  @override
  State<StaffView> createState() => _StaffViewState();
}

class _StaffViewState extends State<StaffView> {
  static const _fallbackPermissionOptions = <String, String>{
    'VISITORS': 'Visitor gate management',
    'COMPLAINTS': 'Complaints',
    'PAYMENTS': 'Maintenance payments and receipts',
    'TASKS': 'Assigned tasks',
    'NOTICES': 'Notices',
    'AMENITIES': 'Amenities',
    'HOUSEHOLDS': 'Household verification and access',
    'DELIVERIES': 'Delivery desk and secure handover',
    'EMERGENCY_INCIDENTS': 'Emergency and accident response',
    'ACTIVITIES_GALLERIES': 'Activity registrations, attendance, and galleries',
  };

  Map<String, String> _permissionOptions = _fallbackPermissionOptions;
  int? _staffSeats;
  String _query = '';
  String _status = 'ALL';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  void _load() {
    context.read<StaffBloc>().loadDirectory();
  }

  void _syncOptions(AdminResourceState state) {
    final profile = state.resource('staff-profile-options').value;
    if (profile is ResourceDocument) {
      _staffSeats = int.tryParse(
        profile.data['planned_staff']?.toString() ?? '',
      );
    }
    final configuration = state.resource('staff-configuration-options').value;
    if (configuration is! ResourceDocument) return;
    final options =
        (configuration.data['staff_permission_options'] as List? ?? const [])
            .map((item) => Map<String, dynamic>.from(item as Map))
            .where((item) => item['code'] != null && item['label'] != null);
    if (options.isNotEmpty) {
      _permissionOptions = {
        for (final option in options)
          option['code'].toString(): option['label'].toString(),
      };
    }
  }

  Future<void> _run(Future<void> Function() action, String success) async {
    try {
      await action();
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(success), behavior: SnackBarBehavior.floating),
      );
      _load();
    } catch (error) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(error.toString()),
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  Future<void> _staffDialog([Map<String, dynamic>? staff]) async {
    final editing = staff != null;
    final name = TextEditingController(text: staff?['full_name']?.toString());
    final email = TextEditingController(text: staff?['email']?.toString());
    final mobile = TextEditingController(text: staff?['mobile']?.toString());
    final employee = TextEditingController(
      text: staff?['employee_code']?.toString(),
    );
    final duty = TextEditingController(text: staff?['duty_area']?.toString());
    final shift = TextEditingController(text: staff?['shift_name']?.toString());
    final password = TextEditingController();
    var staffType = staff?['staff_type']?.toString() ?? 'SECURITY';
    final formKey = GlobalKey<FormState>();
    final saved = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: Text(editing ? 'Edit staff profile' : 'Add staff member'),
          content: SizedBox(
            width: 560,
            child: Form(
              key: formKey,
              child: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    _field(name, 'Full name', required: true),
                    _field(
                      email,
                      'Email address',
                      required: true,
                      keyboardType: TextInputType.emailAddress,
                    ),
                    _field(
                      mobile,
                      'Mobile number',
                      required: true,
                      keyboardType: TextInputType.phone,
                    ),
                    DropdownButtonFormField<String>(
                      initialValue: staffType,
                      decoration: const InputDecoration(
                        labelText: 'Staff type',
                      ),
                      items: const [
                        DropdownMenuItem(
                          value: 'SECURITY',
                          child: Text('Security'),
                        ),
                        DropdownMenuItem(
                          value: 'MANAGER',
                          child: Text('Society manager'),
                        ),
                        DropdownMenuItem(
                          value: 'ACCOUNTANT',
                          child: Text('Accountant'),
                        ),
                        DropdownMenuItem(
                          value: 'HOUSEKEEPING',
                          child: Text('Housekeeping'),
                        ),
                        DropdownMenuItem(
                          value: 'MAINTENANCE',
                          child: Text('Maintenance'),
                        ),
                        DropdownMenuItem(value: 'OTHER', child: Text('Other')),
                      ],
                      onChanged: (value) =>
                          setDialogState(() => staffType = value!),
                    ),
                    const SizedBox(height: 12),
                    _field(employee, 'Employee code'),
                    _field(duty, 'Duty area'),
                    _field(shift, 'Shift name'),
                    if (!editing)
                      _field(
                        password,
                        'Temporary password',
                        required: true,
                        obscure: true,
                        minLength: 8,
                      ),
                  ],
                ),
              ),
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext, false),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () {
                if (formKey.currentState!.validate()) {
                  Navigator.pop(dialogContext, true);
                }
              },
              child: Text(editing ? 'Save changes' : 'Create account'),
            ),
          ],
        ),
      ),
    );
    if (saved != true) return;
    await _run(() async {
      final body = {
        'fullName': name.text.trim(),
        'email': email.text.trim(),
        'mobile': mobile.text.trim(),
        'staffType': staffType,
        'employeeCode': employee.text.trim(),
        'dutyArea': duty.text.trim(),
        'shiftName': shift.text.trim(),
      };
      await context.read<StaffBloc>().saveStaff(
        body,
        id: staff?['id'],
        temporaryPassword: password.text,
      );
    }, editing ? 'Staff profile updated' : 'Staff account created');
  }

  Widget _field(
    TextEditingController controller,
    String label, {
    bool required = false,
    bool obscure = false,
    int minLength = 1,
    TextInputType? keyboardType,
  }) => Padding(
    padding: const EdgeInsets.only(bottom: 12),
    child: TextFormField(
      controller: controller,
      obscureText: obscure,
      keyboardType: keyboardType,
      decoration: InputDecoration(labelText: label),
      validator: (value) {
        if (required && (value == null || value.trim().isEmpty)) {
          return '$label is required';
        }
        if (required && (value?.length ?? 0) < minLength) {
          return 'Use at least $minLength characters';
        }
        return null;
      },
    ),
  );

  Future<void> _resetPassword(Map<String, dynamic> staff) async {
    final controller = TextEditingController();
    final key = GlobalKey<FormState>();
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Reset temporary password'),
        content: Form(
          key: key,
          child: SizedBox(
            width: 420,
            child: _field(
              controller,
              'New temporary password',
              required: true,
              obscure: true,
              minLength: 8,
            ),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              if (key.currentState!.validate()) {
                Navigator.pop(dialogContext, true);
              }
            },
            child: const Text('Reset password'),
          ),
        ],
      ),
    );
    if (confirmed != true) return;
    await _run(() async {
      await context.read<StaffBloc>().resetPassword(
        staff['id'],
        controller.text,
      );
    }, 'Temporary password reset');
  }

  Future<void> _permissions(Map<String, dynamic> staff) async {
    final selected = Set<String>.from(
      (staff['permissions'] as List? ?? []).map((item) => item.toString()),
    );
    final saved = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: Text('Permissions · ${staff['full_name']}'),
          content: SizedBox(
            width: 460,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    'Choose the modules this staff member can access.',
                    style: TextStyle(color: SocietyColors.textMuted),
                  ),
                ),
                const SizedBox(height: 12),
                for (final option in _permissionOptions.entries)
                  CheckboxListTile(
                    contentPadding: EdgeInsets.zero,
                    title: Text(option.value),
                    value: selected.contains(option.key),
                    onChanged: (value) => setDialogState(() {
                      value == true
                          ? selected.add(option.key)
                          : selected.remove(option.key);
                    }),
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
              child: const Text('Save permissions'),
            ),
          ],
        ),
      ),
    );
    if (saved != true) return;
    await _run(() async {
      await context.read<StaffBloc>().updatePermissions(staff['id'], selected);
    }, 'Staff permissions updated');
  }

  Future<void> _details(Map<String, dynamic> staff) async {
    final bloc = context.read<StaffBloc>();
    await showDialog<void>(
      context: context,
      builder: (_) => BlocProvider.value(
        value: bloc,
        child: StaffDetailDialog(
          staffId: staff['id'].toString(),
          onManagePermissions: () => _permissions(staff),
        ),
      ),
    );
    _load();
  }

  Future<void> _toggleStatus(Map<String, dynamic> staff) async {
    final active = staff['status'] == 'ACTIVE';
    await _run(() async {
      await context.read<StaffBloc>().setActive(staff['id'], !active);
    }, active ? 'Staff account deactivated' : 'Staff account activated');
  }

  @override
  Widget build(BuildContext context) => PageFrame(
    title: 'Staff directory',
    subtitle: 'Accounts, duties, shifts and module access',
    action: FilledButton.icon(
      onPressed: _staffDialog,
      icon: const Icon(Icons.person_add_alt_1),
      label: const Text('Add staff'),
    ),
    child: BlocBuilder<StaffBloc, AdminResourceState>(
      builder: (context, state) {
        _syncOptions(state);
        final snapshot = state.resource('staff');
        if (snapshot.status == ResourceStatus.initial ||
            snapshot.status == ResourceStatus.loading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.error != null) {
          return ErrorState(message: snapshot.error!, onRetry: _load);
        }
        final all =
            (snapshot.value as ResourceCollection?)?.items ??
            const <Map<String, dynamic>>[];
        final rows = all.where((staff) {
          final haystack =
              '${staff['full_name']} ${staff['email']} ${staff['staff_type']} '
                      '${staff['employee_code']} ${staff['duty_area']}'
                  .toLowerCase();
          return haystack.contains(_query.toLowerCase()) &&
              (_status == 'ALL' || staff['status'] == _status);
        }).toList();
        return Column(
          children: [
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                _Metric(
                  label: 'Total staff',
                  value: '${all.length}',
                  icon: Icons.groups_2_outlined,
                  color: SocietyColors.blue,
                ),
                _Metric(
                  label: 'Active',
                  value: '${all.where((s) => s['status'] == 'ACTIVE').length}',
                  icon: Icons.verified_user_outlined,
                  color: SocietyColors.success,
                ),
                if (_staffSeats != null)
                  _Metric(
                    label: 'Purchased seats',
                    value:
                        '${all.where((s) => s['status'] == 'ACTIVE').length}'
                        ' / $_staffSeats',
                    icon: Icons.badge_outlined,
                    color: const Color(0xFF6E42BD),
                  ),
                _Metric(
                  label: 'On inactive status',
                  value:
                      '${all.where((s) => s['status'] == 'INACTIVE').length}',
                  icon: Icons.person_off_outlined,
                  color: SocietyColors.warning,
                ),
              ],
            ),
            const SizedBox(height: 16),
            LayoutBuilder(
              builder: (context, constraints) {
                final search = TextField(
                  onChanged: (value) => setState(() => _query = value),
                  decoration: const InputDecoration(
                    hintText: 'Search staff by name, role, code or duty area',
                    prefixIcon: Icon(Icons.search),
                  ),
                );
                final status = DropdownButtonFormField<String>(
                  initialValue: _status,
                  decoration: const InputDecoration(labelText: 'Status'),
                  items: const [
                    DropdownMenuItem(value: 'ALL', child: Text('All')),
                    DropdownMenuItem(value: 'ACTIVE', child: Text('Active')),
                    DropdownMenuItem(
                      value: 'INACTIVE',
                      child: Text('Inactive'),
                    ),
                  ],
                  onChanged: (value) => setState(() => _status = value!),
                );
                if (constraints.maxWidth < 600) {
                  return Column(
                    children: [search, const SizedBox(height: 10), status],
                  );
                }
                return Row(
                  children: [
                    Expanded(child: search),
                    const SizedBox(width: 12),
                    SizedBox(width: 180, child: status),
                  ],
                );
              },
            ),
            const SizedBox(height: 16),
            Expanded(
              child: rows.isEmpty
                  ? const Empty('No staff match the current filters.')
                  : ListView.separated(
                      itemCount: rows.length,
                      separatorBuilder: (_, _) => const SizedBox(height: 10),
                      itemBuilder: (_, index) => _StaffCard(
                        staff: rows[index],
                        onView: () => _details(rows[index]),
                        onEdit: () => _staffDialog(rows[index]),
                        onPermissions: () => _permissions(rows[index]),
                        onResetPassword: () => _resetPassword(rows[index]),
                        onToggleStatus: () => _toggleStatus(rows[index]),
                      ),
                    ),
            ),
          ],
        );
      },
    ),
  );
}

class _Metric extends StatelessWidget {
  const _Metric({
    required this.label,
    required this.value,
    required this.icon,
    required this.color,
  });

  final String label;
  final String value;
  final IconData icon;
  final Color color;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: 210,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            CircleAvatar(
              backgroundColor: color.withValues(alpha: .12),
              foregroundColor: color,
              child: Icon(icon),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(value, style: Theme.of(context).textTheme.headlineSmall),
                  Text(
                    label,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
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

class _StaffCard extends StatelessWidget {
  const _StaffCard({
    required this.staff,
    required this.onView,
    required this.onEdit,
    required this.onPermissions,
    required this.onResetPassword,
    required this.onToggleStatus,
  });

  final Map<String, dynamic> staff;
  final VoidCallback onView;
  final VoidCallback onEdit;
  final VoidCallback onPermissions;
  final VoidCallback onResetPassword;
  final VoidCallback onToggleStatus;

  @override
  Widget build(BuildContext context) {
    final active = staff['status'] == 'ACTIVE';
    final permissions = (staff['permissions'] as List? ?? []);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            CircleAvatar(
              radius: 24,
              backgroundColor: SocietyColors.blue.withValues(alpha: .12),
              foregroundColor: SocietyColors.blue,
              child: Text(
                staff['full_name']
                    .toString()
                    .trim()
                    .substring(0, 1)
                    .toUpperCase(),
                style: const TextStyle(fontWeight: FontWeight.w700),
              ),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Wrap(
                    crossAxisAlignment: WrapCrossAlignment.center,
                    spacing: 10,
                    runSpacing: 6,
                    children: [
                      Text(
                        staff['full_name'].toString(),
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                      StatusChip(staff['status'].toString()),
                      Chip(
                        label: Text(
                          staff['staff_type']?.toString().replaceAll(
                                '_',
                                ' ',
                              ) ??
                              'STAFF',
                        ),
                        side: BorderSide.none,
                      ),
                    ],
                  ),
                  const SizedBox(height: 6),
                  Text(
                    '${staff['email']}  •  ${staff['mobile'] ?? 'No mobile'}',
                    style: const TextStyle(color: SocietyColors.textMuted),
                  ),
                  const SizedBox(height: 8),
                  Wrap(
                    spacing: 18,
                    runSpacing: 6,
                    children: [
                      _detail(Icons.badge_outlined, staff['employee_code']),
                      _detail(Icons.location_on_outlined, staff['duty_area']),
                      _detail(Icons.schedule_outlined, staff['shift_name']),
                    ],
                  ),
                  if (permissions.isNotEmpty) ...[
                    const SizedBox(height: 10),
                    Wrap(
                      spacing: 6,
                      runSpacing: 6,
                      children: [
                        for (final item in permissions)
                          Chip(
                            visualDensity: VisualDensity.compact,
                            avatar: const Icon(Icons.check, size: 15),
                            label: Text(item.toString().replaceAll('_', ' ')),
                          ),
                      ],
                    ),
                  ],
                ],
              ),
            ),
            PopupMenuButton<String>(
              tooltip: 'Staff actions',
              onSelected: (action) {
                switch (action) {
                  case 'view':
                    onView();
                    break;
                  case 'edit':
                    onEdit();
                    break;
                  case 'permissions':
                    onPermissions();
                    break;
                  case 'password':
                    onResetPassword();
                    break;
                  case 'status':
                    onToggleStatus();
                    break;
                }
              },
              itemBuilder: (_) => [
                const PopupMenuItem(
                  value: 'view',
                  child: Text('Open staff workspace'),
                ),
                const PopupMenuItem(value: 'edit', child: Text('Edit profile')),
                const PopupMenuItem(
                  value: 'permissions',
                  child: Text('Manage permissions'),
                ),
                const PopupMenuItem(
                  value: 'password',
                  child: Text('Reset password'),
                ),
                PopupMenuItem(
                  value: 'status',
                  child: Text(
                    active ? 'Deactivate account' : 'Activate account',
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _detail(IconData icon, dynamic value) => Row(
    mainAxisSize: MainAxisSize.min,
    children: [
      Icon(icon, size: 17, color: SocietyColors.textMuted),
      const SizedBox(width: 5),
      Text(
        value?.toString().trim().isNotEmpty == true
            ? value.toString()
            : 'Not set',
        style: const TextStyle(color: SocietyColors.textMuted),
      ),
    ],
  );
}
