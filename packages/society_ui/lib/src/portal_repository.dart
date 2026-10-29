import 'api_client.dart';
import 'portal_models.dart';

class PortalRepository {
  const PortalRepository(this.publicApi);

  final ApiGateway publicApi;

  Future<List<SocietyOption>> loadSocieties() async {
    final response = await publicApi.get('/api/public/societies') as List;
    return List.unmodifiable(
      response.map(
        (value) =>
            SocietyOption.fromJson(Map<String, dynamic>.from(value as Map)),
      ),
    );
  }

  Future<PortalAccess> loadAccess(
    PortalRole role,
    ApiGateway authenticatedApi,
  ) async {
    Set<String> permissions = const {};
    Set<String>? enabledServices;

    final reference = Map<String, dynamic>.from(
      await authenticatedApi.get('/api/configuration/reference') as Map,
    );
    enabledServices = Set.unmodifiable(
      (reference['enabled_modules'] as List? ?? const []).map(
        (value) => value.toString(),
      ),
    );

    if (role == PortalRole.staff) {
      final profile = Map<String, dynamic>.from(
        await authenticatedApi.get('/api/society/staff/me') as Map,
      );
      permissions = Set.unmodifiable(
        (profile['permissions'] as List? ?? const []).map(
          (value) => value.toString(),
        ),
      );
    }

    return PortalAccess(
      permissions: permissions,
      enabledServices: enabledServices,
    );
  }

  Future<Object?> loadModule(ApiGateway api, PortalModuleResource resource) =>
      api.get(switch (resource) {
        PortalModuleResource.societyDashboard => '/api/dashboard/society',
      });
}
