package owner.backflow.web;

public record VerificationRunRequest(
        String reviewerInitials,
        String note
) {
}
