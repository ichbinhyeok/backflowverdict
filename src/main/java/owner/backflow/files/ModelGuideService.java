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
import owner.backflow.data.model.product.ModelGuideRecord;
import org.springframework.stereotype.Service;

@Service
public class ModelGuideService {
    private final AppDataProperties dataProperties;
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private Map<String, ModelGuideRecord> bySlug = Map.of();

    public ModelGuideService(AppDataProperties dataProperties) {
        this.dataProperties = dataProperties;
    }

    @PostConstruct
    void load() {
        reload();
    }

    public synchronized void reload() {
        Path root = Path.of(dataProperties.root()).resolve("model-guides");
        if (!Files.isDirectory(root)) {
            bySlug = Map.of();
            return;
        }
        Map<String, ModelGuideRecord> loaded = new LinkedHashMap<>();
        try (Stream<Path> files = Files.list(root)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
                ModelGuideRecord record = objectMapper.readValue(file.toFile(), ModelGuideRecord.class);
                validate(record, file);
                String key = record.slug().trim().toLowerCase(Locale.US);
                if (loaded.putIfAbsent(key, record) != null) {
                    throw new IllegalStateException("Duplicate model guide slug: " + record.slug());
                }
                long duplicatePaths = loaded.values().stream()
                        .filter(item -> item.canonicalPath().equals(record.canonicalPath()))
                        .count();
                if (duplicatePaths > 1) {
                    throw new IllegalStateException("Duplicate model guide path: " + record.canonicalPath());
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load model guides from " + root, exception);
        }
        bySlug = Map.copyOf(loaded);
    }

    public Optional<ModelGuideRecord> findPublished(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(bySlug.get(slug.trim().toLowerCase(Locale.US)))
                .filter(ModelGuideRecord::published);
    }

    public List<ModelGuideRecord> listPublished() {
        return bySlug.values().stream()
                .filter(ModelGuideRecord::published)
                .sorted(Comparator.comparing(ModelGuideRecord::manufacturer).thenComparing(ModelGuideRecord::model))
                .toList();
    }

    public List<ModelGuideRecord> listIndexable() {
        return listPublished().stream().filter(ModelGuideRecord::indexable).toList();
    }

    private void validate(ModelGuideRecord record, Path file) {
        if (record.slug() == null || record.slug().isBlank()
                || record.canonicalPath() == null || !record.canonicalPath().equals("/models/" + record.slug() + "/")
                || record.manufacturer() == null || record.manufacturer().isBlank()
                || record.model() == null || record.model().isBlank()
                || record.title() == null || record.title().isBlank()
                || record.description() == null || record.description().isBlank()
                || record.sizes() == null || record.sizes().isEmpty()
                || record.identifiers() == null || record.identifiers().size() < 2
                || record.kits() == null || record.kits().size() < 2
                || record.sources() == null || record.sources().isEmpty()
                || record.lastReviewed() == null || record.lastReviewed().isBlank()) {
            throw new IllegalStateException("Incomplete model guide: " + file);
        }
        if (record.indexable() && !record.published()) {
            throw new IllegalStateException("An indexable model guide must be published: " + file);
        }
    }
}
