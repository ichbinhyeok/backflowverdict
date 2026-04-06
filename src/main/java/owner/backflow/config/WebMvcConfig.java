package owner.backflow.config;

import owner.backflow.web.OpsEndpointAccessInterceptor;
import owner.backflow.web.SiteVisibilityInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final OpsEndpointAccessInterceptor opsEndpointAccessInterceptor;
    private final SiteVisibilityInterceptor siteVisibilityInterceptor;

    public WebMvcConfig(
            OpsEndpointAccessInterceptor opsEndpointAccessInterceptor,
            SiteVisibilityInterceptor siteVisibilityInterceptor
    ) {
        this.opsEndpointAccessInterceptor = opsEndpointAccessInterceptor;
        this.siteVisibilityInterceptor = siteVisibilityInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(siteVisibilityInterceptor);
        registry.addInterceptor(opsEndpointAccessInterceptor)
                .addPathPatterns("/ops/**");
    }
}
