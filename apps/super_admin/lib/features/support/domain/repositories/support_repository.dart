import '../entities/support_models.dart';

abstract interface class SupportRepository {
  Future<List<SupportTicket>> loadTickets();
  Future<List<SupportMessage>> loadMessages(String ticketId);
  Future<void> sendMessage(String ticketId, String message);
  Future<void> resolveTicket(String id, String resolution);
}
