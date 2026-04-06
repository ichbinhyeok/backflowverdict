package owner.backflow.data.model;

import java.time.LocalDate;

public record CityAliasCsvRow(
        String city,
        String state,
        String utilityId,
        String aliasSlug,
        String aliasMode,
        String justification,
        LocalDate lastReviewed
) {
}
