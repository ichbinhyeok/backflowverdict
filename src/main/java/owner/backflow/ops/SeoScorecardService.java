package owner.backflow.ops;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import owner.backflow.data.model.ApprovedTesterMode;
import owner.backflow.data.model.CityAliasRecord;
import owner.backflow.data.model.CostBand;
import owner.backflow.data.model.SourceLink;
import owner.backflow.data.model.SubmissionMethod;
import owner.backflow.data.model.UtilityRecord;
import owner.backflow.files.BackflowRegistryService;
import owner.backflow.ops.SearchConsolePerformanceService.SearchConsolePageMetric;
import owner.backflow.ops.SearchConsolePerformanceService.SearchConsoleSnapshot;
import owner.backflow.ops.SearchConsoleQueryPerformanceService.SearchConsoleQueryMetric;
import owner.backflow.ops.SearchConsoleQueryPerformanceService.SearchConsoleQuerySnapshot;
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
    private final SearchConsoleQueryPerformanceService searchConsoleQueryPerformanceService;

    public SeoScorecardService(
            BackflowRegistryService registryService,
            SearchConsolePerformanceService searchConsolePerformanceService,
            SearchConsoleQueryPerformanceService searchConsoleQueryPerformanceService
    ) {
        this.registryService = registryService;
        this.searchConsolePerformanceService = searchConsolePerformanceService;
        this.searchConsoleQueryPerformanceService = searchConsoleQueryPerformanceService;
    }

    public SeoScorecardReport buildReport() {
        List<UtilityRecord> utilities = registryService.listPublishedUtilities();
        SearchConsoleSnapshot searchConsole = searchConsolePerformanceService.loadSnapshot();
        SearchConsoleQuerySnapshot searchConsoleQueries = searchConsoleQueryPerformanceService.loadSnapshot();
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
                searchConsole,
                searchConsoleQueries,
                rewriteQueue(searchConsole),
                queryRewriteQueue(searchConsoleQueries),
                deepFactQueue(utilities)
        );
    }

    public String exportRewriteQueueCsv() {
        SeoScorecardReport report = buildReport();
        StringBuilder csv = new StringBuilder();
        csv.append("priority,page,bottleneck,clicks,impressions,ctr,position,suggested_title_pattern,action\n");
        for (SeoRewriteQueueItem item : report.rewriteQueue()) {
            appendCsvRow(csv,
                    item.priority(),
                    item.path(),
                    item.bottleneck(),
                    String.valueOf(item.clicks()),
                    String.valueOf(item.impressions()),
                    item.ctr(),
                    item.position(),
                    item.suggestedTitlePattern(),
                    item.action());
        }
        return csv.toString();
    }

    public String exportQueryRewriteQueueCsv() {
        SeoScorecardReport report = buildReport();
        StringBuilder csv = new StringBuilder();
        csv.append("priority,query,intent,target_path,target_label,bottleneck,clicks,impressions,ctr,position,suggested_title_pattern,suggested_h1_pattern,suggested_meta_pattern,action\n");
        for (SeoQueryRewriteQueueItem item : report.queryRewriteQueue()) {
            appendCsvRow(csv,
                    item.priority(),
                    item.query(),
                    item.intent(),
                    item.targetPath(),
                    item.targetLabel(),
                    item.bottleneck(),
                    String.valueOf(item.clicks()),
                    String.valueOf(item.impressions()),
                    item.ctr(),
                    item.position(),
                    item.suggestedTitlePattern(),
                    item.suggestedH1Pattern(),
                    item.suggestedMetaPattern(),
                    item.action());
        }
        return csv.toString();
    }

    public String exportDeepFactQueueCsv() {
        SeoScorecardReport report = buildReport();
        StringBuilder csv = new StringBuilder();
        csv.append("priority,utility_id,utility_name,state,path,fact_score,missing_facts,next_action\n");
        for (SeoDeepFactQueueItem item : report.deepFactQueue()) {
            appendCsvRow(csv,
                    item.priority(),
                    item.utilityId(),
                    item.utilityName(),
                    item.state(),
                    item.path(),
                    item.factScore(),
                    String.join("; ", item.missingFacts()),
                    item.nextAction());
        }
        return csv.toString();
    }

    private List<SeoRewriteQueueItem> rewriteQueue(SearchConsoleSnapshot searchConsole) {
        if (searchConsole == null || !searchConsole.available()) {
            return List.of();
        }
        return searchConsole.topBottlenecks().stream()
                .map(metric -> new SeoRewriteQueueItem(
                        metric.rewritePriorityLabel(),
                        metric.path(),
                        metric.bottleneckLabel(),
                        metric.clicks(),
                        metric.impressions(),
                        metric.displayCtr(),
                        metric.displayPosition(),
                        metric.suggestedTitlePattern(),
                        metric.suggestedRewriteAction()
                ))
                .toList();
    }

    private List<SeoQueryRewriteQueueItem> queryRewriteQueue(SearchConsoleQuerySnapshot searchConsoleQueries) {
        if (searchConsoleQueries == null || !searchConsoleQueries.available()) {
            return List.of();
        }
        return searchConsoleQueries.topBottlenecks().stream()
                .map(metric -> {
                    QueryTarget target = queryTarget(metric);
                    return new SeoQueryRewriteQueueItem(
                        metric.priorityLabel(),
                        metric.query(),
                        metric.intentFamily(),
                        target.path(),
                        target.label(),
                        metric.bottleneckLabel(),
                        metric.clicks(),
                        metric.impressions(),
                        metric.displayCtr(),
                        metric.displayPosition(),
                        metric.suggestedTitlePattern(),
                        metric.suggestedH1Pattern(),
                        metric.suggestedMetaPattern(),
                        metric.action()
                    );
                })
                .toList();
    }

    private QueryTarget queryTarget(SearchConsoleQueryMetric metric) {
        String normalizedQuery = metric.normalizedQuery() == null ? "" : metric.normalizedQuery();
        String intentSlug = intentSlug(metric.intentFamily());
        CityAliasRecord matchedAlias = registryService.listCityAliases().stream()
                .filter(alias -> alias.aliasMode() != owner.backflow.data.model.AliasMode.NOINDEX_BRIDGE)
                .filter(alias -> queryMentionsAlias(normalizedQuery, alias))
                .findFirst()
                .orElse(null);
        if (matchedAlias != null) {
            UtilityRecord utility = registryService.findUtilityById(matchedAlias.utilityId()).orElse(null);
            if (utility != null && intentSlug != null && utility.supportsCityIntent(intentSlug)) {
                return new QueryTarget(cityIntentPath(matchedAlias, intentSlug), matchedAlias.city() + " " + readableIntent(metric.intentFamily()));
            }
            return new QueryTarget(cityPath(matchedAlias), matchedAlias.city() + " backflow testing");
        }

        UtilityRecord matchedUtility = registryService.listPublishedUtilities().stream()
                .filter(utility -> queryMentionsUtility(normalizedQuery, utility))
                .findFirst()
                .orElse(null);
        if (matchedUtility != null) {
            if ("failed-test".equals(metric.intentFamily()) && matchedUtility.supportsFailedTestPage()) {
                return new QueryTarget(utilityPath(matchedUtility) + "failed-test", matchedUtility.utilityName() + " failed-test page");
            }
            if ("approved-testers".equals(metric.intentFamily()) && matchedUtility.supportsApprovedTestersPage()) {
                return new QueryTarget(utilityPath(matchedUtility) + "approved-testers", matchedUtility.utilityName() + " approved testers page");
            }
            if ("annual-notice".equals(metric.intentFamily()) && matchedUtility.supportsAnnualTestingPage()) {
                return new QueryTarget(utilityPath(matchedUtility) + "annual-testing", matchedUtility.utilityName() + " annual testing page");
            }
            return new QueryTarget(utilityPath(matchedUtility), matchedUtility.utilityName() + " utility page");
        }

        if ("portal".equals(metric.intentFamily())) {
            return new QueryTarget("/backflow-reporting-portals", "Reporting portal hub");
        }
        if ("submit-report".equals(metric.intentFamily())) {
            return new QueryTarget("/submit-backflow-report", "Submit report hub");
        }
        if ("approved-testers".equals(metric.intentFamily())) {
            return new QueryTarget("/official-backflow-tester-lists", "Official tester list hub");
        }
        if ("cost-fee".equals(metric.intentFamily())) {
            return new QueryTarget("/guides/backflow-test-cost", "Backflow test cost guide");
        }
        return new QueryTarget("/notice-finder", "Notice finder");
    }

    private boolean queryMentionsAlias(String normalizedQuery, CityAliasRecord alias) {
        return containsAny(normalizedQuery, alias.city(), alias.aliasSlug());
    }

    private boolean queryMentionsUtility(String normalizedQuery, UtilityRecord utility) {
        return containsAny(normalizedQuery, utility.utilityName(), utility.canonicalSlug(), utility.utilityId())
                || utility.searchAliases().stream().anyMatch(alias -> containsAny(normalizedQuery, alias));
    }

    private String cityPath(CityAliasRecord alias) {
        return "/cities/" + alias.state() + "/" + alias.aliasSlug() + "/backflow-testing";
    }

    private String cityIntentPath(CityAliasRecord alias, String intentSlug) {
        return "/cities/" + alias.state() + "/" + alias.aliasSlug() + "/" + intentSlug;
    }

    private String intentSlug(String intentFamily) {
        return switch (intentFamily) {
            case "failed-test" -> "failed-backflow-test";
            case "submit-report" -> "submit-backflow-report";
            case "approved-testers" -> "approved-backflow-testers";
            case "portal" -> "backflow-reporting-portal";
            case "annual-notice" -> "annual-backflow-testing";
            case "irrigation" -> "irrigation-backflow-testing";
            case "fire-line" -> "fire-line-backflow-testing";
            default -> null;
        };
    }

    private String readableIntent(String intentFamily) {
        return switch (intentFamily) {
            case "failed-test" -> "failed-test route";
            case "submit-report" -> "submit report route";
            case "approved-testers" -> "approved tester route";
            case "portal" -> "portal route";
            case "annual-notice" -> "annual notice route";
            case "irrigation" -> "irrigation route";
            case "fire-line" -> "fire-line route";
            default -> "backflow route";
        };
    }

    private List<SeoDeepFactQueueItem> deepFactQueue(List<UtilityRecord> utilities) {
        return utilities.stream()
                .map(this::deepFactQueueItem)
                .filter(item -> !item.missingFacts().isEmpty())
                .sorted(Comparator.comparingInt(SeoDeepFactQueueItem::opportunityScore).reversed()
                        .thenComparing(Comparator.comparingInt(item -> Integer.parseInt(item.factScore())))
                        .thenComparing(SeoDeepFactQueueItem::utilityName))
                .limit(40)
                .toList();
    }

    private SeoDeepFactQueueItem deepFactQueueItem(UtilityRecord utility) {
        List<String> missingFacts = new ArrayList<>();
        int factScore = 0;
        if (utility.hasReportWorkflow()) {
            factScore++;
        } else {
            missingFacts.add("reportWorkflow");
        }
        if (utility.hasTesterGate()) {
            factScore++;
        } else {
            missingFacts.add("testerGate");
        }
        if (utility.hasDeadlinePolicy()) {
            factScore++;
        } else {
            missingFacts.add("deadlinePolicy");
        }
        if (utility.hasFailedTestPolicy()) {
            factScore++;
        } else {
            missingFacts.add("failedTestPolicy");
        }
        if (hasStructuredFees(utility)) {
            factScore++;
        } else {
            missingFacts.add("structuredFees");
        }
        if (!isBlank(utility.reportWorkflow().portalVendor()) || !isBlank(utility.reportWorkflow().portalName())) {
            factScore++;
        } else {
            missingFacts.add("portalVendorOrName");
        }
        if (!utility.reportWorkflow().requiredIdentifiers().isEmpty()) {
            factScore++;
        } else {
            missingFacts.add("noticeOrDeviceIdentifiers");
        }
        if (!isBlank(utility.reportWorkflow().acceptanceProof())) {
            factScore++;
        } else {
            missingFacts.add("acceptanceProof");
        }

        int opportunityScore = (missingFacts.size() * 10)
                + Math.min(utility.indexQualityScore(), 20)
                + (utility.supportsCityIntent("submit-backflow-report") ? 8 : 0)
                + (utility.supportsCityIntent("backflow-reporting-portal") ? 8 : 0)
                + (utility.supportsApprovedTestersPage() ? 5 : 0);

        return new SeoDeepFactQueueItem(
                deepFactPriority(missingFacts.size()),
                utility.utilityId(),
                utility.utilityName(),
                utility.state(),
                utilityPath(utility),
                String.valueOf(factScore),
                opportunityScore,
                missingFacts,
                deepFactNextAction(missingFacts)
        );
    }

    private String deepFactPriority(int missingFactCount) {
        if (missingFactCount >= 6) {
            return "P0";
        }
        if (missingFactCount >= 4) {
            return "P1";
        }
        return "P2";
    }

    private String deepFactNextAction(List<String> missingFacts) {
        if (missingFacts.contains("reportWorkflow") || missingFacts.contains("portalVendorOrName")) {
            return "Verify portal vendor/name, submitter, required IDs, deadline, acceptance proof, and sourceRefs.";
        }
        if (missingFacts.contains("testerGate")) {
            return "Verify approved tester route, credential, registration, gauge calibration, portal enrollment, and sourceRefs.";
        }
        if (missingFacts.contains("failedTestPolicy")) {
            return "Verify repair/retest deadline, failed-report deadline, shutoff risk, penalty, and sourceRefs.";
        }
        if (missingFacts.contains("structuredFees")) {
            return "Convert filing, portal, late, fine, or penalty amounts into structured fee fields with sourceRefs.";
        }
        return "Fill the remaining answer-card fact slots with official source evidence.";
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
            if (!isBlank(keyword) && lowered.contains(keyword.toLowerCase(Locale.US))) {
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

    private static void appendCsvRow(StringBuilder csv, String... values) {
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                csv.append(',');
            }
            csv.append(csvCell(values[index]));
        }
        csv.append('\n');
    }

    private static String csvCell(String value) {
        String safeValue = value == null ? "" : value;
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }

    public record SeoScorecardReport(
            SeoScorecardSummary summary,
            List<SeoScorecardItem> strongestRoutes,
            List<SeoScorecardItem> improvementCandidates,
            SearchConsoleSnapshot searchConsole,
            SearchConsoleQuerySnapshot searchConsoleQueries,
            List<SeoRewriteQueueItem> rewriteQueue,
            List<SeoQueryRewriteQueueItem> queryRewriteQueue,
            List<SeoDeepFactQueueItem> deepFactQueue
    ) {
        public SeoScorecardReport {
            strongestRoutes = strongestRoutes == null ? List.of() : List.copyOf(strongestRoutes);
            improvementCandidates = improvementCandidates == null ? List.of() : List.copyOf(improvementCandidates);
            searchConsole = searchConsole == null ? SearchConsoleSnapshot.missing("") : searchConsole;
            searchConsoleQueries = searchConsoleQueries == null ? SearchConsoleQuerySnapshot.missing("") : searchConsoleQueries;
            rewriteQueue = rewriteQueue == null ? List.of() : List.copyOf(rewriteQueue);
            queryRewriteQueue = queryRewriteQueue == null ? List.of() : List.copyOf(queryRewriteQueue);
            deepFactQueue = deepFactQueue == null ? List.of() : List.copyOf(deepFactQueue);
        }
    }

    public record SeoRewriteQueueItem(
            String priority,
            String path,
            String bottleneck,
            int clicks,
            int impressions,
            String ctr,
            String position,
            String suggestedTitlePattern,
            String action
    ) {
    }

    public record SeoQueryRewriteQueueItem(
            String priority,
            String query,
            String intent,
            String targetPath,
            String targetLabel,
            String bottleneck,
            int clicks,
            int impressions,
            String ctr,
            String position,
            String suggestedTitlePattern,
            String suggestedH1Pattern,
            String suggestedMetaPattern,
            String action
    ) {
    }

    private record QueryTarget(
            String path,
            String label
    ) {
    }

    public record SeoDeepFactQueueItem(
            String priority,
            String utilityId,
            String utilityName,
            String state,
            String path,
            String factScore,
            int opportunityScore,
            List<String> missingFacts,
            String nextAction
    ) {
        public SeoDeepFactQueueItem {
            missingFacts = missingFacts == null ? List.of() : List.copyOf(missingFacts);
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
