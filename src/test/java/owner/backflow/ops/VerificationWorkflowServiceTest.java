package owner.backflow.ops;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import owner.backflow.config.AppDataProperties;
import owner.backflow.config.AppOpsProperties;
import owner.backflow.files.BackflowRegistryService;

class VerificationWorkflowServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void verificationWorkflowReloadsDataAndWritesReportArtifacts() throws Exception {
        WorkflowContext context = workflowContext("2026-06-29");

        VerificationReport report = context.verificationWorkflowService().run("TL", "manual verification");

        Assertions.assertEquals("ok", report.status());
        Assertions.assertTrue(report.summary().publishedUtilityCount() >= 13);
        Assertions.assertTrue(report.summary().publishedGuideCount() >= 6);
        Assertions.assertTrue(report.summary().publishedStateGuideCount() >= 1);
        Assertions.assertEquals(0, report.summary().errorCount());
        Assertions.assertEquals(0, report.summary().warningCount());
        Assertions.assertTrue(report.findings().stream()
                .noneMatch(finding -> "broken_source_links".equals(finding.code())));
        Assertions.assertTrue(Files.exists(Path.of(context.opsProperties().freshnessReportPath())));
        Assertions.assertTrue(Files.exists(Path.of(context.opsProperties().verificationReportPath())));
        Assertions.assertTrue(
                Files.readString(context.dataRoot().resolve("ops").resolve("change_log.jsonl")).contains("\"action\":\"verification_run\"")
        );
        Assertions.assertTrue(context.verificationWorkflowService().latestReport().isPresent());
    }

    @Test
    void staleRegistryStaysPublishedAndRequiresReverification() throws Exception {
        WorkflowContext context = workflowContext("2026-09-01");

        VerificationReport report = context.verificationWorkflowService().run("TL", "stale regression");

        Assertions.assertEquals("warning", report.status());
        Assertions.assertTrue(report.summary().publishedUtilityCount() >= 80);
        Assertions.assertEquals(0, report.summary().errorCount());
        Assertions.assertTrue(report.findings().stream()
                .anyMatch(finding -> "stale_utilities".equals(finding.code())));
        Assertions.assertTrue(report.findings().stream()
                .noneMatch(finding -> "no_published_utilities".equals(finding.code())));
    }

    private WorkflowContext workflowContext(String currentDate) throws IOException {
        Path tempWorkspaceRoot = prepareWorkspace();
        Path tempDataRoot = tempWorkspaceRoot.resolve("data");
        AppDataProperties dataProperties = new AppDataProperties(tempDataRoot.toString());
        AppOpsProperties opsProperties = new AppOpsProperties(
                tempDir.resolve("build").resolve("ops").resolve(currentDate).resolve("freshness_report.json").toString(),
                "0 15 3 * * *",
                false,
                tempDir.resolve("build").resolve("ops").resolve(currentDate).resolve("verification_report.json").toString(),
                true,
                "",
                7,
                currentDate,
                tempDir.resolve("storage").resolve("search-console").resolve("pages.csv").toString(),
                tempDir.resolve("storage").resolve("search-console").resolve("queries.csv").toString()
        );

        ChangeLogService changeLogService = new ChangeLogService(dataProperties);
        OpsIssueService opsIssueService = new OpsIssueService(dataProperties, opsProperties);
        opsIssueService.reload();
        SourceEvidenceService sourceEvidenceService = new SourceEvidenceService(dataProperties);
        BackflowRegistryService registryService = new BackflowRegistryService(
                dataProperties,
                opsIssueService,
                sourceEvidenceService,
                opsProperties
        );
        registryService.reload();

        FreshnessAuditService freshnessAuditService = new FreshnessAuditService(
                registryService,
                opsIssueService,
                changeLogService,
                opsProperties
        );
        VerificationWorkflowService verificationWorkflowService = new VerificationWorkflowService(
                registryService,
                opsIssueService,
                freshnessAuditService,
                changeLogService,
                sourceEvidenceService,
                opsProperties
        );
        return new WorkflowContext(tempDataRoot, opsProperties, verificationWorkflowService);
    }

    private Path prepareWorkspace() throws IOException {
        Path workspaceRoot = tempDir.resolve("workspace");
        copyTree(Path.of("data").toAbsolutePath().normalize(), workspaceRoot.resolve("data"));
        copyTree(Path.of("storage").toAbsolutePath().normalize(), workspaceRoot.resolve("storage"));
        return workspaceRoot;
    }

    private void copyTree(Path sourceRoot, Path targetRoot) throws IOException {
        try (var paths = Files.walk(sourceRoot)) {
            paths.forEach(path -> copyPath(sourceRoot, targetRoot, path));
        }
    }

    private void copyPath(Path sourceRoot, Path targetRoot, Path sourcePath) {
        Path relative = sourceRoot.relativize(sourcePath);
        Path targetPath = targetRoot.resolve(relative);
        try {
            if (Files.isDirectory(sourcePath)) {
                Files.createDirectories(targetPath);
            } else {
                Files.createDirectories(targetPath.getParent());
                Files.copy(sourcePath, targetPath);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to copy test data from " + sourcePath + " to " + targetPath, exception);
        }
    }

    private record WorkflowContext(
            Path dataRoot,
            AppOpsProperties opsProperties,
            VerificationWorkflowService verificationWorkflowService
    ) {
    }
}
