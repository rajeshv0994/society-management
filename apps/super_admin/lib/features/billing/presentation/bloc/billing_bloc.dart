import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/billing_dashboard.dart';
import '../../domain/usecases/billing_use_cases.dart';

sealed class BillingEvent {
  const BillingEvent();
}

class BillingLoaded extends BillingEvent {
  const BillingLoaded();
}

class SubscriptionOrderCreated extends BillingEvent {
  const SubscriptionOrderCreated(this.body);
  final Map<String, dynamic> body;
}

class SubscriptionInvoiceRequested extends BillingEvent {
  const SubscriptionInvoiceRequested(this.orderId);
  final String orderId;
}

class SubscriptionPaymentReconciled extends BillingEvent {
  const SubscriptionPaymentReconciled(this.orderId);
  final String orderId;
}

class SubscriptionOrderCancelled extends BillingEvent {
  const SubscriptionOrderCancelled(this.orderId, this.reason);
  final String orderId;
  final String reason;
}

enum BillingMutation {
  createOrder,
  downloadInvoice,
  reconcilePayment,
  cancelOrder,
}

enum BillingStatus { initial, loading, ready, mutating, failure }

class BillingState {
  const BillingState({
    this.status = BillingStatus.initial,
    this.data,
    this.error,
    this.completedMutation,
    this.result,
    this.revision = 0,
  });
  final BillingStatus status;
  final BillingDashboard? data;
  final String? error;
  final BillingMutation? completedMutation;
  final dynamic result;
  final int revision;
  bool get loading =>
      status == BillingStatus.initial || status == BillingStatus.loading;
}

class BillingBloc extends Bloc<BillingEvent, BillingState> {
  BillingBloc(this.useCases) : super(const BillingState()) {
    on<BillingLoaded>(_load);
    on<SubscriptionOrderCreated>(_create);
    on<SubscriptionInvoiceRequested>(_invoice);
    on<SubscriptionPaymentReconciled>(_reconcile);
    on<SubscriptionOrderCancelled>(_cancel);
  }
  final BillingUseCases useCases;
  Future<void> _load(BillingLoaded event, Emitter<BillingState> emit) async =>
      _reload(emit);
  Future<void> _reload(
    Emitter<BillingState> emit, {
    BillingMutation? mutation,
    dynamic result,
  }) async {
    try {
      emit(
        BillingState(
          status: BillingStatus.ready,
          data: await useCases.load(),
          completedMutation: mutation,
          result: result,
          revision: state.revision + (mutation == null ? 0 : 1),
        ),
      );
    } catch (error) {
      emit(
        BillingState(
          status: BillingStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision,
        ),
      );
    }
  }

  Future<void> _create(
    SubscriptionOrderCreated event,
    Emitter<BillingState> emit,
  ) async {
    emit(
      BillingState(
        status: BillingStatus.mutating,
        data: state.data,
        revision: state.revision,
      ),
    );
    try {
      await useCases.createOrder(CreateSubscriptionOrderCommand(event.body));
      await _reload(emit, mutation: BillingMutation.createOrder);
    } catch (error) {
      emit(
        BillingState(
          status: BillingStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision + 1,
        ),
      );
    }
  }

  Future<void> _invoice(
    SubscriptionInvoiceRequested event,
    Emitter<BillingState> emit,
  ) async {
    emit(
      BillingState(
        status: BillingStatus.mutating,
        data: state.data,
        revision: state.revision,
      ),
    );
    try {
      final bytes = await useCases.downloadInvoice(event.orderId);
      emit(
        BillingState(
          status: BillingStatus.ready,
          data: state.data,
          completedMutation: BillingMutation.downloadInvoice,
          result: bytes,
          revision: state.revision + 1,
        ),
      );
    } catch (error) {
      emit(
        BillingState(
          status: BillingStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision + 1,
        ),
      );
    }
  }

  Future<void> _reconcile(
    SubscriptionPaymentReconciled event,
    Emitter<BillingState> emit,
  ) async {
    emit(BillingState(status: BillingStatus.mutating, data: state.data));
    try {
      await useCases.reconcilePayment(event.orderId);
      await _reload(emit, mutation: BillingMutation.reconcilePayment);
    } catch (error) {
      emit(
        BillingState(
          status: BillingStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision + 1,
        ),
      );
    }
  }

  Future<void> _cancel(
    SubscriptionOrderCancelled event,
    Emitter<BillingState> emit,
  ) async {
    emit(BillingState(status: BillingStatus.mutating, data: state.data));
    try {
      await useCases.cancelOrder(event.orderId, event.reason);
      await _reload(emit, mutation: BillingMutation.cancelOrder);
    } catch (error) {
      emit(
        BillingState(
          status: BillingStatus.failure,
          data: state.data,
          error: error.toString(),
          revision: state.revision + 1,
        ),
      );
    }
  }
}
