package owner.backflow.ops;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import owner.backflow.config.AppDataProperties;
import owner.backflow.data.model.ApprovedTesterMode;
import owner.backflow.data.model.CostBand;
import owner.backflow.data.model.GuideRecord;
import owner.backflow.data.model.GuideSection;
import owner.backflow.data.model.PageStatus;
import owner.backflow.data.model.SourceLink;
import owner.backflow.data.model.StateGuideRecord;
import owner.backflow.data.model.UtilityRecord;

class SourceEvidenceServiceTest {

    @TempDir
    java.nio.file.Path tempDir;

    @Test
    void utilityMissingSnapshotFileIsBlocking() {
        SourceEvidenceService service = new SourceEvidenceService(new AppDataProperties(tempDir.resolve("data").toString()));

        Assertions.assertTrue(service.hasBlockingIssue(sampleUtility("storage/snapshots/missing.html")));
    }

    @Test
    void guideAndStateGuideWithSnapshotFilesAreNotBlocking() throws Exception {
        java.nio.file.Path workspaceRoot = tempDir;
        java.nio.file.Files.createDirectories(workspaceRoot.resolve("storage").resolve("snapshots").resolve("guides"));
        java.nio.file.Files.createDirectories(workspaceRoot.resolve("storage").resolve("snapshots").resolve("states"));
        java.nio.file.Files.writeString(workspaceRoot.resolve("storage").resolve("snapshots").resolve("guides").resolve("guide.html"), "<html></html>");
        java.nio.file.Files.writeString(workspaceRoot.resolve("storage").resolve("snapshots").resolve("states").resolve("state.html"), "<html></html>");

        SourceEvidenceService service = new SourceEvidenceService(new AppDataProperties(workspaceRoot.resolve("data").toString()));

        Assertions.assertFalse(service.hasBlockingIssue(sampleGuide("storage/snapshots/guides/guide.html")));
        Assertions.assertFalse(service.hasBlockingIssue(sampleStateGuide("storage/snapshots/states/state.html")));
    }

    private UtilityRecord sampleUtility(String snapshotPath) {
        return new UtilityRecord(
                "sample-utility",
                "Sample Utility",
                "municipal utility",
                "sample-utility",
                "texas",
                List.of("Sample City"),
                List.of("Sample County"),
                List.of(),
                "https://example.gov/backflow",
                List.of("https://example.gov/backflow"),
                "Annual",
                "Annual testing is required.",
                List.of("Commercial"),
                List.of("RPZ"),
                ApprovedTesterMode.NONE,
                "",
                "",
                List.of(),
                "555-0100",
                "Penalty text",
                "Source excerpt",
                snapshotPath,
                "TL",
                45,
                PageStatus.PUBLISH,
                LocalDate.of(2026, 4, 4),
                "Summary",
                "Affected owners",
                List.of(),
                List.of(),
                null,
                null,
                null,
                List.of("Do the test"),
                List.of("Failure matters"),
                new CostBand("test", "repair", "notes"),
                List.of(new SourceLink("Official page", "https://example.gov/backflow", "official page"))
        );
    }

    private GuideRecord sampleGuide(String snapshotPath) {
        return new GuideRecord(
                "sample-guide",
                "Sample Guide",
                "Description",
                "Intro",
                "Callout",
                "Excerpt",
                snapshotPath,
                "TL",
                LocalDate.of(2026, 4, 4),
                120,
                true,
                List.of(new GuideSection("Section", "Body", List.of("Bullet"))),
                List.of(new SourceLink("Official page", "https://example.gov/guide", "official page"))
        );
    }

    private StateGuideRecord sampleStateGuide(String snapshotPath) {
        return new StateGuideRecord(
                "texas",
                "Texas guide",
                "Description",
                "Summary",
                "Authority summary",
                "Excerpt",
                snapshotPath,
                "TL",
                LocalDate.of(2026, 4, 4),
                45,
                true,
                List.of("Highlight"),
                List.of("sample-utility"),
                List.of(new SourceLink("Official page", "https://example.gov/state", "official page"))
        );
    }
}
