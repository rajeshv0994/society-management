import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:super_admin_app/features/services/presentation/bloc/service_catalog_bloc.dart';
import 'package:super_admin_app/features/services/presentation/pages/services_view.dart';
import 'package:super_admin_app/features/services/domain/entities/service_catalog_item.dart';
import 'package:super_admin_app/features/services/domain/usecases/service_catalog_use_cases.dart';
import 'support/fake_feature_repositories.dart';

class _CatalogService extends FakeFeatureRepositories {
  Map<String, dynamic>? created;

  @override
  Future<List<ServiceCatalogItem>> loadCatalog() async => [
    ServiceCatalogItem({
      'code': 'RESIDENTS',
      'name': 'Residents',
      'description': 'Core resident records',
      'status': 'ACTIVE',
      'availability_type': 'DEFAULT_FREE',
      'price_per_space': 0,
      'sort_order': 10,
    }),
    ServiceCatalogItem({
      'code': 'AMENITIES',
      'name': 'Amenities',
      'description': 'Bookings and payments',
      'status': 'ACTIVE',
      'availability_type': 'PAID_PER_SPACE',
      'price_per_space': 2.5,
      'sort_order': 20,
    }),
  ];

  @override
  Future<void> saveService(SaveServiceCommand command) async {
    created = Map<String, dynamic>.from(command.values);
  }
}

void main() {
  testWidgets(
    'catalog clearly shows free defaults and paid per-space services',
    (tester) async {
      tester.view.physicalSize = const Size(390, 844);
      tester.view.devicePixelRatio = 1;
      addTearDown(tester.view.resetPhysicalSize);
      addTearDown(tester.view.resetDevicePixelRatio);
      await tester.pumpWidget(
        MaterialApp(
          home: BlocProvider(
            create: (_) =>
                ServiceCatalogBloc(ServiceCatalogUseCases(_CatalogService())),
            child: const ServicesView(),
          ),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.textContaining('Default for all societies · Free'), findsOne);
      await tester.drag(find.byType(ListView), const Offset(0, -1000));
      await tester.pumpAndSettle();
      expect(find.textContaining('₹2.5 per space/month'), findsOne);
      expect(tester.takeException(), isNull);
    },
  );

  testWidgets('super admin creates a paid per-space catalog service', (
    tester,
  ) async {
    final service = _CatalogService();
    await tester.pumpWidget(
      MaterialApp(
        home: BlocProvider(
          create: (_) => ServiceCatalogBloc(ServiceCatalogUseCases(service)),
          child: const ServicesView(),
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.tap(find.text('Add service'));
    await tester.pumpAndSettle();

    await tester.enterText(
      find.widgetWithText(TextFormField, 'Service code'),
      'GYM_PREMIUM',
    );
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Service name'),
      'Premium gym',
    );
    await tester.tap(
      find.widgetWithText(DropdownButtonFormField<String>, 'COMING SOON'),
    );
    await tester.pumpAndSettle();
    await tester.tap(find.text('ACTIVE').last);
    await tester.pumpAndSettle();
    await tester.ensureVisible(find.byKey(const Key('service-availability')));
    await tester.tap(find.byKey(const Key('service-availability')));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Optional · paid per space').last);
    await tester.pumpAndSettle();
    await tester.ensureVisible(
      find.byKey(const Key('service-price-per-space')),
    );
    await tester.enterText(
      find.byKey(const Key('service-price-per-space')),
      '4.75',
    );
    await tester.pump();
    await tester.ensureVisible(find.widgetWithText(FilledButton, 'Save'));
    await tester.tap(find.widgetWithText(FilledButton, 'Save'));
    await tester.pumpAndSettle();

    expect(service.created?['code'], 'GYM_PREMIUM');
    expect(service.created?['availabilityType'], 'PAID_PER_SPACE');
    expect(service.created?['pricePerSpace'], 4.75);
  });
}
