import '../datasources/billing_remote_data_source.dart';
import '../../domain/entities/billing_dashboard.dart';
import '../../domain/repositories/billing_repository.dart';
import '../models/billing_dashboard_model.dart';

class BillingRepositoryImpl implements BillingRepository {
  const BillingRepositoryImpl(this.remote);
  final BillingRemoteDataSource remote;
  @override
  Future<BillingDashboard> loadBilling() async =>
      BillingDashboardModel.fromJson(await remote.load());
  @override
  Future<void> createOrder(CreateSubscriptionOrderCommand command) =>
      remote.createOrder(command.values);
  @override
  Future<List<int>> downloadInvoice(String orderId) => remote.invoice(orderId);
  @override
  Future<void> reconcilePayment(String orderId) => remote.reconcile(orderId);
  @override
  Future<void> cancelOrder(String orderId, String reason) =>
      remote.cancel(orderId, reason);
}
