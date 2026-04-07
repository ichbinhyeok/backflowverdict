package owner.backflow.service;

import java.time.LocalDateTime;

public record ProviderClaimRecord(
        String claimId,
        LocalDateTime submittedAt,
        String fullName,
        String companyName,
        String email,
        String phone,
        String website,
        String serviceArea,
        String requestType,
        String listingReference,
        String notes,
        String referrer
) {
    public String displayRequestType() {
        return humanize(requestType);
    }

    private String humanize(String value) {
        if (value == null || value.isBlank()) {
            return "Unspecified";
        }
        String spaced = value.replace('-', ' ').replace('_', ' ');
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
