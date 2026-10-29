import 'package:flutter_bloc/flutter_bloc.dart';

import '../../domain/public_models.dart';
import '../../domain/public_repository.dart';

enum PublicStatus { initial, loading, ready, submitting, success, failure }

class PublicState<T> {
  const PublicState({
    this.status = PublicStatus.initial,
    this.data,
    this.result,
    this.message,
  });
  final PublicStatus status;
  final T? data;
  final PublicRecord? result;
  final String? message;
  bool get busy =>
      status == PublicStatus.loading || status == PublicStatus.submitting;
}

class PlansCubit extends Cubit<PublicState<List<PublicRecord>>> {
  PlansCubit(this.repository) : super(const PublicState());
  final PublicRepository repository;
  Future<void> load() async {
    emit(const PublicState(status: PublicStatus.loading));
    try {
      emit(
        PublicState(
          status: PublicStatus.ready,
          data: await repository.loadPlans(),
        ),
      );
    } catch (error) {
      emit(
        PublicState(
          status: PublicStatus.failure,
          message: error.toString(),
          data: const [],
        ),
      );
    }
  }
}

class LoginCubit extends Cubit<PublicState<List<PublicRecord>>> {
  LoginCubit(this.repository) : super(const PublicState());
  final PublicRepository repository;
  Future<void> load() async {
    emit(const PublicState(status: PublicStatus.loading));
    try {
      emit(
        PublicState(
          status: PublicStatus.ready,
          data: await repository.loadSocieties(),
        ),
      );
    } catch (error) {
      emit(
        PublicState(
          status: PublicStatus.failure,
          data: const [],
          message: error.toString(),
        ),
      );
    }
  }

  Future<PublicRecord?> login(LoginCommand command) async {
    emit(PublicState(status: PublicStatus.submitting, data: state.data));
    try {
      final result = await repository.login(command);
      emit(
        PublicState(
          status: PublicStatus.success,
          data: state.data,
          result: result,
        ),
      );
      return result;
    } catch (error) {
      emit(
        PublicState(
          status: PublicStatus.failure,
          data: state.data,
          message: error.toString(),
        ),
      );
      return null;
    }
  }
}

class RegistrationCubit extends Cubit<PublicState<List<PublicRecord>>> {
  RegistrationCubit(this.repository) : super(const PublicState());
  final PublicRepository repository;
  Future<void> load() async {
    emit(const PublicState(status: PublicStatus.loading));
    try {
      emit(
        PublicState(
          status: PublicStatus.ready,
          data: await repository.loadServices(),
        ),
      );
    } catch (error) {
      emit(
        PublicState(
          status: PublicStatus.failure,
          message: error.toString(),
          data: const [],
        ),
      );
    }
  }

  Future<void> submit(SocietyRegistrationCommand command) async {
    emit(PublicState(status: PublicStatus.submitting, data: state.data));
    try {
      final result = await repository.register(command);
      emit(
        PublicState(
          status: PublicStatus.success,
          data: state.data,
          result: result,
        ),
      );
    } catch (error) {
      emit(
        PublicState(
          status: PublicStatus.failure,
          data: state.data,
          message: error.toString(),
        ),
      );
    }
  }
}

class ActivationCubit extends Cubit<PublicState<void>> {
  ActivationCubit(this.repository) : super(const PublicState());
  final PublicRepository repository;
  Future<void> submit(SocietyActivationCommand command) async {
    emit(const PublicState(status: PublicStatus.submitting));
    try {
      await repository.activate(command);
      emit(
        const PublicState(
          status: PublicStatus.success,
          message: 'Society activated. You can now sign in.',
        ),
      );
    } catch (error) {
      emit(
        PublicState(status: PublicStatus.failure, message: error.toString()),
      );
    }
  }
}

class RequirementCubit extends Cubit<PublicState<void>> {
  RequirementCubit(this.repository) : super(const PublicState());
  final PublicRepository repository;
  Future<void> submit(RequirementCommand command) async {
    emit(const PublicState(status: PublicStatus.submitting));
    try {
      final result = await repository.submitRequirement(command);
      emit(
        PublicState(
          status: PublicStatus.success,
          result: result,
          message:
              'Request ${result['requirementId']} submitted. Our team will contact you.',
        ),
      );
    } catch (error) {
      emit(
        PublicState(status: PublicStatus.failure, message: error.toString()),
      );
    }
  }
}
