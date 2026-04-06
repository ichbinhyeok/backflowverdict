package owner.backflow.ops;

public record FreshnessAuditSummary(
        int utilityCount,
        int staleUtilityCount,
        int guideCount,
        int staleGuideCount,
        int stateGuideCount,
        int staleStateGuideCount,
        int brokenLinkCount,
        int conflictCount
) {
}
