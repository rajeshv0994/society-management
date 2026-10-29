# SocietyHub

Four independently deployable Flutter portals with a Spring Boot and PostgreSQL backend for a multi-tenant society management SaaS.

## Documentation

- [Documentation index](docs/README.md)
- [Complete application architecture diagram](docs/COMPLETE_APPLICATION_DIAGRAM.md)
- [Complete application flow and database structure](docs/COMPLETE_APPLICATION_FLOW_AND_DATABASE.md)
- [Feature status: done, partial, and pending](docs/FEATURE_STATUS.md)
- [Current and target architecture](docs/ARCHITECTURE.md)
- [Authentication and session lifecycle](docs/AUTHENTICATION.md)
- [Shared website and application design system](docs/DESIGN_SYSTEM.md)
- [API and business workflows](docs/API_AND_WORKFLOWS.md)
- [Development, testing, deployment, and production checklist](docs/DEVELOPMENT_AND_OPERATIONS.md)

The feature status is the authoritative implementation ledger. Staff and Member include their
documented role-scoped mutations, and the backend enforces Clean Architecture boundaries for
its domain, application ports/use cases, and infrastructure adapters.

## Frontend applications

| Application | Folder | Required role | Main responsibilities |
| --- | --- | --- | --- |
| Platform Control | `apps/super_admin` | `SUPER_ADMIN` | Society approval, subscriptions, billing, monitoring, support |
| Society Administration | `apps/society_admin` | `SOCIETY_ADMIN` | Structure, residents, staff, billing, complaints, visitors, notices |
| Staff Workspace | `apps/staff` | `STAFF` | Duties, visitor desk, complaints, deliveries, incidents |
| Member Home | `apps/member` | `RESIDENT` | Payments, complaints, visitors, amenities, notices, household |

The applications share API/authentication and design primitives from `packages/society_ui`, but have separate entry points, bundles, navigation, session storage, role checks, Android/iOS identifiers, and web deployments. The earlier `frontend` folder remains the public society/resident onboarding application.

The public Flutter website also provides the service catalog, live plan display, onboarding
actions, and configurable launch links for all four role-specific applications.

## Implemented workflow

- Society self-registration with automatic approval for Trial/Basic and manual approval for higher plans
- Super Admin-controlled per-space and per-staff prices, quantity limits, and trial duration
- Expiring, one-time setup codes and Society Admin activation
- JWT access authentication, rotating refresh sessions, logout/revocation, and role validation
- Multi-tenant society isolation
- Property group → optional section → space configuration
- Resident owner/tenant self-registration and admin approval/rejection
- Space occupancy enforcement (one active owner and one active tenant)
- Platform and society operational dashboards
- Staff accounts, profiles, shifts, duty areas, and assigned tasks
- Society notices with audience and expiry controls
- Complaints with assignment, priority, lifecycle, and resolution
- Visitor pre-approval, check-in, and check-out lifecycle
- Amenities with collision-safe booking and calculated charges
- Maintenance invoices, partial/full payment ledger, and space ownership checks
- Platform-membership Razorpay orders, captured-payment verification, signed idempotent webhooks,
  invoice PDFs, and Super Admin reconciliation
- Request-time trial/payment entitlement enforcement with Society Admin payment-only recovery
- Member profiles and household members
- Flyway-managed PostgreSQL schema
- Responsive Flutter web/mobile shell with onboarding, activation, login, and dashboard

Documents and society logos use local volume-backed storage in development. Connect S3/MinIO
before production deployment. Platform-membership payments have a Razorpay adapter and verified
webhook; live charging requires production credentials and webhook configuration. Society-level
maintenance and operational payments remain an internal/test ledger pending a connected-account
settlement design. Email/SMS/WhatsApp delivery and scheduled move-out/lease-expiry jobs remain
external integrations.

## Backend modules

The Spring Boot API in [`backend/`](backend/) includes JWT security, BCrypt credentials, method-level role authorization, transaction boundaries, society-level tenant filtering, PostgreSQL constraints, indexes, and versioned Flyway migrations. See the [backend development guide](backend/README.md) for its package structure, layer rules, configuration, migrations, Swagger usage, and feature-development convention.

Primary API groups are:

- `/api/auth`, `/api/dashboard`
- `/api/public/societies`, `/api/admin/societies`
- `/api/society/structure`, `/api/society/residents`, `/api/society/staff`
- `/api/tasks`, `/api/complaints`, `/api/notices`, `/api/visitors`
- `/api/amenities`, `/api/billing`, `/api/member`

## Swagger / OpenAPI

After starting the backend, interactive Swagger documentation is available at:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Complete OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Complete OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`
- Explicit complete API group: `http://localhost:8080/v3/api-docs/complete-api`

Swagger UI provides Complete API, Public/Auth, Super Admin, Society Admin, Staff, and Member groups. For secured requests, log in through `POST /api/auth/login`, copy the returned `token`, select **Authorize**, and paste the token. The UI supplies the `Bearer` prefix.

## Run locally

Requirements: Docker and Flutter 3.41+.

```bash
docker compose up --build -d
cd apps/super_admin # or society_admin, staff, member
flutter run -d chrome --dart-define=API_BASE_URL=http://localhost:8080
```

Docker publishes PostgreSQL on host port `5544` by default to avoid conflicts with locally installed PostgreSQL servers. Containers still use `postgres:5432`. Override the host port with `POSTGRES_PORT=<port> docker compose up --build -d` when needed.

### Headless Docker on macOS (Colima)

Docker Desktop is not required. Colima provides the Docker runtime as a macOS
background service:

```bash
brew install colima docker docker-compose docker-buildx
brew services start colima
docker context use colima
docker compose up --build -d
```

The Compose services use `restart: unless-stopped`, so PostgreSQL and the backend
restart automatically whenever the Colima service starts. Use
`brew services stop colima` when the runtime should be stopped intentionally.

Local development test credentials:

- Super Admin: `admin@society.local` / `Admin@12345`
- Society code for the accounts below: `GREEN-VALLEY`
- Society Admin: `green.admin@society.local` / `Admin@12345`
- Staff: `staff.green@society.local` / `Staff@12345`
- Member: `member.green@society.local` / `Member@12345`

These accounts are development-only. Change or disable bootstrap credentials outside local
development.

Stop services with `docker compose down`. Use `docker compose down -v` only when you intentionally want to delete all local database data.

Run the complete cross-role API workflow suite against a disposable fresh database:

```bash
scripts/run_complete_app_e2e.sh
```

It writes machine-readable evidence to `build/reports/complete_app_flow_smoke.json` and cleans up
its temporary containers automatically. The maintained success, failure, retry, concurrency, and
external-provider scenarios are listed in
[`docs/COMPLETE_APP_TEST_CASES.md`](docs/COMPLETE_APP_TEST_CASES.md).

## API flow

1. `POST /api/public/societies/register`
2. For manual plans, Super Admin logs in and calls `POST /api/admin/societies/{id}/approve`
3. Copy the returned setup code once and call `POST /api/public/societies/activate`
4. Society Admin logs in and creates property groups, optional sections, and spaces under `/api/society/structure`
5. Resident submits `POST /api/public/residents/register`
6. Society Admin reviews it with `POST /api/society/residents/{id}/review`

Public discovery endpoints:

- `GET /api/public/plans`

Platform charges use `(base space price × selected spaces) + (staff price × selected staff)
+ Σ(selected paid-service price × selected spaces)`. Super Admin marks each catalog service as
default-free, optional-free, or paid per space. Default services are included for every society;
Society Admin selects optional services from the subscription requirements screen.
Trial limits and duration are configured by Super Admin. Duration can be set in
days or months; the default migration sets Trial to 10 spaces, 2 staff seats,
and 1 month.
Registration and custom-plan requests capture both quantities. Society Admin can
later change purchased spaces and staff seats from **Society profile → Subscription
quantities**. Quantities cannot be reduced below configured spaces or active staff,
and new space/staff creation is blocked when purchased capacity is reached.

An active custom plan may instead use a fixed monthly base. Legacy custom plans that store this
amount in `price_monthly` are preserved: the fixed base is included once per month, space/staff
rates display as included, and paid service/storage add-ons remain separate lines.

Trial expiry, pending initial payment, membership expiry, and unpaid invoices on or after their
due date block society operations at request time, including requests made with an existing JWT.
Staff and Members cannot log in or refresh while blocked. Society Admin retains only profile-read
and platform-membership payment access so the society can recover. A captured, verified payment
reactivates the membership period.

Billing configuration is quantity-based. Super Admin manages only **Trial
settings** and **Paid usage rates**; legacy Basic, Premium, and Enterprise tiers
are retired from registration and administration.

- `GET /api/public/societies`
- `GET /api/public/societies/{societyId}/spaces`
- `GET /api/public/societies/{societyId}/units` (legacy alias)
- `GET /api/public/societies/{societyId}/flats` (legacy alias)

## Repository layout

```text
backend/   Spring Boot 4 / Java 21 API
frontend/  Public society and resident onboarding application
apps/      Four role-specific Flutter applications
packages/  Shared Flutter API and design package
docker-compose.yml  PostgreSQL and API development stack
```

## Production checklist

- Put secrets in a secret manager and rotate bootstrap credentials
- Add S3/MinIO signed uploads, malware scanning, and document retention rules
- Add email/mobile OTP rather than marking activation contacts verified
- Add rate limiting, tamper-evident external audit archival, account lockout, and mobile secure token storage
- Configure live Razorpay credentials/webhooks and complete society-level connected-account settlement
- Add database row-level tenant tests, backups, and observability; CI/CD is defined under `.github/workflows`
- Store Aadhaar/PAN only after a privacy/security review; encrypt sensitive fields and minimize retention
