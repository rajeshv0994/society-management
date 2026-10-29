package com.societyapp.domain.type;

public enum StaffPermissionCode implements DomainCode {
    VISITORS("Visitor gate management", BuiltInServiceCode.VISITORS),
    COMPLAINTS("Complaints", BuiltInServiceCode.COMPLAINTS),
    PAYMENTS("Maintenance payments and receipts", BuiltInServiceCode.PAYMENTS),
    TASKS("Assigned tasks", BuiltInServiceCode.STAFF),
    NOTICES("Notices", BuiltInServiceCode.NOTICES),
    AMENITIES("Amenities", BuiltInServiceCode.AMENITIES),
    HOUSEHOLDS("Household verification and access", BuiltInServiceCode.RESIDENTS),
    DELIVERIES("Delivery desk and secure handover", BuiltInServiceCode.DELIVERIES),
    EMERGENCY_INCIDENTS("Emergency and accident response", BuiltInServiceCode.EMERGENCY_INCIDENTS),
    ACTIVITIES_GALLERIES(
            "Activity registrations, attendance and galleries",
            BuiltInServiceCode.ACTIVITIES_GALLERIES);

    private final String label;
    private final BuiltInServiceCode requiredModule;

    StaffPermissionCode(String label, BuiltInServiceCode requiredModule) {
        this.label = label;
        this.requiredModule = requiredModule;
    }

    public String label() {
        return label;
    }

    public BuiltInServiceCode requiredModule() {
        return requiredModule;
    }

    public static StaffPermissionCode from(String value) {
        return DomainCode.parse(StaffPermissionCode.class, value, "staff permission");
    }
}
