package owner.backflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.admin")
public record AppAdminProperties(
        @DefaultValue("") String username,
        @DefaultValue("") String password
) {
}
