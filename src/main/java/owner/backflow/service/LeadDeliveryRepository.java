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
public class LeadDeliveryRepository {
    private final AppLeadsProperties leadsProperties;
    private final ObjectMapper jsonMapper;
    private final CsvMapper csvMapper;
    private final CsvSchema csvSchema;

    public LeadDeliveryRepository(AppLeadsProperties leadsProperties) {
        this.leadsProperties = leadsProperties;
        this.jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
        this.csvMapper = CsvMapper.builder().findAndAddModules().build();
        this.csvSchema = csvMapper.schemaFor(LeadDeliveryCsvRow.class).withHeader();
    }

    public synchronized LeadDeliveryRecord save(LeadDeliveryRecord record) {
        LeadDeliveryRecord normalized = new LeadDeliveryRecord(
                record.deliveryId() == null || record.deliveryId().isBlank() ? UUID.randomUUID().toString() : record.deliveryId().trim(),
                normalize(record.leadId()),
                normalize(record.utilityId()),
                normalize(record.providerId()),
                normalize(record.providerName()),
                normalize(record.providerEmail()),
                normalize(record.status()),
                normalize(record.channel()),
                normalize(record.subject()),
                normalize(record.body()),
                normalize(record.sourcePage()),
                record.createdAt() == null ? LocalDateTime.now() : record.createdAt(),
                record.deliveredAt(),
                normalize(record.note())
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
            throw new IllegalStateException("Failed to save lead delivery to " + path, exception);
        }
    }

    public synchronized List<LeadDeliveryRecord> findAll() {
        return List.copyOf(findAllInternal());
    }

    public synchronized long countByStatus(String status) {
        String expected = normalize(status);
        return findAllInternal().stream()
                .filter(record -> record.status().equalsIgnoreCase(expected))
                .count();
    }

    private List<LeadDeliveryRecord> findAllInternal() {
        Path path = jsonPath();
        if (!Files.exists(path)) {
            return List.of();
        }
        try {
            List<LeadDeliveryRecord> deliveries = new ArrayList<>();
            for (String line : Files.readAllLines(path)) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                deliveries.add(jsonMapper.readValue(line, LeadDeliveryRecord.class));
            }
            deliveries.sort(Comparator.comparing(LeadDeliveryRecord::createdAt).reversed());
            return deliveries;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read lead deliveries from " + path, exception);
        }
    }

    private void writeCsvSnapshot(List<LeadDeliveryRecord> deliveries) throws IOException {
        Path csvPath = csvPath();
        Files.createDirectories(csvPath.getParent());
        Files.writeString(
                csvPath,
                csvMapper.writer(csvSchema).writeValueAsString(toCsvRows(deliveries)),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    private List<LeadDeliveryCsvRow> toCsvRows(List<LeadDeliveryRecord> deliveries) {
        return deliveries.stream()
                .map(delivery -> new LeadDeliveryCsvRow(
                        delivery.deliveryId(),
                        delivery.leadId(),
                        delivery.utilityId(),
                        delivery.providerId(),
                        delivery.providerName(),
                        delivery.providerEmail(),
                        delivery.status(),
                        delivery.channel(),
                        delivery.subject(),
                        delivery.sourcePage(),
                        delivery.createdAt() == null ? "" : delivery.createdAt().toString(),
                        delivery.deliveredAt() == null ? "" : delivery.deliveredAt().toString(),
                        delivery.note()
                ))
                .toList();
    }

    private Path jsonPath() {
        return Path.of(leadsProperties.root()).resolve("lead-deliveries.jsonl");
    }

    private Path csvPath() {
        return Path.of(leadsProperties.root()).resolve("lead-deliveries.csv");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private record LeadDeliveryCsvRow(
            String deliveryId,
            String leadId,
            String utilityId,
            String providerId,
            String providerName,
            String providerEmail,
            String status,
            String channel,
            String subject,
            String sourcePage,
            String createdAt,
            String deliveredAt,
            String note
    ) {
    }
}
