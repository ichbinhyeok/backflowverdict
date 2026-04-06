package owner.backflow.data.model;

import java.time.LocalDate;

public record CityAliasRecord(
        String city,
        String state,
        String utilityId,
        String aliasSlug,
        AliasMode aliasMode,
        String justification,
        LocalDate lastReviewed
) {
}
