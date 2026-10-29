import '../datasources/support_remote_data_source.dart';
import '../../domain/entities/support_models.dart';
import '../../domain/repositories/support_repository.dart';
import '../models/support_models.dart';

class SupportRepositoryImpl implements SupportRepository {
  const SupportRepositoryImpl(this.remote);
  final SupportRemoteDataSource remote;
  @override
  Future<List<SupportTicket>> loadTickets() async => (await remote.records(
    '/api/admin/platform/support',
  )).map(SupportTicketModel.fromJson).toList(growable: false);
  @override
  Future<List<SupportMessage>> loadMessages(String ticketId) async =>
      (await remote.records(
        '/api/support/$ticketId/messages',
      )).map(SupportMessageModel.fromJson).toList(growable: false);
  @override
  Future<void> sendMessage(String ticketId, String message) =>
      remote.sendMessage(ticketId, message);
  @override
  Future<void> resolveTicket(String id, String resolution) =>
      remote.resolveTicket(id, resolution);
}
