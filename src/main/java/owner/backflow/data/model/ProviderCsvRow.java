package owner.backflow.data.model;

import java.time.LocalDate;

public record ProviderCsvRow(
        String providerId,
        String providerName,
        String coverageType,
        String coverageTargets,
        String listingStatus,
        String licenseOrCertificationNotes,
        String officialApprovalSourceUrl,
        String phone,
        String email,
        String siteUrl,
        String sponsorStatus,
        String pageLabel,
        LocalDate lastReviewed
) {
}
