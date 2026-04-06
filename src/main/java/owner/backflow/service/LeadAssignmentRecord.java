package owner.backflow.service;

import java.time.LocalDateTime;

public record LeadAssignmentRecord(
        String leadId,
        String utilityId,
        String providerId,
        String providerName,
        LocalDateTime assignedAt,
        String assignedBy,
        String note
) {
}
