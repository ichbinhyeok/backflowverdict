package owner.backflow.service;

import java.time.LocalDateTime;

public record ProviderCommercialStateOverrideRecord(
        String providerId,
        String providerName,
        String sponsorStatus,
        LocalDateTime updatedAt,
        String updatedBy,
        String note
) {
}
