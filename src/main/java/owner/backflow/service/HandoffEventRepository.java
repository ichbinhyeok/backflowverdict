package owner.backflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
public class HandoffEventRepository {
    private final AppLeadsProperties leadsProperties;
    private final ObjectMapper jsonMapper;

    public HandoffEventRepository(AppLeadsProperties leadsProperties) {
        this.leadsProperties = leadsProperties;
        this.jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
    }

    public synchronized HandoffEventRecord save(HandoffEventRecord record) {
        HandoffEventRecord normalized = new HandoffEventRecord(
                record.eventId() == null || record.eventId().isBlank()
                        ? UUID.randomUUID().toString()
                        : record.eventId().trim(),
                record.occurredAt() == null ? LocalDateTime.now() : record.occurredAt(),
                normalize(record.eventType()),
                normalize(record.handoffId()),
                normalize(record.publicToken()),
                normalize(record.internalToken()),
                normalize(record.utilityId()),
                normalize(record.issueType()),
                normalize(record.resultStatus()),
                normalize(record.submissionStatus()),
                normalize(record.vendorCompanyName()),
                normalize(record.vendorSlug()),
                normalize(record.officeKey()),
                normalize(record.vendorEmail()),
                normalize(record.originPath()),
                normalize(record.requestPath()),
                normalize(record.referrer()),
                normalize(record.userAgent()),
                normalize(record.trafficClass())
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
            return normalized;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save handoff event to " + path, exception);
        }
    }

    public synchronized List<HandoffEventRecord> findAll() {
        return List.copyOf(findAllInternal());
    }

    private List<HandoffEventRecord> findAllInternal() {
        Path path = jsonPath();
        if (!Files.exists(path)) {
            return List.of();
        }
        try {
            List<HandoffEventRecord> events = new ArrayList<>();
            for (String line : Files.readAllLines(path)) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                events.add(jsonMapper.readValue(line, HandoffEventRecord.class));
            }
            events.sort(Comparator.comparing(HandoffEventRecord::occurredAt).reversed());
            return events;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read handoff events from " + path, exception);
        }
    }

    private Path jsonPath() {
        return Path.of(leadsProperties.root()).resolve("handoff-events.jsonl");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
