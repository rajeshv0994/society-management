import 'package:society_ui/society_ui.dart';
import '../core/data/datasources/society_admin_remote_data_source.dart';
import '../core/data/repositories/admin_resource_repository_impl.dart';
import '../core/domain/usecases/admin_resource_use_cases.dart';
import '../features/auth/data/datasources/auth_remote_data_source.dart';
import '../features/auth/data/repositories/auth_repository_impl.dart';
import '../features/auth/domain/usecases/auth_use_cases.dart';

class AppDependencies {
  AppDependencies._({required this.auth, required this.societyAdmin});

  final AuthUseCases auth;
  final SocietyAdminDependencies societyAdmin;

  factory AppDependencies.bootstrap() {
    final sessions = SessionManager(
      storageKey: 'SOCIETY_ADMIN',
      expectedRole: 'SOCIETY_ADMIN',
    );
    final authenticatedClient = HttpApiClient(session: sessions);
    final publicClient = HttpApiClient();
    return AppDependencies._(
      auth: AuthUseCases(
        AuthRepositoryImpl(sessions, AuthRemoteDataSource(publicClient)),
      ),
      societyAdmin: SocietyAdminDependencies.fromClient(authenticatedClient),
    );
  }
}

class SocietyAdminDependencies {
  const SocietyAdminDependencies({required this.resources});
  final AdminResourceUseCases resources;
  factory SocietyAdminDependencies.fromClient(ApiClient client) {
    return SocietyAdminDependencies(
      resources: AdminResourceUseCases(
        AdminResourceRepositoryImpl(SocietyAdminRemoteDataSource(client)),
      ),
    );
  }
}
