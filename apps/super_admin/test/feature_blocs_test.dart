import 'package:flutter_test/flutter_test.dart';
import 'package:super_admin_app/features/billing/presentation/bloc/billing_bloc.dart';
import 'package:super_admin_app/features/billing/domain/usecases/billing_use_cases.dart';
import 'package:super_admin_app/features/services/domain/entities/service_catalog_item.dart';
import 'package:super_admin_app/features/services/domain/usecases/service_catalog_use_cases.dart';
import 'package:super_admin_app/features/services/presentation/bloc/service_catalog_bloc.dart';
import 'package:super_admin_app/features/storage/presentation/bloc/storage_bloc.dart';
import 'package:super_admin_app/features/storage/domain/entities/storage_dashboard.dart';
import 'package:super_admin_app/features/storage/domain/usecases/storage_use_cases.dart';

import 'support/fake_feature_repositories.dart';

class _OperationsRepository extends FakeFeatureRepositories {
  final services = <Map<String, dynamic>>[
    {'code': 'VISITORS', 'name': 'Visitors', 'status': 'ACTIVE'},
  ];
  bool failStorage = false;
  int catalogLoads = 0;

  @override
  Future<List<ServiceCatalogItem>> loadCatalog() async {
    catalogLoads++;
    return services
        .map((item) => ServiceCatalogItem(Map.unmodifiable(item)))
        .toList();
  }

  @override
  Future<void> saveService(SaveServiceCommand command) async {
    services.add(Map<String, dynamic>.from(command.values));
  }

  @override
  Future<StorageDashboard> loadStorage() async {
    if (failStorage) throw Exception('Storage unavailable');
    return StorageDashboard(policy: StoragePolicy({}), societies: []);
  }

  @override
  Future<List<int>> downloadInvoice(String orderId) async => [37, 80, 68, 70];
}

void main() {
  test('service feature loads through its own BLoC', () async {
    final repository = _OperationsRepository();
    final bloc = ServiceCatalogBloc(ServiceCatalogUseCases(repository));
    addTearDown(bloc.close);

    final ready = bloc.stream.firstWhere(
      (state) => state.status == ServiceCatalogStatus.ready,
    );
    bloc.add(const ServiceCatalogLoaded());

    final state = await ready;
    expect(state.data.single.code, 'VISITORS');
    expect(repository.catalogLoads, 1);
  });

  test('service mutation reloads only the service feature', () async {
    final repository = _OperationsRepository();
    final bloc = ServiceCatalogBloc(ServiceCatalogUseCases(repository));
    addTearDown(bloc.close);
    bloc.add(const ServiceCatalogLoaded());
    await bloc.stream.firstWhere(
      (state) => state.status == ServiceCatalogStatus.ready,
    );

    final completed = bloc.stream.firstWhere((state) => state.revision == 1);
    bloc.add(
      const ServiceCatalogSaved({'code': 'PAYMENTS', 'name': 'Payments'}),
    );

    expect((await completed).data.length, 2);
    expect(repository.catalogLoads, 2);
  });

  test('storage failures become explicit feature failure state', () async {
    final repository = _OperationsRepository()..failStorage = true;
    final bloc = StorageBloc(StorageUseCases(repository));
    addTearDown(bloc.close);
    final failed = bloc.stream.firstWhere(
      (state) => state.status == StorageStatus.failure,
    );
    bloc.add(const StorageLoaded());
    expect((await failed).error, contains('Storage unavailable'));
  });

  test('billing BLoC delivers invoice bytes', () async {
    final bloc = BillingBloc(BillingUseCases(_OperationsRepository()));
    addTearDown(bloc.close);
    bloc.add(const BillingLoaded());
    await bloc.stream.firstWhere(
      (state) => state.status == BillingStatus.ready,
    );
    final completed = bloc.stream.firstWhere(
      (state) => state.completedMutation == BillingMutation.downloadInvoice,
    );
    bloc.add(const SubscriptionInvoiceRequested('order-1'));
    expect((await completed).result, [37, 80, 68, 70]);
  });
}
