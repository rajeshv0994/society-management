import 'package:flutter_test/flutter_test.dart';
import 'package:society_ui/society_ui.dart';

void main() {
  test(
    'gateway adapter represents successful requests in BLoC state',
    () async {
      final bloc = ApiGatewayBloc(_FakeGateway());
      addTearDown(bloc.close);
      final gateway = BlocBackedApiGateway(bloc);

      final result = await gateway.post('/records', {'name': 'Typed'});

      expect(result, {
        'path': '/records',
        'body': {'name': 'Typed'},
      });
      expect(
        bloc.state.operations.values.single.status,
        GatewayOperationStatus.succeeded,
      );
    },
  );

  test(
    'gateway adapter returns failures from immutable operation state',
    () async {
      final bloc = ApiGatewayBloc(_FakeGateway(fail: true));
      addTearDown(bloc.close);
      final gateway = BlocBackedApiGateway(bloc);

      await expectLater(gateway.get('/broken'), throwsStateError);

      expect(
        bloc.state.operations.values.single.status,
        GatewayOperationStatus.failed,
      );
      expect(bloc.state.operations.values.single.error, isA<StateError>());
    },
  );
}

class _FakeGateway implements ApiGateway {
  _FakeGateway({this.fail = false});
  final bool fail;

  @override
  String get baseUrl => 'https://example.test';

  @override
  Future<Object?> get(String path) async {
    if (fail) throw StateError('offline');
    return {'path': path};
  }

  @override
  Future<Object?> post(String path, [Object? body]) async => {
    'path': path,
    'body': body,
  };

  @override
  Future<Object?> put(String path, [Object? body]) => post(path, body);

  @override
  Future<Object?> patch(String path, [Object? body]) => post(path, body);

  @override
  Future<Object?> delete(String path) => get(path);

  @override
  Future<List<int>> getBytes(String path) async => [1, 2, 3];

  @override
  Future<Object?> upload(
    String path, {
    required List<int> bytes,
    required String filename,
    String field = 'file',
  }) async => {'path': path, 'filename': filename};
}
