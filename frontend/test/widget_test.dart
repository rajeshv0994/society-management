import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:society_app/main.dart';
import 'package:society_app/features/public_portal/domain/public_models.dart';
import 'package:society_app/features/public_portal/domain/public_repository.dart';

void main() {
  testWidgets('marketing home renders core calls to action', (tester) async {
    tester.view.physicalSize = const Size(1440, 1000);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    await tester.pumpWidget(const SocietyApp());
    await tester.pumpAndSettle();

    expect(find.text('SocietyHub'), findsOneWidget);
    expect(
      find.text('Your entire society,\nworking beautifully together.'),
      findsOneWidget,
    );
    expect(find.text('Start your society'), findsOneWidget);
    expect(find.text('Open your portal'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('services and role portals are reachable from navigation', (
    tester,
  ) async {
    tester.view.physicalSize = const Size(1200, 900);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    await tester.pumpWidget(const SocietyApp());
    await tester.pumpAndSettle();

    await tester.tap(find.text('Services'));
    await tester.pumpAndSettle();
    expect(find.text('Visitor management'), findsOneWidget);
    expect(find.text('Billing & collections'), findsOneWidget);

    await tester.tap(find.text('Open portal'));
    await tester.pumpAndSettle();
    expect(find.text('Member'), findsOneWidget);
    expect(find.text('Society Admin'), findsOneWidget);
    expect(find.text('Staff'), findsOneWidget);
    expect(find.text('Platform Admin'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('home hero is responsive on a mobile viewport', (tester) async {
    tester.view.physicalSize = const Size(390, 844);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    await tester.pumpWidget(const SocietyApp());
    await tester.pumpAndSettle();

    expect(find.text('Register'), findsOneWidget);
    expect(find.text('Start your society'), findsOneWidget);
    expect(find.text('Open your portal'), findsOneWidget);
  });

  testWidgets(
    'society registration uses an auto-generated registration number',
    (tester) async {
      await tester.pumpWidget(
        RepositoryProvider<PublicRepository>.value(
          value: _FakePublicRepository(),
          child: const MaterialApp(home: SocietyRegistrationPage()),
        ),
      );

      expect(find.text('Number of unit spaces'), findsOneWidget);
      expect(find.text('Registration number'), findsNothing);
      expect(find.text('Select services'), findsOneWidget);
    },
  );
}

class _FakePublicRepository implements PublicRepository {
  @override
  Future<void> activate(SocietyActivationCommand command) async {}
  @override
  Future<PublicRecord> login(LoginCommand command) async =>
      PublicRecord.from({'token': 'token'});
  @override
  Future<List<PublicRecord>> loadPlans() async => const [];
  @override
  Future<List<PublicRecord>> loadServices() async => const [];
  @override
  Future<List<PublicRecord>> loadSocieties() async => const [];
  @override
  Future<PublicRecord> register(SocietyRegistrationCommand command) async =>
      PublicRecord.from({'registrationNumber': 'REG-1'});
  @override
  Future<PublicRecord> submitRequirement(RequirementCommand command) async =>
      PublicRecord.from({'requirementId': 'REQ-1'});
}
