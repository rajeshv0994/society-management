# SocietyHub Backend

Spring Boot 4.0 / Java 21 REST API for the SocietyHub multi-tenant platform. PostgreSQL is the system of record, Flyway owns schema changes, JWT protects role-specific APIs, and Springdoc publishes Swagger documentation.

## Technology

| Concern | Technology |
| --- | --- |
| Runtime | Java 21 |
| Framework | Spring Boot 4.0 |
| Database | PostgreSQL 18 for local Compose; supported PostgreSQL deployment required in production |
| Database access | Spring Data JPA; direct JDBC is confined to bootstrap fixture adapters |
| Migrations | Flyway |
| Messaging | Apache Kafka with a transactional notification outbox dispatcher |
| Security | Spring Security, JWT, BCrypt |
| Validation | Jakarta Bean Validation |
| API documentation | Springdoc OpenAPI / Swagger UI |
| Build | Maven |
| Local infrastructure | Docker Compose |

## Source layout

```text
backend/
├── Dockerfile
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/societyapp/
    │   │   ├── SocietyApplication.java       # application entry point
    │   │   ├── controller/                   # inbound HTTP adapters and validation
    │   │   ├── domain/model/                 # plain framework-free domain model
    │   │   ├── domain/type/                  # typed business codes and state vocabulary
    │   │   ├── application/usecase/          # framework-free use-case interactors
    │   │   ├── application/port/in/          # use-case input boundaries
    │   │   ├── application/port/out/         # technical output capabilities
    │   │   ├── application/port/out/persistence/ # persistence output contracts
    │   │   ├── application/exception/        # framework-free application failures
    │   │   ├── application/policy/           # shared business policies
    │   │   ├── auth/                         # authentication domain, ports, and adapters
    │   │   ├── common/                       # shared HTTP error handling
    │   │   ├── config/                       # OpenAPI and application configuration
    │   │   ├── infrastructure/persistence/   # Spring Data/JPA adapters
    │   │   ├── infrastructure/documents/     # PDF rendering adapters
    │   │   ├── infrastructure/security/      # password adapter
    │   │   ├── infrastructure/serialization/ # Jackson adapter
    │   │   ├── infrastructure/bootstrap/     # development fixture adapters
    │   │   ├── documents/                    # local file-storage adapter
    │   │   ├── security/                     # inbound JWT/Spring Security adapter
    │   │   └── society/profile/dto/          # profile request contracts
    │   └── resources/
    │       ├── application.yml
    │       ├── META-INF/orm.xml              # external JPA mapping for plain models
    │       └── db/migration/                  # immutable Flyway migrations
    └── test/java/com/societyapp/              # unit and integration tests
```

Legacy `entity`, `service`, and `repository` core packages have been removed. Spring
composition, transactions, persistence, serialization, security, file handling, payment,
and document rendering live outside the domain and application core.

## Layer rules

The enforced dependency direction is:

```text
inbound adapters ──> application use cases ──> domain model
                              │
                              └──> output-port interfaces
                                        ▲
                                        │
                              outbound adapters
```

- `controller` owns HTTP contracts, validation, authorization annotations, and adaptation.
- `application.port.in` exposes every externally invoked use case to inbound adapters.
- `application.usecase` implements those input ports and owns framework-free workflows,
  business rules, and tenant authorization.
- `application.port.out.persistence` and `application.port.out` contain framework-free output
  contracts.
- `domain.model` contains plain Java state; `META-INF/orm.xml` supplies persistence metadata.
- `domain.type` owns roles, actions, payment methods, billing cycles, priorities, severity,
  pricing, storage-source types, and lifecycle codes as framework-free enums.
- `infrastructure.persistence.repository` contains Spring Data interfaces and adapters.
- `ApplicationServiceConfiguration` composes use cases and applies transactions externally.
- Cross-feature workflows collaborate through input ports, never controllers or concrete
  service implementations.
- Every tenant-owned repository operation receives a society ID derived from the
  authenticated principal.
- Controllers, security interceptors, and other outer adapters depend on input ports rather
  than concrete interactors.
- Interactor-to-interactor collaboration also uses input ports. Application file boundaries
  expose streams and metadata, never local filesystem paths or Spring resources.
- HTTP exception translation stays in `common`; business failures, policies, and mapping
  utilities live in the application core.
- `StrictCleanArchitectureTest`, `BackendArchitectureTest`, and `AuthArchitectureTest`
  prevent framework and adapter dependencies from entering the core or bypassing ports.

## Typed business codes

Business decisions do not switch on raw request strings. HTTP-compatible strings are parsed
once into `DomainCode` enums at the use-case boundary, and enum switches implement workflow
rules. Enum `code()` values preserve the existing uppercase database and JSON representation,
so this tightening does not require a destructive schema or API migration. Unknown and blank
codes fail immediately with a field-specific error. Domain enums are covered by the same
framework-independence architecture rule as domain models.

Bounded enums cover account and society lifecycle, subscriptions, payments, verification,
complaints, tasks, notices, visitors, activities, amenity bookings, deliveries, emergencies,
parking, storage, staff attendance and permissions, pricing, priorities, severity, and other
closed business vocabularies. Transition graphs for complaints and tasks live with their domain
statuses instead of being reconstructed in application services. Extensible identifiers such as
administrator-created service codes, audit event names, notification template names, and external
provider references remain strings by design.

`BusinessCodeArchitectureTest` scans the application core and rejects raw uppercase string switch
cases, raw lifecycle assignments/comparisons, and direct identity/payment/billing literals.
`StrictCleanArchitectureTest` additionally requires every enum in `domain.type` to implement the
shared `DomainCode` boundary contract.

Empty placeholder packages are intentionally not retained. Add a package only with its first
cohesive class; this keeps the source tree an accurate map of the implemented architecture.

## Multi-tenancy and roles

JWT claims contain the authenticated user ID, role, and society ID. `CurrentUser.societyId()` supplies the tenant ID for society-scoped operations.

| Role | Scope |
| --- | --- |
| `SUPER_ADMIN` | Platform-wide; may manage every society |
| `SOCIETY_ADMIN` | One society; tenant ID is mandatory |
| `STAFF` | Assigned society and permitted operational modules |
| `RESIDENT` | Assigned society and owned/tenanted space data |

Never accept a society ID from a Society Admin request when it can be derived from the JWT. Repository queries must use both the entity ID and tenant ID where applicable.

## Run with Docker (recommended)

From the repository root:

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f backend
```

Services:

- API: `http://localhost:8080`
- PostgreSQL from host: `localhost:5544`
- PostgreSQL inside Compose: `postgres:5432`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

Stop without deleting data:

```bash
docker compose down
```

Delete containers and local database/upload volumes only when a complete reset is intended:

```bash
docker compose down -v
```

## Run without Docker

Requirements: Java 21, Maven 3.9+, and PostgreSQL.

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/societydb
export SPRING_DATASOURCE_USERNAME=societyapp
export SPRING_DATASOURCE_PASSWORD=societyapp-dev-password
export JWT_SECRET=replace-with-at-least-32-random-characters
export SUPER_ADMIN_EMAIL=admin@society.local
export SUPER_ADMIN_PASSWORD=replace-with-a-strong-admin-password
export CORS_ALLOWED_ORIGINS=http://localhost:3000
export SPRING_PROFILES_ACTIVE=local
mvn spring-boot:run
```

## Configuration

| Environment variable | Default | Purpose |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | none | Use `local` for development and `production` for deployment |
| `SPRING_DATASOURCE_URL` | local-profile value | JDBC connection; required without the local profile |
| `SPRING_DATASOURCE_USERNAME` | local-profile value | Database user; required without the local profile |
| `SPRING_DATASOURCE_PASSWORD` | local-profile value | Database password; required without the local profile |
| `JWT_SECRET` | local-profile value | JWT signing secret; required without the local profile |
| `JWT_EXPIRATION` | `900000` | Access-token lifetime in milliseconds (15 minutes) |
| `JWT_REFRESH_EXPIRATION_DAYS` | `30` | Rotating refresh-session lifetime in days |
| `SUPER_ADMIN_EMAIL` | local-profile value | Bootstrap Super Admin email; required without the local profile |
| `SUPER_ADMIN_PASSWORD` | local-profile value | Bootstrap password; required without the local profile |
| `UPLOADS_DIRECTORY` | `./data/uploads` | Filesystem root for uploaded files; the database stores only relative paths and metadata |
| `UPLOADS_HOST_PATH` | `./backend/data/uploads` | Docker host folder mounted at `/data/uploads` |
| `MAX_UPLOAD_BYTES` | `104857600` | Maximum individual upload size (100 MB) |
| `MAX_UPLOAD_SIZE` | `100MB` | Spring multipart per-file limit |
| `MAX_REQUEST_SIZE` | `105MB` | Spring multipart request limit |
| `SEED_DEMO_DATA` | `false` | Adds demonstration accounts only when the `local` profile is active |
| `RAZORPAY_KEY_ID` | empty | Platform Razorpay public key ID (`rzp_test_...` or `rzp_live_...`) |
| `RAZORPAY_KEY_SECRET` | empty | Platform Razorpay API secret; server-side only |
| `RAZORPAY_WEBHOOK_SECRET` | empty | Secret used to authenticate Razorpay webhooks |
| `RAZORPAY_API_BASE` | `https://api.razorpay.com/v1` | Razorpay API base URL; override only for isolated provider tests |
| `KAFKA_ENABLED` | `false` | Enable scheduled publication from the notification outbox |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Comma-separated Kafka brokers |
| `KAFKA_NOTIFICATION_TOPIC` | `society.notifications.v1` | Versioned notification-request topic |
| `KAFKA_DISPATCH_DELAY_MS` | `5000` | Delay between outbox polling runs |
| `KAFKA_BATCH_SIZE` | `50` | Locked records selected per polling run; maximum 500 |
| `KAFKA_MAX_ATTEMPTS` | `5` | Publish failures before an outbox record is marked failed |
| `KAFKA_SEND_TIMEOUT_MS` | `10000` | Maximum synchronous broker acknowledgement wait |

The `production` profile fails startup when it detects development or placeholder database,
JWT, administrator, or CORS values. Demo seeding is forcibly disabled. Razorpay may be entirely
disabled, but a partial configuration or a non-live key is rejected. OpenAPI and Swagger are
disabled by default in production; set `SPRINGDOC_ENABLED=true` only when protected by the
deployment's access controls. When Kafka publishing is enabled, production also rejects local
broker addresses and missing topic names.

## Kafka notification outbox

Notification-producing workflows first persist `notification_outbox` records in the same
PostgreSQL transaction as their business changes. When Kafka is enabled, a scheduled application
use case locks pending rows with `FOR UPDATE SKIP LOCKED`, publishes them to the versioned topic,
and marks each row sent only after the broker acknowledges it. Failed sends are retained and move
to `FAILED` after the configured attempt limit; Super Admin's existing retry operation moves them
back to `PENDING`.

Delivery is at least once. Every event contains a stable `event_id`, and consumers must store or
otherwise deduplicate that ID. The society ID is used as the Kafka key to preserve per-society
ordering. Producers use `acks=all` and Kafka idempotence.

Start the optional local single-broker KRaft service with:

```bash
KAFKA_ENABLED=true docker compose --profile kafka up --build -d
```

## Platform membership payments

Platform membership checkout uses a server-created Razorpay Order. The Society Admin web,
Android, or iOS checkout returns the provider order ID, payment ID, and signature to the backend. Membership is
activated only after the signature is valid and Razorpay reports the payment as captured.

Configure Razorpay to send `payment.captured`, `payment.failed`, and `order.paid` events to:

```text
https://YOUR_API_HOST/api/public/payments/razorpay/webhook
```

Webhook delivery is authenticated against the unmodified request body and duplicate event IDs are
ignored. Every checkout retry has a separate persisted payment attempt. Super Admin can inspect the
gateway mode and payment references and reconcile a pending provider payment from Platform Billing.
Secrets must never be placed in Flutter build arguments or browser assets.

Membership entitlement is evaluated on every authenticated society request, not only at login.
An expired trial/membership, pending initial payment, or unpaid invoice whose due date has arrived
returns `402 Payment Required`. Staff and Member login/refresh are denied. Society Admin login is
retained only for `GET /api/society/profile` and `/api/society/subscription/**`, allowing invoice
review and payment recovery. Captured payment activation restores normal access.

Pricing supports both usage-based plans and fixed-price custom plans. For backward compatibility,
an active custom plan with zero space/staff rates and a positive legacy `price_monthly` uses that
value as `fixed_monthly_price`; it is included in the pricing breakdown and invoice total exactly
once per billing month.

Super Admin may cancel a `DRAFT` or `ACTIVE` custom plan with a mandatory audit reason. The record
is retained as `CANCELLED`; cancelling an active custom plan makes future pricing fall back to the
society's standard platform plan and does not alter invoices already issued. An untouched `DUE`
membership invoice may be cancelled separately with a reason. Invoices with a payment attempt, or
in `PAYMENT_PENDING`, `SUBMITTED`, or `PAID` state, are deliberately not cancellable through this
operation. A cancelled invoice remains in history, no longer blocks entitlement, and permits a
corrected invoice to be issued for the same billing month.

Uploaded binary content is kept outside PostgreSQL. New files use this relative layout:

```text
<society-id>/<images|videos|documents>/<yyyy>/<MM>/<random-file-name>.<extension>
```

Database records contain only the relative storage key, content type, and functional metadata.
Older flat storage keys remain readable for backward compatibility.

## Database migrations

Flyway automatically validates and applies migrations at application startup. Migration files live in `src/main/resources/db/migration`.

Rules:

1. Never edit a migration that has already been applied.
2. Add the next version, for example `V10__add_payment_reference.sql`.
3. Make constraints and indexes explicit.
4. Validate upgrades against a database containing the previous schema.
5. Do not use Hibernate schema generation; Flyway is the schema authority.

Check migration status in Docker logs:

```bash
docker compose logs backend | grep Flyway
```

## Swagger and authentication

Open `http://localhost:8080/swagger-ui.html`. To call secured operations:

1. Execute `POST /api/auth/login`.
2. Copy the returned `token`.
3. Select **Authorize**.
4. Paste the token; Swagger adds the `Bearer` prefix.

OpenAPI endpoints:

- JSON: `http://localhost:8080/v3/api-docs`
- YAML: `http://localhost:8080/v3/api-docs.yaml`
- Complete grouped API: `http://localhost:8080/v3/api-docs/complete-api`
- Role groups: `public-auth`, `platform-admin`, `society-admin`, `staff`, and `member`

The Complete API group is checked against the default Springdoc document so every generated
controller operation remains discoverable. Public login/registration operations have no bearer
requirement; secured operations explicitly declare `bearerAuth`.

## Important API groups

| Path | Capability |
| --- | --- |
| `/api/public/**` | Registration, activation and public discovery |
| `/api/auth/**` | Authentication |
| `/api/admin/platform/**` | Super Admin platform management |
| `/api/society/profile` | Society Admin profile management |
| `/api/society/structure/**` | Property groups, optional sections and spaces |
| `/api/society/residents/**` | Resident approval lifecycle |
| `/api/society/staff/**` | Staff management |
| `/api/complaints`, `/api/visitors`, `/api/notices` | Daily operations |
| `/api/amenities`, `/api/activities` | Facility bookings, activities and galleries |
| `/api/deliveries`, `/api/emergencies` | Delivery and emergency lifecycles |
| `/api/households`, `/api/member` | Household management and resident-owned profile data |
| `/api/billing`, `/api/tasks` | Maintenance, society finance and work |
| `/api/support/**` | Platform support and chat |

## Build and verification

With Maven installed:

```bash
mvn spotless:apply
mvn clean test
mvn package
```

Using the same Java/Maven environment as production:

```bash
docker compose build backend
docker compose up -d backend
docker compose logs --tail=100 backend
```

A change is ready when compilation and tests pass, Flyway starts successfully, Swagger loads, and authenticated smoke tests confirm tenant authorization.

## Adding a backend feature

For a new bounded context, use this feature-first structure:

```text
com/societyapp/payments/
├── domain/
├── application/
│   ├── port/in/
│   ├── port/out/
│   └── usecase/
└── adapter/
    ├── in/web/
    └── out/persistence/
```

Keep domain and application code independent of HTTP, JPA, Spring Data, and concrete
providers. Define output ports before persistence/storage adapters. Add a Flyway migration
if persistence changes, role annotations at the HTTP boundary, tenant filtering in every
repository operation, and matching unit, architecture, and integration tests.

## Local accounts

When the Compose development configuration is used:

- Super Admin: `admin@society.local` / `Admin@12345`
- Demo society code: `GREEN-VALLEY`
- Demo Society Admin: `green.admin@society.local` / `Admin@12345`

These credentials are for local development only.



That is exactly how it is configured now:
Docker CLI → Colima Docker Engine → project containers
Docker Desktop is not launched or required.


Use only Terminal:
brew services start colima
docker context use colima
cd /Users/drmac/society_app
docker compose up -d


Check status:
docker compose ps
curl http://localhost:8080/actuator/health

Stop the project:
docker compose down

Stop the Docker engine entirely:
brew services stop colima

Currently, the backend and PostgreSQL containers are already running through the Docker CLI with the colima context. The Docker CLI needs an engine; Colima provides that engine invisibly without Docker Desktop.