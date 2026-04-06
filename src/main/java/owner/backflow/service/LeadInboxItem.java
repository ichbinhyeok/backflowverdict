package owner.backflow.service;

import java.util.List;
import owner.backflow.data.model.ProviderRecord;

public record LeadInboxItem(
        LeadRecord lead,
        LeadAssignmentRecord assignment,
        List<ProviderRecord> availableProviders
) {
    public LeadInboxItem {
        availableProviders = availableProviders == null ? List.of() : List.copyOf(availableProviders);
    }

    public boolean assigned() {
        return assignment != null;
    }

    public boolean hasProviderCoverage() {
        return !availableProviders.isEmpty();
    }

    public long sponsoredProviderCount() {
        return availableProviders.stream().filter(ProviderRecord::isSponsored).count();
    }

    public long publicProviderCount() {
        return availableProviders.stream().filter(ProviderRecord::isPublicListing).count();
    }

    public long sponsorOnlyProviderCount() {
        return availableProviders.stream().filter(ProviderRecord::isSponsorOnlyListing).count();
    }
}
