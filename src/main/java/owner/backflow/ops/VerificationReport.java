package owner.backflow.ops;

import java.time.LocalDate;
import java.util.List;

public record VerificationReport(
        LocalDate generatedAt,
        String status,
        String reviewerInitials,
        String note,
        FreshnessAuditSummary freshness,
        VerificationSummary summary,
        List<VerificationFinding> findings
) {
}
