package owner.backflow.ops;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import owner.backflow.config.AppDataProperties;
import owner.backflow.config.AppLeadsProperties;
import owner.backflow.config.AppOpsProperties;
import owner.backflow.files.BackflowRegistryService;
import owner.backflow.service.ProviderCommercialStateRepository;

class VerificationWorkflowServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void verificationWorkflowReloadsDataAndWritesReportArtifacts() throws Exception {
        Path tempWorkspaceRoot = prepareWorkspace();
        Path tempDataRoot = tempWorkspaceRoot.resolve("data");
        AppDataProperties dataProperties = new AppDataProperties(tempDataRoot.toString());
        AppOpsProperties opsProperties = new AppOpsProperties(
                tempDir.resolve("build").resolve("ops").resolve("freshness_report.json").toString(),
                "0 15 3 * * *",
                false,
                tempDir.resolve("build").resolve("ops").resolve("verification_report.json").toString(),
                true,
                "",
                7
        );

        ChangeLogService changeLogService = new ChangeLogService(dataProperties);
        OpsIssueService opsIssueService = new OpsIssueService(dataProperties, opsProperties);
        opsIssueService.reload();
        SourceEvidenceService sourceEvidenceService = new SourceEvidenceService(dataProperties);
        ProviderCommercialStateRepository providerCommercialStateRepository = new ProviderCommercialStateRepository(
                new AppLeadsProperties(tempWorkspaceRoot.resolve("storage").resolve("leads").toString(), 600, 3)
        );

        BackflowRegistryService registryService = new BackflowRegistryService(
                dataProperties,
                opsIssueService,
                sourceEvidenceService,
                providerCommercialStateRepository
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

        VerificationReport report = verificationWorkflowService.run("TL", "manual verification");

        Assertions.assertEquals("ok", report.status());
        Assertions.assertTrue(report.summary().publishedUtilityCount() >= 13);
        Assertions.assertTrue(report.summary().publishedGuideCount() >= 6);
        Assertions.assertTrue(report.summary().publishedStateGuideCount() >= 1);
        Assertions.assertEquals(0, report.summary().errorCount());
        Assertions.assertEquals(0, report.summary().warningCount());
        Assertions.assertTrue(report.findings().isEmpty());
        Assertions.assertTrue(Files.exists(Path.of(opsProperties.freshnessReportPath())));
        Assertions.assertTrue(Files.exists(Path.of(opsProperties.verificationReportPath())));
        Assertions.assertTrue(
                Files.readString(tempDataRoot.resolve("ops").resolve("change_log.jsonl")).contains("\"action\":\"verification_run\"")
        );
        Assertions.assertTrue(verificationWorkflowService.latestReport().isPresent());
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
}
