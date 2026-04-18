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
public class HandoffRepository {
    private final AppLeadsProperties leadsProperties;
    private final ObjectMapper jsonMapper;

    public HandoffRepository(AppLeadsProperties leadsProperties) {
        this.leadsProperties = leadsProperties;
        this.jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
    }

    public synchronized HandoffRecord save(HandoffRecord handoffRecord) {
        HandoffRecord normalized = new HandoffRecord(
                handoffRecord.handoffId() == null || handoffRecord.handoffId().isBlank()
                        ? UUID.randomUUID().toString()
                        : handoffRecord.handoffId().trim(),
                handoffRecord.publicToken() == null || handoffRecord.publicToken().isBlank()
                        ? UUID.randomUUID().toString()
                        : handoffRecord.publicToken().trim(),
                handoffRecord.internalToken() == null || handoffRecord.internalToken().isBlank()
                        ? UUID.randomUUID().toString()
                        : handoffRecord.internalToken().trim(),
                handoffRecord.createdAt() == null ? LocalDateTime.now() : handoffRecord.createdAt(),
                normalize(handoffRecord.utilityId()),
                normalize(handoffRecord.utilityName()),
                normalize(handoffRecord.utilityState()),
                normalize(handoffRecord.utilitySlug()),
                normalize(handoffRecord.issueType()),
                normalize(handoffRecord.issueLabel()),
                normalize(handoffRecord.propertyType()),
                normalize(handoffRecord.propertyLabel()),
                normalize(handoffRecord.customerFirstName()),
                normalize(handoffRecord.accountIdentifier()),
                normalize(handoffRecord.vendorCompanyName()),
                normalize(handoffRecord.vendorSlug()),
                normalize(handoffRecord.officeKey()),
                normalize(handoffRecord.vendorContactName()),
                normalize(handoffRecord.vendorPhone()),
                normalize(handoffRecord.vendorEmail()),
                normalize(handoffRecord.testerLicenseNumber()),
                handoffRecord.dueDate(),
                normalize(handoffRecord.noticeSummary()),
                normalize(handoffRecord.internalNote()),
                normalize(handoffRecord.createdFromPath()),
                normalize(handoffRecord.headline()),
                normalize(handoffRecord.publicSummary()),
                normalizeList(handoffRecord.nextSteps()),
                normalizeList(handoffRecord.vendorActions()),
                normalizeList(handoffRecord.customerActions()),
                normalizeList(handoffRecord.commonMistakes()),
                normalize(handoffRecord.fullRulePath()),
                normalize(handoffRecord.officialProgramUrl()),
                normalize(handoffRecord.officialProgramLabel()),
                normalize(handoffRecord.helpPath()),
                handoffRecord.lastVerified(),
                normalize(handoffRecord.smsText()),
                normalize(handoffRecord.emailSubject()),
                normalize(handoffRecord.emailBody()),
                normalize(handoffRecord.siteAddress()),
                normalize(handoffRecord.deviceLocation()),
                normalize(handoffRecord.technicianName()),
                normalize(handoffRecord.gaugeId()),
                normalize(handoffRecord.calibrationReference()),
                normalize(handoffRecord.testReadingSummary()),
                normalize(handoffRecord.assemblyMakeModel()),
                normalize(handoffRecord.assemblySize()),
                normalize(handoffRecord.assemblyType()),
                normalize(handoffRecord.assemblySerial()),
                normalize(handoffRecord.checkValveOneReading()),
                normalize(handoffRecord.checkValveTwoReading()),
                normalize(handoffRecord.openingPointReading()),
                normalize(handoffRecord.repairSummary()),
                normalize(handoffRecord.resultStatus()),
                normalize(handoffRecord.resultLabel()),
                handoffRecord.testDate(),
                normalize(handoffRecord.submissionStatus()),
                normalize(handoffRecord.submissionLabel()),
                handoffRecord.submissionDate(),
                normalize(handoffRecord.submissionReference()),
                normalize(handoffRecord.permitOrReportNumber()),
                normalize(handoffRecord.failedReason())
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
            throw new IllegalStateException("Failed to save handoff to " + path, exception);
        }
    }

    public synchronized HandoffRecord findByPublicToken(String publicToken) {
        if (publicToken == null || publicToken.isBlank()) {
            return null;
        }
        String normalizedToken = publicToken.trim();
        return findAllInternal().stream()
                .filter(record -> normalizedToken.equalsIgnoreCase(record.publicToken()))
                .findFirst()
                .orElse(null);
    }

    public synchronized HandoffRecord findByInternalToken(String internalToken) {
        if (internalToken == null || internalToken.isBlank()) {
            return null;
        }
        String normalizedToken = internalToken.trim();
        return findAllInternal().stream()
                .filter(record -> normalizedToken.equalsIgnoreCase(record.internalToken()))
                .findFirst()
                .orElse(null);
    }

    public synchronized List<HandoffRecord> findAll() {
        return List.copyOf(findAllInternal());
    }

    private List<HandoffRecord> findAllInternal() {
        Path path = jsonPath();
        if (!Files.exists(path)) {
            return List.of();
        }
        try {
            List<HandoffRecord> handoffs = new ArrayList<>();
            for (String line : Files.readAllLines(path)) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                handoffs.add(jsonMapper.readValue(line, HandoffRecord.class));
            }
            handoffs.sort(Comparator.comparing(HandoffRecord::createdAt).reversed());
            return handoffs;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read handoffs from " + path, exception);
        }
    }

    private List<String> normalizeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .map(this::normalize)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private Path jsonPath() {
        return Path.of(leadsProperties.root()).resolve("handoffs.jsonl");
    }
}
