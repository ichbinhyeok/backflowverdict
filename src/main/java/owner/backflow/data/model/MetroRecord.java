package owner.backflow.data.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record MetroRecord(
        String metroId,
        String title,
        String state,
        String metroSlug,
        List<String> cityNames,
        List<String> countyNames,
        List<String> utilityIds,
        List<String> guideSlugs,
        String description,
        String summary,
        String reviewerInitials,
        Integer staleAfterDays,
        PageStatus pageStatus,
        LocalDate lastReviewed
) {
    public MetroRecord {
        cityNames = defaultList(cityNames);
        countyNames = defaultList(countyNames);
        utilityIds = defaultList(utilityIds);
        guideSlugs = defaultList(guideSlugs);
        staleAfterDays = staleAfterDays == null ? 60 : staleAfterDays;
        pageStatus = pageStatus == null ? PageStatus.HOLD : pageStatus;
    }

    public boolean isPublishable(LocalDate today) {
        return pageStatus == PageStatus.PUBLISH && isFresh(today);
    }

    public boolean isFresh(LocalDate today) {
        if (lastReviewed == null) {
            return false;
        }
        long age = ChronoUnit.DAYS.between(lastReviewed, today);
        return age <= staleAfterDays;
    }

    private static <T> List<T> defaultList(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
