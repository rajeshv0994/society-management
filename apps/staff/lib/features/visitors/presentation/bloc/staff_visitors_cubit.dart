import 'package:flutter_bloc/flutter_bloc.dart';

import '../../domain/visitor_models.dart';
import '../../domain/visitor_repository.dart';

enum StaffVisitorsStatus { initial, loading, ready, mutating, failure }

class StaffVisitorsState {
  const StaffVisitorsState({
    this.status = StaffVisitorsStatus.initial,
    this.data,
    this.error,
  });
  final StaffVisitorsStatus status;
  final StaffVisitorWorkspaceData? data;
  final Object? error;
}

class StaffVisitorsCubit extends Cubit<StaffVisitorsState> {
  StaffVisitorsCubit(this.repository) : super(const StaffVisitorsState());
  final StaffVisitorRepository repository;
  int _loadVersion = 0;

  Future<void> load() async {
    final version = ++_loadVersion;
    emit(
      StaffVisitorsState(status: StaffVisitorsStatus.loading, data: state.data),
    );
    try {
      final data = await repository.load();
      if (version != _loadVersion || isClosed) return;
      emit(StaffVisitorsState(status: StaffVisitorsStatus.ready, data: data));
    } catch (error) {
      if (version != _loadVersion || isClosed) return;
      emit(
        StaffVisitorsState(
          status: StaffVisitorsStatus.failure,
          data: state.data,
          error: error,
        ),
      );
    }
  }

  Future<List<VisitorTarget>> loadHosts() => repository.loadHosts();
  Future<List<VisitorTarget>> loadTargets() => repository.loadTargets();

  Future<void> updateStatus(StaffVisitorRecord visitor, String status) =>
      _mutate(() => repository.updateStatus(visitor.id, status));

  Future<void> uploadPhoto(
    StaffVisitorRecord visitor,
    List<int> bytes,
    String filename,
  ) => _mutate(() => repository.uploadPhoto(visitor.id, bytes, filename));

  Future<StaffVisitorRecord> create(StaffVisitorRegistration input) async {
    StaffVisitorRecord? created;
    await _mutate(() async => created = await repository.create(input));
    return created!;
  }

  Future<void> recordPhoneApproval(StaffVisitorRecord visitor, String note) =>
      _mutate(() => repository.recordPhoneApproval(visitor.id, note));

  Future<void> recordDecision(StaffVisitorRecord visitor, String decision) =>
      _mutate(() => repository.recordDecision(visitor.id, decision));

  Future<void> _mutate(Future<void> Function() operation) async {
    emit(
      StaffVisitorsState(
        status: StaffVisitorsStatus.mutating,
        data: state.data,
      ),
    );
    try {
      await operation();
      await load();
    } catch (error) {
      emit(
        StaffVisitorsState(
          status: StaffVisitorsStatus.failure,
          data: state.data,
          error: error,
        ),
      );
      rethrow;
    }
  }
}
