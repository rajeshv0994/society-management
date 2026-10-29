import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/admin_section.dart';

class NavigationRequested {
  const NavigationRequested(this.section, {this.societyStatus});
  final AdminSection section;
  final String? societyStatus;
}

class NavigationState {
  const NavigationState(this.section, {this.societyStatus});
  final AdminSection section;
  final String? societyStatus;
}

class NavigationBloc extends Bloc<NavigationRequested, NavigationState> {
  NavigationBloc() : super(const NavigationState(AdminSection.dashboard)) {
    on<NavigationRequested>(
      (e, emit) =>
          emit(NavigationState(e.section, societyStatus: e.societyStatus)),
    );
  }
}
