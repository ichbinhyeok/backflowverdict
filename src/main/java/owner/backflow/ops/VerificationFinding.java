package owner.backflow.ops;

public record VerificationFinding(
        String severity,
        String pageType,
        String pageId,
        String code,
        String message
) {
}
