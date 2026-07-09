package owner.backflow.data.model;

import java.util.List;

public record ReportWorkflow(
        String portalVendor,
        String portalName,
        String portalUrl,
        Integer submissionDeadlineDaysAfterTest,
        String submitter,
        Boolean paperAccepted,
        List<String> requiredIdentifiers,
        String acceptanceProof,
        FeeAmount filingFee,
        List<String> sourceRefs
) {
    public ReportWorkflow {
        requiredIdentifiers = requiredIdentifiers == null ? List.of() : List.copyOf(requiredIdentifiers);
        sourceRefs = sourceRefs == null ? List.of() : List.copyOf(sourceRefs);
    }

    public static ReportWorkflow empty() {
        return new ReportWorkflow(null, null, null, null, null, null, List.of(), null, null, List.of());
    }

    public boolean hasContent() {
        return !isBlank(portalVendor)
                || !isBlank(portalName)
                || !isBlank(portalUrl)
                || submissionDeadlineDaysAfterTest != null
                || !isBlank(submitter)
                || paperAccepted != null
                || !requiredIdentifiers.isEmpty()
                || !isBlank(acceptanceProof)
                || (filingFee != null && filingFee.hasContent());
    }

    public String deadlineLabel() {
        if (submissionDeadlineDaysAfterTest == null) {
            return "";
        }
        if (submissionDeadlineDaysAfterTest == 1) {
            return "within 1 day after testing";
        }
        return "within " + submissionDeadlineDaysAfterTest + " days after testing";
    }

    public String paperAcceptedLabel() {
        if (paperAccepted == null) {
            return "Confirm paper acceptance with the utility.";
        }
        return paperAccepted ? "Paper reports may be accepted." : "Paper reports are not accepted.";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
