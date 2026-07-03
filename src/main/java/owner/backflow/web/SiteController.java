package owner.backflow.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
import owner.backflow.service.LeadRoutingService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class SiteController {
    private final BackflowRegistryService registryService;
    private final AppSiteProperties siteProperties;
    private final SiteVisibilityService siteVisibilityService;

    public SiteController(
            BackflowRegistryService registryService,
            AppSiteProperties siteProperties,
            SiteVisibilityService siteVisibilityService
    ) {
        this.registryService = registryService;
        this.siteProperties = siteProperties;
        this.siteVisibilityService = siteVisibilityService;
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
                "BackflowPath | Official backflow tester lists, portals, and local rules",
                "Find utility-specific backflow testing requirements, annual testing steps, reporting portals, failed-test guidance, and official tester list routes.",
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
                "State backflow guides | BackflowPath",
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
                "Metro backflow coverage | BackflowPath",
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
                "Backflow guides | BackflowPath",
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

    @GetMapping("/official-backflow-tester-lists")
    public String officialTesterListsPage(Model model) {
        List<UtilityRecord> utilities = officialTesterUtilities();
        Map<String, List<UtilityRecord>> utilitiesByState = utilitiesByState(utilities);
        model.addAttribute("page", page(
                "Official backflow tester lists by utility | BackflowPath",
                "Browse utility-published approved, registered, and certified backflow tester list routes without mixing them into non-official directories.",
                "/official-backflow-tester-lists",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("Official tester lists", canonical("/official-backflow-tester-lists"))
                ))
        ));
        model.addAttribute("utilities", utilities);
        model.addAttribute("utilitiesByState", utilitiesByState);
        model.addAttribute("stateLabels", stateLabelsFor(utilitiesByState.keySet()));
        model.addAttribute("stateGuides", registryService.listPublishedStateGuides());
        model.addAttribute("relatedGuides", guidesByPreferredSlugs(List.of(
                "approved-testers-vs-find-a-tester",
                "county-certified-vs-utility-approved-testers",
                "how-we-verify-backflow-rules"
        ), 3, null));
        return "pages/official-tester-lists";
    }

    @GetMapping("/backflow-reporting-portals")
    public String reportingPortalsPage(Model model) {
        List<UtilityRecord> utilities = portalUtilities("all");
        model.addAttribute("page", page(
                "Backflow reporting portals: BSI, SwiftComply, WEIRS, VEPO | BackflowPath",
                "Find which utilities route backflow test reports through BSI, SwiftComply, WEIRS, VEPO, Envirotrax, or local online submission portals.",
                "/backflow-reporting-portals",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("Reporting portals", canonical("/backflow-reporting-portals"))
                ))
        ));
        model.addAttribute("portalName", "Backflow reporting portals");
        model.addAttribute("portalSlug", "all");
        model.addAttribute("intro", "Use this page when a notice mentions BSI, SwiftComply, WEIRS, VEPO, Envirotrax, a customer account, or another online report submission workflow.");
        model.addAttribute("overview", true);
        model.addAttribute("utilities", utilities);
        model.addAttribute("portalCounts", portalCounts());
        model.addAttribute("cityAliasesByUtility", publishedCityAliasesByUtility(utilities));
        model.addAttribute("relatedGuides", guidesByPreferredSlugs(List.of(
                "backflow-test-notice-next-steps",
                "backflow-reporting-portals",
                "anniversary-date-vs-calendar-deadline",
                "approved-testers-vs-find-a-tester"
        ), 3, null));
        return "pages/portal-hub";
    }

    @GetMapping("/backflow-reporting-portals/{portalSlug}")
    public String reportingPortalDetailPage(@PathVariable String portalSlug, Model model) {
        if (!isSupportedPortalSlug(portalSlug)) {
            throw new NotFoundException("Reporting portal page not found.");
        }
        List<UtilityRecord> utilities = portalUtilities(portalSlug);
        if (utilities.isEmpty()) {
            throw new NotFoundException("Reporting portal page not available.");
        }
        String portalName = portalName(portalSlug);
        model.addAttribute("page", page(
                portalName + " backflow portal utilities and tester reports | BackflowPath",
                portalDescription(portalSlug),
                "/backflow-reporting-portals/" + portalSlug,
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("Reporting portals", canonical("/backflow-reporting-portals")),
                        new BreadcrumbItem(portalName, canonical("/backflow-reporting-portals/" + portalSlug))
                ))
        ));
        model.addAttribute("portalName", portalName);
        model.addAttribute("portalSlug", portalSlug);
        model.addAttribute("intro", portalDescription(portalSlug));
        model.addAttribute("overview", false);
        model.addAttribute("utilities", utilities);
        model.addAttribute("portalCounts", portalCounts());
        model.addAttribute("cityAliasesByUtility", publishedCityAliasesByUtility(utilities));
        model.addAttribute("relatedGuides", guidesByPreferredSlugs(List.of(
                "backflow-test-notice-next-steps",
                "backflow-reporting-portals",
                "how-we-verify-backflow-rules",
                "approved-testers-vs-find-a-tester"
        ), 3, null));
        return "pages/portal-hub";
    }

    @GetMapping("/privacy")
    public String privacyPage(Model model) {
        model.addAttribute("page", new PageMeta(
                "Privacy and request handling | BackflowPath",
                "How BackflowPath stores contact requests, preserves page context, and handles manual follow-up.",
                canonical("/privacy"),
                true
        ));
        return "pages/privacy";
    }

    @GetMapping("/about")
    public String aboutPage(Model model) {
        model.addAttribute("page", page(
                "About BackflowPath | BackflowPath",
                "How BackflowPath organizes utility-specific backflow compliance guidance without mixing authority rules and provider routing.",
                "/about",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("About", canonical("/about"))
                ))
        ));
        return "pages/about";
    }

    @GetMapping("/methodology")
    public String methodologyPage(Model model) {
        model.addAttribute("page", page(
                "Methodology | BackflowPath",
                "How BackflowPath verifies utility rules, freshness windows, reviewer codes, and source-backed updates.",
                "/methodology",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("Methodology", canonical("/methodology"))
                ))
        ));
        return "pages/methodology";
    }

    @GetMapping("/editorial-standards")
    public String editorialStandardsPage(Model model) {
        model.addAttribute("page", page(
                "Editorial standards | BackflowPath",
                "Editorial rules for separating official guidance, support content, and non-official provider directories on BackflowPath.",
                "/editorial-standards",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("Editorial standards", canonical("/editorial-standards"))
                ))
        ));
        return "pages/editorial-standards";
    }

    @GetMapping("/corrections")
    public String correctionsPage(Model model) {
        model.addAttribute("page", page(
                "Corrections policy | BackflowPath",
                "How BackflowPath handles reported errors, stale utility rules, broken source links, and public updates.",
                "/corrections",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("Corrections", canonical("/corrections"))
                ))
        ));
        return "pages/corrections";
    }

    @GetMapping("/contact")
    public String contactPage(Model model) {
        model.addAttribute("page", page(
                "Contact BackflowPath | BackflowPath",
                "Contact BackflowPath for corrections, source updates, and utility-specific routing questions.",
                "/contact",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("Contact", canonical("/contact"))
                ))
        ));
        return "pages/contact";
    }

    @GetMapping("/claim-listing")
    public String claimListingPage(Model model) {
        model.addAttribute("page", page(
                "Claim or correct a BackflowPath provider listing | BackflowPath",
                "Request a public provider listing correction, claim context, or official source update without changing the utility rule layer.",
                "/claim-listing",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("Claim listing", canonical("/claim-listing"))
                ))
        ));
        model.addAttribute("publicProviderCount", registryService.listPublicProviders().size());
        model.addAttribute("publishedUtilityCount", registryService.listPublishedUtilities().size());
        return "pages/claim-listing";
    }

    @GetMapping("/states/{state}/backflow-testing")
    public String stateGuidePage(@PathVariable String state, Model model) {
        StateGuideRecord stateGuide = registryService.findPublishedStateGuide(state)
                .orElseThrow(() -> new NotFoundException("State guide not found."));
        List<UtilityRecord> utilities = registryService.listPublishedUtilitiesForState(state);
        model.addAttribute("page", page(
                stateGuide.title() + " | BackflowPath",
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
        model.addAttribute("cityAliases", publishedCityAliasesForState(stateGuide.state()));
        model.addAttribute("guides", supportGuidesForStateGuide(stateGuide));
        return "pages/state-guide";
    }

    @GetMapping("/states/{state}/approved-backflow-testers")
    public String stateApprovedTesterListsPage(@PathVariable String state, Model model) {
        StateGuideRecord stateGuide = registryService.findPublishedStateGuide(state)
                .orElseThrow(() -> new NotFoundException("State guide not found."));
        List<UtilityRecord> utilities = officialTesterUtilities().stream()
                .filter(utility -> utility.state().equalsIgnoreCase(state))
                .toList();
        if (utilities.isEmpty()) {
            throw new NotFoundException("State approved tester page not available.");
        }
        String label = stateLabel(stateGuide.state());
        model.addAttribute("page", page(
                label + " approved backflow tester lists by utility | BackflowPath",
                "Browse " + label + " utility pages with official approved, certified, or registered backflow tester list routes.",
                "/states/" + stateGuide.state() + "/approved-backflow-testers",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem(label, canonical("/states/" + stateGuide.state() + "/backflow-testing")),
                        new BreadcrumbItem("Approved tester lists", canonical("/states/" + stateGuide.state() + "/approved-backflow-testers"))
                ))
        ));
        model.addAttribute("stateGuide", stateGuide);
        model.addAttribute("stateLabel", label);
        model.addAttribute("utilities", utilities);
        model.addAttribute("relatedGuides", guidesByPreferredSlugs(List.of(
                "approved-testers-vs-find-a-tester",
                "county-certified-vs-utility-approved-testers",
                "backflow-reporting-portals"
        ), 3, null));
        return "pages/state-approved-testers";
    }

    @GetMapping("/guides/{slug}")
    public String guidePage(@PathVariable String slug, Model model) {
        GuideRecord guide = registryService.findPublishedGuide(slug)
                .orElseThrow(() -> new NotFoundException("Guide page not found."));
        List<UtilityRecord> relatedUtilities = relatedUtilitiesForGuide(guide);
        List<MetroRecord> relatedMetros = relatedMetrosForUtilities(relatedUtilities);
        List<StateGuideRecord> stateGuides = relatedStateGuidesForUtilities(relatedUtilities);
        model.addAttribute("page", page(
                guide.title() + " | BackflowPath",
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
                metro.title() + " | BackflowPath",
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
        model.addAttribute("cityAliasesByName", publishedCityAliasesByNameForState(metro.state()));
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
        List<String> coverageStates = providerCoverageStates(utilities);
        List<String> coverageCities = providerCoverageCities(utilities);
        List<String> coverageCounties = providerCoverageCounties(utilities);
        int providerOfficialRouteCount = providerOfficialRouteCount(utilities);
        int providerDirectoryRouteCount = providerDirectoryRouteCount(utilities);
        int providerSubmissionWorkflowCount = providerSubmissionWorkflowCount(utilities);
        model.addAttribute("page", page(
                provider.providerName() + " | BackflowPath",
                provider.pageLabel(),
                providerPath(provider),
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem("Providers", canonical("/")),
                                new BreadcrumbItem(provider.providerName(), canonical(providerPath(provider)))
                        )),
                        providerStructuredData(provider, utilities, coverageStates, coverageCities, coverageCounties)
                )
        ));
        model.addAttribute("provider", provider);
        model.addAttribute("utilities", utilities);
        model.addAttribute("metros", metros);
        model.addAttribute("coverageStates", coverageStates);
        model.addAttribute("coverageCities", coverageCities);
        model.addAttribute("coverageCounties", coverageCounties);
        model.addAttribute("providerServiceTypes", providerServiceTypes(utilities));
        model.addAttribute("providerOfficialRouteCount", providerOfficialRouteCount);
        model.addAttribute("providerDirectoryRouteCount", providerDirectoryRouteCount);
        model.addAttribute("providerSubmissionWorkflowCount", providerSubmissionWorkflowCount);
        model.addAttribute("latestUtilityVerification", providerLatestUtilityVerification(utilities));
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
                utilityPageTitle(utility),
                utilityPageDescription(utility),
                utilityPath(utility),
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                                new BreadcrumbItem(utility.utilityName(), canonical(utilityPath(utility)))
                        )),
                        faqStructuredData(faqItems)
                )
        ).withRequestHelpPath(LeadRoutingService.requestHelpPath(
                utility.utilityId(),
                utilityPath(utility),
                "general-testing",
                "utility"
        )));
        model.addAttribute("utility", utility);
        model.addAttribute("annualTestingPath", utility.supportsAnnualTestingPage() ? utilityPath(utility) + "annual-testing" : null);
        model.addAttribute("irrigationPath", utility.supportsIrrigationPage() ? utilityPath(utility) + "irrigation" : null);
        model.addAttribute("fireLinePath", utility.supportsFireLinePage() ? utilityPath(utility) + "fire-line" : null);
        model.addAttribute("testerPath", testerPath(utility));
        model.addAttribute("testerLabel", testerLabel(utility));
        model.addAttribute("portalHubPath", portalHubPath(utility));
        model.addAttribute("portalHubLabel", portalHubLabel(utility));
        model.addAttribute("cityAliases", publishedCityAliasesForUtility(utility.utilityId()));
        model.addAttribute("faqItems", faqItems);
        model.addAttribute("stateGuide", registryService.findPublishedStateGuide(utility.state()).orElse(null));
        model.addAttribute("metros", registryService.listPublishedMetrosForUtility(utility.utilityId()));
        model.addAttribute("relatedGuides", utilitySupportGuides(utility));
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
                utility.utilityName() + " failed backflow test | BackflowPath",
                "Repair, retest, and submission next steps for a failed backflow test in " + utility.utilityName() + ".",
                utilityPath(utility) + "failed-test",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                        new BreadcrumbItem(utility.utilityName(), canonical(utilityPath(utility))),
                        new BreadcrumbItem("Failed test", canonical(utilityPath(utility) + "failed-test"))
                ))
        ).withRequestHelpPath(LeadRoutingService.requestHelpPath(
                utility.utilityId(),
                utilityPath(utility) + "failed-test",
                "failed-test-repair",
                "failed-test"
        )));
        model.addAttribute("utility", utility);
        model.addAttribute("failedGuide", registryService.findPublishedGuide("failed-backflow-test-next-steps").orElse(null));
        model.addAttribute("testerPath", testerPath(utility));
        model.addAttribute("testerLabel", testerLabel(utility));
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
                officialTesterPageTitle(utility) + " | BackflowPath",
                officialTesterPageDescription(utility),
                utilityPath(utility) + "approved-testers",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                        new BreadcrumbItem(utility.utilityName(), canonical(utilityPath(utility))),
                        new BreadcrumbItem("Approved testers", canonical(utilityPath(utility) + "approved-testers"))
                ))
        ).withRequestHelpPath(LeadRoutingService.requestHelpPath(
                utility.utilityId(),
                utilityPath(utility) + "approved-testers",
                "tester-search",
                "tester-directory"
        )));
        model.addAttribute("utility", utility);
        model.addAttribute("providers", providers);
        model.addAttribute("official", true);
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
                utility.utilityName() + " find a tester | BackflowPath",
                "Non-official provider directory for " + utility.utilityName() + ", kept separate from authority guidance.",
                utilityPath(utility) + "find-a-tester",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                        new BreadcrumbItem(utility.utilityName(), canonical(utilityPath(utility))),
                        new BreadcrumbItem("Find a tester", canonical(utilityPath(utility) + "find-a-tester"))
                ))
        ).withRequestHelpPath(LeadRoutingService.requestHelpPath(
                utility.utilityId(),
                utilityPath(utility) + "find-a-tester",
                "tester-search",
                "tester-directory"
        )));
        model.addAttribute("utility", utility);
        model.addAttribute("providers", providers);
        model.addAttribute("official", false);
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
    public String cityAliasPage(
            @PathVariable String state,
            @PathVariable String citySlug,
            Model model
    ) {
        CityAliasRecord alias = registryService.findCityAlias(state, citySlug)
                .orElseThrow(() -> new NotFoundException("City alias not found."));
        UtilityRecord utility = registryService.findUtilityById(alias.utilityId())
                .orElseThrow(() -> new NotFoundException("Mapped utility is not available."));

        String path = cityPath(alias);
        model.addAttribute("page", new PageMeta(
                cityPageTitle(alias, utility),
                cityPageDescription(alias, utility),
                canonical(path),
                alias.aliasMode() == AliasMode.NOINDEX_BRIDGE,
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                        new BreadcrumbItem(alias.city(), canonical(path))
                ))
        ).withRequestHelpPath(LeadRoutingService.requestHelpPath(
                utility.utilityId(),
                path,
                "general-testing",
                "city"
        )));
        model.addAttribute("alias", alias);
        model.addAttribute("utility", utility);
        model.addAttribute("annualTestingPath", utility.supportsAnnualTestingPage() ? utilityPath(utility) + "annual-testing" : null);
        model.addAttribute("irrigationPath", utility.supportsIrrigationPage() ? utilityPath(utility) + "irrigation" : null);
        model.addAttribute("fireLinePath", utility.supportsFireLinePage() ? utilityPath(utility) + "fire-line" : null);
        model.addAttribute("failedTestPath", utilityPath(utility) + "failed-test");
        model.addAttribute("testerPath", testerPath(utility));
        model.addAttribute("testerLabel", testerLabel(utility));
        model.addAttribute("portalHubPath", portalHubPath(utility));
        model.addAttribute("portalHubLabel", portalHubLabel(utility));
        model.addAttribute("usesPortalWorkflow", usesPortalWorkflow(utility));
        model.addAttribute("providers", registryService.findProvidersForUtility(utility.utilityId()));
        model.addAttribute("relatedGuides", utilitySupportGuides(utility));
        return "pages/city-bridge";
    }

    @GetMapping("/cities/{state}/{citySlug}/{intentSlug}")
    public String cityIntentPage(
            @PathVariable String state,
            @PathVariable String citySlug,
            @PathVariable String intentSlug,
            Model model
    ) {
        CityAliasRecord alias = registryService.findCityAlias(state, citySlug)
                .orElseThrow(() -> new NotFoundException("City intent page not found."));
        UtilityRecord utility = registryService.findUtilityById(alias.utilityId())
                .orElseThrow(() -> new NotFoundException("Mapped utility is not available."));
        CityIntentConfig intent = cityIntentConfig(intentSlug, alias, utility);
        if (intent == null) {
            throw new NotFoundException("City intent page not available.");
        }

        String path = cityIntentPath(alias, intent.slug());
        model.addAttribute("page", new PageMeta(
                intent.title(),
                intent.description(),
                canonical(path),
                alias.aliasMode() == AliasMode.NOINDEX_BRIDGE,
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                                new BreadcrumbItem(alias.city(), canonical(cityPath(alias))),
                                new BreadcrumbItem(intent.heading(), canonical(path))
                        )),
                        faqStructuredData(utilityFaqItems(utility))
                )
        ).withRequestHelpPath(LeadRoutingService.requestHelpPath(
                utility.utilityId(),
                path,
                intent.slug(),
                "city-intent"
        )));
        model.addAttribute("alias", alias);
        model.addAttribute("utility", utility);
        model.addAttribute("eyebrow", intent.eyebrow());
        model.addAttribute("heading", intent.heading());
        model.addAttribute("intro", intent.intro());
        model.addAttribute("highlights", intent.highlights());
        model.addAttribute("workflowSteps", intent.workflowSteps());
        model.addAttribute("primaryPath", intent.primaryPath());
        model.addAttribute("primaryLabel", intent.primaryLabel());
        model.addAttribute("utilityPath", utilityPath(utility));
        model.addAttribute("failedTestPath", utilityPath(utility) + "failed-test");
        model.addAttribute("testerPath", testerPath(utility));
        model.addAttribute("testerLabel", testerLabel(utility));
        model.addAttribute("portalHubPath", portalHubPath(utility));
        model.addAttribute("portalHubLabel", portalHubLabel(utility));
        model.addAttribute("relatedGuides", guidesByPreferredSlugs(intent.guideSlugs(), 4, null));
        return "pages/city-intent-page";
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap(HttpServletRequest request) {
        if (siteVisibilityService.shouldForceNoindex(request)) {
            return emptySitemap();
        }
        List<SitemapEntry> urls = new ArrayList<>();
        urls.add(new SitemapEntry(canonical("/"), homeLastModified()));
        urls.add(new SitemapEntry(canonical("/about"), homeLastModified()));
        urls.add(new SitemapEntry(canonical("/methodology"), homeLastModified()));
        urls.add(new SitemapEntry(canonical("/editorial-standards"), homeLastModified()));
        urls.add(new SitemapEntry(canonical("/corrections"), homeLastModified()));
        urls.add(new SitemapEntry(canonical("/contact"), homeLastModified()));
        urls.add(new SitemapEntry(canonical("/claim-listing"), homeLastModified()));
        List<UtilityRecord> officialTesterUtilities = officialTesterUtilities();
        urls.add(new SitemapEntry(
                canonical("/official-backflow-tester-lists"),
                latestUtilityModified(officialTesterUtilities)
        ));
        urls.add(new SitemapEntry(
                canonical("/backflow-reporting-portals"),
                latestUtilityModified(portalUtilities("all"))
        ));
        for (String portalSlug : List.of("bsi", "weirs", "swiftcomply", "vepo")) {
            List<UtilityRecord> portalUtilities = portalUtilities(portalSlug);
            if (!portalUtilities.isEmpty()) {
                urls.add(new SitemapEntry(
                        canonical("/backflow-reporting-portals/" + portalSlug),
                        latestUtilityModified(portalUtilities)
                ));
            }
        }
        utilitiesByState(officialTesterUtilities).forEach((state, utilities) -> urls.add(new SitemapEntry(
                canonical("/states/" + state + "/approved-backflow-testers"),
                latestUtilityModified(utilities)
        )));
        for (CityAliasRecord alias : registryService.listCityAliases()) {
            if (alias.aliasMode() == AliasMode.NOINDEX_BRIDGE || registryService.findUtilityById(alias.utilityId()).isEmpty()) {
                continue;
            }
            urls.add(new SitemapEntry(canonical(cityPath(alias)), alias.lastReviewed()));
            registryService.findUtilityById(alias.utilityId()).ifPresent(utility ->
                    cityIntentConfigs(alias, utility).forEach(intent -> urls.add(new SitemapEntry(
                            canonical(cityIntentPath(alias, intent.slug())),
                            latestDate(alias.lastReviewed(), utility.lastVerified())
                    )))
            );
        }
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
    public String robots(HttpServletRequest request) {
        if (siteVisibilityService.shouldForceNoindex(request)) {
            return siteVisibilityService.stagingRobotsTxt();
        }
        return "User-agent: *\n"
                + "Allow: /\n\n"
                + "Sitemap: " + canonical("/sitemap.xml") + "\n";
    }

    private String emptySitemap() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"></urlset>";
    }

    private PageMeta page(String title, String description, String path) {
        return page(title, description, path, null);
    }

    private PageMeta page(String title, String description, String path, String structuredDataJson) {
        return new PageMeta(title, description, canonical(path), false, structuredDataJson);
    }

    private List<UtilityRecord> officialTesterUtilities() {
        return registryService.listPublishedUtilities().stream()
                .filter(UtilityRecord::supportsApprovedTestersPage)
                .sorted(Comparator.comparing(UtilityRecord::state).thenComparing(UtilityRecord::utilityName))
                .toList();
    }

    private Map<String, List<UtilityRecord>> utilitiesByState(List<UtilityRecord> utilities) {
        return utilities.stream()
                .sorted(Comparator.comparing(UtilityRecord::state).thenComparing(UtilityRecord::utilityName))
                .collect(Collectors.groupingBy(
                        UtilityRecord::state,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private Map<String, String> stateLabelsFor(Set<String> states) {
        Map<String, String> labels = new LinkedHashMap<>();
        states.stream()
                .sorted()
                .forEach(state -> labels.put(state, stateLabel(state)));
        return labels;
    }

    private List<UtilityRecord> portalUtilities(String portalSlug) {
        return registryService.listPublishedUtilities().stream()
                .filter(utility -> switch (portalSlug.toLowerCase(Locale.US)) {
                    case "all" -> usesPortalWorkflow(utility);
                    case "bsi" -> utilityContainsAny(utility, "bsi", "backflow solutions", "backflowtest.com", "bsi online");
                    case "weirs" -> utilityContainsAny(utility, "weirs", "water environmental inspection reporting system");
                    case "swiftcomply" -> utilityContainsAny(utility, "swiftcomply", "c3swift", "swift comply");
                    case "vepo" -> utilityContainsAny(utility, "vepo", "envirotrax");
                    default -> false;
                })
                .sorted(Comparator.comparing(UtilityRecord::state).thenComparing(UtilityRecord::utilityName))
                .toList();
    }

    private Map<String, Integer> portalCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("bsi", portalUtilities("bsi").size());
        counts.put("weirs", portalUtilities("weirs").size());
        counts.put("swiftcomply", portalUtilities("swiftcomply").size());
        counts.put("vepo", portalUtilities("vepo").size());
        return counts;
    }

    private Map<String, List<CityAliasRecord>> publishedCityAliasesByUtility(List<UtilityRecord> utilities) {
        Set<String> utilityIds = utilities.stream()
                .map(UtilityRecord::utilityId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return registryService.listCityAliases().stream()
                .filter(alias -> alias.aliasMode() != AliasMode.NOINDEX_BRIDGE)
                .filter(alias -> utilityIds.contains(alias.utilityId()))
                .filter(alias -> registryService.findUtilityById(alias.utilityId()).isPresent())
                .collect(Collectors.groupingBy(
                        CityAliasRecord::utilityId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private Map<String, CityAliasRecord> publishedCityAliasesByNameForState(String state) {
        return registryService.listCityAliasesForState(state).stream()
                .filter(alias -> alias.aliasMode() != AliasMode.NOINDEX_BRIDGE)
                .filter(alias -> registryService.findUtilityById(alias.utilityId()).isPresent())
                .collect(Collectors.toMap(
                        CityAliasRecord::city,
                        alias -> alias,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private boolean isSupportedPortalSlug(String portalSlug) {
        return "bsi".equalsIgnoreCase(portalSlug)
                || "weirs".equalsIgnoreCase(portalSlug)
                || "swiftcomply".equalsIgnoreCase(portalSlug)
                || "vepo".equalsIgnoreCase(portalSlug);
    }

    private String portalName(String portalSlug) {
        return switch (portalSlug.toLowerCase(Locale.US)) {
            case "bsi" -> "BSI";
            case "weirs" -> "WEIRS";
            case "swiftcomply" -> "SwiftComply";
            case "vepo" -> "VEPO/Envirotrax";
            default -> "Backflow reporting portals";
        };
    }

    private String portalDescription(String portalSlug) {
        return switch (portalSlug.toLowerCase(Locale.US)) {
            case "bsi" -> "Find utility pages where BSI Online or Backflow Solutions appears in the official backflow test report, tester enrollment, or submission workflow.";
            case "weirs" -> "Find utility pages where WEIRS appears in the official backflow tester lookup, water inspection, or report submission workflow.";
            case "swiftcomply" -> "Find utility pages where SwiftComply or C3Swift appears in the official backflow report submission workflow.";
            case "vepo" -> "Find utility pages where VEPO or Envirotrax appears in the official backflow tester registration, credential verification, or report submission workflow.";
            default -> "Find utility backflow reporting portal routes and online submission workflows.";
        };
    }

    private String portalHubPath(UtilityRecord utility) {
        String portalSlug = portalSlugForUtility(utility);
        return portalSlug == null ? null : "/backflow-reporting-portals/" + portalSlug;
    }

    private String portalHubLabel(UtilityRecord utility) {
        String portalSlug = portalSlugForUtility(utility);
        if (portalSlug == null) {
            return null;
        }
        return "Compare " + portalName(portalSlug) + " portal utilities";
    }

    private String portalSlugForUtility(UtilityRecord utility) {
        if (utilityContainsAny(utility, "weirs", "water environmental inspection reporting system")) {
            return "weirs";
        }
        if (utilityContainsAny(utility, "swiftcomply", "c3swift", "swift comply")) {
            return "swiftcomply";
        }
        if (utilityContainsAny(utility, "vepo", "envirotrax")) {
            return "vepo";
        }
        if (utilityContainsAny(utility, "bsi", "backflow solutions", "backflowtest.com", "bsi online")) {
            return "bsi";
        }
        return null;
    }

    private String utilityPageTitle(UtilityRecord utility) {
        if (utility.supportsApprovedTestersPage() && usesPortalWorkflow(utility)) {
            return utility.utilityName() + " backflow testing, portal, and official tester list | BackflowPath";
        }
        if (utility.supportsApprovedTestersPage()) {
            return utility.utilityName() + " backflow testing and official tester list | BackflowPath";
        }
        if (usesPortalWorkflow(utility)) {
            return utility.utilityName() + " backflow testing and reporting portal | BackflowPath";
        }
        return utility.utilityName() + " backflow testing requirements | BackflowPath";
    }

    private String utilityPageDescription(UtilityRecord utility) {
        StringBuilder description = new StringBuilder(utility.verdictSummary());
        if (usesPortalWorkflow(utility)) {
            description.append(" Includes reporting portal and submission workflow context.");
        }
        if (utility.supportsApprovedTestersPage()) {
            description.append(" Includes the official tester list route.");
        } else if (utility.supportsFindATesterPage()) {
            description.append(" Includes a clearly labeled non-official tester route when provider inventory is available.");
        }
        return description.toString();
    }

    private String officialTesterPageTitle(UtilityRecord utility) {
        if (utilityContainsAny(utility, "weirs")) {
            return utility.utilityName() + " WEIRS registered backflow tester list";
        }
        if (utilityContainsAny(utility, "bsi", "backflowtest.com", "backflow solutions")) {
            return utility.utilityName() + " BSI backflow tester route";
        }
        if (utilityContainsAny(utility, "vepo", "envirotrax")) {
            return utility.utilityName() + " VEPO registered backflow tester list";
        }
        if (utilityContainsAny(utility, "certified")) {
            return utility.utilityName() + " certified backflow tester list";
        }
        if (utilityContainsAny(utility, "registered")) {
            return utility.utilityName() + " registered backflow tester list";
        }
        return utility.utilityName() + " official backflow tester list";
    }

    private String officialTesterPageDescription(UtilityRecord utility) {
        return "Open the utility-published tester route for " + utility.utilityName()
                + " and keep it separate from non-official provider directory options.";
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

    private String cityPath(CityAliasRecord alias) {
        return "/cities/" + alias.state() + "/" + alias.aliasSlug() + "/backflow-testing";
    }

    private String cityIntentPath(CityAliasRecord alias, String intentSlug) {
        return "/cities/" + alias.state() + "/" + alias.aliasSlug() + "/" + intentSlug;
    }

    private String cityPageTitle(CityAliasRecord alias, UtilityRecord utility) {
        if (utility.supportsApprovedTestersPage() && usesPortalWorkflow(utility)) {
            return alias.city() + " backflow testing, reporting portal, and approved testers | BackflowPath";
        }
        if (utility.supportsApprovedTestersPage()) {
            return alias.city() + " backflow testing and approved tester list | BackflowPath";
        }
        if (usesPortalWorkflow(utility)) {
            return alias.city() + " backflow testing and reporting portal | BackflowPath";
        }
        return alias.city() + " backflow testing and prevention requirements | BackflowPath";
    }

    private String cityPageDescription(CityAliasRecord alias, UtilityRecord utility) {
        StringBuilder description = new StringBuilder();
        description.append(alias.city())
                .append(" backflow testing route mapped to ")
                .append(utility.utilityName())
                .append(": ")
                .append(utility.testingFrequency())
                .append(" ")
                .append(utility.dueBasis());
        if (utility.supportsApprovedTestersPage()) {
            description.append(" Includes the official tester list route.");
        }
        if (usesPortalWorkflow(utility)) {
            description.append(" Includes reporting portal context.");
        }
        return description.toString();
    }

    private List<CityIntentConfig> cityIntentConfigs(CityAliasRecord alias, UtilityRecord utility) {
        return List.of(
                        "annual-backflow-testing",
                        "backflow-reporting-portal",
                        "approved-backflow-testers",
                        "failed-backflow-test",
                        "irrigation-backflow-testing",
                        "fire-line-backflow-testing"
                )
                .stream()
                .map(slug -> cityIntentConfig(slug, alias, utility))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private CityIntentConfig cityIntentConfig(String intentSlug, CityAliasRecord alias, UtilityRecord utility) {
        String slug = intentSlug == null ? "" : intentSlug.toLowerCase(Locale.US);
        return switch (slug) {
            case "annual-backflow-testing" -> annualCityIntent(alias, utility);
            case "backflow-reporting-portal" -> portalCityIntent(alias, utility);
            case "approved-backflow-testers" -> approvedTesterCityIntent(alias, utility);
            case "failed-backflow-test" -> failedTestCityIntent(alias, utility);
            case "irrigation-backflow-testing" -> irrigationCityIntent(alias, utility);
            case "fire-line-backflow-testing" -> fireLineCityIntent(alias, utility);
            default -> null;
        };
    }

    private CityIntentConfig annualCityIntent(CityAliasRecord alias, UtilityRecord utility) {
        if (!utility.supportsAnnualTestingPage()) {
            return null;
        }
        UtilityFocusContent focus = utility.resolvedAnnualTesting();
        String heading = alias.city() + " annual backflow testing";
        return new CityIntentConfig(
                "annual-backflow-testing",
                alias.city() + " annual backflow testing and due dates | BackflowPath",
                alias.city() + " annual backflow testing route mapped to " + utility.utilityName() + ": " + focus.summary(),
                "Annual city route",
                heading,
                focus.summary(),
                focus.highlights(),
                focus.workflowSteps(),
                utilityPath(utility) + "annual-testing",
                "Open annual testing workflow",
                List.of("backflow-test-notice-next-steps", "anniversary-date-vs-calendar-deadline", "backflow-test-cost", "how-we-verify-backflow-rules")
        );
    }

    private CityIntentConfig portalCityIntent(CityAliasRecord alias, UtilityRecord utility) {
        if (!usesPortalWorkflow(utility)) {
            return null;
        }
        List<String> highlights = new ArrayList<>();
        utility.submissionMethods().forEach(method -> highlights.add(method.label() + " - " + method.kind()));
        highlights.add("Due basis: " + utility.dueBasis());
        highlights.add("Program phone: " + utility.phone());
        String portalLabel = portalSlugForUtility(utility) == null ? "reporting portal" : portalName(portalSlugForUtility(utility));
        String heading = alias.city() + " backflow reporting portal";
        return new CityIntentConfig(
                "backflow-reporting-portal",
                alias.city() + " backflow reporting portal and test reports | BackflowPath",
                "Find the " + alias.city() + " backflow report submission route through " + utility.utilityName() + ", including " + portalLabel + " context when the utility workflow names a portal.",
                "Portal city route",
                heading,
                "Use this page when a notice for " + alias.city() + " mentions BSI, SwiftComply, WEIRS, VEPO, a customer portal, or online backflow test report submission.",
                highlights,
                utility.workflowSteps(),
                portalHubPath(utility) == null ? utilityPath(utility) : portalHubPath(utility),
                portalHubLabel(utility) == null ? "Open utility submission workflow" : portalHubLabel(utility),
                List.of("backflow-test-notice-next-steps", "backflow-reporting-portals", "approved-testers-vs-find-a-tester", "backflow-test-cost")
        );
    }

    private CityIntentConfig approvedTesterCityIntent(CityAliasRecord alias, UtilityRecord utility) {
        if (!utility.supportsApprovedTestersPage()) {
            return null;
        }
        List<String> highlights = new ArrayList<>();
        if (utility.officialListLabel() != null && !utility.officialListLabel().isBlank()) {
            highlights.add(utility.officialListLabel());
        }
        highlights.add("Confirm tester status on the governing list before treating a provider as approved.");
        highlights.add("Use the utility workflow for deadlines, report acceptance, and submission requirements.");
        String heading = alias.city() + " approved backflow testers";
        return new CityIntentConfig(
                "approved-backflow-testers",
                alias.city() + " approved backflow testers and utility list | BackflowPath",
                "Find the " + alias.city() + " approved backflow tester route mapped to " + utility.utilityName() + " without mixing official lists with non-official directories.",
                "Tester city route",
                heading,
                "Use this page when the search or notice says approved, certified, registered, or authorized backflow tester for " + alias.city() + ".",
                highlights,
                utility.workflowSteps(),
                testerPath(utility),
                testerLabel(utility),
                List.of("backflow-test-notice-next-steps", "approved-testers-vs-find-a-tester", "county-certified-vs-utility-approved-testers", "how-we-verify-backflow-rules")
        );
    }

    private CityIntentConfig failedTestCityIntent(CityAliasRecord alias, UtilityRecord utility) {
        if (utility.failureHighlights().isEmpty()) {
            return null;
        }
        String heading = alias.city() + " failed backflow test";
        return new CityIntentConfig(
                "failed-backflow-test",
                alias.city() + " failed backflow test repair and retest | BackflowPath",
                "Repair, retest, and report-submission route for a failed backflow test in " + alias.city() + " through " + utility.utilityName() + ".",
                "Failed-test city route",
                heading,
                "Use this page when the assembly already failed and the next step is repair, retest, and accepted report submission.",
                utility.failureHighlights(),
                utility.workflowSteps(),
                utilityPath(utility) + "failed-test",
                "Open failed-test workflow",
                List.of("backflow-test-notice-next-steps", "failed-backflow-test-next-steps", "backflow-test-cost", "backflow-reporting-portals")
        );
    }

    private CityIntentConfig irrigationCityIntent(CityAliasRecord alias, UtilityRecord utility) {
        if (!utility.supportsIrrigationPage()) {
            return null;
        }
        UtilityFocusContent focus = utility.irrigation();
        String heading = alias.city() + " irrigation backflow testing";
        return new CityIntentConfig(
                "irrigation-backflow-testing",
                alias.city() + " irrigation backflow testing rules | BackflowPath",
                "Irrigation backflow testing route for " + alias.city() + " mapped to " + utility.utilityName() + ": " + focus.summary(),
                "Irrigation city route",
                heading,
                focus.summary(),
                focus.highlights(),
                focus.workflowSteps(),
                utilityPath(utility) + "irrigation",
                "Open irrigation workflow",
                List.of("backflow-test-notice-next-steps", "rpz-vs-dcva-vs-pvb", "residential-vs-commercial-backflow-rules", "backflow-test-cost")
        );
    }

    private CityIntentConfig fireLineCityIntent(CityAliasRecord alias, UtilityRecord utility) {
        if (!utility.supportsFireLinePage()) {
            return null;
        }
        UtilityFocusContent focus = utility.fireLine();
        String heading = alias.city() + " fire-line backflow testing";
        return new CityIntentConfig(
                "fire-line-backflow-testing",
                alias.city() + " fire-line backflow testing rules | BackflowPath",
                "Fire-line backflow testing route for " + alias.city() + " mapped to " + utility.utilityName() + ": " + focus.summary(),
                "Fire-line city route",
                heading,
                focus.summary(),
                focus.highlights(),
                focus.workflowSteps(),
                utilityPath(utility) + "fire-line",
                "Open fire-line workflow",
                List.of("backflow-test-notice-next-steps", "rpz-vs-dcva-vs-pvb", "failed-backflow-test-next-steps", "backflow-test-cost")
        );
    }

    private List<CityAliasRecord> publishedCityAliasesForState(String state) {
        return registryService.listCityAliasesForState(state).stream()
                .filter(alias -> alias.aliasMode() != AliasMode.NOINDEX_BRIDGE)
                .filter(alias -> registryService.findUtilityById(alias.utilityId()).isPresent())
                .toList();
    }

    private List<CityAliasRecord> publishedCityAliasesForUtility(String utilityId) {
        return registryService.listCityAliases().stream()
                .filter(alias -> alias.aliasMode() != AliasMode.NOINDEX_BRIDGE)
                .filter(alias -> alias.utilityId().equals(utilityId))
                .filter(alias -> registryService.findUtilityById(alias.utilityId()).isPresent())
                .toList();
    }

    private LocalDate latestUtilityModified(List<UtilityRecord> utilities) {
        return utilities.stream()
                .map(UtilityRecord::lastVerified)
                .filter(java.util.Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(homeLastModified());
    }

    private LocalDate latestDate(LocalDate left, LocalDate right) {
        if (left == null) {
            return right == null ? homeLastModified() : right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
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
        String requestHelpPath = LeadRoutingService.requestHelpPath(
                utility.utilityId(),
                path,
                eyebrow.toLowerCase(Locale.ROOT).replace(" ", "-"),
                "utility-focus"
        );
        model.addAttribute("page", page(
                titleStem + " | BackflowPath",
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
        ).withRequestHelpPath(requestHelpPath));
        model.addAttribute("utility", utility);
        model.addAttribute("eyebrow", eyebrow);
        model.addAttribute("heading", titleStem);
        model.addAttribute("intro", description);
        model.addAttribute("focus", focus);
        model.addAttribute("residentialNotes", utility.residentialNotes());
        model.addAttribute("commercialNotes", utility.commercialNotes());
        model.addAttribute("testerPath", testerPath(utility));
        model.addAttribute("testerLabel", testerLabel(utility));
        model.addAttribute("requestHelpPath", requestHelpPath);
        model.addAttribute("faqItems", utilityFaqItems(utility));
        model.addAttribute("stateGuide", registryService.findPublishedStateGuide(utility.state()).orElse(null));
        model.addAttribute("relatedGuides", utilitySupportGuides(utility));
        return "pages/utility-focus-page";
    }

    private List<GuideRecord> utilitySupportGuides(UtilityRecord utility) {
        List<String> preferred = new ArrayList<>(List.of(
                "backflow-test-notice-next-steps",
                "how-we-verify-backflow-rules"
        ));
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

    private List<String> providerCoverageStates(List<UtilityRecord> utilities) {
        return utilities.stream()
                .map(UtilityRecord::state)
                .map(this::stateLabel)
                .distinct()
                .toList();
    }

    private List<String> providerCoverageCities(List<UtilityRecord> utilities) {
        return utilities.stream()
                .flatMap(utility -> utility.serviceAreaCities().stream())
                .filter(city -> city != null && !city.isBlank())
                .distinct()
                .limit(10)
                .toList();
    }

    private List<String> providerCoverageCounties(List<UtilityRecord> utilities) {
        return utilities.stream()
                .flatMap(utility -> utility.serviceAreaCounties().stream())
                .filter(county -> county != null && !county.isBlank())
                .distinct()
                .limit(10)
                .toList();
    }

    private List<String> providerServiceTypes(List<UtilityRecord> utilities) {
        LinkedHashSet<String> serviceTypes = new LinkedHashSet<>(List.of(
                "Backflow testing",
                "Backflow compliance support",
                "Annual testing coordination",
                "Failed test follow-up"
        ));
        if (utilities.stream().anyMatch(UtilityRecord::supportsIrrigationPage)) {
            serviceTypes.add("Irrigation backflow testing");
        }
        if (utilities.stream().anyMatch(UtilityRecord::supportsFireLinePage)) {
            serviceTypes.add("Fire line backflow testing");
        }
        return List.copyOf(serviceTypes);
    }

    private int providerOfficialRouteCount(List<UtilityRecord> utilities) {
        return (int) utilities.stream()
                .filter(UtilityRecord::supportsApprovedTestersPage)
                .count();
    }

    private int providerDirectoryRouteCount(List<UtilityRecord> utilities) {
        return (int) utilities.stream()
                .filter(utility -> utility.supportsFindATesterPage() && !utility.supportsApprovedTestersPage())
                .count();
    }

    private int providerSubmissionWorkflowCount(List<UtilityRecord> utilities) {
        return (int) utilities.stream()
                .filter(utility -> !utility.submissionMethods().isEmpty())
                .count();
    }

    private LocalDate providerLatestUtilityVerification(List<UtilityRecord> utilities) {
        return utilities.stream()
                .map(UtilityRecord::lastVerified)
                .filter(java.util.Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    private String providerStructuredData(
            ProviderRecord provider,
            List<UtilityRecord> utilities,
            List<String> coverageStates,
            List<String> coverageCities,
            List<String> coverageCounties
    ) {
        String canonicalProviderUrl = canonical(providerPath(provider));
        StringBuilder json = new StringBuilder();
        json.append("{\"@context\":\"https://schema.org\",\"@type\":\"LocalBusiness\"")
                .append(",\"@id\":\"").append(jsonEscape(canonicalProviderUrl)).append("#provider\"")
                .append(",\"name\":\"").append(jsonEscape(provider.providerName())).append("\"")
                .append(",\"description\":\"").append(jsonEscape(provider.pageLabel())).append("\"")
                .append(",\"url\":\"").append(jsonEscape(canonicalProviderUrl)).append("\"")
                .append(",\"image\":\"").append(jsonEscape(canonical("/images/design/provider-map.jpg"))).append("\"")
                .append(",\"keywords\":\"").append(jsonEscape(String.join(", ", providerServiceTypes(utilities)))).append("\"")
                .append(",\"disambiguatingDescription\":\"")
                .append(jsonEscape("Public provider profile grounded in utility or authority sources."))
                .append("\"")
                .append(",\"serviceType\":").append(jsonStringArray(providerServiceTypes(utilities)))
                .append(",\"knowsAbout\":").append(jsonStringArray(List.of(
                        "Backflow testing",
                        "Cross-connection control",
                        "Utility compliance",
                        "Failed test workflow"
                )));

        if (provider.lastReviewed() != null) {
            json.append(",\"dateModified\":\"").append(provider.lastReviewed()).append("\"");
        }
        if (provider.phone() != null && !provider.phone().isBlank()) {
            json.append(",\"telephone\":\"").append(jsonEscape(provider.phone())).append("\"");
        }
        if (provider.email() != null && !provider.email().isBlank()) {
            json.append(",\"email\":\"").append(jsonEscape(provider.email())).append("\"");
        }

        List<String> sameAs = new ArrayList<>();
        if (provider.siteUrl() != null && !provider.siteUrl().isBlank()) {
            sameAs.add(provider.siteUrl());
        }
        if (provider.officialApprovalSourceUrl() != null && !provider.officialApprovalSourceUrl().isBlank()) {
            sameAs.add(provider.officialApprovalSourceUrl());
        }
        if (!sameAs.isEmpty()) {
            json.append(",\"sameAs\":").append(jsonStringArray(sameAs));
        }

        String areaServed = providerAreaServedJson(coverageStates, coverageCities, coverageCounties);
        if (areaServed != null) {
            json.append(",\"areaServed\":").append(areaServed);
        }
        if (provider.officialApprovalSourceUrl() != null && !provider.officialApprovalSourceUrl().isBlank()) {
            json.append(",\"subjectOf\":{\"@type\":\"WebPage\",\"url\":\"")
                    .append(jsonEscape(provider.officialApprovalSourceUrl()))
                    .append("\"}");
        }
        json.append("}");
        return json.toString();
    }

    private String providerAreaServedJson(
            List<String> coverageStates,
            List<String> coverageCities,
            List<String> coverageCounties
    ) {
        List<String> entries = new ArrayList<>();
        for (String state : coverageStates) {
            entries.add("{\"@type\":\"AdministrativeArea\",\"name\":\"" + jsonEscape(state) + "\"}");
        }
        for (String city : coverageCities) {
            entries.add("{\"@type\":\"City\",\"name\":\"" + jsonEscape(city) + "\"}");
        }
        for (String county : coverageCounties) {
            entries.add("{\"@type\":\"AdministrativeArea\",\"name\":\"" + jsonEscape(county) + "\"}");
        }
        if (entries.isEmpty()) {
            return null;
        }
        return "[" + String.join(",", entries) + "]";
    }

    private String jsonStringArray(List<String> values) {
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> "\"" + jsonEscape(value) + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private List<GuideRecord> metroGuides(MetroRecord metro) {
        if (!metro.guideSlugs().isEmpty()) {
            return guidesByPreferredSlugs(metro.guideSlugs(), 5, null);
        }
        return guidesByPreferredSlugs(List.of(
                "backflow-test-notice-next-steps",
                "how-we-verify-backflow-rules",
                "backflow-reporting-portals",
                "anniversary-date-vs-calendar-deadline",
                "approved-testers-vs-find-a-tester",
                "backflow-test-cost"
        ), 5, null);
    }

    private List<GuideRecord> supportGuidesForStateGuide(StateGuideRecord stateGuide) {
        return guidesByPreferredSlugs(List.of(
                "backflow-test-notice-next-steps",
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
                "backflow-test-notice-next-steps",
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
            case "backflow-test-notice-next-steps" -> utilities.stream()
                    .filter(utility -> usesPortalWorkflow(utility)
                            || hasDateSpecificWorkflow(utility)
                            || !utility.submissionMethods().isEmpty()
                            || utility.supportsApprovedTestersPage())
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
        if (usesPortalWorkflow(utility)) {
            items.add(new FaqItem(
                    "Which backflow reporting portal does " + utility.utilityName() + " use?",
                    portalAnswer(utility)
            ));
        }
        items.add(new FaqItem(
                "Where should I look for testers for " + utility.utilityName() + "?",
                testerAnswer(utility)
        ));
        items.add(new FaqItem(
                "What should I check before scheduling a tester for " + utility.utilityName() + "?",
                schedulingChecklistAnswer(utility)
        ));
        items.add(new FaqItem(
                "What costs or portal fees should I expect for " + utility.utilityName() + "?",
                costAnswer(utility)
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

    private String portalAnswer(UtilityRecord utility) {
        String methods = utility.submissionMethods().stream()
                .map(method -> method.label() + " (" + method.kind() + ")")
                .collect(Collectors.joining(", "));
        if (methods.isBlank()) {
            return "The page does not store a named portal for this utility yet. Confirm the current report path on the official utility page before submitting a test.";
        }
        String hubLabel = portalHubLabel(utility);
        String hubContext = hubLabel == null ? "" : " The matching portal hub on BackflowPath is " + hubLabel + ".";
        return "The stored submission route is: " + methods
                + ". Follow the utility workflow first because tester enrollment, filing fees, and pass/fail handling can differ by jurisdiction."
                + hubContext;
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

    private String schedulingChecklistAnswer(UtilityRecord utility) {
        String testerRoute = utility.supportsApprovedTestersPage()
                ? "open the official tester-list route"
                : "confirm tester eligibility directly with the utility or portal";
        if (utility.supportsFindATesterPage()) {
            testerRoute = "use the non-official directory only after confirming the utility workflow";
        }
        return "Confirm the due basis, property type, device type, and submission method before booking. Then "
                + testerRoute
                + ", and make sure the report can be filed through the required utility or portal path.";
    }

    private String costAnswer(UtilityRecord utility) {
        if (utility.costBand() == null) {
            return "BackflowPath does not store a local cost band for this utility yet. Confirm test pricing with the tester and any filing fee with the utility or portal.";
        }
        return utility.costBand().testingRange()
                + " "
                + utility.costBand().repairRetestRange()
                + " "
                + utility.costBand().pricingNotes();
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
        String value = utilitySearchText(utility).toLowerCase(Locale.US);
        return value.contains("portal")
                || value.contains("swift")
                || value.contains("bsi")
                || value.contains("backflowtest")
                || value.contains("customerportal")
                || value.contains("c3swift")
                || value.contains("vepo")
                || value.contains("envirotrax")
                || value.contains("online submission");
    }

    private boolean utilityContainsAny(UtilityRecord utility, String... keywords) {
        return pathContainsAny(utilitySearchText(utility), keywords);
    }

    private String utilitySearchText(UtilityRecord utility) {
        StringBuilder text = new StringBuilder();
        append(text, utility.utilityUrl());
        append(text, utility.approvedTesterListUrl());
        append(text, utility.officialListLabel());
        append(text, utility.dueBasis());
        append(text, utility.verdictSummary());
        append(text, utility.sourceExcerpt());
        for (String step : utility.workflowSteps()) {
            append(text, step);
        }
        for (String alias : utility.searchAliases()) {
            append(text, alias);
        }
        utility.sources().forEach(source -> {
            append(text, source.label());
            append(text, source.url());
            append(text, source.kind());
        });
        utility.submissionMethods().forEach(method -> {
            append(text, method.label());
            append(text, method.url());
            append(text, method.kind());
        });
        return text.toString();
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
        for (StateGuideRecord stateGuide : registryService.listPublishedStateGuides()) {
            if (stateGuide.lastVerified() != null && stateGuide.lastVerified().isAfter(latest)) {
                latest = stateGuide.lastVerified();
            }
        }
        return latest;
    }

    private record SitemapEntry(String url, LocalDate lastModified) {
    }

    private record BreadcrumbItem(String name, String url) {
    }

    private record CityIntentConfig(
            String slug,
            String title,
            String description,
            String eyebrow,
            String heading,
            String intro,
            List<String> highlights,
            List<String> workflowSteps,
            String primaryPath,
            String primaryLabel,
            List<String> guideSlugs
    ) {
    }

}
