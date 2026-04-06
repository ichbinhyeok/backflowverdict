package owner.backflow.ops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import owner.backflow.config.AppOpsProperties;
import owner.backflow.files.BackflowRegistryService;
import org.springframework.stereotype.Service;

@Service
public class FreshnessAuditService {
    private final BackflowRegistryService registryService;
    private final OpsIssueService opsIssueService;
    private final ChangeLogService changeLogService;
    private final AppOpsProperties opsProperties;
    private final ObjectMapper objectMapper;

    public FreshnessAuditService(
            BackflowRegistryService registryService,
            OpsIssueService opsIssueService,
            ChangeLogService changeLogService,
            AppOpsProperties opsProperties
    ) {
        this.registryService = registryService;
        this.opsIssueService = opsIssueService;
        this.changeLogService = changeLogService;
        this.opsProperties = opsProperties;
        this.objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
    }

    @PostConstruct
    void initializeReport() {
        if (opsProperties.writeFreshnessReportOnStartup()) {
            writeReport();
        }
    }

    public synchronized FreshnessAuditReport buildReport() {
        LocalDate today = LocalDate.now();
        List<FreshnessAuditEntry> staleUtilities = registryService.listAllUtilities().stream()
                .filter(utility -> !utility.isFresh(today))
                .map(utility -> new FreshnessAuditEntry(
                        "utility",
                        utility.utilityId(),
                        utility.utilityName(),
                        "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/",
                        utility.lastVerified(),
                        utility.staleAfterDays(),
                        ageDays(utility.lastVerified(), today)
                ))
                .toList();
        List<FreshnessAuditEntry> staleGuides = registryService.listAllGuides().stream()
                .filter(guide -> !guide.isFresh(today))
                .map(guide -> new FreshnessAuditEntry(
                        "guide",
                        guide.slug(),
                        guide.title(),
                        "/guides/" + guide.slug(),
                        guide.lastReviewed(),
                        guide.staleAfterDays(),
                        ageDays(guide.lastReviewed(), today)
                ))
                .toList();
        List<FreshnessAuditEntry> staleStateGuides = registryService.listAllStateGuides().stream()
                .filter(guide -> !guide.isFresh(today))
                .map(guide -> new FreshnessAuditEntry(
                        "state-guide",
                        guide.state(),
                        guide.title(),
                        "/states/" + guide.state() + "/backflow-testing",
                        guide.lastVerified(),
                        guide.staleAfterDays(),
                        ageDays(guide.lastVerified(), today)
                ))
                .toList();

        return new FreshnessAuditReport(
                today,
                new FreshnessAuditSummary(
                        registryService.listAllUtilities().size(),
                        staleUtilities.size(),
                        registryService.listAllGuides().size(),
                        staleGuides.size(),
                        registryService.listAllStateGuides().size(),
                        staleStateGuides.size(),
                        opsIssueService.brokenLinks().size(),
                        opsIssueService.conflicts().size()
                ),
                staleUtilities,
                staleGuides,
                staleStateGuides,
                opsIssueService.brokenLinks(),
                opsIssueService.conflicts()
        );
    }

    public synchronized FreshnessAuditReport writeReport() {
        FreshnessAuditReport report = buildReport();
        Path output = Path.of(opsProperties.freshnessReportPath());
        try {
            Files.createDirectories(output.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(output.toFile(), report);
            changeLogService.appendFreshnessAudit(report);
            return report;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write freshness report to " + output, exception);
        }
    }

    private long ageDays(LocalDate lastVerified, LocalDate today) {
        if (lastVerified == null) {
            return -1;
        }
        return ChronoUnit.DAYS.between(lastVerified, today);
    }

}
