import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/audit_page.dart';
import '../../domain/usecases/audit_use_cases.dart';

sealed class AuditEvent {
  const AuditEvent();
}

class AuditLoaded extends AuditEvent {
  const AuditLoaded({
    this.page = 0,
    this.search,
    this.action,
    this.entityType,
    this.from,
    this.to,
  });
  final int page;
  final String? search;
  final String? action;
  final String? entityType;
  final DateTime? from;
  final DateTime? to;
}

enum AuditStatus { initial, loading, ready, failure }

class AuditState {
  const AuditState({this.status = AuditStatus.initial, this.data, this.error});
  final AuditStatus status;
  final AuditPage? data;
  final String? error;
  bool get loading =>
      status == AuditStatus.initial || status == AuditStatus.loading;
}

class AuditBloc extends Bloc<AuditEvent, AuditState> {
  AuditBloc(this.useCases) : super(const AuditState()) {
    on<AuditLoaded>(_load);
  }
  final AuditUseCases useCases;
  Future<void> _load(AuditLoaded event, Emitter<AuditState> emit) async {
    emit(AuditState(status: AuditStatus.loading, data: state.data));
    try {
      emit(
        AuditState(
          status: AuditStatus.ready,
          data: await useCases.load(
            AuditQuery(
              page: event.page,
              search: event.search,
              action: event.action,
              entityType: event.entityType,
              from: event.from,
              to: event.to,
            ),
          ),
        ),
      );
    } catch (error) {
      emit(
        AuditState(
          status: AuditStatus.failure,
          data: state.data,
          error: error.toString(),
        ),
      );
    }
  }
}
