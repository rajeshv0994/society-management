import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter/material.dart';
import 'package:super_admin_app/main.dart' as app;
import 'package:super_admin_app/features/audit/data/models/audit_page_model.dart';
import 'package:super_admin_app/features/audit/domain/entities/audit_page.dart';
import 'package:super_admin_app/features/audit/domain/usecases/audit_use_cases.dart';
import 'package:super_admin_app/features/audit/presentation/bloc/audit_bloc.dart';
import 'package:super_admin_app/features/audit/presentation/pages/audit_view.dart';
import 'package:super_admin_app/features/services/presentation/bloc/service_catalog_bloc.dart';
import 'package:super_admin_app/features/services/presentation/pages/services_view.dart';
import 'package:super_admin_app/features/services/domain/entities/service_catalog_item.dart';
import 'package:super_admin_app/features/services/domain/usecases/service_catalog_use_cases.dart';
import 'package:super_admin_app/features/shell/presentation/bloc/navigation_bloc.dart';
import 'package:super_admin_app/features/shell/domain/entities/admin_section.dart';
import 'package:super_admin_app/features/storage/presentation/bloc/storage_bloc.dart';
import 'package:super_admin_app/features/storage/presentation/pages/storage_view.dart';
import 'package:super_admin_app/features/storage/domain/entities/storage_dashboard.dart';
import 'package:super_admin_app/features/storage/domain/usecases/storage_use_cases.dart';
import 'package:super_admin_app/features/societies/domain/entities/society_services_parser.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'support/fake_feature_repositories.dart';

void main() {
  testWidgets('renders super admin login', (tester) async {
    SharedPreferences.setMockInitialValues({});
    app.main();
    await tester.pump();
    await tester.pump();
    expect(find.text('Platform Control'), findsOneWidget);
  });

  test('dashboard navigation changes section and carries filters', () async {
    final bloc = NavigationBloc();
    bloc.add(
      const NavigationRequested(
        AdminSection.societies,
        societyStatus: 'PENDING_VERIFICATION',
      ),
    );
    await expectLater(
      bloc.stream,
      emits(
        predicate<NavigationState>(
          (state) =>
              state.section == AdminSection.societies &&
              state.societyStatus == 'PENDING_VERIFICATION',
        ),
      ),
    );
    await bloc.close();
  });

  test('reads selected society services from API data', () {
    expect(societyServicesFrom('["VISITORS","BILLING"]'), {
      'VISITORS',
      'BILLING',
    });
  });

  test('audit page accepts legacy list and paginated API responses', () {
    final legacy = AuditPageModel.fromResponse([
      {'id': 1, 'action': 'SOCIETY_CREATED', 'entity_type': 'SOCIETY'},
    ]);
    final paginated = AuditPageModel.fromResponse({
      'items': [
        {'id': 2, 'action': 'PLAN_UPDATED', 'entity_type': 'PLAN'},
      ],
      'page': 2,
      'size': 50,
      'total': 120,
      'total_pages': 3,
    });

    expect(legacy.items.single.action, 'SOCIETY_CREATED');
    expect(legacy.totalPages, 1);
    expect(paginated.items.single.action, 'PLAN_UPDATED');
    expect(paginated.page, 2);
    expect(paginated.total, 120);
  });

  testWidgets('service catalog loads without a runtime state error', (
    tester,
  ) async {
    await tester.pumpWidget(
      MaterialApp(
        home: BlocProvider(
          create: (_) => ServiceCatalogBloc(
            ServiceCatalogUseCases(_FakeFeatureRepositories()),
          ),
          child: const ServicesView(),
        ),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Visitors'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('audit view filters and opens structured details', (
    tester,
  ) async {
    final service = _FakeFeatureRepositories();
    await tester.pumpWidget(
      MaterialApp(
        home: BlocProvider(
          create: (_) => AuditBloc(AuditUseCases(service)),
          child: const AuditView(),
        ),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.textContaining('API_UPDATE_VENDOR'), findsOneWidget);

    await tester.enterText(
      find.byKey(const ValueKey('audit-search')),
      'vendor',
    );
    await tester.tap(find.byKey(const ValueKey('audit-apply')));
    await tester.pumpAndSettle();
    expect(service.lastAuditSearch, 'vendor');

    await tester.tap(find.textContaining('API_UPDATE_VENDOR').first);
    await tester.pumpAndSettle();
    expect(find.textContaining('http_method'), findsOneWidget);
    expect(find.text('Close'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('super admin can review and update storage policy', (
    tester,
  ) async {
    final service = _FakeFeatureRepositories();
    await tester.pumpWidget(
      MaterialApp(
        home: BlocProvider(
          create: (_) => StorageBloc(StorageUseCases(service)),
          child: const StorageView(),
        ),
      ),
    );
    await tester.pumpAndSettle();
    expect(find.text('5 GB'), findsWidgets);
    expect(find.text('Green View'), findsOneWidget);
    await tester.tap(find.text('Edit policy'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Save policy'));
    await tester.pumpAndSettle();
    expect(service.storagePolicyUpdated, isTrue);
    expect(tester.takeException(), isNull);
  });
}

class _FakeFeatureRepositories extends FakeFeatureRepositories {
  String? lastAuditSearch;
  bool storagePolicyUpdated = false;

  @override
  Future<List<ServiceCatalogItem>> loadCatalog() async => [
    ServiceCatalogItem({
      'code': 'VISITORS',
      'name': 'Visitors',
      'description': 'Visitor management',
      'status': 'ACTIVE',
      'sort_order': 1,
    }),
  ];

  @override
  Future<AuditPage> loadAudit(AuditQuery query) async {
    lastAuditSearch = query.search;
    return AuditPage(
      items: [
        AuditEventRecord({
          'id': 1,
          'action': 'API_UPDATE_VENDOR',
          'entity_type': 'CONFIGURATION',
          'entity_id': 'vendor-1',
          'actor_name': 'Admin User',
          'society_name': 'Green View',
          'occurred_at': '2026-07-28T12:00:00Z',
          'details': {'http_method': 'PATCH', 'outcome': 'SUCCEEDED'},
        }),
      ],
      page: query.page,
      size: query.size,
      total: 1,
      totalPages: 1,
    );
  }

  @override
  Future<StorageDashboard> loadStorage() async => StorageDashboard(
    policy: StoragePolicy({
      'default_free_gb': 5,
      'purchase_block_gb': 5,
      'price_per_block_monthly': 199,
    }),
    societies: [
      SocietyStorageUsage({
        'society_name': 'Green View',
        'used_bytes': 1048576,
        'purchased_bytes': 5368709120,
        'monthly_addon_amount': 199,
      }),
    ],
  );

  @override
  Future<void> updatePolicy(UpdateStoragePolicyCommand command) async {
    storagePolicyUpdated = true;
  }
}
