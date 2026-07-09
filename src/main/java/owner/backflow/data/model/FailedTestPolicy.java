package owner.backflow.data.model;

import java.util.List;

public record FailedTestPolicy(
        Integer failedReportDeadlineHours,
        Integer repairDeadlineDays,
        Boolean retestRequired,
        Boolean failedReportMustBeSubmitted,
        Boolean inspectionRequired,
        Boolean shutoffRisk,
        FeeAmount penaltyAmount,
        List<String> sourceRefs
) {
    public FailedTestPolicy {
        sourceRefs = sourceRefs == null ? List.of() : List.copyOf(sourceRefs);
    }

    public static FailedTestPolicy empty() {
        return new FailedTestPolicy(null, null, null, null, null, null, null, List.of());
    }

    public boolean hasContent() {
        return failedReportDeadlineHours != null
                || repairDeadlineDays != null
                || retestRequired != null
                || failedReportMustBeSubmitted != null
                || inspectionRequired != null
                || shutoffRisk != null
                || (penaltyAmount != null && penaltyAmount.hasContent());
    }

    public String failedReportDeadlineLabel() {
        if (failedReportDeadlineHours == null) {
            return "";
        }
        if (failedReportDeadlineHours == 1) {
            return "Failed result due within 1 hour.";
        }
        return "Failed result due within " + failedReportDeadlineHours + " hours.";
    }

    public String repairDeadlineLabel() {
        if (repairDeadlineDays == null) {
            return "";
        }
        if (repairDeadlineDays == 1) {
            return "Repair or correction due within 1 day.";
        }
        return "Repair or correction due within " + repairDeadlineDays + " days.";
    }
}
