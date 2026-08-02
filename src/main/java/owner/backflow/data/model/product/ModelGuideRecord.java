package owner.backflow.data.model.product;

import java.util.List;

public record ModelGuideRecord(
        String slug,
        String canonicalPath,
        String manufacturer,
        String model,
        List<String> aliases,
        String deviceType,
        String title,
        String description,
        String summary,
        String status,
        String statusNote,
        String diagram,
        String diagramAlt,
        List<String> sizes,
        List<String> identifiers,
        List<ModelKitRecord> kits,
        List<ModelSourceRecord> sources,
        String lastReviewed,
        boolean published,
        boolean indexable
) {
}
