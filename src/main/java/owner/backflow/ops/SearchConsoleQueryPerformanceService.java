package owner.backflow.ops;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.IOException;
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
public class SearchConsoleQueryPerformanceService {
    private static final List<String> QUERY_HEADERS = List.of("query", "top queries", "queries", "search query", "keyword", "keywords");
    private static final List<String> CLICK_HEADERS = List.of("clicks");
    private static final List<String> IMPRESSION_HEADERS = List.of("impressions");
    private static final List<String> CTR_HEADERS = List.of("ctr", "click through rate");
    private static final List<String> POSITION_HEADERS = List.of("position", "avg position", "average position");

    private final AppOpsProperties opsProperties;
    private final CsvMapper csvMapper;

    public SearchConsoleQueryPerformanceService(AppOpsProperties opsProperties) {
        this.opsProperties = opsProperties;
        this.csvMapper = CsvMapper.builder().findAndAddModules().build();
    }

    public SearchConsoleQuerySnapshot loadSnapshot() {
        Path path = Path.of(opsProperties.searchConsoleQueriesPath()).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            return SearchConsoleQuerySnapshot.missing(path.toString());
        }

        List<String> warnings = new java.util.ArrayList<>();
        Map<String, SearchConsoleQueryMetric> metricsByQuery = new LinkedHashMap<>();
        int ignoredRows = 0;
        try {
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            MappingIterator<Map<String, String>> iterator = csvMapper
                    .readerFor(new TypeReference<Map<String, String>>() {
                    })
                    .with(schema)
                    .readValues(path.toFile());
            while (iterator.hasNext()) {
                Map<String, String> row = normalizeHeaders(iterator.next());
                Optional<SearchConsoleQueryMetric> metric = toMetric(row);
                if (metric.isEmpty()) {
                    ignoredRows++;
                    continue;
                }
                SearchConsoleQueryMetric value = metric.get();
                metricsByQuery.merge(value.normalizedQuery(), value, SearchConsoleQueryMetric::merge);
            }
        } catch (IOException | IllegalArgumentException exception) {
            warnings.add("Could not read Search Console query CSV: " + exception.getMessage());
            return new SearchConsoleQuerySnapshot(path.toString(), false, 0, 0, 0, ignoredRows, warnings, Map.of(), List.of());
        }

        if (ignoredRows > 0) {
            warnings.add(ignoredRows + " query row(s) ignored because query, clicks, impressions, CTR, or position columns were missing.");
        }

        List<SearchConsoleQueryMetric> bottlenecks = metricsByQuery.values().stream()
                .filter(metric -> !"earning_clicks".equals(metric.bottleneckCode()))
                .sorted(Comparator.comparingInt(SearchConsoleQueryMetric::opportunityScore).reversed()
                        .thenComparing(Comparator.comparingInt(SearchConsoleQueryMetric::impressions).reversed())
                        .thenComparing(SearchConsoleQueryMetric::query))
                .limit(30)
                .toList();

        int totalClicks = metricsByQuery.values().stream().mapToInt(SearchConsoleQueryMetric::clicks).sum();
        int totalImpressions = metricsByQuery.values().stream().mapToInt(SearchConsoleQueryMetric::impressions).sum();

        return new SearchConsoleQuerySnapshot(
                path.toString(),
                true,
                metricsByQuery.size(),
                totalClicks,
                totalImpressions,
                ignoredRows,
                warnings,
                metricsByQuery,
                bottlenecks
        );
    }

    private Optional<SearchConsoleQueryMetric> toMetric(Map<String, String> row) {
        String query = first(row, QUERY_HEADERS);
        if (isBlank(query)) {
            return Optional.empty();
        }
        int clicks = parseInt(first(row, CLICK_HEADERS));
        int impressions = parseInt(first(row, IMPRESSION_HEADERS));
        double ctrPercent = parseCtrPercent(first(row, CTR_HEADERS));
        double position = parseDouble(first(row, POSITION_HEADERS));
        String normalizedQuery = normalizeQuery(query);
        if (isBlank(normalizedQuery)) {
            return Optional.empty();
        }
        return Optional.of(new SearchConsoleQueryMetric(
                query.trim(),
                normalizedQuery,
                clicks,
                impressions,
                ctrPercent,
                position,
                intentFamily(normalizedQuery),
                bottleneckCode(clicks, impressions, ctrPercent, position),
                bottleneckLabel(clicks, impressions, ctrPercent, position),
                opportunityScore(clicks, impressions, ctrPercent, position)
        ));
    }

    private Map<String, String> normalizeHeaders(Map<String, String> row) {
        Map<String, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : row.entrySet()) {
            if (entry.getKey() != null) {
                normalized.put(normalizeHeader(entry.getKey()), entry.getValue());
            }
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

    private static String intentFamily(String normalizedQuery) {
        if (containsAny(normalizedQuery, "failed", "fail", "repair", "retest")) {
            return "failed-test";
        }
        if (containsAny(normalizedQuery, "submit", "submission", "upload", "file report", "test report")) {
            return "submit-report";
        }
        if (containsAny(normalizedQuery, "approved", "certified", "registered", "tester", "testers", "calibration")) {
            return "approved-testers";
        }
        if (containsAny(normalizedQuery, "portal", "swiftcomply", "c3swift", "bsi", "weirs", "vepo", "envirotrax", "aqua", "trackmybackflow", "tokay", "spry")) {
            return "portal";
        }
        if (containsAny(normalizedQuery, "annual", "notice", "due", "deadline", "anniversary")) {
            return "annual-notice";
        }
        if (containsAny(normalizedQuery, "cost", "price", "fee", "fine", "penalty")) {
            return "cost-fee";
        }
        if (containsAny(normalizedQuery, "irrigation", "sprinkler")) {
            return "irrigation";
        }
        if (containsAny(normalizedQuery, "fire line", "fireline", "fire protection", "fire sprinkler")) {
            return "fire-line";
        }
        return "general-testing";
    }

    private static String normalizeHeader(String value) {
        return value == null ? "" : value.replace("\uFEFF", "").trim().toLowerCase(Locale.US);
    }

    private static String normalizeQuery(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.US);
    }

    private static boolean containsAny(String value, String... keywords) {
        if (isBlank(value)) {
            return false;
        }
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record SearchConsoleQuerySnapshot(
            String sourcePath,
            boolean available,
            int rowCount,
            int totalClicks,
            int totalImpressions,
            int ignoredRows,
            List<String> warnings,
            Map<String, SearchConsoleQueryMetric> metricsByQuery,
            List<SearchConsoleQueryMetric> topBottlenecks
    ) {
        public SearchConsoleQuerySnapshot {
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
            metricsByQuery = metricsByQuery == null ? Map.of() : Map.copyOf(metricsByQuery);
            topBottlenecks = topBottlenecks == null ? List.of() : List.copyOf(topBottlenecks);
        }

        public static SearchConsoleQuerySnapshot missing(String sourcePath) {
            return new SearchConsoleQuerySnapshot(sourcePath, false, 0, 0, 0, 0, List.of(), Map.of(), List.of());
        }
    }

    public record SearchConsoleQueryMetric(
            String query,
            String normalizedQuery,
            int clicks,
            int impressions,
            double ctrPercent,
            double position,
            String intentFamily,
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
                    normalizedQuery,
                    mergedClicks,
                    mergedImpressions,
                    mergedCtr,
                    mergedPosition,
                    intentFamily,
                    SearchConsoleQueryPerformanceService.bottleneckCode(mergedClicks, mergedImpressions, mergedCtr, mergedPosition),
                    SearchConsoleQueryPerformanceService.bottleneckLabel(mergedClicks, mergedImpressions, mergedCtr, mergedPosition),
                    SearchConsoleQueryPerformanceService.opportunityScore(mergedClicks, mergedImpressions, mergedCtr, mergedPosition)
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

        public String priorityLabel() {
            return switch (bottleneckCode) {
                case "ctr_bottleneck" -> "Rewrite now";
                case "ranking_bottleneck" -> "Add depth";
                case "discovery_bottleneck", "no_impressions" -> "Add links";
                case "watch" -> "Watch next export";
                default -> clicks > 0 ? "Protect winner" : "Review";
            };
        }

        public String suggestedTitlePattern() {
            return switch (intentFamily) {
                case "failed-test" -> "[City/Utility] failed backflow test: repair, retest, and report deadline";
                case "submit-report" -> "Submit a backflow test report in [City]: portal, notice ID, and proof";
                case "approved-testers" -> "[City/Utility] approved backflow testers: official list and report rules";
                case "portal" -> "[Portal] backflow report routes by city, tester gate, and filing proof";
                case "annual-notice" -> "[City/Utility] annual backflow testing: due date, tester, and report route";
                case "cost-fee" -> "Backflow test cost in [City]: testing, repair, retest, and filing fees";
                case "irrigation" -> "[City/Utility] irrigation backflow testing: sprinkler rules and report route";
                case "fire-line" -> "[City/Utility] fire line backflow testing: tester gate and report route";
                default -> "[City/Utility] backflow testing: deadline, tester gate, portal, and next step";
            };
        }

        public String suggestedH1Pattern() {
            return switch (intentFamily) {
                case "failed-test" -> "Failed backflow test next steps for [City/Utility]";
                case "submit-report" -> "Submit a backflow test report for [City/Utility]";
                case "approved-testers" -> "Approved backflow tester route for [City/Utility]";
                case "portal" -> "[Portal] backflow reporting routes by utility";
                case "annual-notice" -> "Annual backflow testing notice steps for [City/Utility]";
                case "cost-fee" -> "Backflow testing cost and fee signals for [City/Utility]";
                case "irrigation" -> "Irrigation backflow testing rules for [City/Utility]";
                case "fire-line" -> "Fire line backflow testing rules for [City/Utility]";
                default -> "Backflow testing requirements for [City/Utility]";
            };
        }

        public String suggestedMetaPattern() {
            return switch (intentFamily) {
                case "failed-test" -> "Confirm repair, retest, deadline, accepted report proof, and utility contact before closing a failed backflow test.";
                case "submit-report" -> "Find the portal, notice/device ID clue, approved tester gate, report deadline, and proof needed to submit a backflow test report.";
                case "approved-testers" -> "Use the official tester route and verify credential, registration, portal enrollment, and report submission rules before scheduling.";
                case "portal" -> "Compare portal routes by city and utility, including tester gate, notice ID, report acceptance, fee, and failed-test handling.";
                case "annual-notice" -> "Confirm annual due basis, accepted tester route, portal/report method, and proof needed to keep the utility account compliant.";
                case "cost-fee" -> "Review source-backed testing, repair, retest, filing, late-fee, and penalty signals before choosing the next route.";
                default -> "Find the source-backed utility workflow, tester route, report path, deadline, and next step for local backflow compliance.";
            };
        }

        public String action() {
            if ("ctr_bottleneck".equals(bottleneckCode)) {
                return "Rewrite title, H1, and meta around this exact query intent.";
            }
            if ("ranking_bottleneck".equals(bottleneckCode)) {
                return "Add visible hard facts before rewriting: portal, notice ID, tester gate, deadline, fee, failed-test branch.";
            }
            if ("discovery_bottleneck".equals(bottleneckCode) || "no_impressions".equals(bottleneckCode)) {
                return "Add internal links from state, portal, guide, and city-task pages.";
            }
            return "Review after the next Search Console export.";
        }

        private double weightedPosition(SearchConsoleQueryMetric other, int mergedImpressions) {
            if (mergedImpressions <= 0) {
                return Math.max(position, other.position);
            }
            return ((position * impressions) + (other.position * other.impressions)) / mergedImpressions;
        }
    }
}
