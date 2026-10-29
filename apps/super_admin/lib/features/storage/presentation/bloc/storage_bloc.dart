import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/storage_dashboard.dart';
import '../../domain/usecases/storage_use_cases.dart';

sealed class StorageEvent {
  const StorageEvent();
}

class StorageLoaded extends StorageEvent {
  const StorageLoaded();
}

class StoragePolicySaved extends StorageEvent {
  const StoragePolicySaved(this.body);
  final Map<String, dynamic> body;
}

enum StorageStatus { initial, loading, ready, mutating, failure }

class StorageState {
  const StorageState({
    this.status = StorageStatus.initial,
    this.data,
    this.error,
    this.revision = 0,
  });
  final StorageStatus status;
  final StorageDashboard? data;
  final String? error;
  final int revision;
  bool get loading =>
      status == StorageStatus.initial || status == StorageStatus.loading;
}

class StorageBloc extends Bloc<StorageEvent, StorageState> {
  StorageBloc(this.useCases) : super(const StorageState()) {
    on<StorageLoaded>(_load);
    on<StoragePolicySaved>(_save);
  }
  final StorageUseCases useCases;
  Future<void> _load(StorageLoaded event, Emitter<StorageState> emit) async {
    emit(
      StorageState(
        status: StorageStatus.loading,
        data: state.data,
        revision: state.revision,
      ),
    );
    try {
      emit(
        StorageState(
          status: StorageStatus.ready,
          data: await useCases.load(),
          revision: state.revision,
        ),
      );
    } catch (error) {
      emit(
        StorageState(
          status: StorageStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision,
        ),
      );
    }
  }

  Future<void> _save(
    StoragePolicySaved event,
    Emitter<StorageState> emit,
  ) async {
    emit(
      StorageState(
        status: StorageStatus.mutating,
        data: state.data,
        revision: state.revision,
      ),
    );
    try {
      await useCases.updatePolicy(UpdateStoragePolicyCommand(event.body));
      emit(
        StorageState(
          status: StorageStatus.ready,
          data: await useCases.load(),
          revision: state.revision + 1,
        ),
      );
    } catch (error) {
      emit(
        StorageState(
          status: StorageStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision + 1,
        ),
      );
    }
  }
}
