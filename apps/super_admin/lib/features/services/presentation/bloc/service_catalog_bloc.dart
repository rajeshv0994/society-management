import 'package:flutter_bloc/flutter_bloc.dart';

import '../../domain/entities/service_catalog_item.dart';
import '../../domain/usecases/service_catalog_use_cases.dart';

sealed class ServiceCatalogEvent {
  const ServiceCatalogEvent();
}

class ServiceCatalogLoaded extends ServiceCatalogEvent {
  const ServiceCatalogLoaded();
}

class ServiceCatalogSaved extends ServiceCatalogEvent {
  const ServiceCatalogSaved(this.body, {this.code});
  final Map<String, dynamic> body;
  final String? code;
}

enum ServiceCatalogStatus { initial, loading, ready, mutating, failure }

class ServiceCatalogState {
  const ServiceCatalogState({
    this.status = ServiceCatalogStatus.initial,
    this.data = const [],
    this.error,
    this.revision = 0,
  });
  final ServiceCatalogStatus status;
  final List<ServiceCatalogItem> data;
  final String? error;
  final int revision;
  bool get loading =>
      status == ServiceCatalogStatus.initial ||
      status == ServiceCatalogStatus.loading;
}

class ServiceCatalogBloc
    extends Bloc<ServiceCatalogEvent, ServiceCatalogState> {
  ServiceCatalogBloc(this.useCases) : super(const ServiceCatalogState()) {
    on<ServiceCatalogLoaded>(_load);
    on<ServiceCatalogSaved>(_save);
  }
  final ServiceCatalogUseCases useCases;
  Future<void> _load(
    ServiceCatalogLoaded event,
    Emitter<ServiceCatalogState> emit,
  ) async {
    emit(
      ServiceCatalogState(
        status: ServiceCatalogStatus.loading,
        data: state.data,
        revision: state.revision,
      ),
    );
    try {
      emit(
        ServiceCatalogState(
          status: ServiceCatalogStatus.ready,
          data: await useCases.load(),
          revision: state.revision,
        ),
      );
    } catch (error) {
      emit(
        ServiceCatalogState(
          status: ServiceCatalogStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision,
        ),
      );
    }
  }

  Future<void> _save(
    ServiceCatalogSaved event,
    Emitter<ServiceCatalogState> emit,
  ) async {
    emit(
      ServiceCatalogState(
        status: ServiceCatalogStatus.mutating,
        data: state.data,
        revision: state.revision,
      ),
    );
    try {
      await useCases.save(SaveServiceCommand(event.body, code: event.code));
      emit(
        ServiceCatalogState(
          status: ServiceCatalogStatus.ready,
          data: await useCases.load(),
          revision: state.revision + 1,
        ),
      );
    } catch (error) {
      emit(
        ServiceCatalogState(
          status: ServiceCatalogStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision + 1,
        ),
      );
    }
  }
}
