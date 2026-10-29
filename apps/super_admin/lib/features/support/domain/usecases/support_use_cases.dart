import '../entities/support_models.dart';
import '../repositories/support_repository.dart';

class SupportUseCases {
  const SupportUseCases(this.repository);
  final SupportRepository repository;
  Future<List<SupportTicket>> loadTickets() => repository.loadTickets();
  Future<List<SupportMessage>> loadMessages(String id) =>
      repository.loadMessages(id);
  Future<void> sendMessage(String id, String message) =>
      repository.sendMessage(id, message);
  Future<void> resolveTicket(String id, String resolution) =>
      repository.resolveTicket(id, resolution);
}
