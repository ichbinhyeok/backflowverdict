package owner.backflow.ops;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import owner.backflow.config.AppOpsProperties;
import org.springframework.stereotype.Service;

@Service
public class SearchConsolePerformanceService {
    private static final List<String> PAGE_HEADERS = List.of(
            "page",
            "top pages",
            "pages",
            "url",
            "landing page",
            "페이지",
            "상위 페이지"
    );
    private static final List<String> QUERY_HEADERS = List.of(
            "query",
            "top queries",
            "queries",
            "search query",
            "검색어",
            "상위 검색어"
    );
    private static final List<String> CLICK_HEADERS = List.of("clicks", "클릭수", "클릭");
    private static final List<String> IMPRESSION_HEADERS = List.of("impressions", "노출수", "노출");
    private static final List<String> CTR_HEADERS = List.of("ctr", "click through rate", "클릭률");
    private static final List<String> POSITION_HEADERS = List.of("position", "avg position", "average position", "평균 게재순위", "게재순위", "위치");

    private final AppOpsProperties opsProperties;
    private final CsvMapper csvMapper;

    public SearchConsolePerformanceService(AppOpsProperties opsProperties) {
        this.opsProperties = opsProperties;
        this.csvMapper = CsvMapper.builder().findAndAddModules().build();
    }

    public SearchConsoleSnapshot loadSnapshot() {
        List<String> warnings = new java.util.ArrayList<>();
        Path pagePath = Path.of(opsProperties.searchConsolePagesPath()).toAbsolutePath().normalize();
        Path queryPath = Path.of(opsProperties.searchConsoleQueriesPath()).toAbsolutePath().normalize();
        PageLoadResult pages = loadPages(pagePath, warnings);
        QueryLoadResult queries = loadQueries(queryPath, warnings);

        List<SearchConsolePageMetric> bottlenecks = pages.metricsByPath().values().stream()
                .distinct()
                .filter(metric -> !"earning_clicks".equals(metric.bottleneckCode()))
                .sorted(Comparator.comparingInt(SearchConsolePageMetric::opportunityScore).reversed()
                        .thenComparing(Comparator.comparingInt(SearchConsolePageMetric::impressions).reversed())
                        .thenComparing(SearchConsolePageMetric::path))
                .limit(20)
                .toList();

        List<SearchConsoleQueryMetric> queryOpportunities = queries.metricsByQuery().values().stream()
                .filter(metric -> !"earning_clicks".equals(metric.bottleneckCode()))
                .sorted(Comparator.comparingInt(SearchConsoleQueryMetric::opportunityScore).reversed()
                        .thenComparing(Comparator.comparingInt(SearchConsoleQueryMetric::impressions).reversed())
                        .thenComparing(SearchConsoleQueryMetric::query))
                .limit(30)
                .toList();

        return new SearchConsoleSnapshot(
                pagePath.toString(),
                pages.available(),
                pages.metricsByPath().values().stream().distinct().toList().size(),
                pages.totalClicks(),
                pages.totalImpressions(),
                pages.ignoredRows(),
                warnings,
                pages.metricsByPath(),
                bottlenecks,
                queryPath.toString(),
                queries.available(),
                queries.metricsByQuery().size(),
                queries.totalClicks(),
                queries.totalImpressions(),
                queries.ignoredRows(),
                queries.metricsByQuery(),
                queryOpportunities
        );
    }

    private PageLoadResult loadPages(Path path, List<String> warnings) {
        if (!Files.exists(path)) {
            return PageLoadResult.missing();
        }
        Map<String, SearchConsolePageMetric> metricsByPath = new LinkedHashMap<>();
        int ignoredRows = 0;
        try {
            MappingIterator<Map<String, String>> iterator = csvRows(path);
            while (iterator.hasNext()) {
                Map<String, String> row = normalizeHeaders(iterator.next());
                Optional<SearchConsolePageMetric> metric = toMetric(row);
                if (metric.isEmpty()) {
                    ignoredRows++;
                    continue;
                }
                SearchConsolePageMetric value = metric.get();
                metricsByPath.merge(value.path(), value, SearchConsolePageMetric::merge);
                String stripped = stripTrailingSlash(value.path());
                if (!stripped.equals(value.path())) {
                    metricsByPath.merge(stripped, value, SearchConsolePageMetric::merge);
                }
            }
        } catch (IOException | IllegalArgumentException exception) {
            warnings.add("Could not read Search Console page CSV: " + exception.getMessage());
            return new PageLoadResult(false, ignoredRows, 0, 0, Map.of());
        }

        if (ignoredRows > 0) {
            warnings.add(ignoredRows + " row(s) ignored because page, clicks, impressions, CTR, or position columns were missing.");
        }

        int totalClicks = metricsByPath.values().stream()
                .distinct()
                .mapToInt(SearchConsolePageMetric::clicks)
                .sum();
        int totalImpressions = metricsByPath.values().stream()
                .distinct()
                .mapToInt(SearchConsolePageMetric::impressions)
                .sum();

        return new PageLoadResult(true, ignoredRows, totalClicks, totalImpressions, metricsByPath);
    }

    private QueryLoadResult loadQueries(Path path, List<String> warnings) {
        if (!Files.exists(path)) {
            return QueryLoadResult.missing();
        }
        Map<String, SearchConsoleQueryMetric> metricsByQuery = new LinkedHashMap<>();
        int ignoredRows = 0;
        try {
            MappingIterator<Map<String, String>> iterator = csvRows(path);
            while (iterator.hasNext()) {
                Map<String, String> row = normalizeHeaders(iterator.next());
                Optional<SearchConsoleQueryMetric> metric = toQueryMetric(row);
                if (metric.isEmpty()) {
                    ignoredRows++;
                    continue;
                }
                SearchConsoleQueryMetric value = metric.get();
                metricsByQuery.merge(value.queryKey(), value, SearchConsoleQueryMetric::merge);
            }
        } catch (IOException | IllegalArgumentException exception) {
            warnings.add("Could not read Search Console query CSV: " + exception.getMessage());
            return new QueryLoadResult(false, ignoredRows, 0, 0, Map.of());
        }

        if (ignoredRows > 0) {
            warnings.add(ignoredRows + " query row(s) ignored because query, clicks, impressions, CTR, or position columns were missing.");
        }

        int totalClicks = metricsByQuery.values().stream()
                .mapToInt(SearchConsoleQueryMetric::clicks)
                .sum();
        int totalImpressions = metricsByQuery.values().stream()
                .mapToInt(SearchConsoleQueryMetric::impressions)
                .sum();
        return new QueryLoadResult(true, ignoredRows, totalClicks, totalImpressions, metricsByQuery);
    }

    private MappingIterator<Map<String, String>> csvRows(Path path) throws IOException {
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        return csvMapper
                .readerFor(new TypeReference<Map<String, String>>() {
                })
                .with(schema)
                .readValues(path.toFile());
    }

    private Optional<SearchConsolePageMetric> toMetric(Map<String, String> row) {
        String page = first(row, PAGE_HEADERS);
        if (isBlank(page)) {
            return Optional.empty();
        }
        int clicks = parseInt(first(row, CLICK_HEADERS));
        int impressions = parseInt(first(row, IMPRESSION_HEADERS));
        double ctrPercent = parseCtrPercent(first(row, CTR_HEADERS));
        double position = parseDouble(first(row, POSITION_HEADERS));
        String path = normalizePath(page);
        if (isBlank(path)) {
            return Optional.empty();
        }
        return Optional.of(new SearchConsolePageMetric(
                page.trim(),
                path,
                clicks,
                impressions,
                ctrPercent,
                position,
                bottleneckCode(clicks, impressions, ctrPercent, position),
                bottleneckLabel(clicks, impressions, ctrPercent, position),
                opportunityScore(clicks, impressions, ctrPercent, position)
        ));
    }

    private Optional<SearchConsoleQueryMetric> toQueryMetric(Map<String, String> row) {
        String query = first(row, QUERY_HEADERS);
        if (isBlank(query)) {
            return Optional.empty();
        }
        int clicks = parseInt(first(row, CLICK_HEADERS));
        int impressions = parseInt(first(row, IMPRESSION_HEADERS));
        double ctrPercent = parseCtrPercent(first(row, CTR_HEADERS));
        double position = parseDouble(first(row, POSITION_HEADERS));
        String queryKey = normalizeQuery(query);
        if (isBlank(queryKey)) {
            return Optional.empty();
        }
        return Optional.of(new SearchConsoleQueryMetric(
                query.trim(),
                queryKey,
                clicks,
                impressions,
                ctrPercent,
                position,
                queryBottleneckCode(clicks, impressions, ctrPercent, position),
                queryBottleneckLabel(clicks, impressions, ctrPercent, position),
                queryOpportunityScore(clicks, impressions, ctrPercent, position)
        ));
    }

    private Map<String, String> normalizeHeaders(Map<String, String> row) {
        Map<String, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : row.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            normalized.put(normalizeHeader(entry.getKey()), entry.getValue());
        }
        return normalized;
    }

    private String first(Map<String, String> row, List<String> headers) {
        for (String header : headers) {
            String value = row.get(normalizeHeader(header));
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private String normalizePath(String page) {
        String value = page.trim();
        int queryIndex = value.indexOf('?');
        if (queryIndex >= 0) {
            value = value.substring(0, queryIndex);
        }
        int hashIndex = value.indexOf('#');
        if (hashIndex >= 0) {
            value = value.substring(0, hashIndex);
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            try {
                URI uri = new URI(value);
                value = uri.getPath();
            } catch (URISyntaxException exception) {
                return "";
            }
        }
        if (isBlank(value)) {
            return "/";
        }
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        return value.replaceAll("/{2,}", "/");
    }

    private String normalizeQuery(String query) {
        if (isBlank(query)) {
            return "";
        }
        return query.trim()
                .toLowerCase(Locale.US)
                .replaceAll("\\s+", " ");
    }

    private static String bottleneckCode(int clicks, int impressions, double ctrPercent, double position) {
        if (clicks > 0) {
            return "earning_clicks";
        }
        if (impressions <= 0) {
            return "no_impressions";
        }
        if (impressions >= 50 && ctrPercent < 1.0 && position > 0 && position <= 20) {
            return "ctr_bottleneck";
        }
        if (position > 20) {
            return "ranking_bottleneck";
        }
        if (impressions < 50) {
            return "discovery_bottleneck";
        }
        return "watch";
    }

    private static String bottleneckLabel(int clicks, int impressions, double ctrPercent, double position) {
        return switch (bottleneckCode(clicks, impressions, ctrPercent, position)) {
            case "earning_clicks" -> "Already earning clicks";
            case "no_impressions" -> "No impressions yet";
            case "ctr_bottleneck" -> "CTR/title bottleneck";
            case "ranking_bottleneck" -> "Ranking/content depth bottleneck";
            case "discovery_bottleneck" -> "Discovery/indexing bottleneck";
            default -> "Watch in Search Console";
        };
    }

    private static int opportunityScore(int clicks, int impressions, double ctrPercent, double position) {
        if (clicks > 0) {
            return Math.min(clicks * 2, 30);
        }
        if (impressions >= 50 && ctrPercent < 1.0 && position > 0 && position <= 20) {
            return 40 + Math.min(impressions / 25, 30);
        }
        if (position > 20) {
            return 30 + Math.min(impressions / 50, 25);
        }
        if (impressions > 0) {
            return 18 + Math.min(impressions / 10, 20);
        }
        return 10;
    }

    private static String queryBottleneckCode(int clicks, int impressions, double ctrPercent, double position) {
        if (impressions <= 0) {
            return "no_impressions";
        }
        if (impressions >= 50 && ctrPercent < 1.0 && position > 0 && position <= 20) {
            return "ctr_bottleneck";
        }
        if (position > 20) {
            return "ranking_bottleneck";
        }
        if (clicks > 0) {
            return "earning_clicks";
        }
        if (impressions < 50) {
            return "discovery_bottleneck";
        }
        return "watch";
    }

    private static String queryBottleneckLabel(int clicks, int impressions, double ctrPercent, double position) {
        return switch (queryBottleneckCode(clicks, impressions, ctrPercent, position)) {
            case "earning_clicks" -> "Already earning clicks";
            case "no_impressions" -> "No impressions yet";
            case "ctr_bottleneck" -> "CTR/title bottleneck";
            case "ranking_bottleneck" -> "Ranking/content depth bottleneck";
            case "discovery_bottleneck" -> "Discovery/indexing bottleneck";
            default -> "Watch in Search Console";
        };
    }

    private static int queryOpportunityScore(int clicks, int impressions, double ctrPercent, double position) {
        if (impressions >= 50 && ctrPercent < 1.0 && position > 0 && position <= 20) {
            return 50 + Math.min(impressions / 25, 35);
        }
        if (position > 20) {
            return 35 + Math.min(impressions / 50, 30);
        }
        if (clicks > 0) {
            return Math.min(clicks * 2, 30);
        }
        if (impressions > 0) {
            return 18 + Math.min(impressions / 10, 20);
        }
        return 10;
    }

    private int parseInt(String value) {
        String cleaned = cleanNumeric(value);
        if (cleaned.isBlank()) {
            return 0;
        }
        return (int) Math.round(Double.parseDouble(cleaned));
    }

    private double parseCtrPercent(String value) {
        if (isBlank(value)) {
            return 0;
        }
        boolean percent = value.contains("%");
        double parsed = parseDouble(value);
        if (!percent && parsed > 0 && parsed <= 1) {
            return parsed * 100;
        }
        return parsed;
    }

    private double parseDouble(String value) {
        String cleaned = cleanNumeric(value);
        if (cleaned.isBlank()) {
            return 0;
        }
        return Double.parseDouble(cleaned);
    }

    private String cleanNumeric(String value) {
        if (isBlank(value)) {
            return "";
        }
        return value.trim()
                .replace("%", "")
                .replace(",", "")
                .replaceAll("[^0-9.\\-]", "");
    }

    private static String normalizeHeader(String value) {
        return value == null ? "" : value.replace("\uFEFF", "").trim().toLowerCase(Locale.US);
    }

    private static String stripTrailingSlash(String path) {
        if (path == null || path.length() <= 1) {
            return path;
        }
        return path.replaceAll("/+$", "");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record SearchConsoleSnapshot(
            String sourcePath,
            boolean available,
            int rowCount,
            int totalClicks,
            int totalImpressions,
            int ignoredRows,
            List<String> warnings,
            Map<String, SearchConsolePageMetric> metricsByPath,
            List<SearchConsolePageMetric> topBottlenecks,
            String querySourcePath,
            boolean queryAvailable,
            int queryRowCount,
            int queryTotalClicks,
            int queryTotalImpressions,
            int queryIgnoredRows,
            Map<String, SearchConsoleQueryMetric> metricsByQuery,
            List<SearchConsoleQueryMetric> topQueryOpportunities
    ) {
        public SearchConsoleSnapshot {
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
            metricsByPath = metricsByPath == null ? Map.of() : Map.copyOf(metricsByPath);
            topBottlenecks = topBottlenecks == null ? List.of() : List.copyOf(topBottlenecks);
            metricsByQuery = metricsByQuery == null ? Map.of() : Map.copyOf(metricsByQuery);
            topQueryOpportunities = topQueryOpportunities == null ? List.of() : List.copyOf(topQueryOpportunities);
        }

        public static SearchConsoleSnapshot missing(String sourcePath) {
            return new SearchConsoleSnapshot(
                    sourcePath,
                    false,
                    0,
                    0,
                    0,
                    0,
                    List.of(),
                    Map.of(),
                    List.of(),
                    "",
                    false,
                    0,
                    0,
                    0,
                    0,
                    Map.of(),
                    List.of()
            );
        }

        public SearchConsolePageMetric metricForPath(String path) {
            if (path == null) {
                return null;
            }
            SearchConsolePageMetric exact = metricsByPath.get(path);
            if (exact != null) {
                return exact;
            }
            return metricsByPath.get(stripTrailingSlash(path));
        }
    }

    public record SearchConsoleQueryMetric(
            String query,
            String queryKey,
            int clicks,
            int impressions,
            double ctrPercent,
            double position,
            String bottleneckCode,
            String bottleneckLabel,
            int opportunityScore
    ) {
        public SearchConsoleQueryMetric merge(SearchConsoleQueryMetric other) {
            if (other == null) {
                return this;
            }
            int mergedClicks = clicks + other.clicks;
            int mergedImpressions = impressions + other.impressions;
            double mergedCtr = mergedImpressions <= 0 ? 0 : (mergedClicks * 100.0) / mergedImpressions;
            double mergedPosition = weightedPosition(other, mergedImpressions);
            return new SearchConsoleQueryMetric(
                    query,
                    queryKey,
                    mergedClicks,
                    mergedImpressions,
                    mergedCtr,
                    mergedPosition,
                    SearchConsolePerformanceService.queryBottleneckCode(mergedClicks, mergedImpressions, mergedCtr, mergedPosition),
                    SearchConsolePerformanceService.queryBottleneckLabel(mergedClicks, mergedImpressions, mergedCtr, mergedPosition),
                    SearchConsolePerformanceService.queryOpportunityScore(mergedClicks, mergedImpressions, mergedCtr, mergedPosition)
            );
        }

        public String displayCtr() {
            return String.format(Locale.US, "%.1f%%", ctrPercent);
        }

        public String displayPosition() {
            if (position <= 0) {
                return "n/a";
            }
            return String.format(Locale.US, "%.1f", position);
        }

        private double weightedPosition(SearchConsoleQueryMetric other, int mergedImpressions) {
            if (mergedImpressions <= 0) {
                return Math.max(position, other.position);
            }
            return ((position * impressions) + (other.position * other.impressions)) / mergedImpressions;
        }
    }

    public record SearchConsolePageMetric(
            String page,
            String path,
            int clicks,
            int impressions,
            double ctrPercent,
            double position,
            String bottleneckCode,
            String bottleneckLabel,
            int opportunityScore
    ) {
        public SearchConsolePageMetric merge(SearchConsolePageMetric other) {
            if (other == null) {
                return this;
            }
            int mergedClicks = clicks + other.clicks;
            int mergedImpressions = impressions + other.impressions;
            double mergedCtr = mergedImpressions <= 0 ? 0 : (mergedClicks * 100.0) / mergedImpressions;
            double mergedPosition = weightedPosition(other, mergedImpressions);
            return new SearchConsolePageMetric(
                    page,
                    path,
                    mergedClicks,
                    mergedImpressions,
                    mergedCtr,
                    mergedPosition,
                    SearchConsolePerformanceService.bottleneckCode(mergedClicks, mergedImpressions, mergedCtr, mergedPosition),
                    SearchConsolePerformanceService.bottleneckLabel(mergedClicks, mergedImpressions, mergedCtr, mergedPosition),
                    SearchConsolePerformanceService.opportunityScore(mergedClicks, mergedImpressions, mergedCtr, mergedPosition)
            );
        }

        public String displayCtr() {
            return String.format(Locale.US, "%.1f%%", ctrPercent);
        }

        public String displayPosition() {
            if (position <= 0) {
                return "n/a";
            }
            return String.format(Locale.US, "%.1f", position);
        }

        private double weightedPosition(SearchConsolePageMetric other, int mergedImpressions) {
            if (mergedImpressions <= 0) {
                return Math.max(position, other.position);
            }
            return ((position * impressions) + (other.position * other.impressions)) / mergedImpressions;
        }
    }

    private record PageLoadResult(
            boolean available,
            int ignoredRows,
            int totalClicks,
            int totalImpressions,
            Map<String, SearchConsolePageMetric> metricsByPath
    ) {
        private static PageLoadResult missing() {
            return new PageLoadResult(false, 0, 0, 0, Map.of());
        }
    }

    private record QueryLoadResult(
            boolean available,
            int ignoredRows,
            int totalClicks,
            int totalImpressions,
            Map<String, SearchConsoleQueryMetric> metricsByQuery
    ) {
        private static QueryLoadResult missing() {
            return new QueryLoadResult(false, 0, 0, 0, Map.of());
        }
    }
}
