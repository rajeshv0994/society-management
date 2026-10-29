import 'package:flutter/material.dart';
import 'package:society_ui/society_ui.dart';
import 'visitor_approval_page.dart';

void main() => runApp(
  const SocietyPortalApp(
    role: PortalRole.member,
    brandColor: Color(0xffd14d25),
    modules: [
      PortalModule(
        'Home',
        'Space summary, announcements, and quick actions.',
        Icons.home,
        resource: PortalModuleResource.societyDashboard,
      ),
      PortalModule(
        'Payments',
        'Maintenance invoices, receipts, and payment history.',
        Icons.account_balance_wallet,
        serviceCode: 'PAYMENTS',
        pageBuilder: buildResidentMaintenanceWorkspace,
      ),
      PortalModule(
        'Complaints',
        'Raise and follow society service requests.',
        Icons.support,
        serviceCode: 'COMPLAINTS',
        pageBuilder: buildResidentComplaintWorkspace,
      ),
      PortalModule(
        'Visitors',
        'Approve gate visitors and review visitor history.',
        Icons.person_pin_circle,
        serviceCode: 'VISITORS',
        pageBuilder: buildVisitorApprovalPage,
      ),
      PortalModule(
        'Amenities',
        'View facilities and manage bookings.',
        Icons.pool,
        serviceCode: 'AMENITIES',
        pageBuilder: buildResidentAmenityWorkspace,
      ),
      PortalModule(
        'Activities & galleries',
        'Join free or paid society events and share approved gallery photos.',
        Icons.celebration_outlined,
        serviceCode: 'ACTIVITIES_GALLERIES',
        pageBuilder: buildResidentActivityWorkspace,
      ),
      PortalModule(
        'Deliveries',
        'Pre-register parcels, respond to arrivals, pay COD, and collect securely.',
        Icons.inventory_2_outlined,
        serviceCode: 'DELIVERIES',
        pageBuilder: buildResidentDeliveryWorkspace,
      ),
      PortalModule(
        'Notices',
        'Society announcements, meetings, and documents.',
        Icons.notifications,
        serviceCode: 'NOTICES',
        pageBuilder: buildResidentNoticeWorkspace,
      ),
      PortalModule(
        'My household',
        'Family, vehicles, tenancy, and emergency details.',
        Icons.family_restroom,
        serviceCode: 'RESIDENTS',
        pageBuilder: buildResidentHouseholdWorkspace,
      ),
      PortalModule(
        'Emergency',
        'Report an emergency or accident and track the society response.',
        Icons.sos_outlined,
        serviceCode: 'EMERGENCY_INCIDENTS',
        pageBuilder: buildResidentEmergencyWorkspace,
      ),
    ],
  ),
);
