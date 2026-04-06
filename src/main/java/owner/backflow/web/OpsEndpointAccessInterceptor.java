package owner.backflow.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import owner.backflow.config.AppOpsProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class OpsEndpointAccessInterceptor implements HandlerInterceptor {
    private static final String OPS_TOKEN_HEADER = "X-Ops-Token";

    private final AppOpsProperties opsProperties;

    public OpsEndpointAccessInterceptor(AppOpsProperties opsProperties) {
        this.opsProperties = opsProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isAllowed(request)) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Ops endpoint access denied.");
        return false;
    }

    private boolean isAllowed(HttpServletRequest request) {
        if (opsProperties.allowLocalRequests()
                && isLoopback(request.getRemoteAddr())
                && !hasForwardedClientHeaders(request)) {
            return true;
        }

        String configuredToken = opsProperties.verificationToken();
        String requestToken = request.getHeader(OPS_TOKEN_HEADER);
        return configuredToken != null
                && !configuredToken.isBlank()
                && configuredToken.equals(requestToken);
    }

    private boolean hasForwardedClientHeaders(HttpServletRequest request) {
        return hasText(request.getHeader("X-Forwarded-For"))
                || hasText(request.getHeader("Forwarded"))
                || hasText(request.getHeader("X-Real-IP"));
    }

    private boolean isLoopback(String remoteAddr) {
        if (remoteAddr == null || remoteAddr.isBlank()) {
            return false;
        }
        return "127.0.0.1".equals(remoteAddr)
                || "0:0:0:0:0:0:0:1".equals(remoteAddr)
                || "::1".equals(remoteAddr);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
