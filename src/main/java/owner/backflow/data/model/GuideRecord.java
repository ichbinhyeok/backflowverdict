package owner.backflow.data.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record GuideRecord(
        String slug,
        String title,
        String description,
        String intro,
        String callout,
        String sourceExcerpt,
        String sourceSnapshotPath,
        String reviewerInitials,
        LocalDate lastReviewed,
        Integer staleAfterDays,
        boolean published,
        List<GuideSection> sections,
        List<SourceLink> sources
) {
    public GuideRecord {
        sections = sections == null ? List.of() : List.copyOf(sections);
        sources = sources == null ? List.of() : List.copyOf(sources);
        staleAfterDays = staleAfterDays == null ? 120 : staleAfterDays;
    }

    public boolean isPublishable(LocalDate today) {
        return published && isFresh(today);
    }

    public boolean isFresh(LocalDate today) {
        if (lastReviewed == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(lastReviewed, today) <= staleAfterDays;
    }
}
