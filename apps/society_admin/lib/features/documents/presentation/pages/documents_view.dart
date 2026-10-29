// ignore_for_file: use_build_context_synchronously

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'package:society_admin_app/core/presentation/widgets/common.dart';
import '../bloc/documents_bloc.dart';

class DocumentsView extends StatefulWidget {
  const DocumentsView({super.key});

  @override
  State<DocumentsView> createState() => _DocumentsViewState();
}

class _DocumentsViewState extends State<DocumentsView> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => load());
  }

  void load() => context.read<DocumentsBloc>().loadDocuments();

  Future<void> upload() async {
    String type = 'REGISTRATION_CERTIFICATE';
    final selected = await showDialog<String>(
      context: context,
      builder: (dialog) => StatefulBuilder(
        builder: (context, setDialog) => AlertDialog(
          title: const Text('Upload society document'),
          content: DropdownButtonFormField<String>(
            initialValue: type,
            decoration: const InputDecoration(labelText: 'Document type'),
            items:
                const [
                      'REGISTRATION_CERTIFICATE',
                      'GST',
                      'PAN',
                      'ADDRESS_PROOF',
                      'OTHER',
                    ]
                    .map(
                      (value) => DropdownMenuItem(
                        value: value,
                        child: Text(value.replaceAll('_', ' ')),
                      ),
                    )
                    .toList(),
            onChanged: (value) => setDialog(() => type = value!),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialog),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(dialog, type),
              child: const Text('Choose file'),
            ),
          ],
        ),
      ),
    );
    if (selected == null) return;
    final picked = await FilePicker.platform.pickFiles(withData: true);
    final file = picked?.files.single;
    if (file?.bytes == null) return;
    try {
      await context.read<DocumentsBloc>().uploadDocument(
        type: selected,
        bytes: file!.bytes!,
        filename: file.name,
      );
      load();
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(error.toString())));
      }
    }
  }

  @override
  Widget build(BuildContext context) => PageFrame(
    title: 'Documents',
    subtitle: 'Upload and track society verification documents',
    action: FilledButton.icon(
      onPressed: upload,
      icon: const Icon(Icons.upload_file),
      label: const Text('Upload document'),
    ),
    child: BlocBuilder<DocumentsBloc, AdminResourceState>(
      builder: (context, state) {
        final snapshot = state.resource('documents');
        if (snapshot.status == ResourceStatus.initial ||
            snapshot.status == ResourceStatus.loading) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.error != null) {
          return ErrorState(message: snapshot.error!, onRetry: load);
        }
        final rows =
            (snapshot.value as ResourceCollection?)?.items ??
            const <Map<String, dynamic>>[];
        if (rows.isEmpty) return const Empty('No documents uploaded.');
        return ListView.separated(
          itemCount: rows.length,
          separatorBuilder: (_, _) => const SizedBox(height: 8),
          itemBuilder: (context, index) {
            final document = rows[index];
            return Card(
              child: ListTile(
                leading: const Icon(Icons.description_outlined),
                title: Text(
                  document['document_type'].toString().replaceAll('_', ' '),
                ),
                subtitle: Text(document['storage_key'].toString()),
                trailing: StatusChip(
                  document['verification_status'].toString(),
                ),
              ),
            );
          },
        );
      },
    ),
  );
}
