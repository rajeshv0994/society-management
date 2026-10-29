// ignore_for_file: use_build_context_synchronously

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'package:society_admin_app/core/presentation/widgets/common.dart';
import '../bloc/support_bloc.dart';

class SupportView extends StatefulWidget {
  const SupportView({super.key});
  @override
  State<SupportView> createState() => _SupportState();
}

class _SupportState extends State<SupportView> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => load());
  }

  void load() => context.read<SupportBloc>().loadTickets();

  Future<void> create() async {
    final a = TextEditingController(), b = TextEditingController();
    final ok = await showDialog<bool>(
      context: context,
      builder: (d) => AlertDialog(
        title: const Text('Contact platform support'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: a,
              decoration: const InputDecoration(labelText: 'Subject'),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: b,
              maxLines: 4,
              decoration: const InputDecoration(labelText: 'Message'),
            ),
          ],
        ),
        actions: [
          FilledButton(
            onPressed: () => Navigator.pop(d, true),
            child: const Text('Create ticket'),
          ),
        ],
      ),
    );
    if (ok == true) {
      await context.read<SupportBloc>().createTicket(a.text, b.text);
      load();
    }
  }

  Future<void> chat(Map ticket) async {
    final bloc = context.read<SupportBloc>();
    await showDialog(
      context: context,
      builder: (_) => BlocProvider.value(
        value: bloc,
        child: _Chat(ticket: ticket),
      ),
    );
    load();
  }

  @override
  Widget build(c) => PageFrame(
    title: 'Platform support',
    subtitle: 'Create tickets and chat with Super Admin',
    action: FilledButton.icon(
      onPressed: create,
      icon: const Icon(Icons.add),
      label: const Text('New ticket'),
    ),
    child: BlocBuilder<SupportBloc, AdminResourceState>(
      builder: (c, state) {
        final s = state.resource('tickets');
        if (s.status == ResourceStatus.initial ||
            s.status == ResourceStatus.loading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (s.error != null) {
          return ErrorState(message: s.error!, onRetry: load);
        }
        final rows =
            (s.value as ResourceCollection?)?.items ??
            const <Map<String, dynamic>>[];
        if (rows.isEmpty) return const Empty('No support tickets.');
        return ListView.builder(
          itemCount: rows.length,
          itemBuilder: (c, i) {
            final r = rows[i];
            return Card(
              child: ListTile(
                onTap: () => chat(r),
                title: Text(r['subject'].toString()),
                subtitle: Text(
                  '${r['message_count']} messages • ${r['priority']}',
                ),
                trailing: StatusChip(r['status'].toString()),
              ),
            );
          },
        );
      },
    ),
  );
}

class _Chat extends StatefulWidget {
  const _Chat({required this.ticket});
  final Map ticket;
  @override
  State<_Chat> createState() => _ChatState();
}

class _ChatState extends State<_Chat> {
  final message = TextEditingController();
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => load());
  }

  String get keyName =>
      context.read<SupportBloc>().messagesKey(widget.ticket['id']);
  void load() => context.read<SupportBloc>().loadMessages(widget.ticket['id']);

  Future<void> send() async {
    if (message.text.isEmpty) return;
    await context.read<SupportBloc>().sendMessage(
      widget.ticket['id'],
      message.text,
    );
    message.clear();
    load();
  }

  @override
  Widget build(c) => Dialog.fullscreen(
    child: Scaffold(
      appBar: AppBar(
        leading: IconButton(
          onPressed: () => Navigator.pop(c),
          icon: const Icon(Icons.close),
        ),
        title: Text(widget.ticket['subject'].toString()),
      ),
      body: Column(
        children: [
          Expanded(
            child: BlocBuilder<SupportBloc, AdminResourceState>(
              builder: (c, state) {
                final s = state.resource(keyName);
                if (s.status == ResourceStatus.initial ||
                    s.status == ResourceStatus.loading) {
                  return const Center(child: CircularProgressIndicator());
                }
                if (s.error != null) {
                  return ErrorState(message: s.error!, onRetry: load);
                }
                final rows =
                    (s.value as ResourceCollection?)?.items ??
                    const <Map<String, dynamic>>[];
                return ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: rows.length,
                  itemBuilder: (c, i) {
                    final r = rows[i],
                        mine = r['sender_role'] == 'SOCIETY_ADMIN';
                    return Align(
                      alignment: mine
                          ? Alignment.centerRight
                          : Alignment.centerLeft,
                      child: Card(
                        color: mine
                            ? Theme.of(c).colorScheme.primaryContainer
                            : null,
                        child: Padding(
                          padding: const EdgeInsets.all(12),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                r['sender_name'].toString(),
                                style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              Text(r['message'].toString()),
                            ],
                          ),
                        ),
                      ),
                    );
                  },
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(12),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: message,
                    decoration: const InputDecoration(
                      hintText: 'Type message…',
                    ),
                  ),
                ),
                IconButton.filled(
                  onPressed: send,
                  icon: const Icon(Icons.send),
                ),
              ],
            ),
          ),
        ],
      ),
    ),
  );
}
