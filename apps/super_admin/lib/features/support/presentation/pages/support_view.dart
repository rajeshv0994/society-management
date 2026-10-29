import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:super_admin_app/core/presentation/widgets/common_widgets.dart';
import '../bloc/support_bloc.dart';

class SupportView extends StatelessWidget {
  const SupportView({super.key});
  @override
  Widget build(c) => PageFrame(
    title: 'Support centre',
    subtitle: 'Open a ticket to chat with the society in real time',
    onRefresh: () => c.read<SupportBloc>().add(const SupportLoaded()),
    child: BlocBuilder<SupportBloc, SupportState>(
      builder: (context, state) {
        if (state is SupportLoading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (state is SupportFailure) {
          return FailureState(
            state.message,
            retry: () => context.read<SupportBloc>().add(const SupportLoaded()),
          );
        }
        final ready = state as SupportReady;
        return LayoutBuilder(
          builder: (context, size) {
            final wide = size.maxWidth > 850;
            final list = _TicketList(state: ready);
            final chat = _ChatPanel(state: ready);
            return wide
                ? Row(
                    children: [
                      SizedBox(width: 360, child: list),
                      const VerticalDivider(width: 1),
                      Expanded(child: chat),
                    ],
                  )
                : ready.selected == null
                ? list
                : chat;
          },
        );
      },
    ),
  );
}

class _TicketList extends StatelessWidget {
  const _TicketList({required this.state});
  final SupportReady state;
  @override
  Widget build(c) {
    if (state.tickets.isEmpty) return const EmptyState('No support tickets.');
    return ListView.separated(
      padding: const EdgeInsets.only(right: 12),
      itemCount: state.tickets.length,
      separatorBuilder: (_, _) => const SizedBox(height: 6),
      itemBuilder: (context, index) {
        final ticket = state.tickets[index],
            d = ticket.data,
            selected = state.selected?.data['id'] == d['id'];
        return Card(
          color: selected
              ? Theme.of(context).colorScheme.primaryContainer
              : null,
          child: ListTile(
            selected: selected,
            onTap: () => context.read<SupportBloc>().add(TicketOpened(ticket)),
            leading: CircleAvatar(
              child: Text((d['society_name'] ?? 'S').toString()[0]),
            ),
            title: Text(d['subject'].toString()),
            subtitle: Text('${d['society_name']} • ${d['priority']}'),
            trailing: StatusChip(d['status'].toString()),
          ),
        );
      },
    );
  }
}

class _ChatPanel extends StatefulWidget {
  const _ChatPanel({required this.state});
  final SupportReady state;
  @override
  State<_ChatPanel> createState() => _ChatPanelState();
}

class _ChatPanelState extends State<_ChatPanel> {
  final message = TextEditingController();
  void send() {
    if (message.text.trim().isEmpty) return;
    context.read<SupportBloc>().add(MessageSent(message.text));
    message.clear();
  }

  Future<void> resolve() async {
    final note = TextEditingController();
    final ok = await showDialog<bool>(
      context: context,
      builder: (dialog) => AlertDialog(
        title: const Text('Resolve ticket'),
        content: TextField(
          controller: note,
          maxLines: 3,
          decoration: const InputDecoration(labelText: 'Resolution summary'),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialog, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialog, true),
            child: const Text('Resolve'),
          ),
        ],
      ),
    );
    if (ok == true && mounted) {
      context.read<SupportBloc>().add(TicketResolved(note.text));
    }
  }

  @override
  Widget build(c) {
    final selected = widget.state.selected;
    if (selected == null) {
      return const EmptyState('Select a support ticket to open chat.');
    }
    final ticket = selected.data;
    return Column(
      children: [
        Container(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              IconButton(
                onPressed: () =>
                    context.read<SupportBloc>().add(const SupportLoaded()),
                icon: const Icon(Icons.arrow_back),
              ),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      ticket['subject'].toString(),
                      style: Theme.of(c).textTheme.titleLarge,
                    ),
                    Text(
                      '${ticket['society_name']} • ${ticket['created_by_name']}',
                    ),
                  ],
                ),
              ),
              StatusChip(ticket['status'].toString()),
              const SizedBox(width: 8),
              if (!['RESOLVED', 'CLOSED'].contains(ticket['status']))
                OutlinedButton.icon(
                  onPressed: resolve,
                  icon: const Icon(Icons.check),
                  label: const Text('Resolve'),
                ),
            ],
          ),
        ),
        const Divider(height: 1),
        Expanded(
          child: widget.state.messages.isEmpty
              ? const EmptyState('No messages yet.')
              : ListView.builder(
                  reverse: true,
                  padding: const EdgeInsets.all(16),
                  itemCount: widget.state.messages.length,
                  itemBuilder: (context, index) {
                    final d = widget.state.messages.reversed
                            .toList()[index]
                            .data,
                        isAdmin = d['sender_role'] == 'SUPER_ADMIN';
                    return Align(
                      alignment: isAdmin
                          ? Alignment.centerRight
                          : Alignment.centerLeft,
                      child: Container(
                        constraints: const BoxConstraints(maxWidth: 520),
                        margin: const EdgeInsets.only(bottom: 10),
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: isAdmin
                              ? Theme.of(context).colorScheme.primaryContainer
                              : Colors.white,
                          borderRadius: BorderRadius.circular(14),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              d['sender_name'].toString(),
                              style: Theme.of(context).textTheme.labelMedium,
                            ),
                            const SizedBox(height: 3),
                            Text(d['message'].toString()),
                            const SizedBox(height: 4),
                            Text(
                              d['created_at'].toString(),
                              style: Theme.of(context).textTheme.labelSmall,
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
        ),
        if (!['RESOLVED', 'CLOSED'].contains(ticket['status']))
          Padding(
            padding: const EdgeInsets.all(12),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: message,
                    minLines: 1,
                    maxLines: 4,
                    onSubmitted: (_) => send(),
                    decoration: const InputDecoration(
                      hintText: 'Type a reply…',
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                IconButton.filled(
                  onPressed: widget.state.sending ? null : send,
                  icon: const Icon(Icons.send),
                ),
              ],
            ),
          ),
      ],
    );
  }
}
