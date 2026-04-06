package owner.backflow.ops;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import owner.backflow.config.AppDataProperties;

class ChangeLogServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void duplicateFreshnessAuditEntryIsNotAppendedTwice() throws Exception {
        ChangeLogService service = new ChangeLogService(new AppDataProperties(tempDir.toString()));
        FreshnessAuditReport report = reportWithCounts(13, 0, 6, 0, 1, 0, 0, 0);

        service.appendFreshnessAudit(report);
        service.appendFreshnessAudit(report);

        Path path = tempDir.resolve("ops").resolve("change_log.jsonl");
        Assertions.assertEquals(1, Files.readAllLines(path).size());
        Assertions.assertTrue(Files.readString(path).contains("\"checkedAt\":\"2026-04-04\""));
    }

    @Test
    void changedFreshnessAuditEntryIsAppended() throws Exception {
        ChangeLogService service = new ChangeLogService(new AppDataProperties(tempDir.toString()));

        service.appendFreshnessAudit(reportWithCounts(13, 0, 6, 0, 1, 0, 0, 0));
        service.appendFreshnessAudit(reportWithCounts(13, 1, 6, 0, 1, 0, 1, 0));

        Path path = tempDir.resolve("ops").resolve("change_log.jsonl");
        Assertions.assertEquals(2, Files.readAllLines(path).size());
    }

    @Test
    void legacyFreshnessAuditEntryIsRewrittenToCanonicalFormat() throws Exception {
        ChangeLogService service = new ChangeLogService(new AppDataProperties(tempDir.toString()));
        Path path = tempDir.resolve("ops").resolve("change_log.jsonl");
        Files.createDirectories(path.getParent());
        Files.writeString(
                path,
                "{\"checkedAt\":[2026,4,4],\"action\":\"freshness_audit\",\"entityType\":\"ops\",\"entityId\":\"freshness-report\",\"note\":\"Freshness audit wrote summary for 13 utilities, 6 guides, and 1 state guides.\",\"metadata\":{\"utilityCount\":13,\"staleUtilityCount\":0,\"guideCount\":6,\"staleGuideCount\":0,\"stateGuideCount\":1,\"staleStateGuideCount\":0,\"brokenLinkCount\":0,\"conflictCount\":0}}"
                        + System.lineSeparator()
        );

        service.appendFreshnessAudit(reportWithCounts(13, 0, 6, 0, 1, 0, 0, 0));

        Assertions.assertEquals(
                "{\"checkedAt\":\"2026-04-04\",\"action\":\"freshness_audit\",\"entityType\":\"ops\",\"entityId\":\"freshness-report\",\"note\":\"Freshness audit wrote summary for 13 utilities, 6 guides, and 1 state guides.\",\"metadata\":{\"utilityCount\":13,\"staleUtilityCount\":0,\"guideCount\":6,\"staleGuideCount\":0,\"stateGuideCount\":1,\"staleStateGuideCount\":0,\"brokenLinkCount\":0,\"conflictCount\":0}}",
                Files.readString(path).trim()
        );
    }

    private FreshnessAuditReport reportWithCounts(
            int utilityCount,
            int staleUtilityCount,
            int guideCount,
            int staleGuideCount,
            int stateGuideCount,
            int staleStateGuideCount,
            int brokenLinkCount,
            int conflictCount
    ) {
        return new FreshnessAuditReport(
                LocalDate.of(2026, 4, 4),
                new FreshnessAuditSummary(
                        utilityCount,
                        staleUtilityCount,
                        guideCount,
                        staleGuideCount,
                        stateGuideCount,
                        staleStateGuideCount,
                        brokenLinkCount,
                        conflictCount
                ),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
