import '../../../../core/immutable_fields.dart';

import '../../../services/domain/entities/service_catalog_item.dart';

class SocietySummary {
  SocietySummary(Map<String, dynamic> data) : data = ImmutableFields.from(data);
  final ImmutableFields data;
  String get id => data['id'].toString();
  String get name => data['name']?.toString() ?? 'Society';
  String get status => data['status']?.toString() ?? 'UNKNOWN';
  String get subtitle =>
      '${data['registration_number']} • ${data['city']} • ${data['configured_space_count'] ?? data['configured_unit_count'] ?? data['configured_flat_count']}/${data['total_spaces'] ?? data['total_units'] ?? data['total_flats']} spaces • ${data['active_member_count']} members';
}

class SocietyDetail {
  SocietyDetail(Map<String, dynamic> data) : data = ImmutableFields.from(data);
  final ImmutableFields data;
}

class SocietyCreateResult {
  SocietyCreateResult(Map<String, dynamic> data)
    : data = ImmutableFields.from(data);
  final ImmutableFields data;
}

class SocietyCreateWorkspace {
  const SocietyCreateWorkspace(this.services);
  final List<ServiceCatalogItem> services;
}

class SocietyDetailWorkspace {
  const SocietyDetailWorkspace({
    required this.detail,
    required this.services,
    required this.baseUrl,
  });
  final SocietyDetail detail;
  final List<ServiceCatalogItem> services;
  final String baseUrl;
}

class CreateSocietyCommand {
  CreateSocietyCommand(Map<String, dynamic> values)
    : values = ImmutableFields.from(values);
  final ImmutableFields values;
}

class UpdateSocietyCommand {
  UpdateSocietyCommand(this.id, Map<String, dynamic> values)
    : values = ImmutableFields.from(values);
  final String id;
  final ImmutableFields values;
}

class UploadSocietyLogoCommand {
  const UploadSocietyLogoCommand(this.id, this.bytes, this.filename);
  final String id;
  final List<int> bytes;
  final String filename;
}
