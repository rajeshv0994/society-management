# Complete Application Test Cases

Last executed: 29 July 2026.

This is the release-level scenario ledger for the public website, Platform Control, Society
Administration, Staff Workspace, Member Home, backend, database, and shared Flutter package. It
tests successful journeys, invalid input, authorization, retries, concurrency, and cross-role
handoffs. A screen rendering is not considered a completed flow unless the next responsible role
can see and finish the resulting work.

## Execution levels

| Mark | Meaning |
| --- | --- |
| `API` | Executed by `scripts/complete_app_flow_test.py` against a running backend and PostgreSQL |
| `Unit` | Covered by backend or shared-package unit tests |
| `Widget` | Covered by Flutter widget/BLoC tests |
| `External` | Requires configured third-party infrastructure or a physical-device/staging exercise |

Run the non-mutating portal and concurrency suite:

```bash
python3 scripts/complete_app_flow_test.py
```

Run the state-changing cross-role suite in automatically managed disposable containers:

```bash
scripts/run_complete_app_e2e.sh
```

The JSON evidence is written to `build/reports/complete_app_flow_smoke.json`. Every request has a
10-second default deadline, so an indefinitely loading resource becomes a failed case rather than
a hung test run.

## Release and installation

| ID | Level | Scenario | Expected completion |
| --- | --- | --- | --- |
| REL-01 | API | Start with an empty PostgreSQL database | Flyway V1–V54 applies and the backend remains healthy |
| REL-02 | API | Start with demo seeding enabled | All demo accounts/data seed without violating the current schema |
| REL-03 | API | Upgrade an existing V51 database | V52–V54 apply without deleting invoices, plans, users, or uploads |
| REL-04 | Unit | Hibernate schema validation | Entity mappings match Flyway-owned tables |
| REL-05 | Unit | Backend architecture rules | Controllers, services, repositories, and auth ports keep allowed dependency direction |
| REL-06 | Widget | Build all five Flutter web applications | Compilation succeeds and Material/Cupertino icon assets are bundled |
| REL-07 | Widget | Shared package tests | API, auth, immutable models, BLoCs, and role workspaces pass |
| REL-08 | External | Restore a production-like backup | Database and matching upload backup restore together |
| REL-09 | Widget | Build Society Admin Android app with native Razorpay SDK | Debug APK compiles and checkout adapter is bundled without a web-only dead end |
| REL-10 | Widget | Build Society Admin iOS app with native Razorpay SDK | Simulator app compiles and checkout adapter is bundled without a web-only dead end |

## Public discovery and society onboarding

| ID | Level | Scenario | Expected completion |
| --- | --- | --- | --- |
| ONB-01 | API/Widget | Load public plans, services, and societies | Non-blocking lists render with live prices and availability |
| ONB-02 | API | Submit invalid or over-limit registration | Validation explains the field/plan limit; no partial society is created |
| ONB-03 | API | Register eligible Trial society | Society is auto-approved and one-time setup code is returned |
| ONB-04 | API | Register manual-approval plan | Request appears in Super Admin pending queue |
| ONB-05 | API/Widget | Super Admin approves pending society | Setup code is generated and society can proceed |
| ONB-06 | Unit | Super Admin rejects with reason | Rejected status/reason remain auditable; activation is unavailable |
| ONB-07 | API | Activate approved society | Society Admin is created and Trial/Pending Payment membership begins correctly |
| ONB-08 | API | Reuse setup code | Second activation is rejected without creating another admin |
| ONB-09 | API | Login newly activated Society Admin | Correct tenant and role are returned |
| ONB-10 | API | Create block → section → space | Each identifier feeds the next step and structure reload contains the result |
| ONB-11 | API | Public resident registers against space | Registration appears in the correct society pending queue |
| ONB-12 | API | Society Admin approves resident with temporary password | Resident can log in and occupancy is linked to the selected space |
| ONB-13 | Unit/Widget | Reject resident with reason | No member login/occupancy is created; reason remains visible |
| ONB-14 | API | Create staff, grant permissions, and log in | Staff sees only enabled and permitted modules |
| ONB-15 | Unit/Widget | Reach configured capacity | Additional space/staff creation is blocked with an actionable message |
| ONB-16 | Unit/Widget | Society directory request fails | The page exposes the error/retry state instead of rendering a misleading empty directory |
| ONB-17 | API/Widget | Valid user attempts login after society suspension | API returns the inactive-society reason only after credential/code validation; every portal shows activation guidance without transport details |

## Authentication, sessions, and access recovery

| ID | Level | Scenario | Expected completion |
| --- | --- | --- | --- |
| AUTH-01 | API/Widget | Login each role with correct society code | Correct portal opens with user, role, and tenant context |
| AUTH-02 | API | Invalid password or unknown account | Generic 401 response does not leak account existence |
| AUTH-03 | Unit | Wrong society code | Login is denied even when email/password are valid |
| AUTH-04 | API | Rotate refresh token | New access/refresh pair works and old refresh token is rejected |
| AUTH-05 | API | Logout current session | Revoked refresh token cannot restore a session |
| AUTH-06 | Unit/Widget | Logout all sessions | Every persisted refresh session for the user becomes unusable |
| AUTH-07 | Unit/Widget | Expired access token during API call | Client refreshes once and retries the original request once |
| AUTH-08 | Unit | Concurrent refresh race | Reused/rotated token is rejected without issuing duplicate live sessions |
| AUTH-09 | API | Cross-role platform/tenant request | Request returns 403 and does not disclose protected records |
| AUTH-10 | Unit | Expired trial, due invoice, pending payment | Member/Staff blocked; Society Admin retains payment-recovery routes |
| AUTH-11 | Unit | Verified captured membership payment | Membership period activates and normal requests resume |
| AUTH-12 | External | Mobile app restart/session restoration | Secure platform storage restores or signs out cleanly without endless loading |

## Platform Control (Super Admin)

| ID | Level | Scenario | Expected completion |
| --- | --- | --- | --- |
| SUP-01 | API/Widget | Open every Platform Control section | Dashboard, societies, plans, services, billing, requirements, storage, support, notifications, and audit load |
| SUP-02 | Unit/Widget | Search/filter society list and open detail | Correct society profile, documents, subscription, custom plans, and support appear |
| SUP-03 | Unit/Widget | Edit society quantities/modules/status | Persisted values reload; invalid reductions are rejected |
| SUP-04 | Unit/Widget | Create/update service commercial policy | Availability and price propagate to public and society selection views |
| SUP-05 | Unit/Widget | Edit Trial/paid usage rates | Public estimate and invoice calculation use current rates |
| SUP-06 | API/Widget | Create Draft custom plan | Draft displays agreed capacity, modules, cycle, and non-zero pricing |
| SUP-07 | Unit/Widget | Update Draft/Active custom plan | Updated values reload; replaced/cancelled history is immutable |
| SUP-08 | API/Widget | Cancel Draft/Active custom plan | Mandatory reason stored; future active pricing falls back safely |
| SUP-09 | API | Cancel same custom plan twice | Second request is rejected without changing history |
| SUP-10 | API/Widget | Issue untouched DUE membership invoice | Society Admin sees the exact snapshotted pricing lines and amount |
| SUP-11 | API/Widget | Cancel untouched DUE invoice | Status becomes CANCELLED, history stays visible, entitlement ignores that invoice |
| SUP-12 | API | Reissue corrected invoice for same month | Partial unique index allows one new non-cancelled invoice |
| SUP-13 | Unit | Cancel invoice with payment attempt/pending/paid state | Cancellation is rejected to prevent a payment race |
| SUP-14 | API | Society opens support ticket; platform replies/resolves | Both roles see the same ordered conversation and terminal status |
| SUP-15 | Unit/Widget | Retry failed notification | Outbox returns to retryable state; no false delivered state is shown |
| SUP-16 | API/Widget | Load audit with filters/paging | Mutations are searchable with actor/society/entity snapshots |
| SUP-17 | Unit/Widget | Storage policy and usage | Quota, paid blocks, recurring price, and cleanup eligibility agree |

## Society Administration

| ID | Level | Scenario | Expected completion |
| --- | --- | --- | --- |
| SAD-01 | API/Widget | Open every administration section concurrently | No page hangs; profile through audit returns structured data |
| SAD-02 | Unit/Widget | Edit profile and upload logo | Saved identity/logo reload and invalid file types fail visibly |
| SAD-03 | API/Widget | Build/update/remove structure | Referenced/occupied spaces cannot be removed into an invalid state |
| SAD-04 | API/Widget | Review resident registration | Approval/rejection completes occupancy and account outcome atomically |
| SAD-05 | API/Widget | Create/update/deactivate staff | Login, permissions, employment, attendance, salary, and reset password stay linked |
| SAD-06 | Unit/Widget | Change purchased quantities | Cannot go below configured spaces/active staff; valid increase persists |
| SAD-07 | Unit/Widget | Configure parking/vendors/categories/charges | Create, edit, deactivate, validation, and reference-safe behavior work |
| SAD-08 | Unit/Widget | Complete setup readiness | Missing required configuration is listed; completion succeeds only when ready |
| SAD-09 | Unit/Widget | Create society invoice and record partial/full payment | Outstanding balance/status and receipt ledger remain consistent |
| SAD-10 | Unit/Widget | Configure society collection account | Member checkout uses society settings, not platform membership settings |
| SAD-11 | API | Assign resident complaint to permitted staff | Complaint becomes visible/actionable to that staff member |
| SAD-12 | API | Publish acknowledgement-required notice | Target users can read/acknowledge and admin sees resulting state |
| SAD-13 | Unit/Widget | Create/edit amenity and resolve booking | Capacity/collision/charge/status rules complete without orphan bookings |
| SAD-14 | API | Open/reply to support ticket | Platform can continue and resolve the same thread |
| SAD-15 | Unit/Widget | Storage cleanup | Only eligible files are removed and recorded byte usage reconciles |
| SAD-16 | Unit/Widget | Platform membership checkout on unsupported desktop | Action fails promptly with web recovery guidance instead of waiting indefinitely |
| SAD-17 | Unit/Widget | Razorpay checkout never returns a terminal event | Checkout ends at its bounded deadline and tells the user to refresh the invoice before retrying |

## Staff Workspace

| ID | Level | Scenario | Expected completion |
| --- | --- | --- | --- |
| STF-01 | API/Widget | Load profile, attendance, and every permitted workspace | No unauthorized page is exposed and no permitted page hangs |
| STF-02 | Unit/Widget | Clock in twice / clock out before clock in | Invalid action is blocked; valid daily attendance completes once |
| STF-03 | Unit/Widget | Start/progress/block/complete assigned task | Only valid transitions appear and completion is retained |
| STF-04 | Unit/Widget | Register visitor and obtain resident/staff approval | Approval path reaches check-in then check-out |
| STF-05 | Unit | Attempt gate action without VISITORS permission | 403 returned; visitor state is unchanged |
| STF-06 | API | Work assigned complaint through IN_PROGRESS to RESOLVED | Resolution required and Member sees timeline/result |
| STF-07 | Unit/Widget | Record maintenance collection | Balance and receipt update once; duplicate submission cannot double-pay |
| STF-08 | Unit/Widget | Manage amenity/activity participation | Approval, attendance, cash payment, and refund rules remain consistent |
| STF-09 | Unit/Widget | Verify household/vehicle | Member sees verified state; other-society identifiers are rejected |
| STF-10 | API | Acknowledge and resolve emergency | Member sees live status/history and required resolution note |
| STF-11 | Unit/Widget | Receive and release delivery | Arrival → notification → collection/return lifecycle is valid |

## Member Home

| ID | Level | Scenario | Expected completion |
| --- | --- | --- | --- |
| MEM-01 | API/Widget | Open every enabled Member workspace concurrently | Home, money, operations, household, and emergency pages load without hanging |
| MEM-02 | Unit/Widget | View own invoices/payment history | Only occupied-space invoices and own receipts are visible |
| MEM-03 | Unit/Widget | Complete test payment and download receipt | One payment ledger entry updates balance; PDF is downloadable |
| MEM-04 | API | Create complaint, comment after assignment, observe resolution | Cross-role timeline stays ordered and resident cannot change status directly |
| MEM-05 | API | Pre-register then cancel visitor | Visitor remains auditable as CANCELLED and cannot be checked in |
| MEM-06 | Unit/Widget | Approve/deny gate-created visitor | Only the host resident can decide; Staff sees resulting action |
| MEM-07 | Unit/Widget | Book colliding/full amenity slot | Conflict is explained; no duplicate booking or charge is created |
| MEM-08 | Unit/Widget | Join paid/free activity and cancel/refund | Participation/payment/refund states remain aligned |
| MEM-09 | API | Read and acknowledge published notice | Read/ack timestamps persist and non-target notices remain hidden |
| MEM-10 | Unit/Widget | Add/update/remove household, vehicle, contact | Ownership, verification, and active-history rules are enforced |
| MEM-11 | Unit/Widget | Pre-register/COD/collect delivery | Only resident-owned delivery can be acted upon |
| MEM-12 | API | Report emergency and follow Staff resolution | Required space, severity, description, and terminal history are preserved |

## Reliability, dead-end, and concurrency cases

| ID | Level | Scenario | Expected completion |
| --- | --- | --- | --- |
| RELI-01 | API | Load all role landing resources in parallel | Every request completes within deadline; no database/API deadlock |
| RELI-02 | Unit | HTTP request stalls | Shared client emits useful timeout failure and retry remains available |
| RELI-03 | Unit/Widget | Initial page load fails | Failure state replaces spinner and exposes Retry |
| RELI-04 | Unit/Widget | Mutation fails validation | Dialog remains actionable, error is visible, duplicate mutation is not sent |
| RELI-05 | Unit | Double-click/double callback payment | Idempotency prevents duplicate capture/ledger activation |
| RELI-06 | Unit | Two users book same amenity slot | One succeeds; one receives conflict without hanging transaction |
| RELI-07 | Unit | Two resident approvals compete for occupied space | Database/service rules permit only valid owner/tenant occupancy |
| RELI-08 | API | Forbidden ID from another role/tenant | 403/404 returned without leaking record contents |
| RELI-09 | Unit | Cancel while payment attempt exists | Cancellation loses safely to payment state and no inconsistent terminal state appears |
| RELI-10 | External | Network drops during mutation response | Refresh/reload converges to server state; user can safely retry idempotent operations |
| RELI-11 | External | Two backend instances process webhook | Event/payment uniqueness keeps exactly one financial transition |
| RELI-12 | External | Load, connection-pool, and long transaction test | Defined latency/error thresholds pass with database lock monitoring enabled |
| RELI-13 | Unit/Widget | API is unreachable, times out, or returns an invalid payload | Every app stops loading within its deadline, hides technical host details, explains availability, and exposes retry where the page can recover |

## External production acceptance

These cases cannot honestly pass from repository-only tests. They are mandatory before the
corresponding production capability is enabled.

| ID | Scenario | Required evidence |
| --- | --- | --- |
| EXT-01 | Live Razorpay success, failure, abandonment, late webhook, refund/chargeback | Provider test/live references, signed webhook logs, settlement reconciliation |
| EXT-02 | Society connected-account collections | Provider-connected account, settlement and bank reconciliation evidence |
| EXT-03 | Email/SMS/WhatsApp/push delivery and retry | Provider delivery IDs, bounce/failure handling, background worker metrics |
| EXT-04 | OTP/contact verification and recovery | Expiry, throttling, replay, wrong-code, lockout, and recovery results |
| EXT-05 | S3/MinIO upload, malware scan, signed download, retention | Object/audit evidence and failure-mode tests |
| EXT-06 | Mobile Keychain/Keystore session storage | Android/iOS physical-device restart, revoke, and upgrade tests |
| EXT-07 | Backup/restore and disaster recovery | Timed restore drill with verified database/file consistency |
| EXT-08 | Security and performance acceptance | Tenant penetration, SAST/dependency, rate-limit, load, soak, and lock reports |

## Defect rule

A failed scenario is a release blocker when it prevents a user from continuing, leaves a spinner
without a timeout/retry, loses a cross-role handoff, creates contradictory financial/occupancy
state, violates tenant isolation, or prevents a clean installation. Fixes must add or extend an
automated regression whenever the scenario is locally reproducible.
