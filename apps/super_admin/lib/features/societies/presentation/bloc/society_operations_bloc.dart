import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/society_summary.dart';
import '../../domain/usecases/societies_use_cases.dart';

sealed class SocietyOperationsEvent {
  const SocietyOperationsEvent();
}

class SocietyCreatePrepared extends SocietyOperationsEvent {
  const SocietyCreatePrepared();
}

class SocietyCreated extends SocietyOperationsEvent {
  const SocietyCreated(this.body);
  final Map<String, dynamic> body;
}

class SocietyDetailLoaded extends SocietyOperationsEvent {
  const SocietyDetailLoaded(this.id);
  final String id;
}

class SocietyUpdated extends SocietyOperationsEvent {
  const SocietyUpdated(this.id, this.body);
  final String id;
  final Map<String, dynamic> body;
}

class SocietyLogoUploaded extends SocietyOperationsEvent {
  const SocietyLogoUploaded(this.id, this.bytes, this.filename);
  final String id;
  final List<int> bytes;
  final String filename;
}

enum SocietyOperationFeature { create, detail }

enum SocietyOperationMutation { create, update, uploadLogo }

enum SocietyOperationStatus { initial, loading, ready, mutating, failure }

class SocietyOperationsState {
  const SocietyOperationsState({
    this.feature,
    this.status = SocietyOperationStatus.initial,
    this.data,
    this.error,
    this.completedMutation,
    this.result,
    this.revision = 0,
  });
  final SocietyOperationFeature? feature;
  final SocietyOperationStatus status;
  final Object? data;
  final String? error;
  final SocietyOperationMutation? completedMutation;
  final dynamic result;
  final int revision;
  bool get loading =>
      status == SocietyOperationStatus.initial ||
      status == SocietyOperationStatus.loading;
}

class SocietyOperationsBloc
    extends Bloc<SocietyOperationsEvent, SocietyOperationsState> {
  SocietyOperationsBloc(this.useCases) : super(const SocietyOperationsState()) {
    on<SocietyCreatePrepared>(_prepare);
    on<SocietyCreated>(_create);
    on<SocietyDetailLoaded>(_detail);
    on<SocietyUpdated>(_update);
    on<SocietyLogoUploaded>(_logo);
  }
  final SocietiesUseCases useCases;
  String? _detailId;
  Future<void> _prepare(
    SocietyCreatePrepared event,
    Emitter<SocietyOperationsState> emit,
  ) async => _loadCreate(emit);
  Future<void> _loadCreate(
    Emitter<SocietyOperationsState> emit, {
    SocietyOperationMutation? mutation,
  }) async {
    try {
      emit(
        SocietyOperationsState(
          feature: SocietyOperationFeature.create,
          status: SocietyOperationStatus.ready,
          data: await useCases.loadCreateWorkspace(),
          completedMutation: mutation,
          revision: state.revision + (mutation == null ? 0 : 1),
        ),
      );
    } catch (error) {
      emit(
        SocietyOperationsState(
          feature: SocietyOperationFeature.create,
          status: SocietyOperationStatus.failure,
          data: state.data is SocietyCreateWorkspace ? state.data : null,
          error: error.toString(),
          revision: state.revision,
        ),
      );
    }
  }

  Future<SocietyDetailWorkspace> _detailData(String id) =>
      useCases.loadDetail(id);

  Future<void> _detail(
    SocietyDetailLoaded event,
    Emitter<SocietyOperationsState> emit,
  ) async {
    _detailId = event.id;
    emit(
      SocietyOperationsState(
        feature: SocietyOperationFeature.detail,
        status: SocietyOperationStatus.loading,
        revision: state.revision,
      ),
    );
    await _reloadDetail(emit);
  }

  Future<void> _reloadDetail(
    Emitter<SocietyOperationsState> emit, {
    SocietyOperationMutation? mutation,
  }) async {
    try {
      emit(
        SocietyOperationsState(
          feature: SocietyOperationFeature.detail,
          status: SocietyOperationStatus.ready,
          data: await _detailData(_detailId!),
          completedMutation: mutation,
          revision: state.revision + (mutation == null ? 0 : 1),
        ),
      );
    } catch (error) {
      emit(
        SocietyOperationsState(
          feature: SocietyOperationFeature.detail,
          status: SocietyOperationStatus.failure,
          data: state.data is SocietyDetailWorkspace ? state.data : null,
          error: error.toString(),
          revision: state.revision,
        ),
      );
    }
  }

  Future<void> _create(
    SocietyCreated event,
    Emitter<SocietyOperationsState> emit,
  ) async {
    emit(
      SocietyOperationsState(
        feature: SocietyOperationFeature.create,
        status: SocietyOperationStatus.mutating,
        data: state.data is SocietyCreateWorkspace ? state.data : null,
        revision: state.revision,
      ),
    );
    try {
      final result = await useCases.create(CreateSocietyCommand(event.body));
      emit(
        SocietyOperationsState(
          feature: SocietyOperationFeature.create,
          status: SocietyOperationStatus.ready,
          data: await useCases.loadCreateWorkspace(),
          completedMutation: SocietyOperationMutation.create,
          result: result,
          revision: state.revision + 1,
        ),
      );
    } catch (error) {
      emit(
        SocietyOperationsState(
          feature: SocietyOperationFeature.create,
          status: SocietyOperationStatus.failure,
          data: state.data is SocietyCreateWorkspace ? state.data : null,
          error: error.toString(),
          revision: state.revision + 1,
        ),
      );
    }
  }

  Future<void> _update(
    SocietyUpdated event,
    Emitter<SocietyOperationsState> emit,
  ) async {
    await _mutateDetail(
      emit,
      () => useCases.update(UpdateSocietyCommand(event.id, event.body)),
      SocietyOperationMutation.update,
    );
  }

  Future<void> _logo(
    SocietyLogoUploaded event,
    Emitter<SocietyOperationsState> emit,
  ) async {
    await _mutateDetail(
      emit,
      () => useCases.uploadLogo(
        UploadSocietyLogoCommand(event.id, event.bytes, event.filename),
      ),
      SocietyOperationMutation.uploadLogo,
    );
  }

  Future<void> _mutateDetail(
    Emitter<SocietyOperationsState> emit,
    Future<void> Function() action,
    SocietyOperationMutation mutation,
  ) async {
    emit(
      SocietyOperationsState(
        feature: SocietyOperationFeature.detail,
        status: SocietyOperationStatus.mutating,
        data: state.data is SocietyDetailWorkspace ? state.data : null,
        revision: state.revision,
      ),
    );
    try {
      await action();
      await _reloadDetail(emit, mutation: mutation);
    } catch (error) {
      emit(
        SocietyOperationsState(
          feature: SocietyOperationFeature.detail,
          status: SocietyOperationStatus.failure,
          data: state.data is SocietyDetailWorkspace ? state.data : null,
          error: error.toString(),
          revision: state.revision + 1,
        ),
      );
    }
  }
}
