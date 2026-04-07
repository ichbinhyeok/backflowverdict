package owner.backflow.service;

import jakarta.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminCsrfService {
    static final String ADMIN_CSRF_SESSION_KEY = "adminCsrfToken";

    public String ensureToken(HttpSession session) {
        Object existing = session.getAttribute(ADMIN_CSRF_SESSION_KEY);
        if (existing instanceof String token && !token.isBlank()) {
            return token;
        }
        String token = UUID.randomUUID().toString();
        session.setAttribute(ADMIN_CSRF_SESSION_KEY, token);
        return token;
    }

    public void rotate(HttpSession session) {
        session.setAttribute(ADMIN_CSRF_SESSION_KEY, UUID.randomUUID().toString());
    }

    public void requireValid(HttpSession session, String candidateToken) {
        Object existing = session.getAttribute(ADMIN_CSRF_SESSION_KEY);
        if (!(existing instanceof String expectedToken) || expectedToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing CSRF session token.");
        }
        String provided = candidateToken == null ? "" : candidateToken.trim();
        if (provided.isBlank() || !MessageDigest.isEqual(
                expectedToken.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid CSRF token.");
        }
    }
}
