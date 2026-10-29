package com.societyapp.domain.type;

public enum MessageType implements DomainCode {
    TEXT;

    public static MessageType from(String value) {
        return DomainCode.parse(MessageType.class, value, "message type");
    }
}
