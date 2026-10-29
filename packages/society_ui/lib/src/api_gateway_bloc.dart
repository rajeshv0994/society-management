import 'package:flutter_bloc/flutter_bloc.dart';

import 'api_client.dart';

enum GatewayMethod { get, post, put, patch, delete, getBytes, upload }

enum GatewayOperationStatus { running, succeeded, failed }

class GatewayOperation {
  const GatewayOperation({required this.status, this.result, this.error});

  final GatewayOperationStatus status;
  final Object? result;
  final Object? error;

  bool get completed => status != GatewayOperationStatus.running;
}

class ApiGatewayState {
  ApiGatewayState({Map<int, GatewayOperation> operations = const {}})
    : operations = Map.unmodifiable(operations);

  final Map<int, GatewayOperation> operations;
}

class ApiGatewayRequested {
  const ApiGatewayRequested({
    required this.id,
    required this.method,
    required this.path,
    this.body,
    this.bytes,
    this.filename,
    this.field = 'file',
  });

  final int id;
  final GatewayMethod method;
  final String path;
  final Object? body;
  final List<int>? bytes;
  final String? filename;
  final String field;
}

/// Owns the observable lifecycle of every request made by shared portal pages.
class ApiGatewayBloc extends Bloc<ApiGatewayRequested, ApiGatewayState> {
  ApiGatewayBloc(this.upstream) : super(ApiGatewayState()) {
    on<ApiGatewayRequested>(_request);
  }

  final ApiGateway upstream;
  int _nextId = 0;

  Future<Object?> execute({
    required GatewayMethod method,
    required String path,
    Object? body,
    List<int>? bytes,
    String? filename,
    String field = 'file',
  }) {
    final id = ++_nextId;
    final result = stream
        .firstWhere((next) => next.operations[id]?.completed == true)
        .then((next) {
          final operation = next.operations[id]!;
          if (operation.status == GatewayOperationStatus.failed) {
            return Future<Object?>.error(operation.error!);
          }
          return operation.result;
        });
    add(
      ApiGatewayRequested(
        id: id,
        method: method,
        path: path,
        body: body,
        bytes: bytes,
        filename: filename,
        field: field,
      ),
    );
    return result;
  }

  Future<void> _request(
    ApiGatewayRequested event,
    Emitter<ApiGatewayState> emit,
  ) async {
    emit(
      ApiGatewayState(
        operations: Map.of(state.operations)
          ..[event.id] = const GatewayOperation(
            status: GatewayOperationStatus.running,
          ),
      ),
    );
    try {
      final result = await switch (event.method) {
        GatewayMethod.get => upstream.get(event.path),
        GatewayMethod.post => upstream.post(event.path, event.body),
        GatewayMethod.put => upstream.put(event.path, event.body),
        GatewayMethod.patch => upstream.patch(event.path, event.body),
        GatewayMethod.delete => upstream.delete(event.path),
        GatewayMethod.getBytes => upstream.getBytes(event.path),
        GatewayMethod.upload => upstream.upload(
          event.path,
          bytes: event.bytes!,
          filename: event.filename!,
          field: event.field,
        ),
      };
      emit(
        ApiGatewayState(
          operations: Map.of(state.operations)
            ..[event.id] = GatewayOperation(
              status: GatewayOperationStatus.succeeded,
              result: result,
            ),
        ),
      );
    } catch (error) {
      emit(
        ApiGatewayState(
          operations: Map.of(state.operations)
            ..[event.id] = GatewayOperation(
              status: GatewayOperationStatus.failed,
              error: error,
            ),
        ),
      );
    }
  }
}

class BlocBackedApiGateway implements BlocManagedApiGateway {
  const BlocBackedApiGateway(this.bloc);

  final ApiGatewayBloc bloc;

  @override
  String get baseUrl => bloc.upstream.baseUrl;

  @override
  Future<Object?> get(String path) =>
      bloc.execute(method: GatewayMethod.get, path: path);

  @override
  Future<Object?> post(String path, [Object? body]) =>
      bloc.execute(method: GatewayMethod.post, path: path, body: body);

  @override
  Future<Object?> put(String path, [Object? body]) =>
      bloc.execute(method: GatewayMethod.put, path: path, body: body);

  @override
  Future<Object?> patch(String path, [Object? body]) =>
      bloc.execute(method: GatewayMethod.patch, path: path, body: body);

  @override
  Future<Object?> delete(String path) =>
      bloc.execute(method: GatewayMethod.delete, path: path);

  @override
  Future<List<int>> getBytes(String path) async =>
      (await bloc.execute(method: GatewayMethod.getBytes, path: path))
          as List<int>;

  @override
  Future<Object?> upload(
    String path, {
    required List<int> bytes,
    required String filename,
    String field = 'file',
  }) => bloc.execute(
    method: GatewayMethod.upload,
    path: path,
    bytes: bytes,
    filename: filename,
    field: field,
  );
}
