package owner.backflow.ops;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import owner.backflow.config.AppOpsProperties;

class SearchConsolePerformanceServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void loadsSearchConsolePageExportAndClassifiesBottlenecks() throws Exception {
        Path csv = tempDir.resolve("pages.csv");
        Files.writeString(
                csv,
                """
                Top pages,Clicks,Impressions,CTR,Position
                https://backflowpath.com/utilities/texas/dallas-water-utilities/,0,180,0.4%,9.8
                /utilities/texas/garland-water-utilities/,3,90,3.3%,8.1
                """
        );
        SearchConsolePerformanceService service = new SearchConsolePerformanceService(opsProperties(csv));

        SearchConsolePerformanceService.SearchConsoleSnapshot snapshot = service.loadSnapshot();

        Assertions.assertTrue(snapshot.available());
        Assertions.assertEquals(2, snapshot.rowCount());
        Assertions.assertEquals(3, snapshot.totalClicks());
        Assertions.assertEquals(270, snapshot.totalImpressions());
        SearchConsolePerformanceService.SearchConsolePageMetric dallas = snapshot.metricForPath("/utilities/texas/dallas-water-utilities/");
        Assertions.assertNotNull(dallas);
        Assertions.assertEquals("ctr_bottleneck", dallas.bottleneckCode());
        Assertions.assertEquals("CTR/title bottleneck", dallas.bottleneckLabel());
        Assertions.assertEquals("3.3%", snapshot.metricForPath("/utilities/texas/garland-water-utilities/").displayCtr());
    }

    @Test
    void missingExportReturnsUnavailableSnapshot() {
        Path csv = tempDir.resolve("missing.csv");
        SearchConsolePerformanceService service = new SearchConsolePerformanceService(opsProperties(csv));

        SearchConsolePerformanceService.SearchConsoleSnapshot snapshot = service.loadSnapshot();

        Assertions.assertFalse(snapshot.available());
        Assertions.assertEquals(csv.toAbsolutePath().normalize().toString(), snapshot.sourcePath());
        Assertions.assertEquals(0, snapshot.rowCount());
    }

    private AppOpsProperties opsProperties(Path searchConsolePath) {
        return new AppOpsProperties(
                "",
                "",
                false,
                "",
                true,
                "",
                7,
                "",
                searchConsolePath.toString(),
                tempDir.resolve("queries.csv").toString()
        );
    }
}
