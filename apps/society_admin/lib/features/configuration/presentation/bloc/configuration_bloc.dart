// ignore_for_file: use_super_parameters
import '../../../../core/domain/usecases/admin_resource_use_cases.dart';
import '../../../../core/domain/entities/admin_resource.dart';
import '../../../../core/presentation/bloc/admin_resource_bloc.dart';

enum ConfigurationResource {
  parking('parking'),
  vendor('vendors'),
  complaintCategory('complaint-categories'),
  chargeRule('charge-rules');

  const ConfigurationResource(this.segment);
  final String segment;
}

class ConfigurationBloc extends AdminResourceBloc {
  ConfigurationBloc(AdminResourceUseCases useCases) : super(useCases);

  void loadConfiguration() => load(
    'configuration',
    const ResourceRequest(AdminTargets.configuration, ResourceKind.document),
  );

  Future<Object?> completeSetup() => execute(
    ResourceCommand(ResourceMethod.post, AdminTargets.configurationComplete),
  );

  Future<Object?> saveResource(
    ConfigurationResource resource,
    Map<String, dynamic> body, {
    Object? id,
  }) => execute(
    ResourceCommand(
      id == null ? ResourceMethod.post : ResourceMethod.put,
      AdminTargets.configurationResource(resource.segment, id),
      body: body,
    ),
  );

  Future<Object?> updateResource(
    ConfigurationResource resource,
    Object id,
    Map<String, dynamic> body,
  ) => execute(
    ResourceCommand(
      ResourceMethod.patch,
      AdminTargets.configurationResource(resource.segment, id),
      body: body,
    ),
  );
}
