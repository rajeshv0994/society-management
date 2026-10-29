import 'dart:async';
import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'app_failure.dart';

class SessionData {
  const SessionData({
    required this.accessToken,
    required this.refreshToken,
    required this.expiresIn,
    required this.userId,
    required this.fullName,
    required this.role,
    this.societyId,
  });

  factory SessionData.fromMap(Map<String, dynamic> value) => SessionData(
    accessToken: (value['accessToken'] ?? value['token']).toString(),
    refreshToken: value['refreshToken'].toString(),
    expiresIn: (value['expiresIn'] as num?)?.toInt() ?? 0,
    userId: value['userId'].toString(),
    fullName: value['fullName'].toString(),
    role: value['role'].toString(),
    societyId: value['societyId']?.toString(),
  );

  factory SessionData.fromJson(String source) =>
      SessionData.fromMap(Map<String, dynamic>.from(jsonDecode(source) as Map));

  final String accessToken;
  final String refreshToken;
  final int expiresIn;
  final String userId;
  final String fullName;
  final String role;
  final String? societyId;

  Map<String, dynamic> toMap() => {
    'accessToken': accessToken,
    'refreshToken': refreshToken,
    'expiresIn': expiresIn,
    'userId': userId,
    'fullName': fullName,
    'role': role,
    if (societyId != null) 'societyId': societyId,
  };
}

class SessionManager extends ChangeNotifier {
  SessionManager({
    required this.storageKey,
    required this.expectedRole,
    String? baseUrl,
    http.Client? httpClient,
    this.requestTimeout = const Duration(seconds: 20),
  }) : baseUrl =
           baseUrl ??
           const String.fromEnvironment(
             'API_BASE_URL',
             defaultValue: 'http://localhost:8080',
           ),
       _http = httpClient ?? http.Client();

  final String storageKey;
  final String expectedRole;
  final String baseUrl;
  final http.Client _http;
  final Duration requestTimeout;
  SessionData? _current;
  Future<SessionData>? _refreshing;

  SessionData? get current => _current;
  String? get accessToken => _current?.accessToken;
  bool get isAuthenticated => _current != null;

  Future<SessionData?> restore() async {
    final preferences = await SharedPreferences.getInstance();
    final stored = preferences.getString('${storageKey}_session');
    if (stored == null) {
      await _removeLegacyValues(preferences);
      return null;
    }
    try {
      _current = SessionData.fromJson(stored);
      _validateRole(_current!);
      return await refresh();
    } on SessionException catch (error) {
      if (error.retryable) {
        notifyListeners();
        return _current;
      }
      await _revokeBestEffort(_current?.refreshToken);
      await clear();
      return null;
    } catch (_) {
      // Preserve a previously valid session during a temporary network outage.
      notifyListeners();
      return _current;
    }
  }

  Future<SessionData> login(
    String email,
    String password, {
    String? societyCode,
  }) async {
    final response = await _post('/api/auth/login', {
      'email': email.trim(),
      'password': password,
      if (societyCode != null && societyCode.trim().isNotEmpty)
        'societyCode': societyCode.trim().toUpperCase(),
    });
    final session = SessionData.fromMap(response);
    try {
      _validateRole(session);
    } on SessionException {
      await _revokeBestEffort(session.refreshToken);
      rethrow;
    }
    await _save(session);
    return session;
  }

  Future<SessionData> refresh() {
    final active = _refreshing;
    if (active != null) return active;
    final operation = _performRefresh();
    _refreshing = operation;
    unawaited(
      operation.then<void>(
        (_) {
          if (identical(_refreshing, operation)) _refreshing = null;
        },
        onError: (Object _, StackTrace _) {
          if (identical(_refreshing, operation)) _refreshing = null;
        },
      ),
    );
    return operation;
  }

  Future<SessionData> _performRefresh() async {
    final refreshToken = _current?.refreshToken;
    if (refreshToken == null || refreshToken.isEmpty) {
      throw const SessionException('Your session has expired.');
    }
    try {
      final response = await _post('/api/auth/refresh', {
        'refreshToken': refreshToken,
      });
      final session = SessionData.fromMap(response);
      _validateRole(session);
      await _save(session);
      return session;
    } on SessionException {
      await clear();
      rethrow;
    }
  }

  Future<void> logout() async {
    final refreshToken = _current?.refreshToken;
    try {
      if (refreshToken != null) {
        await _post('/api/auth/logout', {
          'refreshToken': refreshToken,
        }, allowEmpty: true);
      }
    } catch (_) {
      // Local logout must still complete if the API is temporarily unavailable.
    } finally {
      await clear();
    }
  }

  Future<void> logoutAll() async {
    final accessToken = _current?.accessToken;
    try {
      if (accessToken != null) {
        await _post(
          '/api/auth/logout-all',
          const {},
          accessToken: accessToken,
          allowEmpty: true,
        );
      }
    } catch (_) {
      // Local logout must still complete if the API is temporarily unavailable.
    } finally {
      await clear();
    }
  }

  Future<void> clear() async {
    _current = null;
    final preferences = await SharedPreferences.getInstance();
    await preferences.remove('${storageKey}_session');
    await _removeLegacyValues(preferences);
    notifyListeners();
  }

  Future<void> _save(SessionData session) async {
    _current = session;
    final preferences = await SharedPreferences.getInstance();
    await preferences.setString(
      '${storageKey}_session',
      jsonEncode(session.toMap()),
    );
    await _removeLegacyValues(preferences);
    notifyListeners();
  }

  Future<void> _removeLegacyValues(SharedPreferences preferences) async {
    await preferences.remove('${storageKey}_token');
    await preferences.remove('${storageKey}_name');
  }

  void _validateRole(SessionData session) {
    if (session.role != expectedRole) {
      throw SessionException(
        'This account belongs to the ${session.role} portal.',
      );
    }
  }

  Future<void> _revokeBestEffort(String? refreshToken) async {
    if (refreshToken == null || refreshToken.isEmpty) return;
    try {
      await _post('/api/auth/logout', {
        'refreshToken': refreshToken,
      }, allowEmpty: true);
    } catch (_) {
      // The role is still rejected locally if revocation cannot reach the API.
    }
  }

  Future<Map<String, dynamic>> _post(
    String path,
    Map<String, dynamic> body, {
    String? accessToken,
    bool allowEmpty = false,
  }) async {
    late http.Response response;
    try {
      response = await _http
          .post(
            Uri.parse('$baseUrl$path'),
            headers: {
              'Content-Type': 'application/json',
              'Accept': 'application/json',
              if (accessToken != null) 'Authorization': 'Bearer $accessToken',
            },
            body: jsonEncode(body),
          )
          .timeout(requestTimeout);
    } catch (error) {
      throw SessionException.fromFailure(appFailureFromError(error));
    }
    Map<String, dynamic> value;
    try {
      value = response.body.isEmpty
          ? <String, dynamic>{}
          : Map<String, dynamic>.from(jsonDecode(response.body) as Map);
    } catch (error) {
      throw SessionException.fromFailure(appFailureFromError(error));
    }
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw SessionException.fromFailure(
        appFailureFromResponse(
          value['message']?.toString() ?? 'Authentication request failed.',
          statusCode: response.statusCode,
        ),
      );
    }
    if (!allowEmpty && value.isEmpty) {
      throw const SessionException('Authentication response was empty.');
    }
    return value;
  }

  @override
  void dispose() {
    _http.close();
    super.dispose();
  }
}

class SessionException extends AppException {
  const SessionException(
    super.message, {
    super.statusCode,
    super.kind,
    super.retryable,
  });

  SessionException.fromFailure(AppException failure)
    : super(
        failure.message,
        statusCode: failure.statusCode,
        kind: failure.kind,
        retryable: failure.retryable,
      );
}
