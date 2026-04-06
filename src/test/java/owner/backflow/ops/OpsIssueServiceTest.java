package owner.backflow.ops;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import owner.backflow.data.model.ApprovedTesterMode;
import owner.backflow.data.model.CostBand;
import owner.backflow.data.model.PageStatus;
import owner.backflow.data.model.SourceLink;
import owner.backflow.data.model.UtilityRecord;

class OpsIssueServiceTest {

    @Test
    void oldBrokenLinkBlocksUtility() {
        OpsIssueService service = OpsIssueService.forTest(
                List.of(new OpsCsvEntry(Map.of(
                        "checkedAt", "2026-03-20",
                        "url", "https://example.gov/backflow",
                        "status", "404",
                        "note", "broken"
                ))),
                List.of(),
                7
        );

        Assertions.assertTrue(service.hasBlockingIssue(sampleUtility(), LocalDate.of(2026, 4, 4)));
    }

    @Test
    void recentBrokenLinkDoesNotBlockYet() {
        OpsIssueService service = OpsIssueService.forTest(
                List.of(new OpsCsvEntry(Map.of(
                        "checkedAt", "2026-04-02",
                        "url", "https://example.gov/backflow",
                        "status", "404",
                        "note", "broken"
                ))),
                List.of(),
                7
        );

        Assertions.assertFalse(service.hasBlockingIssue(sampleUtility(), LocalDate.of(2026, 4, 4)));
    }

    @Test
    void conflictBlocksUtilityEvenWithoutBrokenLink() {
        OpsIssueService service = OpsIssueService.forTest(
                List.of(),
                List.of(new OpsCsvEntry(Map.of(
                        "checkedAt", "2026-04-04",
                        "utilityId", "sample-utility",
                        "field", "dueBasis",
                        "note", "source conflict"
                ))),
                7
        );

        Assertions.assertTrue(service.hasBlockingIssue(sampleUtility(), LocalDate.of(2026, 4, 4)));
    }

    private UtilityRecord sampleUtility() {
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
                "storage/snapshots/sample.html",
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
}
