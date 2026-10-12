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
        String toolType,
        List<DecisionFieldSection> fieldSections,
        List<DecisionCaseStudy> caseStudies,
        List<DecisionQuestion> questions
) {
    public DecisionGuideRecord {
        fieldSections = fieldSections == null ? List.of() : List.copyOf(fieldSections);
        caseStudies = caseStudies == null ? List.of() : List.copyOf(caseStudies);
        questions = questions == null ? List.of() : List.copyOf(questions);
    }
}
