package owner.backflow.ops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import owner.backflow.config.AppOpsProperties;
import owner.backflow.files.BackflowRegistryService;
import org.springframework.stereotype.Service;

@Service
public class VerificationWorkflowService {
    private final BackflowRegistryService registryService;
    private final OpsIssueService opsIssueService;
    private final FreshnessAuditService freshnessAuditService;
    private final ChangeLogService changeLogService;
    private final SourceEvidenceService sourceEvidenceService;
    private final AppOpsProperties opsProperties;
    private final ObjectMapper objectMapper;

    public VerificationWorkflowService(
            BackflowRegistryService registryService,
            OpsIssueService opsIssueService,
            FreshnessAuditService freshnessAuditService,
            ChangeLogService changeLogService,
            SourceEvidenceService sourceEvidenceService,
            AppOpsProperties opsProperties
    ) {
        this.registryService = registryService;
        this.opsIssueService = opsIssueService;
        this.freshnessAuditService = freshnessAuditService;
        this.changeLogService = changeLogService;
        this.sourceEvidenceService = sourceEvidenceService;
        this.opsProperties = opsProperties;
        this.objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
    }

    public synchronized VerificationReport run(String reviewerInitials, String note) {
        opsIssueService.reload();
        registryService.reload();
        FreshnessAuditReport freshnessReport = freshnessAuditService.writeReport();

        List<VerificationFinding> findings = new ArrayList<>(sourceEvidenceService.findings(
                registryService.listAllUtilities(),
                registryService.listAllGuides(),
                registryService.listAllStateGuides()
        ));
        VerificationSummary summary = new VerificationSummary(
                registryService.listPublishedUtilities().size(),
                registryService.listPublishedGuides().size(),
                (int) registryService.listAllStateGuides().stream()
                        .filter(guide -> registryService.findPublishedStateGuide(guide.state()).isPresent())
                        .count(),
                0,
                0
        );
        findings.addAll(operationalFindings(freshnessReport.summary(), summary));

        VerificationReport report = new VerificationReport(
                freshnessReport.generatedAt(),
                status(findings),
                normalize(reviewerInitials),
                normalize(note),
                freshnessReport.summary(),
                new VerificationSummary(
                        summary.publishedUtilityCount(),
                        summary.publishedGuideCount(),
                        summary.publishedStateGuideCount(),
                        count(findings, "error"),
                        count(findings, "warning")
                ),
                List.copyOf(findings)
        );

        writeReport(report);
        changeLogService.appendVerificationRun(report);
        return report;
    }

    public Optional<VerificationReport> latestReport() {
        Path path = Path.of(opsProperties.verificationReportPath());
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(path.toFile(), VerificationReport.class));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read verification report from " + path, exception);
        }
    }

    private void writeReport(VerificationReport report) {
        Path path = Path.of(opsProperties.verificationReportPath());
        try {
            Files.createDirectories(path.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), report);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write verification report to " + path, exception);
        }
    }

    private int count(List<VerificationFinding> findings, String severity) {
        return (int) findings.stream().filter(finding -> severity.equals(finding.severity())).count();
    }

    private String status(List<VerificationFinding> findings) {
        if (findings.stream().anyMatch(finding -> "error".equals(finding.severity()))) {
            return "needs-review";
        }
        if (findings.stream().anyMatch(finding -> "warning".equals(finding.severity()))) {
            return "warning";
        }
        return "ok";
    }

    private List<VerificationFinding> operationalFindings(FreshnessAuditSummary freshness, VerificationSummary summary) {
        List<VerificationFinding> findings = new ArrayList<>();
        if (summary.publishedUtilityCount() == 0) {
            findings.add(error(
                    "ops",
                    "registry",
                    "no_published_utilities",
                    "No utility pages are currently publishable. Check stale utility counts, broken links, and source evidence."
            ));
        }
        if (summary.publishedStateGuideCount() == 0) {
            findings.add(error(
                    "ops",
                    "registry",
                    "no_published_state_guides",
                    "No state guide pages are currently publishable. Check state guide freshness and source evidence."
            ));
        }
        if (freshness.staleUtilityCount() > 0) {
            findings.add(warning(
                    "ops",
                    "freshness-report",
                    "stale_utilities",
                    freshness.staleUtilityCount() + " utility pages are stale and need re-verification."
            ));
        }
        if (freshness.staleGuideCount() > 0) {
            findings.add(warning(
                    "ops",
                    "freshness-report",
                    "stale_guides",
                    freshness.staleGuideCount() + " guide pages are stale and need re-verification."
            ));
        }
        if (freshness.staleStateGuideCount() > 0) {
            findings.add(warning(
                    "ops",
                    "freshness-report",
                    "stale_state_guides",
                    freshness.staleStateGuideCount() + " state guide pages are stale and need re-verification."
            ));
        }
        if (freshness.brokenLinkCount() > 0) {
            findings.add(warning(
                    "ops",
                    "freshness-report",
                    "broken_source_links",
                    freshness.brokenLinkCount() + " source links are tracked in ops/broken_links.csv."
            ));
        }
        if (freshness.conflictCount() > 0) {
            findings.add(error(
                    "ops",
                    "freshness-report",
                    "source_conflicts",
                    freshness.conflictCount() + " source conflicts are tracked in ops/conflicts.csv."
            ));
        }
        return findings;
    }

    private VerificationFinding error(String pageType, String pageId, String code, String message) {
        return new VerificationFinding("error", pageType, pageId, code, message);
    }

    private VerificationFinding warning(String pageType, String pageId, String code, String message) {
        return new VerificationFinding("warning", pageType, pageId, code, message);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
