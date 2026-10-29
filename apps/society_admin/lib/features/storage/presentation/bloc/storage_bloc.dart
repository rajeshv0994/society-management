// ignore_for_file: use_super_parameters
import '../../../../core/domain/usecases/admin_resource_use_cases.dart';
import '../../../../core/domain/entities/admin_resource.dart';
import '../../../../core/presentation/bloc/admin_resource_bloc.dart';

class StorageBloc extends AdminResourceBloc {
  StorageBloc(AdminResourceUseCases useCases) : super(useCases);

  void loadStorage() => load(
    'storage',
    const ResourceRequest(AdminTargets.storage, ResourceKind.document),
  );

  Future<Object?> purchaseStorage(int blocks) => execute(
    ResourceCommand(
      ResourceMethod.post,
      AdminTargets.storagePurchases,
      body: {'blocks': blocks},
    ),
  );

  Future<Object?> deleteFile(Object id) => execute(
    ResourceCommand(ResourceMethod.delete, AdminTargets.storageFile(id)),
  );
}
