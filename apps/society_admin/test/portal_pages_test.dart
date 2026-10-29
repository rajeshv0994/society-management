import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_admin_app/app/dependency_injection.dart';
import 'package:society_admin_app/core/domain/usecases/admin_resource_use_cases.dart';
import 'package:society_admin_app/features/audit/presentation/bloc/audit_bloc.dart';
import 'package:society_admin_app/features/residents/presentation/bloc/residents_bloc.dart';
import 'package:society_admin_app/features/residents/presentation/pages/residents_view.dart';
import 'package:society_admin_app/features/staff/presentation/bloc/staff_bloc.dart';
import 'package:society_admin_app/features/storage/presentation/bloc/storage_bloc.dart';
import 'package:society_admin_app/features/structure/presentation/bloc/structure_bloc.dart';
import 'package:society_admin_app/features/shell/presentation/pages/shell_view.dart';
import 'package:society_admin_app/features/staff/presentation/pages/staff_view.dart';
import 'package:society_admin_app/features/structure/presentation/pages/structure_view.dart';
import 'package:society_admin_app/features/audit/presentation/pages/audit_view.dart';
import 'package:society_admin_app/features/storage/presentation/pages/storage_view.dart';
import 'package:society_admin_app/features/support/presentation/bloc/support_bloc.dart';
import 'package:society_admin_app/features/support/presentation/pages/support_view.dart';
import 'package:society_ui/society_ui.dart';
import 'support/fake_admin_repository.dart';

class FakeService extends FakeAdminRepository {
  String? lastAuditSearch;
  bool? lastSensitiveOnly;
  int? purchasedStorageBlocks;
  String? deletedStorageFile;
  Map<String, dynamic>? createdResident;

  Map<String, dynamic> get storageData => {
    'used_bytes': 1048576,
    'free_bytes': 5368709120,
    'purchased_bytes': 0,
    'total_bytes': 5368709120,
    'available_bytes': 5367660544,
    'usage_percent': 0.02,
    'monthly_addon_amount': 0,
    'policy': {
      'default_free_gb': 5,
      'purchase_block_gb': 5,
      'price_per_block_monthly': 199,
    },
    'files': [
      {
        'id': 'file-1',
        'name': 'Annual meeting photo',
        'category': 'IMAGES',
        'size_bytes': 1048576,
        'cleanup_eligible': true,
      },
    ],
    'purchases': [],
  };

  Map<String, dynamic> auditData(int page, int size) => {
    'items': [
      {
        'id': 1,
        'action': 'API_UPDATE_PERMISSIONS',
        'entity_type': 'CONFIGURATION',
        'entity_id': 'staff-1',
        'actor_user_id': 'admin-1',
        'actor_name': 'Society Admin',
        'sensitive': true,
        'occurred_at': '2026-07-28T12:00:00Z',
        'details': {'http_method': 'PUT', 'outcome': 'SUCCEEDED'},
      },
    ],
    'page': page,
    'size': size,
    'total': 1,
    'total_pages': 1,
  };

  @override
  Future<Map<String, dynamic>> map(String path) async {
    if (path.contains('/society/storage')) return storageData;
    if (path.contains('/society/audit')) {
      final uri = Uri.parse(path);
      lastAuditSearch = uri.queryParameters['search'];
      lastSensitiveOnly = uri.queryParameters['sensitiveOnly'] == 'true';
      return auditData(
        int.tryParse(uri.queryParameters['page'] ?? '0') ?? 0,
        int.tryParse(uri.queryParameters['size'] ?? '50') ?? 50,
      );
    }
    if (path.endsWith('/society/residents/r1')) {
      return {
        'resident': {
          'id': 'r1',
          'full_name': 'Resident One',
          'email': 'resident@test.local',
          'mobile': '9000000001',
          'status': 'ACTIVE',
          'resident_type': 'OWNER',
        },
        'registration': {
          'move_in_date': '2026-01-01',
          'emergency_contact': '9000000002',
        },
        'space': {
          'id': 'x1',
          'block': 'A',
          'section': 'Residential area',
          'space_number': 'House 101',
          'occupancy_status': 'OWNER_OCCUPIED',
        },
        'household': [],
        'complaints': [
          {
            'id': 'c1',
            'title': 'Lift issue',
            'description': 'Lift stopped',
            'priority': 'NORMAL',
            'status': 'OPEN',
          },
        ],
        'invoices': [],
        'payments': [],
        'visitors': [],
        'amenityBookings': [],
      };
    }
    if (path.contains('dashboard')) {
      return {
        'spaces': 24,
        'members': 2,
        'open_complaints': 1,
        'visitors_inside': 0,
        'unpaid_invoices': 3,
      };
    }
    if (path.contains('/society/subscription')) {
      return {
        'plan_code': 'STANDARD',
        'selected_spaces': 24,
        'selected_staff': 5,
        'configured_spaces': 1,
        'active_staff': 1,
        'price_per_space': 10,
        'price_per_staff': 50,
        'max_spaces': 100,
        'max_staff': 20,
        'estimated_monthly_price': 490,
      };
    }
    if (path.contains('/society/profile')) {
      return {
        'id': 'society-1',
        'name': 'Test Residency',
        'registration_number': 'REG-1',
        'society_type': 'COOPERATIVE',
        'contact_person': 'Test Admin',
        'email': 'admin@test.local',
        'mobile': '9000000000',
        'address': 'Test address',
        'city': 'Pune',
        'state': 'Maharashtra',
        'pin_code': '411001',
        'plan_code': 'STANDARD',
        'planned_staff': 5,
        'enabled_modules':
            '["RESIDENTS","STAFF","VISITORS","COMPLAINTS","BILLING","PAYMENTS","AMENITIES","ACTIVITIES_GALLERIES","DELIVERIES","EMERGENCY_INCIDENTS","NOTICES","DOCUMENTS","PARKING","VENDORS"]',
      };
    }
    if (path.contains('structure')) {
      return {
        'blocks': [
          {'id': 'b1', 'name': 'A', 'floor_count': 1},
        ],
        'sections': [
          {
            'id': 'f1',
            'block_id': 'b1',
            'name': 'Residential area',
            'section_order': 1,
            'block': 'A',
          },
        ],
        'spaces': [
          {
            'id': 'x1',
            'section_id': 'f1',
            'block': 'A',
            'section': 'Residential area',
            'space_number': 'House 101',
            'occupancy_status': 'VACANT',
          },
        ],
      };
    }
    if (path.contains('configuration')) {
      return {
        'parking': [],
        'vendors': [],
        'complaintCategories': [],
        'chargeRules': [],
        'spaces': [
          {
            'id': 'x1',
            'label': 'A · Residential area · House 101',
            'space_number': 'House 101',
            'occupancy_status': 'VACANT',
          },
        ],
      };
    }
    return {};
  }

  @override
  Future<List<Map<String, dynamic>>> list(String path) async {
    if (path.contains('residents/pending')) return [];
    if (path.endsWith('/residents')) {
      return [
        {
          'id': 'r1',
          'full_name': 'Resident One',
          'email': 'resident@test.local',
          'status': 'ACTIVE',
          'resident_type': 'OWNER',
          'space_number': 'House 101',
          'block': 'A',
        },
      ];
    }
    if (path.contains('staff')) {
      return [
        {
          'id': 's1',
          'full_name': 'Guard',
          'email': 'guard@test.com',
          'status': 'ACTIVE',
        },
      ];
    }
    if (path.contains('complaints')) {
      return [
        {'id': 'c1', 'title': 'Lift', 'description': 'Issue', 'status': 'OPEN'},
      ];
    }
    if (path.contains('visitors')) {
      return [
        {
          'id': 'v1',
          'visitor_name': 'Guest',
          'purpose': 'Visit',
          'status': 'EXPECTED',
        },
      ];
    }
    if (path.contains('amenities')) {
      return [
        {'id': 'a1', 'name': 'Clubhouse', 'status': 'ACTIVE'},
      ];
    }
    if (path.contains('notices')) {
      return [
        {'id': 'n1', 'title': 'Meeting', 'body': 'AGM'},
      ];
    }
    if (path.contains('tasks')) {
      return [
        {
          'id': 't1',
          'title': 'Inspect',
          'assigned_to_name': 'Guard',
          'status': 'OPEN',
        },
      ];
    }
    if (path.contains('/billing/payments')) {
      return [
        {
          'id': 'pay1',
          'invoice_number': 'INV-1',
          'amount': 500,
          'payment_method': 'UPI',
          'status': 'SUCCESS',
          'paid_at': '2026-07-26T10:00:00Z',
        },
      ];
    }
    if (path.contains('/society/documents')) {
      return [
        {
          'id': 'doc1',
          'document_type': 'GST',
          'storage_key': 'society/doc.pdf',
          'verification_status': 'VERIFIED',
        },
      ];
    }
    if (path.contains('support')) {
      return [
        {
          'id': 'p1',
          'subject': 'Help',
          'priority': 'NORMAL',
          'status': 'OPEN',
          'message_count': 1,
        },
      ];
    }
    return [];
  }

  @override
  Future<Object?> post(String path, Map<String, dynamic> body) async {
    if (path.contains('/storage/purchases')) {
      purchasedStorageBlocks = (body['blocks'] as num).toInt();
    }
    if (path.endsWith('/society/residents')) createdResident = body;
    return {};
  }

  @override
  Future<Object?> delete(String path) async {
    if (path.contains('/storage/files/')) {
      deletedStorageFile = path.split('/').last;
    }
    return {};
  }
}

void main() {
  testWidgets('every society admin page renders without exception', (
    tester,
  ) async {
    tester.view.physicalSize = const Size(1440, 1000);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(
      MaterialApp(
        theme: ThemeData(useMaterial3: true),
        home: ShellView(
          dependencies: SocietyAdminDependencies(
            resources: AdminResourceUseCases(FakeService()),
          ),
          profile: const {'fullName': 'Test Admin'},
        ),
      ),
    );
    await tester.pumpAndSettle();
    for (final label in [
      'Dashboard',
      'Society profile',
      'Property spaces',
      'Residents',
      'Households',
      'Staff',
      'Configuration',
      'Billing',
      'Payments',
      'Complaints',
      'Visitors',
      'Amenities',
      'Activities & galleries',
      'Deliveries',
      'Emergency & accidents',
      'Notices',
      'Documents',
      'Staff tasks',
      'Sensitive activity',
      'Storage',
      'Platform support',
    ]) {
      await tester.ensureVisible(find.text(label).first);
      await tester.tap(find.text(label).first);
      await tester.pumpAndSettle();
      expect(
        tester.takeException(),
        isNull,
        reason: '$label rendered a red screen',
      );
    }
  });

  testWidgets('society admin can purchase storage and clean a file', (
    tester,
  ) async {
    tester.view.physicalSize = const Size(1100, 850);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    final service = FakeService();
    await tester.pumpWidget(
      MaterialApp(
        theme: SocietyTheme.light(),
        home: Scaffold(
          body: BlocProvider(
            create: (_) => StorageBloc(AdminResourceUseCases(service)),
            child: const StorageView(),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.textContaining('of 5.00 GB used'), findsOneWidget);
    expect(find.text('Annual meeting photo'), findsOneWidget);
    await tester.tap(find.byKey(const ValueKey('purchase-storage')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const ValueKey('confirm-storage-purchase')));
    await tester.pumpAndSettle();
    expect(service.purchasedStorageBlocks, 1);

    await tester.tap(find.byTooltip('Delete and free space'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Delete and free space'));
    await tester.pumpAndSettle();
    expect(service.deletedStorageFile, 'file-1');
    expect(tester.takeException(), isNull);
  });

  testWidgets('society admin can inspect who performed a sensitive action', (
    tester,
  ) async {
    tester.view.physicalSize = const Size(1000, 800);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    final service = FakeService();
    await tester.pumpWidget(
      MaterialApp(
        theme: SocietyTheme.light(),
        home: Scaffold(
          body: BlocProvider(
            create: (_) => AuditBloc(AdminResourceUseCases(service)),
            child: const AuditView(),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Update Permissions'), findsOneWidget);
    expect(find.textContaining('Society Admin'), findsOneWidget);
    expect(service.lastSensitiveOnly, isTrue);

    await tester.tap(find.text('Update Permissions'));
    await tester.pumpAndSettle();
    expect(find.text('Performed by'), findsOneWidget);
    expect(find.text('Society Admin'), findsWidgets);
    expect(find.text('Change type'), findsOneWidget);
    expect(find.text('Updated'), findsOneWidget);
    expect(find.textContaining('performed_by'), findsNothing);
    expect(find.textContaining('actor_user_id'), findsNothing);
    expect(tester.takeException(), isNull);
  });

  testWidgets('structure and staff views fit a narrow web viewport', (
    tester,
  ) async {
    tester.view.physicalSize = const Size(390, 844);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    final service = FakeService();

    await tester.pumpWidget(
      MaterialApp(
        theme: SocietyTheme.light(),
        home: Scaffold(
          body: BlocProvider(
            create: (_) => StructureBloc(AdminResourceUseCases(service)),
            child: const StructureView(),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();
    expect(tester.takeException(), isNull);
    expect(find.text('Property spaces'), findsOneWidget);

    await tester.pumpWidget(
      MaterialApp(
        theme: SocietyTheme.light(),
        home: Scaffold(
          body: BlocProvider(
            create: (_) => StaffBloc(AdminResourceUseCases(service)),
            child: const StaffView(),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();
    expect(tester.takeException(), isNull);
    expect(find.text('Staff directory'), findsOneWidget);
  });

  testWidgets('resident record exposes related operational tabs', (
    tester,
  ) async {
    tester.view.physicalSize = const Size(1440, 1000);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(
      MaterialApp(
        theme: SocietyTheme.light(),
        home: ShellView(
          dependencies: SocietyAdminDependencies(
            resources: AdminResourceUseCases(FakeService()),
          ),
          profile: const {'fullName': 'Test Admin'},
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.tap(find.text('Residents').first);
    await tester.pumpAndSettle();
    await tester.tap(find.text('Active residents'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Resident One'));
    await tester.pumpAndSettle();

    expect(find.text('Complaints'), findsWidgets);
    expect(find.text('Billing & payments'), findsOneWidget);
    expect(find.text('Visitors'), findsWidgets);
    expect(find.text('Household'), findsOneWidget);
    expect(find.text('Amenity bookings'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('support chat route retains SupportBloc', (tester) async {
    final repository = FakeService();
    await tester.pumpWidget(
      MaterialApp(
        theme: SocietyTheme.light(),
        home: BlocProvider(
          create: (_) => SupportBloc(AdminResourceUseCases(repository)),
          child: const Scaffold(body: SupportView()),
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.tap(find.text('Help'));
    await tester.pumpAndSettle();

    expect(find.byIcon(Icons.close), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('society admin can add and activate a resident', (tester) async {
    final repository = FakeService();
    await tester.pumpWidget(
      MaterialApp(
        theme: SocietyTheme.light(),
        home: BlocProvider(
          create: (_) => ResidentsBloc(AdminResourceUseCases(repository)),
          child: const Scaffold(body: ResidentsView()),
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.tap(find.text('Add resident').first);
    await tester.pumpAndSettle();

    await tester.enterText(
      find.widgetWithText(TextFormField, 'Full name'),
      'New Resident',
    );
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Mobile'),
      '9000012345',
    );
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Email'),
      'new.resident@example.com',
    );
    await tester.tap(find.text('Add resident').last);
    await tester.pumpAndSettle();

    expect(repository.createdResident?['flatId'], 'x1');
    expect(repository.createdResident?['residentType'], 'OWNER');
    expect(repository.createdResident?['email'], 'new.resident@example.com');
    expect(find.text('Resident added successfully'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });
}
