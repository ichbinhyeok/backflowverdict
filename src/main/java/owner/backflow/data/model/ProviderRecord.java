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
        String sponsorStatus,
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

    public boolean isSponsorOnlyListing() {
        return listingStatus == ProviderListingStatus.SPONSOR_ONLY;
    }

    public boolean isHoldListing() {
        return listingStatus == ProviderListingStatus.HOLD;
    }

    public boolean isAssignableListing() {
        return isPublicListing() || isSponsorActiveListing();
    }

    public boolean isSponsored() {
        return sponsorStatus != null && sponsorStatus.equalsIgnoreCase("ACTIVE");
    }

    public boolean isSponsorActiveListing() {
        return isSponsorOnlyListing() && isSponsored();
    }

    public boolean isSponsorProspect() {
        return sponsorStatus != null && sponsorStatus.equalsIgnoreCase("PROSPECT");
    }

    public boolean isSponsorProspectListing() {
        return isSponsorOnlyListing() && isSponsorProspect();
    }

    public ProviderRecord withSponsorStatus(String updatedSponsorStatus, LocalDate reviewedOn) {
        return new ProviderRecord(
                providerId,
                providerName,
                coverageType,
                coverageTargets,
                listingStatus,
                licenseOrCertificationNotes,
                officialApprovalSourceUrl,
                phone,
                email,
                siteUrl,
                updatedSponsorStatus,
                pageLabel,
                reviewedOn == null ? lastReviewed : reviewedOn
        );
    }

    public int coverageSize() {
        return coverageTargets.size();
    }
}
