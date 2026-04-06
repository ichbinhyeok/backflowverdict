package owner.backflow.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import owner.backflow.config.AppDeliveryProperties;
import owner.backflow.data.model.ProviderRecord;
import owner.backflow.files.BackflowRegistryService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class LeadDeliveryService {
    private final LeadDeliveryRepository leadDeliveryRepository;
    private final BackflowRegistryService registryService;
    private final AppDeliveryProperties deliveryProperties;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public LeadDeliveryService(
            LeadDeliveryRepository leadDeliveryRepository,
            BackflowRegistryService registryService,
            AppDeliveryProperties deliveryProperties,
            ObjectProvider<JavaMailSender> mailSenderProvider
    ) {
        this.leadDeliveryRepository = leadDeliveryRepository;
        this.registryService = registryService;
        this.deliveryProperties = deliveryProperties;
        this.mailSenderProvider = mailSenderProvider;
    }

    public List<LeadDeliveryRecord> deliverIfPossible(LeadRecord lead) {
        List<ProviderRecord> activeSponsors = registryService.findAssignableProvidersForUtility(lead.utilityId()).stream()
                .filter(ProviderRecord::isSponsorActiveListing)
                .filter(provider -> provider.email() != null && !provider.email().isBlank())
                .toList();
        if (activeSponsors.isEmpty()) {
            return List.of();
        }
        List<LeadDeliveryRecord> deliveries = new ArrayList<>();
        for (ProviderRecord provider : activeSponsors) {
            deliveries.add(sendOrQueueEmail(lead, provider));
        }
        return deliveries;
    }

    public List<LeadDeliveryRecord> listDeliveries() {
        return leadDeliveryRepository.findAll();
    }

    public int sentCount() {
        return (int) leadDeliveryRepository.countByStatus("SENT");
    }

    public int queuedCount() {
        return (int) leadDeliveryRepository.countByStatus("QUEUED");
    }

    public int skippedCount() {
        return (int) leadDeliveryRepository.countByStatus("SKIPPED");
    }

    private LeadDeliveryRecord sendOrQueueEmail(LeadRecord lead, ProviderRecord provider) {
        String subject = "[" + safeUtilityName(lead) + "] New backflow lead";
        String body = buildEmailBody(lead, provider);
        LocalDateTime now = LocalDateTime.now();

        if (!deliveryProperties.emailEnabled() || mailSenderProvider.getIfAvailable() == null || deliveryProperties.fromEmail().isBlank()) {
            return leadDeliveryRepository.save(new LeadDeliveryRecord(
                    null,
                    lead.leadId(),
                    lead.utilityId(),
                    provider.providerId(),
                    provider.providerName(),
                    provider.email(),
                    "QUEUED",
                    "EMAIL",
                    subject,
                    body,
                    lead.sourcePage(),
                    now,
                    null,
                    "Email transport is not configured; queued for manual send."
            ));
        }

        try {
            JavaMailSender mailSender = mailSenderProvider.getObject();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(provider.email());
            message.setFrom(deliveryProperties.fromEmail());
            if (!deliveryProperties.replyTo().isBlank()) {
                message.setReplyTo(deliveryProperties.replyTo());
            }
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            return leadDeliveryRepository.save(new LeadDeliveryRecord(
                    null,
                    lead.leadId(),
                    lead.utilityId(),
                    provider.providerId(),
                    provider.providerName(),
                    provider.email(),
                    "SENT",
                    "EMAIL",
                    subject,
                    body,
                    lead.sourcePage(),
                    now,
                    now,
                    "Email delivered to active sponsor."
            ));
        } catch (MailException exception) {
            return leadDeliveryRepository.save(new LeadDeliveryRecord(
                    null,
                    lead.leadId(),
                    lead.utilityId(),
                    provider.providerId(),
                    provider.providerName(),
                    provider.email(),
                    "QUEUED",
                    "EMAIL",
                    subject,
                    body,
                    lead.sourcePage(),
                    now,
                    null,
                    "Delivery attempt failed: " + exception.getClass().getSimpleName()
            ));
        }
    }

    private String buildEmailBody(LeadRecord lead, ProviderRecord provider) {
        return """
                A new BackflowPath lead is ready for review.

                Provider: %s
                Utility: %s
                Full name: %s
                Phone: %s
                Email: %s
                City: %s
                Property type: %s
                Issue: %s
                Page family: %s
                Source page: %s
                Notes: %s
                Captured at: %s
                """.formatted(
                provider.providerName(),
                safeUtilityName(lead),
                blankToPlaceholder(lead.fullName()),
                blankToPlaceholder(lead.phone()),
                blankToPlaceholder(lead.email()),
                blankToPlaceholder(lead.city()),
                blankToPlaceholder(lead.propertyType()),
                blankToPlaceholder(lead.issueType()),
                blankToPlaceholder(lead.pageFamily()),
                blankToPlaceholder(lead.sourcePage()),
                blankToPlaceholder(lead.notes()),
                lead.capturedAt() == null ? "" : lead.capturedAt().toString()
        );
    }

    private String safeUtilityName(LeadRecord lead) {
        return lead.displayUtility().isBlank() ? blankToPlaceholder(lead.utilityId()) : lead.displayUtility();
    }

    private String blankToPlaceholder(String value) {
        return value == null || value.isBlank() ? "(blank)" : value;
    }
}
