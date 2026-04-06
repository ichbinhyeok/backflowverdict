package owner.backflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import owner.backflow.config.AppLeadsProperties;
import org.springframework.stereotype.Service;

@Service
public class CtaClickRepository {
    private final AppLeadsProperties leadsProperties;
    private final ObjectMapper jsonMapper;
    private final CsvMapper csvMapper;
    private final CsvSchema csvSchema;

    public CtaClickRepository(AppLeadsProperties leadsProperties) {
        this.leadsProperties = leadsProperties;
        this.jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
        this.csvMapper = CsvMapper.builder().findAndAddModules().build();
        this.csvSchema = csvMapper.schemaFor(CtaClickCsvRow.class).withHeader();
    }

    public synchronized CtaClickRecord save(CtaClickRecord record) {
        CtaClickRecord normalized = new CtaClickRecord(
                record.clickId() == null || record.clickId().isBlank() ? UUID.randomUUID().toString() : record.clickId().trim(),
                record.clickedAt() == null ? LocalDateTime.now() : record.clickedAt(),
                normalize(record.pageFamily()),
                normalize(record.utilityId()),
                normalize(record.providerId()),
                normalize(record.ctaType()),
                normalize(record.sourcePage()),
                normalize(record.destination()),
                normalize(record.referrer())
        );
        Path path = jsonPath();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(
                    path,
                    jsonMapper.writeValueAsString(normalized) + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
            writeCsvSnapshot(findAllInternal());
            return normalized;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save CTA click to " + path, exception);
        }
    }

    public synchronized List<CtaClickRecord> findAll() {
        return List.copyOf(findAllInternal());
    }

    private List<CtaClickRecord> findAllInternal() {
        Path path = jsonPath();
        if (!Files.exists(path)) {
            return List.of();
        }
        try {
            List<CtaClickRecord> clicks = new ArrayList<>();
            for (String line : Files.readAllLines(path)) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                clicks.add(jsonMapper.readValue(line, CtaClickRecord.class));
            }
            clicks.sort(Comparator.comparing(CtaClickRecord::clickedAt).reversed());
            return clicks;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read CTA clicks from " + path, exception);
        }
    }

    private void writeCsvSnapshot(List<CtaClickRecord> clicks) throws IOException {
        Path csvPath = csvPath();
        Files.createDirectories(csvPath.getParent());
        Files.writeString(
                csvPath,
                csvMapper.writer(csvSchema).writeValueAsString(toCsvRows(clicks)),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    private List<CtaClickCsvRow> toCsvRows(List<CtaClickRecord> clicks) {
        return clicks.stream()
                .map(click -> new CtaClickCsvRow(
                        click.clickId(),
                        click.clickedAt() == null ? "" : click.clickedAt().toString(),
                        click.pageFamily(),
                        click.utilityId(),
                        click.providerId(),
                        click.ctaType(),
                        click.sourcePage(),
                        click.destination(),
                        click.referrer()
                ))
                .toList();
    }

    private Path jsonPath() {
        return Path.of(leadsProperties.root()).resolve("cta-clicks.jsonl");
    }

    private Path csvPath() {
        return Path.of(leadsProperties.root()).resolve("cta-clicks.csv");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private record CtaClickCsvRow(
            String clickId,
            String clickedAt,
            String pageFamily,
            String utilityId,
            String providerId,
            String ctaType,
            String sourcePage,
            String destination,
            String referrer
    ) {
    }
}
