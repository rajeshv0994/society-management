package com.societyapp.domain.type;

public enum MemberChargeType implements DomainCode {
    MEMBERSHIP,
    OTHER;

    public static MemberChargeType from(String value) {
        return DomainCode.parse(MemberChargeType.class, value, "member charge type");
    }
}
