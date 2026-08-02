package owner.backflow.data.model.decision;

import java.util.List;

public record DecisionGuideRecord(
        String slug,
        String pageType,
        String canonicalPath,
        String eyebrow,
        String title,
        String description,
        String prompt,
        String answer,
        String urgency,
        String diagram,
        String diagramAlt,
        List<String> aliases,
        List<String> identifiers,
        List<DecisionChoice> choices,
        List<DecisionStep> steps,
        List<String> safeChecks,
        List<String> stopConditions,
        List<String> notThisDevice,
        List<DecisionLink> related,
        String evidenceNote,
        String lastReviewed,
        boolean published,
        boolean indexable,
        String toolType
) {
}
