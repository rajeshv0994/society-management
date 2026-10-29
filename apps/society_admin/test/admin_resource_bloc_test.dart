import 'package:flutter_test/flutter_test.dart';
import 'dart:async';
import 'package:society_admin_app/core/domain/entities/admin_resource.dart';
import 'package:society_admin_app/core/domain/usecases/admin_resource_use_cases.dart';
import 'package:society_admin_app/core/presentation/bloc/admin_resource_bloc.dart';
import 'support/fake_admin_repository.dart';

class _Repository extends FakeAdminRepository {
  bool fail = false;
  int mutations = 0;

  @override
  Future<Map<String, dynamic>> map(String path) async {
    if (fail) throw StateError('unavailable');
    return {'path': path};
  }

  @override
  Future<Object?> post(String path, Map<String, dynamic> body) async {
    mutations++;
    return {'saved': true};
  }
}

class _ConcurrentRepository extends FakeAdminRepository {
  final responses = <String, Completer<Map<String, dynamic>>>{};

  @override
  Future<Map<String, dynamic>> map(String path) =>
      (responses[path] ??= Completer<Map<String, dynamic>>()).future;
}

class _SameKeyRepository extends FakeAdminRepository {
  final responses = <Completer<Map<String, dynamic>>>[];

  @override
  Future<Map<String, dynamic>> map(String path) {
    final response = Completer<Map<String, dynamic>>();
    responses.add(response);
    return response.future;
  }
}

class _ConcurrentMutationRepository extends FakeAdminRepository {
  final responses = <Completer<Object?>>[];

  @override
  Future<Object?> post(String path, Map<String, dynamic> body) {
    final response = Completer<Object?>();
    responses.add(response);
    return response.future;
  }
}

void main() {
  test('generic admin records are deeply immutable domain models', () {
    final record = AdminRecord.from({
      'name': 'Example',
      'nested': {'status': 'READY'},
    });
    expect(record['nested'], isA<AdminRecord>());
    expect(() => record['name'] = 'Changed', throwsUnsupportedError);
  });

  test('generic admin command payloads are deeply immutable', () {
    final command = ResourceCommand(
      ResourceMethod.post,
      AdminTargets.visitors,
      body: {
        'name': 'Visitor',
        'metadata': {'source': 'ADMIN'},
      },
    );
    expect(command.body['metadata'], isA<AdminRecord>());
    expect(() => command.body['name'] = 'Changed', throwsUnsupportedError);
  });

  test('BLoC owns load, success, mutation and failure state', () async {
    final repository = _Repository();
    final bloc = AdminResourceBloc(AdminResourceUseCases(repository));
    addTearDown(bloc.close);

    bloc.load(
      'profile',
      ResourceRequest(AdminTargets.external('/profile'), ResourceKind.document),
    );
    await bloc.stream.firstWhere(
      (state) => state.resource('profile').status == ResourceStatus.ready,
    );
    expect(
      (bloc.state.resource('profile').value as ResourceDocument).data['path'],
      '/profile',
    );

    final result = await bloc.execute(
      ResourceCommand(
        ResourceMethod.post,
        AdminTargets.external('/profile'),
        body: {'x': 1},
      ),
    );
    expect(result, {'saved': true});
    expect(repository.mutations, 1);
    expect(bloc.state.revision, 1);

    repository.fail = true;
    bloc.load(
      'audit',
      ResourceRequest(AdminTargets.external('/audit'), ResourceKind.document),
    );
    await bloc.stream.firstWhere(
      (state) => state.resource('audit').status == ResourceStatus.failure,
    );
    expect(bloc.state.resource('audit').error, contains('unavailable'));
  });

  test('concurrent loads preserve every keyed resource', () async {
    final repository = _ConcurrentRepository();
    final bloc = AdminResourceBloc(AdminResourceUseCases(repository));
    addTearDown(bloc.close);

    bloc.load(
      'profile',
      ResourceRequest(AdminTargets.external('/profile'), ResourceKind.document),
    );
    bloc.load(
      'subscription',
      ResourceRequest(
        AdminTargets.external('/subscription'),
        ResourceKind.document,
      ),
    );
    await Future<void>.delayed(Duration.zero);

    repository.responses['/subscription']!.complete({'plan': 'STANDARD'});
    await Future<void>.delayed(Duration.zero);
    repository.responses['/profile']!.complete({'name': 'Green View'});

    await bloc.stream.firstWhere(
      (state) =>
          state.resource('profile').status == ResourceStatus.ready &&
          state.resource('subscription').status == ResourceStatus.ready,
    );
    expect(bloc.state.resources.keys, containsAll(['profile', 'subscription']));
  });

  test(
    'an older response cannot overwrite a newer load for the same key',
    () async {
      final repository = _SameKeyRepository();
      final bloc = AdminResourceBloc(AdminResourceUseCases(repository));
      addTearDown(bloc.close);

      final request = ResourceRequest(
        AdminTargets.external('/profile'),
        ResourceKind.document,
      );
      bloc.load('profile', request);
      await Future<void>.delayed(Duration.zero);
      bloc.load('profile', request);
      await Future<void>.delayed(Duration.zero);

      repository.responses[1].complete({'name': 'New profile'});
      await bloc.stream.firstWhere(
        (state) =>
            (state.resource('profile').value as ResourceDocument?)
                ?.data['name'] ==
            'New profile',
      );
      repository.responses[0].complete({'name': 'Old profile'});
      await Future<void>.delayed(Duration.zero);

      expect(
        (bloc.state.resource('profile').value as ResourceDocument).data['name'],
        'New profile',
      );
    },
  );

  test(
    'mutation state remains active until every mutation completes',
    () async {
      final repository = _ConcurrentMutationRepository();
      final bloc = AdminResourceBloc(AdminResourceUseCases(repository));
      addTearDown(bloc.close);

      final first = bloc.execute(
        ResourceCommand(ResourceMethod.post, AdminTargets.external('/first')),
      );
      final second = bloc.execute(
        ResourceCommand(ResourceMethod.post, AdminTargets.external('/second')),
      );
      await Future<void>.delayed(Duration.zero);
      expect(bloc.state.mutating, isTrue);

      repository.responses.first.complete({});
      await first;
      expect(bloc.state.mutating, isTrue);

      repository.responses.last.complete({});
      await second;
      expect(bloc.state.mutating, isFalse);
      expect(bloc.state.revision, 2);
    },
  );
}
