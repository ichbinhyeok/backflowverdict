package owner.backflow.ops;

import java.time.LocalDate;

public record FreshnessAuditEntry(
        String pageType,
        String pageId,
        String title,
        String path,
        LocalDate lastVerified,
        Integer staleAfterDays,
        long ageDays
) {
}
