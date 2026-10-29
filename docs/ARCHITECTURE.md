# Architecture

## Domain terminology

`Space` is the public domain term for a house number, apartment, entire level,
shop, office, parking-related property, or other managed/rented place. Spaces may
optionally be organized into neutral sections and property groups. Existing
database columns and Java persistence types named `floor` or `flat` are retained
to avoid a destructive migration. API and UI boundaries use section/space routes
and fields while accepting the legacy names as aliases.

## System context

SocietyHub is a multi-tenant society-management SaaS with five Flutter entry points and one
Spring Boot API.

```text
Public onboarding ─┐
Super Admin ───────┤
Society Admin ─────┼── HTTPS/JSON ── Spring Boot API ── PostgreSQL
Staff ─────────────┤                         │
Member ────────────┘                         ├── local document volume (development)
                                             └── Kafka notification events (optional)
```

The role-specific Flutter applications are independently deployable. They share API,
authentication, and visual primitives through `packages/society_ui`.

## Repository map

```text
society_app/
├── apps/
│   ├── super_admin/       platform operations application
│   ├── society_admin/     society management application
│   ├── staff/             staff application (partial workflows)
│   └── member/            resident application (partial workflows)
├── backend/               Java 21 / Spring Boot API
├── frontend/              public onboarding Flutter application
├── packages/society_ui/   shared Flutter client and portal components
├── docs/                  product and engineering documentation
└── docker-compose.yml     PostgreSQL and backend development stack
```

## Backend architecture

The backend enforces Clean Architecture across all features:

```text
HTTP/security adapter ──> input port <── application interactor ──> plain domain model
                                          │
                                          └──> output port
                                                   ▲
                                                   │
                                 persistence/storage/payment/rendering adapter
```

Responsibilities:

- Controllers own routes, request validation, authorization annotations, upload/resource
  adaptation, and response mapping. They do not execute SQL or call persistence adapters.
- Framework-free input ports define every use case invoked by HTTP, security, bootstrap, or
  storage adapters. Interactors implement those ports and own workflows, business validation,
  and tenant checks.
- Interactors collaborate through ports instead of concrete implementations. Binary input and
  output boundaries use stream-based application models, so local paths and Spring resources
  never cross into the core.
- Framework-free repository and application output ports describe capabilities required by
  use cases without exposing Spring Data, HTTP, Jackson, PDF, or storage types.
- Plain Java models represent business state. External `META-INF/orm.xml` metadata allows
  persistence adapters to reuse those models without placing JPA in the core.
- Infrastructure adapters implement Spring Data persistence, transactions, BCrypt, Jackson,
  local storage, Razorpay HTTP access, and PDF rendering.
- `ApplicationServiceConfiguration` composes use cases and applies transaction policy at the
  outer boundary, including `REQUIRES_NEW` audit recording.
- `ApiViewMapper` converts domain/read-model properties to snake_case where controllers use
  it; command/workflow maps retain their explicitly declared keys.
- Global exception handling maps domain/service failures to HTTP responses.

Architecture tests enforce that domain, use cases, and ports are framework-free; the core
cannot depend on controllers or infrastructure; Spring Data is restricted to persistence
adapters; controllers cannot access persistence directly; and outer adapters cannot depend
on concrete interactor implementations. Additional rules reject concrete interactor coupling,
HTTP-common dependencies from the core, and filesystem paths in application ports.

### Authentication

Authentication uses explicit input and output interfaces:

```text
AuthController
    │
    ▼
UserLogin input port ← AuthenticateUserService → output ports
                                            ├── UserAccountRepository
                                            ├── PasswordVerifier
                                            └── AccessTokenIssuer
                                                     ▲
                                                     │
                                    JPA, BCrypt, and JWT adapters
```

`auth/domain` and `auth/application` are independent of Spring Data and HTTP. Every other
backend workflow likewise exposes a framework-free interface in `application.port.in` and
uses framework-free interfaces for outbound dependencies. The shared core uses explicit
`domain.model`, `application.usecase`, and application port packages so package names and
dependency direction describe the same architectural role.

Domain and application code must not import controllers, JPA, Spring Data, servlet/multipart,
or concrete storage/provider classes.

## Multi-tenancy

Every authenticated JWT contains:

- user ID;
- role;
- society ID when the user belongs to a society.

Society-scoped workflows obtain the society ID from `CurrentUser`, not from an arbitrary
request body. Repository operations filter by that society ID. Platform administrators are
the only role with cross-society access.

Tenant safety rules:

1. Never load a tenant-owned record by ID alone when processing a society user request.
2. Validate related IDs (space, staff, amenity, invoice) belong to the same society.
3. Apply checks to writes as well as reads.
4. Add an integration test whenever a new tenant-owned query is introduced.

## Security architecture

- Stateless Spring Security filter chain
- Bearer JWT access authentication with rotating, server-revocable refresh sessions
- BCrypt password hashes
- Method-level role rules with `@PreAuthorize`
- Configurable CORS origin patterns
- Public access restricted to `/api/public/**`, `/api/auth/**`, health, and OpenAPI
- Multipart size limit and configurable application-level upload limit
- Request-time platform-membership policy returns `402` for expired/due societies, blocks
  Staff/Member session renewal, and preserves Society Admin payment-only recovery

Current gaps include account lockout, rate limiting, password recovery, real OTP confirmation,
tamper-evident external audit archival, platform secure storage for mobile sessions, and
field-level protection for sensitive identity information.

## Data architecture

PostgreSQL is the system of record. Flyway owns schema creation and upgrades; Hibernate runs
in `validate` mode and Open Session in View is disabled.

The V1–V54 migration history covers core tenancy/authentication, society structure and people,
operations, configuration, subscriptions/custom plans, service entitlements, storage quota,
audit/outbox records, rotating refresh sessions, and Razorpay platform payment attempts/webhook
idempotency and auditable custom-plan/invoice cancellation. `platform_payment_attempts` is owned by
`subscription_orders`; webhook event IDs are persisted before processing so retries cannot activate
membership twice.

Important database guarantees include foreign keys, tenant/status indexes, structural
uniqueness, and transaction-backed business validation. New schema changes must use a new
immutable Flyway migration.

## Frontend architecture

### Super Admin

Feature-oriented presentation structure using BLoCs, data models, service adapters, views,
and shared widgets. This is the most structured frontend application.

### Society Admin

Feature-first BLoC architecture with domain use cases and repository ports. All feature views,
including reusable `society_ui` workspaces, enter remote operations through a feature BLoC. A
`BlocApiGateway` adapts shared workspaces to BLoC events without exposing the concrete API client
to presentation. Resource state is deeply immutable, same-key requests reject stale responses,
overlapping mutations remain observable, and authentication transitions are serialized.
Feature pages do not contain endpoint paths or generic transport commands. Feature BLoCs expose
named operations and hold asynchronous operation results in state; events never carry completers.

### Member and Staff

Both compose role-specific BLoC-managed modules through the shared `society_ui` workspace and
gateway ports. Login/session state, keyed resource loading, mutations, permissions and service
entitlements are represented in BLoC state; their role-specific screens provide the implemented
visitor, complaint, task, collection, amenity, activity, household, delivery and emergency actions.

### Public frontend

A standalone onboarding application calling the public registration, activation,
requirements, and resident endpoints.

## External boundaries

The following should remain adapters behind application output ports:

- platform Razorpay provider (implemented adapter; live credentials remain deployment concerns);
- Apache Kafka notification-event publisher (implemented optional adapter with PostgreSQL outbox);
- society connected-account/settlement provider (not yet implemented);
- email/SMS/WhatsApp/push provider;
- S3/MinIO document storage;
- malware scanning;
- background job scheduler/queue;
- observability exporters.

Internal ledger or outbox state must not be described as successful real-world delivery
until the external provider confirms it through a verified callback.
