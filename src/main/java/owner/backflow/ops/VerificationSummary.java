package owner.backflow.ops;

public record VerificationSummary(
        int publishedUtilityCount,
        int publishedGuideCount,
        int publishedStateGuideCount,
        int errorCount,
        int warningCount
) {
}
