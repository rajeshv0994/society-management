import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:super_admin_app/features/billing/domain/entities/billing_dashboard.dart';
import 'package:super_admin_app/features/billing/domain/usecases/billing_use_cases.dart';
import 'package:super_admin_app/features/billing/presentation/bloc/billing_bloc.dart';
import 'package:super_admin_app/features/billing/presentation/pages/subscription_billing_view.dart';

import 'support/fake_feature_repositories.dart';

class _BillingRepository extends FakeFeatureRepositories {
  String? cancelledId;
  String? cancellationReason;

  @override
  Future<BillingDashboard> loadBilling() async => BillingDashboard(
    societies: const [],
    orders: [
      SubscriptionOrder({
        'id': 'invoice-1',
        'society_name': 'Green Valley',
        'society_code': 'GREEN-VALLEY',
        'invoice_number': 'SUB-202607-TEST',
        'amount': 8500,
        'status': 'DUE',
        'billing_cycle': 'MONTHLY',
        'period_start': '2026-07-01',
        'period_end': '2026-07-31',
        'due_date': '2026-07-29',
        'society_email': 'admin@example.com',
        'society_mobile': '9000000000',
        'payment_attempts': const [],
      }),
    ],
  );

  @override
  Future<void> cancelOrder(String orderId, String reason) async {
    cancelledId = orderId;
    cancellationReason = reason;
  }
}

void main() {
  testWidgets('super admin cancels a due invoice with a reason', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(1200, 900));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final repository = _BillingRepository();
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: BlocProvider(
            create: (_) => BillingBloc(BillingUseCases(repository)),
            child: const SubscriptionBillingView(),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    await tester.tap(find.byTooltip('Cancel due invoice'));
    await tester.pumpAndSettle();
    await tester.enterText(
      find.widgetWithText(TextField, 'Cancellation reason'),
      'Invoice issued with incorrect quantities',
    );
    await tester.tap(find.text('Cancel invoice'));
    await tester.pumpAndSettle();

    expect(repository.cancelledId, 'invoice-1');
    expect(
      repository.cancellationReason,
      'Invoice issued with incorrect quantities',
    );
    expect(find.text('Invoice cancelled'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });
}
