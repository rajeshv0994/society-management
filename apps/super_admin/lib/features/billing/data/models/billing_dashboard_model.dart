import '../../domain/entities/billing_dashboard.dart';
import '../../../../core/immutable_fields.dart';

class BillingDashboardModel extends BillingDashboard {
  BillingDashboardModel.fromJson(Map<String, dynamic> json)
    : super(
        orders: (json['orders'] as List? ?? const [])
            .map(
              (item) => SubscriptionOrder(
                Map.unmodifiable(Map<String, dynamic>.from(item as Map)),
              ),
            )
            .toList(growable: false),
        societies: (json['societies'] as List? ?? const [])
            .map(
              (item) => BillableSociety(
                Map.unmodifiable(Map<String, dynamic>.from(item as Map)),
              ),
            )
            .toList(growable: false),
        gateway: ImmutableFields.from(
          Map<String, dynamic>.from(json['gateway'] as Map? ?? const {}),
        ),
      );
}
