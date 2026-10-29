import '../entities/billing_dashboard.dart';

abstract interface class BillingRepository {
  Future<BillingDashboard> loadBilling();
  Future<void> createOrder(CreateSubscriptionOrderCommand command);
  Future<List<int>> downloadInvoice(String orderId);
  Future<void> reconcilePayment(String orderId);
  Future<void> cancelOrder(String orderId, String reason);
}
