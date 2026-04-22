package owner.backflow.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import owner.backflow.config.AppDataProperties;
import owner.backflow.data.model.AliasMode;
import owner.backflow.data.model.CityAliasCsvRow;
import owner.backflow.data.model.CityAliasRecord;
import owner.backflow.data.model.GuideRecord;
import owner.backflow.data.model.MetroRecord;
import owner.backflow.data.model.ProviderListingStatus;
import owner.backflow.data.model.ProviderCsvRow;
import owner.backflow.data.model.ProviderRecord;
import owner.backflow.data.model.StateGuideRecord;
import owner.backflow.data.model.UtilityRecord;
import owner.backflow.ops.OpsIssueService;
import owner.backflow.ops.SourceEvidenceService;
import org.springframework.stereotype.Service;

@Service
public class BackflowRegistryService {
    private static final Comparator<ProviderRecord> PROVIDER_SORT = Comparator
            .comparing(ProviderRecord::coverageSize, Comparator.reverseOrder())
            .thenComparing(ProviderRecord::providerName);

    private final AppDataProperties dataProperties;
    private final OpsIssueService opsIssueService;
    private final SourceEvidenceService sourceEvidenceService;
    private final ObjectMapper objectMapper;
    private final CsvMapper csvMapper;

    private Map<String, UtilityRecord> utilitiesByKey = Map.of();
    private Map<String, UtilityRecord> utilitiesById = Map.of();
    private Map<String, CityAliasRecord> aliasesByKey = Map.of();
    private Map<String, StateGuideRecord> stateGuidesByState = Map.of();
    private Map<String, GuideRecord> guidesBySlug = Map.of();
    private Map<String, MetroRecord> metrosByKey = Map.of();
    private List<ProviderRecord> providers = List.of();

    public BackflowRegistryService(
            AppDataProperties dataProperties,
            OpsIssueService opsIssueService,
            SourceEvidenceService sourceEvidenceService
    ) {
        this.dataProperties = dataProperties;
        this.opsIssueService = opsIssueService;
        this.sourceEvidenceService = sourceEvidenceService;
        this.objectMapper = JsonMapper.builder().findAndAddModules().build();
        this.csvMapper = CsvMapper.builder().findAndAddModules().build();
    }

    @PostConstruct
    void load() {
        reload();
    }

    public synchronized void reload() {
        Path root = Path.of(dataProperties.root());
        this.utilitiesByKey = loadUtilities(root.resolve("utilities")).stream()
                .collect(Collectors.toMap(
                        utility -> key(utility.state(), utility.canonicalSlug()),
                        Function.identity(),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
        this.utilitiesById = utilitiesByKey.values().stream()
                .collect(Collectors.toMap(
                        UtilityRecord::utilityId,
                        Function.identity(),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
        this.aliasesByKey = loadAliases(root.resolve("city_aliases.csv")).stream()
                .collect(Collectors.toMap(
                        alias -> key(alias.state(), alias.aliasSlug()),
                        Function.identity(),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
        this.providers = loadProviders(root.resolve("providers").resolve("providers.csv")).stream()
                .sorted(PROVIDER_SORT)
                .toList();
        this.stateGuidesByState = loadStateGuides(root.resolve("states")).stream()
                .collect(Collectors.toMap(
                        guide -> guide.state().trim().toLowerCase(Locale.US),
                        Function.identity(),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
        this.guidesBySlug = loadGuides(root.resolve("guides")).stream()
                .collect(Collectors.toMap(
                        guide -> guide.slug().trim().toLowerCase(Locale.US),
                        Function.identity(),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
        this.metrosByKey = loadMetros(root.resolve("metros")).stream()
                .collect(Collectors.toMap(
                        metro -> key(metro.state(), metro.metroSlug()),
                        Function.identity(),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
    }

    public List<UtilityRecord> listPublishedUtilities() {
        LocalDate today = LocalDate.now();
        return utilitiesByKey.values().stream()
                .filter(utility -> utility.isPublishable(today))
                .filter(utility -> !opsIssueService.hasBlockingIssue(utility, today))
                .filter(utility -> !sourceEvidenceService.hasBlockingIssue(utility))
                .sorted(Comparator.comparing(UtilityRecord::utilityName))
                .toList();
    }

    public List<UtilityRecord> listAllUtilities() {
        return utilitiesByKey.values().stream()
                .sorted(Comparator.comparing(UtilityRecord::utilityName))
                .toList();
    }

    public Optional<UtilityRecord> findPublishedUtility(String state, String slug) {
        LocalDate today = LocalDate.now();
        return Optional.ofNullable(utilitiesByKey.get(key(state, slug)))
                .filter(utility -> utility.isPublishable(today))
                .filter(utility -> !opsIssueService.hasBlockingIssue(utility, today))
                .filter(utility -> !sourceEvidenceService.hasBlockingIssue(utility));
    }

    public Optional<UtilityRecord> findUtilityById(String utilityId) {
        LocalDate today = LocalDate.now();
        return Optional.ofNullable(utilitiesById.get(utilityId))
                .filter(utility -> utility.isPublishable(today))
                .filter(utility -> !opsIssueService.hasBlockingIssue(utility, today))
                .filter(utility -> !sourceEvidenceService.hasBlockingIssue(utility));
    }

    public List<UtilityRecord> listPublishedUtilitiesForState(String state) {
        return listPublishedUtilities().stream()
                .filter(utility -> utility.state().equalsIgnoreCase(state))
                .toList();
    }

    public Optional<CityAliasRecord> findCityAlias(String state, String aliasSlug) {
        return Optional.ofNullable(aliasesByKey.get(key(state, aliasSlug)));
    }

    public List<ProviderRecord> findProvidersForUtility(String utilityId) {
        return sortProviders(providers.stream()
                .filter(ProviderRecord::isPublicListing)
                .filter(provider -> provider.matchesUtility(utilityId)));
    }

    public List<ProviderRecord> findAssignableProvidersForUtility(String utilityId) {
        return sortProviders(providers.stream()
                .filter(ProviderRecord::isPublicListing)
                .filter(provider -> provider.matchesUtility(utilityId)));
    }

    public List<ProviderRecord> listPublicProviders() {
        return sortProviders(providers.stream().filter(ProviderRecord::isPublicListing));
    }

    public List<ProviderRecord> listHeldProviders() {
        return sortProviders(providers.stream().filter(ProviderRecord::isHoldListing));
    }

    public List<ProviderRecord> listAssignableProviders() {
        return sortProviders(providers.stream().filter(ProviderRecord::isAssignableListing));
    }

    public Optional<ProviderRecord> findPublicProvider(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return Optional.empty();
        }
        return providers.stream()
                .filter(ProviderRecord::isPublicListing)
                .filter(provider -> provider.providerId().equalsIgnoreCase(providerId.trim()))
                .findFirst();
    }

    public Optional<ProviderRecord> findAssignableProvider(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return Optional.empty();
        }
        return providers.stream()
                .filter(ProviderRecord::isPublicListing)
                .filter(provider -> provider.providerId().equalsIgnoreCase(providerId.trim()))
                .findFirst();
    }

    public List<UtilityRecord> findPublishedUtilitiesForProvider(ProviderRecord provider) {
        LocalDate today = LocalDate.now();
        return provider.coverageTargets().stream()
                .map(utilitiesById::get)
                .filter(java.util.Objects::nonNull)
                .filter(utility -> utility.isPublishable(today))
                .filter(utility -> !opsIssueService.hasBlockingIssue(utility, today))
                .filter(utility -> !sourceEvidenceService.hasBlockingIssue(utility))
                .distinct()
                .toList();
    }

    public List<MetroRecord> listPublishedMetros() {
        LocalDate today = LocalDate.now();
        return metrosByKey.values().stream()
                .filter(metro -> metro.isPublishable(today))
                .sorted(Comparator.comparing(MetroRecord::title))
                .toList();
    }

    public Optional<MetroRecord> findPublishedMetro(String state, String metroSlug) {
        if (state == null || state.isBlank() || metroSlug == null || metroSlug.isBlank()) {
            return Optional.empty();
        }
        LocalDate today = LocalDate.now();
        return Optional.ofNullable(metrosByKey.get(key(state, metroSlug)))
                .filter(metro -> metro.isPublishable(today));
    }

    public List<MetroRecord> listPublishedMetrosForUtility(String utilityId) {
        LocalDate today = LocalDate.now();
        return metrosByKey.values().stream()
                .filter(metro -> metro.isPublishable(today))
                .filter(metro -> metro.utilityIds().stream().anyMatch(id -> id.equalsIgnoreCase(utilityId)))
                .sorted(Comparator.comparing(MetroRecord::title))
                .toList();
    }

    public List<UtilityRecord> featuredUtilitiesForMetro(MetroRecord metro) {
        LocalDate today = LocalDate.now();
        return metro.utilityIds().stream()
                .map(utilitiesById::get)
                .filter(java.util.Objects::nonNull)
                .filter(utility -> utility.isPublishable(today))
                .filter(utility -> !opsIssueService.hasBlockingIssue(utility, today))
                .filter(utility -> !sourceEvidenceService.hasBlockingIssue(utility))
                .distinct()
                .toList();
    }

    public List<ProviderRecord> findProvidersForMetro(MetroRecord metro) {
        return providers.stream()
                .filter(ProviderRecord::isPublicListing)
                .filter(provider -> metro.utilityIds().stream().anyMatch(provider::matchesUtility))
                .sorted(Comparator.comparing((ProviderRecord provider) -> metroCoverageSize(provider, metro), Comparator.reverseOrder())
                        .thenComparing(ProviderRecord::providerName))
                .toList();
    }

    private int metroCoverageSize(ProviderRecord provider, MetroRecord metro) {
        return (int) metro.utilityIds().stream()
                .filter(provider::matchesUtility)
                .count();
    }

    private List<ProviderRecord> sortProviders(Stream<ProviderRecord> providerStream) {
        return providerStream.sorted(PROVIDER_SORT).toList();
    }

    public Optional<StateGuideRecord> findPublishedStateGuide(String state) {
        if (state == null || state.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(stateGuidesByState.get(state.trim().toLowerCase(Locale.US)))
                .filter(guide -> guide.isPublishable(LocalDate.now()))
                .filter(guide -> !opsIssueService.hasBlockingIssue(guide, LocalDate.now()))
                .filter(guide -> !sourceEvidenceService.hasBlockingIssue(guide));
    }

    public List<StateGuideRecord> listAllStateGuides() {
        return stateGuidesByState.values().stream()
                .sorted(Comparator.comparing(StateGuideRecord::title))
                .toList();
    }

    public List<StateGuideRecord> listPublishedStateGuides() {
        LocalDate today = LocalDate.now();
        return stateGuidesByState.values().stream()
                .filter(guide -> guide.isPublishable(today))
                .filter(guide -> !opsIssueService.hasBlockingIssue(guide, today))
                .filter(guide -> !sourceEvidenceService.hasBlockingIssue(guide))
                .sorted(Comparator.comparing(StateGuideRecord::title))
                .toList();
    }

    public List<UtilityRecord> featuredUtilitiesForStateGuide(StateGuideRecord stateGuide) {
        LocalDate today = LocalDate.now();
        return stateGuide.featuredUtilityIds().stream()
                .map(utilitiesById::get)
                .filter(java.util.Objects::nonNull)
                .filter(utility -> utility.isPublishable(today))
                .filter(utility -> !opsIssueService.hasBlockingIssue(utility, today))
                .filter(utility -> !sourceEvidenceService.hasBlockingIssue(utility))
                .toList();
    }

    public List<GuideRecord> listPublishedGuides() {
        LocalDate today = LocalDate.now();
        return guidesBySlug.values().stream()
                .filter(guide -> guide.isPublishable(today))
                .filter(guide -> !opsIssueService.hasBlockingIssue(guide, today))
                .filter(guide -> !sourceEvidenceService.hasBlockingIssue(guide))
                .sorted(Comparator.comparing(GuideRecord::title))
                .toList();
    }

    public List<GuideRecord> listAllGuides() {
        return guidesBySlug.values().stream()
                .sorted(Comparator.comparing(GuideRecord::title))
                .toList();
    }

    public Optional<GuideRecord> findPublishedGuide(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        LocalDate today = LocalDate.now();
        return Optional.ofNullable(guidesBySlug.get(slug.trim().toLowerCase(Locale.US)))
                .filter(guide -> guide.isPublishable(today))
                .filter(guide -> !opsIssueService.hasBlockingIssue(guide, today))
                .filter(guide -> !sourceEvidenceService.hasBlockingIssue(guide));
    }

    private List<UtilityRecord> loadUtilities(Path utilitiesRoot) {
        if (!Files.exists(utilitiesRoot)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(utilitiesRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .sorted()
                    .map(this::readUtility)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan utility JSON files in " + utilitiesRoot, exception);
        }
    }

    private UtilityRecord readUtility(Path path) {
        try {
            return objectMapper.readValue(path.toFile(), UtilityRecord.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse utility JSON " + path, exception);
        }
    }

    private List<CityAliasRecord> loadAliases(Path csvPath) {
        if (!Files.exists(csvPath)) {
            return List.of();
        }
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        try (Reader reader = Files.newBufferedReader(csvPath)) {
            return csvMapper.readerFor(CityAliasCsvRow.class)
                    .with(schema)
                    .<CityAliasCsvRow>readValues(reader)
                    .readAll()
                    .stream()
                    .map(row -> new CityAliasRecord(
                            row.city(),
                            row.state(),
                            row.utilityId(),
                            row.aliasSlug(),
                            AliasMode.valueOf(row.aliasMode().trim().toUpperCase(Locale.US)),
                            row.justification(),
                            row.lastReviewed()
                    ))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse city alias CSV " + csvPath, exception);
        }
    }

    private List<ProviderRecord> loadProviders(Path csvPath) {
        if (!Files.exists(csvPath)) {
            return List.of();
        }
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        try (Reader reader = Files.newBufferedReader(csvPath)) {
            return csvMapper.readerFor(ProviderCsvRow.class)
                    .with(schema)
                    .<ProviderCsvRow>readValues(reader)
                    .readAll()
                    .stream()
                    .map(row -> new ProviderRecord(
                            row.providerId(),
                            row.providerName(),
                            row.coverageType(),
                            splitTargets(row.coverageTargets()),
                            parseListingStatus(row.listingStatus()),
                            row.licenseOrCertificationNotes(),
                            row.officialApprovalSourceUrl(),
                            row.phone(),
                            row.email(),
                            row.siteUrl(),
                            row.listingSource(),
                            row.pageLabel(),
                            row.lastReviewed()
                    ))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse provider CSV " + csvPath, exception);
        }
    }

    private ProviderListingStatus parseListingStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return ProviderListingStatus.HOLD;
        }
        String normalized = rawStatus.trim().toUpperCase(Locale.US);
        return switch (normalized) {
            case "PUBLIC" -> ProviderListingStatus.PUBLIC;
            default -> ProviderListingStatus.HOLD;
        };
    }

    private List<StateGuideRecord> loadStateGuides(Path statesRoot) {
        if (!Files.exists(statesRoot)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(statesRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .sorted()
                    .map(this::readStateGuide)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan state guide JSON files in " + statesRoot, exception);
        }
    }

    private StateGuideRecord readStateGuide(Path path) {
        try {
            return objectMapper.readValue(path.toFile(), StateGuideRecord.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse state guide JSON " + path, exception);
        }
    }

    private List<GuideRecord> loadGuides(Path guidesRoot) {
        if (!Files.exists(guidesRoot)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(guidesRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .sorted()
                    .map(this::readGuide)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan guide JSON files in " + guidesRoot, exception);
        }
    }

    private List<MetroRecord> loadMetros(Path metrosRoot) {
        if (!Files.exists(metrosRoot)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(metrosRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .sorted()
                    .map(this::readMetro)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan metro JSON files in " + metrosRoot, exception);
        }
    }

    private MetroRecord readMetro(Path path) {
        try {
            return objectMapper.readValue(path.toFile(), MetroRecord.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse metro JSON " + path, exception);
        }
    }

    private GuideRecord readGuide(Path path) {
        try {
            return objectMapper.readValue(path.toFile(), GuideRecord.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse guide JSON " + path, exception);
        }
    }

    private List<String> splitTargets(String rawTargets) {
        if (rawTargets == null || rawTargets.isBlank()) {
            return List.of();
        }
        return Stream.of(rawTargets.split("\\|"))
                .map(String::trim)
                .filter(target -> !target.isBlank())
                .toList();
    }

    private String key(String state, String slug) {
        return (state == null ? "" : state.trim().toLowerCase(Locale.US))
                + "::"
                + (slug == null ? "" : slug.trim().toLowerCase(Locale.US));
    }
}
