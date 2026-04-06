package owner.backflow.ops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
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

        List<VerificationFinding> findings = sourceEvidenceService.findings(
                registryService.listAllUtilities(),
                registryService.listAllGuides(),
                registryService.listAllStateGuides()
        );

        VerificationReport report = new VerificationReport(
                LocalDate.now(),
                status(findings),
                normalize(reviewerInitials),
                normalize(note),
                freshnessReport.summary(),
                new VerificationSummary(
                        registryService.listPublishedUtilities().size(),
                        registryService.listPublishedGuides().size(),
                        (int) registryService.listAllStateGuides().stream()
                                .filter(guide -> registryService.findPublishedStateGuide(guide.state()).isPresent())
                                .count(),
                        count(findings, "error"),
                        count(findings, "warning")
                ),
                findings
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

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
