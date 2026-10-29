# SocietyHub Member

Flutter application for authenticated residents (`RESIDENT`).

## Current capability

- JWT login, role validation, local session persistence, and logout
- Responsive navigation for web/mobile
- Society dashboard and entitlement-aware module navigation
- Maintenance payment, history, numbered receipt view, and PDF receipt download
- Complaint creation and progress timeline
- Visitor pre-registration, approval/rejection, cancellation, and history
- Amenity booking/payment and activity participation/gallery workflows
- Household members, vehicles, emergency contacts, tenancy/space context, and verification state
- Delivery, notice, and emergency/accident workflows

Production payment settlement, push/email delivery, and provider reconciliation remain external
integrations. The repository provides complete test checkout and internal ledger behavior.

See the repository [feature status](../../docs/FEATURE_STATUS.md#member-application) and
[API workflows](../../docs/API_AND_WORKFLOWS.md#operations-billing-and-member).

## Run and test

```bash
flutter pub get
flutter run -d chrome --dart-define=API_BASE_URL=http://localhost:8080
flutter analyze
flutter test
```
