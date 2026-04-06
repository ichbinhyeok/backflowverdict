package owner.backflow.data.model;

import java.util.List;

public record UtilityFocusContent(
        String summary,
        List<String> highlights,
        List<String> workflowSteps
) {
    public UtilityFocusContent {
        highlights = highlights == null ? List.of() : List.copyOf(highlights);
        workflowSteps = workflowSteps == null ? List.of() : List.copyOf(workflowSteps);
    }

    public boolean hasContent() {
        return (summary != null && !summary.isBlank())
                || !highlights.isEmpty()
                || !workflowSteps.isEmpty();
    }
}
