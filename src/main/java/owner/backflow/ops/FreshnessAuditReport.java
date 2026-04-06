package owner.backflow.ops;

import java.time.LocalDate;
import java.util.List;

public record FreshnessAuditReport(
        LocalDate generatedAt,
        FreshnessAuditSummary summary,
        List<FreshnessAuditEntry> staleUtilities,
        List<FreshnessAuditEntry> staleGuides,
        List<FreshnessAuditEntry> staleStateGuides,
        List<OpsCsvEntry> brokenLinks,
        List<OpsCsvEntry> conflicts
) {
}
