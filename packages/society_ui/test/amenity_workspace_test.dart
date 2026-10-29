import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  testWidgets(
    'resident sees multiple instances and booking charges on narrow screens',
    (tester) async {
      await tester.binding.setSurfaceSize(const Size(390, 844));
      addTearDown(() => tester.binding.setSurfaceSize(null));
      final client = _client();

      await _pumpWorkspace(tester, client, AmenityWorkspaceRole.resident);

      expect(find.text('Society amenities'), findsOneWidget);
      expect(find.text('Community Hall B'), findsOneWidget);
      expect(find.text('Swimming Pool Main'), findsOneWidget);
      expect(find.textContaining('₹750.00 per booking'), findsOneWidget);
      expect(find.textContaining('₹500.00 refundable deposit'), findsOneWidget);
      expect(find.text('Book now'), findsNWidgets(2));
      expect(find.text('Add amenity'), findsNothing);
      expect(tester.takeException(), isNull);
    },
  );

  testWidgets('facility type filter separates multiple amenity categories', (
    tester,
  ) async {
    final client = _client();
    await _pumpWorkspace(tester, client, AmenityWorkspaceRole.resident);

    await tester.tap(find.byType(DropdownButtonFormField<String>).first);
    await tester.pumpAndSettle();
    await tester.tap(find.text('Swimming Pool').last);
    await tester.pumpAndSettle();

    expect(find.text('Swimming Pool Main'), findsOneWidget);
    expect(find.text('Community Hall B'), findsNothing);
    expect(tester.takeException(), isNull);
  });

  testWidgets('booking dialog calculates fixed charge and refundable deposit', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(800, 1000));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final client = _client();
    await _pumpWorkspace(tester, client, AmenityWorkspaceRole.resident);

    await tester.ensureVisible(find.text('Book now').first);
    await tester.tap(find.text('Book now').first);
    await tester.pumpAndSettle();

    expect(find.text('Book Community Hall B'), findsOneWidget);
    expect(find.text('₹1250.00 estimated charge'), findsOneWidget);
    expect(
      find.textContaining('Base ₹750.00 + refundable deposit ₹500.00'),
      findsOneWidget,
    );
    expect(find.text('Create booking'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets(
    'manager can record cash and resident can complete online checkout',
    (tester) async {
      await tester.binding.setSurfaceSize(const Size(800, 1000));
      addTearDown(() => tester.binding.setSurfaceSize(null));
      final requests = <http.Request>[];
      final client = _client(requests: requests);
      await _pumpWorkspace(tester, client, AmenityWorkspaceRole.societyAdmin);

      expect(find.text('Amenity management'), findsOneWidget);
      expect(find.text('Add amenity'), findsOneWidget);
      expect(find.text('Edit'), findsWidgets);
      await tester.drag(find.byType(ListView), const Offset(0, -500));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Record cash'));
      await tester.pumpAndSettle();
      await tester.tap(find.widgetWithText(FilledButton, 'Record cash'));
      await tester.pumpAndSettle();

      expect(
        requests.any(
          (request) =>
              request.method == 'POST' &&
              request.url.path == '/api/amenities/bookings/booking-1/cash',
        ),
        isTrue,
      );
      expect(tester.takeException(), isNull);

      requests.clear();
      await tester.pumpWidget(const SizedBox.shrink());
      await _pumpWorkspace(tester, client, AmenityWorkspaceRole.resident);
      await tester.drag(find.byType(ListView), const Offset(0, -500));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Pay online'));
      await tester.pumpAndSettle();
      expect(find.text('TEST MODE · No real money charged'), findsOneWidget);
      await tester.tap(find.widgetWithText(FilledButton, 'Pay ₹1250.00'));
      await tester.pumpAndSettle();

      final checkout = requests.singleWhere(
        (request) => request.url.path.endsWith('/booking-1/checkout'),
      );
      expect(jsonDecode(checkout.body), {'paymentMethod': 'UPI'});
      expect(tester.takeException(), isNull);
    },
  );
}

Future<void> _pumpWorkspace(
  WidgetTester tester,
  ApiClient client,
  AmenityWorkspaceRole role,
) async {
  final bloc = ApiGatewayBloc(client);
  addTearDown(bloc.close);
  await tester.pumpWidget(
    MaterialApp(
      theme: SocietyTheme.light(),
      home: Scaffold(
        body: AmenityWorkspace(client: BlocBackedApiGateway(bloc), role: role),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

ApiClient _client({List<http.Request>? requests}) => HttpApiClient(
  token: 'test-token',
  baseUrl: 'http://localhost:8080',
  httpClient: MockClient((request) async {
    requests?.add(request);
    if (request.url.path == '/api/amenities') {
      return http.Response(jsonEncode(_amenities), 200);
    }
    if (request.url.path == '/api/amenities/bookings') {
      return http.Response(jsonEncode(_bookings), 200);
    }
    if (request.url.path.endsWith('/cash')) {
      return http.Response(
        jsonEncode({
          ..._bookings.first,
          'status': 'PENDING_APPROVAL',
          'payment_status': 'PAID',
          'payment_method': 'CASH',
          'payment_reference': 'AM-CASH-TEST',
        }),
        200,
      );
    }
    if (request.url.path.endsWith('/checkout')) {
      return http.Response(
        jsonEncode({
          ..._bookings.first,
          'status': 'PENDING_APPROVAL',
          'payment_status': 'PAID',
          'payment_method': 'RAZORPAY_UPI',
          'payment_reference': 'pay_amenity_test_123',
        }),
        200,
      );
    }
    return http.Response(jsonEncode({'message': 'Not found'}), 404);
  }),
);

final _amenities = [
  {
    'id': 'hall-b',
    'name': 'Community Hall B',
    'amenity_type': 'COMMUNITY_CENTER',
    'amenity_code': 'HALL-B',
    'description': 'Events and meetings',
    'location': 'Clubhouse first floor',
    'capacity': 80,
    'hourly_charge': 0,
    'pricing_type': 'FIXED',
    'booking_charge': 750,
    'refundable_deposit': 500,
    'opens_at': '08:00:00',
    'closes_at': '22:00:00',
    'slot_minutes': 60,
    'advance_booking_days': 90,
    'cancellation_hours': 24,
    'booking_approval_required': true,
    'instructions': 'Identity verification required',
    'active': true,
  },
  {
    'id': 'pool-main',
    'name': 'Swimming Pool Main',
    'amenity_type': 'SWIMMING_POOL',
    'amenity_code': 'POOL-MAIN',
    'description': 'Main pool',
    'location': 'Recreation zone',
    'capacity': 25,
    'hourly_charge': 0,
    'pricing_type': 'PER_SLOT',
    'booking_charge': 50,
    'refundable_deposit': 0,
    'opens_at': '06:00:00',
    'closes_at': '21:00:00',
    'slot_minutes': 60,
    'advance_booking_days': 30,
    'cancellation_hours': 4,
    'booking_approval_required': false,
    'active': true,
  },
];

final _bookings = [
  {
    'id': 'booking-1',
    'booking_number': 'AMB-TEST-1',
    'amenity_id': 'hall-b',
    'amenity_name': 'Community Hall B',
    'amenity_type': 'COMMUNITY_CENTER',
    'amenity_code': 'HALL-B',
    'starts_at': DateTime.now().add(const Duration(days: 2)).toIso8601String(),
    'ends_at': DateTime.now()
        .add(const Duration(days: 2, hours: 2))
        .toIso8601String(),
    'attendees': 30,
    'purpose': 'Family celebration',
    'base_charge': 750,
    'deposit_amount': 500,
    'amount': 1250,
    'status': 'PENDING_PAYMENT',
    'payment_status': 'PENDING',
  },
];
