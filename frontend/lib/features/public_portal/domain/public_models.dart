import 'dart:collection';

final class PublicRecord extends MapBase<String, dynamic> {
  PublicRecord._(this._values);

  factory PublicRecord.from(Map values) => PublicRecord._(
    Map.unmodifiable(
      values.map((key, value) => MapEntry(key.toString(), _freeze(value))),
    ),
  );

  final Map<String, dynamic> _values;

  static Object? _freeze(Object? value) => switch (value) {
    Map value => PublicRecord.from(value),
    List value => List.unmodifiable(value.map(_freeze)),
    _ => value,
  };

  @override
  dynamic operator [](Object? key) => _values[key];
  @override
  void operator []=(String key, dynamic value) =>
      throw UnsupportedError('PublicRecord is immutable.');
  @override
  void clear() => throw UnsupportedError('PublicRecord is immutable.');
  @override
  Iterable<String> get keys => _values.keys;
  @override
  dynamic remove(Object? key) =>
      throw UnsupportedError('PublicRecord is immutable.');
}

class LoginCommand {
  const LoginCommand({
    required this.email,
    required this.password,
    this.societyCode,
  });
  final String email;
  final String password;
  final String? societyCode;
}

class SocietyRegistrationCommand {
  const SocietyRegistrationCommand({
    required this.name,
    required this.societyType,
    required this.totalBlocks,
    required this.totalSpaces,
    required this.plannedStaff,
    required this.approximateResidents,
    required this.address,
    required this.city,
    required this.state,
    required this.pinCode,
    required this.contactPerson,
    required this.email,
    required this.planCode,
    required this.enabledModules,
  });
  final String name,
      societyType,
      address,
      city,
      state,
      pinCode,
      contactPerson,
      email,
      planCode;
  final int totalBlocks, totalSpaces, plannedStaff;
  final int? approximateResidents;
  final List<String> enabledModules;
}

class SocietyActivationCommand {
  const SocietyActivationCommand({
    required this.setupCode,
    required this.fullName,
    required this.email,
    required this.mobile,
    required this.password,
  });
  final String setupCode, fullName, email, mobile, password;
}

class RequirementCommand {
  const RequirementCommand({
    required this.societyName,
    required this.contactPerson,
    required this.email,
    required this.mobile,
    required this.city,
    required this.estimatedSpaces,
    required this.estimatedStaff,
    required this.requestedModules,
    required this.requirementDetails,
    required this.budgetMonthly,
  });
  final String societyName,
      contactPerson,
      email,
      mobile,
      city,
      requirementDetails;
  final int? estimatedSpaces, estimatedStaff;
  final List<String> requestedModules;
  final double? budgetMonthly;
}
