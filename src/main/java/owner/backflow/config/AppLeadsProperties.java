package owner.backflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.leads")
public record AppLeadsProperties(@DefaultValue("./build/leads") String root) {
}
