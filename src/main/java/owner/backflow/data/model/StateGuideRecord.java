package owner.backflow.data.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record StateGuideRecord(
        String state,
        String title,
        String description,
        String summary,
        String authoritySummary,
        String sourceExcerpt,
        String sourceSnapshotPath,
        String reviewerInitials,
        LocalDate lastVerified,
        Integer staleAfterDays,
        boolean published,
        List<String> statewideHighlights,
        List<String> featuredUtilityIds,
        List<SourceLink> sources
) {
    public StateGuideRecord {
        statewideHighlights = statewideHighlights == null ? List.of() : List.copyOf(statewideHighlights);
        featuredUtilityIds = featuredUtilityIds == null ? List.of() : List.copyOf(featuredUtilityIds);
        sources = sources == null ? List.of() : List.copyOf(sources);
        staleAfterDays = staleAfterDays == null ? 45 : staleAfterDays;
    }

    public boolean isPublishable(LocalDate today) {
        return published && isFresh(today);
    }

    public boolean isFresh(LocalDate today) {
        if (lastVerified == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(lastVerified, today) <= staleAfterDays;
    }
}
