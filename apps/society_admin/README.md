# SocietyHub Society Admin

The Society Admin Flutter application uses feature-first BLoC architecture. Pages retain only
ephemeral UI state; remote loading, mutation, errors, and revisions are owned by BLoCs.

```text
lib/
├── main.dart
├── app/
│   ├── app.dart                    Application root
│   └── dependency_injection.dart  Composition root
├── core/
│   ├── data/                       JSON resource models and API implementation
│   ├── domain/                     Resource entities, contracts, and use cases
│   └── presentation/               Shared BLoC state and widgets
└── features/
    ├── auth/                        Full data/domain/presentation slices
    ├── dashboard, profile, structure, residents, staff
    ├── configuration, operations, payments, documents
    ├── audit, storage, support      Dedicated feature BLoCs and pages
    └── shell/                       Navigation domain and composition UI
```

The administration APIs expose many heterogeneous document and collection shapes. Their common
transport is centralized as deeply immutable `ResourceDocument` / `ResourceCollection` models.
Each feature owns a dedicated BLoC and page, while auth has its own typed session and
public-society entities and repository.

```text
Page → Feature BLoC → Use case → Repository contract → Repository implementation → Data source → API
Page ← BLoC state  ← Use case ← Immutable resource entity ← JSON model      ← Data source ← API
```

Reusable `society_ui` workspaces depend on the `ApiGateway` port rather than `ApiClient`.
Society Admin supplies a `BlocApiGateway` backed by a dedicated feature BLoC, so their reads,
downloads, uploads, and mutations all participate in BLoC loading, failure, mutation, and revision
state. The concrete client exists only in the application composition and data-source layers.

Feature pages contain no API paths or generic resource requests/commands. Their public interaction
is through feature-named BLoC methods; endpoint selection and request construction live in the
feature BLoCs. Asynchronous command/query results are represented in immutable operation state and
awaited from the BLoC stream—events do not carry `Completer` objects.

Loads are versioned per resource key to prevent stale responses from overwriting newer state.
Overlapping mutations are counted, and authentication transitions are processed sequentially.

Covered capabilities include profile and subscription services, property structure, residents and
households, staff and permissions, configuration, billing-related workspaces, visitors and other
operations, documents, support chat, sensitive audit activity, and storage purchase/cleanup.

Platform membership supports usage-based and fixed custom-plan pricing, invoice PDFs, server-side
Razorpay checkout and captured-payment verification. When trial/membership access is expired or an
invoice is due unpaid, the shell exposes only the membership recovery workspace until payment
reactivates the society.

Architecture tests prevent pages from calling infrastructure, prevent remote `FutureBuilder` state,
keep domain code Flutter-free, and prevent legacy layer-first folders from returning.

## Run and verify

```bash
flutter pub get
flutter run -d chrome --dart-define=API_BASE_URL=http://localhost:8080
flutter analyze
flutter test
flutter build web --release
```
