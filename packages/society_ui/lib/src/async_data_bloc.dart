import 'package:flutter_bloc/flutter_bloc.dart';

sealed class AsyncDataEvent {
  const AsyncDataEvent(this.requestId);
  final int requestId;
}

final class AsyncDataRequested extends AsyncDataEvent {
  const AsyncDataRequested(super.requestId);
}

enum AsyncDataStatus { initial, loading, ready, failure }

class AsyncDataState<T> {
  const AsyncDataState({
    this.status = AsyncDataStatus.initial,
    this.data,
    this.error,
    this.requestId = 0,
  });

  final AsyncDataStatus status;
  final T? data;
  final Object? error;
  final int requestId;
}

/// Reusable BLoC for a typed remote read-model.
///
/// Requests are serialized so an older response can never replace newer data.
class AsyncDataBloc<T> extends Bloc<AsyncDataEvent, AsyncDataState<T>> {
  AsyncDataBloc(this.loader) : super(const AsyncDataState()) {
    on<AsyncDataRequested>(_load, transformer: _sequential());
  }

  final Future<T> Function() loader;
  int _latestRequest = 0;

  static EventTransformer<AsyncDataRequested> _sequential() =>
      (events, mapper) => events.asyncExpand(mapper);

  Future<void> refresh() {
    final requestId = ++_latestRequest;
    final completed = stream.firstWhere(
      (next) =>
          next.requestId == requestId && next.status != AsyncDataStatus.loading,
    );
    add(AsyncDataRequested(requestId));
    return completed.then((_) {});
  }

  Future<void> _load(
    AsyncDataRequested event,
    Emitter<AsyncDataState<T>> emit,
  ) async {
    emit(
      AsyncDataState(
        status: AsyncDataStatus.loading,
        data: state.data,
        requestId: event.requestId,
      ),
    );
    try {
      final data = await loader();
      emit(
        AsyncDataState(
          status: AsyncDataStatus.ready,
          data: data,
          requestId: event.requestId,
        ),
      );
    } catch (error) {
      emit(
        AsyncDataState(
          status: AsyncDataStatus.failure,
          data: state.data,
          error: error,
          requestId: event.requestId,
        ),
      );
    }
  }
}
