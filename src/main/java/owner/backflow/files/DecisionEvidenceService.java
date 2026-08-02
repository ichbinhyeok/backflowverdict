package owner.backflow.files;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import owner.backflow.config.AppDataProperties;
import owner.backflow.data.model.decision.DecisionEvidenceSource;
import owner.backflow.data.model.decision.DecisionGuideRecord;
import org.springframework.stereotype.Service;

/** Loads the public decision evidence ledger and fails closed on incomplete metadata. */
@Service
public class DecisionEvidenceService {
    private static final int MAX_EVIDENCE_AGE_DAYS = 365;
    private final AppDataProperties dataProperties;
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private List<DecisionEvidenceSource> sources = List.of();

    public DecisionEvidenceService(AppDataProperties dataProperties) {
        this.dataProperties = dataProperties;
    }

    @PostConstruct
    void load() {
        Path ledger = Path.of(dataProperties.root()).resolve("decision-evidence.json");
        if (!Files.isRegularFile(ledger)) {
            throw new IllegalStateException("Decision evidence ledger is missing: " + ledger);
        }
        try {
            List<DecisionEvidenceSource> loaded = objectMapper.readValue(
                    ledger.toFile(), new TypeReference<List<DecisionEvidenceSource>>() { });
            validate(loaded, ledger);
            validateAgainstBrokenLinkLedger(loaded, Path.of(dataProperties.root()).resolve("ops/broken_links.csv"));
            sources = List.copyOf(loaded);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load decision evidence ledger: " + ledger, exception);
        }
    }

    public List<DecisionEvidenceSource> forGuide(DecisionGuideRecord guide) {
        List<DecisionEvidenceSource> matches = sources.stream()
                .filter(source -> source.guideSlugs().contains("*") || source.guideSlugs().contains(guide.slug()))
                .toList();
        if (matches.isEmpty()) {
            throw new IllegalStateException("No evidence claims mapped to decision guide: " + guide.slug());
        }
        return matches;
    }

    public List<DecisionEvidenceSource> all() {
        return sources;
    }

    private void validate(List<DecisionEvidenceSource> loaded, Path ledger) {
        if (loaded == null || loaded.isEmpty()) {
            throw new IllegalStateException("Decision evidence ledger is empty: " + ledger);
        }
        Set<String> claimIds = new HashSet<>();
        LocalDate today = LocalDate.now();
        for (DecisionEvidenceSource source : loaded) {
            if (blank(source.claimId()) || blank(source.publisher()) || blank(source.title())
                    || blank(source.url()) || blank(source.use()) || blank(source.lastVerified())
                    || source.guideSlugs() == null || source.guideSlugs().isEmpty()) {
                throw new IllegalStateException("Incomplete decision evidence claim: " + source);
            }
            if (!claimIds.add(source.claimId())) {
                throw new IllegalStateException("Duplicate decision evidence claim id: " + source.claimId());
            }
            URI uri = URI.create(source.url());
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalStateException("Decision evidence must use HTTPS: " + source.claimId());
            }
            LocalDate verified = LocalDate.parse(source.lastVerified());
            if (verified.isAfter(today) || ChronoUnit.DAYS.between(verified, today) > MAX_EVIDENCE_AGE_DAYS) {
                throw new IllegalStateException("Decision evidence is stale or future-dated: " + source.claimId());
            }
            if (!blank(source.snapshotPath())) {
                Path snapshot = Path.of(source.snapshotPath());
                if (!snapshot.isAbsolute()) {
                    snapshot = Path.of(".").resolve(snapshot).normalize();
                }
                if (!Files.isRegularFile(snapshot)) {
                    throw new IllegalStateException("Decision evidence snapshot is missing: " + source.snapshotPath());
                }
            }
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private void validateAgainstBrokenLinkLedger(List<DecisionEvidenceSource> loaded, Path brokenLinkLedger) throws IOException {
        if (!Files.isRegularFile(brokenLinkLedger)) {
            throw new IllegalStateException("Broken-link ledger is missing: " + brokenLinkLedger);
        }
        List<String> unresolved = Files.readAllLines(brokenLinkLedger).stream()
                .skip(1)
                .filter(line -> !line.isBlank())
                .filter(line -> !line.toLowerCase().contains(",fixed,"))
                .toList();
        for (DecisionEvidenceSource source : loaded) {
            if (unresolved.stream().anyMatch(line -> line.contains(source.url()))) {
                throw new IllegalStateException("Decision evidence has an unresolved broken-link entry: " + source.claimId());
            }
        }
    }
}
