import 'dart:collection';

class StaffVisitorRecord {
  const StaffVisitorRecord(this.fields);

  final StaffVisitorFields fields;

  String get id => fields['id']?.toString() ?? '';
  Object? operator [](String key) => fields[key];
}

class VisitorTarget {
  const VisitorTarget(this.fields);

  final StaffVisitorFields fields;
  Object? operator [](String key) => fields[key];
}

final class StaffVisitorFields extends MapBase<String, dynamic> {
  StaffVisitorFields._(this._values);

  factory StaffVisitorFields.from(Map values) => StaffVisitorFields._(
    Map.unmodifiable(
      values.map((key, value) => MapEntry(key.toString(), _freeze(value))),
    ),
  );

  final Map<String, dynamic> _values;

  static dynamic _freeze(dynamic value) {
    if (value is Map) return StaffVisitorFields.from(value);
    if (value is List) return List.unmodifiable(value.map(_freeze));
    return value;
  }

  @override
  dynamic operator [](Object? key) => _values[key];
  @override
  void operator []=(String key, dynamic value) =>
      throw UnsupportedError('StaffVisitorFields is immutable.');
  @override
  void clear() => throw UnsupportedError('StaffVisitorFields is immutable.');
  @override
  Iterable<String> get keys => _values.keys;
  @override
  dynamic remove(Object? key) =>
      throw UnsupportedError('StaffVisitorFields is immutable.');
}

class StaffVisitorWorkspaceData {
  const StaffVisitorWorkspaceData({
    required this.visitors,
    required this.canManage,
    required this.currentStaffId,
  });

  final List<StaffVisitorRecord> visitors;
  final bool canManage;
  final String currentStaffId;
}

class StaffVisitorRegistration {
  const StaffVisitorRegistration({
    required this.targetType,
    required this.visitorName,
    required this.mobile,
    required this.purpose,
    required this.vehicleNumber,
    required this.expectedAt,
    required this.accompanyingMembers,
    this.spaceId,
    this.hostResidentId,
    this.targetUserId,
    this.targetName,
  });

  final String targetType;
  final String visitorName;
  final String mobile;
  final String purpose;
  final String vehicleNumber;
  final DateTime expectedAt;
  final int accompanyingMembers;
  final String? spaceId;
  final String? hostResidentId;
  final String? targetUserId;
  final String? targetName;
}
