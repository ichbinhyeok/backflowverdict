package owner.backflow.service;

import java.time.LocalDateTime;

public record CtaClickRecord(
        String clickId,
        LocalDateTime clickedAt,
        String pageFamily,
        String utilityId,
        String providerId,
        String ctaType,
        String sourcePage,
        String destination,
        String referrer
) {
}
