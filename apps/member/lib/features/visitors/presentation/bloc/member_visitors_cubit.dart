import 'package:flutter_bloc/flutter_bloc.dart';

import '../../domain/visitor_models.dart';
import '../../domain/visitor_repository.dart';

enum MemberVisitorsStatus { initial, loading, ready, mutating, failure }

class MemberVisitorsState {
  const MemberVisitorsState({
    this.status = MemberVisitorsStatus.initial,
    this.data,
    this.error,
  });

  final MemberVisitorsStatus status;
  final MemberVisitorWorkspaceData? data;
  final Object? error;
}

class MemberVisitorsCubit extends Cubit<MemberVisitorsState> {
  MemberVisitorsCubit(this.repository) : super(const MemberVisitorsState());

  final MemberVisitorRepository repository;
  int _loadVersion = 0;

  Future<void> load() async {
    final version = ++_loadVersion;
    emit(
      MemberVisitorsState(
        status: MemberVisitorsStatus.loading,
        data: state.data,
      ),
    );
    try {
      final data = await repository.load();
      if (version != _loadVersion || isClosed) return;
      emit(MemberVisitorsState(status: MemberVisitorsStatus.ready, data: data));
    } catch (error) {
      if (version != _loadVersion || isClosed) return;
      emit(
        MemberVisitorsState(
          status: MemberVisitorsStatus.failure,
          data: state.data,
          error: error,
        ),
      );
    }
  }

  Future<void> decide(VisitorRecord visitor, String decision, String? note) =>
      _mutate(() => repository.decide(visitor.id, decision, note));

  Future<void> cancel(VisitorRecord visitor, String? note) =>
      _mutate(() => repository.cancel(visitor.id, note));

  Future<void> register(VisitorRegistrationInput input) =>
      _mutate(() => repository.register(input));

  Future<List<int>> loadImage(String url) => repository.loadImage(url);

  Future<void> _mutate(Future<void> Function() operation) async {
    emit(
      MemberVisitorsState(
        status: MemberVisitorsStatus.mutating,
        data: state.data,
      ),
    );
    try {
      await operation();
      await load();
    } catch (error) {
      emit(
        MemberVisitorsState(
          status: MemberVisitorsStatus.failure,
          data: state.data,
          error: error,
        ),
      );
      rethrow;
    }
  }
}
