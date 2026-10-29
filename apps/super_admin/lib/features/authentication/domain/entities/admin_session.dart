class AdminSession {
  const AdminSession({
    required this.userId,
    required this.fullName,
    required this.role,
    this.societyId,
  });
  final String userId, fullName, role;
  final String? societyId;
}
