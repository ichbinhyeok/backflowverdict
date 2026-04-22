package owner.backflow.data.model;

import java.time.LocalDate;
import java.util.List;

public record ProviderRecord(
        String providerId,
        String providerName,
        String coverageType,
        List<String> coverageTargets,
        ProviderListingStatus listingStatus,
        String licenseOrCertificationNotes,
        String officialApprovalSourceUrl,
        String phone,
        String email,
        String siteUrl,
        String listingSource,
        String pageLabel,
        LocalDate lastReviewed
) {
    public ProviderRecord {
        coverageTargets = coverageTargets == null ? List.of() : List.copyOf(coverageTargets);
        listingStatus = listingStatus == null ? ProviderListingStatus.HOLD : listingStatus;
    }

    public boolean matchesUtility(String utilityId) {
        return coverageTargets.stream().anyMatch(target -> target.equalsIgnoreCase(utilityId));
    }

    public boolean isPublicListing() {
        return listingStatus == ProviderListingStatus.PUBLIC;
    }

    public boolean isHoldListing() {
        return listingStatus == ProviderListingStatus.HOLD;
    }

    public boolean isAssignableListing() {
        return isPublicListing();
    }

    public int coverageSize() {
        return coverageTargets.size();
    }
}
