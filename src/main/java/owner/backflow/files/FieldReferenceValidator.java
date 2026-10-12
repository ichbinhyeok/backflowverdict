package owner.backflow.files;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import owner.backflow.data.model.decision.DecisionFieldSection;

final class FieldReferenceValidator {
    private FieldReferenceValidator() {}

    static void validate(List<DecisionFieldSection> sections, Path file) {
        var ids = new HashSet<String>();
        for (var section : sections) {
            if (blank(section.id()) || !section.id().matches("[a-z0-9-]+") || !ids.add(section.id())
                    || blank(section.title()) || blank(section.introduction())
                    || blank(section.firstColumn()) || blank(section.secondColumn()) || blank(section.thirdColumn())
                    || section.rows().isEmpty() || section.sources().isEmpty()) {
                throw new IllegalStateException("Incomplete or duplicate field reference: " + file);
            }
            if (!blank(section.diagram()) && (!section.diagram().matches("[a-z0-9-]+") || blank(section.diagramAlt()) || blank(section.caption()))) {
                throw new IllegalStateException("Invalid field diagram: " + file);
            }
            for (var row : section.rows()) {
                if (blank(row.observation()) || blank(row.meaning()) || blank(row.nextStep())
                        || (!blank(row.href()) && !safeLink(row.href()))) {
                    throw new IllegalStateException("Invalid field reference row: " + file);
                }
            }
            for (var source : section.sources()) {
                if (blank(source.title()) || blank(source.href()) || !source.href().startsWith("https://")) {
                    throw new IllegalStateException("Invalid field reference source: " + file);
                }
            }
        }
    }

    private static boolean safeLink(String href) {
        return href.startsWith("https://") || (href.startsWith("/") && !href.startsWith("//"));
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
}
