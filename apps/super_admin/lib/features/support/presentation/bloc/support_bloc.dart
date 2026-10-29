import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/support_models.dart';
import '../../domain/usecases/support_use_cases.dart';

sealed class SupportEvent {
  const SupportEvent();
}

class SupportLoaded extends SupportEvent {
  const SupportLoaded();
}

class TicketOpened extends SupportEvent {
  const TicketOpened(this.ticket);
  final SupportTicket ticket;
}

class MessageSent extends SupportEvent {
  const MessageSent(this.message);
  final String message;
}

class TicketResolved extends SupportEvent {
  const TicketResolved(this.resolution);
  final String resolution;
}

sealed class SupportState {
  const SupportState();
}

class SupportLoading extends SupportState {
  const SupportLoading();
}

class SupportReady extends SupportState {
  const SupportReady(
    this.tickets, {
    this.selected,
    this.messages = const [],
    this.sending = false,
  });
  final List<SupportTicket> tickets;
  final SupportTicket? selected;
  final List<SupportMessage> messages;
  final bool sending;
}

class SupportFailure extends SupportState {
  const SupportFailure(this.message);
  final String message;
}

class SupportBloc extends Bloc<SupportEvent, SupportState> {
  SupportBloc(this.useCases) : super(const SupportLoading()) {
    on<SupportLoaded>((event, emit) async {
      emit(const SupportLoading());
      try {
        emit(SupportReady(await useCases.loadTickets()));
      } catch (error) {
        emit(SupportFailure(error.toString()));
      }
    });
    on<TicketOpened>((event, emit) async {
      final tickets = state is SupportReady
          ? (state as SupportReady).tickets
          : await useCases.loadTickets();
      emit(SupportReady(tickets, selected: event.ticket));
      try {
        emit(
          SupportReady(
            tickets,
            selected: event.ticket,
            messages: await useCases.loadMessages(event.ticket.id),
          ),
        );
      } catch (error) {
        emit(SupportFailure(error.toString()));
      }
    });
    on<MessageSent>((event, emit) async {
      if (state is! SupportReady) return;
      final current = state as SupportReady;
      if (current.selected == null || event.message.trim().isEmpty) return;
      emit(
        SupportReady(
          current.tickets,
          selected: current.selected,
          messages: current.messages,
          sending: true,
        ),
      );
      try {
        await useCases.sendMessage(current.selected!.id, event.message.trim());
        emit(
          SupportReady(
            current.tickets,
            selected: current.selected,
            messages: await useCases.loadMessages(current.selected!.id),
          ),
        );
      } catch (error) {
        emit(SupportFailure(error.toString()));
      }
    });
    on<TicketResolved>((event, emit) async {
      if (state is! SupportReady) return;
      final current = state as SupportReady;
      if (current.selected == null) return;
      await useCases.resolveTicket(current.selected!.id, event.resolution);
      final tickets = await useCases.loadTickets();
      SupportTicket? selected;
      for (final ticket in tickets) {
        if (ticket.data['id'] == current.selected!.data['id']) {
          selected = ticket;
        }
      }
      emit(
        SupportReady(tickets, selected: selected, messages: current.messages),
      );
    });
  }
  final SupportUseCases useCases;
}
