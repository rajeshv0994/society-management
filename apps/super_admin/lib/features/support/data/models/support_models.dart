import '../../domain/entities/support_models.dart';

class SupportTicketModel extends SupportTicket {
  SupportTicketModel(super.data);
  factory SupportTicketModel.fromJson(Map<String, dynamic> json) =>
      SupportTicketModel(Map.unmodifiable(json));
}

class SupportMessageModel extends SupportMessage {
  SupportMessageModel(super.data);
  factory SupportMessageModel.fromJson(Map<String, dynamic> json) =>
      SupportMessageModel(Map.unmodifiable(json));
}
