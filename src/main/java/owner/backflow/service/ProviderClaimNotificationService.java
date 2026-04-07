package owner.backflow.service;

import owner.backflow.config.AppDeliveryProperties;
import owner.backflow.config.AppSiteProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ProviderClaimNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(ProviderClaimNotificationService.class);

    private final AppDeliveryProperties deliveryProperties;
    private final AppSiteProperties siteProperties;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String mailPassword;

    public ProviderClaimNotificationService(
            AppDeliveryProperties deliveryProperties,
            AppSiteProperties siteProperties,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.password:}") String mailPassword
    ) {
        this.deliveryProperties = deliveryProperties;
        this.siteProperties = siteProperties;
        this.mailSenderProvider = mailSenderProvider;
        this.mailPassword = mailPassword == null ? "" : mailPassword.trim();
    }

    public boolean notifyInbox(ProviderClaimRecord claim) {
        if (!deliveryProperties.emailEnabled()
                || deliveryProperties.fromEmail().isBlank()
                || mailPassword.isBlank()
                || siteProperties.supportEmail() == null
                || siteProperties.supportEmail().isBlank()
                || mailSenderProvider.getIfAvailable() == null) {
            return false;
        }

        try {
            JavaMailSender mailSender = mailSenderProvider.getObject();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(siteProperties.supportEmail().trim());
            message.setFrom(deliveryProperties.fromEmail().trim());
            if (claim.email() != null && !claim.email().isBlank()) {
                message.setReplyTo(claim.email().trim());
            } else if (!deliveryProperties.replyTo().isBlank()) {
                message.setReplyTo(deliveryProperties.replyTo().trim());
            }
            message.setSubject(buildSubject(claim));
            message.setText(buildBody(claim));
            mailSender.send(message);
            return true;
        } catch (MailException exception) {
            logger.warn("Failed to send provider claim notification for claim {}", claim.claimId(), exception);
            return false;
        }
    }

    private String buildSubject(ProviderClaimRecord claim) {
        return "[Provider request] "
                + blankToPlaceholder(claim.companyName())
                + " - "
                + claim.displayRequestType();
    }

    private String buildBody(ProviderClaimRecord claim) {
        return """
                A new provider request was submitted on BackflowPath.

                Company: %s
                Contact: %s
                Email: %s
                Phone: %s
                Website: %s
                Request type: %s
                Service area: %s
                Listing reference: %s
                Notes: %s
                Referrer: %s
                Submitted at: %s
                """.formatted(
                blankToPlaceholder(claim.companyName()),
                blankToPlaceholder(claim.fullName()),
                blankToPlaceholder(claim.email()),
                blankToPlaceholder(claim.phone()),
                blankToPlaceholder(claim.website()),
                claim.displayRequestType(),
                blankToPlaceholder(claim.serviceArea()),
                blankToPlaceholder(claim.listingReference()),
                blankToPlaceholder(claim.notes()),
                blankToPlaceholder(claim.referrer()),
                claim.submittedAt() == null ? "(blank)" : claim.submittedAt().toString()
        );
    }

    private String blankToPlaceholder(String value) {
        return value == null || value.isBlank() ? "(blank)" : value;
    }
}
