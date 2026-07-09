package owner.backflow.data.model;

import java.util.List;

public record TesterGate(
        Boolean licenseRequired,
        Boolean utilityRegistrationRequired,
        Boolean portalEnrollmentRequired,
        List<String> credentialDocuments,
        Integer gaugeCalibrationWindowMonths,
        Boolean offListAllowedIfAccepted,
        String deviceScopeLimit,
        List<String> sourceRefs
) {
    public TesterGate {
        credentialDocuments = credentialDocuments == null ? List.of() : List.copyOf(credentialDocuments);
        sourceRefs = sourceRefs == null ? List.of() : List.copyOf(sourceRefs);
    }

    public static TesterGate empty() {
        return new TesterGate(null, null, null, List.of(), null, null, null, List.of());
    }

    public boolean hasContent() {
        return licenseRequired != null
                || utilityRegistrationRequired != null
                || portalEnrollmentRequired != null
                || !credentialDocuments.isEmpty()
                || gaugeCalibrationWindowMonths != null
                || offListAllowedIfAccepted != null
                || !isBlank(deviceScopeLimit);
    }

    public String credentialSummary() {
        if (credentialDocuments.isEmpty()) {
            return "Confirm tester credentials with the utility before scheduling.";
        }
        return String.join(", ", credentialDocuments);
    }

    public String calibrationLabel() {
        if (gaugeCalibrationWindowMonths == null) {
            return "";
        }
        return "Gauge calibration within " + gaugeCalibrationWindowMonths + " months";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
