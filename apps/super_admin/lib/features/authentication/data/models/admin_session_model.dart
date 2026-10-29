import 'package:society_ui/society_ui.dart';
import '../../domain/entities/admin_session.dart';

class AdminSessionModel extends AdminSession {
  const AdminSessionModel({
    required super.userId,
    required super.fullName,
    required super.role,
    super.societyId,
  });
  factory AdminSessionModel.fromSession(SessionData session) =>
      AdminSessionModel(
        userId: session.userId,
        fullName: session.fullName,
        role: session.role,
        societyId: session.societyId,
      );
}
