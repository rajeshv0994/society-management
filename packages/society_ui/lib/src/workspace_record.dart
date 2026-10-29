import 'dart:collection';

/// Immutable generic model for workspace payloads whose fields are configured
/// by the backend and therefore do not have one stable feature schema.
final class WorkspaceRecord extends MapBase<String, dynamic> {
  const WorkspaceRecord._(this._values);

  factory WorkspaceRecord.from(Object? value) {
    if (value is! Map) {
      throw const FormatException('Expected a JSON object.');
    }
    return WorkspaceRecord._(
      Map.unmodifiable(
        value.map(
          (key, item) => MapEntry(key.toString(), _freezeWorkspaceValue(item)),
        ),
      ),
    );
  }

  static const empty = WorkspaceRecord._({});

  final Map<String, dynamic> _values;

  @override
  dynamic operator [](Object? key) => _values[key];

  @override
  void operator []=(String key, dynamic value) =>
      throw UnsupportedError('WorkspaceRecord is immutable.');

  @override
  void clear() => throw UnsupportedError('WorkspaceRecord is immutable.');

  @override
  Iterable<String> get keys => _values.keys;

  @override
  dynamic remove(Object? key) =>
      throw UnsupportedError('WorkspaceRecord is immutable.');
}

Object? _freezeWorkspaceValue(Object? value) => switch (value) {
  Map value => WorkspaceRecord.from(value),
  List value => List.unmodifiable(value.map(_freezeWorkspaceValue)),
  _ => value,
};

List<WorkspaceRecord> workspaceRecords(Object? value) =>
    List.unmodifiable((value as List? ?? const []).map(WorkspaceRecord.from));
