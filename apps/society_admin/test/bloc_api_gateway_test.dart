import 'package:flutter_test/flutter_test.dart';
import 'package:society_ui/society_ui.dart' show ApiClient;
import 'package:society_admin_app/core/domain/usecases/admin_resource_use_cases.dart';
import 'package:society_admin_app/core/presentation/adapters/bloc_api_gateway.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';

import 'support/fake_admin_repository.dart';

class _GatewayRepository extends FakeAdminRepository {
  @override
  Future<Map<String, dynamic>> map(String path) async => {'path': path};

  @override
  Future<Object?> post(String path, Map<String, dynamic> body) async => {
    'path': path,
    ...body,
  };
}

void main() {
  test('shared workspace requests are represented in BLoC state', () async {
    final bloc = AdminResourceBloc(AdminResourceUseCases(_GatewayRepository()));
    final gateway = BlocApiGateway(bloc);
    final ApiClient legacyCompatibleGateway = gateway;
    addTearDown(bloc.close);

    expect(await legacyCompatibleGateway.get('/api/example'), {
      'path': '/api/example',
    });
    expect(
      bloc.state.resource('query:/api/example').status,
      ResourceStatus.ready,
    );

    expect(await gateway.post('/api/example', {'name': 'Example'}), {
      'path': '/api/example',
      'name': 'Example',
    });
    expect(bloc.state.revision, 1);
    expect(bloc.state.mutating, isFalse);
  });
}
