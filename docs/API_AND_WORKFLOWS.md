# API and Workflows

The backend base URL is `http://localhost:8080` in local development. All bodies are JSON
unless an endpoint is marked multipart. Live field-level schemas and examples are available
in Swagger UI at `/swagger-ui.html`.

Swagger exposes an explicit Complete API group at `/v3/api-docs/complete-api` and separate
`public-auth`, `platform-admin`, `society-admin`, `staff`, and `member` groups. The complete group
contains every generated controller path; role groups organize operations by application without
changing backend authorization.

## Roles

| Role | Scope |
| --- | --- |
| Public | Plans, discovery, onboarding, activation, requirements, and resident registration |
| `SUPER_ADMIN` | All societies and platform configuration |
| `SOCIETY_ADMIN` | One society and all of its management workflows |
| `STAFF` | Assigned society operations allowed by endpoint role rules |
| `RESIDENT` | Assigned society and resident/space-related data |

Society-scoped login accepts a selected society's unique code:

```json
{
  "societyCode": "GREEN-VALLEY",
  "email": "user@example.com",
  "password": "secret"
}
```

The code is required for Society Admin, Staff, and Member accounts. Login screens
can load codes from `GET /api/public/societies` or accept direct entry. Super Admin
login omits `societyCode`.

Send secured requests with:

```http
Authorization: Bearer <token>
```

## Core workflows

### Society onboarding

1. Load `GET /api/public/plans`.
2. Submit `POST /api/public/societies/register`.
3. Trial/Basic registrations can proceed automatically; other plans enter platform review.
4. Registration validates space/staff limits, includes all default-free services, validates
   optional selections, and returns the estimated monthly price:
   `(spaces × base space rate) + (staff × staff rate) + Σ(spaces × selected paid-service rate)`.
5. The selected space and staff quantities become the society's purchased capacity.
   Society Admin can later increase or reduce them from the profile subscription card.
   A reduction cannot go below configured spaces or active staff accounts.
6. Trial activation sets its expiry from the Super Admin-configured duration,
   which can be entered in days or months. Standard activation starts in `PENDING_PAYMENT`.
7. Entitlement is re-evaluated on every society API request. Expired trials/memberships and unpaid
   invoices on or after their due date return `402`, so an existing access token cannot bypass the
   payment gate. Staff/Member login and refresh are rejected; Society Admin retains only profile
   read and platform-membership payment recovery routes.
8. Super Admin reviews documents and calls approve or reject.
9. The approved setup code is shown once and expires.
10. Submit `POST /api/public/societies/activate` with the code and admin credentials.
11. Log in, pay the initial platform invoice when required, configure property groups, optional
    sections and spaces, then complete setup.

### Platform membership payment and pricing

1. Usage pricing is `(spaces × space rate) + (staff × staff rate) + paid service add-ons`.
2. An active fixed custom plan contributes its fixed monthly base exactly once. Legacy custom plans
   with zero usage rates read the agreed amount from `price_monthly` rather than calculating zero.
3. Super Admin issues an invoice containing snapshotted pricing lines and the billing period.
4. Society Admin starts checkout; the server creates the Razorpay order for the invoice amount.
5. The backend verifies the Razorpay signature and independently confirms `captured` status, or
   accepts the equivalent signed webhook. Duplicate callbacks/payment attempts are idempotent.
6. Only verified capture marks the invoice paid and activates the next membership period.
7. Super Admin may cancel only a `DUE` invoice with no payment attempt. A reason is required; the
   invoice remains `CANCELLED` history and no longer blocks entitlement. `PAYMENT_PENDING`,
   `SUBMITTED`, and `PAID` invoices cannot be cancelled. A corrected invoice can then be issued for
   the same billing month.
8. Draft or Active custom plans can be cancelled with a reason. Cancelling an Active custom plan
   returns future calculation to the society's platform plan; existing invoices remain immutable
   and must be cancelled separately when eligible.

### Resident onboarding

1. Discover a society and its available spaces.
2. Submit owner or tenant registration.
3. Society Admin reviews the pending registration.
4. Approval validates space occupancy and creates/enables the resident account.
5. Resident logs into the Member application.

### Daily operations

- Society Admin creates staff, notices, amenities, invoices, tasks, complaints, and visitors.
- Staff uses permission-aware screens to progress tasks, update complaints, register and
  transition visitors, collect maintenance, and operate enabled society modules.
- Residents use dedicated screens to create complaints and visitors, book amenities, complete
  test-mode online payments, download receipts, and manage household records.

### Support

1. Society Admin creates a support ticket.
2. Society Admin and Super Admin exchange ticket messages.
3. Super Admin records resolution and closes the ticket.

## Endpoint catalog

### Public and authentication

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Authenticate and return JWT/profile claims |
| `POST` | `/api/auth/refresh` | Rotate refresh session and return new tokens |
| `GET` | `/api/auth/me` | Validate bearer session and return active account |
| `POST` | `/api/auth/logout` | Revoke current refresh session |
| `POST` | `/api/auth/logout-all` | Revoke all sessions for authenticated account |
| `GET` | `/api/public/plans` | List public subscription plans |
| `GET` | `/api/public/societies` | Discover societies |
| `POST` | `/api/public/societies/register` | Register a society |
| `POST` | `/api/public/societies/activate` | Consume setup code and activate admin |
| `GET` | `/api/public/societies/{societyId}/spaces` | List society spaces for registration |
| `GET` | `/api/public/societies/{societyId}/units` | Legacy alias for the spaces endpoint |
| `GET` | `/api/public/societies/{societyId}/flats` | Legacy alias for the spaces endpoint |
| `POST` | `/api/public/residents/register` | Submit owner/tenant registration |
| `POST` | `/api/public/requirements` | Submit custom commercial requirements |
| `POST` | `/api/public/payments/razorpay/webhook` | Receive signed/idempotent platform-membership provider events |
| `POST` | `/api/public/societies/{id}/documents` | Upload onboarding document (multipart) |
| `GET` | `/api/public/societies/{id}/logo` | Read public society logo |

### Super Admin

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/dashboard/platform` | Legacy/platform dashboard summary |
| `GET` | `/api/admin/platform/dashboard` | Platform dashboard |
| `GET` | `/api/admin/platform/societies` | Search/filter societies |
| `POST` | `/api/admin/platform/societies` | Create society |
| `GET` | `/api/admin/platform/societies/{id}` | Complete society detail: profile, hierarchy, users, documents, plans/orders, support, requirements, notifications, audit events, and operational summaries |
| `PUT` | `/api/admin/platform/societies/{id}` | Update society |
| `POST` | `/api/admin/platform/societies/{id}/logo` | Upload society logo (multipart) |
| `GET` | `/api/admin/societies/pending` | Pending registration queue |
| `POST` | `/api/admin/societies/{id}/approve` | Approve and create setup state |
| `POST` | `/api/admin/societies/{id}/reject` | Reject with reason |
| `GET` | `/api/admin/societies/{id}/documents` | List onboarding documents |
| `PATCH` | `/api/admin/societies/{societyId}/documents/{documentId}` | Review a document |
| `GET` | `/api/admin/platform/plans` | List platform plans |
| `PUT` | `/api/admin/platform/plans/{code}` | Update space/staff prices and limits, approval mode, and trial duration |
| `GET` | `/api/admin/platform/services` | List service commercial policy, society adoption, and projected monthly revenue |
| `POST` | `/api/admin/platform/services` | Create a default-free, optional-free, or paid-per-space service |
| `PUT` | `/api/admin/platform/services/{code}` | Update service availability, status, order, and per-space price |
| `GET` | `/api/admin/platform/orders` | List subscription orders |
| `POST` | `/api/admin/platform/orders` | Issue a platform-membership invoice |
| `POST` | `/api/admin/platform/orders/{id}/review` | Review an offline/manual payment submission |
| `POST` | `/api/admin/platform/orders/{id}/reconcile` | Reconcile a pending Razorpay attempt against provider state |
| `POST` | `/api/admin/platform/orders/{id}/cancel` | Cancel an untouched Due invoice with a mandatory reason |
| `GET` | `/api/admin/platform/orders/{id}/invoice` | Download the platform invoice PDF |
| `GET`, `PUT` | `/api/admin/platform/payment-settings` | Read/update platform beneficiary instructions |
| `GET` | `/api/admin/platform/notifications` | List notification outbox |
| `POST` | `/api/admin/platform/notifications/{id}/retry` | Reset failed notification for retry |
| `GET` | `/api/admin/platform/audit` | Page and filter audit events by action, entity type, society, actor, time range, or search text |
| `GET` | `/api/admin/platform/storage` | Read the storage policy and per-society usage/add-on overview |
| `PUT` | `/api/admin/platform/storage/policy` | Set free GB, purchasable block size, and monthly block price |
| `GET` | `/api/admin/platform/support` | List platform support tickets |
| `PATCH` | `/api/admin/platform/support/{id}` | Resolve support ticket |
| `GET` | `/api/admin/platform/requirements` | List requirement requests |
| `PATCH` | `/api/admin/platform/requirements/{id}` | Review requirement request |
| `POST` | `/api/admin/platform/custom-plans` | Create a usage-based or fixed-monthly custom plan |
| `GET` | `/api/admin/platform/custom-plans` | List custom plans |
| `PUT` | `/api/admin/platform/custom-plans/{id}` | Update Draft/Active pricing, capacity, modules, validity, cycle, and activation state; Replaced plans are immutable |
| `POST` | `/api/admin/platform/custom-plans/{id}/cancel` | Cancel a Draft/Active plan with reason while retaining immutable history |

### Society profile, structure, residents, and configuration

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/dashboard/society` | Tenant dashboard summary |
| `GET` | `/api/society/profile` | Read society profile |
| `PUT` | `/api/society/profile` | Update society profile |
| `POST` | `/api/society/profile/logo` | Upload own society logo (multipart) |
| `GET` | `/api/society/structure` | Read property-group/section/space hierarchy |
| `POST` | `/api/society/structure/blocks` | Create block |
| `PUT` | `/api/society/structure/blocks/{blockId}` | Rename block |
| `DELETE` | `/api/society/structure/blocks/{blockId}` | Delete an empty block |
| `POST` | `/api/society/structure/blocks/{blockId}/sections` | Create an optional organizing section |
| `PUT` | `/api/society/structure/sections/{sectionId}` | Update section name/display order |
| `DELETE` | `/api/society/structure/sections/{sectionId}` | Delete an empty section |
| `POST` | `/api/society/structure/spaces` | Create a space directly; a neutral General group/section is created when needed |
| `POST` | `/api/society/structure/sections/{sectionId}/spaces` | Create a space |
| `PUT` | `/api/society/structure/spaces/{spaceId}` | Update space number/name and area |
| `DELETE` | `/api/society/structure/spaces/{spaceId}` | Delete a vacant, unassigned space |

The older `/floors`, `/units`, and `/flats` routes and their related request fields
remain compatibility aliases. New clients should use `/sections`, `/spaces`,
`sectionOrder`, `spaceId`, and `spaceNumber`.
| `GET` | `/api/society/residents/pending` | Pending resident registrations |
| `GET` | `/api/society/subscription` | Purchased quantities, service catalog/selections, pricing breakdown, limits, and cycle total |
| `PUT` | `/api/society/subscription/quantities` | Atomically update spaces, staff, services, and billing cycle |
| `GET` | `/api/society/subscription/orders` | List the society's platform-membership invoices and attempts |
| `POST` | `/api/society/subscription/orders/{id}/checkout` | Create a server-side Razorpay order |
| `POST` | `/api/society/subscription/orders/{id}/verify` | Verify signature/captured payment and activate membership |
| `GET` | `/api/society/subscription/orders/{id}/invoice` | Download the society's platform invoice PDF |
| `GET` | `/api/society/residents` | Active residents |
| `POST` | `/api/society/residents/{id}/review` | Approve/reject resident |
| `GET` | `/api/society/configuration` | Read configuration, summaries, space options, module-aware permission catalog, and setup readiness |
| `GET` | `/api/configuration/reference` | Read active/current configuration references as Society Admin, Staff, or Member |
| `POST`, `PUT` | `/api/society/configuration/parking[/{id}]` | Create or edit a parking slot |
| `PATCH` | `/api/society/configuration/parking/{id}` | Allocate, release, reserve, maintain, activate, or deactivate a parking slot |
| `DELETE` | `/api/society/configuration/parking/{id}` | Deactivate a parking slot while retaining history |
| `POST`, `PUT` | `/api/society/configuration/vendors[/{id}]` | Create or edit a complete vendor profile |
| `PATCH`, `DELETE` | `/api/society/configuration/vendors/{id}` | Change vendor status or deactivate it |
| `POST`, `PUT` | `/api/society/configuration/complaint-categories[/{id}]` | Create or edit an SLA category |
| `PATCH`, `DELETE` | `/api/society/configuration/complaint-categories/{id}` | Activate or deactivate a category |
| `POST`, `PUT` | `/api/society/configuration/charge-rules[/{id}]` | Create or edit an effective-dated charge rule |
| `PATCH`, `DELETE` | `/api/society/configuration/charge-rules/{id}` | Activate or deactivate a charge rule |
| `PUT` | `/api/society/configuration/staff/{staffId}/permissions` | Replace staff permissions |
| `POST` | `/api/society/configuration/complete` | Mark a structurally ready society setup complete |
| `GET` | `/api/society/audit` | Society Admin-only, tenant-scoped activity history; defaults to sensitive actions and includes performer snapshots |
| `GET` | `/api/society/storage` | Read quota, usage, category totals, files, purchases, and monthly add-on charge |
| `POST` | `/api/society/storage/purchases` | Purchase recurring storage blocks at the current snapshotted price |
| `DELETE` | `/api/society/storage/files/{id}` | Safely delete an eligible file and its owning metadata to release quota |
| `GET` | `/api/society/staff` | List staff |
| `POST` | `/api/society/staff` | Create staff account/profile |
| `PUT` | `/api/society/staff/{staffId}` | Update staff account/profile |
| `PATCH` | `/api/society/staff/{staffId}/status` | Activate/deactivate staff |
| `POST` | `/api/society/staff/{staffId}/reset-password` | Set a new temporary password |
| `PUT` | `/api/society/staff/{staffId}/permissions` | Replace module permissions |

### Operations, billing, and member

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| `GET` | `/api/complaints` | Admin/Staff/Resident | List scoped complaints |
| `POST` | `/api/complaints` | Admin/Resident | Create complaint |
| `PATCH` | `/api/complaints/{id}` | Admin/Staff | Update assignment/status/resolution |
| `GET` | `/api/visitors` | Admin/Staff/Resident | List scoped visitors |
| `POST` | `/api/visitors` | Admin/Resident | Create/pre-approve visitor |
| `PATCH` | `/api/visitors/{id}/status` | Admin/Staff | Check in/out or update status |
| `GET` | `/api/notices` | Admin/Staff/Resident | List visible notices |
| `POST` | `/api/notices` | Admin | Publish notice |
| `GET` | `/api/amenities` | Admin/Staff/Resident | List amenities |
| `POST` | `/api/amenities` | Admin | Create amenity |
| `POST` | `/api/amenities/{id}/book` | Admin/Resident | Book available time |
| `GET` | `/api/tasks` | Admin/Staff | List tasks |
| `POST` | `/api/tasks` | Admin | Assign task |
| `PATCH` | `/api/tasks/{id}/complete` | Admin/Staff | Complete task |
| `GET` | `/api/billing/invoices` | Admin/Resident | List scoped invoices |
| `POST` | `/api/billing/invoices` | Admin | Create invoice |
| `POST` | `/api/billing/invoices/{id}/payments` | Admin/Resident | Add internal payment ledger entry |
| `GET` | `/api/billing/payments/{id}/receipt.pdf` | Admin/Staff/Resident | Download a tenant/ownership-scoped payment receipt PDF |
| `GET` | `/api/member/profile` | Resident | Read resident and household profile |
| `POST` | `/api/member/household` | Resident | Add household member |
| `DELETE` | `/api/member/household/{id}` | Resident | Remove own household member |

### Support

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| `GET` | `/api/support` | Super/Society Admin | List role-scoped tickets |
| `POST` | `/api/support` | Society Admin | Create ticket |
| `GET` | `/api/support/{ticketId}/messages` | Super/Society Admin | Read authorized ticket chat |
| `POST` | `/api/support/{ticketId}/messages` | Super/Society Admin | Add ticket message |

## API conventions

- Read-model responses generally use snake_case. Workflow responses that are explicitly
  assembled as maps may use camelCase (for example `setupCode`). Treat the generated OpenAPI
  schema and the calling Flutter model as authoritative until a single naming policy is
  introduced.
- Validation errors and business conflicts are mapped by the global exception handler.
- IDs are UUID strings.
- Date/time values are ISO-8601.
- Tenant-owned IDs are always validated against the JWT society context.
- Society-level maintenance/module payment endpoint success records an internal/test ledger entry;
  it is not verified bank settlement. Platform-membership payment is separate and succeeds only
  after verified Razorpay capture or approved manual review.
- Setup codes and credentials should be treated as secrets and never logged.
