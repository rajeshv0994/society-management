import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';
import 'async_data_bloc.dart';
import 'theme.dart';
import 'workspace_record.dart';

enum HouseholdWorkspaceRole { societyAdmin, staff, resident }

Widget buildResidentHouseholdWorkspace(BlocManagedApiGateway client) =>
    HouseholdWorkspace(client: client, role: HouseholdWorkspaceRole.resident);

Widget buildStaffHouseholdWorkspace(BlocManagedApiGateway client) =>
    HouseholdWorkspace(client: client, role: HouseholdWorkspaceRole.staff);

class HouseholdWorkspace extends StatefulWidget {
  const HouseholdWorkspace({
    super.key,
    required this.client,
    required this.role,
  });

  final BlocManagedApiGateway client;
  final HouseholdWorkspaceRole role;

  @override
  State<HouseholdWorkspace> createState() => _HouseholdWorkspaceState();
}

class _HouseholdWorkspaceState extends State<HouseholdWorkspace> {
  late final AsyncDataBloc<_HouseholdData> _bloc;
  Map<String, dynamic> get _data => _bloc.state.data?.data ?? const {};
  List<Map<String, dynamic>> get _parkingOptions =>
      _bloc.state.data?.parkingOptions ?? const [];
  String? _selectedResident;

  bool get _resident => widget.role == HouseholdWorkspaceRole.resident;
  bool get _manager => !_resident;

  @override
  void initState() {
    super.initState();
    _bloc = AsyncDataBloc(() => _HouseholdData.load(widget.client));
    _load();
  }

  Future<void> _load() => _bloc.refresh();

  @override
  void dispose() {
    _bloc.close();
    super.dispose();
  }

  Map<String, dynamic>? get _household {
    final rows = _maps(_data['households']);
    if (rows.isEmpty) return null;
    return rows.firstWhere(
      (row) => _residentId(row) == _selectedResident,
      orElse: () => rows.first,
    );
  }

  Future<void> _memberForm([Map<String, dynamic>? item]) async {
    final changed = await showDialog<bool>(
      context: context,
      builder: (_) => _HouseholdMemberForm(
        client: widget.client,
        residentId: _selectedResident,
        item: item,
      ),
    );
    if (changed == true) await _load();
  }

  Future<void> _vehicleForm([Map<String, dynamic>? item]) async {
    final changed = await showDialog<bool>(
      context: context,
      builder: (_) => _HouseholdVehicleForm(
        client: widget.client,
        residentId: _selectedResident,
        item: item,
        parkingOptions: _parkingOptions,
      ),
    );
    if (changed == true) await _load();
  }

  Future<void> _contactForm([Map<String, dynamic>? item]) async {
    final changed = await showDialog<bool>(
      context: context,
      builder: (_) => _HouseholdContactForm(
        client: widget.client,
        residentId: _selectedResident,
        item: item,
      ),
    );
    if (changed == true) await _load();
  }

  Future<void> _review(
    String kind,
    Map<String, dynamic> item,
    String status,
  ) async {
    String? note;
    if (status == 'REJECTED') {
      note = await _textDialog('Reject verification', 'Rejection note');
      if (note == null) return;
    } else if (!await _confirm(
      'Verify ${kind == 'members' ? 'household member' : 'vehicle'}',
      'Confirm that the submitted details have been checked?',
      'Verify',
    )) {
      return;
    }
    try {
      await widget.client
          .patch('/api/households/$kind/${item['id']}/verification', {
            'residentId': _selectedResident,
            'status': status,
            if (note != null) 'note': note,
          });
      await _load();
      _message('Verification updated.');
    } catch (error) {
      _message(error.toString());
    }
  }

  Future<void> _remove(String path, String label) async {
    if (!await _confirm(
      'Remove $label',
      'This record will be deactivated and retained for audit history.',
      'Remove',
    )) {
      return;
    }
    final resident = _manager ? '?residentId=$_selectedResident' : '';
    try {
      await widget.client.delete('$path$resident');
      await _load();
      _message('$label removed.');
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
    final value = await showDialog<String>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: Text(title),
        content: TextField(
          controller: controller,
          maxLength: 300,
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
    return value;
  }

  void _message(String message) {
    if (mounted) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text(message)));
    }
  }

  @override
  Widget build(
    BuildContext context,
  ) => BlocBuilder<AsyncDataBloc<_HouseholdData>, AsyncDataState<_HouseholdData>>(
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
      final households = _maps(_data['households']);
      if (households.isNotEmpty &&
          !households.any((row) => _residentId(row) == _selectedResident)) {
        _selectedResident = _residentId(households.first);
      }
      final household = _household;
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
                      _resident ? 'My household' : 'Household management',
                      style: Theme.of(context).textTheme.headlineMedium,
                    ),
                    Text(
                      _resident
                          ? 'Manage family, vehicles, emergency contacts, and unit details.'
                          : 'Verify household identities, vehicles, access, and emergency records.',
                      style: const TextStyle(color: SocietyColors.textMuted),
                    ),
                  ],
                ),
                if (_manager && households.isNotEmpty)
                  SizedBox(
                    width: 300,
                    child: DropdownButtonFormField<String>(
                      isExpanded: true,
                      initialValue: _selectedResident,
                      decoration: const InputDecoration(labelText: 'Resident'),
                      items: [
                        for (final row in households)
                          DropdownMenuItem(
                            value: _residentId(row),
                            child: Text(
                              '${row['resident']['full_name']} · ${_spaceLabel(row)}',
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                      ],
                      onChanged: (value) =>
                          setState(() => _selectedResident = value),
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                _Metric(
                  _manager ? 'Households' : 'Linked units',
                  '${_manager ? _data['resident_count'] ?? 0 : _maps(household?['spaces']).length}',
                  Icons.home_work_outlined,
                ),
                _Metric(
                  'Family members',
                  '${_manager ? _data['member_count'] ?? 0 : _maps(household?['household']).length}',
                  Icons.family_restroom,
                ),
                _Metric(
                  'Vehicles',
                  '${_manager ? _data['vehicle_count'] ?? 0 : _maps(household?['vehicles']).length}',
                  Icons.directions_car_outlined,
                ),
                if (_manager)
                  _Metric(
                    'Pending checks',
                    '${_data['pending_verification'] ?? 0}',
                    Icons.fact_check_outlined,
                  ),
              ],
            ),
            if (household == null) ...[
              const SizedBox(height: 20),
              const _Empty('No active household records are available.'),
            ] else ...[
              const SizedBox(height: 20),
              _ResidentSummary(household: household),
              const SizedBox(height: 16),
              _SectionHeader(
                title: 'Family and occupants',
                subtitle:
                    'Identity, contact, access, and verification details.',
                action: 'Add member',
                icon: Icons.person_add_alt,
                onPressed: _memberForm,
              ),
              const SizedBox(height: 8),
              if (_maps(household['household']).isEmpty)
                const _Empty('No household members added yet.')
              else
                for (final item in _maps(household['household'])) ...[
                  _MemberCard(
                    item: item,
                    manager: _manager,
                    onEdit: () => _memberForm(item),
                    onVerify: () => _review('members', item, 'VERIFIED'),
                    onReject: () => _review('members', item, 'REJECTED'),
                    onRemove: () => _remove(
                      '/api/households/members/${item['id']}',
                      'household member',
                    ),
                  ),
                  const SizedBox(height: 8),
                ],
              const SizedBox(height: 16),
              _SectionHeader(
                title: 'Vehicles and parking',
                subtitle:
                    'Registration, sticker, parking slot, and verification.',
                action: 'Add vehicle',
                icon: Icons.add_road,
                onPressed: _vehicleForm,
              ),
              const SizedBox(height: 8),
              if (_maps(household['vehicles']).isEmpty)
                const _Empty('No vehicles registered for this household.')
              else
                Wrap(
                  spacing: 10,
                  runSpacing: 10,
                  children: [
                    for (final item in _maps(household['vehicles']))
                      _VehicleCard(
                        item: item,
                        manager: _manager,
                        onEdit: () => _vehicleForm(item),
                        onVerify: () => _review('vehicles', item, 'VERIFIED'),
                        onReject: () => _review('vehicles', item, 'REJECTED'),
                        onRemove: () => _remove(
                          '/api/households/vehicles/${item['id']}',
                          'vehicle',
                        ),
                      ),
                  ],
                ),
              const SizedBox(height: 16),
              _SectionHeader(
                title: 'Emergency contacts',
                subtitle: 'Up to three contacts in escalation priority order.',
                action: 'Add contact',
                icon: Icons.contact_emergency_outlined,
                onPressed: _contactForm,
              ),
              const SizedBox(height: 8),
              if (_maps(household['emergency_contacts']).isEmpty)
                const _Empty('No emergency contacts configured.')
              else
                Wrap(
                  spacing: 10,
                  runSpacing: 10,
                  children: [
                    for (final item in _maps(household['emergency_contacts']))
                      _ContactCard(
                        item: item,
                        onEdit: () => _contactForm(item),
                        onRemove: () => _remove(
                          '/api/households/emergency-contacts/${item['id']}',
                          'emergency contact',
                        ),
                      ),
                  ],
                ),
            ],
          ],
        ),
      );
    },
  );
}

class _HouseholdData {
  const _HouseholdData({required this.data, required this.parkingOptions});
  final Map<String, dynamic> data;
  final List<Map<String, dynamic>> parkingOptions;

  static Future<_HouseholdData> load(BlocManagedApiGateway client) async {
    final data = Map<String, dynamic>.unmodifiable(
      Map<String, dynamic>.from(await client.get('/api/households') as Map),
    );
    List<Map<String, dynamic>> parking = const [];
    try {
      final reference = Map<String, dynamic>.from(
        await client.get('/api/configuration/reference') as Map,
      );
      parking = List.unmodifiable(_maps(reference['parking']));
    } catch (_) {
      // Household management remains available without the Parking module.
    }
    return _HouseholdData(data: data, parkingOptions: parking);
  }
}

class _ResidentSummary extends StatelessWidget {
  const _ResidentSummary({required this.household});
  final Map<String, dynamic> household;

  @override
  Widget build(BuildContext context) {
    final resident = Map<String, dynamic>.from(household['resident'] as Map);
    final spaces = _maps(household['spaces']);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              resident['full_name']?.toString() ?? 'Resident',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            Text(
              '${resident['mobile'] ?? 'No mobile'} · ${resident['email'] ?? 'No email'}',
              style: const TextStyle(color: SocietyColors.textMuted),
            ),
            const Divider(height: 24),
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: [
                for (final space in spaces)
                  Chip(
                    avatar: const Icon(Icons.apartment, size: 18),
                    label: Text(
                      '${space['block'] ?? ''} ${space['space_number']} · ${_label(space['resident_type'])} · ${_label(space['occupancy_status'])}',
                    ),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _MemberCard extends StatelessWidget {
  const _MemberCard({
    required this.item,
    required this.manager,
    required this.onEdit,
    required this.onVerify,
    required this.onReject,
    required this.onRemove,
  });
  final Map<String, dynamic> item;
  final bool manager;
  final VoidCallback onEdit, onVerify, onReject, onRemove;

  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(15),
      child: LayoutBuilder(
        builder: (context, constraints) {
          final details = Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Wrap(
                spacing: 8,
                runSpacing: 6,
                crossAxisAlignment: WrapCrossAlignment.center,
                children: [
                  Text(
                    item['full_name']?.toString() ?? 'Member',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  Chip(label: Text(_label(item['relationship']))),
                  _StatusChip(item['verification_status']),
                  if (item['access_enabled'] == true)
                    const Chip(label: Text('Access enabled')),
                ],
              ),
              Text(
                '${item['member_code'] ?? '—'} · ${item['mobile'] ?? 'No mobile'} · ${item['email'] ?? 'No email'}',
                style: const TextStyle(color: SocietyColors.textMuted),
              ),
              Text(
                'DOB ${item['date_of_birth'] ?? '—'} · ${_label(item['gender'])} · Blood ${item['blood_group'] ?? '—'} · ${item['occupation'] ?? 'Occupation not provided'}',
              ),
              if (item['identity_type'] != null)
                Text(
                  '${_label(item['identity_type'])} ending ${item['identity_last4'] ?? '—'}',
                ),
              if (item['emergency_contact_mobile'] != null)
                Text(
                  'Emergency: ${item['emergency_contact_name'] ?? ''} ${item['emergency_contact_mobile']}',
                ),
              if (item['verification_note'] != null)
                Text(
                  'Review note: ${item['verification_note']}',
                  style: const TextStyle(color: SocietyColors.danger),
                ),
            ],
          );
          final actions = Wrap(
            spacing: 6,
            runSpacing: 6,
            children: [
              if (manager && item['verification_status'] != 'VERIFIED')
                FilledButton.tonal(
                  onPressed: onVerify,
                  child: const Text('Verify'),
                ),
              if (manager && item['verification_status'] != 'REJECTED')
                TextButton(onPressed: onReject, child: const Text('Reject')),
              TextButton(onPressed: onEdit, child: const Text('Edit')),
              TextButton(onPressed: onRemove, child: const Text('Remove')),
            ],
          );
          if (constraints.maxWidth < 720) {
            return Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [details, const SizedBox(height: 10), actions],
            );
          }
          return Row(
            children: [
              Expanded(child: details),
              const SizedBox(width: 10),
              actions,
            ],
          );
        },
      ),
    ),
  );
}

class _VehicleCard extends StatelessWidget {
  const _VehicleCard({
    required this.item,
    required this.manager,
    required this.onEdit,
    required this.onVerify,
    required this.onReject,
    required this.onRemove,
  });
  final Map<String, dynamic> item;
  final bool manager;
  final VoidCallback onEdit, onVerify, onReject, onRemove;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: 360,
    child: Card(
      child: Padding(
        padding: const EdgeInsets.all(15),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Wrap(
              spacing: 7,
              runSpacing: 5,
              children: [
                Text(
                  item['registration_number']?.toString() ?? 'Vehicle',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                _StatusChip(item['verification_status']),
              ],
            ),
            Text(
              '${_label(item['vehicle_type'])} · ${item['make'] ?? ''} ${item['model'] ?? ''} · ${item['color'] ?? 'Color —'}',
            ),
            Text(
              'Parking ${item['parking_slot'] ?? '—'} · Sticker ${item['sticker_number'] ?? '—'}',
              style: const TextStyle(color: SocietyColors.textMuted),
            ),
            if (item['verification_note'] != null)
              Text('Review note: ${item['verification_note']}'),
            const SizedBox(height: 8),
            Wrap(
              spacing: 5,
              runSpacing: 5,
              children: [
                if (manager && item['verification_status'] != 'VERIFIED')
                  FilledButton.tonal(
                    onPressed: onVerify,
                    child: const Text('Verify'),
                  ),
                if (manager && item['verification_status'] != 'REJECTED')
                  TextButton(onPressed: onReject, child: const Text('Reject')),
                TextButton(onPressed: onEdit, child: const Text('Edit')),
                TextButton(onPressed: onRemove, child: const Text('Remove')),
              ],
            ),
          ],
        ),
      ),
    ),
  );
}

class _ContactCard extends StatelessWidget {
  const _ContactCard({
    required this.item,
    required this.onEdit,
    required this.onRemove,
  });
  final Map<String, dynamic> item;
  final VoidCallback onEdit, onRemove;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: 330,
    child: Card(
      child: ListTile(
        leading: CircleAvatar(child: Text('${item['priority'] ?? 1}')),
        title: Text(item['full_name']?.toString() ?? 'Contact'),
        subtitle: Text(
          '${_label(item['relationship'])} · ${item['mobile']}${item['alternate_mobile'] == null ? '' : '\nAlternate ${item['alternate_mobile']}'}',
        ),
        trailing: PopupMenuButton<String>(
          onSelected: (value) => value == 'EDIT' ? onEdit() : onRemove(),
          itemBuilder: (_) => const [
            PopupMenuItem(value: 'EDIT', child: Text('Edit')),
            PopupMenuItem(value: 'REMOVE', child: Text('Remove')),
          ],
        ),
      ),
    ),
  );
}

class _SectionHeader extends StatelessWidget {
  const _SectionHeader({
    required this.title,
    required this.subtitle,
    required this.action,
    required this.icon,
    required this.onPressed,
  });
  final String title, subtitle, action;
  final IconData icon;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) => Wrap(
    spacing: 12,
    runSpacing: 8,
    alignment: WrapAlignment.spaceBetween,
    crossAxisAlignment: WrapCrossAlignment.center,
    children: [
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: Theme.of(context).textTheme.titleLarge),
          Text(
            subtitle,
            style: const TextStyle(color: SocietyColors.textMuted),
          ),
        ],
      ),
      FilledButton.tonalIcon(
        onPressed: onPressed,
        icon: Icon(icon),
        label: Text(action),
      ),
    ],
  );
}

class _Metric extends StatelessWidget {
  const _Metric(this.label, this.value, this.icon);
  final String label, value;
  final IconData icon;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: 205,
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

class _StatusChip extends StatelessWidget {
  const _StatusChip(this.status);
  final Object? status;

  @override
  Widget build(BuildContext context) => Chip(
    avatar: Icon(
      status == 'VERIFIED'
          ? Icons.verified
          : status == 'REJECTED'
          ? Icons.cancel_outlined
          : Icons.schedule,
      size: 17,
    ),
    label: Text(_label(status)),
  );
}

class _Empty extends StatelessWidget {
  const _Empty(this.message);
  final String message;

  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(24),
      child: Center(child: Text(message)),
    ),
  );
}

class _HouseholdMemberForm extends StatefulWidget {
  const _HouseholdMemberForm({
    required this.client,
    required this.residentId,
    this.item,
  });
  final BlocManagedApiGateway client;
  final String? residentId;
  final Map<String, dynamic>? item;

  @override
  State<_HouseholdMemberForm> createState() => _HouseholdMemberFormState();
}

class _HouseholdMemberFormState extends State<_HouseholdMemberForm> {
  late final _name = _controller('full_name');
  late final _relationship = _controller('relationship');
  late final _mobile = _controller('mobile');
  late final _email = _controller('email');
  late final _dob = _controller('date_of_birth');
  late final _occupation = _controller('occupation');
  late final _identityLast4 = _controller('identity_last4');
  late final _emergencyName = _controller('emergency_contact_name');
  late final _emergencyMobile = _controller('emergency_contact_mobile');
  late String _gender =
      widget.item?['gender']?.toString() ?? 'PREFER_NOT_TO_SAY';
  late String _blood = widget.item?['blood_group']?.toString() ?? 'O+';
  late String _identity =
      widget.item?['identity_type']?.toString() ?? 'AADHAAR';
  late bool _access = widget.item?['access_enabled'] == true;
  bool _saving = false;
  String? _error;

  TextEditingController _controller(String key) =>
      TextEditingController(text: widget.item?[key]?.toString() ?? '');

  @override
  void dispose() {
    for (final controller in [
      _name,
      _relationship,
      _mobile,
      _email,
      _dob,
      _occupation,
      _identityLast4,
      _emergencyName,
      _emergencyMobile,
    ]) {
      controller.dispose();
    }
    super.dispose();
  }

  Future<void> _save() async {
    if (_name.text.trim().isEmpty ||
        _relationship.text.trim().isEmpty ||
        (_identityLast4.text.isNotEmpty &&
            !RegExp(
              r'^[A-Za-z0-9]{4}$',
            ).hasMatch(_identityLast4.text.trim()))) {
      setState(
        () =>
            _error = 'Name, relationship, and identity details must be valid.',
      );
      return;
    }
    setState(() {
      _saving = true;
      _error = null;
    });
    final body = {
      if (widget.residentId != null) 'residentId': widget.residentId,
      'fullName': _name.text.trim(),
      'relationship': _relationship.text.trim(),
      'mobile': _mobile.text.trim(),
      'email': _email.text.trim(),
      if (_dob.text.trim().isNotEmpty) 'dateOfBirth': _dob.text.trim(),
      'gender': _gender,
      'bloodGroup': _blood,
      'occupation': _occupation.text.trim(),
      'identityType': _identity,
      if (_identityLast4.text.trim().isNotEmpty)
        'identityLast4': _identityLast4.text.trim(),
      'emergencyContactName': _emergencyName.text.trim(),
      'emergencyContactMobile': _emergencyMobile.text.trim(),
      'accessEnabled': _access,
    };
    try {
      if (widget.item == null) {
        await widget.client.post('/api/households/members', body);
      } else {
        await widget.client.put(
          '/api/households/members/${widget.item!['id']}',
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
    title: Text(
      widget.item == null ? 'Add household member' : 'Edit household member',
    ),
    content: SizedBox(
      width: 690,
      child: SingleChildScrollView(
        child: Wrap(
          spacing: 10,
          runSpacing: 10,
          children: [
            _field(_name, 'Full name'),
            _field(_relationship, 'Relationship'),
            _field(_mobile, 'Mobile'),
            _field(_email, 'Email'),
            _field(_dob, 'Date of birth (YYYY-MM-DD)'),
            _field(_occupation, 'Occupation'),
            _dropdown('Gender', _gender, const [
              'MALE',
              'FEMALE',
              'OTHER',
              'PREFER_NOT_TO_SAY',
            ], (value) => _gender = value),
            _dropdown('Blood group', _blood, const [
              'A+',
              'A-',
              'B+',
              'B-',
              'AB+',
              'AB-',
              'O+',
              'O-',
            ], (value) => _blood = value),
            _dropdown('Identity type', _identity, const [
              'AADHAAR',
              'PASSPORT',
              'DRIVING_LICENCE',
              'VOTER_ID',
              'OTHER',
            ], (value) => _identity = value),
            _field(_identityLast4, 'Identity last 4 characters'),
            _field(_emergencyName, 'Emergency contact name'),
            _field(_emergencyMobile, 'Emergency contact mobile'),
            SizedBox(
              width: 330,
              child: SwitchListTile.adaptive(
                value: _access,
                onChanged: (value) => setState(() => _access = value),
                title: const Text('Society access enabled'),
              ),
            ),
            if (_error != null)
              SizedBox(
                width: 670,
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
        child: Text(_saving ? 'Saving…' : 'Save member'),
      ),
    ],
  );

  Widget _field(TextEditingController controller, String label) => SizedBox(
    width: 330,
    child: TextField(
      controller: controller,
      decoration: InputDecoration(labelText: label),
    ),
  );

  Widget _dropdown(
    String label,
    String value,
    List<String> values,
    ValueChanged<String> assign,
  ) => SizedBox(
    width: 330,
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

class _HouseholdVehicleForm extends StatefulWidget {
  const _HouseholdVehicleForm({
    required this.client,
    required this.residentId,
    required this.parkingOptions,
    this.item,
  });
  final BlocManagedApiGateway client;
  final String? residentId;
  final List<Map<String, dynamic>> parkingOptions;
  final Map<String, dynamic>? item;

  @override
  State<_HouseholdVehicleForm> createState() => _HouseholdVehicleFormState();
}

class _HouseholdVehicleFormState extends State<_HouseholdVehicleForm> {
  late final _registration = _controller('registration_number');
  late final _make = _controller('make');
  late final _model = _controller('model');
  late final _color = _controller('color');
  late final _parking = _controller('parking_slot');
  late final _sticker = _controller('sticker_number');
  late String _type = widget.item?['vehicle_type']?.toString() ?? 'CAR';
  bool _saving = false;
  String? _error;

  TextEditingController _controller(String key) =>
      TextEditingController(text: widget.item?[key]?.toString() ?? '');

  List<Map<String, dynamic>> get _selectableParking => widget.parkingOptions
      .where(
        (option) =>
            option['status'] == 'AVAILABLE' ||
            option['slot_number']?.toString() == _parking.text,
      )
      .toList();

  String get _selectedParking =>
      _selectableParking.any(
        (option) => option['slot_number']?.toString() == _parking.text,
      )
      ? _parking.text
      : '';

  @override
  void dispose() {
    for (final controller in [
      _registration,
      _make,
      _model,
      _color,
      _parking,
      _sticker,
    ]) {
      controller.dispose();
    }
    super.dispose();
  }

  Future<void> _save() async {
    if (!RegExp(r'^[A-Za-z0-9 -]{4,20}$').hasMatch(_registration.text.trim())) {
      setState(() => _error = 'Enter a valid vehicle registration number.');
      return;
    }
    setState(() {
      _saving = true;
      _error = null;
    });
    final body = {
      if (widget.residentId != null) 'residentId': widget.residentId,
      'vehicleType': _type,
      'registrationNumber': _registration.text.trim(),
      'make': _make.text.trim(),
      'model': _model.text.trim(),
      'color': _color.text.trim(),
      'parkingSlot': _parking.text.trim(),
      'stickerNumber': _sticker.text.trim(),
    };
    try {
      if (widget.item == null) {
        await widget.client.post('/api/households/vehicles', body);
      } else {
        await widget.client.put(
          '/api/households/vehicles/${widget.item!['id']}',
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
    title: Text(widget.item == null ? 'Add vehicle' : 'Edit vehicle'),
    content: SizedBox(
      width: 680,
      child: SingleChildScrollView(
        child: Wrap(
          spacing: 10,
          runSpacing: 10,
          children: [
            SizedBox(
              width: 325,
              child: DropdownButtonFormField<String>(
                isExpanded: true,
                initialValue: _type,
                decoration: const InputDecoration(labelText: 'Vehicle type'),
                items: [
                  for (final value in const [
                    'CAR',
                    'MOTORCYCLE',
                    'SCOOTER',
                    'BICYCLE',
                    'OTHER',
                  ])
                    DropdownMenuItem(value: value, child: Text(_label(value))),
                ],
                onChanged: (value) => setState(() => _type = value!),
              ),
            ),
            _field(_registration, 'Registration number'),
            _field(_make, 'Make'),
            _field(_model, 'Model'),
            _field(_color, 'Color'),
            if (_selectableParking.isEmpty)
              _field(_parking, 'Parking slot')
            else
              SizedBox(
                width: 325,
                child: DropdownButtonFormField<String>(
                  isExpanded: true,
                  initialValue: _selectedParking,
                  decoration: const InputDecoration(labelText: 'Parking slot'),
                  items: [
                    const DropdownMenuItem(
                      value: '',
                      child: Text('No parking slot'),
                    ),
                    for (final option in _selectableParking)
                      DropdownMenuItem(
                        value: option['slot_number'].toString(),
                        child: Text(
                          '${option['slot_number']} · ${_label(option['slot_type'])} · ${_label(option['status'])}',
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                  ],
                  onChanged: (value) =>
                      setState(() => _parking.text = value ?? ''),
                ),
              ),
            _field(_sticker, 'Sticker number'),
            if (_error != null)
              SizedBox(
                width: 660,
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
        child: Text(_saving ? 'Saving…' : 'Save vehicle'),
      ),
    ],
  );

  Widget _field(TextEditingController controller, String label) => SizedBox(
    width: 325,
    child: TextField(
      controller: controller,
      decoration: InputDecoration(labelText: label),
    ),
  );
}

class _HouseholdContactForm extends StatefulWidget {
  const _HouseholdContactForm({
    required this.client,
    required this.residentId,
    this.item,
  });
  final BlocManagedApiGateway client;
  final String? residentId;
  final Map<String, dynamic>? item;

  @override
  State<_HouseholdContactForm> createState() => _HouseholdContactFormState();
}

class _HouseholdContactFormState extends State<_HouseholdContactForm> {
  late final _name = _controller('full_name');
  late final _relationship = _controller('relationship');
  late final _mobile = _controller('mobile');
  late final _alternate = _controller('alternate_mobile');
  late int _priority =
      int.tryParse(widget.item?['priority']?.toString() ?? '') ?? 1;
  bool _saving = false;
  String? _error;

  TextEditingController _controller(String key) =>
      TextEditingController(text: widget.item?[key]?.toString() ?? '');

  @override
  void dispose() {
    for (final controller in [_name, _relationship, _mobile, _alternate]) {
      controller.dispose();
    }
    super.dispose();
  }

  Future<void> _save() async {
    if (_name.text.trim().isEmpty ||
        _relationship.text.trim().isEmpty ||
        _mobile.text.trim().length < 7) {
      setState(() => _error = 'Name, relationship, and mobile are required.');
      return;
    }
    setState(() {
      _saving = true;
      _error = null;
    });
    final body = {
      if (widget.residentId != null) 'residentId': widget.residentId,
      'fullName': _name.text.trim(),
      'relationship': _relationship.text.trim(),
      'mobile': _mobile.text.trim(),
      'alternateMobile': _alternate.text.trim(),
      'priority': _priority,
    };
    try {
      if (widget.item == null) {
        await widget.client.post('/api/households/emergency-contacts', body);
      } else {
        await widget.client.put(
          '/api/households/emergency-contacts/${widget.item!['id']}',
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
    title: Text(
      widget.item == null ? 'Add emergency contact' : 'Edit emergency contact',
    ),
    content: SizedBox(
      width: 540,
      child: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            _field(_name, 'Full name'),
            const SizedBox(height: 10),
            _field(_relationship, 'Relationship'),
            const SizedBox(height: 10),
            _field(_mobile, 'Mobile'),
            const SizedBox(height: 10),
            _field(_alternate, 'Alternate mobile'),
            const SizedBox(height: 10),
            DropdownButtonFormField<int>(
              isExpanded: true,
              initialValue: _priority,
              decoration: const InputDecoration(
                labelText: 'Escalation priority',
              ),
              items: const [
                DropdownMenuItem(value: 1, child: Text('1 · Contact first')),
                DropdownMenuItem(value: 2, child: Text('2 · Contact second')),
                DropdownMenuItem(value: 3, child: Text('3 · Contact third')),
              ],
              onChanged: (value) => setState(() => _priority = value!),
            ),
            if (_error != null) ...[
              const SizedBox(height: 8),
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
        onPressed: _saving ? null : () => Navigator.pop(context),
        child: const Text('Cancel'),
      ),
      FilledButton(
        onPressed: _saving ? null : _save,
        child: Text(_saving ? 'Saving…' : 'Save contact'),
      ),
    ],
  );

  Widget _field(TextEditingController controller, String label) => TextField(
    controller: controller,
    decoration: InputDecoration(labelText: label),
  );
}

List<WorkspaceRecord> _maps(Object? value) => workspaceRecords(value);

String _residentId(Map<String, dynamic> household) =>
    (household['resident'] as Map)['id'].toString();

String _spaceLabel(Map<String, dynamic> household) {
  final spaces = _maps(household['spaces']);
  if (spaces.isEmpty) return 'No unit';
  return '${spaces.first['block'] ?? ''} ${spaces.first['space_number'] ?? ''}'
      .trim();
}

String _label(Object? value) => (value?.toString() ?? '')
    .toLowerCase()
    .split('_')
    .map(
      (part) =>
          part.isEmpty ? '' : '${part[0].toUpperCase()}${part.substring(1)}',
    )
    .join(' ');
