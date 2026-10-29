import 'package:flutter/material.dart';
import 'package:society_ui/society_ui.dart';
import 'visitor_desk_page.dart';

void main() => runApp(
  const SocietyPortalApp(
    role: PortalRole.staff,
    brandColor: Color(0xff087e8b),
    modules: [
      PortalModule(
        'My profile',
        'Personal details, employment, permissions, attendance, and salary history.',
        Icons.person_outline,
        pageBuilder: _buildStaffProfile,
      ),
      PortalModule(
        'My attendance',
        'Check in and out, review attendance, and see admin corrections.',
        Icons.fact_check_outlined,
        pageBuilder: _buildStaffAttendance,
      ),
      PortalModule(
        'My duties',
        'Assigned tasks and today’s operational checklist.',
        Icons.task_alt,
        serviceCode: 'STAFF',
        pageBuilder: buildStaffTaskWorkspace,
        permissionCode: 'TASKS',
      ),
      PortalModule(
        'Visitor desk',
        'Capture visitor details, photo, and resident approval.',
        Icons.badge,
        serviceCode: 'VISITORS',
        pageBuilder: buildStaffVisitorDesk,
        permissionCode: 'VISITORS',
      ),
      PortalModule(
        'Complaints',
        'Raise resident complaints and track assigned work to resolution.',
        Icons.handyman,
        serviceCode: 'COMPLAINTS',
        pageBuilder: buildStaffComplaintWorkspace,
        permissionCode: 'COMPLAINTS',
      ),
      PortalModule(
        'Payments',
        'Record resident maintenance collections and issue receipts.',
        Icons.payments_outlined,
        serviceCode: 'PAYMENTS',
        pageBuilder: buildStaffMaintenanceWorkspace,
        permissionCode: 'PAYMENTS',
      ),
      PortalModule(
        'Amenities',
        'Manage bookings, payments, approvals, check-ins, and facility usage.',
        Icons.pool_outlined,
        serviceCode: 'AMENITIES',
        pageBuilder: buildStaffAmenityWorkspace,
        permissionCode: 'AMENITIES',
      ),
      PortalModule(
        'Activities & galleries',
        'Manage registrations, cash payments, attendance, and gallery submissions.',
        Icons.celebration_outlined,
        serviceCode: 'ACTIVITIES_GALLERIES',
        pageBuilder: buildStaffActivityWorkspace,
        permissionCode: 'ACTIVITIES_GALLERIES',
      ),
      PortalModule(
        'Households',
        'Verify residents, family members, vehicles, access, and emergency contacts.',
        Icons.home_work_outlined,
        serviceCode: 'RESIDENTS',
        pageBuilder: buildStaffHouseholdWorkspace,
        permissionCode: 'HOUSEHOLDS',
      ),
      PortalModule(
        'Notices',
        'Read staff announcements or publish notices with permission.',
        Icons.campaign_outlined,
        serviceCode: 'NOTICES',
        pageBuilder: buildStaffNoticeWorkspace,
        permissionCode: 'NOTICES',
      ),
      PortalModule(
        'Deliveries',
        'Track parcel arrival and resident collection.',
        Icons.local_shipping,
        serviceCode: 'DELIVERIES',
        pageBuilder: buildStaffDeliveryWorkspace,
        permissionCode: 'DELIVERIES',
      ),
      PortalModule(
        'Emergency',
        'Respond to incidents, coordinate services, and maintain the live timeline.',
        Icons.emergency,
        serviceCode: 'EMERGENCY_INCIDENTS',
        pageBuilder: buildStaffEmergencyWorkspace,
        permissionCode: 'EMERGENCY_INCIDENTS',
      ),
    ],
  ),
);

Widget _buildStaffProfile(BlocManagedApiGateway client) =>
    StaffProfileWorkspace(client: client);

Widget _buildStaffAttendance(BlocManagedApiGateway client) =>
    StaffAttendanceWorkspace(client: client);
