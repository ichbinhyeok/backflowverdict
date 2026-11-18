package owner.backflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;
import owner.backflow.config.AppLeadsProperties;
import org.springframework.stereotype.Service;

@Service
public class LeadReviewService {
    private final Path root;
    private final ObjectMapper mapper = new ObjectMapper();

    public LeadReviewService(AppLeadsProperties properties) {
        root = Path.of(properties.root()).toAbsolutePath().normalize().resolve("reviews");
    }

    public Review find(String leadId) {
        var path = path(leadId);
        if (!Files.exists(path)) return new Review("", "", "", "", "");
        try { return mapper.readValue(Files.readString(path), Review.class); }
        catch (IOException exception) { throw new IllegalStateException("Could not read saved review.", exception); }
    }

    public synchronized void save(String leadId, String confirmed, String candidates, String unknowns, String nextSteps) {
        for (var value : new String[]{confirmed, candidates, unknowns, nextSteps}) {
            if (value == null || value.isBlank() || value.length() > 8000) throw new IllegalArgumentException("Complete all four review sections, using at most 8,000 characters each.");
        }
        try {
            Files.createDirectories(root);
            Path temporary = Files.createTempFile(root, "review-", ".tmp");
            try {
                Files.writeString(temporary, mapper.writeValueAsString(new Review(confirmed.trim(), candidates.trim(), unknowns.trim(), nextSteps.trim(), Instant.now().toString())));
                Files.move(temporary, path(leadId), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } finally { Files.deleteIfExists(temporary); }
        } catch (IOException exception) { throw new IllegalStateException("Could not save review.", exception); }
    }

    private Path path(String leadId) { return root.resolve(UUID.fromString(leadId).toString() + ".json"); }

    public record Review(String confirmed, String candidates, String unknowns, String nextSteps, String savedAt) {
        public String replyText() {
            return "BACKFLOWVERDICT / REFERENCE REVIEW\n\n1. CONFIRMED INFORMATION\n" + confirmed
                    + "\n\n2. PART OR DOCUMENT CANDIDATES\n" + candidates + "\n\n3. STILL TO CONFIRM\n" + unknowns
                    + "\n\n4. NEXT ACTION\n" + nextSteps + "\n\nReference review only. The manufacturer, governing authority and qualified professional confirm compatibility, required work and return to service.";
        }
    }
}
