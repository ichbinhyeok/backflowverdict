package owner.backflow.service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import owner.backflow.data.model.UtilityRecord;

public enum HandoffIssueType {
    GENERAL_TESTING("general-testing", "Annual or routine testing", "Annual testing", "utility-focus"),
    FAILED_TEST_REPAIR("failed-test-repair", "Failed test repair or retest", "Failed test", "failed-test"),
    IRRIGATION("irrigation", "Irrigation assembly testing", "Irrigation", "utility-focus"),
    FIRE_LINE("fire-line", "Fire line testing", "Fire line", "utility-focus");

    private final String code;
    private final String formLabel;
    private final String briefLabel;
    private final String leadPageFamily;

    HandoffIssueType(String code, String formLabel, String briefLabel, String leadPageFamily) {
        this.code = code;
        this.formLabel = formLabel;
        this.briefLabel = briefLabel;
        this.leadPageFamily = leadPageFamily;
    }

    public String code() {
        return code;
    }

    public String formLabel() {
        return formLabel;
    }

    public String briefLabel() {
        return briefLabel;
    }

    public String leadPageFamily() {
        return leadPageFamily;
    }

    public boolean supports(UtilityRecord utility) {
        if (utility == null) {
            return false;
        }
        return switch (this) {
            case GENERAL_TESTING -> utility.supportsAnnualTestingPage();
            case FAILED_TEST_REPAIR -> true;
            case IRRIGATION -> utility.supportsIrrigationPage();
            case FIRE_LINE -> utility.supportsFireLinePage();
        };
    }

    public static Optional<HandoffIssueType> from(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }
        String normalized = rawValue.trim().toLowerCase(Locale.US);
        return Arrays.stream(values())
                .filter(issueType -> issueType.code.equals(normalized))
                .findFirst();
    }
}
