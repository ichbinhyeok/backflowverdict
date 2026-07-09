package owner.backflow.ops;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import owner.backflow.data.model.ApprovedTesterMode;
import owner.backflow.data.model.CostBand;
import owner.backflow.data.model.SourceLink;
import owner.backflow.data.model.SubmissionMethod;
import owner.backflow.data.model.UtilityRecord;
import owner.backflow.files.BackflowRegistryService;
import owner.backflow.ops.SearchConsolePerformanceService.SearchConsolePageMetric;
import owner.backflow.ops.SearchConsolePerformanceService.SearchConsoleSnapshot;
import org.springframework.stereotype.Service;

@Service
public class SeoScorecardService {
    private static final int STRONG_ROUTE_LIMIT = 10;
    private static final int IMPROVEMENT_LIMIT = 12;
    private static final List<String> PRIORITY_INTENTS = List.of(
            "submit-backflow-report",
            "backflow-reporting-portal",
            "annual-backflow-testing",
            "approved-backflow-testers",
            "failed-backflow-test",
            "irrigation-backflow-testing",
            "fire-line-backflow-testing"
    );

    private final BackflowRegistryService registryService;
    private final SearchConsolePerformanceService searchConsolePerformanceService;

    public SeoScorecardService(
            BackflowRegistryService registryService,
            SearchConsolePerformanceService searchConsolePerformanceService
    ) {
        this.registryService = registryService;
        this.searchConsolePerformanceService = searchConsolePerformanceService;
    }

    public SeoScorecardReport buildReport() {
        List<UtilityRecord> utilities = registryService.listPublishedUtilities();
        SearchConsoleSnapshot searchConsole = searchConsolePerformanceService.loadSnapshot();
        List<SeoScorecardItem> items = utilities.stream()
                .map(utility -> scoreUtility(utility, searchConsole))
                .toList();

        List<SeoScorecardItem> strongestRoutes = items.stream()
                .sorted(Comparator.comparingInt(SeoScorecardItem::score).reversed()
                        .thenComparing(SeoScorecardItem::utilityName))
                .limit(STRONG_ROUTE_LIMIT)
                .toList();

        List<SeoScorecardItem> improvementCandidates = items.stream()
                .filter(item -> !item.gaps().isEmpty())
                .sorted(Comparator.comparingInt(SeoScorecardItem::opportunityScore).reversed()
                        .thenComparing(Comparator.comparingInt(SeoScorecardItem::score).reversed())
                        .thenComparing(SeoScorecardItem::utilityName))
                .limit(IMPROVEMENT_LIMIT)
                .toList();

        return new SeoScorecardReport(
                new SeoScorecardSummary(
                        utilities.size(),
                        count(utilities.stream().filter(UtilityRecord::meetsIndexQualityFloor).toList()),
                        count(utilities.stream().filter(utility -> utility.hasReportWorkflow()
                                && utility.hasTesterGate()
                                && utility.hasDeadlinePolicy()).toList()),
                        count(utilities.stream().filter(UtilityRecord::hasReportWorkflow).toList()),
                        count(utilities.stream().filter(UtilityRecord::hasTesterGate).toList()),
                        count(utilities.stream().filter(UtilityRecord::hasDeadlinePolicy).toList()),
                        count(utilities.stream().filter(UtilityRecord::hasFailedTestPolicy).toList()),
                        count(utilities.stream().filter(this::hasStructuredFees).toList()),
                        count(items.stream().filter(item -> item.score() >= 80).toList()),
                        improvementCandidates.size(),
                        searchConsole.available(),
                        searchConsole.sourcePath(),
                        searchConsole.totalClicks(),
                        searchConsole.totalImpressions(),
                        count(items.stream().filter(item -> item.searchConsolePerformance() != null).toList()),
                        count(items.stream().filter(item -> "ctr_bottleneck".equals(item.searchConsoleBottleneckCode())).toList()),
                        count(items.stream().filter(item -> "ranking_bottleneck".equals(item.searchConsoleBottleneckCode())).toList()),
                        count(items.stream().filter(item -> "discovery_bottleneck".equals(item.searchConsoleBottleneckCode())
                                || "no_impressions".equals(item.searchConsoleBottleneckCode())).toList()),
                        count(items.stream().filter(item -> item.searchConsolePerformance() == null).toList())
                ),
                strongestRoutes,
                improvementCandidates,
                searchConsole
        );
    }

    private SeoScorecardItem scoreUtility(UtilityRecord utility, SearchConsoleSnapshot searchConsole) {
        List<String> strengths = new ArrayList<>();
        List<String> gaps = new ArrayList<>();
        List<String> nextActions = new ArrayList<>();
        String evidence = evidenceText(utility);
        String path = utilityPath(utility);
        SearchConsolePageMetric searchConsolePerformance = searchConsole.metricForPath(path);

        int score = utility.indexQualityScore() * 3;
        int opportunityScore = utility.meetsIndexQualityFloor() ? 18 : 8;

        if (utility.meetsIndexQualityFloor()) {
            score += 8;
            strengths.add("Index quality floor cleared (" + utility.indexQualityScore() + ")");
        } else {
            gaps.add("Index quality score is below the publishing floor (" + utility.indexQualityScore() + ")");
            nextActions.add("Add official source links, workflow steps, submission methods, and current verification date.");
            opportunityScore += 16;
        }

        if (utility.hasReportWorkflow()) {
            score += 14;
            strengths.add("Structured report workflow");
            if (utility.reportWorkflow().sourceRefs().isEmpty()) {
                gaps.add("Report workflow needs sourceRefs for stronger evidence");
                nextActions.add("Attach sourceRefs to report_workflow for the utility JSON.");
                opportunityScore += 8;
            }
        } else if (hasReportWorkflowSignal(utility, evidence)) {
            gaps.add("Submission or portal language exists but report_workflow is not structured");
            nextActions.add("Add portal vendor, submitter, accepted format, deadline, required IDs, and sourceRefs.");
            opportunityScore += 18;
        }

        if (utility.hasTesterGate()) {
            score += 10;
            strengths.add("Tester gate facts");
            if (utility.testerGate().sourceRefs().isEmpty()) {
                gaps.add("Tester gate needs sourceRefs for stronger evidence");
                nextActions.add("Attach sourceRefs to tester_gate for credential, registration, or portal enrollment claims.");
                opportunityScore += 6;
            }
        } else if (hasTesterGateSignal(utility, evidence)) {
            gaps.add("Tester approval signal exists but tester_gate is not structured");
            nextActions.add("Add license, utility registration, portal enrollment, gauge calibration, and sourceRefs.");
            opportunityScore += 14;
        }

        if (utility.hasDeadlinePolicy()) {
            score += 10;
            strengths.add("Deadline policy facts");
            if (utility.deadlinePolicy().sourceRefs().isEmpty()) {
                gaps.add("Deadline policy needs sourceRefs for stronger evidence");
                nextActions.add("Attach sourceRefs to deadline_policy for cadence, notice, or report due claims.");
                opportunityScore += 6;
            }
        } else if (hasDeadlineSignal(utility, evidence)) {
            gaps.add("Annual, due-date, or notice language exists but deadline_policy is not structured");
            nextActions.add("Add cadence, report due days, notice window, past-due ladder, and sourceRefs.");
            opportunityScore += 14;
        }

        if (utility.hasFailedTestPolicy()) {
            score += 7;
            strengths.add("Failed-test policy facts");
            if (utility.failedTestPolicy().sourceRefs().isEmpty()) {
                gaps.add("Failed-test policy needs sourceRefs for stronger evidence");
                nextActions.add("Attach sourceRefs to failed_test_policy for repair, retest, shutoff, or penalty claims.");
                opportunityScore += 5;
            }
        } else if (hasFailedTestSignal(utility, evidence)) {
            gaps.add("Failure, retest, repair, or penalty language exists but failed_test_policy is not structured");
            nextActions.add("Add failed report deadline, repair deadline, retest requirement, shutoff risk, and sourceRefs.");
            opportunityScore += 12;
        }

        if (hasStructuredFees(utility)) {
            score += 6;
            strengths.add("Structured fees or fines");
        } else if (hasCostSignal(utility, evidence)) {
            gaps.add("Fee or penalty language exists but costBand has no structured fee fields");
            nextActions.add("Convert filing, portal, late, fine, or penalty amounts into structured FeeAmount fields.");
            opportunityScore += 9;
        }

        int priorityIntentCount = priorityIntentCount(utility);
        if (priorityIntentCount > 0) {
            score += priorityIntentCount * 3;
            strengths.add(priorityIntentCount + " priority city-intent route(s)");
        }

        int focusPageCount = focusPageCount(utility);
        if (focusPageCount > 0) {
            score += focusPageCount * 2;
            strengths.add(focusPageCount + " utility focus page(s)");
        }

        if (!utility.serviceAreaCities().isEmpty()) {
            score += Math.min(utility.serviceAreaCities().size(), 6);
            strengths.add(utility.serviceAreaCities().size() + " service-area city signal(s)");
            opportunityScore += Math.min(utility.serviceAreaCities().size(), 5);
        }

        if (!utility.sources().isEmpty()) {
            score += Math.min(utility.sources().size() * 2, 8);
            strengths.add(utility.sources().size() + " official source link(s)");
        } else {
            gaps.add("No official source links attached");
            nextActions.add("Add at least two official utility source links before expanding internal links.");
            opportunityScore += 12;
        }

        if (hasPortalSignal(evidence)) {
            score += 5;
            strengths.add("Portal or online submission signal");
        }

        if (searchConsolePerformance != null) {
            score += searchConsolePerformance.clicks() > 0 ? Math.min(searchConsolePerformance.clicks() * 2, 10) : 0;
            score += searchConsolePerformance.impressions() > 0 ? 4 : 0;
            strengths.add(searchConsolePerformance.clicks() + " clicks / "
                    + searchConsolePerformance.impressions() + " impressions in Search Console");
            opportunityScore += searchConsolePerformance.opportunityScore();
            switch (searchConsolePerformance.bottleneckCode()) {
                case "ctr_bottleneck" -> {
                    gaps.add("Search Console shows impressions but weak CTR");
                    nextActions.add("Rewrite title/meta around the query family and put the deadline, portal, or approved-tester hook first.");
                }
                case "ranking_bottleneck" -> {
                    gaps.add("Search Console shows ranking/content depth bottleneck");
                    nextActions.add("Add source-backed utility details, internal links, and city-intent variants before asking for reindexing.");
                }
                case "discovery_bottleneck", "no_impressions" -> {
                    gaps.add("Search Console shows low or no impressions");
                    nextActions.add("Keep the URL in the priority sitemap and add more internal links from state, hub, and related guide pages.");
                }
                default -> {
                    if (searchConsolePerformance.clicks() > 0) {
                        strengths.add("Already earning organic clicks");
                    }
                }
            }
        } else if (searchConsole.available()) {
            gaps.add("No Search Console page row for this route yet");
            nextActions.add("Confirm the URL is indexed, then add internal links or request indexing if it remains invisible.");
            opportunityScore += utility.meetsIndexQualityFloor() ? 10 : 4;
        }

        if (nextActions.isEmpty()) {
            nextActions.add("Keep this route in the priority sitemap and watch Search Console impressions by query family.");
        }

        return new SeoScorecardItem(
                utility.utilityId(),
                utility.utilityName(),
                utility.state(),
                path,
                Math.min(score, 100),
                utility.indexQualityScore(),
                opportunityScore,
                searchConsolePerformance,
                strengths,
                gaps,
                distinct(nextActions)
        );
    }

    private int priorityIntentCount(UtilityRecord utility) {
        int count = 0;
        for (String intent : PRIORITY_INTENTS) {
            if (utility.supportsCityIntent(intent)) {
                count++;
            }
        }
        return count;
    }

    private int focusPageCount(UtilityRecord utility) {
        int count = 0;
        count += utility.supportsAnnualTestingPage() ? 1 : 0;
        count += utility.supportsApprovedTestersPage() || utility.supportsFindATesterPage() ? 1 : 0;
        count += utility.supportsIrrigationPage() ? 1 : 0;
        count += utility.supportsFireLinePage() ? 1 : 0;
        count += utility.supportsFailedTestPage() ? 1 : 0;
        return count;
    }

    private boolean hasStructuredFees(UtilityRecord utility) {
        return utility.costBand() != null && utility.costBand().hasStructuredFees();
    }

    private boolean hasReportWorkflowSignal(UtilityRecord utility, String evidence) {
        return !utility.submissionMethods().isEmpty()
                || hasPortalSignal(evidence)
                || containsAny(evidence, "submit", "submission", "report", "backflow test report", "test report");
    }

    private boolean hasTesterGateSignal(UtilityRecord utility, String evidence) {
        return utility.approvedTesterMode() != ApprovedTesterMode.NONE
                || utility.supportsApprovedTestersPage()
                || containsAny(evidence,
                "approved tester",
                "certified tester",
                "registered tester",
                "licensed tester",
                "tester list",
                "gauge calibration",
                "portal enrollment");
    }

    private boolean hasDeadlineSignal(UtilityRecord utility, String evidence) {
        return !isBlank(utility.testingFrequency())
                || !isBlank(utility.dueBasis())
                || containsAny(evidence,
                "annual",
                "annually",
                "due",
                "deadline",
                "within",
                "notice",
                "past due",
                "calendar",
                "anniversary");
    }

    private boolean hasFailedTestSignal(UtilityRecord utility, String evidence) {
        return !utility.failureHighlights().isEmpty()
                || containsAny(evidence,
                "failed",
                "failure",
                "retest",
                "repair",
                "correction",
                "shutoff",
                "shut off",
                "penalty",
                "violation");
    }

    private boolean hasCostSignal(UtilityRecord utility, String evidence) {
        CostBand costBand = utility.costBand();
        return costBand != null && (!isBlank(costBand.testingRange())
                || !isBlank(costBand.repairRetestRange())
                || !isBlank(costBand.pricingNotes()))
                || containsAny(evidence, "$", " fee", " fine", " penalty", "late charge", "portal fee");
    }

    private boolean hasPortalSignal(String evidence) {
        return containsAny(evidence,
                "portal",
                "online",
                "swiftcomply",
                "c3swift",
                "bsi",
                "backflowtest",
                "vepo",
                "envirotrax",
                "aqua backflow",
                "aquabackflow",
                "trackmybackflow",
                "tokay",
                "webtest",
                "sprybackflow");
    }

    private String evidenceText(UtilityRecord utility) {
        StringBuilder text = new StringBuilder();
        append(text, utility.utilityName());
        append(text, utility.utilityUrl());
        append(text, utility.approvedTesterListUrl());
        append(text, utility.officialListLabel());
        append(text, utility.testingFrequency());
        append(text, utility.dueBasis());
        append(text, utility.penalties());
        append(text, utility.sourceExcerpt());
        append(text, utility.verdictSummary());
        append(text, utility.whoIsAffected());
        utility.searchAliases().forEach(value -> append(text, value));
        utility.officialProgramUrls().forEach(value -> append(text, value));
        utility.coveredPropertyTypes().forEach(value -> append(text, value));
        utility.coveredDeviceTypes().forEach(value -> append(text, value));
        utility.workflowSteps().forEach(value -> append(text, value));
        utility.failureHighlights().forEach(value -> append(text, value));
        utility.submissionMethods().forEach(method -> appendSubmission(text, method));
        utility.sources().forEach(source -> appendSource(text, source));
        append(text, utility.reportWorkflow().portalVendor());
        append(text, utility.reportWorkflow().portalName());
        append(text, utility.reportWorkflow().portalUrl());
        append(text, utility.reportWorkflow().submitter());
        append(text, utility.reportWorkflow().acceptanceProof());
        utility.reportWorkflow().requiredIdentifiers().forEach(value -> append(text, value));
        append(text, utility.testerGate().deviceScopeLimit());
        utility.testerGate().credentialDocuments().forEach(value -> append(text, value));
        utility.deadlinePolicy().cadenceByPropertyType().forEach(value -> append(text, value));
        utility.deadlinePolicy().pastDueLadder().forEach(value -> append(text, value));
        append(text, utility.deadlinePolicy().calendarWindow());
        appendCostBand(text, utility.costBand());
        return text.toString();
    }

    private void appendSubmission(StringBuilder text, SubmissionMethod method) {
        append(text, method.label());
        append(text, method.url());
        append(text, method.kind());
    }

    private void appendSource(StringBuilder text, SourceLink source) {
        append(text, source.label());
        append(text, source.url());
        append(text, source.kind());
    }

    private void appendCostBand(StringBuilder text, CostBand costBand) {
        if (costBand == null) {
            return;
        }
        append(text, costBand.testingRange());
        append(text, costBand.repairRetestRange());
        append(text, costBand.pricingNotes());
        append(text, costBand.feeNotes());
    }

    private String utilityPath(UtilityRecord utility) {
        return "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/";
    }

    private static void append(StringBuilder builder, String value) {
        if (!isBlank(value)) {
            builder.append(' ').append(value);
        }
    }

    private static boolean containsAny(String value, String... keywords) {
        if (isBlank(value)) {
            return false;
        }
        String lowered = value.toLowerCase(Locale.US);
        for (String keyword : keywords) {
            if (lowered.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> distinct(List<String> values) {
        return values.stream().distinct().toList();
    }

    private static int count(List<?> values) {
        return values.size();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record SeoScorecardReport(
            SeoScorecardSummary summary,
            List<SeoScorecardItem> strongestRoutes,
            List<SeoScorecardItem> improvementCandidates,
            SearchConsoleSnapshot searchConsole
    ) {
        public SeoScorecardReport {
            strongestRoutes = strongestRoutes == null ? List.of() : List.copyOf(strongestRoutes);
            improvementCandidates = improvementCandidates == null ? List.of() : List.copyOf(improvementCandidates);
            searchConsole = searchConsole == null ? SearchConsoleSnapshot.missing("") : searchConsole;
        }
    }

    public record SeoScorecardSummary(
            int publishedUtilityCount,
            int indexReadyUtilityCount,
            int structuredWorkflowCount,
            int reportWorkflowCount,
            int testerGateCount,
            int deadlinePolicyCount,
            int failedTestPolicyCount,
            int structuredFeeCount,
            int highScoreRouteCount,
            int improvementCandidateCount,
            boolean searchConsoleAvailable,
            String searchConsoleSourcePath,
            int searchConsoleTotalClicks,
            int searchConsoleTotalImpressions,
            int searchConsoleMatchedRouteCount,
            int searchConsoleCtrBottleneckCount,
            int searchConsoleRankingBottleneckCount,
            int searchConsoleDiscoveryBottleneckCount,
            int searchConsoleNoDataRouteCount
    ) {
        public int indexReadyCoveragePercent() {
            return percent(indexReadyUtilityCount, publishedUtilityCount);
        }

        public int structuredWorkflowCoveragePercent() {
            return percent(structuredWorkflowCount, publishedUtilityCount);
        }

        public int reportWorkflowCoveragePercent() {
            return percent(reportWorkflowCount, publishedUtilityCount);
        }

        public int testerGateCoveragePercent() {
            return percent(testerGateCount, publishedUtilityCount);
        }

        public int deadlinePolicyCoveragePercent() {
            return percent(deadlinePolicyCount, publishedUtilityCount);
        }

        public int failedTestPolicyCoveragePercent() {
            return percent(failedTestPolicyCount, publishedUtilityCount);
        }

        public int structuredFeeCoveragePercent() {
            return percent(structuredFeeCount, publishedUtilityCount);
        }

        public int searchConsoleMatchedRoutePercent() {
            return percent(searchConsoleMatchedRouteCount, publishedUtilityCount);
        }

        private static int percent(int numerator, int denominator) {
            if (denominator <= 0) {
                return 0;
            }
            return Math.round((numerator * 100f) / denominator);
        }
    }

    public record SeoScorecardItem(
            String utilityId,
            String utilityName,
            String state,
            String path,
            int score,
            int indexQualityScore,
            int opportunityScore,
            SearchConsolePageMetric searchConsolePerformance,
            List<String> strengths,
            List<String> gaps,
            List<String> nextActions
    ) {
        public SeoScorecardItem {
            strengths = strengths == null ? List.of() : List.copyOf(strengths);
            gaps = gaps == null ? List.of() : List.copyOf(gaps);
            nextActions = nextActions == null ? List.of() : List.copyOf(nextActions);
        }

        public String displayScore() {
            return score + "/100";
        }

        public String searchConsoleBottleneckCode() {
            return searchConsolePerformance == null ? "no_data" : searchConsolePerformance.bottleneckCode();
        }

        public String searchConsoleBottleneckLabel() {
            return searchConsolePerformance == null ? "No Search Console row" : searchConsolePerformance.bottleneckLabel();
        }
    }
}
