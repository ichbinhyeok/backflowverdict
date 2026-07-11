package owner.backflow.web;

import java.util.List;

public record NoticeFinderResult(
        String label,
        String path,
        String resultType,
        String summary,
        List<String> signals,
        int score
) {
    public NoticeFinderResult {
        signals = signals == null ? List.of() : List.copyOf(signals);
    }

    public String actionLabel() {
        String normalizedPath = path == null ? "" : path.toLowerCase();
        if (normalizedPath.contains("failed-test") || normalizedPath.contains("failed-backflow-test")) {
            return "Repair and retest";
        }
        if (normalizedPath.contains("submit-backflow-report") || normalizedPath.contains("backflow-reporting-portal")) {
            return "Submit the report";
        }
        if (normalizedPath.contains("approved-testers") || normalizedPath.contains("approved-backflow-testers") || normalizedPath.contains("find-a-tester")) {
            return "Find an eligible tester";
        }
        if (normalizedPath.contains("annual-testing") || normalizedPath.contains("annual-backflow-testing")) {
            return "Schedule required testing";
        }
        return "Open the local workflow";
    }

    public String actionSummary() {
        return switch (actionLabel()) {
            case "Repair and retest" -> "Follow the repair and retest steps before treating the notice as resolved.";
            case "Submit the report" -> "Use the named utility or portal route and keep proof that the report was accepted.";
            case "Find an eligible tester" -> "Confirm the utility's approval, registration, or certification rule before booking.";
            case "Schedule required testing" -> "Confirm the assembly and deadline, then schedule the required test.";
            default -> "Open the local utility workflow to confirm the rule before scheduling work.";
        };
    }

    public String dueSummary() {
        return signalStartingWith("Due basis: ")
                .map(this::compact)
                .orElse("Use the local route to confirm the exact due date, annual window, or failed-test deadline.");
    }

    public String submissionSummary() {
        return signalStartingWith("Portal: ")
                .map(portal -> "Use " + portal + " and the utility's accepted submission path.")
                .orElse("Use the submission method listed on the governing utility page.");
    }

    public String testerSummary() {
        java.util.Optional<String> testerGate = signalStartingWith("Tester gate: ")
                .map(gate -> "Tester status: " + gate + ".")
                ;
        if (testerGate.isPresent()) {
            return testerGate.get();
        }
        String due = signalStartingWith("Due basis: ").orElse("").toLowerCase();
        if (due.contains("licensed tester") || due.contains("registered tester") || due.contains("certified tester")) {
            return "Use a qualified tester registered or certified by the governing utility.";
        }
        return "Confirm tester eligibility with the governing utility before scheduling.";
    }

    private String compact(String value) {
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 190) {
            return normalized;
        }
        return normalized.substring(0, 187).trim() + "…";
    }

    private java.util.Optional<String> signalStartingWith(String prefix) {
        return signals.stream()
                .filter(signal -> signal != null && signal.startsWith(prefix))
                .map(signal -> signal.substring(prefix.length()))
                .findFirst();
    }
}
