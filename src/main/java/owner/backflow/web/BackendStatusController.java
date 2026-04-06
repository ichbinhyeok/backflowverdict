package owner.backflow.web;

import java.time.LocalDate;
import java.util.Map;
import owner.backflow.files.BackflowRegistryService;
import owner.backflow.ops.FreshnessAuditReport;
import owner.backflow.ops.OpsIssueService;
import owner.backflow.ops.FreshnessAuditService;
import owner.backflow.ops.SourceEvidenceService;
import owner.backflow.ops.VerificationReport;
import owner.backflow.ops.VerificationWorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class BackendStatusController {
    private final BackflowRegistryService registryService;
    private final FreshnessAuditService freshnessAuditService;
    private final OpsIssueService opsIssueService;
    private final SourceEvidenceService sourceEvidenceService;
    private final VerificationWorkflowService verificationWorkflowService;

    public BackendStatusController(
            BackflowRegistryService registryService,
            FreshnessAuditService freshnessAuditService,
            OpsIssueService opsIssueService,
            SourceEvidenceService sourceEvidenceService,
            VerificationWorkflowService verificationWorkflowService
    ) {
        this.registryService = registryService;
        this.freshnessAuditService = freshnessAuditService;
        this.opsIssueService = opsIssueService;
        this.sourceEvidenceService = sourceEvidenceService;
        this.verificationWorkflowService = verificationWorkflowService;
    }

    @GetMapping("/healthz")
    public BackendHealthResponse healthz() {
        FreshnessAuditReport report = freshnessAuditService.buildReport();
        LocalDate today = LocalDate.now();

        int totalUtilityCount = registryService.listAllUtilities().size();
        int totalGuideCount = registryService.listAllGuides().size();
        int totalStateGuideCount = registryService.listAllStateGuides().size();

        return new BackendHealthResponse(
                "ok",
                report.generatedAt(),
                totalUtilityCount,
                registryService.listPublishedUtilities().size(),
                registryService.listAllUtilities().stream()
                        .filter(utility -> utility.isPublishable(today))
                        .filter(utility -> opsIssueService.hasBlockingIssue(utility, today) || sourceEvidenceService.hasBlockingIssue(utility))
                        .count(),
                totalGuideCount,
                registryService.listPublishedGuides().size(),
                registryService.listAllGuides().stream()
                        .filter(guide -> guide.isPublishable(today))
                        .filter(guide -> opsIssueService.hasBlockingIssue(guide, today) || sourceEvidenceService.hasBlockingIssue(guide))
                        .count(),
                totalStateGuideCount,
                (int) registryService.listAllStateGuides().stream()
                        .filter(guide -> registryService.findPublishedStateGuide(guide.state()).isPresent())
                        .count(),
                registryService.listAllStateGuides().stream()
                        .filter(guide -> guide.isPublishable(today))
                        .filter(guide -> opsIssueService.hasBlockingIssue(guide, today) || sourceEvidenceService.hasBlockingIssue(guide))
                        .count(),
                report.summary()
        );
    }

    @GetMapping("/readyz")
    public Map<String, Object> readyz() {
        BackendHealthResponse health = healthz();
        return Map.of(
                "status", health.publishedUtilityCount() > 0 ? "ready" : "degraded",
                "publishedUtilityCount", health.publishedUtilityCount(),
                "blockedUtilityCount", health.blockedUtilityCount(),
                "staleUtilityCount", health.freshness().staleUtilityCount()
        );
    }

    @PostMapping("/ops/verification/run")
    public VerificationReport runVerification(@RequestBody(required = false) VerificationRunRequest request) {
        VerificationRunRequest payload = request == null ? new VerificationRunRequest("", "") : request;
        return verificationWorkflowService.run(payload.reviewerInitials(), payload.note());
    }

    @GetMapping("/ops/verification/report")
    public VerificationReport latestVerificationReport() {
        return verificationWorkflowService.latestReport()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No verification report available yet."));
    }
}
