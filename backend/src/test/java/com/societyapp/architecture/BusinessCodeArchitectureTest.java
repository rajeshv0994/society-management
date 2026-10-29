package com.societyapp.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class BusinessCodeArchitectureTest {
    private static final List<Path> APPLICATION_CORE =
            List.of(Path.of("src/main/java/com/societyapp/application"));
    private static final Pattern STRING_SWITCH_CASE =
            Pattern.compile("case\\s+\"[A-Z][A-Z0-9_]*\"");
    private static final Pattern RAW_IDENTITY_OR_PAYMENT_CODE =
            Pattern.compile(
                    "\"(SUPER_ADMIN|SOCIETY_ADMIN|STAFF|RESIDENT|CASH|UPI|CARD|NET_BANKING|BANK_TRANSFER|CHEQUE|MONTHLY|QUARTERLY|YEARLY)\"");
    private static final Pattern RAW_STATE_SETTER =
            Pattern.compile(
                    "\\.set(?:[A-Za-z]*Status|Role|Priority|Severity|PaymentMethod|BillingCycle)\\(\\s*\"[A-Z]");
    private static final Pattern RAW_BUSINESS_COMPARISON =
            Pattern.compile("\"[A-Z][A-Z0-9_]*\"\\.equals");

    @Test
    void interactorsDoNotImplementBusinessDecisionsWithStringSwitches() throws IOException {
        assertNoMatch(STRING_SWITCH_CASE);
    }

    @Test
    void sharedIdentityPaymentAndBillingCodesUseDomainEnums() throws IOException {
        assertNoMatch(RAW_IDENTITY_OR_PAYMENT_CODE);
    }

    @Test
    void stateAssignmentsAndComparisonsUseTypedDomainCodes() throws IOException {
        assertNoMatch(RAW_STATE_SETTER);
        assertNoMatch(RAW_BUSINESS_COMPARISON);
    }

    private static void assertNoMatch(Pattern forbidden) throws IOException {
        var violations = new ArrayList<String>();
        for (var root : APPLICATION_CORE) {
            try (var files = Files.walk(root)) {
                files.filter(path -> path.toString().endsWith(".java"))
                        .filter(
                                path -> {
                                    try {
                                        return forbidden.matcher(Files.readString(path)).find();
                                    } catch (IOException exception) {
                                        throw new IllegalStateException(exception);
                                    }
                                })
                        .map(Path::toString)
                        .forEach(violations::add);
            }
        }
        assertThat(violations)
                .as("business codes must be parsed into domain enums at use-case boundaries")
                .isEmpty();
    }
}
