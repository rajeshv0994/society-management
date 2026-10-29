import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/data/datasources/society_admin_remote_data_source.dart';
import 'package:society_admin_app/core/domain/repositories/admin_resource_repository.dart';

class FakeAdminRepository implements AdminResourceRepository {
  Future<Map<String, dynamic>> map(String path) async => {};
  Future<List<Map<String, dynamic>>> list(String path) async => [];
  Future<Object?> post(String path, Map<String, dynamic> body) async => {};
  Future<Object?> put(String path, Map<String, dynamic> body) async => {};
  Future<Object?> patch(String path, Map<String, dynamic> body) async => {};
  Future<Object?> delete(String path) async => {};
  Future<Object?> uploadLogo(List<int> bytes, String filename) async => {};
  Future<Object?> uploadVisitorPhoto(
    String id,
    List<int> bytes,
    String filename,
  ) async => {};
  Future<Object?> uploadDocument(
    String type,
    List<int> bytes,
    String filename,
  ) async => {};
  @override
  Future<AdminResource> load(ResourceRequest request) async =>
      switch (request.kind) {
        ResourceKind.document => ResourceDocument(
          AdminRecord.from(
            await map(SocietyAdminRemoteDataSource.pathFor(request.target)),
          ),
        ),
        ResourceKind.collection => ResourceCollection(
          (await list(
            SocietyAdminRemoteDataSource.pathFor(request.target),
          )).map(AdminRecord.from).toList(),
        ),
        ResourceKind.raw => RawResource(
          await map(SocietyAdminRemoteDataSource.pathFor(request.target)),
        ),
      };
  @override
  Future<Object?> execute(ResourceCommand command) => switch (command.method) {
    ResourceMethod.post => post(
      SocietyAdminRemoteDataSource.pathFor(command.target),
      command.body,
    ),
    ResourceMethod.put => put(
      SocietyAdminRemoteDataSource.pathFor(command.target),
      command.body,
    ),
    ResourceMethod.patch => patch(
      SocietyAdminRemoteDataSource.pathFor(command.target),
      command.body,
    ),
    ResourceMethod.delete => delete(
      SocietyAdminRemoteDataSource.pathFor(command.target),
    ),
    ResourceMethod.uploadLogo => uploadLogo(command.bytes!, command.filename!),
    ResourceMethod.uploadVisitorPhoto => uploadVisitorPhoto(
      command.target.values.single.toString(),
      command.bytes!,
      command.filename!,
    ),
    ResourceMethod.uploadDocument => uploadDocument(
      command.documentType!,
      command.bytes!,
      command.filename!,
    ),
    ResourceMethod.download => Future<Object?>.value(<int>[]),
    ResourceMethod.upload => uploadDocument(
      SocietyAdminRemoteDataSource.pathFor(command.target),
      command.bytes!,
      command.filename!,
    ),
  };
  @override
  String absoluteUrl(String path) => 'http://invalid$path';
}
