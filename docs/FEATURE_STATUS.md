# Feature Status

Last audited: 31 July 2026.

Domain terminology: the product uses **space** for any managed place, including a
house number, apartment, entire level, villa, shop, office, or rental. Optional
**sections** organize spaces without assuming a floor-based building. Legacy
database/API names containing `floor` or `flat` remain for compatibility only.

## Executive summary

| Area | Status | Current reality |
| --- | --- | --- |
| Spring Boot API and PostgreSQL schema | Done | Flyway schema through V54, JPA entities, repositories, services, controllers, JWT security, and OpenAPI |
| Public website and onboarding | Done for current scope | Responsive service catalog, live plans, four portal launchers, society registration, activation, and custom requirements |
| Shared website/app design system | Done | All four role apps use the website palette, typography hierarchy, branded login treatment, cards, forms, buttons, and navigation with role accents |
| Super Admin app | Done for current scope | Feature-first Clean Architecture with repository contracts, a remote data source, dependency injection, and dedicated BLoC-controlled remote state/mutations across dashboard, societies, services, storage, plans, billing, notifications, requirements, support, and audit |
| Society Admin app | Done for current scope | Profile, structure, residents, configuration, staff, operations, billing ledger, and support workflows |
| Staff app | Done for current scope | Profile, attendance, permission-aware navigation, task/visitor/complaint actions, collections, activities, deliveries, emergencies, amenities, notices, and household verification |
| Member app | Done for current scope | Dashboard, payments and PDF receipts, complaints, visitors, bookings, household mutations, activities, deliveries, notices, and emergencies |
| Strict Clean Architecture backend | Done | Every workflow has an input port; interactors collaborate through ports; domain/application code is framework- and HTTP-independent; binary boundaries are stream-based; all technical concerns are outer adapters guarded by architecture tests |
| Production integrations | Partial | Platform membership has Razorpay order/capture/webhook/reconciliation support; society-level settlement, outbound messaging, object storage, OTP, observability, and scheduled lifecycle jobs remain pending |

## Public onboarding application

Location: `frontend/`

| Feature | Status | Detail |
| --- | --- | --- |
| Product landing and plan discovery | Done | Shows per-space/per-staff prices, limits, and trial duration |
| Public service catalog | Done | Responsive marketing sections explain visitors, billing, complaints, staff, amenities, and notices |
| Role application launcher | Done | Configurable links open Member, Society Admin, Staff, and Platform Admin deployments |
| Society registration | Done | Sends society/contact/plan information and receives registration result |
| Automatic Trial/Basic approval | Done | Backend generates the setup workflow immediately |
| Manual paid-plan approval | Done | Registration enters the Super Admin review queue |
| Setup-code activation | Done | One-time expiring code creates/activates the Society Admin account |
| Custom requirement request | Done | Public request is stored for Super Admin review |
| Public society and space discovery | Backend done, UI partial | APIs exist; resident registration UI must supply the selected society/space |
| Resident owner/tenant registration | Done | Registration is stored for Society Admin review |
| Public document upload | Backend done, UI partial | Society document upload API exists; complete onboarding upload UX needs validation |
| Contact verification | Pending | Verification records exist, but real email/mobile OTP delivery and confirmation are not complete |
| Password recovery | Pending | No forgot/reset-password workflow |

## Super Admin application

Location: `apps/super_admin/`

| Feature | Status | Detail |
| --- | --- | --- |
| Role-specific login/session | Done | JWT is stored locally and role is checked |
| Platform dashboard | Done | Live totals and interactive navigation |
| Society list/search/filter/detail | Done | Platform-wide society data |
| Create and edit society | Done | Includes profile data and logo upload |
| Approve/reject registrations | Done | Approval produces activation/setup state; rejection stores reason |
| Society document review | Done | List and verify/reject document status |
| Subscription rate settings | Done | Super Admin controls space/staff rates and limits, approval mode, and trial duration in days or months; society billing UI is quantity/rate focused rather than plan-name focused |
| Service catalog commercial rules | Done | Every active service is configurable as default-free, optional-free, or paid per purchased space; default services are synchronized to every society and paid selections contribute separate invoice line items |
| Service entitlement enforcement | Done | Society, Staff, and Member navigation follows selected services; protected service APIs return 403 when a service is not included |
| Legacy named tiers | Retired | Basic, Premium, and Enterprise are no longer selectable or visible; existing societies use the paid usage-rate configuration |
| Adjustable subscription requirements | Done | Registration and Society Admin profile capture spaces, staff, free/default services, paid add-ons, and billing cycle with immediate complete-cost calculation |
| Automatic payment/proration after quantity changes | Partial | Monthly/cycle totals recalculate immediately and the next issued invoice uses them; automatic mid-cycle proration/invoice issuance is not scheduled |
| Trial/payment entitlement enforcement | Done | Request-time checks block expired trials/memberships, pending initial payment, and unpaid invoices from their due date; existing JWTs cannot bypass the gate, while Society Admin retains payment-only recovery |
| Subscription orders and payment | Done for provider workflow | Razorpay order creation, signature/captured verification, signed idempotent webhooks, attempt history, invoice PDF, manual review, reconciliation, and safe cancellation/reissue of untouched Due invoices are implemented; live use requires credentials |
| Notification outbox | Partial | Kafka dispatcher, bounded retries, list, and manual retry are implemented; channel-specific email/SMS/WhatsApp/push consumers remain external |
| Audit view | Done | Search, exact action/entity filters, date range, pagination, actor/society labels, and structured event details |
| Society detail workspace | Done | Profile, full block/section/space hierarchy, users, documents, billing, support, requirements, notifications, audit trail, and operational summaries scoped to the selected society |
| Requirement review | Done | Review status and notes |
| Custom plan lifecycle | Done | Creates, edits and reason-cancels society-specific usage/fixed plans; activation replaces the previous Active plan, historical records are immutable, and legacy fixed monthly pricing is preserved |
| Support queue and resolution | Done | Ticket listing, resolution, and chat messages |
| Reporting/export | Pending | No CSV/PDF exports or scheduled reports |
| Fine-grained Super Admin permissions | Pending | Single `SUPER_ADMIN` role only |
| Storage policy and society usage | Done | Configure free allowance, paid block/price, and review per-society consumption and recurring add-on value |

## Society Admin application

Location: `apps/society_admin/`

| Feature | Status | Detail |
| --- | --- | --- |
| Role-specific login/session | Done | JWT session, role gate, and society-code/selector authentication |
| Society dashboard | Done | Live society operational counts |
| Society profile and logo | Done | Read/update profile and upload logo |
| Platform membership recovery | Done | Shows usage or fixed custom-plan totals, invoices, Razorpay checkout on web/Android/iOS, bounded checkout failure recovery, and invoice PDFs; when access is blocked, navigation is reduced to payment recovery |
| Property/section/space structure | Done | Hierarchical dashboard; create/edit property groups, optional sections and spaces; safe delete rules; area and occupancy summaries |
| Resident review | Done | Pending/active lists and approve/reject action |
| Occupancy rules | Done in backend | One active owner and one active tenant per space |
| Parking configuration | Done | Create/edit slots; allocate to a society space and vehicle; release, reserve, maintain, activate, or deactivate; configured availability feeds household vehicles |
| Vendor configuration | Done | Complete contact, service, email, GST, edit, activation, deactivation, duplicate protection, and audit lifecycle |
| Complaint categories | Done | Create/edit SLA categories, activate/deactivate, validate duplicates, and expose active options to operational forms |
| Maintenance charge rules | Done | Fixed, per-square-foot, and percentage rules with frequency, effective dates, validation, edit, and status lifecycle |
| Staff accounts/profiles | Done | Searchable directory; create/edit profiles, duty area, shift, employee code, status, and temporary-password reset |
| Staff seat billing | Done | Selected staff seats are priced monthly, shown in the staff directory, and enforced when accounts are created |
| Staff permissions | Done for administration | Society Admin assigns only permissions supported by enabled society modules; unknown and disabled permissions are rejected server-side |
| Setup completion | Done | Readiness metrics and required/optional checklist drive a confirmed completion action |
| Complaints | Done | Create/list/update lifecycle and staff assignment |
| Visitors | Done | Create/list and status transitions |
| Notices | Done | Create/list with audience and expiry data |
| Amenities | Done | Create/list; booking is available to resident/backend |
| Activities & galleries | Done | Free/paid activity publishing, capacity, registrations, payments, attendance, refunds, and photo moderation |
| Deliveries | Done | Parcel receipt, COD, secure collection, return/damage/loss handling, notifications, and audit history |
| Emergency & accidents | Done | Incident command queue, emergency directory, responder assignment, escalation, closure, and audit timeline |
| Maintenance invoices | Done as ledger | Create/list invoices; provider-based collection is pending |
| Staff tasks | Done | Create/list/complete assigned tasks |
| Support tickets and chat | Done | Create/list tickets and exchange messages |
| Storage management | Done | Usage dashboard, paid block purchase, protected legacy records, and safe file cleanup |
| Sensitive activity | Done | Tenant-scoped performer/action history for security-sensitive changes |
| Bulk import/export | Pending | No resident/space/staff CSV import or export |
| Accounting reconciliation | Partial | Platform membership can reconcile pending Razorpay attempts; society maintenance/operational bank settlement remains pending |

## Staff application

Location: `apps/staff/`

| Feature | Status | Detail |
| --- | --- | --- |
| Role-specific login/session | Done | `STAFF` role is enforced by the client and API |
| Assigned duties | Done | Assigned task list, detail, comments, progress, and completion actions |
| Visitor desk | Done | Registration, target selection, current photo capture, approval decisions, check-in, and check-out |
| Assigned complaints | Done | Creation, assignment-aware listing, comments, progress, and resolution actions |
| Deliveries | Done | Receipt, COD, unit delivery, pickup-code handover, exceptions, and history |
| Emergency/incidents | Done | Incident reporting, response, external-service records, escalation, resolution, and history |
| Activities & galleries | Done | Registration management, cash collection, approval, check-in, attendance, refunds, and gallery submissions |
| Staff permission-aware navigation | Done | Every operational entry requires both the society service entitlement and matching staff permission; profile and attendance remain always available |
| Push notifications | Pending | No mobile push integration |

## Member application

Location: `apps/member/`

| Feature | Status | Detail |
| --- | --- | --- |
| Role-specific login/session | Done | `RESIDENT` role is enforced by client and API |
| Home/dashboard | Done | Live society dashboard summary |
| Invoice/payment history | Done | Outstanding-invoice payment, history, numbered receipt view, copy, and PDF receipt download |
| Complaints | Done | Create complaint, select category/priority, and follow comments/status timeline |
| Visitors | Done | Pre-register, approve/reject arrivals, cancel, and review visitor history |
| Amenities | Done | Browse, book, pay, and track approval/check-in/refund states |
| Notices | Done as read-only | Live notice list |
| Household | Done | Add/edit/remove members, vehicles and emergency contacts with space and verification context |
| Deliveries | Done | Pre-registration, arrival decision, COD checkout, secure pickup, and history |
| Emergency & accidents | Done | SOS reporting, emergency directory, response tracking, updates, and cancellation |
| Activities & galleries | Done | Free/paid participation, online checkout, cancellation/refunds, attendance status, and moderated photos |
| Vehicle and tenancy details | Done for current scope | Active space/occupancy context and household vehicles are modeled and editable; verification state is visible |
| Online payment gateway | Split boundary | Platform membership uses verified Razorpay capture/webhooks; Member-to-society payments remain test/internal ledger pending connected-account settlement |
| Downloadable receipts | Done | Tenant/ownership-scoped PDF receipt generation and Member/Staff/Admin download action |
| Push/email reminders | Pending | No delivery provider |

## Backend capability status

| Domain | Status | Implemented behavior |
| --- | --- | --- |
| Authentication | Done for current scope | BCrypt login, JWT access tokens, rotating hashed refresh sessions, current-user validation, logout/revocation, role validation, and automatic Flutter refresh |
| Tenant isolation | Done for implemented queries | Society ID is derived from authenticated context for tenant-owned operations |
| Society onboarding | Done | Register, approve/reject, setup code, activate |
| Platform administration | Done | Society, plan, order, notification, audit, requirement, and support administration |
| Society structure | Done | Property groups, optional sections, spaces, uniqueness and hierarchy |
| Residents | Done | Self-registration, documents, review, account creation, occupancy checks |
| Staff | Done | Account/profile creation, shifts/duty areas, permissions |
| Daily operations | Done | Complaints, notices, visitors, amenities/bookings, tasks |
| Deliveries | Done | Role-scoped delivery lifecycle, COD collection, notifications, and immutable event history |
| Emergency incidents | Done | Role-scoped SOS/accident lifecycle, directory, responders, escalation, notifications, and immutable event history |
| Activities & galleries | Done | Role-scoped publishing, participation, payment, attendance, refund, photo moderation, notifications, and audit history |
| Billing | Partial | Platform membership has verified Razorpay processing; society maintenance and module collections remain internal/test ledger |
| Household | Done in API | Resident profile and add/remove household members |
| Configuration | Done | Tenant-safe parking, vendors, categories, effective charge rules, module-aware permissions, setup readiness/completion, audit events, responsive administration UI, and shared active references for Staff/Member |
| Support | Done | Tickets, resolution, and role-aware chat |
| Document storage | Partial | Local filesystem adapter; production object storage/scanning pending |
| Storage quota and commercial management | Done | Global free allowance and paid-block policy, byte-level enforcement, existing-file reconciliation, society usage/cleanup/purchase UI, recurring invoice line item, tenant isolation, and audit coverage |
| Notification delivery | Partial | Kafka outbox dispatch is implemented; channel-specific provider consumers remain external |
| Audit | Done | Every successful HTTP mutation emits a safe audit event; Society Admin gets tenant-scoped sensitive activity with performer snapshots, while Super Admin gets platform-wide filtering, paging, and structured details |
| Scheduled jobs | Pending | Lease expiry, move-out, reminders, invoice generation, and outbox dispatch |
| API versioning | Pending | Routes are unversioned under `/api` |

## Automated verification

| Verification | Status |
| --- | --- |
| Backend compilation, formatting, unit tests, and package | Passing in the latest audit |
| Architecture dependency tests | Passing for the rules currently defined |
| Flyway migration and deployed PostgreSQL upgrade | Stored deployment evidence passes through V51; V52–V54 fresh-database rerun is pending |
| Hibernate schema validation | Passing |
| Registration → activation → admin login → structure → setup smoke flow | Passing |
| Flutter widget smoke tests | Present for all applications |
| Society Admin all-pages render test | Present |
| Cross-role API-driven E2E smoke suite | Latest stored run passes 151 scenarios on a disposable fresh V51 database; V54 rerun is pending |
| CI/CD | Implemented | GitHub Actions verifies backend, six Flutter targets, five web builds, Android checkout build, and fresh-database E2E; tagged/manual releases publish immutable GHCR images with optional protected SSH rollout and health rollback |
| Browser/device interaction E2E suite | Pending; widget suites cover rendering/BLoC behavior, while physical browser/device automation remains external |
| Backend controller/repository integration test suite | Pending |
| Security/tenant penetration tests | Pending |
| Load/performance tests | Pending |

## Prioritized pending work

### P0 — before production data or money

1. Configure production Razorpay credentials/webhooks for platform membership and design
   connected-account settlement/reconciliation for society-level collections.
2. Replace local document storage with S3/MinIO, signed access, malware scanning, retention,
   and encryption controls.
3. Add real email/SMS/mobile OTP delivery and verification.
4. Move secrets and bootstrap credentials to managed secrets; add account lockout, rate
   limiting, recovery, and platform secure storage for mobile sessions.
5. Add database backups/restores, structured logs, metrics, tracing, and alerts.
6. Complete tenant-isolation integration tests for every ID-based read and mutation.

### P1 — complete user workflows

1. Add background workers for notifications, reminders, lease expiry, and invoice schedules.
2. Add bulk/reporting exports and finance-provider reconciliation.

### P2 — architecture and maintainability

1. Add API versioning, generated clients, contract tests, and full E2E coverage.
2. Continue splitting large use cases into feature-focused modules when that improves team
   ownership; preserve the enforced inward dependency direction during any package moves.
