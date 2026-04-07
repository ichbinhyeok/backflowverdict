package owner.backflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
public class LeadRepository {
    private static final char CSV_FORMULA_ESCAPE = '\'';

    private final AppLeadsProperties leadsProperties;
    private final ObjectMapper jsonMapper;
    private final CsvMapper csvMapper;
    private final CsvSchema csvSchema;

    public LeadRepository(AppLeadsProperties leadsProperties) {
        this.leadsProperties = leadsProperties;
        this.jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
        this.csvMapper = CsvMapper.builder()
                .findAndAddModules()
                .build();
        this.csvSchema = csvMapper.schemaFor(LeadCsvRow.class).withHeader();
    }

    public synchronized LeadRecord save(LeadRecord leadRecord) {
        LeadRecord record = new LeadRecord(
                leadRecord.leadId() == null || leadRecord.leadId().isBlank()
                        ? UUID.randomUUID().toString()
                        : leadRecord.leadId().trim(),
                leadRecord.capturedAt() == null ? LocalDateTime.now() : leadRecord.capturedAt(),
                normalize(leadRecord.fullName()),
                normalize(leadRecord.phone()),
                normalize(leadRecord.email()),
                normalize(leadRecord.city()),
                normalize(leadRecord.utilityId()),
                normalize(leadRecord.utilityName()),
                normalize(leadRecord.propertyType()),
                normalize(leadRecord.issueType()),
                normalize(leadRecord.pageFamily()),
                normalize(leadRecord.notes()),
                normalize(leadRecord.sourcePage()),
                normalize(leadRecord.referrer()),
                normalize(leadRecord.submittedUtilityId()),
                normalize(leadRecord.submittedUtilityName()),
                normalize(leadRecord.submittedPageFamily()),
                normalize(leadRecord.submittedSourcePage()),
                normalize(leadRecord.routingStatus()),
                normalize(leadRecord.routingReason())
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
            throw new IllegalStateException("Failed to save lead to " + path, exception);
        }
    }

    public synchronized List<LeadRecord> findAll() {
        return List.copyOf(findAllInternal());
    }

    public synchronized LeadRecord findById(String leadId) {
        if (leadId == null || leadId.isBlank()) {
            return null;
        }
        return findAllInternal().stream()
                .filter(record -> record.leadId().equalsIgnoreCase(leadId.trim()))
                .findFirst()
                .orElse(null);
    }

    public synchronized String exportJson() {
        try {
            return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(findAllInternal());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to serialize lead export JSON.", exception);
        }
    }

    public synchronized String exportCsv() {
        try {
            return csvMapper.writer(csvSchema).writeValueAsString(toCsvRows(findAllInternal()));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to serialize lead export CSV.", exception);
        }
    }

    public synchronized int count() {
        return findAllInternal().size();
    }

    private List<LeadRecord> findAllInternal() {
        Path path = jsonPath();
        if (!Files.exists(path)) {
            return List.of();
        }
        try {
            List<LeadRecord> leads = new ArrayList<>();
            for (String line : Files.readAllLines(path)) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                leads.add(jsonMapper.readValue(line, LeadRecord.class));
            }
            leads.sort(Comparator.comparing(LeadRecord::capturedAt).reversed());
            return leads;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read leads from " + path, exception);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private void writeCsvSnapshot(List<LeadRecord> leads) throws IOException {
        Path csvPath = csvPath();
        Files.createDirectories(csvPath.getParent());
        Files.writeString(
                csvPath,
                csvMapper.writer(csvSchema).writeValueAsString(toCsvRows(leads)),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    private List<LeadCsvRow> toCsvRows(List<LeadRecord> leads) {
        return leads.stream()
                .map(lead -> new LeadCsvRow(
                        sanitizeCsvCell(lead.leadId()),
                        lead.capturedAt() == null ? "" : lead.capturedAt().toString(),
                        sanitizeCsvCell(lead.fullName()),
                        sanitizeCsvCell(lead.phone()),
                        sanitizeCsvCell(lead.email()),
                        sanitizeCsvCell(lead.city()),
                        sanitizeCsvCell(lead.utilityId()),
                        sanitizeCsvCell(lead.utilityName()),
                        sanitizeCsvCell(lead.propertyType()),
                        sanitizeCsvCell(lead.issueType()),
                        sanitizeCsvCell(lead.pageFamily()),
                        sanitizeCsvCell(lead.notes()),
                        sanitizeCsvCell(lead.sourcePage()),
                        sanitizeCsvCell(lead.referrer()),
                        sanitizeCsvCell(lead.submittedUtilityId()),
                        sanitizeCsvCell(lead.submittedUtilityName()),
                        sanitizeCsvCell(lead.submittedPageFamily()),
                        sanitizeCsvCell(lead.submittedSourcePage()),
                        sanitizeCsvCell(lead.routingStatus()),
                        sanitizeCsvCell(lead.routingReason())
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

    private Path jsonPath() {
        return Path.of(leadsProperties.root()).resolve("leads.jsonl");
    }

    private Path csvPath() {
        return Path.of(leadsProperties.root()).resolve("leads.csv");
    }

    private record LeadCsvRow(
            String leadId,
            String capturedAt,
            String fullName,
            String phone,
            String email,
            String city,
            String utilityId,
            String utilityName,
            String propertyType,
            String issueType,
            String pageFamily,
            String notes,
            String sourcePage,
            String referrer,
            String submittedUtilityId,
            String submittedUtilityName,
            String submittedPageFamily,
            String submittedSourcePage,
            String routingStatus,
            String routingReason
    ) {
    }
}
