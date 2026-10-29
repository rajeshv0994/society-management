import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:super_admin_app/features/services/domain/entities/service_catalog_item.dart';
import 'package:super_admin_app/features/societies/data/models/society_models.dart';
import 'package:super_admin_app/features/societies/domain/entities/society_summary.dart';
import 'package:super_admin_app/features/societies/domain/usecases/societies_use_cases.dart';
import 'package:super_admin_app/features/societies/presentation/bloc/societies_bloc.dart';
import 'package:super_admin_app/features/societies/presentation/bloc/society_operations_bloc.dart';
import 'package:super_admin_app/features/societies/presentation/pages/societies_view.dart';
import 'package:society_ui/society_ui.dart';
import 'support/fake_feature_repositories.dart';

class _SocietiesRepository extends FakeFeatureRepositories {
  @override
  Future<List<SocietySummary>> loadSocieties({
    String? status,
    String? search,
  }) async => [
    SocietySummaryModel.fromJson({
      'society': {
        'id': 'society-1',
        'name': 'Green View',
        'status': 'ACTIVE',
        'registration_number': 'REG-1',
        'city': 'Pune',
        'total_spaces': 10,
      },
      'configured_space_count': 10,
      'active_member_count': 5,
    }),
  ];

  @override
  Future<SocietyCreateWorkspace> loadCreateWorkspace() async =>
      SocietyCreateWorkspace(<ServiceCatalogItem>[]);

  @override
  Future<SocietyDetailWorkspace> loadDetail(String id) async {
    if (id != 'society-1') throw StateError('Unexpected society ID: $id');
    return SocietyDetailWorkspace(
      baseUrl: 'http://unused',
      services: <ServiceCatalogItem>[],
      detail: SocietyDetail({
        'society': {
          'id': 'society-1',
          'name': 'Green View',
          'status': 'ACTIVE',
          'enabled_modules': <String>[],
        },
        'household_summary': <String, dynamic>{},
        'delivery_summary': <String, dynamic>{},
        'emergency_summary': <String, dynamic>{},
        'activity_summary': <String, dynamic>{},
        'structure': <String, dynamic>{
          'blocks': 1,
          'sections': 1,
          'spaces': 1,
          'parking_slots': 0,
          'amenities': 0,
          'hierarchy': [
            {
              'name': 'A Wing',
              'sections': [
                {
                  'name': 'Ground floor',
                  'spaces': [
                    {'space_number': 'A-001', 'occupancy_status': 'OCCUPIED'},
                  ],
                },
              ],
            },
          ],
        },
        'users': <dynamic>[],
        'documents': <dynamic>[],
        'orders': <dynamic>[],
        'customPlans': <dynamic>[
          {
            'plan_name': 'Usage plan',
            'price_per_space': 100,
            'price_per_staff': 200,
            'price_monthly': 0,
            'enabled_modules': '["VISITORS", "BILLING"]',
            'status': 'ACTIVE',
          },
          {
            'plan_name': 'Fixed plan',
            'price_per_space': 0,
            'price_per_staff': 0,
            'price_monthly': 8500,
            'enabled_modules': '["RESIDENTS"]',
            'status': 'REPLACED',
          },
        ],
        'support': <dynamic>[],
        'requirements': <dynamic>[
          {
            'requested_modules': '["BILLING"]',
            'status': 'ACCEPTED',
            'created_at': '2026-07-29',
          },
        ],
        'notifications': <dynamic>[
          {
            'template_code': 'INVOICE_DUE',
            'channel': 'EMAIL',
            'status': 'SENT',
          },
        ],
        'audit_events': <dynamic>[
          {
            'action': 'SOCIETY_UPDATED',
            'entity_type': 'SOCIETY',
            'occurred_at': '2026-07-29',
          },
        ],
      }),
    );
  }
}

class _FailingDetailRepository extends _SocietiesRepository {
  bool failDetail = true;
  @override
  Future<SocietyDetailWorkspace> loadDetail(String id) async {
    if (failDetail) throw Exception('Detail request failed');
    return super.loadDetail(id);
  }
}

void main() {
  testWidgets('failed detail after create shows error and supports retry', (
    tester,
  ) async {
    final repository = _FailingDetailRepository();
    final useCases = SocietiesUseCases(repository);
    final operations = SocietyOperationsBloc(useCases);
    addTearDown(operations.close);
    final prepared = operations.stream.firstWhere(
      (state) => state.status == SocietyOperationStatus.ready,
    );
    operations.add(const SocietyCreatePrepared());
    await prepared;
    expect(operations.state.data, isA<SocietyCreateWorkspace>());
    await tester.pumpWidget(
      MaterialApp(
        theme: SocietyTheme.light().copyWith(
          splashFactory: NoSplash.splashFactory,
        ),
        home: MultiBlocProvider(
          providers: [
            BlocProvider(
              create: (_) =>
                  SocietiesBloc(useCases)..add(const SocietiesLoaded()),
            ),
            BlocProvider.value(value: operations),
          ],
          child: const Scaffold(body: SocietiesView()),
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.tap(find.text('Green View'));
    await tester.pumpAndSettle();
    expect(operations.state.status, SocietyOperationStatus.failure);
    expect(operations.state.data, isNull);
    expect(find.textContaining('Detail request failed'), findsWidgets);
    expect(tester.takeException(), isNull);
    repository.failDetail = false;
    await tester.tap(find.text('Retry'));
    await tester.pumpAndSettle();
    expect(find.text('Overview'), findsOneWidget);
    expect(operations.state.data, isA<SocietyDetailWorkspace>());
    expect(tester.takeException(), isNull);
  });

  testWidgets('society detail route retains SocietyOperationsBloc', (
    tester,
  ) async {
    final useCases = SocietiesUseCases(_SocietiesRepository());
    await tester.pumpWidget(
      MaterialApp(
        theme: SocietyTheme.light().copyWith(
          splashFactory: NoSplash.splashFactory,
        ),
        home: MultiBlocProvider(
          providers: [
            BlocProvider(
              create: (_) =>
                  SocietiesBloc(useCases)..add(const SocietiesLoaded()),
            ),
            BlocProvider(create: (_) => SocietyOperationsBloc(useCases)),
          ],
          child: const Scaffold(body: SocietiesView()),
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.tap(find.text('Green View'));
    await tester.pumpAndSettle();

    expect(find.text('Overview'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('society detail shows the correct custom-plan pricing model', (
    tester,
  ) async {
    final useCases = SocietiesUseCases(_SocietiesRepository());
    await tester.pumpWidget(
      MaterialApp(
        theme: SocietyTheme.light().copyWith(
          splashFactory: NoSplash.splashFactory,
        ),
        home: MultiBlocProvider(
          providers: [
            BlocProvider(
              create: (_) =>
                  SocietiesBloc(useCases)..add(const SocietiesLoaded()),
            ),
            BlocProvider(create: (_) => SocietyOperationsBloc(useCases)),
          ],
          child: const Scaffold(body: SocietiesView()),
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.tap(find.text('Green View'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Plans & billing'));
    await tester.pumpAndSettle();

    expect(
      find.textContaining('₹100 / space + ₹200 / staff monthly'),
      findsOneWidget,
    );
    expect(find.textContaining('₹8500 fixed monthly'), findsOneWidget);
    expect(find.textContaining('₹0 •'), findsNothing);
  });

  testWidgets('society detail exposes hierarchy and all related activity', (
    tester,
  ) async {
    final useCases = SocietiesUseCases(_SocietiesRepository());
    await tester.pumpWidget(
      MaterialApp(
        theme: SocietyTheme.light().copyWith(
          splashFactory: NoSplash.splashFactory,
        ),
        home: MultiBlocProvider(
          providers: [
            BlocProvider(
              create: (_) =>
                  SocietiesBloc(useCases)..add(const SocietiesLoaded()),
            ),
            BlocProvider(create: (_) => SocietyOperationsBloc(useCases)),
          ],
          child: const Scaffold(body: SocietiesView()),
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.tap(find.text('Green View'));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Structure'));
    await tester.pumpAndSettle();
    expect(find.text('A Wing'), findsOneWidget);

    await tester.ensureVisible(find.text('Related activity'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Related activity'));
    await tester.pumpAndSettle();
    expect(find.text('Custom requirements'), findsOneWidget);
    expect(find.textContaining('BILLING'), findsOneWidget);
    expect(find.text('INVOICE_DUE • EMAIL'), findsOneWidget);
    expect(find.text('SOCIETY_UPDATED'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });
}
