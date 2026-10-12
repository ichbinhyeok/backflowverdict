package owner.backflow.data.model.decision;

public record DecisionCaseStudy(
        String title, String setting, String constraint, String documentedAction,
        String planningLesson, String publisher, String sourceTitle, String sourceUrl
) {}
