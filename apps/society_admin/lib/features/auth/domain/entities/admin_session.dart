class AdminSession {
  const AdminSession({
    required this.userId,
    required this.fullName,
    required this.role,
    required this.expiresIn,
    this.societyId,
  });

  final String userId;
  final String fullName;
  final String role;
  final int expiresIn;
  final String? societyId;

  Map<String, dynamic> get profile => Map.unmodifiable({
    'userId': userId,
    'fullName': fullName,
    'role': role,
    'expiresIn': expiresIn,
    if (societyId != null) 'societyId': societyId,
  });
}

class PublicSociety {
  const PublicSociety({required this.code, required this.name});

  final String code;
  final String name;
}
