# SocietyHub Staff

Flutter application for society staff (`STAFF`).

## Current capability

- JWT login, role validation, local session persistence, and logout
- Responsive, service-entitlement and permission-aware navigation for web/mobile
- Staff profile, attendance, assigned task progress/comments/completion
- Visitor registration, arrival photos, approval, check-in, and check-out
- Complaint creation, assignment, progress comments, and resolution
- Maintenance collection with numbered receipts and PDF download
- Amenities, activities, household verification, notices, deliveries, and emergency workflows

Real payment settlement, outbound notifications, and push delivery are external production
integrations. The repository provides the complete local/test workflow and internal ledgers.

See the repository [feature status](../../docs/FEATURE_STATUS.md#staff-application) and
[API workflows](../../docs/API_AND_WORKFLOWS.md#operations-billing-and-member).

## Run and test

```bash
flutter pub get
flutter run -d chrome --dart-define=API_BASE_URL=http://localhost:8080
flutter analyze
flutter test
```
