import 'dart:async';

import 'package:http/http.dart' as http;

enum AppFailureKind {
  serviceUnavailable,
  timeout,
  societyInactive,
  paymentRequired,
  societySuspended,
  authentication,
  authorization,
  validation,
  unknown,
}

class AppException implements Exception {
  const AppException(
    this.message, {
    this.kind = AppFailureKind.unknown,
    this.statusCode,
    this.retryable = false,
  });

  final String message;
  final AppFailureKind kind;
  final int? statusCode;
  final bool retryable;

  @override
  String toString() => message;
}

AppException appFailureFromResponse(String message, {int? statusCode}) {
  final normalized = message.trim();
  final lower = normalized.toLowerCase();
  if (lower.contains('society') &&
      (lower.contains('not active') || lower.contains('not activated'))) {
    return AppException(
      'This society is not active yet. Contact the platform administrator to activate it.',
      kind: AppFailureKind.societyInactive,
      statusCode: statusCode,
    );
  }
  if (lower.contains('membership') &&
      (lower.contains('payment') ||
          lower.contains('invoice') ||
          lower.contains('expired'))) {
    return AppException(
      'This society’s platform membership requires payment. Society Admin can sign in to review and pay the outstanding invoice.',
      kind: AppFailureKind.paymentRequired,
      statusCode: statusCode,
    );
  }
  if (lower.contains('membership') && lower.contains('suspend')) {
    return AppException(
      'This society’s platform membership is suspended. Contact platform support.',
      kind: AppFailureKind.societySuspended,
      statusCode: statusCode,
    );
  }
  if (statusCode == 502 || statusCode == 503 || statusCode == 504) {
    return AppException(
      'SocietyHub is temporarily unavailable. Please try again in a few moments.',
      kind: AppFailureKind.serviceUnavailable,
      statusCode: statusCode,
      retryable: true,
    );
  }
  return AppException(
    normalized.isEmpty ? 'The request could not be completed.' : normalized,
    kind: switch (statusCode) {
      401 => AppFailureKind.authentication,
      403 => AppFailureKind.authorization,
      400 || 409 || 422 => AppFailureKind.validation,
      _ => AppFailureKind.unknown,
    },
    statusCode: statusCode,
  );
}

AppException appFailureFromError(Object error) {
  if (error is AppException) return error;
  if (error is TimeoutException) {
    return const AppException(
      'The request timed out. Check your connection and try again.',
      kind: AppFailureKind.timeout,
      retryable: true,
    );
  }
  if (error is http.ClientException) {
    return const AppException(
      'Unable to reach SocietyHub. Check your internet connection or try again when the service is available.',
      kind: AppFailureKind.serviceUnavailable,
      retryable: true,
    );
  }
  if (error is FormatException) {
    return const AppException(
      'SocietyHub returned an invalid response. Please try again in a few moments.',
      kind: AppFailureKind.serviceUnavailable,
      retryable: true,
    );
  }
  return AppException(error.toString());
}

String userFacingError(Object error) => appFailureFromError(error).message;

bool isRetryableAppError(Object error) => appFailureFromError(error).retryable;
