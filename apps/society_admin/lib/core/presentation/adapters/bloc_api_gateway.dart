import 'package:society_ui/society_ui.dart'
    show ApiClient, BlocManagedApiGateway;

import '../../domain/entities/admin_resource.dart';
import '../bloc/admin_resource_bloc.dart';

/// Presentation-port adapter for shared Society UI workspaces.
///
/// Shared widgets remain independent of this application's state-management
/// package. Every request is represented by immutable operation state in the
/// owning feature BLoC, and this adapter awaits that state transition.
class BlocApiGateway extends ApiClient implements BlocManagedApiGateway {
  const BlocApiGateway(this.bloc) : super();

  final AdminResourceBloc bloc;

  @override
  String get baseUrl => bloc.absoluteUrl('');

  @override
  Future<dynamic> get(String path) => bloc.query(AdminTargets.external(path));

  @override
  Future<dynamic> post(String path, [Object? body]) =>
      _execute(ResourceMethod.post, path, body);

  @override
  Future<dynamic> put(String path, [Object? body]) =>
      _execute(ResourceMethod.put, path, body);

  @override
  Future<dynamic> patch(String path, [Object? body]) =>
      _execute(ResourceMethod.patch, path, body);

  @override
  Future<dynamic> delete(String path) => bloc.execute(
    ResourceCommand(ResourceMethod.delete, AdminTargets.external(path)),
  );

  @override
  Future<List<int>> getBytes(String path) async =>
      (await bloc.execute(
            ResourceCommand(
              ResourceMethod.download,
              AdminTargets.external(path),
            ),
          ))
          as List<int>;

  @override
  Future<dynamic> upload(
    String path, {
    required List<int> bytes,
    required String filename,
    String field = 'file',
  }) => bloc.execute(
    ResourceCommand(
      ResourceMethod.upload,
      AdminTargets.external(path),
      bytes: bytes,
      filename: filename,
      field: field,
    ),
  );

  Future<Object?> _execute(ResourceMethod method, String path, Object? body) =>
      bloc.execute(
        ResourceCommand(
          method,
          AdminTargets.external(path),
          body: _mapBody(body),
        ),
      );

  Map<String, dynamic> _mapBody(Object? body) {
    if (body == null) return const {};
    if (body is Map) return Map<String, dynamic>.from(body);
    throw ArgumentError.value(body, 'body', 'Expected a JSON object');
  }
}
