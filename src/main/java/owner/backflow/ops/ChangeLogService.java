package owner.backflow.ops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import owner.backflow.config.AppDataProperties;
import org.springframework.stereotype.Service;

@Service
public class ChangeLogService {
    private final AppDataProperties dataProperties;
    private final ObjectMapper objectMapper;

    public ChangeLogService(AppDataProperties dataProperties) {
        this.dataProperties = dataProperties;
        this.objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
    }

    public synchronized void appendFreshnessAudit(FreshnessAuditReport report) {
        ChangeLogEntry entry = new ChangeLogEntry(
                report.generatedAt(),
                "freshness_audit",
                "ops",
                "freshness-report",
                auditNote(report.summary()),
                auditMetadata(report.summary())
        );
        appendIfChanged(entry);
    }

    public synchronized void appendVerificationRun(VerificationReport report) {
        ChangeLogEntry entry = new ChangeLogEntry(
                report.generatedAt(),
                "verification_run",
                "ops",
                "verification-workflow",
                verificationNote(report),
                verificationMetadata(report)
        );
        appendIfChanged(entry);
    }

    synchronized void appendIfChanged(ChangeLogEntry entry) {
        Path path = changeLogPath();
        try {
            Files.createDirectories(path.getParent());
            String line = objectMapper.writeValueAsString(entry);
            Optional<LatestChangeLogEntry> latest = readLatestEntry(path);
            if (latest.isPresent() && latest.get().entry().equals(entry)) {
                if (!latest.get().rawLine().equals(line)) {
                    rewriteLatestLine(path, line);
                }
                return;
            }

            Files.writeString(path, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            Files.writeString(path, System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to append change log entry to " + path, exception);
        }
    }

    private Optional<LatestChangeLogEntry> readLatestEntry(Path path) {
        if (!Files.exists(path)) {
            return Optional.empty();
        }

        try (var lines = Files.lines(path)) {
            return lines
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .reduce((left, right) -> right)
                    .map(line -> new LatestChangeLogEntry(line, parseEntry(line)));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read change log " + path, exception);
        }
    }

    private ChangeLogEntry parseEntry(String line) {
        try {
            return objectMapper.readValue(line, ChangeLogEntry.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse change log entry", exception);
        }
    }

    private String auditNote(FreshnessAuditSummary summary) {
        return "Freshness audit wrote summary for "
                + summary.utilityCount()
                + " utilities, "
                + summary.guideCount()
                + " guides, and "
                + summary.stateGuideCount()
                + " state guides.";
    }

    private Map<String, Object> auditMetadata(FreshnessAuditSummary summary) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("utilityCount", summary.utilityCount());
        metadata.put("staleUtilityCount", summary.staleUtilityCount());
        metadata.put("guideCount", summary.guideCount());
        metadata.put("staleGuideCount", summary.staleGuideCount());
        metadata.put("stateGuideCount", summary.stateGuideCount());
        metadata.put("staleStateGuideCount", summary.staleStateGuideCount());
        metadata.put("brokenLinkCount", summary.brokenLinkCount());
        metadata.put("conflictCount", summary.conflictCount());
        return metadata;
    }

    private String verificationNote(VerificationReport report) {
        String reviewer = report.reviewerInitials().isBlank() ? "unknown reviewer" : report.reviewerInitials();
        String suffix = report.note().isBlank() ? "" : " Note: " + report.note();
        return "Verification workflow run by " + reviewer
                + " with status "
                + report.status()
                + "."
                + suffix;
    }

    private Map<String, Object> verificationMetadata(VerificationReport report) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("status", report.status());
        metadata.put("reviewerInitials", report.reviewerInitials());
        metadata.put("publishedUtilityCount", report.summary().publishedUtilityCount());
        metadata.put("publishedGuideCount", report.summary().publishedGuideCount());
        metadata.put("publishedStateGuideCount", report.summary().publishedStateGuideCount());
        metadata.put("errorCount", report.summary().errorCount());
        metadata.put("warningCount", report.summary().warningCount());
        return metadata;
    }

    private Path changeLogPath() {
        return Path.of(dataProperties.root()).resolve("ops").resolve("change_log.jsonl");
    }

    private void rewriteLatestLine(Path path, String replacementLine) throws IOException {
        List<String> lines = new ArrayList<>(Files.readAllLines(path));
        for (int index = lines.size() - 1; index >= 0; index--) {
            if (!lines.get(index).trim().isBlank()) {
                lines.set(index, replacementLine);
                Files.write(path, lines);
                return;
            }
        }

        Files.writeString(path, replacementLine + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private record LatestChangeLogEntry(String rawLine, ChangeLogEntry entry) {
    }
}
