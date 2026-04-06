package owner.backflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.delivery")
public record AppDeliveryProperties(
        @DefaultValue("false") boolean emailEnabled,
        @DefaultValue("") String fromEmail,
        @DefaultValue("BackflowVerdict") String fromName,
        @DefaultValue("") String replyTo
) {
}
