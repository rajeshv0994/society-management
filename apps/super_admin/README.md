# SocietyHub Super Admin

The Super Admin frontend uses feature-oriented BLoC architecture for every remote state and
mutation.

```text
lib/
├── main.dart
├── app/
│   ├── app.dart                  Application root and theme
│   └── dependency_injection.dart Composition root
├── core/
│   └── presentation/widgets/     Truly shared UI primitives
└── features/
    ├── authentication/
    ├── dashboard/
    ├── societies/
    ├── services/
    ├── requirements/
    ├── billing/
    ├── management/
    ├── support/
    ├── audit/
    ├── storage/
    └── shell/                     Local navigation domain + presentation

Each remote feature owns:

feature/
├── data/
│   ├── datasources/               Feature API endpoints
│   ├── models/                    DTO-to-entity mapping
│   └── repositories/              Repository implementations
├── domain/
│   ├── entities/                  Typed feature state and commands
│   ├── repositories/              Abstract contracts
│   └── usecases/                  Application operations
└── presentation/
    ├── bloc/                      Events, state and orchestration
    └── pages/                     UI and ephemeral form state
```

Dashboard cards are interactive. They dispatch navigation events to the appropriate live-data section, including filtered pending society approvals.

```text
Page → Event → Feature BLoC → Use case → Repository contract → Repository implementation → Feature data source → ApiClient
Page ← State ← Feature BLoC ← Use case ← Domain entity ← DTO model ← Feature data source ← ApiClient
```

- `AuthBloc`: login, restore, logout and session state
- `NavigationBloc`: selected workspace and dashboard navigation intent
- `DashboardBloc`: platform metrics
- `SocietiesBloc`: society list, filters, approval and rejection
- `SupportBloc`: ticket selection, messages, replies and resolution
- Dedicated `ServiceCatalogBloc`, `StorageBloc`, `RequirementsBloc`, `BillingBloc`,
  `ManagementBloc`, `AuditBloc`, and `SocietyOperationsBloc` own their feature queries and
  mutations. There is no cross-feature operations BLoC.

Views retain only ephemeral form state such as text controllers, selected tabs and dialog
values. Architecture tests prevent feature pages from calling the remote data source or
`ApiClient`, require BLoCs to enter through use cases, keep domain code independent from Flutter
and the data layer, verify every remote feature has all eight architecture folders, and prevent
`FutureBuilder` from owning remote state.

## Current capability

- Platform dashboard and society search/filter/detail
- Society creation/editing, logo, approval/rejection, and document review
- Plan management, order ledger, notification retry, and audit view
- Platform membership invoice issue/review, eligible Due-invoice cancellation/reissue, Razorpay attempt visibility, and reconciliation
- Storage free-allowance, paid-block pricing, and per-society usage management
- Requirement review and editable/cancellable custom-plan lifecycle with usage/fixed pricing and safe activation history
- Support queue, resolution, and chat

Platform membership has verified Razorpay order/capture/webhook support; live settlement requires
production credentials, Dashboard webhook registration, and operational reconciliation.
Society-level connected-account settlement and notification delivery remain external.
Reporting/export and fine-grained administrator permissions are pending.

See the repository
[feature status](../../docs/FEATURE_STATUS.md#super-admin-application) and
[API workflows](../../docs/API_AND_WORKFLOWS.md#super-admin).

## Run and test

```bash
flutter pub get
flutter run -d chrome --dart-define=API_BASE_URL=http://localhost:8080
flutter analyze
flutter test
```
