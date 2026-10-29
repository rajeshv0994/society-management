import 'package:society_ui/society_ui.dart';
import '../features/audit/data/datasources/audit_remote_data_source.dart';
import '../features/audit/data/repositories/audit_repository_impl.dart';
import '../features/audit/domain/usecases/audit_use_cases.dart';
import '../features/authentication/data/repositories/session_authentication_repository.dart';
import '../features/authentication/data/datasources/session_data_source.dart';
import '../features/authentication/domain/usecases/authentication_use_cases.dart';
import '../features/billing/data/datasources/billing_remote_data_source.dart';
import '../features/billing/data/repositories/billing_repository_impl.dart';
import '../features/billing/domain/usecases/billing_use_cases.dart';
import '../features/dashboard/data/datasources/dashboard_remote_data_source.dart';
import '../features/dashboard/data/repositories/dashboard_repository_impl.dart';
import '../features/dashboard/domain/usecases/dashboard_use_cases.dart';
import '../features/management/data/datasources/management_remote_data_source.dart';
import '../features/management/data/repositories/management_repository_impl.dart';
import '../features/management/domain/usecases/management_use_cases.dart';
import '../features/requirements/data/datasources/requirements_remote_data_source.dart';
import '../features/requirements/data/repositories/requirements_repository_impl.dart';
import '../features/requirements/domain/usecases/requirements_use_cases.dart';
import '../features/services/data/datasources/service_catalog_remote_data_source.dart';
import '../features/services/data/repositories/service_catalog_repository_impl.dart';
import '../features/services/domain/usecases/service_catalog_use_cases.dart';
import '../features/societies/data/datasources/societies_remote_data_source.dart';
import '../features/societies/data/repositories/societies_repository_impl.dart';
import '../features/societies/domain/usecases/societies_use_cases.dart';
import '../features/storage/data/datasources/storage_remote_data_source.dart';
import '../features/storage/data/repositories/storage_repository_impl.dart';
import '../features/storage/domain/usecases/storage_use_cases.dart';
import '../features/support/data/datasources/support_remote_data_source.dart';
import '../features/support/data/repositories/support_repository_impl.dart';
import '../features/support/domain/usecases/support_use_cases.dart';

class SuperAdminDependencies {
  const SuperAdminDependencies({
    required this.dashboard,
    required this.societies,
    required this.services,
    required this.storage,
    required this.requirements,
    required this.billing,
    required this.management,
    required this.support,
    required this.audit,
  });
  final DashboardUseCases dashboard;
  final SocietiesUseCases societies;
  final ServiceCatalogUseCases services;
  final StorageUseCases storage;
  final RequirementsUseCases requirements;
  final BillingUseCases billing;
  final ManagementUseCases management;
  final SupportUseCases support;
  final AuditUseCases audit;
}

class AppDependencies {
  AppDependencies._(this._authenticationRepository)
    : authentication = AuthenticationUseCases(_authenticationRepository);
  factory AppDependencies.bootstrap() => AppDependencies._(
    SessionAuthenticationRepository(
      SessionDataSource(
        SessionManager(storageKey: 'SUPER_ADMIN', expectedRole: 'SUPER_ADMIN'),
      ),
    ),
  );
  final SessionAuthenticationRepository _authenticationRepository;
  final AuthenticationUseCases authentication;
  SuperAdminDependencies platform() {
    final api = HttpApiClient(session: _authenticationRepository.sessions);
    return SuperAdminDependencies(
      dashboard: DashboardUseCases(
        DashboardRepositoryImpl(DashboardRemoteDataSource(api)),
      ),
      societies: SocietiesUseCases(
        SocietiesRepositoryImpl(SocietiesRemoteDataSource(api)),
      ),
      services: ServiceCatalogUseCases(
        ServiceCatalogRepositoryImpl(ServiceCatalogRemoteDataSource(api)),
      ),
      storage: StorageUseCases(
        StorageRepositoryImpl(StorageRemoteDataSource(api)),
      ),
      requirements: RequirementsUseCases(
        RequirementsRepositoryImpl(RequirementsRemoteDataSource(api)),
      ),
      billing: BillingUseCases(
        BillingRepositoryImpl(BillingRemoteDataSource(api)),
      ),
      management: ManagementUseCases(
        ManagementRepositoryImpl(ManagementRemoteDataSource(api)),
      ),
      support: SupportUseCases(
        SupportRepositoryImpl(SupportRemoteDataSource(api)),
      ),
      audit: AuditUseCases(AuditRepositoryImpl(AuditRemoteDataSource(api))),
    );
  }

  void dispose() => _authenticationRepository.dispose();
}
