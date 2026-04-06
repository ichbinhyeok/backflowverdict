package owner.backflow.web;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Locale;
import owner.backflow.config.AppSiteProperties;
import org.springframework.stereotype.Service;

@Service
public class SiteVisibilityService {
    private final AppSiteProperties siteProperties;

    public SiteVisibilityService(AppSiteProperties siteProperties) {
        this.siteProperties = siteProperties;
    }

    public PageMeta apply(PageMeta page, HttpServletRequest request) {
        if (page == null || page.noindex() || !shouldForceNoindex(request)) {
            return page;
        }
        return page.withNoindex(true);
    }

    public boolean shouldForceNoindex(HttpServletRequest request) {
        String requestHost = normalizedRequestHost(request);
        if (requestHost.isBlank() || isLocalHost(requestHost)) {
            return false;
        }

        String canonicalHost = canonicalHost();
        if (canonicalHost.isBlank()) {
            return false;
        }
        return !requestHost.equals(canonicalHost);
    }

    public String stagingRobotsTxt() {
        return "User-agent: *\n"
                + "Disallow: /\n";
    }

    private String canonicalHost() {
        String baseUrl = siteProperties.baseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        try {
            return normalizeHost(URI.create(baseUrl.trim()).getHost());
        } catch (IllegalArgumentException exception) {
            return "";
        }
    }

    private String normalizedRequestHost(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        return normalizeHost(request.getServerName());
    }

    private boolean isLocalHost(String host) {
        return "localhost".equals(host)
                || "127.0.0.1".equals(host)
                || "0:0:0:0:0:0:0:1".equals(host)
                || "::1".equals(host);
    }

    private String normalizeHost(String host) {
        return host == null ? "" : host.trim().toLowerCase(Locale.US);
    }
}
