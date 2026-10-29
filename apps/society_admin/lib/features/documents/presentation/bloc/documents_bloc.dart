// ignore_for_file: use_super_parameters
import '../../../../core/domain/usecases/admin_resource_use_cases.dart';
import '../../../../core/domain/entities/admin_resource.dart';
import '../../../../core/presentation/bloc/admin_resource_bloc.dart';

class DocumentsBloc extends AdminResourceBloc {
  DocumentsBloc(AdminResourceUseCases useCases) : super(useCases);

  void loadDocuments() => load(
    'documents',
    const ResourceRequest(AdminTargets.documents, ResourceKind.collection),
  );

  Future<Object?> uploadDocument({
    required String type,
    required List<int> bytes,
    required String filename,
  }) => execute(
    ResourceCommand(
      ResourceMethod.uploadDocument,
      AdminTargets.none,
      documentType: type,
      bytes: bytes,
      filename: filename,
    ),
  );
}
