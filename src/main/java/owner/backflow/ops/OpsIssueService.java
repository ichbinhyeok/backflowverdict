package owner.backflow.ops;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import owner.backflow.config.AppDataProperties;
import owner.backflow.config.AppOpsProperties;
import owner.backflow.data.model.GuideRecord;
import owner.backflow.data.model.SourceLink;
import owner.backflow.data.model.StateGuideRecord;
import owner.backflow.data.model.UtilityRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpsIssueService {
    private static final Set<String> RESOLVED_STATUSES = Set.of("ok", "resolved", "fixed", "200", "301", "302");

    private final AppDataProperties dataProperties;
    private final AppOpsProperties opsProperties;
    private final CsvMapper csvMapper;

    private List<OpsCsvEntry> brokenLinks = List.of();
    private List<OpsCsvEntry> conflicts = List.of();

    @Autowired
    public OpsIssueService(AppDataProperties dataProperties, AppOpsProperties opsProperties) {
        this.dataProperties = dataProperties;
        this.opsProperties = opsProperties;
        this.csvMapper = CsvMapper.builder().findAndAddModules().build();
    }

    static OpsIssueService forTest(List<OpsCsvEntry> brokenLinks, List<OpsCsvEntry> conflicts, int brokenLinkSuppressionDays) {
        OpsIssueService service = new OpsIssueService(
                new AppDataProperties(""),
                new AppOpsProperties("", "", false, "", true, "", brokenLinkSuppressionDays)
        );
        service.brokenLinks = List.copyOf(brokenLinks);
        service.conflicts = List.copyOf(conflicts);
        return service;
    }

    @PostConstruct
    void initialize() {
        reload();
    }

    public synchronized void reload() {
        Path root = Path.of(dataProperties.root()).resolve("ops");
        this.brokenLinks = readOpsCsv(root.resolve("broken_links.csv"));
        this.conflicts = readOpsCsv(root.resolve("conflicts.csv"));
    }

    public List<OpsCsvEntry> brokenLinks() {
        return brokenLinks;
    }

    public List<OpsCsvEntry> conflicts() {
        return conflicts;
    }

    public boolean hasBlockingIssue(UtilityRecord utility, LocalDate today) {
        return hasBlockingConflict(utility.utilityId()) || hasBlockingBrokenLink(utilityUrls(utility), today);
    }

    public boolean hasBlockingIssue(GuideRecord guide, LocalDate today) {
        return hasBlockingBrokenLink(sourceUrls(guide.sources()), today);
    }

    public boolean hasBlockingIssue(StateGuideRecord guide, LocalDate today) {
        return hasBlockingBrokenLink(sourceUrls(guide.sources()), today);
    }

    public long blockingUtilityCount(List<UtilityRecord> utilities, LocalDate today) {
        return utilities.stream().filter(utility -> hasBlockingIssue(utility, today)).count();
    }

    public long blockingGuideCount(List<GuideRecord> guides, LocalDate today) {
        return guides.stream().filter(guide -> hasBlockingIssue(guide, today)).count();
    }

    public long blockingStateGuideCount(List<StateGuideRecord> stateGuides, LocalDate today) {
        return stateGuides.stream().filter(guide -> hasBlockingIssue(guide, today)).count();
    }

    boolean hasBlockingConflict(String utilityId) {
        return conflicts.stream()
                .map(entry -> entry.value("utilityId"))
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toLowerCase(Locale.US))
                .anyMatch(value -> value.equals(utilityId.trim().toLowerCase(Locale.US)));
    }

    boolean hasBlockingBrokenLink(List<String> urls, LocalDate today) {
        if (urls.isEmpty()) {
            return false;
        }
        Set<String> normalizedUrls = urls.stream()
                .filter(Objects::nonNull)
                .map(this::normalizeUrl)
                .filter(url -> !url.isBlank())
                .collect(java.util.stream.Collectors.toSet());

        return brokenLinks.stream().anyMatch(entry -> {
            String status = entry.value("status").trim().toLowerCase(Locale.US);
            if (RESOLVED_STATUSES.contains(status)) {
                return false;
            }
            String normalizedBrokenUrl = normalizeUrl(entry.value("url"));
            if (!normalizedUrls.contains(normalizedBrokenUrl)) {
                return false;
            }
            LocalDate checkedAt = entry.localDate("checkedAt").orElse(today);
            long age = ChronoUnit.DAYS.between(checkedAt, today);
            return age >= opsProperties.brokenLinkSuppressionDays();
        });
    }

    private List<String> utilityUrls(UtilityRecord utility) {
        return Stream.concat(
                        Stream.concat(
                                Stream.of(utility.utilityUrl()),
                                utility.officialProgramUrls().stream()
                        ),
                        utility.sources().stream().map(SourceLink::url)
                )
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> sourceUrls(List<SourceLink> sources) {
        return sources.stream()
                .map(SourceLink::url)
                .filter(Objects::nonNull)
                .toList();
    }

    private String normalizeUrl(String url) {
        if (url == null) {
            return "";
        }
        String normalized = url.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.toLowerCase(Locale.US);
    }

    private List<OpsCsvEntry> readOpsCsv(Path path) {
        if (!Files.exists(path)) {
            return List.of();
        }
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        try (Reader reader = Files.newBufferedReader(path)) {
            return csvMapper.readerFor(Map.class)
                    .with(schema)
                    .<Map<String, String>>readValues(reader)
                    .readAll()
                    .stream()
                    .map(row -> new OpsCsvEntry(new LinkedHashMap<>(row)))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read ops CSV " + path, exception);
        }
    }
}
