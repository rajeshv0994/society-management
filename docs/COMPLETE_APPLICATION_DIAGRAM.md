# Complete Application Architecture Diagram

This document shows how the complete SocietyHub application is assembled. For business-state
flows and the detailed database ER model, see
[Complete Application Flow and Database Structure](COMPLETE_APPLICATION_FLOW_AND_DATABASE.md).

## Rendered flowchart assets

- [Complete application and configuration flowchart — PNG](diagrams/complete_application_configuration_flowchart.png)
- [Complete application and configuration flowchart — zoomable SVG](diagrams/complete_application_configuration_flowchart.svg)
- [Detailed society configuration flowchart — PNG](diagrams/society_configuration_detail_flowchart.png)
- [Detailed society configuration flowchart — zoomable SVG](diagrams/society_configuration_detail_flowchart.svg)
- [Complete database structure flowchart — PNG](diagrams/complete_database_structure_flowchart.png)
- [Complete database structure flowchart — zoomable SVG](diagrams/complete_database_structure_flowchart.svg)

The PNG files are convenient for sharing in chat or presentations. Use the SVG versions for
zooming, printing, or editing without losing text quality.

## 1. Master application diagram

```mermaid
flowchart TB
    subgraph People["Users"]
        Prospect["Prospective society / resident"]
        PlatformUser["Super Admin"]
        SocietyUser["Society Admin"]
        StaffUser["Staff"]
        MemberUser["Member / Resident"]
    end

    subgraph Flutter["Flutter applications"]
        PublicApp["Public onboarding<br/>frontend"]
        SuperApp["Platform Control<br/>apps/super_admin"]
        SocietyApp["Society Administration<br/>apps/society_admin"]
        StaffApp["Staff Workspace<br/>apps/staff"]
        MemberApp["Member Home<br/>apps/member"]
        SharedUI["Shared UI, API client, session manager<br/>packages/society_ui"]
    end

    subgraph Backend["Spring Boot backend"]
        Web["REST controllers and request validation"]
        Security["JWT filter, role authorization, tenant scope, staff permissions"]

        subgraph PlatformDomain["Platform domain"]
            PlatformServices["Societies, service catalog, plans, requirements, subscriptions"]
            PlatformOps["Platform billing, notifications, support and audit"]
            StoragePolicy["Storage policy, society usage and paid capacity"]
        end

        subgraph SocietyDomain["Society administration domain"]
            SetupServices["Profile, structure, residents, households, staff and configuration"]
            FinanceServices["Maintenance invoices, payments, budget, expenses and salaries"]
        end

        subgraph OperationsDomain["Society operations domain"]
            DailyOps["Complaints, tasks, visitors, notices and documents"]
            BookingOps["Amenities, activities and galleries"]
            ResponseOps["Deliveries, emergency and accident response"]
        end

        Persistence["Spring Data repositories and JPA entities"]
        FileService["Validated, quota-metered document/media storage"]
        AuditInterceptor["Successful-mutation audit interceptor"]
        Flyway["Flyway schema migrations"]
    end

    subgraph Data["Persistent data"]
        Database[(PostgreSQL: societydb<br/>business tables + Flyway history)]
        Uploads[(Upload folders<br/>backend/data/uploads/society/type/year/month)]
    end

    subgraph External["External production integrations"]
        Razorpay["Razorpay<br/>platform membership production adapter"]
        Messaging["Email / SMS / WhatsApp / push<br/>provider adapter required"]
        ObjectStorage["Shared filesystem or object storage<br/>recommended for multi-instance production"]
    end

    Prospect --> PublicApp
    PlatformUser --> SuperApp
    SocietyUser --> SocietyApp
    StaffUser --> StaffApp
    MemberUser --> MemberApp

    SuperApp --> SharedUI
    SocietyApp --> SharedUI
    StaffApp --> SharedUI
    MemberApp --> SharedUI
    PublicApp -->|Public HTTP client| Web
    SharedUI -->|HTTPS, JSON, multipart and PDF| Web

    Web --> Security
    Security --> PlatformServices
    Security --> PlatformOps
    Security --> StoragePolicy
    Security --> SetupServices
    Security --> FinanceServices
    Security --> DailyOps
    Security --> BookingOps
    Security --> ResponseOps

    PlatformServices --> Persistence
    PlatformOps --> Persistence
    StoragePolicy --> Persistence
    SetupServices --> Persistence
    FinanceServices --> Persistence
    DailyOps --> Persistence
    BookingOps --> Persistence
    ResponseOps --> Persistence
    Persistence --> Database
    Flyway --> Database

    SetupServices --> FileService
    DailyOps --> FileService
    BookingOps --> FileService
    FileService --> StoragePolicy
    FileService --> Uploads

    Web --> AuditInterceptor
    AuditInterceptor -->|successful POST, PUT, PATCH, DELETE| Persistence

    FinanceServices -. internal/test society ledger .-> Persistence
    BookingOps -. internal/test society ledger .-> Persistence
    ResponseOps -. internal/test COD ledger .-> Persistence
    PlatformServices -. membership checkout .-> Razorpay
    StoragePolicy -. recurring add-on invoice line .-> PlatformOps
    PlatformOps -. outbox dispatch .-> Messaging
    Uploads -. production replacement .-> ObjectStorage
```

## 2. Application-to-module diagram

```mermaid
flowchart LR
    subgraph Apps["Role applications"]
        SA["Super Admin"]
        OA["Society Admin"]
        ST["Staff"]
        MB["Member"]
    end

    subgraph Platform["Platform modules"]
        Societies["Societies and activation"]
        Catalog["Services and subscription rates"]
        Requirements["Custom requirements"]
        Membership["Platform membership billing"]
        PlatformSupport["Notifications, support and audit"]
        StorageAdmin["Free allowance, paid block size, price and society usage"]
    end

    subgraph Masters["Society master modules"]
        Profile["Profile and enabled services"]
        Property["Property spaces"]
        Residents["Residents and households"]
        Staff["Staff, attendance and salary"]
        Config["Parking, vendors, categories and charges"]
        SocietyStorage["Storage usage, purchases and safe cleanup"]
    end

    subgraph Finance["Society finance modules"]
        Billing["Budget and expenses"]
        Payments["Invoices, payments and receipts"]
    end

    subgraph Operations["Operational modules"]
        Complaints["Complaints and tasks"]
        Visitors["Visitors"]
        Amenities["Amenities and bookings"]
        Activities["Activities and galleries"]
        Deliveries["Deliveries and COD"]
        Emergency["Emergency and accidents"]
        Notices["Notices and documents"]
    end

    SA --> Societies
    SA --> Catalog
    SA --> Requirements
    SA --> Membership
    SA --> PlatformSupport
    SA --> StorageAdmin

    OA --> Profile
    OA --> Property
    OA --> Residents
    OA --> Staff
    OA --> Config
    OA --> SocietyStorage
    OA --> Billing
    OA --> Payments
    OA --> Complaints
    OA --> Visitors
    OA --> Amenities
    OA --> Activities
    OA --> Deliveries
    OA --> Emergency
    OA --> Notices

    ST --> Staff
    ST --> Payments
    ST --> Complaints
    ST --> Visitors
    ST --> Amenities
    ST --> Activities
    ST --> Deliveries
    ST --> Emergency
    ST --> Notices
    ST --> Residents

    MB --> Residents
    MB --> Payments
    MB --> Complaints
    MB --> Visitors
    MB --> Amenities
    MB --> Activities
    MB --> Deliveries
    MB --> Emergency
    MB --> Notices
```

Society Admin navigation is filtered by `societies.enabled_modules`. Staff actions are further
filtered by role, tenant, ownership and `staff_permissions`. Members receive only their own or
published society data.

## 3. Backend component diagram

```mermaid
flowchart LR
    Client["Flutter API client"]

    subgraph HTTP["HTTP boundary"]
        Controller["Controllers"]
        DTO["Request validation and response mapping"]
        Error["Global exception handling"]
    end

    subgraph Secure["Security boundary"]
        Jwt["JWT access-token filter"]
        Refresh["Rotating refresh sessions"]
        Roles["Role and method authorization"]
        Tenant["Authenticated society scope"]
    end

    subgraph Business["Business layer"]
        Service["Transactional services"]
        Rules["Status transitions, capacity, ownership, amount and schedule rules"]
        Events["History, notification and audit creation"]
        Quota["Storage quota, purchase and cleanup rules"]
    end

    subgraph Adapters["Persistence and file adapters"]
        Repo["Spring Data repositories"]
        Entity["JPA entities"]
        Storage["LocalDocumentStorage"]
    end

    DB[(PostgreSQL)]
    Disk[(Filesystem)]

    Client --> Controller --> DTO --> Jwt --> Roles --> Tenant --> Service
    Refresh --> Jwt
    Service --> Rules --> Events
    Service --> Quota
    Service --> Repo --> Entity --> DB
    Service --> Storage --> Disk
    Storage -->|register bytes or reject and remove file| Quota
    Error --> Client
    Controller -. failures .-> Error
```

## 4. Multi-tenant data boundary

```mermaid
flowchart TD
    Token["Authenticated token"] --> User["users.id, role and society_id"]
    User --> Role{"Role"}
    Role -->|SUPER_ADMIN| PlatformScope["Platform-wide management endpoints"]
    Role -->|SOCIETY_ADMIN| AdminScope["All allowed records for own society"]
    Role -->|STAFF| StaffScope["Own society plus staff permissions"]
    Role -->|RESIDENT| MemberScope["Own society plus ownership/published-data rules"]

    AdminScope --> Query["Repository/service query includes society_id"]
    StaffScope --> Query
    MemberScope --> Query
    Query --> DB[(Single shared societydb)]
    PlatformScope --> DB

    ClientTenant["Client-supplied society identifier"] -. never trusted as authorization .-> Query
```

The database is shared, but society data is logically isolated. A normal society user cannot
select another society merely by changing a request identifier.

## 5. Payment architecture

```mermaid
flowchart TB
    subgraph PlatformMoney["Society pays the platform"]
        PlatformRate["Usage rates × spaces/staff or fixed custom base × billing cycle"]
        StorageCharge["Purchased storage blocks × monthly block price"]
        PlatformOrder["subscription_orders"]
        PlatformCheckout["Razorpay order / verified capture / signed webhook"]
        Entitlement{"Trial active and no due unpaid invoice?"}
        Recovery["402 service gate; Society Admin payment-only recovery"]
        Cancel["Super Admin cancels untouched Due invoice with reason"]
        PlatformInvoice["Platform invoice PDF and membership period"]
        PlatformRate --> PlatformOrder
        StorageCharge -->|separate STORAGE_ADDON line| PlatformOrder
        PlatformOrder --> Entitlement
        PlatformOrder -->|no payment attempt| Cancel
        Entitlement -->|No| Recovery --> PlatformCheckout --> PlatformInvoice
        PlatformInvoice -->|Activate period| Entitlement
    end

    subgraph SocietyMoney["Member pays the society"]
        Charge["Default or member/space charge"]
        Invoice["maintenance_invoices"]
        Collection["Online test checkout or staff/admin cash record"]
        Ledger["payments and receipt"]
        Finance["Finance summary, received amount and remaining invoice balance"]
        Charge --> Invoice --> Collection --> Ledger --> Finance
    end

    subgraph ModuleMoney["Operational payments"]
        AmenityPay["Amenity booking charge/deposit"]
        ActivityPay["Activity participant fee"]
        DeliveryPay["Delivery COD"]
        Settlement["Online test payment or recorded cash with module lifecycle"]
        AmenityPay --> Settlement
        ActivityPay --> Settlement
        DeliveryPay --> Settlement
    end

    SocietyAccount["society_payment_settings"]
    PlatformAccount["platform_payment_settings"]
    Provider["Razorpay production adapter and verified webhook: external boundary"]

    PlatformCheckout --> PlatformAccount
    Collection --> SocietyAccount
    Settlement --> SocietyAccount
    PlatformAccount -->|configured credentials| Provider
    SocietyAccount -. connected-account design pending .-> Provider
```

## 6. Persistence diagram

```mermaid
flowchart LR
    API["Business services"]
    DB[(One PostgreSQL database: societydb)]
    Files[(Host upload folder)]

    subgraph DatabaseDomains["Database records"]
        Core["Platform, societies, users and sessions"]
        People["Structure, residents, households and staff"]
        Money["Subscriptions, maintenance, payments, budget and expenses"]
        Ops["Complaints, visitors, amenities, activities, deliveries, emergencies and notices"]
        Control["Support, notifications, audit and event history"]
        Paths["Relative storage keys and MIME metadata"]
        Quotas["Storage policy, society accounts, objects and purchases"]
    end

    subgraph FileDomains["Binary files"]
        Images["Images"]
        Videos["Videos"]
        Documents["Documents"]
    end

    API --> Core
    API --> People
    API --> Money
    API --> Ops
    API --> Control
    API --> Paths
    API --> Quotas
    Core --> DB
    People --> DB
    Money --> DB
    Ops --> DB
    Control --> DB
    Paths --> DB
    Quotas --> DB
    API --> Images --> Files
    API --> Videos --> Files
    API --> Documents --> Files
```

## 7. Local deployment diagram

```mermaid
flowchart LR
    Browser["Browser / mobile application"]

    subgraph Host["Developer machine"]
        Flutter["Flutter web/mobile process"]

        subgraph Docker["Docker Compose"]
            Backend["backend container<br/>Spring Boot :8080"]
            Postgres["postgres container<br/>PostgreSQL :5432"]
        end

        HostDB["Host port :5544"]
        UploadFolder["backend/data/uploads"]
        PostgresVolume["postgres_data Docker volume"]
    end

    Browser --> Flutter
    Flutter -->|API_BASE_URL localhost:8080| Backend
    Backend -->|JDBC postgres:5432| Postgres
    Postgres --> PostgresVolume
    Postgres --> HostDB
    Backend -->|bind mount /data/uploads| UploadFolder
```

## 8. Production deployment direction

```mermaid
flowchart LR
    User["Web/mobile users"] --> TLS["TLS load balancer / API gateway"]
    TLS --> App1["Stateless backend instance 1"]
    TLS --> App2["Stateless backend instance 2"]
    App1 --> DB[(Managed PostgreSQL)]
    App2 --> DB
    App1 --> Media[(Shared durable media/object storage)]
    App2 --> Media
    App1 --> Queue["Notification/outbox worker"]
    App2 --> Queue
    Queue --> Providers["Email/SMS/push providers"]
    App1 --> Payment["Platform Razorpay + verified webhooks"]
    App2 --> Payment
    App1 -. future society connected accounts .-> Settlement["Society settlement provider"]
    App2 -. future society connected accounts .-> Settlement
    Backup["Automated database and media backups"] --> DB
    Backup --> Media
    Observe["Logs, metrics, traces and alerts"] --> App1
    Observe --> App2
```

The production diagram is the recommended operational target. The repository currently runs a
single backend, PostgreSQL container, host upload folder, Razorpay integration and notification
outbox without an external dispatcher.

## 9. Repository mapping

| Diagram component | Repository location |
| --- | --- |
| Public onboarding | `frontend` |
| Role applications | `apps/super_admin`, `apps/society_admin`, `apps/staff`, `apps/member` |
| Shared Flutter layer | `packages/society_ui` |
| Controllers | `backend/src/main/java/com/societyapp/controller` |
| Authentication/security | `backend/src/main/java/com/societyapp/auth`, `backend/src/main/java/com/societyapp/security` |
| Business services | `backend/src/main/java/com/societyapp/service` |
| Persistence | `backend/src/main/java/com/societyapp/repository`, `backend/src/main/java/com/societyapp/entity` |
| Upload storage | `backend/src/main/java/com/societyapp/documents`, `backend/data/uploads` |
| Database migrations | `backend/src/main/resources/db/migration` |
| Local deployment | `docker-compose.yml` |

## 10. Complete business flow

```mermaid
flowchart TD
    Start([Visitor opens SocietyHub]) --> Intent{What do they need?}

    Intent -->|Register society| Discover["Review services and usage-based rates"]
    Discover --> Register["Submit society, capacity, modules and billing cycle"]
    Register --> Approval{Manual approval required?}
    Approval -->|Yes| SuperReview["Super Admin reviews and approves or rejects"]
    Approval -->|No| SetupCode["Issue expiring setup code"]
    SuperReview -->|Approved| SetupCode
    SuperReview -->|Rejected| Rejected([Registration closed with reason])
    SetupCode --> Activate["Society Admin activates account and sets password"]
    Activate --> Configure["Complete profile, property, spaces, residents, staff and rules"]

    Intent -->|Existing user| Login["Authenticate with role-specific application"]
    Login --> Token["JWT access token and rotating refresh session"]
    Token --> Membership{"Society membership active and invoice not due?"}
    Membership -->|No: Society Admin| Recovery["402 payment-only recovery workspace"]
    Membership -->|No: Staff/Resident| Denied([Login/refresh denied or API 402])
    Recovery --> Pay["Pay platform invoice and activate membership"] --> Membership
    Membership -->|Yes| Authorization{Role, tenant, module and permission allowed?}
    Authorization -->|No| Denied([401 or 403; no mutation])
    Authorization -->|Yes| Workspace{Role workspace}

    Workspace -->|Super Admin| Platform["Societies, catalog, rates, requirements, storage policy, billing, support and audit"]
    Workspace -->|Society Admin| Configure
    Workspace -->|Staff| StaffOps["Assigned and permitted operational work"]
    Workspace -->|Resident| MemberOps["Own household, payments and society services"]

    Configure --> Operations["Run billing, complaints, visitors, amenities, activities, deliveries, emergencies and notices"]
    StaffOps --> Operations
    MemberOps --> Operations

    Operations --> Mutation["Backend validates tenant scope and business lifecycle"]
    Platform --> Mutation
    Mutation --> Persist["Commit transactional database changes"]
    Persist --> Audit["Append immutable performer and action audit event"]
    Audit --> Notify["Create notification/outbox event when applicable"]
    Notify --> Updated([Return updated role-scoped state])
```

## 11. Storage quota, purchase and cleanup flow

```mermaid
flowchart TD
    Policy["Super Admin sets default free bytes, paid block size and monthly block price"]
    Policy --> Account["Each society storage account = free allowance + purchased capacity"]

    Upload["Authorized user uploads photo, video or document"] --> Validate["Validate file category, metadata and target ownership"]
    Validate --> Write["Write file to society-scoped storage path"]
    Write --> Lock["Lock society storage account and calculate current used bytes"]
    Lock --> Fits{New total within allowance?}
    Fits -->|No| Rollback["Delete newly written file and return quota error"]
    Fits -->|Yes| Register["Register storage object with byte size, category and uploader"]
    Register --> Attach["Attach object to visitor, activity, document or society logo"]
    Attach --> Usage["Society dashboard shows used, free, purchased and available capacity"]

    Usage --> NeedMore{Need more capacity?}
    NeedMore -->|Yes| Purchase["Society Admin buys 1–100 storage blocks"]
    Purchase --> Snapshot["Snapshot block size and monthly price; increase purchased bytes"]
    Snapshot --> Invoice["Add recurring STORAGE_ADDON line to platform membership invoice"]
    Invoice --> AuditPurchase["Record sensitive purchase audit event and performer"]

    NeedMore -->|Clean files| Select["Society Admin selects an eligible owned file"]
    Select --> SafeDelete["Delete owning metadata first, then physical file"]
    SafeDelete --> Release["Mark storage object deleted and release used quota"]
    Release --> AuditDelete["Record sensitive cleanup audit event and performer"]

    Legacy["Startup reconciliation discovers pre-existing files"] --> Protect["Register as metered legacy objects"]
    Protect --> Usage
    Protect -->|No safe owner link| Locked["Visible but protected from destructive cleanup"]
```

## 12. Sensitive activity and audit flow

```mermaid
sequenceDiagram
    actor Actor as Authenticated user
    participant API as Secured API
    participant Domain as Business service
    participant DB as PostgreSQL transaction
    participant Audit as Audit mutation interceptor
    actor SocietyAdmin as Society Admin
    actor SuperAdmin as Super Admin

    Actor->>API: POST / PUT / PATCH / DELETE
    API->>API: Validate JWT, role, society, module and permission
    API->>Domain: Execute validated command
    Domain->>DB: Commit successful change
    DB-->>Domain: Saved result
    Domain-->>API: Success response
    API->>Audit: Capture route, action, entity, actor and society snapshot
    Audit->>DB: Append immutable audit event
    API-->>Actor: Return success

    SocietyAdmin->>API: View own society sensitive activity
    API->>DB: Tenant-scoped filtered query
    DB-->>SocietyAdmin: Performer, action, target, time and safe details

    SuperAdmin->>API: Search platform audit log
    API->>DB: Platform-wide filtered and paginated query
    DB-->>SuperAdmin: Cross-society audit results
```

Audit records are created only after successful mutations. Failed validation, authentication,
authorization, or rolled-back transactions do not appear as completed platform changes.
