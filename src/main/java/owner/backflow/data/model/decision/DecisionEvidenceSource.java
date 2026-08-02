package owner.backflow.data.model.decision;

public record DecisionEvidenceSource(
        String claimId,
        String publisher,
        String title,
        String url,
        String use,
        String lastVerified,
        String snapshotPath,
        java.util.List<String> guideSlugs
) {
}
