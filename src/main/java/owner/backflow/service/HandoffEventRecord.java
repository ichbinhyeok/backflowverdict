package owner.backflow.service;

import java.time.LocalDateTime;

public record HandoffEventRecord(
        String eventId,
        LocalDateTime occurredAt,
        String eventType,
        String handoffId,
        String publicToken,
        String internalToken,
        String utilityId,
        String issueType,
        String resultStatus,
        String submissionStatus,
        String vendorCompanyName,
        String vendorSlug,
        String officeKey,
        String vendorEmail,
        String originPath,
        String requestPath,
        String referrer,
        String userAgent,
        String trafficClass
) {
}
