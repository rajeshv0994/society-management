// ignore_for_file: use_super_parameters
import '../../../../core/domain/usecases/admin_resource_use_cases.dart';
import '../../../../core/domain/entities/admin_resource.dart';
import '../../../../core/presentation/bloc/admin_resource_bloc.dart';

class SupportBloc extends AdminResourceBloc {
  SupportBloc(AdminResourceUseCases useCases) : super(useCases);

  void loadTickets() => load(
    'tickets',
    const ResourceRequest(AdminTargets.support, ResourceKind.collection),
  );

  Future<Object?> createTicket(String subject, String description) => execute(
    ResourceCommand(
      ResourceMethod.post,
      AdminTargets.support,
      body: {
        'subject': subject,
        'description': description,
        'priority': 'NORMAL',
      },
    ),
  );

  String messagesKey(Object ticketId) => 'messages-$ticketId';

  void loadMessages(Object ticketId) => load(
    messagesKey(ticketId),
    ResourceRequest(
      AdminTargets.supportMessages(ticketId),
      ResourceKind.collection,
    ),
  );

  Future<Object?> sendMessage(Object ticketId, String message) => execute(
    ResourceCommand(
      ResourceMethod.post,
      AdminTargets.supportMessages(ticketId),
      body: {'message': message},
    ),
  );
}
