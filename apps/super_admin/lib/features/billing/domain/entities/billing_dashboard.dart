import '../../../../core/immutable_fields.dart';

class SubscriptionOrder {
  SubscriptionOrder(Map<String, dynamic> data)
    : data = ImmutableFields.from(data);
  final ImmutableFields data;
}

class BillableSociety {
  BillableSociety(Map<String, dynamic> data)
    : data = ImmutableFields.from(data);
  final ImmutableFields data;
}

class BillingDashboard {
  const BillingDashboard({
    required this.orders,
    required this.societies,
    this.gateway,
  });
  final List<SubscriptionOrder> orders;
  final List<BillableSociety> societies;
  final ImmutableFields? gateway;
}

class CreateSubscriptionOrderCommand {
  CreateSubscriptionOrderCommand(Map<String, dynamic> values)
    : values = ImmutableFields.from(values);
  final ImmutableFields values;
}
