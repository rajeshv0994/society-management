package com.societyapp.domain.type;

public enum DeliveryAction implements DomainCode {
    ACCEPT,
    REJECT,
    AUTHORIZE,
    CANCEL,
    RECEIVE,
    MARK_READY,
    OUT_FOR_DELIVERY,
    HANDOVER,
    DELIVER,
    RETURN,
    DAMAGED,
    LOST;

    public static DeliveryAction from(String value) {
        return DomainCode.parse(DeliveryAction.class, value, "delivery action");
    }
}
