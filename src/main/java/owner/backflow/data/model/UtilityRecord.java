package owner.backflow.data.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
        List<SourceLink> sources
) {
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
        approvedTesterMode = approvedTesterMode == null ? ApprovedTesterMode.NONE : approvedTesterMode;
        pageStatus = pageStatus == null ? PageStatus.HOLD : pageStatus;
        staleAfterDays = staleAfterDays == null ? 45 : staleAfterDays;
    }

    public boolean isPublishable(LocalDate today) {
        return pageStatus == PageStatus.PUBLISH && isFresh(today);
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

    private static <T> List<T> defaultList(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
