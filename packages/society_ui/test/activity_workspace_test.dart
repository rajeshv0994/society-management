import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('resident activities fit a narrow screen without overflow', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(390, 844));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    await _pump(tester, _client(), ActivityWorkspaceRole.resident);

    expect(find.text('Activities & galleries'), findsOneWidget);
    expect(find.text('Free'), findsOneWidget);
    expect(find.text('Participate'), findsOneWidget);
    expect(find.text('Create activity'), findsNothing);
    expect(tester.takeException(), isNull);
  });

  testWidgets('member can register multiple people for a free activity', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(900, 1000));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    await _pump(
      tester,
      _client(requests: requests),
      ActivityWorkspaceRole.resident,
    );

    await tester.tap(find.text('Participate'));
    await tester.pumpAndSettle();
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Number of participants'),
      '3',
    );
    await tester.enterText(
      find.widgetWithText(TextField, 'Participant names'),
      'Member, Child One, Child Two',
    );
    await tester.tap(
      find.widgetWithText(FilledButton, 'Confirm participation'),
    );
    await tester.pumpAndSettle();

    final request = requests.singleWhere(
      (row) => row.method == 'POST' && row.url.path.endsWith('/join'),
    );
    expect(jsonDecode(request.body), containsPair('participantCount', 3));
    expect(
      jsonDecode(request.body),
      containsPair('participantNames', 'Member, Child One, Child Two'),
    );
    expect(tester.takeException(), isNull);
  });

  testWidgets('member can pay a paid activity fee in Razorpay test mode', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(1000, 1000));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    await _pump(
      tester,
      _client(requests: requests, paid: true, registered: true),
      ActivityWorkspaceRole.resident,
    );

    await tester.tap(find.text('Participations'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Pay online'));
    await tester.pumpAndSettle();
    expect(find.text('TEST MODE · No real money charged'), findsOneWidget);
    await tester.tap(find.text('Upi'));
    await tester.pumpAndSettle();

    final request = requests.singleWhere(
      (row) => row.url.path.endsWith('/checkout'),
    );
    expect(jsonDecode(request.body), {'paymentMethod': 'UPI'});
    expect(tester.takeException(), isNull);
  });

  testWidgets('staff can record cash for a paid registration', (tester) async {
    await tester.binding.setSurfaceSize(const Size(1000, 1000));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    await _pump(
      tester,
      _client(requests: requests, paid: true, registered: true),
      ActivityWorkspaceRole.staff,
    );

    await tester.tap(find.text('Participations'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Record cash'));
    await tester.pumpAndSettle();
    await tester.tap(find.widgetWithText(FilledButton, 'Confirm'));
    await tester.pumpAndSettle();

    expect(
      requests.where(
        (row) => row.method == 'POST' && row.url.path.endsWith('/cash'),
      ),
      hasLength(1),
    );
    expect(tester.takeException(), isNull);
  });

  testWidgets('society admin can create a published free activity', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(1100, 1100));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    await _pump(
      tester,
      _client(requests: requests),
      ActivityWorkspaceRole.societyAdmin,
    );

    await tester.tap(find.text('Create activity'));
    await tester.pumpAndSettle();
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Activity title'),
      'Community Run',
    );
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Description'),
      'Morning health activity',
    );
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Venue'),
      'Main Gate',
    );
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Organizer name'),
      'Sports Committee',
    );
    await tester.tap(find.widgetWithText(FilledButton, 'Save activity'));
    await tester.pumpAndSettle();

    final request = requests.singleWhere(
      (row) => row.method == 'POST' && row.url.path == '/api/activities',
    );
    final body = jsonDecode(request.body);
    expect(body, containsPair('title', 'Community Run'));
    expect(body, containsPair('feePerPerson', 0));
    expect(body, containsPair('publishNow', true));
    expect(tester.takeException(), isNull);
  });

  testWidgets('society admin can approve a member gallery photo', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(1000, 1000));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    await _pump(
      tester,
      _client(requests: requests, includePhoto: true),
      ActivityWorkspaceRole.societyAdmin,
    );

    await tester.tap(find.text('Galleries'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Approve'));
    await tester.pumpAndSettle();

    final body = jsonDecode(
      requests
          .singleWhere(
            (row) => row.method == 'PATCH' && row.url.path.contains('/photos/'),
          )
          .body,
    );
    expect(body, {'action': 'APPROVE'});
    expect(tester.takeException(), isNull);
  });
}

Future<void> _pump(
  WidgetTester tester,
  ApiClient client,
  ActivityWorkspaceRole role,
) async {
  final bloc = ApiGatewayBloc(client);
  addTearDown(bloc.close);
  await tester.pumpWidget(
    MaterialApp(
      theme: SocietyTheme.light(),
      home: Scaffold(
        body: ActivityWorkspace(client: BlocBackedApiGateway(bloc), role: role),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

ApiClient _client({
  List<http.Request>? requests,
  bool paid = false,
  bool registered = false,
  bool includePhoto = false,
}) => HttpApiClient(
  token: 'test-token',
  baseUrl: 'http://localhost:8080',
  httpClient: MockClient((request) async {
    requests?.add(request);
    if (request.method == 'GET' && request.url.path == '/api/activities') {
      return http.Response(
        jsonEncode(
          _workspace(
            paid: paid,
            registered: registered,
            includePhoto: includePhoto,
          ),
        ),
        200,
      );
    }
    if (request.method == 'GET' &&
        request.url.path.startsWith('/api/activities/photos/')) {
      return http.Response.bytes(
        base64Decode(_pixel),
        200,
        headers: {'content-type': 'image/png'},
      );
    }
    if (request.method == 'POST' && request.url.path.endsWith('/join')) {
      return http.Response(jsonEncode(_participation(paid: paid)), 201);
    }
    if (request.method == 'POST' && request.url.path == '/api/activities') {
      return http.Response(jsonEncode(_activity(paid: paid)), 201);
    }
    if (request.method == 'POST' && request.url.path.endsWith('/checkout')) {
      return http.Response(
        jsonEncode({
          ..._participation(paid: true),
          'payment_status': 'PAID',
          'payment_method': 'RAZORPAY_UPI',
          'payment_reference': 'pay_activity_test_123',
        }),
        200,
      );
    }
    if (request.method == 'POST' && request.url.path.endsWith('/cash')) {
      return http.Response(
        jsonEncode({
          ..._participation(paid: true),
          'payment_status': 'PAID',
          'payment_method': 'CASH',
        }),
        200,
      );
    }
    if (request.method == 'PATCH' && request.url.path.contains('/photos/')) {
      return http.Response(
        jsonEncode({..._photo(), 'moderation_status': 'PUBLISHED'}),
        200,
      );
    }
    return http.Response(jsonEncode({'message': 'Not found'}), 404);
  }),
);

Map<String, dynamic> _workspace({
  required bool paid,
  required bool registered,
  required bool includePhoto,
}) => {
  'total_activities': 1,
  'upcoming': 1,
  'my_registrations': registered ? 1 : 0,
  'pending_payments': paid && registered ? 1 : 0,
  'pending_approvals': 0,
  'pending_photos': includePhoto ? 1 : 0,
  'activities': [_activity(paid: paid, registered: registered)],
  'participations': registered ? [_participation(paid: paid)] : [],
  'photos': includePhoto ? [_photo()] : [],
};

Map<String, dynamic> _activity({
  bool paid = false,
  bool registered = false,
}) => {
  'id': 'activity-1',
  'activity_code': 'ACT-TEST-1',
  'title': 'Society Cultural Evening',
  'category': 'CULTURAL',
  'description': 'Music, games, and community participation.',
  'venue': 'Community Hall',
  'starts_at': DateTime.now().add(const Duration(days: 4)).toIso8601String(),
  'ends_at': DateTime.now()
      .add(const Duration(days: 4, hours: 3))
      .toIso8601String(),
  'registration_opens_at': DateTime.now()
      .subtract(const Duration(days: 1))
      .toIso8601String(),
  'registration_closes_at': DateTime.now()
      .add(const Duration(days: 3))
      .toIso8601String(),
  'capacity': 100,
  'registered_count': registered ? 1 : 0,
  'available_places': registered ? 99 : 100,
  'fee_per_person': paid ? 250 : 0,
  'is_paid': paid,
  'approval_required': false,
  'cancellation_hours': 24,
  'audience': 'ALL_MEMBERS',
  'organizer_name': 'Cultural Committee',
  'status': 'PUBLISHED',
  if (registered) 'my_participation': _participation(paid: paid),
};

Map<String, dynamic> _participation({required bool paid}) => {
  'id': 'participation-1',
  'activity_id': 'activity-1',
  'activity_title': 'Society Cultural Evening',
  'participant_id': 'resident-1',
  'participant_name': 'Demo Member',
  'participant_count': 1,
  'amount': paid ? 250 : 0,
  'payment_status': paid ? 'PENDING' : 'FREE',
  'status': paid ? 'PENDING_PAYMENT' : 'CONFIRMED',
  'starts_at': DateTime.now().add(const Duration(days: 4)).toIso8601String(),
};

Map<String, dynamic> _photo() => {
  'id': 'photo-1',
  'activity_id': 'activity-1',
  'activity_title': 'Society Cultural Evening',
  'submitted_by': 'resident-1',
  'submitted_by_name': 'Demo Member',
  'caption': 'Community celebration',
  'moderation_status': 'PENDING',
  'featured': false,
  'photo_url': '/api/activities/photos/photo-1',
  'created_at': DateTime.now().toIso8601String(),
};

const _pixel =
    'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=';
