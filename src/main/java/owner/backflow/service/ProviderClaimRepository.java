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
public class ProviderClaimRepository {
    private static final char CSV_FORMULA_ESCAPE = '\'';

    private final AppLeadsProperties leadsProperties;
    private final ObjectMapper jsonMapper;
    private final CsvMapper csvMapper;
    private final CsvSchema csvSchema;

    public ProviderClaimRepository(AppLeadsProperties leadsProperties) {
        this.leadsProperties = leadsProperties;
        this.jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
        this.csvMapper = CsvMapper.builder()
                .findAndAddModules()
                .build();
        this.csvSchema = csvMapper.schemaFor(ProviderClaimCsvRow.class).withHeader();
    }

    public synchronized ProviderClaimRecord save(ProviderClaimRecord providerClaimRecord) {
        ProviderClaimRecord record = new ProviderClaimRecord(
                providerClaimRecord.claimId() == null || providerClaimRecord.claimId().isBlank()
                        ? UUID.randomUUID().toString()
                        : providerClaimRecord.claimId().trim(),
                providerClaimRecord.submittedAt() == null ? LocalDateTime.now() : providerClaimRecord.submittedAt(),
                normalize(providerClaimRecord.fullName()),
                normalize(providerClaimRecord.companyName()),
                normalize(providerClaimRecord.email()),
                normalize(providerClaimRecord.phone()),
                normalize(providerClaimRecord.website()),
                normalize(providerClaimRecord.serviceArea()),
                normalize(providerClaimRecord.requestType()),
                normalize(providerClaimRecord.listingReference()),
                normalize(providerClaimRecord.notes()),
                normalize(providerClaimRecord.referrer())
        );

        Path path = jsonPath();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(
                    path,
                    jsonMapper.writeValueAsString(record) + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
            writeCsvSnapshot(findAllInternal());
            return record;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save provider claim to " + path, exception);
        }
    }

    public synchronized List<ProviderClaimRecord> findAll() {
        return List.copyOf(findAllInternal());
    }

    public synchronized int count() {
        return findAllInternal().size();
    }

    private List<ProviderClaimRecord> findAllInternal() {
        Path path = jsonPath();
        if (!Files.exists(path)) {
            return List.of();
        }
        try {
            List<ProviderClaimRecord> claims = new ArrayList<>();
            for (String line : Files.readAllLines(path)) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                claims.add(jsonMapper.readValue(line, ProviderClaimRecord.class));
            }
            claims.sort(Comparator.comparing(ProviderClaimRecord::submittedAt).reversed());
            return claims;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read provider claims from " + path, exception);
        }
    }

    private void writeCsvSnapshot(List<ProviderClaimRecord> claims) throws IOException {
        Path csvPath = csvPath();
        Files.createDirectories(csvPath.getParent());
        Files.writeString(
                csvPath,
                csvMapper.writer(csvSchema).writeValueAsString(toCsvRows(claims)),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    private List<ProviderClaimCsvRow> toCsvRows(List<ProviderClaimRecord> claims) {
        return claims.stream()
                .map(claim -> new ProviderClaimCsvRow(
                        sanitizeCsvCell(claim.claimId()),
                        claim.submittedAt() == null ? "" : claim.submittedAt().toString(),
                        sanitizeCsvCell(claim.fullName()),
                        sanitizeCsvCell(claim.companyName()),
                        sanitizeCsvCell(claim.email()),
                        sanitizeCsvCell(claim.phone()),
                        sanitizeCsvCell(claim.website()),
                        sanitizeCsvCell(claim.serviceArea()),
                        sanitizeCsvCell(claim.requestType()),
                        sanitizeCsvCell(claim.listingReference()),
                        sanitizeCsvCell(claim.notes()),
                        sanitizeCsvCell(claim.referrer())
                ))
                .toList();
    }

    private String sanitizeCsvCell(String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return normalized;
        }
        char firstChar = normalized.charAt(0);
        if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@'
                || firstChar == '\t' || firstChar == '\r') {
            return CSV_FORMULA_ESCAPE + normalized;
        }
        return normalized;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private Path jsonPath() {
        return Path.of(leadsProperties.root()).resolve("provider-claims.jsonl");
    }

    private Path csvPath() {
        return Path.of(leadsProperties.root()).resolve("provider-claims.csv");
    }

    private record ProviderClaimCsvRow(
            String claimId,
            String submittedAt,
            String fullName,
            String companyName,
            String email,
            String phone,
            String website,
            String serviceArea,
            String requestType,
            String listingReference,
            String notes,
            String referrer
    ) {
    }
}
