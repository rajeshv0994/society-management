import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  test('refresh publishes typed loading and ready state', () async {
    final bloc = AsyncDataBloc<List<int>>(() async => [1, 2, 3]);
    addTearDown(bloc.close);

    await bloc.refresh();

    expect(bloc.state.status, AsyncDataStatus.ready);
    expect(bloc.state.data, [1, 2, 3]);
    expect(bloc.state.error, isNull);
  });

  test('failure keeps the last successful data available', () async {
    var shouldFail = false;
    final bloc = AsyncDataBloc<String>(() async {
      if (shouldFail) throw StateError('offline');
      return 'cached';
    });
    addTearDown(bloc.close);

    await bloc.refresh();
    shouldFail = true;
    await bloc.refresh();

    expect(bloc.state.status, AsyncDataStatus.failure);
    expect(bloc.state.data, 'cached');
    expect(bloc.state.error, isA<StateError>());
  });

  test('overlapping refreshes execute sequentially', () async {
    final first = Completer<int>();
    var calls = 0;
    final bloc = AsyncDataBloc<int>(() {
      calls++;
      return calls == 1 ? first.future : Future.value(2);
    });
    addTearDown(bloc.close);

    final firstRefresh = bloc.refresh();
    final secondRefresh = bloc.refresh();
    await Future<void>.delayed(Duration.zero);
    expect(calls, 1);

    first.complete(1);
    await Future.wait([firstRefresh, secondRefresh]);

    expect(calls, 2);
    expect(bloc.state.data, 2);
    expect(bloc.state.requestId, 2);
  });
}
