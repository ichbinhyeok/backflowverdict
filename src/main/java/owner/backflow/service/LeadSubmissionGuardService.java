package owner.backflow.service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import owner.backflow.config.AppLeadsProperties;
import org.springframework.stereotype.Service;

@Service
public class LeadSubmissionGuardService {
    private final AppLeadsProperties leadsProperties;
    private final Map<String, ArrayDeque<Instant>> attemptsByKey = new HashMap<>();

    public LeadSubmissionGuardService(AppLeadsProperties leadsProperties) {
        this.leadsProperties = leadsProperties;
    }

    public synchronized boolean tryAcquire(String remoteAddress) {
        if (leadsProperties.submissionRateLimitMaxAttempts() <= 0
                || leadsProperties.submissionRateLimitWindowSeconds() <= 0) {
            return true;
        }

        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(leadsProperties.submissionRateLimitWindowSeconds());
        cleanupExpired(windowStart);

        String key = normalize(remoteAddress);
        ArrayDeque<Instant> attempts = attemptsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(windowStart)) {
            attempts.removeFirst();
        }
        if (attempts.size() >= leadsProperties.submissionRateLimitMaxAttempts()) {
            return false;
        }
        attempts.addLast(now);
        return true;
    }

    public synchronized void clear() {
        attemptsByKey.clear();
    }

    private void cleanupExpired(Instant windowStart) {
        Iterator<Map.Entry<String, ArrayDeque<Instant>>> iterator = attemptsByKey.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ArrayDeque<Instant>> entry = iterator.next();
            ArrayDeque<Instant> attempts = entry.getValue();
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(windowStart)) {
                attempts.removeFirst();
            }
            if (attempts.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private String normalize(String remoteAddress) {
        if (remoteAddress == null || remoteAddress.isBlank()) {
            return "unknown";
        }
        return remoteAddress.trim().toLowerCase(Locale.US);
    }
}
