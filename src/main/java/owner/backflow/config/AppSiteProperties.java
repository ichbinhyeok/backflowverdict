package owner.backflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.site")
public record AppSiteProperties(@DefaultValue("") String baseUrl) {
}
