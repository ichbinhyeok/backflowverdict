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
        Path queries = tempDir.resolve("queries.csv");
        Files.writeString(
                queries,
                """
                Top queries,Clicks,Impressions,CTR,Position
                swiftcomply portal,0,140,0%,8.8
                backflow test cost,1,200,0.5%,7.4
                dallas backflow report,2,80,2.5%,6.2
                """
        );
        SearchConsolePerformanceService service = new SearchConsolePerformanceService(opsProperties(csv, queries));

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
        Assertions.assertTrue(snapshot.queryAvailable());
        Assertions.assertEquals(3, snapshot.queryRowCount());
        Assertions.assertEquals(3, snapshot.queryTotalClicks());
        Assertions.assertEquals(420, snapshot.queryTotalImpressions());
        Assertions.assertEquals("backflow test cost", snapshot.topQueryOpportunities().getFirst().query());
        Assertions.assertEquals("CTR/title bottleneck", snapshot.topQueryOpportunities().getFirst().bottleneckLabel());
    }

    @Test
    void missingExportReturnsUnavailableSnapshot() {
        Path csv = tempDir.resolve("missing.csv");
        Path queries = tempDir.resolve("missing-queries.csv");
        SearchConsolePerformanceService service = new SearchConsolePerformanceService(opsProperties(csv, queries));

        SearchConsolePerformanceService.SearchConsoleSnapshot snapshot = service.loadSnapshot();

        Assertions.assertFalse(snapshot.available());
        Assertions.assertEquals(csv.toAbsolutePath().normalize().toString(), snapshot.sourcePath());
        Assertions.assertEquals(0, snapshot.rowCount());
        Assertions.assertFalse(snapshot.queryAvailable());
        Assertions.assertEquals(queries.toAbsolutePath().normalize().toString(), snapshot.querySourcePath());
    }

    @Test
    void queryExportCanLoadWhenPageExportIsMissing() throws Exception {
        Path pages = tempDir.resolve("missing-pages.csv");
        Path queries = tempDir.resolve("queries-only.csv");
        Files.writeString(
                queries,
                """
                검색어,클릭수,노출수,클릭률,평균 게재순위
                오로라 백플로우,0,55,0%,11.4
                """
        );
        SearchConsolePerformanceService service = new SearchConsolePerformanceService(opsProperties(pages, queries));

        SearchConsolePerformanceService.SearchConsoleSnapshot snapshot = service.loadSnapshot();

        Assertions.assertFalse(snapshot.available());
        Assertions.assertTrue(snapshot.queryAvailable());
        Assertions.assertEquals(1, snapshot.queryRowCount());
        Assertions.assertEquals("오로라 백플로우", snapshot.topQueryOpportunities().getFirst().query());
    }

    private AppOpsProperties opsProperties(Path searchConsolePath, Path searchConsoleQueriesPath) {
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
                searchConsoleQueriesPath.toString()
        );
    }
}
