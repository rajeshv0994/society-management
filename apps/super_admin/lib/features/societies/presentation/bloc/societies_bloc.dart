import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/society_summary.dart';
import '../../domain/usecases/societies_use_cases.dart';

sealed class SocietiesEvent {
  const SocietiesEvent();
}

class SocietiesLoaded extends SocietiesEvent {
  const SocietiesLoaded({this.status, this.search});
  final String? status, search;
}

class SocietyApproved extends SocietiesEvent {
  const SocietyApproved(this.id);
  final String id;
}

class SocietyRejected extends SocietiesEvent {
  const SocietyRejected(this.id, this.reason);
  final String id, reason;
}

sealed class SocietiesState {
  const SocietiesState();
}

class SocietiesLoading extends SocietiesState {
  const SocietiesLoading();
}

class SocietiesReady extends SocietiesState {
  const SocietiesReady(this.items, {this.message});
  final List<SocietySummary> items;
  final String? message;
}

class SocietiesFailure extends SocietiesState {
  const SocietiesFailure(this.message);
  final String message;
}

class SocietiesBloc extends Bloc<SocietiesEvent, SocietiesState> {
  SocietiesBloc(this.useCases) : super(const SocietiesLoading()) {
    on<SocietiesLoaded>((e, emit) async {
      emit(const SocietiesLoading());
      try {
        emit(
          SocietiesReady(
            await useCases.load(status: e.status, search: e.search),
          ),
        );
      } catch (error) {
        emit(SocietiesFailure(error.toString()));
      }
    });
    on<SocietyApproved>((e, emit) async {
      try {
        final result = await useCases.approve(e.id);
        emit(
          SocietiesReady(
            await useCases.load(),
            message: 'Setup code: ${result['setupCode']}',
          ),
        );
      } catch (error) {
        emit(SocietiesFailure(error.toString()));
      }
    });
    on<SocietyRejected>((e, emit) async {
      try {
        await useCases.reject(e.id, e.reason);
        emit(
          SocietiesReady(await useCases.load(), message: 'Society rejected'),
        );
      } catch (error) {
        emit(SocietiesFailure(error.toString()));
      }
    });
  }
  final SocietiesUseCases useCases;
}
