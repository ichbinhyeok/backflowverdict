package owner.backflow.service;

import java.time.LocalDateTime;

public record LeadDeliveryRecord(
        String deliveryId,
        String leadId,
        String utilityId,
        String providerId,
        String providerName,
        String providerEmail,
        String status,
        String channel,
        String subject,
        String body,
        String sourcePage,
        LocalDateTime createdAt,
        LocalDateTime deliveredAt,
        String note
) {
}
