package owner.backflow.data.model;

import java.util.List;

public record DeadlinePolicy(
        List<String> cadenceByPropertyType,
        Boolean initialTestRequired,
        Integer reportDueDaysAfterTest,
        Integer noticeLeadDays,
        List<String> pastDueLadder,
        String calendarWindow,
        Boolean anniversaryBased,
        List<String> sourceRefs
) {
    public DeadlinePolicy {
        cadenceByPropertyType = cadenceByPropertyType == null ? List.of() : List.copyOf(cadenceByPropertyType);
        pastDueLadder = pastDueLadder == null ? List.of() : List.copyOf(pastDueLadder);
        sourceRefs = sourceRefs == null ? List.of() : List.copyOf(sourceRefs);
    }

    public static DeadlinePolicy empty() {
        return new DeadlinePolicy(List.of(), null, null, null, List.of(), null, null, List.of());
    }

    public boolean hasContent() {
        return !cadenceByPropertyType.isEmpty()
                || initialTestRequired != null
                || reportDueDaysAfterTest != null
                || noticeLeadDays != null
                || !pastDueLadder.isEmpty()
                || !isBlank(calendarWindow)
                || anniversaryBased != null;
    }

    public String reportDueLabel() {
        if (reportDueDaysAfterTest == null) {
            return "";
        }
        if (reportDueDaysAfterTest == 1) {
            return "Report due within 1 day after testing.";
        }
        return "Report due within " + reportDueDaysAfterTest + " days after testing.";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
