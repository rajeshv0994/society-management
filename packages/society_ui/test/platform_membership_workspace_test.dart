import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  testWidgets('checkout result is verified before membership is shown paid', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(1000, 1100));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    final api = HttpApiClient(
      token: 'token',
      baseUrl: 'http://localhost:8080',
      httpClient: MockClient((request) async {
        requests.add(request);
        if (request.url.path == '/api/society/subscription') {
          return http.Response(jsonEncode(_subscription), 200);
        }
        if (request.url.path == '/api/society/subscription/orders') {
          return http.Response(jsonEncode([_order]), 200);
        }
        if (request.url.path.endsWith('/checkout')) {
          return http.Response(
            jsonEncode({
              'key_id': 'rzp_test_public',
              'provider_order_id': 'order_123',
              'amount_paise': 12500,
              'currency': 'INR',
              'name': 'SocietyHub',
              'description': 'Platform membership',
              'prefill': <String, dynamic>{},
            }),
            200,
          );
        }
        if (request.url.path.endsWith('/verify')) {
          return http.Response(
            jsonEncode({
              ..._order,
              'status': 'PAID',
              'provider_reference': 'pay_123',
            }),
            200,
          );
        }
        return http.Response('{}', 404);
      }),
    );
    final bloc = ApiGatewayBloc(api);
    addTearDown(bloc.close);

    await tester.pumpWidget(
      MaterialApp(
        theme: SocietyTheme.light(),
        home: Scaffold(
          body: PlatformMembershipWorkspace(
            client: BlocBackedApiGateway(bloc),
            razorpayLauncher: (_) async => const RazorpayCheckoutResult(
              orderId: 'order_123',
              paymentId: 'pay_123',
              signature: 'signed',
            ),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.scrollUntilVisible(find.text('SUB-TEST · ₹125.00'), 400);
    await tester.tap(find.byTooltip('Invoice actions'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Pay now with Razorpay'));
    await tester.pumpAndSettle();
    await tester.tap(find.widgetWithText(FilledButton, 'Pay ₹125.00'));
    await tester.pumpAndSettle();

    final verify = requests.singleWhere(
      (request) => request.url.path.endsWith('/verify'),
    );
    expect(jsonDecode(verify.body), {
      'razorpayOrderId': 'order_123',
      'razorpayPaymentId': 'pay_123',
      'razorpaySignature': 'signed',
    });
    expect(
      find.textContaining('Payment successful. ID: pay_123'),
      findsOneWidget,
    );
  });
}

const _subscription = {
  'plan_code': 'STANDARD',
  'selected_spaces': 10,
  'selected_staff': 2,
  'billing_cycle': 'MONTHLY',
  'billing_cycle_amount': 125,
  'price_per_space': 10,
  'price_per_staff': 12.5,
  'base_monthly_price': 125,
  'service_monthly_price': 0,
  'subscription_status': 'PENDING',
  'service_catalog': <dynamic>[],
};

const _order = {
  'id': 'invoice-1',
  'invoice_number': 'SUB-TEST',
  'amount': 125,
  'status': 'DUE',
  'billing_cycle': 'MONTHLY',
  'period_start': '2026-08-01',
  'period_end': '2026-08-31',
  'due_date': '2026-08-15',
};
