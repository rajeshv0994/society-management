import '../entities/billing_dashboard.dart';
import '../repositories/billing_repository.dart';

class BillingUseCases {
  const BillingUseCases(this.repository);
  final BillingRepository repository;
  Future<BillingDashboard> load() => repository.loadBilling();
  Future<void> createOrder(CreateSubscriptionOrderCommand command) =>
      repository.createOrder(command);
  Future<List<int>> downloadInvoice(String id) =>
      repository.downloadInvoice(id);
  Future<void> reconcilePayment(String id) => repository.reconcilePayment(id);
  Future<void> cancelOrder(String id, String reason) =>
      repository.cancelOrder(id, reason);
}
