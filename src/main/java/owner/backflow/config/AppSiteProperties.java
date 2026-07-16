package owner.backflow.config;

import java.time.LocalDate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.site")
public record AppSiteProperties(
        @DefaultValue("") String baseUrl,
        @DefaultValue("BackflowPath") String siteName,
        @DefaultValue("") String gaMeasurementId,
        @DefaultValue("") String supportEmail,
        @DefaultValue("") String supportPhone,
        @DefaultValue("2026-07-11") LocalDate contentLastModified
) {
}
