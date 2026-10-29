import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_admin_app/core/domain/usecases/admin_resource_use_cases.dart';
import 'package:society_admin_app/features/profile/presentation/bloc/profile_bloc.dart';
import 'package:society_admin_app/features/profile/presentation/pages/profile_view.dart';
import 'package:society_ui/society_ui.dart';
import 'support/fake_admin_repository.dart';

class _SubscriptionService extends FakeAdminRepository {
  Map<String, dynamic>? updated;

  @override
  Future<Map<String, dynamic>> map(String path) async {
    if (path.endsWith('/subscription')) {
      return {
        'plan_code': 'STANDARD',
        'selected_spaces': 10,
        'selected_staff': 2,
        'configured_spaces': 8,
        'active_staff': 1,
        'price_per_space': 10,
        'price_per_staff': 50,
        'base_monthly_price': 200,
        'service_monthly_price': 0,
        'estimated_monthly_price': 200,
        'billing_cycle': 'MONTHLY',
        'billing_cycle_months': 1,
        'billing_cycle_amount': 200,
        'service_catalog': [
          {
            'code': 'RESIDENTS',
            'name': 'Residents',
            'status': 'ACTIVE',
            'availability_type': 'DEFAULT_FREE',
            'price_per_space': 0,
            'selected': true,
            'required': true,
          },
          {
            'code': 'AMENITIES',
            'name': 'Amenities',
            'status': 'ACTIVE',
            'availability_type': 'PAID_PER_SPACE',
            'price_per_space': 2.5,
            'selected': false,
            'required': false,
          },
          {
            'code': 'NOTICES',
            'name': 'Notices',
            'status': 'ACTIVE',
            'availability_type': 'OPTIONAL_FREE',
            'price_per_space': 0,
            'selected': false,
            'required': false,
          },
        ],
      };
    }
    return {
      'name': 'Test Society',
      'registration_number': 'REG-1',
      'society_type': 'COOPERATIVE',
      'contact_person': 'Admin',
      'email': 'admin@example.com',
      'mobile': '9000000000',
      'city': 'Pune',
      'state': 'Maharashtra',
      'pin_code': '411001',
    };
  }

  @override
  Future<dynamic> put(String path, Map<String, dynamic> body) async {
    if (path.endsWith('/subscription/quantities')) {
      updated = Map<String, dynamic>.from(body);
    }
    return <String, dynamic>{};
  }
}

void main() {
  testWidgets('society admin selects paid and free services with live total', (
    tester,
  ) async {
    final service = _SubscriptionService();
    var refreshed = false;
    await tester.pumpWidget(
      MaterialApp(
        theme: SocietyTheme.light(),
        home: Scaffold(
          body: BlocProvider(
            create: (_) => ProfileBloc(AdminResourceUseCases(service)),
            child: ProfileView(onEntitlementsChanged: () => refreshed = true),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.tap(find.text('Change requirements'));
    await tester.pumpAndSettle();

    expect(find.text('Residents · Included free'), findsOne);
    await tester.ensureVisible(find.text('Amenities · ₹2.5/space/month'));
    await tester.tap(find.text('Amenities · ₹2.5/space/month'));
    await tester.pumpAndSettle();
    expect(find.text('Calculated amount for this cycle: ₹225.00'), findsOne);
    await tester.ensureVisible(find.text('Notices · Free'));
    await tester.tap(find.text('Notices · Free'));
    await tester.ensureVisible(
      find.widgetWithText(FilledButton, 'Update subscription'),
    );
    await tester.tap(find.widgetWithText(FilledButton, 'Update subscription'));
    await tester.pumpAndSettle();

    expect(
      service.updated?['enabledModules'],
      containsAll(['RESIDENTS', 'AMENITIES', 'NOTICES']),
    );
    expect(refreshed, isTrue);
  });
}
