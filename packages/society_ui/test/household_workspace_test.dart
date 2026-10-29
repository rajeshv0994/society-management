import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  testWidgets(
    'resident household renders all records without narrow overflow',
    (tester) async {
      await tester.binding.setSurfaceSize(const Size(390, 844));
      addTearDown(() => tester.binding.setSurfaceSize(null));
      await _pump(tester, _client(), HouseholdWorkspaceRole.resident);

      expect(find.text('My household'), findsOneWidget);
      expect(find.text('Demo Society Member'), findsOneWidget);
      expect(find.text('Household Test Member'), findsOneWidget);
      expect(find.text('Add member'), findsOneWidget);
      expect(find.text('Verify'), findsNothing);
      expect(tester.takeException(), isNull);
    },
  );

  testWidgets('manager sees household metrics and can verify pending member', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(900, 1200));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    await _pump(
      tester,
      _client(requests: requests),
      HouseholdWorkspaceRole.societyAdmin,
    );

    expect(find.text('Household management'), findsOneWidget);
    expect(find.text('Pending checks'), findsOneWidget);
    expect(find.text('1'), findsWidgets);
    await tester.ensureVisible(find.text('Verify'));
    await tester.tap(find.text('Verify'));
    await tester.pumpAndSettle();
    await tester.tap(find.widgetWithText(FilledButton, 'Verify').last);
    await tester.pumpAndSettle();

    final request = requests.singleWhere(
      (request) => request.url.path.endsWith('/members/member-1/verification'),
    );
    expect(request.method, 'PATCH');
    expect(jsonDecode(request.body), {
      'residentId': 'resident-1',
      'status': 'VERIFIED',
    });
    expect(tester.takeException(), isNull);
  });

  testWidgets('resident selects a configured available parking slot', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(900, 1200));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    await _pump(
      tester,
      _client(requests: requests),
      HouseholdWorkspaceRole.resident,
    );

    await tester.drag(find.byType(ListView), const Offset(0, -700));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Add vehicle'));
    await tester.pumpAndSettle();
    await tester.enterText(
      find.widgetWithText(TextField, 'Registration number'),
      'MH 12 AB 1234',
    );
    await tester.tap(find.byType(DropdownButtonFormField<String>).last);
    await tester.pumpAndSettle();
    await tester.tap(find.text('P-12 · Car · Available').last);
    await tester.pumpAndSettle();
    await tester.ensureVisible(
      find.widgetWithText(FilledButton, 'Save vehicle'),
    );
    await tester.tap(find.widgetWithText(FilledButton, 'Save vehicle'));
    await tester.pumpAndSettle();

    final request = requests.singleWhere(
      (request) => request.url.path == '/api/households/vehicles',
    );
    final body = jsonDecode(request.body) as Map<String, dynamic>;
    expect(body['registrationNumber'], 'MH 12 AB 1234');
    expect(body['parkingSlot'], 'P-12');
    expect(tester.takeException(), isNull);
  });
}

Future<void> _pump(
  WidgetTester tester,
  ApiClient client,
  HouseholdWorkspaceRole role,
) async {
  final bloc = ApiGatewayBloc(client);
  addTearDown(bloc.close);
  await tester.pumpWidget(
    MaterialApp(
      theme: SocietyTheme.light(),
      home: Scaffold(
        body: HouseholdWorkspace(
          client: BlocBackedApiGateway(bloc),
          role: role,
        ),
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
    if (request.method == 'GET' && request.url.path == '/api/households') {
      return http.Response(jsonEncode(_workspace), 200);
    }
    if (request.method == 'GET' &&
        request.url.path == '/api/configuration/reference') {
      return http.Response(
        jsonEncode({
          'parking': [
            {
              'id': 'parking-1',
              'slot_number': 'P-12',
              'slot_type': 'CAR',
              'status': 'AVAILABLE',
            },
            {
              'id': 'parking-current',
              'slot_number': 'P-TEST-41',
              'slot_type': 'CAR',
              'status': 'ALLOCATED',
            },
          ],
        }),
        200,
      );
    }
    if (request.url.path.endsWith('/verification')) {
      return http.Response(
        jsonEncode({..._member, 'verification_status': 'VERIFIED'}),
        200,
      );
    }
    if (request.method == 'POST' &&
        request.url.path == '/api/households/vehicles') {
      return http.Response(jsonEncode({..._vehicle, 'id': 'vehicle-2'}), 200);
    }
    return http.Response(jsonEncode({'message': 'Not found'}), 404);
  }),
);

final _member = {
  'id': 'member-1',
  'member_code': 'HM-TEST0001',
  'resident_id': 'resident-1',
  'full_name': 'Household Test Member',
  'relationship': 'SPOUSE',
  'mobile': '9000012345',
  'email': 'household@example.com',
  'date_of_birth': '1992-05-12',
  'gender': 'FEMALE',
  'blood_group': 'B+',
  'occupation': 'Designer',
  'identity_type': 'AADHAAR',
  'identity_last4': '1234',
  'emergency_contact_name': 'Demo Society Member',
  'emergency_contact_mobile': '9000000021',
  'access_enabled': true,
  'verification_status': 'PENDING',
  'active': true,
};

final _vehicle = {
  'id': 'vehicle-1',
  'resident_id': 'resident-1',
  'vehicle_type': 'CAR',
  'registration_number': 'MH12TEST41',
  'make': 'Tata',
  'model': 'Nexon',
  'color': 'Blue',
  'parking_slot': 'P-TEST-41',
  'sticker_number': 'ST-41',
  'verification_status': 'VERIFIED',
  'active': true,
};

final _workspace = {
  'resident_count': 1,
  'member_count': 1,
  'vehicle_count': 1,
  'pending_verification': 1,
  'households': [
    {
      'resident': {
        'id': 'resident-1',
        'full_name': 'Demo Society Member',
        'email': 'member.green@society.local',
        'mobile': '9000000021',
        'status': 'ACTIVE',
      },
      'spaces': [
        {
          'id': 'space-1',
          'block': 'Tower A',
          'space_number': '101',
          'resident_type': 'OWNER',
          'occupancy_status': 'OWNER_OCCUPIED',
        },
      ],
      'household': [_member],
      'vehicles': [_vehicle],
      'emergency_contacts': [
        {
          'id': 'contact-1',
          'resident_id': 'resident-1',
          'full_name': 'Emergency Contact',
          'relationship': 'BROTHER',
          'mobile': '9111111111',
          'priority': 1,
          'active': true,
        },
      ],
    },
  ],
};
