// ignore_for_file: use_super_parameters
import '../../../../core/domain/usecases/admin_resource_use_cases.dart';
import '../../../../core/domain/entities/admin_resource.dart';
import '../../../../core/presentation/bloc/admin_resource_bloc.dart';

class AuditBloc extends AdminResourceBloc {
  AuditBloc(AdminResourceUseCases useCases) : super(useCases);

  void loadAudit({
    required int page,
    required bool sensitiveOnly,
    String search = '',
  }) {
    final query = <String, String>{
      'page': '$page',
      'size': '50',
      'sensitiveOnly': '$sensitiveOnly',
      if (search.trim().isNotEmpty) 'search': search.trim(),
    };
    load(
      'audit',
      ResourceRequest(AdminTargets.audit(query), ResourceKind.document),
    );
  }
}
