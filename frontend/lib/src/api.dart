import 'dart:async';
import 'dart:convert';
import 'package:http/http.dart' as http;

class ApiClient {
  final String? token;
  final String baseUrl;
  final Duration requestTimeout;
  final http.Client _http;
  ApiClient({
    this.token,
    String? baseUrl,
    http.Client? httpClient,
    this.requestTimeout = const Duration(seconds: 20),
  }) : _http = httpClient ?? http.Client(),
       baseUrl =
           baseUrl ??
           const String.fromEnvironment(
             'API_BASE_URL',
             defaultValue: 'http://localhost:8080',
           );
  Future<dynamic> post(String path, Map<String, dynamic> body) async {
    final r = await _response(
      _http.post(
        Uri.parse('$baseUrl$path'),
        headers: {
          'Content-Type': 'application/json',
          if (token != null) 'Authorization': 'Bearer $token',
        },
        body: jsonEncode(body),
      ),
    );
    final data = _decode(r.body);
    if (r.statusCode < 200 || r.statusCode >= 300) {
      throw ApiException(
        _serverMessage(
          data is Map ? data['message']?.toString() : null,
          r.statusCode,
        ),
      );
    }
    return data;
  }

  Future<dynamic> get(String path) async {
    final r = await _response(
      _http.get(
        Uri.parse('$baseUrl$path'),
        headers: {if (token != null) 'Authorization': 'Bearer $token'},
      ),
    );
    final data = _decode(r.body);
    if (r.statusCode >= 400) {
      throw ApiException(
        _serverMessage(
          data is Map ? data['message']?.toString() : null,
          r.statusCode,
        ),
      );
    }
    return data;
  }

  Future<http.Response> _response(Future<http.Response> request) async {
    try {
      return await request.timeout(requestTimeout);
    } on TimeoutException {
      throw ApiException(
        'The request timed out. Check your connection and try again.',
      );
    } on http.ClientException {
      throw ApiException(
        'Unable to reach SocietyHub. Check your internet connection or try again when the service is available.',
      );
    }
  }

  Object _decode(String body) {
    if (body.isEmpty) return <String, dynamic>{};
    try {
      return jsonDecode(body) as Object;
    } on FormatException {
      throw ApiException(
        'SocietyHub returned an invalid response. Please try again in a few moments.',
      );
    }
  }

  String _serverMessage(String? message, int statusCode) {
    final value = message?.trim() ?? '';
    final lower = value.toLowerCase();
    if (lower.contains('society') &&
        (lower.contains('not active') || lower.contains('not activated'))) {
      return 'This society is not active yet. Contact the platform administrator to activate it.';
    }
    if (statusCode == 502 || statusCode == 503 || statusCode == 504) {
      return 'SocietyHub is temporarily unavailable. Please try again in a few moments.';
    }
    return value.isEmpty ? 'The request could not be completed.' : value;
  }
}

class ApiException implements Exception {
  final String message;
  ApiException(this.message);
  @override
  String toString() => message;
}
