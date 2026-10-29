import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/management_record.dart';
import '../../domain/usecases/management_use_cases.dart';

sealed class ManagementEvent {
  const ManagementEvent();
}

class ManagementLoaded extends ManagementEvent {
  const ManagementLoaded(this.resource);
  final ManagementResource resource;
}

class PlanSaved extends ManagementEvent {
  const PlanSaved(this.command);
  final UpdatePlanCommand command;
}

class NotificationRetried extends ManagementEvent {
  const NotificationRetried(this.id);
  final String id;
}

class ManagementTicketResolved extends ManagementEvent {
  const ManagementTicketResolved(this.id, this.resolution);
  final String id;
  final String resolution;
}

enum ManagementStatus { initial, loading, ready, mutating, failure }

class ManagementState {
  const ManagementState({
    this.status = ManagementStatus.initial,
    this.data = const [],
    this.error,
    this.revision = 0,
  });
  final ManagementStatus status;
  final List<ManagementRecord> data;
  final String? error;
  final int revision;
  bool get loading =>
      status == ManagementStatus.initial || status == ManagementStatus.loading;
}

class ManagementBloc extends Bloc<ManagementEvent, ManagementState> {
  ManagementBloc(this.useCases) : super(const ManagementState()) {
    on<ManagementLoaded>(_load);
    on<PlanSaved>(_plan);
    on<NotificationRetried>(_retry);
    on<ManagementTicketResolved>(_resolve);
  }
  final ManagementUseCases useCases;
  ManagementResource? _resource;
  Future<void> _load(
    ManagementLoaded event,
    Emitter<ManagementState> emit,
  ) async {
    _resource = event.resource;
    emit(
      ManagementState(
        status: ManagementStatus.loading,
        data: state.data,
        revision: state.revision,
      ),
    );
    await _reload(emit);
  }

  Future<void> _reload(
    Emitter<ManagementState> emit, {
    bool mutated = false,
  }) async {
    try {
      emit(
        ManagementState(
          status: ManagementStatus.ready,
          data: await useCases.load(_resource!),
          revision: state.revision + (mutated ? 1 : 0),
        ),
      );
    } catch (error) {
      emit(
        ManagementState(
          status: ManagementStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision,
        ),
      );
    }
  }

  Future<void> _plan(PlanSaved event, Emitter<ManagementState> emit) async {
    await _mutate(emit, () => useCases.updatePlan(event.command));
  }

  Future<void> _retry(
    NotificationRetried event,
    Emitter<ManagementState> emit,
  ) async {
    await _mutate(emit, () => useCases.retryNotification(event.id));
  }

  Future<void> _resolve(
    ManagementTicketResolved event,
    Emitter<ManagementState> emit,
  ) async {
    await _mutate(
      emit,
      () => useCases.resolveTicket(event.id, event.resolution),
    );
  }

  Future<void> _mutate(
    Emitter<ManagementState> emit,
    Future<void> Function() action,
  ) async {
    emit(
      ManagementState(
        status: ManagementStatus.mutating,
        data: state.data,
        revision: state.revision,
      ),
    );
    try {
      await action();
      await _reload(emit, mutated: true);
    } catch (error) {
      emit(
        ManagementState(
          status: ManagementStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision + 1,
        ),
      );
    }
  }
}
