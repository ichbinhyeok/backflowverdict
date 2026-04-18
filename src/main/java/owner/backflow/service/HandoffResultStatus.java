package owner.backflow.service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum HandoffResultStatus {
    PASS("pass", "Passed test"),
    FAIL("fail", "Failed test"),
    UNABLE_TO_TEST("unable-to-test", "Unable to test");

    private final String code;
    private final String label;

    HandoffResultStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public boolean requiresFailureNote() {
        return this == FAIL || this == UNABLE_TO_TEST;
    }

    public static Optional<HandoffResultStatus> from(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }
        String normalized = rawValue.trim().toLowerCase(Locale.US);
        return Arrays.stream(values())
                .filter(status -> status.code.equals(normalized))
                .findFirst();
    }
}
