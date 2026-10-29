import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_admin_app/core/domain/usecases/admin_resource_use_cases.dart';
import 'package:society_admin_app/features/configuration/presentation/bloc/configuration_bloc.dart';
import 'package:society_admin_app/features/configuration/presentation/pages/configuration_view.dart';
import 'package:society_ui/society_ui.dart';
import 'support/fake_admin_repository.dart';

class _Call {
  const _Call(this.method, this.path, this.body);

  final String method;
  final String path;
  final Map<String, dynamic> body;
}

class _ConfigurationService extends FakeAdminRepository {
  final calls = <_Call>[];

  @override
  Future<Map<String, dynamic>> map(String path) async => {
    'parking': [
      {
        'id': 'parking-1',
        'slot_number': 'P-01',
        'slot_type': 'CAR',
        'status': 'AVAILABLE',
      },
    ],
    'vendors': [
      {
        'id': 'vendor-1',
        'name': 'Safe Electric',
        'service_type': 'Electrical',
        'contact_person': 'Ravi',
        'mobile': '9876543210',
        'email': 'safe@example.com',
        'status': 'ACTIVE',
      },
    ],
    'complaintCategories': [
      {
        'id': 'category-1',
        'name': 'Electrical',
        'sla_hours': 24,
        'active': true,
      },
    ],
    'chargeRules': [
      {
        'id': 'charge-1',
        'name': 'Maintenance',
        'calculation_type': 'FIXED',
        'amount': 1500,
        'frequency': 'MONTHLY',
        'effective_from': '2026-07-01',
        'active': true,
      },
    ],
    'spaces': [
      {'id': 'space-1', 'label': 'A · Residential · 101'},
    ],
    'summary': {
      'parking_total': 1,
      'parking_available': 1,
      'parking_allocated': 0,
      'active_vendors': 1,
      'active_categories': 1,
      'active_charge_rules': 1,
    },
    'setup': {
      'setup_completed': false,
      'ready': true,
      'property_groups': 1,
      'spaces': 1,
      'checklist': [
        {'label': 'Property structure', 'required': true, 'complete': true},
        {'label': 'Parking', 'required': false, 'complete': true},
      ],
    },
  };

  Future<dynamic> _record(
    String method,
    String path,
    Map<String, dynamic> body,
  ) async {
    calls.add(_Call(method, path, Map<String, dynamic>.from(body)));
    return <String, dynamic>{};
  }

  @override
  Future<dynamic> post(String path, Map<String, dynamic> body) =>
      _record('POST', path, body);

  @override
  Future<dynamic> put(String path, Map<String, dynamic> body) =>
      _record('PUT', path, body);

  @override
  Future<dynamic> patch(String path, Map<String, dynamic> body) =>
      _record('PATCH', path, body);
}

const _modules = {'PARKING', 'VENDORS', 'COMPLAINTS', 'BILLING'};

Future<void> _pump(
  WidgetTester tester,
  _ConfigurationService service, {
  Size size = const Size(1200, 900),
}) async {
  tester.view.physicalSize = size;
  tester.view.devicePixelRatio = 1;
  addTearDown(tester.view.resetPhysicalSize);
  addTearDown(tester.view.resetDevicePixelRatio);
  await tester.pumpWidget(
    MaterialApp(
      theme: SocietyTheme.light(),
      home: Scaffold(
        body: BlocProvider(
          create: (_) => ConfigurationBloc(AdminResourceUseCases(service)),
          child: const ConfigurationView(enabledModules: _modules),
        ),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('configuration remains usable on a narrow viewport', (
    tester,
  ) async {
    await _pump(tester, _ConfigurationService(), size: const Size(390, 844));

    expect(find.text('Setup readiness'), findsOneWidget);
    await tester.drag(find.byType(ListView), const Offset(0, -700));
    await tester.pumpAndSettle();
    expect(find.text('Parking management'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('creates parking and allocates it to a configured space', (
    tester,
  ) async {
    final service = _ConfigurationService();
    await _pump(tester, service);

    await tester.tap(find.text('Add slot'));
    await tester.pumpAndSettle();
    await tester.enterText(find.byKey(const Key('parking-number')), 'p-02');
    await tester.tap(find.byKey(const Key('save-parking')));
    await tester.pumpAndSettle();

    expect(service.calls.last.method, 'POST');
    expect(service.calls.last.path, '/api/society/configuration/parking');
    expect(service.calls.last.body['slotNumber'], 'P-02');

    await tester.tap(find.byType(PopupMenuButton<String>).first);
    await tester.pumpAndSettle();
    await tester.tap(find.text('Allocate'));
    await tester.pumpAndSettle();
    await tester.tap(find.byType(DropdownButtonFormField<String>));
    await tester.pumpAndSettle();
    await tester.tap(find.text('A · Residential · 101').last);
    await tester.enterText(
      find.widgetWithText(TextField, 'Vehicle number'),
      'mh12ab1234',
    );
    await tester.tap(find.widgetWithText(FilledButton, 'Allocate'));
    await tester.pumpAndSettle();

    expect(service.calls.last.method, 'PATCH');
    expect(
      service.calls.last.path,
      '/api/society/configuration/parking/parking-1',
    );
    expect(service.calls.last.body, {
      'action': 'ALLOCATE',
      'flatId': 'space-1',
      'vehicleNumber': 'MH12AB1234',
    });
  });

  testWidgets('vendor requires and submits the actual contact details', (
    tester,
  ) async {
    final service = _ConfigurationService();
    await _pump(tester, service);
    await tester.ensureVisible(find.text('Add vendor'));
    await tester.tap(find.text('Add vendor'));
    await tester.pumpAndSettle();

    await tester.enterText(find.byKey(const Key('vendor-name')), 'Clean Co');
    await tester.enterText(
      find.widgetWithText(TextField, 'Service type'),
      'Housekeeping',
    );
    await tester.enterText(
      find.widgetWithText(TextField, 'Contact person'),
      'Meera',
    );
    await tester.enterText(
      find.widgetWithText(TextField, 'Mobile'),
      '9123456780',
    );
    await tester.enterText(
      find.widgetWithText(TextField, 'Email'),
      'meera@clean.example',
    );
    await tester.tap(find.byKey(const Key('save-vendor')));
    await tester.pumpAndSettle();

    expect(service.calls.last.path, '/api/society/configuration/vendors');
    expect(service.calls.last.body['mobile'], '9123456780');
    expect(service.calls.last.body['contactPerson'], 'Meera');
    expect(service.calls.last.body['email'], 'meera@clean.example');
  });

  testWidgets('creates an effective-dated charge and completes setup', (
    tester,
  ) async {
    final service = _ConfigurationService();
    await _pump(tester, service);
    await tester.drag(find.byType(ListView), const Offset(0, -3000));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Add charge rule'));
    await tester.pumpAndSettle();

    await tester.enterText(
      find.byKey(const Key('charge-name')),
      'Sinking fund',
    );
    await tester.enterText(
      find.widgetWithText(TextField, 'Amount / rate'),
      '250',
    );
    await tester.enterText(
      find.widgetWithText(TextField, 'Effective from · YYYY-MM-DD'),
      '2026-08-01',
    );
    await tester.enterText(
      find.widgetWithText(TextField, 'Effective to · optional'),
      '2027-07-31',
    );
    await tester.tap(find.byKey(const Key('save-charge')));
    await tester.pumpAndSettle();

    expect(service.calls.last.path, '/api/society/configuration/charge-rules');
    expect(service.calls.last.body['amount'], 250.0);
    expect(service.calls.last.body['effectiveFrom'], '2026-08-01');
    expect(service.calls.last.body['effectiveTo'], '2027-07-31');

    await tester.drag(find.byType(ListView), const Offset(0, 3000));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Complete setup'));
    await tester.pumpAndSettle();
    await tester.tap(find.widgetWithText(FilledButton, 'Complete setup').last);
    await tester.pumpAndSettle();
    expect(service.calls.last.path, '/api/society/configuration/complete');
  });
}
