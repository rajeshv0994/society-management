import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:super_admin_app/features/requirements/domain/entities/requirements_workspace.dart';
import 'package:super_admin_app/features/requirements/domain/usecases/requirements_use_cases.dart';
import 'package:super_admin_app/features/requirements/presentation/bloc/requirements_bloc.dart';
import 'package:super_admin_app/features/requirements/presentation/pages/requirements_view.dart';
import 'package:super_admin_app/features/societies/domain/entities/society_summary.dart';

import 'support/fake_feature_repositories.dart';

class _RequirementsRepository extends FakeFeatureRepositories {
  UpdateCustomPlanCommand? updated;
  String? cancelledId;
  String? cancellationReason;

  @override
  Future<RequirementsWorkspace> loadWorkspace() async => RequirementsWorkspace(
    requirements: [
      RequirementRecord({
        'id': 'requirement-1',
        'society_id': 'society-1',
        'society_name': 'Green Valley',
        'contact_person': 'Admin',
        'email': 'admin@example.com',
        'mobile': '9000000000',
        'city': 'Mumbai',
        'estimated_flats': 100,
        'estimated_staff': 20,
        'budget_monthly': 8500,
        'requirement_details': 'Custom billing workflow',
        'requested_modules_text': '["RESIDENTS","BILLING"]',
        'status': 'ACCEPTED',
      }),
    ],
    societies: [
      SocietySummary({'id': 'society-1', 'name': 'Green Valley'}),
    ],
    plans: [
      CustomPlanRecord({
        'id': 'plan-1',
        'society_id': 'society-1',
        'society_name': 'Green Valley',
        'plan_name': 'Custom rates',
        'description': 'Original agreement',
        'enabled_modules_text': '["RESIDENTS","BILLING"]',
        'max_flats': 100,
        'max_staff': 20,
        'max_storage_gb': 50,
        'price_monthly': 0,
        'price_per_space': 100,
        'price_per_staff': 200,
        'setup_fee': 0,
        'billing_cycle': 'MONTHLY',
        'valid_from': '2026-07-29',
        'valid_until': null,
        'status': 'DRAFT',
      }),
    ],
  );

  @override
  Future<void> updateCustomPlan(UpdateCustomPlanCommand command) async {
    updated = command;
  }

  @override
  Future<void> cancelCustomPlan(String id, String reason) async {
    cancelledId = id;
    cancellationReason = reason;
  }
}

void main() {
  testWidgets('super admin edits an existing custom plan through BLoC', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(1200, 900));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final repository = _RequirementsRepository();
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: BlocProvider(
            create: (_) => RequirementsBloc(RequirementsUseCases(repository)),
            child: const RequirementsView(),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    await tester.tap(find.text('Custom plans'));
    await tester.pumpAndSettle();
    await tester.tap(find.byTooltip('Edit custom plan'));
    await tester.pumpAndSettle();
    expect(find.text('Edit custom plan'), findsOneWidget);

    await tester.enterText(
      find.widgetWithText(TextField, 'Plan name'),
      'Updated custom rates',
    );
    await tester.tap(find.text('Save changes'));
    await tester.pumpAndSettle();

    expect(repository.updated?.id, 'plan-1');
    expect(repository.updated?.values['planName'], 'Updated custom rates');
    expect(find.text('Custom plan updated'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('accepted requirement opens a new plan as draft', (tester) async {
    await tester.binding.setSurfaceSize(const Size(1200, 900));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final repository = _RequirementsRepository();
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: BlocProvider(
            create: (_) => RequirementsBloc(RequirementsUseCases(repository)),
            child: const RequirementsView(),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    await tester.tap(find.text('Build custom plan'));
    await tester.pumpAndSettle();

    expect(find.text('Build custom plan'), findsNWidgets(2));
    expect(find.text('DRAFT'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('super admin cancels a custom plan with a reason', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(1200, 900));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final repository = _RequirementsRepository();
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: BlocProvider(
            create: (_) => RequirementsBloc(RequirementsUseCases(repository)),
            child: const RequirementsView(),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    await tester.tap(find.text('Custom plans'));
    await tester.pumpAndSettle();
    await tester.tap(find.byTooltip('Cancel custom plan'));
    await tester.pumpAndSettle();
    await tester.enterText(
      find.widgetWithText(TextField, 'Cancellation reason'),
      'Customer ended agreement',
    );
    await tester.tap(find.text('Cancel plan'));
    await tester.pumpAndSettle();

    expect(repository.cancelledId, 'plan-1');
    expect(repository.cancellationReason, 'Customer ended agreement');
    expect(find.text('Custom plan cancelled'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });
}
