package owner.backflow.ops;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import owner.backflow.data.model.CostBand;
import owner.backflow.data.model.DeadlinePolicy;
import owner.backflow.data.model.FailedTestPolicy;
import owner.backflow.config.AppDataProperties;
import owner.backflow.data.model.ReportWorkflow;
import owner.backflow.data.model.TesterGate;
import owner.backflow.data.model.GuideRecord;
import owner.backflow.data.model.PageStatus;
import owner.backflow.data.model.StateGuideRecord;
import owner.backflow.data.model.UtilityRecord;
import org.springframework.stereotype.Service;

@Service
public class SourceEvidenceService {
    private final Path workspaceRoot;

    public SourceEvidenceService(AppDataProperties dataProperties) {
        Path dataRoot = Path.of(dataProperties.root()).toAbsolutePath().normalize();
        this.workspaceRoot = dataRoot.getParent() == null ? dataRoot : dataRoot.getParent();
    }

    public List<VerificationFinding> findings(
            List<UtilityRecord> utilities,
            List<GuideRecord> guides,
            List<StateGuideRecord> stateGuides
    ) {
        List<VerificationFinding> findings = new ArrayList<>();
        utilities.stream()
                .filter(utility -> utility.pageStatus() == PageStatus.PUBLISH)
                .forEach(utility -> findings.addAll(utilityFindings(utility)));
        guides.stream()
                .filter(GuideRecord::published)
                .forEach(guide -> findings.addAll(guideFindings(guide)));
        stateGuides.stream()
                .filter(StateGuideRecord::published)
                .forEach(guide -> findings.addAll(stateGuideFindings(guide)));
        return List.copyOf(findings);
    }

    public boolean hasBlockingIssue(UtilityRecord utility) {
        return utilityFindings(utility).stream().anyMatch(this::isError);
    }

    public boolean hasBlockingIssue(GuideRecord guide) {
        return guideFindings(guide).stream().anyMatch(this::isError);
    }

    public boolean hasBlockingIssue(StateGuideRecord guide) {
        return stateGuideFindings(guide).stream().anyMatch(this::isError);
    }

    private List<VerificationFinding> utilityFindings(UtilityRecord utility) {
        List<VerificationFinding> findings = new ArrayList<>();
        if (isBlank(utility.reviewerInitials())) {
            findings.add(error("utility", utility.utilityId(), "missing_reviewer", "Published utility is missing reviewer initials."));
        }
        if (utility.sources().isEmpty()) {
            findings.add(error("utility", utility.utilityId(), "missing_sources", "Published utility is missing official source links."));
        }
        if (isBlank(utility.sourceExcerpt()) && isBlank(utility.sourceSnapshotPath())) {
            findings.add(error("utility", utility.utilityId(), "missing_source_evidence", "Published utility needs a source excerpt or snapshot path."));
        }
        if (!isBlank(utility.sourceSnapshotPath()) && !Files.exists(resolveSnapshotPath(utility.sourceSnapshotPath()))) {
            findings.add(error("utility", utility.utilityId(), "missing_snapshot_file", "Snapshot path does not exist on disk: " + utility.sourceSnapshotPath()));
        }
        requireFactSourceRefs(findings, utility.utilityId(), "report_workflow", utility.reportWorkflow());
        requireFactSourceRefs(findings, utility.utilityId(), "tester_gate", utility.testerGate());
        requireFactSourceRefs(findings, utility.utilityId(), "deadline_policy", utility.deadlinePolicy());
        requireFactSourceRefs(findings, utility.utilityId(), "failed_test_policy", utility.failedTestPolicy());
        requireCostSourceRefs(findings, utility);
        return findings;
    }

    private void requireFactSourceRefs(List<VerificationFinding> findings, String utilityId, String field, ReportWorkflow workflow) {
        if (workflow != null && workflow.hasContent() && workflow.sourceRefs().isEmpty()) {
            findings.add(error("utility", utilityId, "missing_" + field + "_sources", "Structured " + field + " facts need sourceRefs before publishing."));
        }
        if (workflow != null && workflow.filingFee() != null && workflow.filingFee().hasContent() && workflow.filingFee().sourceRefs().isEmpty()) {
            findings.add(error("utility", utilityId, "missing_report_workflow_fee_sources", "Report workflow filingFee needs sourceRefs before publishing."));
        }
    }

    private void requireFactSourceRefs(List<VerificationFinding> findings, String utilityId, String field, TesterGate testerGate) {
        if (testerGate != null && testerGate.hasContent() && testerGate.sourceRefs().isEmpty()) {
            findings.add(error("utility", utilityId, "missing_" + field + "_sources", "Structured " + field + " facts need sourceRefs before publishing."));
        }
    }

    private void requireFactSourceRefs(List<VerificationFinding> findings, String utilityId, String field, DeadlinePolicy deadlinePolicy) {
        if (deadlinePolicy != null && deadlinePolicy.hasContent() && deadlinePolicy.sourceRefs().isEmpty()) {
            findings.add(error("utility", utilityId, "missing_" + field + "_sources", "Structured " + field + " facts need sourceRefs before publishing."));
        }
    }

    private void requireFactSourceRefs(List<VerificationFinding> findings, String utilityId, String field, FailedTestPolicy failedTestPolicy) {
        if (failedTestPolicy != null && failedTestPolicy.hasContent() && failedTestPolicy.sourceRefs().isEmpty()) {
            findings.add(error("utility", utilityId, "missing_" + field + "_sources", "Structured " + field + " facts need sourceRefs before publishing."));
        }
        if (failedTestPolicy != null
                && failedTestPolicy.penaltyAmount() != null
                && failedTestPolicy.penaltyAmount().hasContent()
                && failedTestPolicy.penaltyAmount().sourceRefs().isEmpty()) {
            findings.add(error("utility", utilityId, "missing_failed_test_penalty_sources", "Failed-test penaltyAmount needs sourceRefs before publishing."));
        }
    }

    private void requireCostSourceRefs(List<VerificationFinding> findings, UtilityRecord utility) {
        CostBand costBand = utility.costBand();
        if (costBand == null || !costBand.hasStructuredFees()) {
            return;
        }
        if (costBand.sourceRefs().isEmpty()) {
            findings.add(error("utility", utility.utilityId(), "missing_structured_cost_sources", "Structured cost facts need sourceRefs before publishing."));
        }
        requireFeeSourceRefs(findings, utility.utilityId(), "filing_fee", costBand.filingFee());
        requireFeeSourceRefs(findings, utility.utilityId(), "portal_fee", costBand.portalFee());
        requireFeeSourceRefs(findings, utility.utilityId(), "late_fee", costBand.lateFee());
        requireFeeSourceRefs(findings, utility.utilityId(), "fine_min", costBand.fineMin());
        requireFeeSourceRefs(findings, utility.utilityId(), "fine_max", costBand.fineMax());
    }

    private void requireFeeSourceRefs(List<VerificationFinding> findings, String utilityId, String field, owner.backflow.data.model.FeeAmount fee) {
        if (fee != null && fee.hasContent() && fee.sourceRefs().isEmpty()) {
            findings.add(error("utility", utilityId, "missing_" + field + "_sources", "Structured " + field + " fact needs sourceRefs before publishing."));
        }
    }

    private List<VerificationFinding> guideFindings(GuideRecord guide) {
        List<VerificationFinding> findings = new ArrayList<>();
        if (isBlank(guide.reviewerInitials())) {
            findings.add(error("guide", guide.slug(), "missing_reviewer", "Published guide is missing reviewer initials."));
        }
        if (guide.lastReviewed() == null) {
            findings.add(error("guide", guide.slug(), "missing_review_date", "Published guide is missing lastReviewed."));
        }
        if (guide.sources().isEmpty()) {
            findings.add(error("guide", guide.slug(), "missing_sources", "Published guide is missing source links."));
        }
        if (isBlank(guide.sourceExcerpt()) && isBlank(guide.sourceSnapshotPath())) {
            findings.add(error("guide", guide.slug(), "missing_source_evidence", "Published guide needs a source excerpt or snapshot path."));
        }
        if (!isBlank(guide.sourceSnapshotPath()) && !Files.exists(resolveSnapshotPath(guide.sourceSnapshotPath()))) {
            findings.add(error("guide", guide.slug(), "missing_snapshot_file", "Snapshot path does not exist on disk: " + guide.sourceSnapshotPath()));
        }
        return findings;
    }

    private List<VerificationFinding> stateGuideFindings(StateGuideRecord guide) {
        List<VerificationFinding> findings = new ArrayList<>();
        if (isBlank(guide.reviewerInitials())) {
            findings.add(error("state-guide", guide.state(), "missing_reviewer", "Published state guide is missing reviewer initials."));
        }
        if (guide.lastVerified() == null) {
            findings.add(error("state-guide", guide.state(), "missing_review_date", "Published state guide is missing lastVerified."));
        }
        if (guide.sources().isEmpty()) {
            findings.add(error("state-guide", guide.state(), "missing_sources", "Published state guide is missing source links."));
        }
        if (isBlank(guide.sourceExcerpt()) && isBlank(guide.sourceSnapshotPath())) {
            findings.add(error("state-guide", guide.state(), "missing_source_evidence", "Published state guide needs a source excerpt or snapshot path."));
        }
        if (!isBlank(guide.sourceSnapshotPath()) && !Files.exists(resolveSnapshotPath(guide.sourceSnapshotPath()))) {
            findings.add(error("state-guide", guide.state(), "missing_snapshot_file", "Snapshot path does not exist on disk: " + guide.sourceSnapshotPath()));
        }
        return findings;
    }

    private Path resolveSnapshotPath(String snapshotPath) {
        Path path = Path.of(snapshotPath);
        if (path.isAbsolute()) {
            return path;
        }
        return workspaceRoot.resolve(path).normalize();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private VerificationFinding error(String pageType, String pageId, String code, String message) {
        return new VerificationFinding("error", pageType, pageId, code, message);
    }

    private boolean isError(VerificationFinding finding) {
        return "error".equals(finding.severity());
    }

    private VerificationFinding warning(String pageType, String pageId, String code, String message) {
        return new VerificationFinding("warning", pageType, pageId, code, message);
    }
}
