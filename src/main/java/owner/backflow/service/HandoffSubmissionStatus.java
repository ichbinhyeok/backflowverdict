package owner.backflow.service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum HandoffSubmissionStatus {
    PENDING("pending-submission", "Pending utility submission"),
    SUBMITTED("submitted", "Submitted to utility");

    private final String code;
    private final String label;

    HandoffSubmissionStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static Optional<HandoffSubmissionStatus> from(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }
        String normalized = rawValue.trim().toLowerCase(Locale.US);
        return Arrays.stream(values())
                .filter(status -> status.code.equals(normalized))
                .findFirst();
    }
}
