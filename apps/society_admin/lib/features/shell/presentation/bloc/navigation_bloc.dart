import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/entities/admin_section.dart';

extension Info on AdminSection {
  String get label => switch (this) {
    AdminSection.dashboard => 'Dashboard',
    AdminSection.profile => 'Society profile',
    AdminSection.platformMembership => 'Platform membership',
    AdminSection.structure => 'Property spaces',
    AdminSection.residents => 'Residents',
    AdminSection.households => 'Households',
    AdminSection.staff => 'Staff',
    AdminSection.configuration => 'Configuration',
    AdminSection.billing => 'Billing',
    AdminSection.payments => 'Payments',
    AdminSection.complaints => 'Complaints',
    AdminSection.visitors => 'Visitors',
    AdminSection.amenities => 'Amenities',
    AdminSection.activities => 'Activities & galleries',
    AdminSection.deliveries => 'Deliveries',
    AdminSection.emergencies => 'Emergency & accidents',
    AdminSection.notices => 'Notices',
    AdminSection.documents => 'Documents',
    AdminSection.tasks => 'Staff tasks',
    AdminSection.audit => 'Sensitive activity',
    AdminSection.storage => 'Storage',
    AdminSection.support => 'Platform support',
  };
  IconData get icon => switch (this) {
    AdminSection.dashboard => Icons.dashboard,
    AdminSection.profile => Icons.business,
    AdminSection.platformMembership => Icons.workspace_premium_outlined,
    AdminSection.structure => Icons.account_tree,
    AdminSection.residents => Icons.people,
    AdminSection.households => Icons.home_work_outlined,
    AdminSection.staff => Icons.badge,
    AdminSection.configuration => Icons.settings,
    AdminSection.billing => Icons.receipt_long,
    AdminSection.payments => Icons.payments,
    AdminSection.complaints => Icons.handyman,
    AdminSection.visitors => Icons.door_front_door,
    AdminSection.amenities => Icons.pool,
    AdminSection.activities => Icons.celebration_outlined,
    AdminSection.deliveries => Icons.inventory_2_outlined,
    AdminSection.emergencies => Icons.sos_outlined,
    AdminSection.notices => Icons.campaign,
    AdminSection.documents => Icons.folder_copy,
    AdminSection.tasks => Icons.task_alt,
    AdminSection.audit => Icons.policy_outlined,
    AdminSection.storage => Icons.cloud_outlined,
    AdminSection.support => Icons.support_agent,
  };
}

class Navigate {
  const Navigate(this.section);
  final AdminSection section;
}

class NavigationBloc extends Bloc<Navigate, AdminSection> {
  NavigationBloc() : super(AdminSection.dashboard) {
    on<Navigate>((e, emit) => emit(e.section));
  }
}
