import '../../../../core/immutable_fields.dart';

class ServiceCatalogItem {
  ServiceCatalogItem(Map<String, dynamic> data)
    : data = ImmutableFields.from(data);
  final ImmutableFields data;
  String get code => data['code']?.toString() ?? '';
  String get name => data['name']?.toString() ?? 'Service';
  String get status => data['status']?.toString() ?? 'UNKNOWN';
}

class SaveServiceCommand {
  SaveServiceCommand(Map<String, dynamic> values, {this.code})
    : values = ImmutableFields.from(values);
  final ImmutableFields values;
  final String? code;
}
