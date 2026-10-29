import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('resident delivery view is responsive and shows pickup code', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(390, 844));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    await _pump(
      tester,
      _client(resident: true),
      DeliveryWorkspaceRole.resident,
    );

    expect(find.text('My deliveries'), findsOneWidget);
    expect(find.textContaining('Pickup code: 123456'), findsOneWidget);
    expect(find.text('Accept'), findsOneWidget);
    expect(find.text('Pay COD online'), findsOneWidget);
    expect(find.text('Record COD'), findsNothing);
    expect(tester.takeException(), isNull);
  });

  testWidgets('resident can accept arrival and pay COD online', (tester) async {
    await tester.binding.setSurfaceSize(const Size(900, 1100));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    await _pump(
      tester,
      _client(resident: true, requests: requests),
      DeliveryWorkspaceRole.resident,
    );

    await tester.tap(find.text('Accept'));
    await tester.pumpAndSettle();
    await tester.tap(find.widgetWithText(FilledButton, 'Accept').last);
    await tester.pumpAndSettle();
    expect(
      jsonDecode(
        requests.singleWhere((request) => request.method == 'PATCH').body,
      ),
      {'action': 'ACCEPT'},
    );

    requests.clear();
    await tester.tap(find.text('Pay COD online'));
    await tester.pumpAndSettle();
    expect(find.text('TEST MODE · No real money charged'), findsOneWidget);
    await tester.tap(find.widgetWithText(FilledButton, 'Pay ₹250.00'));
    await tester.pumpAndSettle();
    final checkout = requests.singleWhere(
      (request) => request.url.path.endsWith('/checkout'),
    );
    expect(jsonDecode(checkout.body), {'paymentMethod': 'UPI'});
    expect(tester.takeException(), isNull);
  });

  testWidgets('manager sees masked code and can record cash COD', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(1000, 1100));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    await _pump(
      tester,
      _client(resident: false, requests: requests),
      DeliveryWorkspaceRole.staff,
    );

    expect(find.textContaining('Pickup code:'), findsNothing);
    expect(find.text('Record COD'), findsOneWidget);
    await tester.tap(find.text('Record COD'));
    await tester.pumpAndSettle();
    await tester.tap(find.widgetWithText(FilledButton, 'Record paid'));
    await tester.pumpAndSettle();
    final request = requests.singleWhere(
      (request) => request.url.path.endsWith('/cod'),
    );
    expect(jsonDecode(request.body), {'paymentMethod': 'CASH'});
    expect(tester.takeException(), isNull);
  });

  testWidgets('manager secure handover sends pickup code and collector', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(1000, 1100));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    await _pump(
      tester,
      _client(resident: false, requests: requests),
      DeliveryWorkspaceRole.societyAdmin,
    );

    await tester.tap(find.text('Handover'));
    await tester.pumpAndSettle();
    await tester.enterText(
      find.widgetWithText(TextField, 'Resident pickup code'),
      '123456',
    );
    await tester.enterText(
      find.widgetWithText(TextField, 'Collector name'),
      'Family Member',
    );
    await tester.tap(find.widgetWithText(FilledButton, 'Confirm handover'));
    await tester.pumpAndSettle();

    final body = jsonDecode(
      requests.singleWhere((request) => request.method == 'PATCH').body,
    );
    expect(body, {
      'action': 'HANDOVER',
      'collectorName': 'Family Member',
      'pickupCode': '123456',
    });
    expect(tester.takeException(), isNull);
  });
}

Future<void> _pump(
  WidgetTester tester,
  ApiClient client,
  DeliveryWorkspaceRole role,
) async {
  final bloc = ApiGatewayBloc(client);
  addTearDown(bloc.close);
  await tester.pumpWidget(
    MaterialApp(
      theme: SocietyTheme.light(),
      home: Scaffold(
        body: DeliveryWorkspace(client: BlocBackedApiGateway(bloc), role: role),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

ApiClient _client({required bool resident, List<http.Request>? requests}) =>
    HttpApiClient(
      token: 'test-token',
      baseUrl: 'http://localhost:8080',
      httpClient: MockClient((request) async {
        requests?.add(request);
        if (request.method == 'GET' && request.url.path == '/api/deliveries') {
          return http.Response(jsonEncode(_workspace(resident: resident)), 200);
        }
        if (request.method == 'PATCH') {
          return http.Response(
            jsonEncode({
              ..._delivery(resident: resident),
              'status': 'READY_FOR_PICKUP',
            }),
            200,
          );
        }
        if (request.url.path.endsWith('/checkout')) {
          return http.Response(
            jsonEncode({
              ..._delivery(resident: resident),
              'cod_payment_status': 'PAID',
              'cod_payment_method': 'RAZORPAY_UPI',
              'cod_payment_reference': 'pay_delivery_test_123',
            }),
            200,
          );
        }
        if (request.url.path.endsWith('/cod')) {
          return http.Response(
            jsonEncode({
              ..._delivery(resident: resident),
              'cod_payment_status': 'PAID',
              'cod_payment_method': 'CASH',
              'cod_payment_reference': 'COD-TEST',
            }),
            200,
          );
        }
        return http.Response(jsonEncode({'message': 'Not found'}), 404);
      }),
    );

Map<String, dynamic> _workspace({required bool resident}) => {
  'total': 1,
  'awaiting_action': 1,
  'ready_for_pickup': 0,
  'completed': 0,
  'pending_cod': 1,
  'options': [
    {
      'resident_id': 'resident-1',
      'resident_name': 'Demo Member',
      'flat_id': 'flat-1',
      'block': 'Tower A',
      'space_number': '101',
    },
  ],
  'deliveries': [_delivery(resident: resident)],
};

Map<String, dynamic> _delivery({required bool resident}) => {
  'id': 'delivery-1',
  'delivery_number': 'DLV-TEST-1',
  'tracking_number': 'TRACK123',
  'provider': 'BlueDart',
  'sender_name': 'Online Store',
  'parcel_type': 'PACKAGE',
  'package_count': 1,
  'parcel_size': 'MEDIUM',
  'fragile': true,
  'storage_location': 'Desk A',
  'status': 'RECEIVED',
  'pickup_code': resident ? '123456' : null,
  'pickup_code_issued': true,
  'cod_amount': 250,
  'cod_payment_status': 'PENDING',
  'resident_id': 'resident-1',
  'resident_name': 'Demo Member',
  'resident_mobile': '9000000000',
  'flat_id': 'flat-1',
  'block': 'Tower A',
  'space_number': '101',
  'received_at': DateTime.now().toIso8601String(),
  'created_at': DateTime.now().toIso8601String(),
};
