import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('resident emergency workspace fits narrow screens', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(390, 844));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    await _pump(tester, _client(), EmergencyWorkspaceRole.resident);

    expect(find.text('Emergency & accident response'), findsOneWidget);
    expect(find.text('Report emergency'), findsOneWidget);
    await tester.drag(find.byType(ListView).first, const Offset(0, -700));
    await tester.pumpAndSettle();
    expect(find.text('My emergency incidents'), findsOneWidget);
    expect(find.text('Acknowledge'), findsNothing);
    expect(find.text('Add contact'), findsNothing);
    expect(tester.takeException(), isNull);
  });

  testWidgets('resident can report a medical emergency for own space', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(900, 1100));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    await _pump(tester, _client(requests), EmergencyWorkspaceRole.resident);

    await tester.tap(find.text('Report emergency'));
    await tester.pumpAndSettle();
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Short incident title'),
      'Resident collapsed',
    );
    await tester.enterText(
      find.widgetWithText(TextFormField, 'What happened?'),
      'Resident is dizzy',
    );
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Exact location / landmark'),
      'Tower A lobby',
    );
    await tester.tap(find.widgetWithText(FilledButton, 'Report incident'));
    await tester.pumpAndSettle();

    final request = requests.singleWhere(
      (request) =>
          request.method == 'POST' && request.url.path == '/api/emergencies',
    );
    expect(jsonDecode(request.body), containsPair('incidentType', 'MEDICAL'));
    expect(jsonDecode(request.body), containsPair('severity', 'HIGH'));
    expect(jsonDecode(request.body), containsPair('flatId', 'flat-1'));
    expect(tester.takeException(), isNull);
  });

  testWidgets('staff can acknowledge a reported emergency', (tester) async {
    await tester.binding.setSurfaceSize(const Size(1100, 1100));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    await _pump(tester, _client(requests), EmergencyWorkspaceRole.staff);

    await tester.ensureVisible(find.text('Acknowledge'));
    await tester.tap(find.text('Acknowledge'));
    await tester.pumpAndSettle();
    await tester.tap(find.widgetWithText(FilledButton, 'Confirm'));
    await tester.pumpAndSettle();

    final body = jsonDecode(
      requests.singleWhere((request) => request.method == 'PATCH').body,
    );
    expect(body, {'action': 'ACKNOWLEDGE'});
    expect(tester.takeException(), isNull);
  });

  testWidgets('staff external-service record requires and sends call details', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(1100, 1200));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    await _pump(tester, _client(requests), EmergencyWorkspaceRole.staff);

    await tester.ensureVisible(find.text('Record ambulance'));
    await tester.tap(find.text('Record ambulance'));
    await tester.pumpAndSettle();
    expect(
      find.text(
        'This records a call already made; it does not contact the service automatically.',
      ),
      findsOneWidget,
    );
    await tester.enterText(
      find.widgetWithText(TextField, 'Dispatch / call details *'),
      'Called ambulance control',
    );
    await tester.enterText(
      find.widgetWithText(TextField, 'Call or dispatch reference'),
      'CALL-100',
    );
    await tester.tap(find.widgetWithText(FilledButton, 'Record call'));
    await tester.pumpAndSettle();

    final body = jsonDecode(
      requests.singleWhere((request) => request.method == 'PATCH').body,
    );
    expect(body, {
      'action': 'REQUEST_AMBULANCE',
      'note': 'Called ambulance control',
      'externalReference': 'CALL-100',
    });
  });

  testWidgets('society admin can add a verified directory contact', (
    tester,
  ) async {
    await tester.binding.setSurfaceSize(const Size(1000, 1000));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final requests = <http.Request>[];
    await _pump(tester, _client(requests), EmergencyWorkspaceRole.societyAdmin);

    await tester.tap(find.text('Add contact'));
    await tester.pumpAndSettle();
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Service / contact name'),
      'City Ambulance',
    );
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Phone number'),
      '1080000000',
    );
    await tester.tap(find.widgetWithText(FilledButton, 'Save contact'));
    await tester.pumpAndSettle();

    final body = jsonDecode(
      requests
          .singleWhere(
            (request) =>
                request.method == 'POST' &&
                request.url.path.endsWith('/directory'),
          )
          .body,
    );
    expect(body, containsPair('contactType', 'AMBULANCE'));
    expect(body, containsPair('name', 'City Ambulance'));
    expect(body, containsPair('priority', 1));
    expect(tester.takeException(), isNull);
  });
}

Future<void> _pump(
  WidgetTester tester,
  ApiClient client,
  EmergencyWorkspaceRole role,
) async {
  final bloc = ApiGatewayBloc(client);
  addTearDown(bloc.close);
  await tester.pumpWidget(
    MaterialApp(
      theme: SocietyTheme.light(),
      home: Scaffold(
        body: EmergencyWorkspace(
          client: BlocBackedApiGateway(bloc),
          role: role,
        ),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

ApiClient _client([List<http.Request>? requests]) => HttpApiClient(
  token: 'test-token',
  baseUrl: 'http://localhost:8080',
  httpClient: MockClient((request) async {
    requests?.add(request);
    if (request.method == 'GET' && request.url.path == '/api/emergencies') {
      return http.Response(jsonEncode(_workspace()), 200);
    }
    if (request.method == 'POST' && request.url.path == '/api/emergencies') {
      return http.Response(jsonEncode(_incident()), 201);
    }
    if (request.method == 'POST' && request.url.path.endsWith('/directory')) {
      return http.Response(
        jsonEncode({
          'id': 'directory-2',
          'contact_type': 'AMBULANCE',
          'name': 'City Ambulance',
          'phone': '1080000000',
          'priority': 1,
        }),
        201,
      );
    }
    if (request.method == 'PATCH') {
      return http.Response(
        jsonEncode({..._incident(), 'status': 'ACKNOWLEDGED'}),
        200,
      );
    }
    return http.Response(jsonEncode({'message': 'Not found'}), 404);
  }),
);

Map<String, dynamic> _workspace() => {
  'total': 1,
  'open': 1,
  'critical': 1,
  'responding': 0,
  'resolved': 0,
  'directory': [
    {
      'id': 'directory-1',
      'contact_type': 'SOCIETY_SECURITY',
      'name': 'Main Gate Security',
      'phone': '9000000000',
      'available_24x7': true,
      'priority': 1,
    },
  ],
  'resident_options': [
    {
      'resident_id': 'resident-1',
      'resident_name': 'Demo Member',
      'resident_mobile': '9000000001',
      'flat_id': 'flat-1',
      'block': 'Tower A',
      'space_number': '101',
      'emergency_contacts': [],
    },
  ],
  'responder_options': [
    {'id': 'staff-1', 'name': 'Security Guard', 'mobile': '9000000002'},
  ],
  'incidents': [_incident()],
};

Map<String, dynamic> _incident() => {
  'id': 'incident-1',
  'incident_number': 'EMG-TEST-1',
  'incident_type': 'MEDICAL',
  'severity': 'CRITICAL',
  'title': 'Medical assistance needed',
  'description': 'Resident reported dizziness in the lobby.',
  'location_details': 'Tower A lobby',
  'people_affected': 1,
  'status': 'REPORTED',
  'resident_id': 'resident-1',
  'resident_name': 'Demo Member',
  'resident_mobile': '9000000001',
  'flat_id': 'flat-1',
  'block': 'Tower A',
  'space_number': '101',
  'emergency_contact_notified': false,
  'ambulance_requested': false,
  'police_requested': false,
  'fire_service_requested': false,
  'created_at': DateTime.now().toIso8601String(),
  'updated_at': DateTime.now().toIso8601String(),
};
