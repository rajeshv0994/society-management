package com.societyapp.domain.type;

public enum DeliveryEventType implements DomainCode {
    CREATED,
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
    LOST,
    COD_PAID_ONLINE,
    COD_RECORDED;

    public static DeliveryEventType from(String value) {
        return DomainCode.parse(DeliveryEventType.class, value, "delivery event type");
    }

    public static DeliveryEventType from(DeliveryAction action) {
        return valueOf(action.name());
    }
}
