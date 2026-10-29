package com.societyapp.domain.type;

public enum SocietyDocumentType implements DomainCode {
    REGISTRATION_CERTIFICATE,
    GST,
    PAN,
    ADDRESS_PROOF,
    OTHER;

    public static SocietyDocumentType from(String value) {
        return DomainCode.parse(SocietyDocumentType.class, value, "society document type");
    }
}
