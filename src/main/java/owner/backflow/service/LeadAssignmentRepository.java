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
import java.util.Optional;
import owner.backflow.config.AppLeadsProperties;
import org.springframework.stereotype.Service;

@Service
public class LeadAssignmentRepository {
    private final AppLeadsProperties leadsProperties;
    private final ObjectMapper jsonMapper;
    private final CsvMapper csvMapper;
    private final CsvSchema csvSchema;

    public LeadAssignmentRepository(AppLeadsProperties leadsProperties) {
        this.leadsProperties = leadsProperties;
        this.jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
        this.csvMapper = CsvMapper.builder()
                .findAndAddModules()
                .build();
        this.csvSchema = csvMapper.schemaFor(LeadAssignmentCsvRow.class).withHeader();
    }

    public synchronized LeadAssignmentRecord save(LeadAssignmentRecord record) {
        LeadAssignmentRecord normalized = new LeadAssignmentRecord(
                normalize(record.leadId()),
                normalize(record.utilityId()),
                normalize(record.providerId()),
                normalize(record.providerName()),
                record.assignedAt() == null ? LocalDateTime.now() : record.assignedAt(),
                normalize(record.assignedBy()),
                normalize(record.note())
        );
        List<LeadAssignmentRecord> assignments = new ArrayList<>(findAllInternal());
        assignments.removeIf(existing -> existing.leadId().equalsIgnoreCase(normalized.leadId()));
        assignments.add(normalized);
        writeAssignments(assignments);
        return normalized;
    }

    public synchronized Optional<LeadAssignmentRecord> findByLeadId(String leadId) {
        if (leadId == null || leadId.isBlank()) {
            return Optional.empty();
        }
        return findAllInternal().stream()
                .filter(record -> record.leadId().equalsIgnoreCase(leadId.trim()))
                .findFirst();
    }

    public synchronized List<LeadAssignmentRecord> findAll() {
        return List.copyOf(findAllInternal());
    }

    private List<LeadAssignmentRecord> findAllInternal() {
        Path path = jsonPath();
        if (!Files.exists(path)) {
            return List.of();
        }
        try {
            List<LeadAssignmentRecord> records = jsonMapper.readerForListOf(LeadAssignmentRecord.class)
                    .readValue(path.toFile());
            records.sort(Comparator.comparing(LeadAssignmentRecord::assignedAt).reversed());
            return records;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read lead assignments from " + path, exception);
        }
    }

    private void writeAssignments(List<LeadAssignmentRecord> assignments) {
        Path jsonPath = jsonPath();
        Path csvPath = csvPath();
        try {
            Files.createDirectories(jsonPath.getParent());
            List<LeadAssignmentRecord> ordered = assignments.stream()
                    .sorted(Comparator.comparing(LeadAssignmentRecord::assignedAt).reversed())
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
            throw new IllegalStateException("Failed to persist lead assignments.", exception);
        }
    }

    private List<LeadAssignmentCsvRow> toCsvRows(List<LeadAssignmentRecord> assignments) {
        return assignments.stream()
                .map(record -> new LeadAssignmentCsvRow(
                        record.leadId(),
                        record.utilityId(),
                        record.providerId(),
                        record.providerName(),
                        record.assignedAt() == null ? "" : record.assignedAt().toString(),
                        record.assignedBy(),
                        record.note()
                ))
                .toList();
    }

    private Path jsonPath() {
        return Path.of(leadsProperties.root()).resolve("lead-assignments.json");
    }

    private Path csvPath() {
        return Path.of(leadsProperties.root()).resolve("lead-assignments.csv");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private record LeadAssignmentCsvRow(
            String leadId,
            String utilityId,
            String providerId,
            String providerName,
            String assignedAt,
            String assignedBy,
            String note
    ) {
    }
}
