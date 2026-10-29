enum AdminSection {
  dashboard,
  profile,
  platformMembership,
  structure,
  residents,
  households,
  staff,
  configuration,
  billing,
  payments,
  complaints,
  visitors,
  amenities,
  activities,
  deliveries,
  emergencies,
  notices,
  documents,
  tasks,
  audit,
  storage,
  support,
}

extension AdminSectionEntitlement on AdminSection {
  bool isAvailable(Set<String> modules) => switch (this) {
    AdminSection.dashboard ||
    AdminSection.profile ||
    AdminSection.platformMembership ||
    AdminSection.structure ||
    AdminSection.audit ||
    AdminSection.storage ||
    AdminSection.support => true,
    AdminSection.residents ||
    AdminSection.households => modules.contains('RESIDENTS'),
    AdminSection.staff || AdminSection.tasks => modules.contains('STAFF'),
    AdminSection.configuration => modules.any(
      const {'PARKING', 'VENDORS', 'COMPLAINTS', 'BILLING'}.contains,
    ),
    AdminSection.billing => modules.contains('BILLING'),
    AdminSection.payments => modules.contains('PAYMENTS'),
    AdminSection.complaints => modules.contains('COMPLAINTS'),
    AdminSection.visitors => modules.contains('VISITORS'),
    AdminSection.amenities => modules.contains('AMENITIES'),
    AdminSection.activities => modules.contains('ACTIVITIES_GALLERIES'),
    AdminSection.deliveries => modules.contains('DELIVERIES'),
    AdminSection.emergencies => modules.contains('EMERGENCY_INCIDENTS'),
    AdminSection.notices => modules.contains('NOTICES'),
    AdminSection.documents => modules.contains('DOCUMENTS'),
  };
}
