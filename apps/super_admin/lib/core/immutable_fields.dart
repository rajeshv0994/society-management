import 'dart:collection';

/// Deeply immutable generic fields for backend-configurable domain records.
final class ImmutableFields extends MapBase<String, dynamic> {
  ImmutableFields._(this._values);

  factory ImmutableFields.from(Map values) => ImmutableFields._(
    Map.unmodifiable(
      values.map((key, value) => MapEntry(key.toString(), _freeze(value))),
    ),
  );

  final Map<String, dynamic> _values;

  static Object? _freeze(Object? value) => switch (value) {
    Map value => ImmutableFields.from(value),
    List value => List.unmodifiable(value.map(_freeze)),
    _ => value,
  };

  @override
  dynamic operator [](Object? key) => _values[key];
  @override
  void operator []=(String key, dynamic value) =>
      throw UnsupportedError('ImmutableFields is immutable.');
  @override
  void clear() => throw UnsupportedError('ImmutableFields is immutable.');
  @override
  Iterable<String> get keys => _values.keys;
  @override
  dynamic remove(Object? key) =>
      throw UnsupportedError('ImmutableFields is immutable.');
}
