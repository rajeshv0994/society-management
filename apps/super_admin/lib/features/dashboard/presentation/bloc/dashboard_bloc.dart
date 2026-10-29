import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/dashboard_metrics.dart';
import '../../domain/usecases/dashboard_use_cases.dart';

sealed class DashboardEvent {
  const DashboardEvent();
}

class DashboardLoaded extends DashboardEvent {
  const DashboardLoaded();
}

sealed class DashboardState {
  const DashboardState();
}

class DashboardLoading extends DashboardState {
  const DashboardLoading();
}

class DashboardReady extends DashboardState {
  const DashboardReady(this.metrics);
  final DashboardMetrics metrics;
}

class DashboardFailure extends DashboardState {
  const DashboardFailure(this.message);
  final String message;
}

class DashboardBloc extends Bloc<DashboardEvent, DashboardState> {
  DashboardBloc(this.useCases) : super(const DashboardLoading()) {
    on<DashboardLoaded>((e, emit) async {
      emit(const DashboardLoading());
      try {
        emit(DashboardReady(await useCases.load()));
      } catch (error) {
        emit(DashboardFailure(error.toString()));
      }
    });
  }
  final DashboardUseCases useCases;
}
