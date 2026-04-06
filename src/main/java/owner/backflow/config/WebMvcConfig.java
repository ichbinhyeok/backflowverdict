package owner.backflow.config;

import owner.backflow.web.OpsEndpointAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final OpsEndpointAccessInterceptor opsEndpointAccessInterceptor;

    public WebMvcConfig(OpsEndpointAccessInterceptor opsEndpointAccessInterceptor) {
        this.opsEndpointAccessInterceptor = opsEndpointAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(opsEndpointAccessInterceptor)
                .addPathPatterns("/ops/**");
    }
}
