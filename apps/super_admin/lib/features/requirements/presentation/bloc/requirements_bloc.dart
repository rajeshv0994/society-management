import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/requirements_workspace.dart';
import '../../domain/usecases/requirements_use_cases.dart';

sealed class RequirementsEvent {
  const RequirementsEvent();
}

class RequirementsLoaded extends RequirementsEvent {
  const RequirementsLoaded();
}

class RequirementReviewed extends RequirementsEvent {
  const RequirementReviewed(this.id, this.status, this.notes);
  final String id;
  final String status;
  final String notes;
}

class CustomPlanCreated extends RequirementsEvent {
  const CustomPlanCreated(this.body);
  final Map<String, dynamic> body;
}

class CustomPlanUpdated extends RequirementsEvent {
  const CustomPlanUpdated(this.id, this.body);
  final String id;
  final Map<String, dynamic> body;
}

class CustomPlanCancelled extends RequirementsEvent {
  const CustomPlanCancelled(this.id, this.reason);
  final String id;
  final String reason;
}

enum RequirementsMutation { review, createPlan, updatePlan, cancelPlan }

enum RequirementsStatus { initial, loading, ready, mutating, failure }

class RequirementsState {
  const RequirementsState({
    this.status = RequirementsStatus.initial,
    this.data,
    this.error,
    this.completedMutation,
    this.revision = 0,
  });
  final RequirementsStatus status;
  final RequirementsWorkspace? data;
  final String? error;
  final RequirementsMutation? completedMutation;
  final int revision;
  bool get loading =>
      status == RequirementsStatus.initial ||
      status == RequirementsStatus.loading;
}

class RequirementsBloc extends Bloc<RequirementsEvent, RequirementsState> {
  RequirementsBloc(this.useCases) : super(const RequirementsState()) {
    on<RequirementsLoaded>(_load);
    on<RequirementReviewed>(_review);
    on<CustomPlanCreated>(_create);
    on<CustomPlanUpdated>(_update);
    on<CustomPlanCancelled>(_cancel);
  }
  final RequirementsUseCases useCases;

  Future<void> _load(
    RequirementsLoaded event,
    Emitter<RequirementsState> emit,
  ) async {
    emit(
      RequirementsState(
        status: RequirementsStatus.loading,
        data: state.data,
        revision: state.revision,
      ),
    );
    await _reload(emit);
  }

  Future<void> _reload(
    Emitter<RequirementsState> emit, {
    RequirementsMutation? mutation,
  }) async {
    try {
      emit(
        RequirementsState(
          status: RequirementsStatus.ready,
          data: await useCases.load(),
          completedMutation: mutation,
          revision: state.revision + (mutation == null ? 0 : 1),
        ),
      );
    } catch (error) {
      emit(
        RequirementsState(
          status: RequirementsStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision,
        ),
      );
    }
  }

  Future<void> _review(
    RequirementReviewed event,
    Emitter<RequirementsState> emit,
  ) async {
    emit(
      RequirementsState(
        status: RequirementsStatus.mutating,
        data: state.data,
        revision: state.revision,
      ),
    );
    try {
      await useCases.review(
        ReviewRequirementCommand(event.id, event.status, event.notes),
      );
      await _reload(emit, mutation: RequirementsMutation.review);
    } catch (error) {
      emit(
        RequirementsState(
          status: RequirementsStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision + 1,
        ),
      );
    }
  }

  Future<void> _create(
    CustomPlanCreated event,
    Emitter<RequirementsState> emit,
  ) async {
    emit(
      RequirementsState(
        status: RequirementsStatus.mutating,
        data: state.data,
        revision: state.revision,
      ),
    );
    try {
      await useCases.createPlan(CreateCustomPlanCommand(event.body));
      await _reload(emit, mutation: RequirementsMutation.createPlan);
    } catch (error) {
      emit(
        RequirementsState(
          status: RequirementsStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision + 1,
        ),
      );
    }
  }

  Future<void> _update(
    CustomPlanUpdated event,
    Emitter<RequirementsState> emit,
  ) async {
    emit(
      RequirementsState(
        status: RequirementsStatus.mutating,
        data: state.data,
        revision: state.revision,
      ),
    );
    try {
      await useCases.updateCustomPlan(
        UpdateCustomPlanCommand(event.id, event.body),
      );
      await _reload(emit, mutation: RequirementsMutation.updatePlan);
    } catch (error) {
      emit(
        RequirementsState(
          status: RequirementsStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision + 1,
        ),
      );
    }
  }

  Future<void> _cancel(
    CustomPlanCancelled event,
    Emitter<RequirementsState> emit,
  ) async {
    emit(
      RequirementsState(
        status: RequirementsStatus.mutating,
        data: state.data,
        revision: state.revision,
      ),
    );
    try {
      await useCases.cancelCustomPlan(event.id, event.reason);
      await _reload(emit, mutation: RequirementsMutation.cancelPlan);
    } catch (error) {
      emit(
        RequirementsState(
          status: RequirementsStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision + 1,
        ),
      );
    }
  }
}
