import 'package:super_admin_app/features/audit/domain/entities/audit_page.dart';
import 'package:super_admin_app/features/audit/domain/repositories/audit_repository.dart';
import 'package:super_admin_app/features/billing/domain/entities/billing_dashboard.dart';
import 'package:super_admin_app/features/billing/domain/repositories/billing_repository.dart';
import 'package:super_admin_app/features/dashboard/domain/entities/dashboard_metrics.dart';
import 'package:super_admin_app/features/dashboard/domain/repositories/dashboard_repository.dart';
import 'package:super_admin_app/features/management/domain/entities/management_record.dart';
import 'package:super_admin_app/features/management/domain/repositories/management_repository.dart';
import 'package:super_admin_app/features/requirements/domain/entities/requirements_workspace.dart';
import 'package:super_admin_app/features/requirements/domain/repositories/requirements_repository.dart';
import 'package:super_admin_app/features/services/domain/entities/service_catalog_item.dart';
import 'package:super_admin_app/features/services/domain/repositories/service_catalog_repository.dart';
import 'package:super_admin_app/features/societies/domain/entities/society_summary.dart';
import 'package:super_admin_app/features/societies/domain/repositories/societies_repository.dart';
import 'package:super_admin_app/features/storage/domain/entities/storage_dashboard.dart';
import 'package:super_admin_app/features/storage/domain/repositories/storage_repository.dart';
import 'package:super_admin_app/features/support/domain/entities/support_models.dart';
import 'package:super_admin_app/features/support/domain/repositories/support_repository.dart';

class FakeFeatureRepositories
    implements
        DashboardRepository,
        SocietiesRepository,
        ServiceCatalogRepository,
        StorageRepository,
        RequirementsRepository,
        BillingRepository,
        ManagementRepository,
        SupportRepository,
        AuditRepository {
  @override
  Future<DashboardMetrics> loadDashboard() async => DashboardMetrics({});
  @override
  Future<List<SocietySummary>> loadSocieties({
    String? status,
    String? search,
  }) async => [];
  @override
  Future<Map<String, dynamic>> approve(String id) async => {};
  @override
  Future<void> reject(String id, String reason) async {}
  @override
  Future<void> verifyDocument(
    String societyId,
    String documentId,
    String status,
  ) async {}
  @override
  Future<SocietyCreateWorkspace> loadCreateWorkspace() async =>
      SocietyCreateWorkspace([]);
  @override
  Future<SocietyDetailWorkspace> loadDetail(String id) async =>
      SocietyDetailWorkspace(
        detail: SocietyDetail({}),
        services: [],
        baseUrl: 'http://unused',
      );
  @override
  Future<SocietyCreateResult> create(CreateSocietyCommand command) async =>
      SocietyCreateResult({});
  @override
  Future<void> update(UpdateSocietyCommand command) async {}
  @override
  Future<void> uploadLogo(UploadSocietyLogoCommand command) async {}
  @override
  Future<List<ServiceCatalogItem>> loadCatalog() async => [];
  @override
  Future<void> saveService(SaveServiceCommand command) async {}
  @override
  Future<StorageDashboard> loadStorage() async =>
      StorageDashboard(policy: StoragePolicy({}), societies: []);
  @override
  Future<void> updatePolicy(UpdateStoragePolicyCommand command) async {}
  @override
  Future<RequirementsWorkspace> loadWorkspace() async =>
      RequirementsWorkspace(requirements: [], plans: [], societies: []);
  @override
  Future<void> review(ReviewRequirementCommand command) async {}
  @override
  Future<void> createPlan(CreateCustomPlanCommand command) async {}
  @override
  Future<void> updateCustomPlan(UpdateCustomPlanCommand command) async {}
  @override
  Future<void> cancelCustomPlan(String id, String reason) async {}
  @override
  Future<BillingDashboard> loadBilling() async =>
      BillingDashboard(orders: [], societies: []);
  @override
  Future<void> createOrder(CreateSubscriptionOrderCommand command) async {}
  @override
  Future<List<int>> downloadInvoice(String orderId) async => [];
  @override
  Future<void> reconcilePayment(String orderId) async {}
  @override
  Future<void> cancelOrder(String orderId, String reason) async {}
  @override
  Future<List<ManagementRecord>> loadRecords(
    ManagementResource resource,
  ) async => [];
  @override
  Future<void> updatePlan(UpdatePlanCommand command) async {}
  @override
  Future<void> retryNotification(String id) async {}
  @override
  Future<void> resolveTicket(String id, String resolution) async {}
  @override
  Future<List<SupportTicket>> loadTickets() async => [];
  @override
  Future<List<SupportMessage>> loadMessages(String ticketId) async => [];
  @override
  Future<void> sendMessage(String ticketId, String message) async {}
  @override
  Future<AuditPage> loadAudit(AuditQuery query) async => AuditPage(
    items: const [],
    page: query.page,
    size: query.size,
    total: 0,
    totalPages: 0,
  );
}
