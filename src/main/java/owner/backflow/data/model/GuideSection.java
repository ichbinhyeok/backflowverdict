package owner.backflow.data.model;

import java.util.List;

public record GuideSection(
        String heading,
        String body,
        List<String> bullets
) {
    public GuideSection {
        bullets = bullets == null ? List.of() : List.copyOf(bullets);
    }
}
