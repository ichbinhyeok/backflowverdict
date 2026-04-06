package owner.backflow.web;

import java.time.LocalDate;
import owner.backflow.ops.FreshnessAuditSummary;

public record BackendHealthResponse(
        String status,
        LocalDate generatedAt,
        int totalUtilityCount,
        int publishedUtilityCount,
        long blockedUtilityCount,
        int totalGuideCount,
        int publishedGuideCount,
        long blockedGuideCount,
        int totalStateGuideCount,
        int publishedStateGuideCount,
        long blockedStateGuideCount,
        FreshnessAuditSummary freshness
) {
}
