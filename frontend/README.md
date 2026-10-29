# SocietyHub Public Onboarding

Public Flutter application for plan discovery, society onboarding, activation, custom
requirements, resident registration, and login.

## Current capability

- Responsive public marketing website with service catalog
- Live subscription plans loaded from the backend
- Portal launcher for Member, Society Admin, Staff, and Platform Admin applications
- Public plan display
- Society registration with plan selection
- Society Admin activation using a one-time setup code
- Custom requirement request
- Owner/tenant registration against a society space
- Login handoff

Real email/mobile OTP verification, password recovery, and a fully validated onboarding
document-upload experience remain pending.

See the repository
[feature status](../docs/FEATURE_STATUS.md#public-onboarding-application) and
[society/resident workflows](../docs/API_AND_WORKFLOWS.md#core-workflows).

## Run and test

```bash
flutter pub get
flutter run -d chrome \
  --dart-define=API_BASE_URL=http://localhost:8080 \
  --dart-define=SUPER_ADMIN_APP_URL=http://localhost:8081 \
  --dart-define=SOCIETY_ADMIN_APP_URL=http://localhost:8082 \
  --dart-define=STAFF_APP_URL=http://localhost:8083 \
  --dart-define=MEMBER_APP_URL=http://localhost:8084
flutter analyze
flutter test
```

## Portal URL configuration

| Build variable | Default | Application |
| --- | --- | --- |
| `SUPER_ADMIN_APP_URL` | `http://localhost:8081` | Platform Admin |
| `SOCIETY_ADMIN_APP_URL` | `http://localhost:8082` | Society Admin |
| `STAFF_APP_URL` | `http://localhost:8083` | Staff |
| `MEMBER_APP_URL` | `http://localhost:8084` | Member |

Set these values to the deployed portal URLs during `flutter build web`. Portal cards open
the configured application in the current browser tab.
