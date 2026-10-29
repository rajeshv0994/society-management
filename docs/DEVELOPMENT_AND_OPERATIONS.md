# Development and Operations

## Prerequisites

- Docker with Compose, or Java 21 + Maven 3.9+ + PostgreSQL 16
- Flutter 3.41+ for frontend development

## Quick start

From the repository root:

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f backend
```

Local services:

| Service | Address |
| --- | --- |
| API | `http://localhost:8080` |
| Health | `http://localhost:8080/actuator/health` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| PostgreSQL from host | `localhost:5544` |

Run one Flutter application:

```bash
cd apps/society_admin
flutter pub get
flutter run -d chrome --dart-define=API_BASE_URL=http://localhost:8080
```

Use `frontend`, `apps/super_admin`, `apps/staff`, or `apps/member` in place of
`apps/society_admin` for another entry point.

To run every web application on a separate local port:

```bash
cd apps/super_admin && flutter run -d web-server --web-port=8081
cd apps/society_admin && flutter run -d web-server --web-port=8082
cd apps/staff && flutter run -d web-server --web-port=8083
cd apps/member && flutter run -d web-server --web-port=8084
cd frontend && flutter run -d web-server --web-port=8090 \
  --dart-define=API_BASE_URL=http://localhost:8080 \
  --dart-define=SUPER_ADMIN_APP_URL=http://localhost:8081 \
  --dart-define=SOCIETY_ADMIN_APP_URL=http://localhost:8082 \
  --dart-define=STAFF_APP_URL=http://localhost:8083 \
  --dart-define=MEMBER_APP_URL=http://localhost:8084
```

Run the commands in separate terminals. For production, pass the four deployed portal URLs
to the public frontend build using the same `--dart-define` names.

Stop without deleting persistent data:

```bash
docker compose down
```

`docker compose down -v` deletes the local database and uploads volumes and should only be
used for an intentional reset.

## Local credentials

When the Compose development seed is enabled:

- Super Admin: `admin@society.local` / `Admin@12345`
- Demo Society Admin: `green.admin@society.local` / `Admin@12345`
- Demo Staff: `staff.green@society.local` / `Staff@12345`
- Demo Member: `member.green@society.local` / `Member@12345`

These values are development-only and must never be deployed.

## Backend configuration

| Environment variable | Default | Purpose |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `local` in development Compose | Select `local` development defaults or production startup safeguards |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/societydb` | PostgreSQL URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | local development value | HMAC signing key |
| `JWT_EXPIRATION` | `900000` | Access-token lifetime in milliseconds (15 minutes) |
| `JWT_REFRESH_EXPIRATION_DAYS` | `30` | Refresh-session lifetime in days |
| `SUPER_ADMIN_EMAIL` | `admin@society.local` | Bootstrap admin email |
| `SUPER_ADMIN_PASSWORD` | `Admin@12345` | Bootstrap admin password |
| `CORS_ALLOWED_ORIGINS` | localhost patterns | Comma-separated allowed origin patterns |
| `UPLOADS_DIRECTORY` | `./data/uploads` | Filesystem upload root; binary file content is not stored in PostgreSQL |
| `UPLOADS_HOST_PATH` | `./backend/data/uploads` | Docker host folder mounted at `/data/uploads` |
| `MAX_UPLOAD_BYTES` | `104857600` | Application upload limit (100 MB) |
| `MAX_UPLOAD_SIZE` | `100MB` | Multipart per-file limit |
| `MAX_REQUEST_SIZE` | `105MB` | Multipart request limit |
| `SEED_DEMO_DATA` | `false` | Enable development fixtures; honored only by the `local` profile |

## Backend build and tests

```bash
cd backend
mvn spotless:check
mvn clean test
mvn package
```

The backend verification includes unit tests and ArchUnit dependency rules. A release
candidate should also be started against a new PostgreSQL database so Flyway and Hibernate
schema validation run.

## Flutter checks

Run these in each application directory:

```bash
flutter pub get
dart format --output=none --set-exit-if-changed lib test
flutter analyze
flutter test
```

The repository contains widget smoke tests for every Flutter application and an all-pages render
test for Society Admin. It also includes a cross-role API-driven suite that starts from a fresh
database, detects request hangs with deadlines, and verifies real PostgreSQL state transitions:

```bash
scripts/run_complete_app_e2e.sh
```

The runner builds a disposable backend, applies every Flyway migration, enables demo fixtures,
executes public/onboarding/authentication/portal/handoff/payment-cancellation scenarios, writes
`build/reports/complete_app_flow_smoke.json`, and removes its temporary containers/network. See
[`COMPLETE_APP_TEST_CASES.md`](COMPLETE_APP_TEST_CASES.md) for the full scenario ledger and the
production-provider cases that remain external.

## Database change process

Flyway files live under `backend/src/main/resources/db/migration`.

1. Never edit a migration already applied in any environment.
2. Add the next migration version (`V10__description.sql`, then V11, and so on).
3. Add explicit foreign keys, uniqueness rules, tenant/status indexes, and safe defaults.
4. Test both a new database and an upgrade from the previous release.
5. Keep Hibernate `ddl-auto: validate`; do not let Hibernate mutate production schema.
6. Document the data/backfill and rollback strategy for material changes.

## Release verification

Minimum release gate:

1. Backend format, compile, unit, and architecture tests pass.
2. All Flutter format, analyze, and widget tests pass.
3. Flyway upgrades a copy of the previous production schema.
4. Hibernate validation passes.
5. OpenAPI is generated and reviewed for accidental breaking changes.
6. Role and cross-tenant negative tests pass.
7. Registration, approval, activation, login, society setup, resident approval, and one
   operation flow pass in a staging smoke test.
8. Backup restore is exercised before a migration with material data risk.

## CI/CD

GitHub Actions is configured in `.github/workflows`:

- `ci.yml` runs on pull requests and pushes to `main`, `master`, or `develop`. It checks backend
  formatting, executes backend unit and architecture tests, runs format/analyze/test for the
  shared Flutter package and all five Flutter applications, builds every web bundle, builds the
  Society Admin Android debug APK, and runs the 151-scenario disposable-database E2E suite.
- `release.yml` runs for `v*` tags or manually. It first calls the complete CI workflow, builds
  environment-specific web bundles, and publishes immutable `societyhub-backend` and
  `societyhub-web` images to GitHub Container Registry. A failed verification prevents publishing.
- Dependabot checks GitHub Actions and Maven dependencies weekly.

The combined web image serves the public site at `/`, and the portals at `/super-admin/`,
`/society-admin/`, `/staff/`, and `/member/`. Nginx proxies `/api/` and `/healthz` to the backend.
Set these GitHub environment variables for both `staging` and `production`:

| Variable | Example | Purpose |
| --- | --- | --- |
| `PUBLIC_WEB_BASE_URL` | `https://society.example.com` | Origin used by public-site portal links; leave empty for relative same-origin links |
| `PUBLIC_API_BASE_URL` | `https://society.example.com` | API origin compiled into Flutter; leave empty when Nginx serves API and web together |
| `DEPLOY_ROOT` | `/opt/societyhub` | Remote directory containing Compose, `.env`, and uploads |
| `HEALTHCHECK_URL` | `https://society.example.com/healthz` | Required post-deployment health gate |

For automatic production rollout from a `v*` tag, set the repository-level variable
`ENABLE_SSH_DEPLOY=true`. Manual releases instead expose an explicit **Deploy after publishing**
checkbox, which defaults to off. When SSH deployment is enabled, add environment secrets `DEPLOY_HOST`, `DEPLOY_USER`,
`DEPLOY_SSH_KEY`, and optionally `DEPLOY_PORT`. Protect the `production` GitHub environment with
required reviewers. On the deployment host, copy `deploy/production.env.example` to
`/opt/societyhub/.env`, replace every placeholder, and keep that file outside source control.
The account must be able to run Docker Compose and write to `DEPLOY_ROOT`.

Release examples:

```bash
git tag v1.0.0
git push origin v1.0.0
```

For a manual staging release, open **Actions → Release and deploy → Run workflow**, select
`staging`, and choose whether to deploy after publishing. Deployment uses SHA-tagged images and
retains the previous image pair on the server. A failed post-deployment health check automatically
restores that previous pair. Database migrations must remain forward-compatible because rolling
back an image does not reverse a Flyway migration; restore testing is still required before
migrations with destructive or expensive backfills.

## Production deployment requirements

### Secrets and identity

- Store database, JWT, provider, and bootstrap secrets in a managed secret store.
- Use a high-entropy JWT key and a documented rotation strategy.
- Disable or rotate bootstrap credentials immediately after provisioning.
- Move mobile session persistence to Keychain/Keystore-backed storage; add rate limiting,
  lockout, password recovery, and verified email/mobile ownership.

### Data and files

- Use managed PostgreSQL with encrypted storage, point-in-time recovery, backups, restore
  drills, connection pooling, and monitoring.
- Replace local uploads with S3/MinIO-compatible storage.
- Add signed access, type/signature validation, malware scanning, encryption, retention,
  deletion, and access audit.
- Complete a privacy review before retaining Aadhaar/PAN or other government identifiers.

### Money and messaging

- Configure the existing platform-membership Razorpay adapter with managed live credentials and
  register the signed webhook endpoint in Razorpay Dashboard.
- Monitor idempotent webhook/payment-attempt records and reconcile provider settlement.
- Design a separate connected-account settlement adapter for money paid by residents to societies.
- Never mark a payment complete based only on a client request.
- Connect email/SMS/WhatsApp/push adapters and a durable background outbox dispatcher.

### Reliability and operations

- Add structured logs with correlation IDs and secret/PII redaction.
- Export metrics and traces; alert on availability, latency, errors, queue age, database
  capacity, failed login spikes, and backup failures.
- Add readiness/liveness checks and graceful shutdown.
- Run multiple stateless backend instances behind TLS termination.
- Add dependency, container, SAST, secret, and migration checks to CI/CD.

## Known operational limitations

- Uploads use local filesystem folders in development; production should use durable shared or
  object storage when running multiple backend instances.
- Notification retry changes outbox state but no provider dispatcher is included.
- Platform-membership payments support verified Razorpay capture. Society-level maintenance and
  operational payments remain internal/test ledger records pending connected-account settlement.
- No scheduled worker executes reminders, automatic invoices, lease expiry, or move-out.
- Mobile session persistence still uses shared preferences rather than platform
  Keychain/Keystore-backed storage.
- API routes are not versioned.
- Monitoring and backup automation are not defined in this repository.

## Upload storage

- PostgreSQL stores only relative file keys, MIME types, and business metadata; it does not store
  uploaded binary content.
- Docker bind-mounts `backend/data/uploads` into the backend at `/data/uploads`, so uploaded files
  remain visible and independently backupable on the host.
- New files are separated by society, media category, UTC year, and month:
  `<society-id>/<images|videos|documents>/<yyyy>/<MM>/<uuid>.<extension>`.
- JPEG, PNG, PDF, TXT, Word, Excel, MP4, WebM, and QuickTime formats are supported by the storage
  layer. Module-level APIs can enforce narrower file-type rules.
- Existing legacy flat paths continue to load without a database rewrite.
