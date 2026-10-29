import '../../domain/entities/admin_session.dart';

class PublicSocietyModel {
  const PublicSocietyModel._();

  static PublicSociety fromJson(Map<String, dynamic> json) => PublicSociety(
    code: json['society_code']?.toString() ?? '',
    name: json['name']?.toString() ?? 'Society',
  );
}
