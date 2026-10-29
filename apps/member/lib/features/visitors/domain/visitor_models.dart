import 'dart:collection';

class VisitorRecord {
  const VisitorRecord(this.fields);

  final VisitorFields fields;

  String get id => fields['id']?.toString() ?? '';
  String get status => fields['status']?.toString() ?? '';
  int get photoCount => (fields['photo_count'] as num?)?.toInt() ?? 0;
  List<String> get photoUrls => List.unmodifiable(
    (fields['photo_urls'] as List? ?? const []).map((url) => url.toString()),
  );
  Object? operator [](String key) => fields[key];
}

final class VisitorFields extends MapBase<String, dynamic> {
  VisitorFields._(this._values);

  factory VisitorFields.from(Map values) => VisitorFields._(
    Map.unmodifiable(
      values.map((key, value) => MapEntry(key.toString(), _freeze(value))),
    ),
  );

  final Map<String, dynamic> _values;

  static dynamic _freeze(dynamic value) {
    if (value is Map) return VisitorFields.from(value);
    if (value is List) return List.unmodifiable(value.map(_freeze));
    return value;
  }

  @override
  dynamic operator [](Object? key) => _values[key];
  @override
  void operator []=(String key, dynamic value) =>
      throw UnsupportedError('VisitorFields is immutable.');
  @override
  void clear() => throw UnsupportedError('VisitorFields is immutable.');
  @override
  Iterable<String> get keys => _values.keys;
  @override
  dynamic remove(Object? key) =>
      throw UnsupportedError('VisitorFields is immutable.');
}

class VisitorSpace {
  const VisitorSpace({required this.id, required this.number, this.block});

  final String id;
  final String number;
  final String? block;
}

class MemberVisitorWorkspaceData {
  const MemberVisitorWorkspaceData({
    required this.visitors,
    required this.spaces,
  });

  final List<VisitorRecord> visitors;
  final List<VisitorSpace> spaces;
}

class VisitorRegistrationInput {
  const VisitorRegistrationInput({
    required this.spaceId,
    required this.visitorName,
    required this.mobile,
    required this.purpose,
    required this.vehicleNumber,
    required this.expectedAt,
    required this.accompanyingMembers,
  });

  final String spaceId;
  final String visitorName;
  final String mobile;
  final String purpose;
  final String vehicleNumber;
  final DateTime expectedAt;
  final int accompanyingMembers;
}
