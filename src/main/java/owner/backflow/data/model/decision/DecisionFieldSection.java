package owner.backflow.data.model.decision;

import java.util.List;

public record DecisionFieldSection(
        String id, String title, String introduction,
        String firstColumn, String secondColumn, String thirdColumn,
        List<DecisionFieldRow> rows, List<DecisionLink> sources,
        String diagram, String diagramAlt, String caption
) {
    public DecisionFieldSection {
        rows = rows == null ? List.of() : List.copyOf(rows);
        sources = sources == null ? List.of() : List.copyOf(sources);
    }
}
