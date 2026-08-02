package owner.backflow.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import owner.backflow.config.AppDataProperties;
import owner.backflow.data.model.decision.DecisionGuideRecord;
import org.springframework.stereotype.Service;

@Service
public class DecisionGuideService {
    private final AppDataProperties dataProperties;
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private Map<String, DecisionGuideRecord> bySlug = Map.of();

    public DecisionGuideService(AppDataProperties dataProperties) {
        this.dataProperties = dataProperties;
    }

    @PostConstruct
    void load() {
        reload();
    }

    public synchronized void reload() {
        Path root = Path.of(dataProperties.root()).resolve("decision-guides");
        if (!Files.isDirectory(root)) {
            this.bySlug = Map.of();
            return;
        }
        Map<String, DecisionGuideRecord> loaded = new LinkedHashMap<>();
        try (Stream<Path> files = Files.list(root)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
                DecisionGuideRecord record = objectMapper.readValue(file.toFile(), DecisionGuideRecord.class);
                validate(record, file);
                String key = record.slug().trim().toLowerCase(Locale.US);
                if (loaded.putIfAbsent(key, record) != null) {
                    throw new IllegalStateException("Duplicate decision guide slug: " + record.slug());
                }
                long duplicatePaths = loaded.values().stream()
                        .filter(item -> item.canonicalPath().equals(record.canonicalPath()))
                        .count();
                if (duplicatePaths > 1) {
                    throw new IllegalStateException("Duplicate decision guide path: " + record.canonicalPath());
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load decision guides from " + root, exception);
        }
        this.bySlug = Map.copyOf(loaded);
    }

    public Optional<DecisionGuideRecord> findPublished(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(bySlug.get(slug.trim().toLowerCase(Locale.US)))
                .filter(DecisionGuideRecord::published);
    }

    public List<DecisionGuideRecord> listAvailable() {
        return bySlug.values().stream()
                .filter(DecisionGuideRecord::published)
                .sorted(Comparator.comparing(DecisionGuideRecord::slug))
                .toList();
    }

    public List<DecisionGuideRecord> listPublished() {
        return bySlug.values().stream()
                .filter(DecisionGuideRecord::published)
                .filter(DecisionGuideRecord::indexable)
                .sorted(Comparator.comparing(DecisionGuideRecord::slug))
                .toList();
    }

    private void validate(DecisionGuideRecord record, Path file) {
        if (record.slug() == null || record.slug().isBlank()
                || record.canonicalPath() == null || record.canonicalPath().isBlank()
                || record.title() == null || record.title().isBlank()
                || record.description() == null || record.description().isBlank()
                || record.lastReviewed() == null || record.lastReviewed().isBlank()
                || record.choices() == null || record.choices().size() < 2
                || record.steps() == null || record.steps().size() < 3
                || record.safeChecks() == null || record.safeChecks().isEmpty()
                || record.stopConditions() == null || record.stopConditions().isEmpty()
                || record.related() == null || record.related().isEmpty()
                || record.diagram() == null || record.diagram().isBlank()
                || record.diagramAlt() == null || record.diagramAlt().isBlank()) {
            throw new IllegalStateException("Incomplete decision guide: " + file);
        }
        String expectedPath = "/" + record.slug() + "/";
        if (!expectedPath.equals(record.canonicalPath())) {
            throw new IllegalStateException("Decision guide path must match slug: " + file);
        }
        if (record.toolType() != null && !record.toolType().isBlank()
                && !List.of("identifier", "diagnostic", "score", "pressure", "cost").contains(record.toolType())) {
            throw new IllegalStateException("Unknown decision guide tool type: " + file);
        }
        if (record.indexable() && !record.published()) {
            throw new IllegalStateException("An indexable guide must also be published: " + file);
        }
    }
}
