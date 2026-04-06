package owner.backflow.web;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import owner.backflow.config.AppSiteProperties;
import owner.backflow.data.model.AliasMode;
import owner.backflow.data.model.CityAliasRecord;
import owner.backflow.data.model.GuideRecord;
import owner.backflow.data.model.MetroRecord;
import owner.backflow.data.model.ProviderRecord;
import owner.backflow.data.model.StateGuideRecord;
import owner.backflow.data.model.UtilityFocusContent;
import owner.backflow.data.model.UtilityRecord;
import owner.backflow.files.BackflowRegistryService;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class SiteController {
    private final BackflowRegistryService registryService;
    private final AppSiteProperties siteProperties;

    public SiteController(BackflowRegistryService registryService, AppSiteProperties siteProperties) {
        this.registryService = registryService;
        this.siteProperties = siteProperties;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<UtilityRecord> utilities = registryService.listPublishedUtilities();
        List<GuideRecord> guides = registryService.listPublishedGuides();
        List<GuideRecord> featuredGuides = guides.stream()
                .limit(6)
                .toList();
        List<StateGuideRecord> stateGuides = registryService.listPublishedStateGuides();
        List<MetroRecord> metros = registryService.listPublishedMetros();
        List<UtilityRecord> featuredUtilities = balancedUtilities(utilities, 5);
        Map<String, String> testerPaths = new LinkedHashMap<>();
        Map<String, String> testerLabels = new LinkedHashMap<>();
        for (UtilityRecord utility : utilities) {
            String path = testerPath(utility);
            if (path != null) {
                testerPaths.put(utility.utilityId(), path);
                testerLabels.put(utility.utilityId(), testerLabel(utility));
            }
        }
        model.addAttribute("page", page(
                "BackflowVerdict | Find local backflow testing requirements",
                "Find utility-specific backflow testing requirements, annual testing steps, failed-test guidance, and clearly labeled tester routes.",
                "/",
                breadcrumbStructuredData(List.of(new BreadcrumbItem("Home", canonical("/"))))
        ));
        model.addAttribute("utilities", utilities);
        model.addAttribute("featuredUtilities", featuredUtilities);
        model.addAttribute("guides", guides);
        model.addAttribute("featuredGuides", featuredGuides);
        model.addAttribute("stateGuides", stateGuides);
        model.addAttribute("metros", metros);
        model.addAttribute("testerPaths", testerPaths);
        model.addAttribute("testerLabels", testerLabels);
        model.addAttribute("featuredStateGuide", stateGuides.isEmpty() ? null : stateGuides.get(0));
        model.addAttribute("publishedUtilityCount", utilities.size());
        model.addAttribute("publishedStateCount", stateGuides.size());
        model.addAttribute("publishedMetroCount", metros.size());
        model.addAttribute("publicProviderCount", registryService.listPublicProviders().size());
        return "pages/home";
    }

    @GetMapping("/states")
    public String statesIndex(Model model) {
        List<StateGuideRecord> stateGuides = registryService.listPublishedStateGuides();
        Map<String, Integer> utilityCounts = stateGuides.stream()
                .collect(Collectors.toMap(
                        StateGuideRecord::state,
                        guide -> registryService.listPublishedUtilitiesForState(guide.state()).size(),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
        int publishedUtilityCount = utilityCounts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        model.addAttribute("page", page(
                "State backflow guides | BackflowVerdict",
                "Browse source-backed state backflow guides that route into utility-specific testing requirements and next-step pages.",
                "/states",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("States", canonical("/states"))
                ))
        ));
        model.addAttribute("stateGuides", stateGuides);
        model.addAttribute("utilityCounts", utilityCounts);
        model.addAttribute("publishedUtilityCount", publishedUtilityCount);
        model.addAttribute("featuredUtilities", balancedUtilities(registryService.listPublishedUtilities(), 6));
        return "pages/states-index";
    }

    @GetMapping("/metros")
    public String metrosIndex(Model model) {
        List<MetroRecord> metros = registryService.listPublishedMetros();
        Map<String, Integer> providerCounts = metros.stream()
                .collect(Collectors.toMap(
                        MetroRecord::metroId,
                        metro -> registryService.findProvidersForMetro(metro).size(),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
        int publicProviderCount = providerCounts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        model.addAttribute("page", page(
                "Metro backflow coverage | BackflowVerdict",
                "Browse metro backflow pages that group nearby utility rules, public provider profiles, and local support guides.",
                "/metros",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("Metros", canonical("/metros"))
                ))
        ));
        model.addAttribute("metros", metros);
        model.addAttribute("providerCounts", providerCounts);
        model.addAttribute("publicProviderCount", publicProviderCount);
        model.addAttribute("featuredUtilities", balancedUtilities(registryService.listPublishedUtilities(), 4));
        return "pages/metros-index";
    }

    @GetMapping("/guides")
    public String guidesIndex(Model model) {
        List<GuideRecord> guides = registryService.listPublishedGuides();
        model.addAttribute("page", page(
                "Backflow guides | BackflowVerdict",
                "Browse practical backflow guides that explain recurring rule patterns without replacing utility-specific authority pages.",
                "/guides",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("Guides", canonical("/guides"))
                ))
        ));
        model.addAttribute("guides", guides);
        model.addAttribute("featuredUtilities", balancedUtilities(registryService.listPublishedUtilities(), 4));
        model.addAttribute("stateGuides", registryService.listPublishedStateGuides());
        model.addAttribute("publishedGuideCount", guides.size());
        return "pages/guides-index";
    }

    @GetMapping("/states/{state}/backflow-testing")
    public String stateGuidePage(@PathVariable String state, Model model) {
        StateGuideRecord stateGuide = registryService.findPublishedStateGuide(state)
                .orElseThrow(() -> new NotFoundException("State guide not found."));
        List<UtilityRecord> utilities = registryService.listPublishedUtilitiesForState(state);
        model.addAttribute("page", page(
                stateGuide.title() + " | BackflowVerdict",
                stateGuide.description(),
                "/states/" + state + "/backflow-testing",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem(stateGuide.title(), canonical("/states/" + state + "/backflow-testing"))
                ))
        ));
        model.addAttribute("stateGuide", stateGuide);
        model.addAttribute("featuredUtilities", registryService.featuredUtilitiesForStateGuide(stateGuide));
        model.addAttribute("allUtilities", utilities);
        model.addAttribute("guides", supportGuidesForStateGuide(stateGuide));
        return "pages/state-guide";
    }

    @GetMapping("/guides/{slug}")
    public String guidePage(@PathVariable String slug, Model model) {
        GuideRecord guide = registryService.findPublishedGuide(slug)
                .orElseThrow(() -> new NotFoundException("Guide page not found."));
        List<UtilityRecord> relatedUtilities = relatedUtilitiesForGuide(guide);
        List<MetroRecord> relatedMetros = relatedMetrosForUtilities(relatedUtilities);
        List<StateGuideRecord> stateGuides = relatedStateGuidesForUtilities(relatedUtilities);
        model.addAttribute("page", page(
                guide.title() + " | BackflowVerdict",
                guide.description(),
                "/guides/" + guide.slug(),
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("Guides", canonical("/guides")),
                        new BreadcrumbItem(guide.title(), canonical("/guides/" + guide.slug()))
                ))
        ));
        model.addAttribute("guide", guide);
        model.addAttribute("stateGuides", stateGuides);
        model.addAttribute("relatedUtilities", relatedUtilities);
        model.addAttribute("relatedMetros", relatedMetros);
        model.addAttribute("relatedGuides", relatedGuidesForGuide(guide));
        return "pages/guide-page";
    }

    @GetMapping("/metros/{state}/{metroSlug}/backflow-testing")
    public String metroPage(
            @PathVariable String state,
            @PathVariable String metroSlug,
            Model model
    ) {
        MetroRecord metro = registryService.findPublishedMetro(state, metroSlug)
                .orElseThrow(() -> new NotFoundException("Metro page not found."));
        List<ProviderRecord> providers = registryService.findProvidersForMetro(metro);
        model.addAttribute("page", page(
                metro.title() + " | BackflowVerdict",
                metro.description(),
                metroPath(metro),
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem(stateLabel(metro.state()), canonical("/states/" + metro.state() + "/backflow-testing")),
                        new BreadcrumbItem(metro.title(), canonical(metroPath(metro)))
                ))
        ));
        model.addAttribute("metro", metro);
        model.addAttribute("utilities", registryService.featuredUtilitiesForMetro(metro));
        model.addAttribute("providers", providers);
        model.addAttribute("providerCoverageCounts", providerCoverageCounts(metro, providers));
        model.addAttribute("guides", metroGuides(metro));
        return "pages/metro-page";
    }

    @GetMapping("/providers/{providerId}/")
    public String providerPage(@PathVariable String providerId, Model model) {
        ProviderRecord provider = registryService.findPublicProvider(providerId)
                .orElseThrow(() -> new NotFoundException("Provider page not found."));
        List<UtilityRecord> utilities = registryService.findPublishedUtilitiesForProvider(provider);
        List<MetroRecord> metros = registryService.listPublishedMetros().stream()
                .filter(metro -> utilities.stream().anyMatch(utility -> metro.utilityIds().contains(utility.utilityId())))
                .toList();
        model.addAttribute("page", page(
                provider.providerName() + " | BackflowVerdict",
                provider.pageLabel(),
                providerPath(provider),
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("Providers", canonical("/")),
                        new BreadcrumbItem(provider.providerName(), canonical(providerPath(provider)))
                ))
        ));
        model.addAttribute("provider", provider);
        model.addAttribute("utilities", utilities);
        model.addAttribute("metros", metros);
        model.addAttribute("relatedGuides", providerSupportGuides(utilities));
        model.addAttribute("primaryUtility", utilities.isEmpty() ? null : utilities.getFirst());
        return "pages/provider-page";
    }

    @GetMapping("/utilities/{state}/{utilitySlug}/")
    public String utilityPage(
            @PathVariable String state,
            @PathVariable String utilitySlug,
            Model model
    ) {
        UtilityRecord utility = registryService.findPublishedUtility(state, utilitySlug)
                .orElseThrow(() -> new NotFoundException("Utility page not found."));
        List<FaqItem> faqItems = utilityFaqItems(utility);
        model.addAttribute("page", page(
                utility.utilityName() + " backflow testing requirements | BackflowVerdict",
                utility.verdictSummary(),
                utilityPath(utility),
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                                new BreadcrumbItem(utility.utilityName(), canonical(utilityPath(utility)))
                        )),
                        faqStructuredData(faqItems)
                )
        ));
        model.addAttribute("utility", utility);
        model.addAttribute("annualTestingPath", utility.supportsAnnualTestingPage() ? utilityPath(utility) + "annual-testing" : null);
        model.addAttribute("irrigationPath", utility.supportsIrrigationPage() ? utilityPath(utility) + "irrigation" : null);
        model.addAttribute("fireLinePath", utility.supportsFireLinePage() ? utilityPath(utility) + "fire-line" : null);
        model.addAttribute("testerPath", testerPath(utility));
        model.addAttribute("testerLabel", testerLabel(utility));
        model.addAttribute("faqItems", faqItems);
        model.addAttribute("stateGuide", registryService.findPublishedStateGuide(utility.state()).orElse(null));
        model.addAttribute("metros", registryService.listPublishedMetrosForUtility(utility.utilityId()));
        model.addAttribute("relatedGuides", utilitySupportGuides(utility));
        model.addAttribute("activeSponsorCount", activeSponsorCount(utility));
        return "pages/utility-page";
    }

    @GetMapping("/utilities/{state}/{utilitySlug}/annual-testing")
    public String annualTestingPage(
            @PathVariable String state,
            @PathVariable String utilitySlug,
            Model model
    ) {
        UtilityRecord utility = registryService.findPublishedUtility(state, utilitySlug)
                .filter(UtilityRecord::supportsAnnualTestingPage)
                .orElseThrow(() -> new NotFoundException("Annual testing page not available for this utility."));
        return renderUtilityFocusPage(
                model,
                utility,
                "Annual testing",
                utility.utilityName() + " annual backflow testing",
                utility.resolvedAnnualTesting(),
                utility.resolvedAnnualTesting().summary(),
                utilityPath(utility) + "annual-testing"
        );
    }

    @GetMapping("/utilities/{state}/{utilitySlug}/failed-test")
    public String failedTestPage(
            @PathVariable String state,
            @PathVariable String utilitySlug,
            Model model
    ) {
        UtilityRecord utility = registryService.findPublishedUtility(state, utilitySlug)
                .orElseThrow(() -> new NotFoundException("Failed-test page not found."));
        model.addAttribute("page", page(
                utility.utilityName() + " failed backflow test | BackflowVerdict",
                "Repair, retest, and submission next steps for a failed backflow test in " + utility.utilityName() + ".",
                utilityPath(utility) + "failed-test",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                        new BreadcrumbItem(utility.utilityName(), canonical(utilityPath(utility))),
                        new BreadcrumbItem("Failed test", canonical(utilityPath(utility) + "failed-test"))
                ))
        ));
        model.addAttribute("utility", utility);
        model.addAttribute("failedGuide", registryService.findPublishedGuide("failed-backflow-test-next-steps").orElse(null));
        model.addAttribute("testerPath", testerPath(utility));
        model.addAttribute("testerLabel", testerLabel(utility));
        model.addAttribute("activeSponsorCount", activeSponsorCount(utility));
        model.addAttribute("relatedGuides", guidesByPreferredSlugs(List.of(
                "failed-backflow-test-next-steps",
                "backflow-test-cost",
                "approved-testers-vs-find-a-tester"
        ), 3, "failed-backflow-test-next-steps"));
        return "pages/failed-test-page";
    }

    @GetMapping("/utilities/{state}/{utilitySlug}/approved-testers")
    public String approvedTestersPage(
            @PathVariable String state,
            @PathVariable String utilitySlug,
            Model model
    ) {
        UtilityRecord utility = registryService.findPublishedUtility(state, utilitySlug)
                .filter(UtilityRecord::supportsApprovedTestersPage)
                .orElseThrow(() -> new NotFoundException("Approved tester page not available for this utility."));
        List<ProviderRecord> providers = registryService.findProvidersForUtility(utility.utilityId());
        model.addAttribute("page", page(
                utility.utilityName() + " approved testers | BackflowVerdict",
                "Official tester list and clearly labeled sponsor directory for " + utility.utilityName() + ".",
                utilityPath(utility) + "approved-testers",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                        new BreadcrumbItem(utility.utilityName(), canonical(utilityPath(utility))),
                        new BreadcrumbItem("Approved testers", canonical(utilityPath(utility) + "approved-testers"))
                ))
        ));
        model.addAttribute("utility", utility);
        model.addAttribute("providers", providers);
        model.addAttribute("official", true);
        model.addAttribute("activeSponsorCount", activeSponsorCount(utility));
        return "pages/tester-page";
    }

    @GetMapping("/utilities/{state}/{utilitySlug}/find-a-tester")
    public String findATesterPage(
            @PathVariable String state,
            @PathVariable String utilitySlug,
            Model model
    ) {
        UtilityRecord utility = registryService.findPublishedUtility(state, utilitySlug)
                .filter(UtilityRecord::supportsFindATesterPage)
                .orElseThrow(() -> new NotFoundException("Find-a-tester page not available for this utility."));
        List<ProviderRecord> providers = registryService.findProvidersForUtility(utility.utilityId());
        if (providers.isEmpty()) {
            throw new NotFoundException("Find-a-tester page not available for this utility.");
        }
        model.addAttribute("page", page(
                utility.utilityName() + " find a tester | BackflowVerdict",
                "Non-official provider directory for " + utility.utilityName() + ", kept separate from authority guidance.",
                utilityPath(utility) + "find-a-tester",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                        new BreadcrumbItem(utility.utilityName(), canonical(utilityPath(utility))),
                        new BreadcrumbItem("Find a tester", canonical(utilityPath(utility) + "find-a-tester"))
                ))
        ));
        model.addAttribute("utility", utility);
        model.addAttribute("providers", providers);
        model.addAttribute("official", false);
        model.addAttribute("activeSponsorCount", activeSponsorCount(utility));
        return "pages/tester-page";
    }

    @GetMapping("/utilities/{state}/{utilitySlug}/irrigation")
    public String irrigationPage(
            @PathVariable String state,
            @PathVariable String utilitySlug,
            Model model
    ) {
        UtilityRecord utility = registryService.findPublishedUtility(state, utilitySlug)
                .filter(UtilityRecord::supportsIrrigationPage)
                .orElseThrow(() -> new NotFoundException("Irrigation page not available for this utility."));
        return renderUtilityFocusPage(
                model,
                utility,
                "Irrigation",
                utility.utilityName() + " irrigation backflow rules",
                utility.irrigation(),
                utility.irrigation().summary(),
                utilityPath(utility) + "irrigation"
        );
    }

    @GetMapping("/utilities/{state}/{utilitySlug}/fire-line")
    public String fireLinePage(
            @PathVariable String state,
            @PathVariable String utilitySlug,
            Model model
    ) {
        UtilityRecord utility = registryService.findPublishedUtility(state, utilitySlug)
                .filter(UtilityRecord::supportsFireLinePage)
                .orElseThrow(() -> new NotFoundException("Fire-line page not available for this utility."));
        return renderUtilityFocusPage(
                model,
                utility,
                "Fire line",
                utility.utilityName() + " fire line backflow rules",
                utility.fireLine(),
                utility.fireLine().summary(),
                utilityPath(utility) + "fire-line"
        );
    }

    @GetMapping("/cities/{state}/{citySlug}/backflow-testing")
    public Object cityAliasPage(
            @PathVariable String state,
            @PathVariable String citySlug,
            Model model
    ) {
        CityAliasRecord alias = registryService.findCityAlias(state, citySlug)
                .orElseThrow(() -> new NotFoundException("City alias not found."));
        UtilityRecord utility = registryService.findUtilityById(alias.utilityId())
                .orElseThrow(() -> new NotFoundException("Mapped utility is not available."));

        if (alias.aliasMode() == AliasMode.REDIRECT) {
            RedirectView redirectView = new RedirectView(utilityPath(utility));
            redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
            redirectView.setExposeModelAttributes(false);
            return redirectView;
        }

        model.addAttribute("page", new PageMeta(
                alias.city() + " backflow testing | BackflowVerdict",
                "City alias bridge page routing " + alias.city() + " searches to the governing utility.",
                canonical("/cities/" + state + "/" + citySlug + "/backflow-testing"),
                alias.aliasMode() == AliasMode.NOINDEX_BRIDGE,
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem(alias.city(), canonical("/cities/" + state + "/" + citySlug + "/backflow-testing"))
                ))
        ));
        model.addAttribute("alias", alias);
        model.addAttribute("utility", utility);
        return "pages/city-bridge";
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        List<SitemapEntry> urls = new ArrayList<>();
        urls.add(new SitemapEntry(canonical("/"), homeLastModified()));
        registryService.listPublishedStateGuides()
                .forEach(guide -> urls.add(new SitemapEntry(
                        canonical("/states/" + guide.state() + "/backflow-testing"),
                        guide.lastVerified()
                )));
        registryService.listPublishedGuides().forEach(guide -> urls.add(new SitemapEntry(
                canonical("/guides/" + guide.slug()),
                guide.lastReviewed()
        )));
        registryService.listPublishedMetros().forEach(metro -> urls.add(new SitemapEntry(
                canonical(metroPath(metro)),
                metro.lastReviewed()
        )));
        for (UtilityRecord utility : registryService.listPublishedUtilities()) {
            urls.add(new SitemapEntry(canonical(utilityPath(utility)), utility.lastVerified()));
            if (utility.supportsAnnualTestingPage()) {
                urls.add(new SitemapEntry(canonical(utilityPath(utility) + "annual-testing"), utility.lastVerified()));
            }
            urls.add(new SitemapEntry(canonical(utilityPath(utility) + "failed-test"), utility.lastVerified()));
            if (utility.supportsIrrigationPage()) {
                urls.add(new SitemapEntry(canonical(utilityPath(utility) + "irrigation"), utility.lastVerified()));
            }
            if (utility.supportsFireLinePage()) {
                urls.add(new SitemapEntry(canonical(utilityPath(utility) + "fire-line"), utility.lastVerified()));
            }
            if (utility.supportsApprovedTestersPage()) {
                urls.add(new SitemapEntry(canonical(utilityPath(utility) + "approved-testers"), utility.lastVerified()));
            }
            if (utility.supportsFindATesterPage() && !registryService.findProvidersForUtility(utility.utilityId()).isEmpty()) {
                urls.add(new SitemapEntry(canonical(utilityPath(utility) + "find-a-tester"), utility.lastVerified()));
            }
        }
        registryService.listPublicProviders().forEach(provider -> urls.add(new SitemapEntry(
                canonical(providerPath(provider)),
                provider.lastReviewed()
        )));

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        for (SitemapEntry entry : urls) {
            xml.append("<url><loc>")
                    .append(entry.url())
                    .append("</loc><lastmod>")
                    .append(entry.lastModified())
                    .append("</lastmod></url>");
        }
        xml.append("</urlset>");
        return xml.toString();
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots() {
        return "User-agent: *\n"
                + "Allow: /\n\n"
                + "Sitemap: " + canonical("/sitemap.xml") + "\n";
    }

    private PageMeta page(String title, String description, String path) {
        return page(title, description, path, null);
    }

    private PageMeta page(String title, String description, String path, String structuredDataJson) {
        return new PageMeta(title, description, canonical(path), false, structuredDataJson);
    }

    private String testerPath(UtilityRecord utility) {
        if (utility.supportsApprovedTestersPage()) {
            return utilityPath(utility) + "approved-testers";
        }
        if (utility.supportsFindATesterPage() && !registryService.findProvidersForUtility(utility.utilityId()).isEmpty()) {
            return utilityPath(utility) + "find-a-tester";
        }
        return null;
    }

    private String testerLabel(UtilityRecord utility) {
        if (utility.supportsApprovedTestersPage()) {
            return "View the official tester list";
        }
        if (utility.supportsFindATesterPage()) {
            return "Find a local tester";
        }
        return null;
    }

    private String utilityPath(UtilityRecord utility) {
        return "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/";
    }

    private String metroPath(MetroRecord metro) {
        return "/metros/" + metro.state() + "/" + metro.metroSlug() + "/backflow-testing";
    }

    private String providerPath(ProviderRecord provider) {
        return "/providers/" + provider.providerId() + "/";
    }

    private String renderUtilityFocusPage(
            Model model,
            UtilityRecord utility,
            String eyebrow,
            String titleStem,
            UtilityFocusContent focus,
            String description,
            String path
    ) {
        model.addAttribute("page", page(
                titleStem + " | BackflowVerdict",
                description,
                path,
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                                new BreadcrumbItem(utility.utilityName(), canonical(utilityPath(utility))),
                                new BreadcrumbItem(titleStem, canonical(path))
                        )),
                        faqStructuredData(utilityFaqItems(utility))
                )
        ));
        model.addAttribute("utility", utility);
        model.addAttribute("eyebrow", eyebrow);
        model.addAttribute("heading", titleStem);
        model.addAttribute("intro", description);
        model.addAttribute("focus", focus);
        model.addAttribute("residentialNotes", utility.residentialNotes());
        model.addAttribute("commercialNotes", utility.commercialNotes());
        model.addAttribute("testerPath", testerPath(utility));
        model.addAttribute("testerLabel", testerLabel(utility));
        model.addAttribute("faqItems", utilityFaqItems(utility));
        model.addAttribute("stateGuide", registryService.findPublishedStateGuide(utility.state()).orElse(null));
        model.addAttribute("relatedGuides", utilitySupportGuides(utility));
        model.addAttribute("activeSponsorCount", activeSponsorCount(utility));
        return "pages/utility-focus-page";
    }

    private List<GuideRecord> utilitySupportGuides(UtilityRecord utility) {
        List<String> preferred = new ArrayList<>(List.of("how-we-verify-backflow-rules"));
        if (usesPortalWorkflow(utility)) {
            preferred.add("backflow-reporting-portals");
        }
        if (hasDateSpecificWorkflow(utility)) {
            preferred.add("anniversary-date-vs-calendar-deadline");
        }
        if (hasResidentialCommercialSplit(utility)) {
            preferred.add("residential-vs-commercial-backflow-rules");
        }
        preferred.add("backflow-test-cost");
        preferred.add("who-needs-a-backflow-preventer");
        if (utility.supportsApprovedTestersPage() || utility.supportsFindATesterPage()) {
            preferred.add("approved-testers-vs-find-a-tester");
            preferred.add("county-certified-vs-utility-approved-testers");
        }
        if (utility.supportsIrrigationPage() || utility.supportsFireLinePage()) {
            preferred.add("rpz-vs-dcva-vs-pvb");
        }
        return guidesByPreferredSlugs(preferred, 5, null);
    }

    private List<GuideRecord> providerSupportGuides(List<UtilityRecord> utilities) {
        List<String> preferred = new ArrayList<>(List.of(
                "approved-testers-vs-find-a-tester",
                "how-we-verify-backflow-rules"
        ));
        if (utilities.stream().anyMatch(this::usesPortalWorkflow)) {
            preferred.add("backflow-reporting-portals");
        }
        if (utilities.stream().anyMatch(this::hasDateSpecificWorkflow)) {
            preferred.add("anniversary-date-vs-calendar-deadline");
        }
        if (utilities.stream().anyMatch(this::hasResidentialCommercialSplit)) {
            preferred.add("residential-vs-commercial-backflow-rules");
        }
        preferred.add("backflow-test-cost");
        return guidesByPreferredSlugs(preferred, 5, null);
    }

    private List<GuideRecord> metroGuides(MetroRecord metro) {
        if (!metro.guideSlugs().isEmpty()) {
            return guidesByPreferredSlugs(metro.guideSlugs(), 5, null);
        }
        return guidesByPreferredSlugs(List.of(
                "how-we-verify-backflow-rules",
                "backflow-reporting-portals",
                "anniversary-date-vs-calendar-deadline",
                "approved-testers-vs-find-a-tester",
                "backflow-test-cost"
        ), 5, null);
    }

    private List<GuideRecord> supportGuidesForStateGuide(StateGuideRecord stateGuide) {
        return guidesByPreferredSlugs(List.of(
                "how-we-verify-backflow-rules",
                "residential-vs-commercial-backflow-rules",
                "anniversary-date-vs-calendar-deadline",
                "who-needs-a-backflow-preventer",
                "approved-testers-vs-find-a-tester",
                "backflow-test-cost"
        ), 6, null);
    }

    private List<GuideRecord> relatedGuidesForGuide(GuideRecord guide) {
        return guidesByPreferredSlugs(List.of(
                "how-we-verify-backflow-rules",
                "failed-backflow-test-next-steps",
                "approved-testers-vs-find-a-tester",
                "backflow-test-cost",
                "who-needs-a-backflow-preventer",
                "rpz-vs-dcva-vs-pvb",
                "backflow-reporting-portals",
                "anniversary-date-vs-calendar-deadline",
                "county-certified-vs-utility-approved-testers",
                "residential-vs-commercial-backflow-rules"
        ), 4, guide.slug());
    }

    private List<UtilityRecord> relatedUtilitiesForGuide(GuideRecord guide) {
        List<UtilityRecord> utilities = registryService.listPublishedUtilities();
        List<UtilityRecord> candidates = switch (guide.slug()) {
            case "approved-testers-vs-find-a-tester" -> utilities.stream()
                    .filter(utility -> testerPath(utility) != null)
                    .toList();
            case "backflow-reporting-portals" -> utilities.stream()
                    .filter(this::usesPortalWorkflow)
                    .toList();
            case "anniversary-date-vs-calendar-deadline" -> utilities.stream()
                    .filter(this::hasDateSpecificWorkflow)
                    .toList();
            case "county-certified-vs-utility-approved-testers" -> utilities.stream()
                    .filter(utility -> utility.supportsApprovedTestersPage() || utility.supportsFindATesterPage())
                    .filter(utility -> pathContainsAny(utility.approvedTesterListUrl(), "county", "health", "certified")
                            || utility.sources().stream().anyMatch(source -> pathContainsAny(source.url(), "county", "health", "certified"))
                            || utility.supportsFindATesterPage())
                    .toList();
            case "residential-vs-commercial-backflow-rules" -> utilities.stream()
                    .filter(this::hasResidentialCommercialSplit)
                    .toList();
            case "how-we-verify-backflow-rules" -> registryService.listPublishedStateGuides().stream()
                    .flatMap(stateGuide -> registryService.featuredUtilitiesForStateGuide(stateGuide).stream())
                    .collect(Collectors.toMap(
                            UtilityRecord::utilityId,
                            utility -> utility,
                            (left, right) -> left,
                            LinkedHashMap::new
                    ))
                    .values()
                    .stream()
                    .toList();
            case "failed-backflow-test-next-steps" -> utilities.stream()
                    .filter(utility -> !utility.failureHighlights().isEmpty())
                    .toList();
            case "rpz-vs-dcva-vs-pvb" -> utilities.stream()
                    .filter(utility -> utility.supportsIrrigationPage() || utility.supportsFireLinePage())
                    .toList();
            case "who-needs-a-backflow-preventer" -> utilities.stream()
                    .filter(this::hasResidentialCommercialSplit)
                    .toList();
            case "backflow-test-cost" -> utilities.stream()
                    .filter(utility -> utility.costBand() != null)
                    .toList();
            default -> utilities.stream()
                    .toList();
        };
        return balancedUtilities(candidates, 6);
    }

    private List<MetroRecord> relatedMetrosForUtilities(List<UtilityRecord> utilities) {
        List<String> utilityIds = utilities.stream()
                .map(UtilityRecord::utilityId)
                .toList();
        return registryService.listPublishedMetros().stream()
                .filter(metro -> metro.utilityIds().stream().anyMatch(utilityIds::contains))
                .sorted(Comparator.comparing(MetroRecord::title))
                .limit(4)
                .toList();
    }

    private List<StateGuideRecord> relatedStateGuidesForUtilities(List<UtilityRecord> utilities) {
        List<String> states = utilities.stream()
                .map(UtilityRecord::state)
                .distinct()
                .toList();
        return registryService.listPublishedStateGuides().stream()
                .filter(stateGuide -> states.contains(stateGuide.state()))
                .toList();
    }

    private List<GuideRecord> guidesByPreferredSlugs(List<String> preferredSlugs, int limit, String excludedSlug) {
        List<GuideRecord> publishedGuides = registryService.listPublishedGuides();
        List<GuideRecord> ordered = new ArrayList<>();
        for (String slug : preferredSlugs) {
            publishedGuides.stream()
                    .filter(guide -> guide.slug().equals(slug))
                    .filter(guide -> excludedSlug == null || !guide.slug().equals(excludedSlug))
                    .findFirst()
                    .ifPresent(ordered::add);
        }
        publishedGuides.stream()
                .filter(guide -> excludedSlug == null || !guide.slug().equals(excludedSlug))
                .filter(guide -> ordered.stream().noneMatch(existing -> existing.slug().equals(guide.slug())))
                .forEach(ordered::add);
        return ordered.stream().limit(limit).toList();
    }

    private List<FaqItem> utilityFaqItems(UtilityRecord utility) {
        List<FaqItem> items = new ArrayList<>();
        items.add(new FaqItem(
                "Does " + utility.utilityName() + " require annual backflow testing?",
                utility.testingFrequency() + ". " + utility.dueBasis()
        ));
        items.add(new FaqItem(
                "Who is affected by " + utility.utilityName() + " backflow rules?",
                utility.whoIsAffected()
        ));
        items.add(new FaqItem(
                "How do I submit or confirm a backflow test for " + utility.utilityName() + "?",
                submissionAnswer(utility)
        ));
        items.add(new FaqItem(
                "Where should I look for testers for " + utility.utilityName() + "?",
                testerAnswer(utility)
        ));
        return items;
    }

    private String submissionAnswer(UtilityRecord utility) {
        String methods = utility.submissionMethods().stream()
                .map(method -> method.label())
                .collect(Collectors.joining(", "));
        if (methods.isBlank()) {
            return "Start with the official utility page and confirm the current submission path directly with the program phone at " + utility.phone() + ".";
        }
        return "Use the official utility workflow and submission methods listed on this page: " + methods + ". Program phone: " + utility.phone() + ".";
    }

    private String testerAnswer(UtilityRecord utility) {
        if (utility.supportsApprovedTestersPage()) {
            return "Start with the governing authority's published tester list. This utility has an official approved-tester route and it should be treated as the primary source.";
        }
        if (utility.supportsFindATesterPage() && !registryService.findProvidersForUtility(utility.utilityId()).isEmpty()) {
            return "This utility does not publish an official list in the registry, so use the clearly labeled non-official find-a-tester route only after confirming the governing utility workflow.";
        }
        return "No public tester directory is live for this utility yet. Use the official utility page first and do not infer approval from a generic directory.";
    }

    private Map<String, Integer> providerCoverageCounts(MetroRecord metro, List<ProviderRecord> providers) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ProviderRecord provider : providers) {
            counts.put(provider.providerId(), (int) metro.utilityIds().stream()
                    .filter(provider::matchesUtility)
                    .count());
        }
        return counts;
    }

    private List<UtilityRecord> balancedUtilities(List<UtilityRecord> candidates, int limit) {
        Map<String, List<UtilityRecord>> byState = candidates.stream()
                .distinct()
                .sorted(Comparator.comparing(UtilityRecord::state).thenComparing(UtilityRecord::utilityName))
                .collect(Collectors.groupingBy(
                        UtilityRecord::state,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        Map<String, Integer> indexes = new LinkedHashMap<>();
        byState.keySet().forEach(state -> indexes.put(state, 0));

        List<UtilityRecord> results = new ArrayList<>();
        boolean added = true;
        while (results.size() < limit && added) {
            added = false;
            for (Map.Entry<String, List<UtilityRecord>> entry : byState.entrySet()) {
                int index = indexes.getOrDefault(entry.getKey(), 0);
                if (index >= entry.getValue().size()) {
                    continue;
                }
                results.add(entry.getValue().get(index));
                indexes.put(entry.getKey(), index + 1);
                added = true;
                if (results.size() >= limit) {
                    break;
                }
            }
        }
        return results;
    }

    private boolean usesPortalWorkflow(UtilityRecord utility) {
        StringBuilder text = new StringBuilder();
        append(text, utility.utilityUrl());
        append(text, utility.approvedTesterListUrl());
        append(text, utility.dueBasis());
        append(text, utility.verdictSummary());
        for (String step : utility.workflowSteps()) {
            append(text, step);
        }
        utility.submissionMethods().forEach(method -> {
            append(text, method.label());
            append(text, method.url());
            append(text, method.kind());
        });
        String value = text.toString().toLowerCase(Locale.US);
        return value.contains("portal")
                || value.contains("swift")
                || value.contains("bsi")
                || value.contains("backflowtest")
                || value.contains("customerportal")
                || value.contains("c3swift")
                || value.contains("online submission");
    }

    private boolean hasDateSpecificWorkflow(UtilityRecord utility) {
        StringBuilder text = new StringBuilder();
        append(text, utility.testingFrequency());
        append(text, utility.dueBasis());
        utility.workflowSteps().forEach(step -> append(text, step));
        utility.failureHighlights().forEach(highlight -> append(text, highlight));
        String value = text.toString().toLowerCase(Locale.US);
        return value.contains("anniversary")
                || value.contains("calendar")
                || value.contains("january")
                || value.contains("may ")
                || value.contains("july ")
                || value.contains("notice")
                || value.contains("due date")
                || value.contains("days before")
                || value.contains("30-day")
                || value.contains("60-day");
    }

    private boolean hasResidentialCommercialSplit(UtilityRecord utility) {
        if (!utility.residentialNotes().isEmpty() && !utility.commercialNotes().isEmpty()) {
            return true;
        }
        return utility.coveredPropertyTypes().stream()
                        .anyMatch(type -> pathContainsAny(type, "residential", "single-family", "homeowner"))
                && utility.coveredPropertyTypes().stream()
                        .anyMatch(type -> pathContainsAny(type, "commercial", "multifamily", "industrial", "restaurant", "managed"));
    }

    private boolean pathContainsAny(String value, String... keywords) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String lowered = value.toLowerCase(Locale.US);
        for (String keyword : keywords) {
            if (lowered.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void append(StringBuilder builder, String value) {
        if (value != null && !value.isBlank()) {
            builder.append(' ').append(value);
        }
    }

    private String faqStructuredData(List<FaqItem> faqItems) {
        StringBuilder json = new StringBuilder();
        json.append("{\"@context\":\"https://schema.org\",\"@type\":\"FAQPage\",\"mainEntity\":[");
        for (int i = 0; i < faqItems.size(); i++) {
            FaqItem item = faqItems.get(i);
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"@type\":\"Question\",\"name\":\"")
                    .append(jsonEscape(item.question()))
                    .append("\",\"acceptedAnswer\":{\"@type\":\"Answer\",\"text\":\"")
                    .append(jsonEscape(item.answer()))
                    .append("\"}}");
        }
        json.append("]}");
        return json.toString();
    }

    private String breadcrumbStructuredData(List<BreadcrumbItem> items) {
        StringBuilder json = new StringBuilder();
        json.append("{\"@context\":\"https://schema.org\",\"@type\":\"BreadcrumbList\",\"itemListElement\":[");
        for (int i = 0; i < items.size(); i++) {
            BreadcrumbItem item = items.get(i);
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"@type\":\"ListItem\",\"position\":")
                    .append(i + 1)
                    .append(",\"name\":\"")
                    .append(jsonEscape(item.name()))
                    .append("\",\"item\":\"")
                    .append(jsonEscape(item.url()))
                    .append("\"}");
        }
        json.append("]}");
        return json.toString();
    }

    private String combineStructuredData(String... jsonSnippets) {
        List<String> snippets = new ArrayList<>();
        for (String jsonSnippet : jsonSnippets) {
            if (jsonSnippet != null && !jsonSnippet.isBlank()) {
                snippets.add(jsonSnippet);
            }
        }
        if (snippets.isEmpty()) {
            return null;
        }
        if (snippets.size() == 1) {
            return snippets.get(0);
        }
        return "[" + String.join(",", snippets) + "]";
    }

    private String jsonEscape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", " ")
                .replace("\n", " ");
    }

    private String canonical(String path) {
        String configuredBaseUrl = siteProperties.baseUrl();
        String baseUrl = configuredBaseUrl == null ? "" : configuredBaseUrl.trim();
        if (baseUrl.isBlank()) {
            baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .build()
                    .toUriString();
        }
        return baseUrl.replaceAll("/+$", "") + path;
    }

    private String stateLabel(String state) {
        if ("texas".equalsIgnoreCase(state)) {
            return "Texas";
        }
        return state == null || state.isBlank()
                ? "State"
                : Character.toUpperCase(state.charAt(0)) + state.substring(1).toLowerCase();
    }

    private LocalDate homeLastModified() {
        LocalDate latest = LocalDate.of(2000, 1, 1);
        for (UtilityRecord utility : registryService.listPublishedUtilities()) {
            if (utility.lastVerified() != null && utility.lastVerified().isAfter(latest)) {
                latest = utility.lastVerified();
            }
        }
        for (GuideRecord guide : registryService.listPublishedGuides()) {
            if (guide.lastReviewed() != null && guide.lastReviewed().isAfter(latest)) {
                latest = guide.lastReviewed();
            }
        }
        for (StateGuideRecord stateGuide : registryService.listAllStateGuides()) {
            if (stateGuide.isPublishable(LocalDate.now())
                    && stateGuide.lastVerified() != null
                    && stateGuide.lastVerified().isAfter(latest)) {
                latest = stateGuide.lastVerified();
            }
        }
        return latest;
    }

    private record SitemapEntry(String url, LocalDate lastModified) {
    }

    private record BreadcrumbItem(String name, String url) {
    }

    private int activeSponsorCount(UtilityRecord utility) {
        return registryService.findActiveSponsorsForUtility(utility.utilityId()).size();
    }
}
