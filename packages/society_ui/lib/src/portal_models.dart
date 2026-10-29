import 'session_manager.dart';

enum PortalRole { superAdmin, societyAdmin, staff, member }

enum PortalModuleResource { societyDashboard }

class SocietyOption {
  const SocietyOption({required this.code, required this.name});

  factory SocietyOption.fromJson(Map<String, dynamic> json) => SocietyOption(
    code: json['society_code']?.toString() ?? '',
    name: json['name']?.toString() ?? 'Society',
  );

  final String code;
  final String name;
}

class PortalAccess {
  const PortalAccess({this.permissions = const {}, this.enabledServices});

  final Set<String> permissions;
  final Set<String>? enabledServices;
}

enum PortalAuthStatus { restoring, signedOut, submitting, signedIn }

class PortalAuthState {
  const PortalAuthState({
    this.status = PortalAuthStatus.restoring,
    this.session,
    this.societies = const [],
    this.error,
  });

  final PortalAuthStatus status;
  final SessionData? session;
  final List<SocietyOption> societies;
  final String? error;

  bool get authenticated => status == PortalAuthStatus.signedIn;
}
