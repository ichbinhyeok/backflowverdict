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
import java.util.Locale;
import java.util.Optional;
import owner.backflow.config.AppLeadsProperties;
import org.springframework.stereotype.Service;

@Service
public class ProviderCommercialStateRepository {
    private final AppLeadsProperties leadsProperties;
    private final ObjectMapper jsonMapper;
    private final CsvMapper csvMapper;
    private final CsvSchema csvSchema;

    public ProviderCommercialStateRepository(AppLeadsProperties leadsProperties) {
        this.leadsProperties = leadsProperties;
        this.jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
        this.csvMapper = CsvMapper.builder()
                .findAndAddModules()
                .build();
        this.csvSchema = csvMapper.schemaFor(ProviderCommercialStateCsvRow.class).withHeader();
    }

    public synchronized ProviderCommercialStateOverrideRecord save(ProviderCommercialStateOverrideRecord record) {
        ProviderCommercialStateOverrideRecord normalized = new ProviderCommercialStateOverrideRecord(
                normalize(record.providerId()),
                normalize(record.providerName()),
                normalizeStatus(record.sponsorStatus()),
                record.updatedAt() == null ? LocalDateTime.now() : record.updatedAt(),
                normalize(record.updatedBy()),
                normalize(record.note())
        );
        List<ProviderCommercialStateOverrideRecord> overrides = new ArrayList<>(findAllInternal());
        overrides.removeIf(existing -> existing.providerId().equalsIgnoreCase(normalized.providerId()));
        overrides.add(normalized);
        writeOverrides(overrides);
        return normalized;
    }

    public synchronized Optional<ProviderCommercialStateOverrideRecord> findByProviderId(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return Optional.empty();
        }
        return findAllInternal().stream()
                .filter(record -> record.providerId().equalsIgnoreCase(providerId.trim()))
                .findFirst();
    }

    public synchronized List<ProviderCommercialStateOverrideRecord> findAll() {
        return List.copyOf(findAllInternal());
    }

    private List<ProviderCommercialStateOverrideRecord> findAllInternal() {
        Path path = jsonPath();
        if (!Files.exists(path)) {
            return List.of();
        }
        try {
            List<ProviderCommercialStateOverrideRecord> records = jsonMapper.readerForListOf(ProviderCommercialStateOverrideRecord.class)
                    .readValue(path.toFile());
            records.sort(Comparator.comparing(ProviderCommercialStateOverrideRecord::updatedAt).reversed());
            return records;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read provider commercial state overrides from " + path, exception);
        }
    }

    private void writeOverrides(List<ProviderCommercialStateOverrideRecord> overrides) {
        Path jsonPath = jsonPath();
        Path csvPath = csvPath();
        try {
            Files.createDirectories(jsonPath.getParent());
            List<ProviderCommercialStateOverrideRecord> ordered = overrides.stream()
                    .sorted(Comparator.comparing(ProviderCommercialStateOverrideRecord::updatedAt).reversed())
                    .toList();
            Files.writeString(
                    jsonPath,
                    jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ordered),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
            Files.writeString(
                    csvPath,
                    csvMapper.writer(csvSchema).writeValueAsString(toCsvRows(ordered)),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to persist provider commercial state overrides.", exception);
        }
    }

    private List<ProviderCommercialStateCsvRow> toCsvRows(List<ProviderCommercialStateOverrideRecord> overrides) {
        return overrides.stream()
                .map(record -> new ProviderCommercialStateCsvRow(
                        record.providerId(),
                        record.providerName(),
                        record.sponsorStatus(),
                        record.updatedAt() == null ? "" : record.updatedAt().toString(),
                        record.updatedBy(),
                        record.note()
                ))
                .toList();
    }

    private Path jsonPath() {
        return Path.of(leadsProperties.root()).resolve("provider-commercial-state.json");
    }

    private Path csvPath() {
        return Path.of(leadsProperties.root()).resolve("provider-commercial-state.csv");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeStatus(String value) {
        return normalize(value).toUpperCase(Locale.US);
    }

    private record ProviderCommercialStateCsvRow(
            String providerId,
            String providerName,
            String sponsorStatus,
            String updatedAt,
            String updatedBy,
            String note
    ) {
    }
}
