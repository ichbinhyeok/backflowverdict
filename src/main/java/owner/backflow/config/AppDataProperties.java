package owner.backflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.data")
public record AppDataProperties(@DefaultValue("./data") String root) {
}
