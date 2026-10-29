class ResidentCreateInput {
  const ResidentCreateInput({
    required this.spaceId,
    required this.residentType,
    required this.fullName,
    required this.mobile,
    required this.email,
    required this.temporaryPassword,
    this.moveInDate,
  });

  final String spaceId;
  final String residentType;
  final String fullName;
  final String mobile;
  final String email;
  final String temporaryPassword;
  final String? moveInDate;
}
