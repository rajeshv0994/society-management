// ignore_for_file: use_super_parameters
import '../../../../core/domain/usecases/admin_resource_use_cases.dart';
import '../../../../core/domain/entities/admin_resource.dart';
import '../../../../core/presentation/bloc/admin_resource_bloc.dart';

class ProfileBloc extends AdminResourceBloc {
  ProfileBloc(AdminResourceUseCases useCases) : super(useCases);

  void loadProfile() => load(
    'profile',
    const ResourceRequest(AdminTargets.profile, ResourceKind.document),
  );

  void loadShellProfile() => load(
    'shell-profile',
    const ResourceRequest(AdminTargets.profile, ResourceKind.document),
  );

  void loadSubscription() => load(
    'subscription',
    const ResourceRequest(AdminTargets.subscription, ResourceKind.document),
  );

  Future<Object?> updateProfile(Map<String, dynamic> data) => execute(
    ResourceCommand(ResourceMethod.put, AdminTargets.profile, body: data),
  );

  Future<Object?> updateSubscription(Map<String, dynamic> data) => execute(
    ResourceCommand(
      ResourceMethod.put,
      AdminTargets.subscriptionQuantities,
      body: data,
    ),
  );

  Future<Object?> uploadLogo(List<int> bytes, String filename) => execute(
    ResourceCommand(
      ResourceMethod.uploadLogo,
      AdminTargets.none,
      bytes: bytes,
      filename: filename,
    ),
  );
}
