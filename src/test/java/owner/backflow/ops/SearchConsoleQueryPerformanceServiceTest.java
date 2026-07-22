package owner.backflow.ops;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import owner.backflow.config.AppOpsProperties;

class SearchConsoleQueryPerformanceServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsSearchConsoleQueryExportAndSuggestsRewritePatterns() throws Exception {
        Path csv = tempDir.resolve("queries.csv");
        Files.writeString(
                csv,
                """
                Query,Clicks,Impressions,CTR,Position
                dallas swiftcomply portal,0,140,0.3%,8.4
                failed backflow test fort worth,0,64,0.2%,18.1
                garland backflow testing,2,50,4.0%,7.2
                """
        );
        SearchConsoleQueryPerformanceService service = new SearchConsoleQueryPerformanceService(opsProperties(csv));

        SearchConsoleQueryPerformanceService.SearchConsoleQuerySnapshot snapshot = service.loadSnapshot();

        Assertions.assertTrue(snapshot.available());
        Assertions.assertEquals(3, snapshot.rowCount());
        Assertions.assertEquals(2, snapshot.totalClicks());
        Assertions.assertEquals(254, snapshot.totalImpressions());
        SearchConsoleQueryPerformanceService.SearchConsoleQueryMetric portal = snapshot.metricsByQuery().get("dallas swiftcomply portal");
        Assertions.assertNotNull(portal);
        Assertions.assertEquals("portal", portal.intentFamily());
        Assertions.assertEquals("Rewrite now", portal.priorityLabel());
        Assertions.assertTrue(portal.suggestedTitlePattern().contains("[Portal] backflow report routes"));
        Assertions.assertTrue(snapshot.topBottlenecks().stream()
                .anyMatch(metric -> "failed-test".equals(metric.intentFamily())));
    }

    @Test
    void missingExportReturnsUnavailableSnapshot() {
        Path csv = tempDir.resolve("missing.csv");
        SearchConsoleQueryPerformanceService service = new SearchConsoleQueryPerformanceService(opsProperties(csv));

        SearchConsoleQueryPerformanceService.SearchConsoleQuerySnapshot snapshot = service.loadSnapshot();

        Assertions.assertFalse(snapshot.available());
        Assertions.assertEquals(csv.toAbsolutePath().normalize().toString(), snapshot.sourcePath());
        Assertions.assertEquals(0, snapshot.rowCount());
    }

    private AppOpsProperties opsProperties(Path searchConsoleQueryPath) {
        return new AppOpsProperties(
                "",
                "",
                false,
                "",
                true,
                "",
                7,
                "",
                tempDir.resolve("pages.csv").toString(),
                searchConsoleQueryPath.toString()
        );
    }
}
