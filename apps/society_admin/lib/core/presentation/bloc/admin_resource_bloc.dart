import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/admin_resource.dart';
import '../../domain/usecases/admin_resource_use_cases.dart';

enum ResourceStatus { initial, loading, ready, failure }

enum ResourceOperationStatus { running, succeeded, failed }

class ResourceOperation {
  const ResourceOperation({required this.status, this.result, this.error});
  final ResourceOperationStatus status;
  final Object? result;
  final Object? error;
  bool get completed => status != ResourceOperationStatus.running;
}

class ResourceSnapshot {
  const ResourceSnapshot({
    this.status = ResourceStatus.initial,
    this.value,
    this.error,
  });
  final ResourceStatus status;
  final AdminResource? value;
  final String? error;
}

class AdminResourceState {
  AdminResourceState({
    Map<String, ResourceSnapshot> resources = const {},
    this.mutating = false,
    this.error,
    this.revision = 0,
    Map<int, ResourceOperation> operations = const {},
  }) : resources = Map.unmodifiable(resources),
       operations = Map.unmodifiable(operations);
  final Map<String, ResourceSnapshot> resources;
  final bool mutating;
  final String? error;
  final int revision;
  final Map<int, ResourceOperation> operations;
  ResourceSnapshot resource(String key) =>
      resources[key] ?? const ResourceSnapshot();
}

sealed class AdminResourceEvent {
  const AdminResourceEvent();
}

class ResourceLoadRequested extends AdminResourceEvent {
  const ResourceLoadRequested(this.key, this.request);
  final String key;
  final ResourceRequest request;
}

class ResourceMutationRequested extends AdminResourceEvent {
  const ResourceMutationRequested(this.command, this.operationId);
  final ResourceCommand command;
  final int operationId;
}

class ResourceQueryRequested extends AdminResourceEvent {
  const ResourceQueryRequested(this.request, this.operationId);
  final ResourceRequest request;
  final int operationId;
}

class AdminResourceBloc extends Bloc<AdminResourceEvent, AdminResourceState> {
  AdminResourceBloc(this.useCases) : super(AdminResourceState()) {
    on<ResourceLoadRequested>(_load);
    on<ResourceMutationRequested>(_mutate);
    on<ResourceQueryRequested>(_query);
  }
  final AdminResourceUseCases useCases;
  final Map<String, int> _loadVersions = {};
  int _pendingMutations = 0;
  int _nextOperationId = 0;
  void load(String key, ResourceRequest request) =>
      add(ResourceLoadRequested(key, request));
  Future<Object?> execute(ResourceCommand command) {
    final id = ++_nextOperationId;
    final result = _operationResult(id);
    add(ResourceMutationRequested(command, id));
    return result;
  }

  Future<ResourceDocument> queryDocument(AdminTarget target) async =>
      await _queryResource(ResourceRequest(target, ResourceKind.document))
          as ResourceDocument;
  Future<ResourceCollection> queryCollection(AdminTarget target) async =>
      await _queryResource(ResourceRequest(target, ResourceKind.collection))
          as ResourceCollection;
  Future<Object?> query(AdminTarget target) async =>
      (await _queryResource(ResourceRequest(target, ResourceKind.raw))
              as RawResource)
          .value;
  Future<AdminResource> _queryResource(ResourceRequest request) {
    final id = ++_nextOperationId;
    final result = _operationResult(id).then((value) => value as AdminResource);
    add(ResourceQueryRequested(request, id));
    return result;
  }

  Future<Object?> _operationResult(int id) => stream
      .firstWhere((next) => next.operations[id]?.completed == true)
      .then((next) {
        final operation = next.operations[id]!;
        if (operation.status == ResourceOperationStatus.failed) {
          return Future<Object?>.error(operation.error!);
        }
        return operation.result;
      });

  String absoluteUrl(String path) => useCases.absoluteUrl(path);
  Future<void> _load(
    ResourceLoadRequested event,
    Emitter<AdminResourceState> emit,
  ) async {
    final version = (_loadVersions[event.key] ?? 0) + 1;
    _loadVersions[event.key] = version;
    final next = Map<String, ResourceSnapshot>.from(state.resources)
      ..[event.key] = ResourceSnapshot(
        status: ResourceStatus.loading,
        value: state.resource(event.key).value,
      );
    emit(
      AdminResourceState(
        resources: next,
        mutating: state.mutating,
        revision: state.revision,
        operations: state.operations,
      ),
    );
    try {
      final value = await useCases.load(event.request);
      if (_loadVersions[event.key] != version) return;
      final ready = Map<String, ResourceSnapshot>.from(state.resources)
        ..[event.key] = ResourceSnapshot(
          status: ResourceStatus.ready,
          value: value,
        );
      emit(
        AdminResourceState(
          resources: ready,
          mutating: state.mutating,
          revision: state.revision,
          operations: state.operations,
        ),
      );
    } catch (error) {
      if (_loadVersions[event.key] != version) return;
      final failed = Map<String, ResourceSnapshot>.from(state.resources)
        ..[event.key] = ResourceSnapshot(
          status: ResourceStatus.failure,
          value: state.resource(event.key).value,
          error: error.toString(),
        );
      emit(
        AdminResourceState(
          resources: failed,
          mutating: state.mutating,
          error: error.toString(),
          revision: state.revision,
          operations: state.operations,
        ),
      );
    }
  }

  Future<void> _mutate(
    ResourceMutationRequested event,
    Emitter<AdminResourceState> emit,
  ) async {
    _pendingMutations++;
    final running = Map<int, ResourceOperation>.from(state.operations)
      ..[event.operationId] = const ResourceOperation(
        status: ResourceOperationStatus.running,
      );
    emit(
      AdminResourceState(
        resources: state.resources,
        mutating: true,
        revision: state.revision,
        operations: running,
      ),
    );
    try {
      final result = await useCases.execute(event.command);
      _pendingMutations--;
      final completed = Map<int, ResourceOperation>.from(state.operations)
        ..[event.operationId] = ResourceOperation(
          status: ResourceOperationStatus.succeeded,
          result: result,
        );
      emit(
        AdminResourceState(
          resources: state.resources,
          mutating: _pendingMutations > 0,
          revision: state.revision + 1,
          operations: completed,
        ),
      );
    } catch (error) {
      _pendingMutations--;
      final failed = Map<int, ResourceOperation>.from(state.operations)
        ..[event.operationId] = ResourceOperation(
          status: ResourceOperationStatus.failed,
          error: error,
        );
      emit(
        AdminResourceState(
          resources: state.resources,
          mutating: _pendingMutations > 0,
          error: error.toString(),
          revision: state.revision + 1,
          operations: failed,
        ),
      );
    }
  }

  Future<void> _query(
    ResourceQueryRequested event,
    Emitter<AdminResourceState> emit,
  ) async {
    final key = 'query:${event.request.target.cacheKey}';
    final version = (_loadVersions[key] ?? 0) + 1;
    _loadVersions[key] = version;
    final operations = Map<int, ResourceOperation>.from(state.operations)
      ..[event.operationId] = const ResourceOperation(
        status: ResourceOperationStatus.running,
      );
    final loading = Map<String, ResourceSnapshot>.from(state.resources)
      ..[key] = ResourceSnapshot(
        status: ResourceStatus.loading,
        value: state.resource(key).value,
      );
    emit(
      AdminResourceState(
        resources: loading,
        mutating: state.mutating,
        revision: state.revision,
        operations: operations,
      ),
    );
    try {
      final value = await useCases.load(event.request);
      final completed = Map<int, ResourceOperation>.from(state.operations)
        ..[event.operationId] = ResourceOperation(
          status: ResourceOperationStatus.succeeded,
          result: value,
        );
      if (_loadVersions[key] != version) {
        emit(
          AdminResourceState(
            resources: state.resources,
            mutating: state.mutating,
            revision: state.revision,
            operations: completed,
          ),
        );
        return;
      }
      final ready = Map<String, ResourceSnapshot>.from(state.resources)
        ..[key] = ResourceSnapshot(status: ResourceStatus.ready, value: value);
      emit(
        AdminResourceState(
          resources: ready,
          mutating: state.mutating,
          revision: state.revision,
          operations: completed,
        ),
      );
    } catch (error) {
      final failedOperations =
          Map<int, ResourceOperation>.from(state.operations)
            ..[event.operationId] = ResourceOperation(
              status: ResourceOperationStatus.failed,
              error: error,
            );
      if (_loadVersions[key] != version) {
        emit(
          AdminResourceState(
            resources: state.resources,
            mutating: state.mutating,
            revision: state.revision,
            operations: failedOperations,
          ),
        );
        return;
      }
      final failed = Map<String, ResourceSnapshot>.from(state.resources)
        ..[key] = ResourceSnapshot(
          status: ResourceStatus.failure,
          value: state.resource(key).value,
          error: error.toString(),
        );
      emit(
        AdminResourceState(
          resources: failed,
          mutating: state.mutating,
          error: error.toString(),
          revision: state.revision,
          operations: failedOperations,
        ),
      );
    }
  }
}
