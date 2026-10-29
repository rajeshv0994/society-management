import 'dart:convert';

Set<String> societyServicesFrom(Object? value) {
  if (value is List) return value.map((item) => item.toString()).toSet();
  try {
    final decoded = jsonDecode(value?.toString() ?? '');
    return decoded is List
        ? decoded.map((item) => item.toString()).toSet()
        : const {};
  } catch (_) {
    return const {};
  }
}
