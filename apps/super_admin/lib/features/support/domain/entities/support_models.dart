import '../../../../core/immutable_fields.dart';

class SupportTicket {
  SupportTicket(Map<String, dynamic> data) : data = ImmutableFields.from(data);
  final ImmutableFields data;
  String get id => data['id'].toString();
}

class SupportMessage {
  SupportMessage(Map<String, dynamic> data) : data = ImmutableFields.from(data);
  final ImmutableFields data;
}
