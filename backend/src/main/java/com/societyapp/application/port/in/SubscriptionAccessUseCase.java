package com.societyapp.application.port.in;

public interface SubscriptionAccessUseCase {
    record Decision(boolean allowed, String reason) {
        public static Decision permit() {
            return new Decision(true, null);
        }

        public static Decision deny(String reason) {
            return new Decision(false, reason);
        }
    }

    SubscriptionAccessUseCase.Decision evaluate(java.util.UUID societyId);

    boolean recoveryLoginAllowed(java.util.UUID societyId);
}
