import 'dart:collection';

class ManagementRecord {
  const ManagementRecord(this.data);
  final ManagementFields data;
}

final class ManagementFields extends MapBase<String, dynamic> {
  ManagementFields._(this._values);

  factory ManagementFields.from(Map value) => ManagementFields._(
    Map.unmodifiable(
      value.map(
        (key, item) => MapEntry(key.toString(), _freezeManagementValue(item)),
      ),
    ),
  );

  final Map<String, dynamic> _values;
  @override
  dynamic operator [](Object? key) => _values[key];
  @override
  void operator []=(String key, dynamic value) =>
      throw UnsupportedError('ManagementFields is immutable.');
  @override
  void clear() => throw UnsupportedError('ManagementFields is immutable.');
  @override
  Iterable<String> get keys => _values.keys;
  @override
  dynamic remove(Object? key) =>
      throw UnsupportedError('ManagementFields is immutable.');
}

Object? _freezeManagementValue(Object? value) => switch (value) {
  Map value => ManagementFields.from(value),
  List value => List.unmodifiable(value.map(_freezeManagementValue)),
  _ => value,
};

enum ManagementResource { plans, notifications, orders, support }

class UpdatePlanCommand {
  const UpdatePlanCommand({
    required this.code,
    required this.approvalMode,
    required this.pricePerSpace,
    required this.pricePerStaff,
    required this.trialDurationValue,
    required this.trialDurationUnit,
    this.maxSpaces,
    this.maxStaff,
  });

  final String code;
  final String approvalMode;
  final int? maxSpaces;
  final double pricePerSpace;
  final int? maxStaff;
  final double pricePerStaff;
  final int trialDurationValue;
  final String trialDurationUnit;
}
