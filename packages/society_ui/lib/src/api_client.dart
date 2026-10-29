import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:http_parser/http_parser.dart';
import 'app_failure.dart';
import 'session_manager.dart';

abstract class ApiGateway {
  String get baseUrl;
  Future<dynamic> get(String path);
  Future<dynamic> post(String path, [Object? body]);
  Future<dynamic> put(String path, [Object? body]);
  Future<dynamic> patch(String path, [Object? body]);
  Future<dynamic> delete(String path);
  Future<List<int>> getBytes(String path);
  Future<dynamic> upload(
    String path, {
    required List<int> bytes,
    required String filename,
    String field = 'file',
  });
}

/// Marker port for gateways whose request lifecycle is owned by a BLoC.
/// Shared workspace widgets require this type so a raw HTTP client cannot be
/// injected into presentation accidentally.
abstract interface class BlocManagedApiGateway implements ApiGateway {}

abstract class ApiClient implements ApiGateway {
  const ApiClient();
}

class HttpApiClient extends ApiClient {
  HttpApiClient({
    this.token,
    this.session,
    String? baseUrl,
    http.Client? httpClient,
    this.requestTimeout = const Duration(seconds: 20),
  }) : baseUrl =
           baseUrl ??
           const String.fromEnvironment(
             'API_BASE_URL',
             defaultValue: 'http://localhost:8080',
           ),
       _http = httpClient ?? http.Client(),
       super();

  final String? token;
  final SessionManager? session;
  final String baseUrl;
  final http.Client _http;
  final Duration requestTimeout;

  Future<Map<String, dynamic>> login(
    String email,
    String password, {
    String? societyCode,
  }) async {
    final value = await post('/api/auth/login', {
      'email': email,
      'password': password,
      if (societyCode != null && societyCode.trim().isNotEmpty)
        'societyCode': societyCode.trim().toUpperCase(),
    });
    return Map<String, dynamic>.from(value);
  }

  Future<dynamic> get(String path) => _send('GET', path);
  Future<dynamic> post(String path, [Object? body]) =>
      _send('POST', path, body);
  Future<dynamic> put(String path, [Object? body]) => _send('PUT', path, body);
  Future<dynamic> patch(String path, [Object? body]) =>
      _send('PATCH', path, body);
  Future<dynamic> delete(String path) => _send('DELETE', path);

  Future<List<int>> getBytes(String path) => _sendBytes(path);

  Future<dynamic> upload(
    String path, {
    required List<int> bytes,
    required String filename,
    String field = 'file',
  }) async {
    final request = http.MultipartRequest('POST', Uri.parse('$baseUrl$path'));
    request.headers['Accept'] = 'application/json';
    if (token != null) request.headers['Authorization'] = 'Bearer $token';
    request.files.add(
      http.MultipartFile.fromBytes(
        field,
        bytes,
        filename: filename,
        contentType: _contentType(filename),
      ),
    );
    _authorize(request);
    var response = await _response(request);
    if (response.statusCode == 401 && session != null) {
      await session!.refresh();
      final retry = http.MultipartRequest('POST', Uri.parse('$baseUrl$path'));
      retry.headers['Accept'] = 'application/json';
      _authorize(retry);
      retry.files.add(
        http.MultipartFile.fromBytes(
          field,
          bytes,
          filename: filename,
          contentType: _contentType(filename),
        ),
      );
      response = await _response(retry);
    }
    final value = _decode(response.body);
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw ApiException.fromFailure(
        appFailureFromResponse(
          value is Map
              ? value['message']?.toString() ?? 'Upload failed'
              : 'Upload failed',
          statusCode: response.statusCode,
        ),
      );
    }
    return value;
  }

  Future<dynamic> _send(
    String method,
    String path, [
    Object? body,
    bool retried = false,
  ]) async {
    final request = http.Request(method, Uri.parse('$baseUrl$path'));
    request.headers['Accept'] = 'application/json';
    _authorize(request);
    if (body != null) {
      request.headers['Content-Type'] = 'application/json';
      request.body = jsonEncode(body);
    }
    final response = await _response(request);
    if (response.statusCode == 401 && session != null && !retried) {
      await session!.refresh();
      return _send(method, path, body, true);
    }
    final value = _decode(response.body);
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw ApiException.fromFailure(
        appFailureFromResponse(
          value is Map
              ? value['message']?.toString() ?? 'Request failed'
              : 'Request failed',
          statusCode: response.statusCode,
        ),
      );
    }
    return value;
  }

  Future<List<int>> _sendBytes(String path, [bool retried = false]) async {
    final request = http.Request('GET', Uri.parse('$baseUrl$path'));
    _authorize(request);
    final response = await _response(request);
    if (response.statusCode == 401 && session != null && !retried) {
      await session!.refresh();
      return _sendBytes(path, true);
    }
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw ApiException.fromFailure(
        appFailureFromResponse(
          'Unable to download file',
          statusCode: response.statusCode,
        ),
      );
    }
    return response.bodyBytes;
  }

  void _authorize(http.BaseRequest request) {
    final accessToken = session?.accessToken ?? token;
    if (accessToken != null) {
      request.headers['Authorization'] = 'Bearer $accessToken';
    }
  }

  Future<http.Response> _response(http.BaseRequest request) async {
    try {
      return await _http
          .send(request)
          .then(http.Response.fromStream)
          .timeout(requestTimeout);
    } catch (error) {
      throw ApiException.fromFailure(appFailureFromError(error));
    }
  }

  Object? _decode(String body) {
    if (body.isEmpty) return null;
    try {
      return jsonDecode(body);
    } catch (error) {
      throw ApiException.fromFailure(appFailureFromError(error));
    }
  }

  MediaType _contentType(String filename) {
    final lower = filename.toLowerCase();
    if (lower.endsWith('.png')) return MediaType('image', 'png');
    if (lower.endsWith('.jpg') || lower.endsWith('.jpeg')) {
      return MediaType('image', 'jpeg');
    }
    if (lower.endsWith('.pdf')) return MediaType('application', 'pdf');
    return MediaType('application', 'octet-stream');
  }
}

class ApiException extends AppException {
  const ApiException(
    super.message, {
    super.statusCode,
    super.kind,
    super.retryable,
  });

  ApiException.fromFailure(AppException failure)
    : super(
        failure.message,
        statusCode: failure.statusCode,
        kind: failure.kind,
        retryable: failure.retryable,
      );
}
