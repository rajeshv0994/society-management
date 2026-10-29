# Backend Clean Architecture Refactoring

The backend already enforces inward dependency direction. The remaining migration replaces broad
feature facades and generic map contracts with small use cases, explicit application models, and
HTTP-only response mapping.

## Enforced rules

- New application input ports cannot expose `Map`, `Map<String, Object>`, or raw `Object` values.
- New use-case classes have a 350-line ceiling; new controllers have a 250-line ceiling.
- Existing oversized classes cannot grow beyond their recorded baseline. Their ceilings should be
  removed from `SourceSizeArchitectureTest` as each class is split below the target.
- Application models remain framework-free. Jackson annotations and HTTP naming belong to
  controller response models and HTTP mappers.
- Controllers depend on input ports, never repositories or concrete interactors.
- Interactor collaboration occurs through input ports.

## Completed migration slice

Platform notification administration now has a dedicated input port, use case, application read
models, pure application mapper, HTTP response model, HTTP mapper, and controller. It also performs
bulk society-name resolution instead of one repository query per notification.

## Remaining migration order

1. Split platform support, catalog, dashboard, society detail, and society lifecycle out of
   `PlatformAdminService`.
2. Divide activity commands, participation/payment commands, gallery commands, queries, and
   mappers currently held by `ActivityService`.
3. Split parking, vendor, complaint-category, maintenance-rule, permission, and setup-readiness
   responsibilities from `ConfigurationService`.
4. Apply the same command/query/model/mapper pattern to emergency, household, delivery, billing,
   amenity, staff, resident, visitor, and complaint boundaries.
5. Remove each migrated interface from `TypedBoundaryArchitectureTest.LEGACY_UNTYPED_PORTS` and
   remove each reduced class from the source-size baseline.

Refactoring must preserve routes and JSON field names so the five Flutter clients remain backward
compatible throughout the migration.
