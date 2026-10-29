import 'package:flutter_test/flutter_test.dart';
import 'package:super_admin_app/features/audit/data/models/audit_page_model.dart';
import 'package:super_admin_app/features/billing/data/models/billing_dashboard_model.dart';
import 'package:super_admin_app/features/dashboard/data/models/dashboard_metrics_model.dart';
import 'package:super_admin_app/features/services/data/models/service_catalog_item_model.dart';
import 'package:super_admin_app/features/societies/data/models/society_models.dart';
import 'package:super_admin_app/features/storage/data/models/storage_dashboard_model.dart';

void main() {
  test('dashboard DTO maps API values', () {
    final model = DashboardMetricsModel.fromJson({'total_societies': 7});
    expect(model.values['total_societies'], 7);
  });
  test('service DTO exposes stable domain fields', () {
    final model = ServiceCatalogItemModel.fromJson({
      'code': 'VISITORS',
      'name': 'Visitors',
      'status': 'ACTIVE',
    });
    expect(model.code, 'VISITORS');
    expect(model.name, 'Visitors');
  });
  test('storage DTO maps policy and society usage', () {
    final model = StorageDashboardModel.fromJson({
      'policy': {'default_free_gb': 5},
      'societies': [
        {'society_name': 'Green View'},
      ],
    });
    expect(model.policy.data['default_free_gb'], 5);
    expect(model.societies.single.data['society_name'], 'Green View');
  });
  test('billing DTO separates orders and societies', () {
    final model = BillingDashboardModel.fromJson({
      'orders': [
        {'id': 'o1'},
      ],
      'societies': [
        {'id': 's1'},
      ],
    });
    expect(model.orders.single.data['id'], 'o1');
    expect(model.societies.single.data['id'], 's1');
  });
  test('society DTO exposes summary identity', () {
    final model = SocietySummaryModel.fromJson({
      'id': 's1',
      'name': 'Green View',
      'status': 'ACTIVE',
    });
    expect(model.id, 's1');
    expect(model.name, 'Green View');
  });
  test('society DTO combines nested profile with summary counts', () {
    final model = SocietySummaryModel.fromJson({
      'society': {
        'id': 's1',
        'name': 'Green View',
        'status': 'ACTIVE',
        'registration_number': 'REG-1',
        'city': 'Pune',
        'total_spaces': 10,
      },
      'configured_space_count': 8,
      'active_member_count': 5,
      'document_count': 2,
      'subscription_estimated_monthly': 1000,
    });
    expect(model.id, 's1');
    expect(model.name, 'Green View');
    expect(model.status, 'ACTIVE');
    expect(model.subtitle, 'REG-1 • Pune • 8/10 spaces • 5 members');
    expect(model.data['document_count'], 2);
    expect(model.data['subscription_estimated_monthly'], 1000);
  });
  test('audit DTO supports legacy and paged responses', () {
    final legacy = AuditPageModel.fromResponse([
      {'action': 'CREATED'},
    ]);
    final paged = AuditPageModel.fromResponse({
      'items': [],
      'page': 2,
      'size': 50,
      'total': 100,
      'total_pages': 2,
    });
    expect(legacy.items.single.action, 'CREATED');
    expect(paged.page, 2);
  });
}
