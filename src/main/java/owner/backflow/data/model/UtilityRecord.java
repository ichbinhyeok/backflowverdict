package owner.backflow.data.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public record UtilityRecord(
        String utilityId,
        String utilityName,
        String governingEntityType,
        String canonicalSlug,
        String state,
        List<String> serviceAreaCities,
        List<String> serviceAreaCounties,
        List<String> searchAliases,
        String utilityUrl,
        List<String> officialProgramUrls,
        String testingFrequency,
        String dueBasis,
        List<String> coveredPropertyTypes,
        List<String> coveredDeviceTypes,
        ApprovedTesterMode approvedTesterMode,
        String approvedTesterListUrl,
        String officialListLabel,
        List<SubmissionMethod> submissionMethods,
        String phone,
        String penalties,
        String sourceExcerpt,
        String sourceSnapshotPath,
        String reviewerInitials,
        Integer staleAfterDays,
        PageStatus pageStatus,
        LocalDate lastVerified,
        String verdictSummary,
        String whoIsAffected,
        List<String> residentialNotes,
        List<String> commercialNotes,
        UtilityFocusContent annualTesting,
        UtilityFocusContent irrigation,
        UtilityFocusContent fireLine,
        List<String> workflowSteps,
        List<String> failureHighlights,
        CostBand costBand,
        List<SourceLink> sources,
        ReportWorkflow reportWorkflow,
        TesterGate testerGate,
        DeadlinePolicy deadlinePolicy,
        FailedTestPolicy failedTestPolicy
) {
    private static final int INDEX_QUALITY_FLOOR = 7;
    private static final int CITY_INTENT_EVIDENCE_FLOOR = 3;

    public UtilityRecord {
        serviceAreaCities = defaultList(serviceAreaCities);
        serviceAreaCounties = defaultList(serviceAreaCounties);
        searchAliases = defaultList(searchAliases);
        officialProgramUrls = defaultList(officialProgramUrls);
        coveredPropertyTypes = defaultList(coveredPropertyTypes);
        coveredDeviceTypes = defaultList(coveredDeviceTypes);
        submissionMethods = defaultList(submissionMethods);
        residentialNotes = defaultList(residentialNotes);
        commercialNotes = defaultList(commercialNotes);
        workflowSteps = defaultList(workflowSteps);
        failureHighlights = defaultList(failureHighlights);
        sources = defaultList(sources);
        reportWorkflow = reportWorkflow == null ? ReportWorkflow.empty() : reportWorkflow;
        testerGate = testerGate == null ? TesterGate.empty() : testerGate;
        deadlinePolicy = deadlinePolicy == null ? DeadlinePolicy.empty() : deadlinePolicy;
        failedTestPolicy = failedTestPolicy == null ? FailedTestPolicy.empty() : failedTestPolicy;
        approvedTesterMode = approvedTesterMode == null ? ApprovedTesterMode.NONE : approvedTesterMode;
        pageStatus = pageStatus == null ? PageStatus.HOLD : pageStatus;
        staleAfterDays = staleAfterDays == null ? 45 : staleAfterDays;
    }

    public boolean isPublishable(LocalDate today) {
        return pageStatus == PageStatus.PUBLISH;
    }

    public boolean isFresh(LocalDate today) {
        if (lastVerified == null) {
            return false;
        }
        long age = ChronoUnit.DAYS.between(lastVerified, today);
        return age <= staleAfterDays;
    }

    public boolean supportsApprovedTestersPage() {
        return approvedTesterMode == ApprovedTesterMode.OFFICIAL_LIST
                && approvedTesterListUrl != null
                && !approvedTesterListUrl.isBlank();
    }

    public boolean supportsFindATesterPage() {
        return approvedTesterMode == ApprovedTesterMode.DIRECTORY_ONLY;
    }

    public boolean supportsAnnualTestingPage() {
        return resolvedAnnualTesting().hasContent();
    }

    public boolean supportsIrrigationPage() {
        return irrigation != null && irrigation.hasContent();
    }

    public boolean supportsFireLinePage() {
        return fireLine != null && fireLine.hasContent();
    }

    public boolean supportsFailedTestPage() {
        return !failureHighlights.isEmpty() || failedTestPolicy.hasContent();
    }

    public boolean hasReportWorkflow() {
        return reportWorkflow.hasContent();
    }

    public boolean hasTesterGate() {
        return testerGate.hasContent();
    }

    public boolean hasDeadlinePolicy() {
        return deadlinePolicy.hasContent();
    }

    public boolean hasFailedTestPolicy() {
        return failedTestPolicy.hasContent();
    }

    public boolean meetsIndexQualityFloor() {
        return indexQualityScore() >= INDEX_QUALITY_FLOOR;
    }

    public int indexQualityScore() {
        int score = 0;
        score += scoreIf(pageStatus == PageStatus.PUBLISH);
        score += scoreIf(lastVerified != null);
        score += scoreIf(!sources.isEmpty());
        score += scoreIf(!isBlank(sourceExcerpt));
        score += scoreIf(!officialProgramUrls.isEmpty() || !isBlank(utilityUrl));
        score += scoreIf(!isBlank(testingFrequency));
        score += scoreIf(!isBlank(dueBasis));
        score += scoreIf(!coveredPropertyTypes.isEmpty());
        score += scoreIf(!coveredDeviceTypes.isEmpty());
        score += scoreIf(!workflowSteps.isEmpty());
        score += scoreIf(!submissionMethods.isEmpty());
        score += scoreIf(supportsAnnualTestingPage());
        score += scoreIf(supportsApprovedTestersPage() || supportsFindATesterPage());
        score += scoreIf(supportsIrrigationPage());
        score += scoreIf(supportsFireLinePage());
        score += scoreIf(supportsFailedTestPage());
        score += scoreIf(hasReportWorkflow());
        score += scoreIf(hasTesterGate());
        score += scoreIf(hasDeadlinePolicy());
        score += scoreIf(hasFailedTestPolicy());
        score += scoreIf(costBand != null && costBand.hasStructuredFees());
        return score;
    }

    public boolean supportsCityIntent(String intentSlug) {
        return cityIntentEvidenceScore(intentSlug) >= CITY_INTENT_EVIDENCE_FLOOR;
    }

    public int cityIntentEvidenceScore(String intentSlug) {
        String slug = intentSlug == null ? "" : intentSlug.toLowerCase(Locale.US);
        return switch (slug) {
            case "annual-backflow-testing" -> annualIntentEvidenceScore();
            case "backflow-reporting-portal" -> portalIntentEvidenceScore();
            case "submit-backflow-report" -> submitReportIntentEvidenceScore();
            case "approved-backflow-testers" -> approvedTesterIntentEvidenceScore();
            case "failed-backflow-test" -> failedTestIntentEvidenceScore();
            case "irrigation-backflow-testing" -> focusIntentEvidenceScore(irrigation);
            case "fire-line-backflow-testing" -> focusIntentEvidenceScore(fireLine);
            default -> 0;
        };
    }

    public UtilityFocusContent resolvedAnnualTesting() {
        if (annualTesting != null && annualTesting.hasContent()) {
            return annualTesting;
        }
        return new UtilityFocusContent(
                testingFrequency == null || testingFrequency.isBlank()
                        ? verdictSummary
                        : "Testing cadence for " + utilityName + ": " + testingFrequency + ".",
                List.of(
                        dueBasis == null ? "" : dueBasis,
                        whoIsAffected == null ? "" : whoIsAffected
                ).stream().filter(value -> value != null && !value.isBlank()).toList(),
                workflowSteps
        );
    }

    private int annualIntentEvidenceScore() {
        int score = focusIntentEvidenceScore(resolvedAnnualTesting());
        score += scoreIf(!isBlank(testingFrequency));
        score += scoreIf(!isBlank(dueBasis));
        score += scoreIf(deadlinePolicy.hasContent());
        score += scoreIf(!deadlinePolicy.cadenceByPropertyType().isEmpty());
        score += scoreIf(!deadlinePolicy.pastDueLadder().isEmpty());
        score += scoreIf(containsAny(evidenceText(), "annual", "due date", "anniversary", "calendar", "notice"));
        return score;
    }

    private int portalIntentEvidenceScore() {
        int score = 0;
        score += scoreIf(hasPortalWorkflowSignal());
        score += scoreIf(!submissionMethods.isEmpty());
        score += scoreIf(reportWorkflow.hasContent());
        score += scoreIf(!isBlank(reportWorkflow.portalVendor()) || !isBlank(reportWorkflow.portalName()));
        score += scoreIf(!isBlank(reportWorkflow.portalUrl()));
        score += scoreIf(!isBlank(dueBasis));
        score += scoreIf(!sources.isEmpty());
        return score;
    }

    private int submitReportIntentEvidenceScore() {
        int score = portalIntentEvidenceScore();
        score += scoreIf(!workflowSteps.isEmpty());
        score += scoreIf(hasTesterGate());
        score += scoreIf(!isBlank(reportWorkflow.submitter()));
        score += scoreIf(!reportWorkflow.requiredIdentifiers().isEmpty());
        score += scoreIf(!isBlank(reportWorkflow.acceptanceProof()));
        score += scoreIf(reportWorkflow.submissionDeadlineDaysAfterTest() != null
                || deadlinePolicy.reportDueDaysAfterTest() != null);
        return score;
    }

    private int approvedTesterIntentEvidenceScore() {
        int score = 0;
        score += scoreIf(supportsApprovedTestersPage());
        score += scoreIf(!isBlank(approvedTesterListUrl));
        score += scoreIf(!isBlank(officialListLabel));
        score += scoreIf(hasTesterGate());
        score += scoreIf(!testerGate.credentialDocuments().isEmpty());
        score += scoreIf(testerGate.licenseRequired() != null || testerGate.utilityRegistrationRequired() != null);
        score += scoreIf(!sources.isEmpty());
        return score;
    }

    private int failedTestIntentEvidenceScore() {
        int score = 0;
        score += scoreIf(supportsFailedTestPage());
        score += scoreIf(hasFailedTestPolicy());
        score += scoreIf(failedTestPolicy.retestRequired() != null);
        score += scoreIf(failedTestPolicy.repairDeadlineDays() != null);
        score += scoreIf(!failureHighlights.isEmpty());
        score += scoreIf(failureHighlights.size() >= 2);
        score += scoreIf(!workflowSteps.isEmpty());
        score += scoreIf(!isBlank(penalties));
        return score;
    }

    private int focusIntentEvidenceScore(UtilityFocusContent focus) {
        if (focus == null || !focus.hasContent()) {
            return 0;
        }
        int score = 0;
        score += scoreIf(!isBlank(focus.summary()));
        score += scoreIf(!focus.highlights().isEmpty());
        score += scoreIf(focus.highlights().size() >= 2);
        score += scoreIf(!focus.workflowSteps().isEmpty());
        score += scoreIf(focus.workflowSteps().size() >= 2);
        score += scoreIf(!sources.isEmpty());
        return score;
    }

    private boolean hasPortalWorkflowSignal() {
        return containsAny(
                evidenceText(),
                "portal",
                "online",
                "swiftcomply",
                "c3swift",
                "bsi",
                "backflowtest",
                "weirs",
                "vepo",
                "envirotrax",
                "aqua backflow",
                "aquabackflow",
                "trackmybackflow",
                "tokay",
                "webtest",
                "sprybackflow"
        );
    }

    private String evidenceText() {
        StringBuilder text = new StringBuilder();
        append(text, utilityUrl);
        append(text, approvedTesterListUrl);
        append(text, officialListLabel);
        append(text, testingFrequency);
        append(text, dueBasis);
        append(text, penalties);
        append(text, sourceExcerpt);
        append(text, verdictSummary);
        append(text, reportWorkflow.portalVendor());
        append(text, reportWorkflow.portalName());
        append(text, reportWorkflow.portalUrl());
        append(text, reportWorkflow.submitter());
        append(text, reportWorkflow.acceptanceProof());
        append(text, testerGate.deviceScopeLimit());
        coveredPropertyTypes.forEach(value -> append(text, value));
        coveredDeviceTypes.forEach(value -> append(text, value));
        submissionMethods.forEach(method -> {
            append(text, method.label());
            append(text, method.url());
            append(text, method.kind());
        });
        workflowSteps.forEach(value -> append(text, value));
        failureHighlights.forEach(value -> append(text, value));
        sources.forEach(source -> {
            append(text, source.label());
            append(text, source.url());
            append(text, source.kind());
        });
        return text.toString();
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

    private static int scoreIf(boolean condition) {
        return condition ? 1 : 0;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static <T> List<T> defaultList(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
