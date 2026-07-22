package owner.backflow.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
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
import owner.backflow.data.model.FailedTestPolicy;
import owner.backflow.data.model.GuideRecord;
import owner.backflow.data.model.MetroRecord;
import owner.backflow.data.model.ProviderRecord;
import owner.backflow.data.model.ReportWorkflow;
import owner.backflow.data.model.SourceLink;
import owner.backflow.data.model.StateGuideRecord;
import owner.backflow.data.model.SubmissionMethod;
import owner.backflow.data.model.UtilityFocusContent;
import owner.backflow.data.model.UtilityRecord;
import owner.backflow.files.BackflowRegistryService;
import owner.backflow.service.LeadRoutingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

@Controller
public class SiteController {
    private static final List<String> PORTAL_SLUGS = List.of("bsi", "weirs", "swiftcomply", "vepo", "aqua", "tokay", "sprybackflow");
    private static final Set<String> PRIORITY_INTENT_SLUGS = Set.of(
            "backflow-reporting-portal",
            "submit-backflow-report",
            "approved-backflow-testers",
            "annual-backflow-testing",
            "failed-backflow-test",
            "irrigation-backflow-testing",
            "fire-line-backflow-testing"
    );

    private final BackflowRegistryService registryService;
    private final AppSiteProperties siteProperties;
    private final SiteVisibilityService siteVisibilityService;
    private final ObjectMapper jsonMapper = new ObjectMapper();

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
                "BackflowPath | Local backflow rules and tester routes",
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
        model.addAttribute("priorityRoutes", priorityRoutes(9));
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
        model.addAttribute("priorityRoutes", priorityRoutes(6));
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
        List<FaqItem> faqItems = portalFaqItems("all", "Backflow reporting portals", utilities);
        model.addAttribute("page", page(
                "Backflow reporting portals by utility | BackflowPath",
                "Find the correct BSI, SwiftComply, WEIRS, VEPO, Aqua, Tokay, or utility backflow report route before filing.",
                "/backflow-reporting-portals",
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem("Reporting portals", canonical("/backflow-reporting-portals"))
                        )),
                        portalItemListStructuredData("Backflow reporting portal utility and report submission routes", utilities),
                        faqStructuredData(faqItems)
                )
        ));
        model.addAttribute("portalName", "Backflow reporting portals");
        model.addAttribute("portalSlug", "all");
        model.addAttribute("intro", "Use this page when a notice mentions BSI, SwiftComply, WEIRS, VEPO, Envirotrax, a customer account, or another online report submission workflow.");
        model.addAttribute("overview", true);
        model.addAttribute("utilities", utilities);
        model.addAttribute("portalCounts", portalCounts());
        model.addAttribute("cityAliasesByUtility", publishedCityAliasesByUtility(utilities));
        model.addAttribute("noticeIdentifierHints", noticeIdentifierHintsFor(utilities));
        model.addAttribute("reportAcceptanceHints", reportAcceptanceHintsFor(utilities));
        model.addAttribute("faqItems", faqItems);
        model.addAttribute("priorityRoutes", priorityRoutesForPortal("all", 10));
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
        List<FaqItem> faqItems = portalFaqItems(portalSlug, portalName, utilities);
        model.addAttribute("page", page(
                portalDetailPageTitle(portalSlug, portalName),
                portalDetailPageDescription(portalSlug, portalName),
                "/backflow-reporting-portals/" + portalSlug,
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem("Reporting portals", canonical("/backflow-reporting-portals")),
                                new BreadcrumbItem(portalName, canonical("/backflow-reporting-portals/" + portalSlug))
                        )),
                        portalItemListStructuredData(portalName + " utility and report submission routes", utilities),
                        faqStructuredData(faqItems)
                )
        ));
        model.addAttribute("portalName", portalName);
        model.addAttribute("portalSlug", portalSlug);
        model.addAttribute("intro", portalDescription(portalSlug));
        model.addAttribute("overview", false);
        model.addAttribute("utilities", utilities);
        model.addAttribute("portalCounts", portalCounts());
        model.addAttribute("cityAliasesByUtility", publishedCityAliasesByUtility(utilities));
        model.addAttribute("noticeIdentifierHints", noticeIdentifierHintsFor(utilities));
        model.addAttribute("reportAcceptanceHints", reportAcceptanceHintsFor(utilities));
        model.addAttribute("faqItems", faqItems);
        model.addAttribute("priorityRoutes", priorityRoutesForPortal(portalSlug, 10));
        model.addAttribute("relatedGuides", guidesByPreferredSlugs(List.of(
                "backflow-test-notice-next-steps",
                "backflow-reporting-portals",
                "how-we-verify-backflow-rules",
                "approved-testers-vs-find-a-tester"
        ), 3, null));
        return "pages/portal-hub";
    }

    @GetMapping("/notice-finder")
    public String noticeFinderPage(
            @RequestParam(name = "q", required = false, defaultValue = "") String query,
            @RequestParam(name = "ref", required = false, defaultValue = "") String referralCode,
            HttpServletRequest request,
            Model model
    ) {
        String trimmedQuery = query == null ? "" : query.trim();
        String normalizedReferralCode = normalizeReferralCode(referralCode);
        List<NoticeFinderResult> results = noticeFinderResults(trimmedQuery);
        List<FaqItem> faqItems = noticeFinderFaqItems();
        String shareUrl = noticeFinderShareUrl(trimmedQuery, normalizedReferralCode);
        String shareMessage = noticeFinderShareMessage(trimmedQuery, shareUrl);
        String shareEmailUrl = noticeFinderShareEmailUrl(shareMessage);
        PageMeta noticeFinderPage = page(
                "Backflow notice finder | BackflowPath",
                "Paste a city, utility, BSI, SwiftComply, TrackMyBackflow, Tokay, notice ID, Hazard ID, approved tester, or failed-test phrase.",
                "/notice-finder",
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem("Notice finder", canonical("/notice-finder"))
                        )),
                        noticeFinderStructuredData(),
                        faqStructuredData(faqItems)
                )
        );
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            noticeFinderPage = noticeFinderPage.withNoindex(true);
        }
        model.addAttribute("page", noticeFinderPage);
        model.addAttribute("query", trimmedQuery);
        model.addAttribute("referralCode", normalizedReferralCode);
        model.addAttribute("shareUrl", shareUrl);
        model.addAttribute("shareMessage", shareMessage);
        model.addAttribute("shareEmailUrl", shareEmailUrl);
        model.addAttribute("results", results);
        model.addAttribute("priorityRoutes", priorityRoutes(12));
        model.addAttribute("popularPortals", PORTAL_SLUGS.stream()
                .map(slug -> new NoticeFinderResult(
                        portalName(slug),
                        "/backflow-reporting-portals/" + slug,
                        "Portal family",
                        portalDescription(slug),
                        List.of("Portal comparison", "Tester credential gate", "Utility examples"),
                        0
                ))
                .toList());
        model.addAttribute("featuredUtilities", balancedUtilities(registryService.listPublishedUtilities(), 6));
        model.addAttribute("relatedGuides", guidesByPreferredSlugs(List.of(
                "backflow-test-notice-next-steps",
                "backflow-reporting-portals",
                "failed-backflow-test-next-steps",
                "approved-testers-vs-find-a-tester"
        ), 4, null));
        model.addAttribute("faqItems", faqItems);
        return "pages/notice-finder";
    }

    @GetMapping("/partners/notice-kit")
    public String partnerNoticeKitPage(Model model) {
        model.addAttribute("page", page(
                "Backflow notice kit for testers and property managers | BackflowPath",
                "Free copy, links, and a notice lookup workflow that backflow testers, contractors, and property managers can send to customers.",
                "/partners/notice-kit",
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem("Partner notice kit", canonical("/partners/notice-kit"))
                ))
        ));
        return "pages/partner-notice-kit";
    }

    @GetMapping("/partners/sample/customer-guide")
    public String partnerCustomerGuideSamplePage(Model model) {
        model.addAttribute("page", new PageMeta(
                "Sample customer handoff page | BackflowPath",
                "A sample post-test customer handoff page for a Dallas SwiftComply backflow notice.",
                canonical("/partners/sample/customer-guide"),
                true
        ));
        return "pages/partner-customer-guide-sample";
    }

    @GetMapping({"/for-providers", "/for-providers/", "/pricing", "/pricing/"})
    public RedirectView legacyProviderAcquisitionRedirect() {
        RedirectView redirect = new RedirectView("/claim-listing");
        redirect.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        redirect.setExposeModelAttributes(false);
        return redirect;
    }

    @GetMapping("/submit-backflow-report")
    public String submitBackflowReportHubPage(Model model) {
        List<UtilityRecord> utilities = portalUtilities("all");
        List<FaqItem> faqItems = submitReportHubFaqItems();
        model.addAttribute("page", page(
                "Submit backflow test reports by city and portal | BackflowPath",
                "Find city and utility routes for submitting backflow test reports through BSI, WEIRS, SwiftComply, VEPO, Aqua/TrackMyBackflow, Tokay, and online portals.",
                "/submit-backflow-report",
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem("Submit backflow report", canonical("/submit-backflow-report"))
                        )),
                        portalItemListStructuredData("Backflow test report submission routes by city and portal", utilities),
                        faqStructuredData(faqItems)
                )
        ));
        model.addAttribute("utilities", utilities);
        model.addAttribute("cityAliasesByUtility", publishedCityAliasesByUtility(utilities));
        model.addAttribute("noticeIdentifierHints", noticeIdentifierHintsFor(utilities));
        model.addAttribute("reportAcceptanceHints", reportAcceptanceHintsFor(utilities));
        model.addAttribute("portalNamesByUtility", portalNamesFor(utilities));
        model.addAttribute("portalHubPathsByUtility", portalHubPathsFor(utilities));
        model.addAttribute("faqItems", faqItems);
        model.addAttribute("priorityRoutes", priorityRoutesForIntent("submit-backflow-report", 12));
        model.addAttribute("relatedGuides", guidesByPreferredSlugs(List.of(
                "backflow-reporting-portals",
                "backflow-test-notice-next-steps",
                "approved-testers-vs-find-a-tester",
                "backflow-test-cost"
        ), 4, null));
        return "pages/submit-report-hub";
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
        model.addAttribute("priorityRoutes", priorityRoutesForState(stateGuide.state(), 8));
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
                guidePageTitle(guide),
                guidePageDescription(guide),
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
        model.addAttribute("priorityRoutes", priorityRoutesForGuide(guide, relatedUtilities, 8));
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
        List<UtilityRecord> utilities = registryService.featuredUtilitiesForMetro(metro);
        model.addAttribute("page", page(
                metroPageTitle(metro),
                metro.description(),
                metroPath(metro),
                breadcrumbStructuredData(List.of(
                        new BreadcrumbItem("Home", canonical("/")),
                        new BreadcrumbItem(stateLabel(metro.state()), canonical("/states/" + metro.state() + "/backflow-testing")),
                        new BreadcrumbItem(metro.title(), canonical(metroPath(metro)))
                ))
        ));
        model.addAttribute("metro", metro);
        model.addAttribute("utilities", utilities);
        model.addAttribute("providers", providers);
        model.addAttribute("cityAliasesByName", publishedCityAliasesByNameForState(metro.state()));
        model.addAttribute("providerCoverageCounts", providerCoverageCounts(metro, providers));
        model.addAttribute("guides", metroGuides(metro));
        model.addAttribute("priorityRoutes", priorityRoutesForUtilities(utilities, PRIORITY_INTENT_SLUGS, true, true, 8));
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
        UtilityRecord primaryUtility = utilities.isEmpty() ? null : utilities.getFirst();
        PageMeta providerPage = page(
                providerPageTitle(provider, utilities, coverageCities),
                providerPageDescription(provider, utilities, coverageCities),
                providerPath(provider),
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem("Providers", canonical("/")),
                                new BreadcrumbItem(provider.providerName(), canonical(providerPath(provider)))
                        )),
                        providerStructuredData(provider, utilities, coverageStates, coverageCities, coverageCounties)
                )
        );
        if (primaryUtility != null) {
            providerPage = providerPage.withRequestHelpPath(LeadRoutingService.requestHelpPath(
                    primaryUtility.utilityId(),
                    providerPath(provider),
                    "general-testing",
                    "provider-profile"
            ));
        }
        model.addAttribute("page", providerPage);
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
        model.addAttribute("primaryUtility", primaryUtility);
        model.addAttribute("noticeIdentifierHints", noticeIdentifierHintsFor(utilities));
        model.addAttribute("reportAcceptanceHints", reportAcceptanceHintsFor(utilities));
        model.addAttribute("portalNamesByUtility", portalNamesFor(utilities));
        model.addAttribute("portalHubPathsByUtility", portalHubPathsFor(utilities));
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
        String title = utilityPageTitle(utility);
        String description = utilityPageDescription(utility);
        String path = utilityPath(utility);
        model.addAttribute("page", page(
                title,
                description,
                path,
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                                new BreadcrumbItem(utility.utilityName(), canonical(path))
                        )),
                        webPageStructuredData(title, description, path, utility.lastVerified(), utilityAbout(utility), utility.sources()),
                        utilityServiceStructuredData(utility, path, title, description),
                        utilityAnswerCardStructuredData(utility, path, utility.utilityName() + " compliance answer"),
                        faqStructuredData(faqItems)
                )
        ).withRequestHelpPath(LeadRoutingService.requestHelpPath(
                utility.utilityId(),
                path,
                "general-testing",
                "utility"
        )));
        model.addAttribute("utility", utility);
        model.addAttribute("utilityHeading", utilityHeading(utility));
        model.addAttribute("utilitySubmissionLabel", utilitySubmissionLabel(utility));
        model.addAttribute("annualTestingPath", utility.supportsAnnualTestingPage() ? utilityPath(utility) + "annual-testing" : null);
        model.addAttribute("irrigationPath", utility.supportsIrrigationPage() ? utilityPath(utility) + "irrigation" : null);
        model.addAttribute("fireLinePath", utility.supportsFireLinePage() ? utilityPath(utility) + "fire-line" : null);
        model.addAttribute("testerPath", testerPath(utility));
        model.addAttribute("testerLabel", testerLabel(utility));
        model.addAttribute("portalHubPath", portalHubPath(utility));
        model.addAttribute("portalHubLabel", portalHubLabel(utility));
        model.addAttribute("noticeIdentifierHint", noticeIdentifierHint(utility));
        model.addAttribute("reportAcceptanceHint", reportAcceptanceHint(utility));
        model.addAttribute("cityAliases", publishedCityAliasesForUtility(utility.utilityId()));
        model.addAttribute("providers", registryService.findProvidersForUtility(utility.utilityId()).stream().limit(4).toList());
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
        String location = utilitySearchLocation(utility);
        String title = location + " utility failed backflow test | BackflowPath";
        String description = "Check " + location + " utility failed-test repair, retest, deadline, and report submission steps.";
        String path = utilityPath(utility) + "failed-test";
        model.addAttribute("page", page(
                title,
                description,
                path,
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                                new BreadcrumbItem(utility.utilityName(), canonical(utilityPath(utility))),
                                new BreadcrumbItem("Failed test", canonical(path))
                        )),
                        webPageStructuredData(title, description, path, utility.lastVerified(), utilityAbout(utility), utility.sources()),
                        utilityServiceStructuredData(utility, path, title, description),
                        utilityAnswerCardStructuredData(utility, path, utility.utilityName() + " failed-test answer"),
                        workflowHowToStructuredData(
                                utility.utilityName() + " failed backflow test repair and retest",
                                description,
                                path,
                                List.of(
                                        "Repair the failed backflow assembly or blocked components first.",
                                        "Schedule and complete a passing retest before the utility deadline slips.",
                                        "Submit the corrected report through the accepted utility workflow and keep proof of acceptance."
                                )
                        )
                )
        ).withRequestHelpPath(LeadRoutingService.requestHelpPath(
                utility.utilityId(),
                path,
                "failed-test-repair",
                "failed-test"
        )));
        model.addAttribute("utility", utility);
        model.addAttribute("failedGuide", registryService.findPublishedGuide("failed-backflow-test-next-steps").orElse(null));
        model.addAttribute("testerPath", testerPath(utility));
        model.addAttribute("testerLabel", testerLabel(utility));
        model.addAttribute("noticeIdentifierHint", noticeIdentifierHint(utility));
        model.addAttribute("reportAcceptanceHint", reportAcceptanceHint(utility));
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
        String title = officialTesterPageTitle(utility) + " | BackflowPath";
        String description = officialTesterPageDescription(utility);
        String path = utilityPath(utility) + "approved-testers";
        model.addAttribute("page", page(
                title,
                description,
                path,
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                                new BreadcrumbItem(utility.utilityName(), canonical(utilityPath(utility))),
                                new BreadcrumbItem("Approved testers", canonical(path))
                        )),
                        webPageStructuredData(title, description, path, utility.lastVerified(), utilityAbout(utility), utility.sources()),
                        utilityServiceStructuredData(utility, path, title, description),
                        utilityAnswerCardStructuredData(utility, path, utility.utilityName() + " approved tester answer")
                )
        ).withRequestHelpPath(LeadRoutingService.requestHelpPath(
                utility.utilityId(),
                path,
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
        String location = utilitySearchLocation(utility);
        String title = location + " utility backflow tester directory | BackflowPath";
        String description = "Compare " + location + " providers after checking the official utility tester and report submission rules.";
        String path = utilityPath(utility) + "find-a-tester";
        model.addAttribute("page", page(
                title,
                description,
                path,
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                                new BreadcrumbItem(utility.utilityName(), canonical(utilityPath(utility))),
                                new BreadcrumbItem("Find a tester", canonical(path))
                        )),
                        webPageStructuredData(title, description, path, utility.lastVerified(), utilityAbout(utility), utility.sources()),
                        utilityServiceStructuredData(utility, path, title, description),
                        utilityAnswerCardStructuredData(utility, path, utility.utilityName() + " tester directory answer")
                )
        ).withRequestHelpPath(LeadRoutingService.requestHelpPath(
                utility.utilityId(),
                path,
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
        String title = cityPageTitle(alias, utility);
        String description = cityPageDescription(alias, utility);
        model.addAttribute("page", new PageMeta(
                title,
                description,
                canonical(path),
                alias.aliasMode() == AliasMode.NOINDEX_BRIDGE,
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                                new BreadcrumbItem(alias.city(), canonical(path))
                        )),
                        webPageStructuredData(
                                title,
                                description,
                                path,
                                latestDate(alias.lastReviewed(), utility.lastVerified()),
                                cityAbout(alias, utility, "City backflow testing route"),
                                utility.sources()
                        ),
                        utilityServiceStructuredData(utility, path, title, description),
                        utilityAnswerCardStructuredData(utility, path, alias.city() + " backflow testing answer")
                )
        ).withDateModified(latestDate(alias.lastReviewed(), utility.lastVerified())).withRequestHelpPath(LeadRoutingService.requestHelpPath(
                utility.utilityId(),
                path,
                "general-testing",
                "city"
        )));
        model.addAttribute("alias", alias);
        model.addAttribute("utility", utility);
        Map<String, String> cityIntentPaths = cityIntentPathsBySlug(alias, utility);
        model.addAttribute("cityIntentLinks", cityIntentLinks(alias, utility));
        model.addAttribute("annualTestingPath", cityIntentPaths.get("annual-backflow-testing"));
        model.addAttribute("portalCityPath", cityIntentPaths.get("backflow-reporting-portal"));
        model.addAttribute("submitReportPath", cityIntentPaths.get("submit-backflow-report"));
        model.addAttribute("irrigationPath", cityIntentPaths.get("irrigation-backflow-testing"));
        model.addAttribute("fireLinePath", cityIntentPaths.get("fire-line-backflow-testing"));
        model.addAttribute("failedTestPath", cityIntentPaths.getOrDefault("failed-backflow-test", utilityPath(utility) + "failed-test"));
        model.addAttribute("testerPath", cityIntentPaths.getOrDefault("approved-backflow-testers", testerPath(utility)));
        model.addAttribute("testerLabel", cityIntentPaths.containsKey("approved-backflow-testers") ? alias.city() + " approved tester route" : testerLabel(utility));
        model.addAttribute("portalHubPath", portalHubPath(utility));
        model.addAttribute("portalHubLabel", portalHubLabel(utility));
        model.addAttribute("noticeIdentifierHint", noticeIdentifierHint(utility));
        model.addAttribute("reportAcceptanceHint", reportAcceptanceHint(utility));
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
        List<FaqItem> faqItems = cityIntentFaqItems(alias, utility, intent);
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
                        webPageStructuredData(
                                intent.title(),
                                intent.description(),
                                path,
                                latestDate(alias.lastReviewed(), utility.lastVerified()),
                                cityAbout(alias, utility, intent.heading()),
                                utility.sources()
                        ),
                        utilityServiceStructuredData(utility, path, intent.title(), intent.description()),
                        utilityAnswerCardStructuredData(utility, path, alias.city() + " " + intent.slug() + " answer"),
                        cityIntentHowToStructuredData(alias, intent),
                        faqStructuredData(faqItems)
                )
        ).withDateModified(latestDate(alias.lastReviewed(), utility.lastVerified())).withRequestHelpPath(LeadRoutingService.requestHelpPath(
                utility.utilityId(),
                path,
                intent.slug(),
                "city-intent"
        )));
        model.addAttribute("alias", alias);
        model.addAttribute("utility", utility);
        model.addAttribute("eyebrow", intent.eyebrow());
        model.addAttribute("intentSlug", intent.slug());
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
        model.addAttribute("cityIntentLinks", cityIntentLinks(alias, utility));
        model.addAttribute("noticeIdentifierHint", noticeIdentifierHint(utility));
        model.addAttribute("reportAcceptanceHint", reportAcceptanceHint(utility));
        model.addAttribute("faqItems", faqItems);
        model.addAttribute("relatedGuides", guidesByPreferredSlugs(intent.guideSlugs(), 4, null));
        return "pages/city-intent-page";
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap(HttpServletRequest request) {
        if (siteVisibilityService.shouldForceNoindex(request)) {
            return emptySitemap();
        }
        return sitemapXml(allSitemapEntries());
    }

    @GetMapping(value = "/sitemap-priority.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String prioritySitemap(HttpServletRequest request) {
        if (siteVisibilityService.shouldForceNoindex(request)) {
            return emptySitemap();
        }
        return sitemapXml(prioritySitemapEntries());
    }

    @GetMapping(value = "/sitemaps/core.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String coreSitemap(HttpServletRequest request) {
        if (siteVisibilityService.shouldForceNoindex(request)) {
            return emptySitemap();
        }
        return sitemapXml(coreSitemapEntries());
    }

    @GetMapping(value = "/sitemaps/utilities.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String utilitiesSitemap(HttpServletRequest request) {
        if (siteVisibilityService.shouldForceNoindex(request)) {
            return emptySitemap();
        }
        return sitemapXml(utilitySitemapEntries());
    }

    @GetMapping(value = "/sitemaps/city-intents.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String cityIntentsSitemap(HttpServletRequest request) {
        if (siteVisibilityService.shouldForceNoindex(request)) {
            return emptySitemap();
        }
        return sitemapXml(cityIntentSitemapEntries());
    }

    @GetMapping(value = "/sitemaps/portals.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String portalsSitemap(HttpServletRequest request) {
        if (siteVisibilityService.shouldForceNoindex(request)) {
            return emptySitemap();
        }
        return sitemapXml(portalSitemapEntries());
    }

    @GetMapping(value = "/sitemaps/providers.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String providersSitemap(HttpServletRequest request) {
        if (siteVisibilityService.shouldForceNoindex(request)) {
            return emptySitemap();
        }
        return sitemapXml(providerSitemapEntries());
    }

    @GetMapping(value = "/sitemaps/guides.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String guidesSitemap(HttpServletRequest request) {
        if (siteVisibilityService.shouldForceNoindex(request)) {
            return emptySitemap();
        }
        return sitemapXml(guideSitemapEntries());
    }

    @GetMapping(value = "/sitemaps/metros.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String metrosSitemap(HttpServletRequest request) {
        if (siteVisibilityService.shouldForceNoindex(request)) {
            return emptySitemap();
        }
        return sitemapXml(metroSitemapEntries());
    }

    @GetMapping(value = "/sitemap-index.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemapIndex(HttpServletRequest request) {
        if (siteVisibilityService.shouldForceNoindex(request)) {
            return emptySitemapIndex();
        }
        return sitemapIndexXml(List.of(
                new SitemapEntry(canonical("/sitemap.xml"), homeLastModified()),
                new SitemapEntry(canonical("/sitemap-priority.xml"), latestSitemapModified(prioritySitemapEntries())),
                new SitemapEntry(canonical("/sitemaps/core.xml"), latestSitemapModified(coreSitemapEntries())),
                new SitemapEntry(canonical("/sitemaps/utilities.xml"), latestSitemapModified(utilitySitemapEntries())),
                new SitemapEntry(canonical("/sitemaps/city-intents.xml"), latestSitemapModified(cityIntentSitemapEntries())),
                new SitemapEntry(canonical("/sitemaps/portals.xml"), latestSitemapModified(portalSitemapEntries())),
                new SitemapEntry(canonical("/sitemaps/providers.xml"), latestSitemapModified(providerSitemapEntries())),
                new SitemapEntry(canonical("/sitemaps/guides.xml"), latestSitemapModified(guideSitemapEntries())),
                new SitemapEntry(canonical("/sitemaps/metros.xml"), latestSitemapModified(metroSitemapEntries()))
        ));
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots(HttpServletRequest request) {
        if (siteVisibilityService.shouldForceNoindex(request)) {
            return siteVisibilityService.stagingRobotsTxt();
        }
        return "User-agent: *\n"
                + "Allow: /\n\n"
                + "Sitemap: " + canonical("/sitemap-index.xml") + "\n"
                + "Sitemap: " + canonical("/sitemap.xml") + "\n"
                + "Sitemap: " + canonical("/sitemap-priority.xml") + "\n";
    }

    private List<SitemapEntry> allSitemapEntries() {
        List<SitemapEntry> entries = new ArrayList<>();
        entries.addAll(coreSitemapEntries());
        entries.addAll(portalSitemapEntries());
        entries.addAll(cityIntentSitemapEntries());
        entries.addAll(guideSitemapEntries());
        entries.addAll(metroSitemapEntries());
        entries.addAll(utilitySitemapEntries());
        entries.addAll(providerSitemapEntries());
        return dedupeSitemapEntries(entries);
    }

    private List<SitemapEntry> coreSitemapEntries() {
        List<SitemapEntry> urls = new ArrayList<>();
        urls.add(new SitemapEntry(canonical("/"), homeLastModified()));
        urls.add(new SitemapEntry(canonical("/about"), homeLastModified()));
        urls.add(new SitemapEntry(canonical("/methodology"), homeLastModified()));
        urls.add(new SitemapEntry(canonical("/editorial-standards"), homeLastModified()));
        urls.add(new SitemapEntry(canonical("/corrections"), homeLastModified()));
        urls.add(new SitemapEntry(canonical("/contact"), homeLastModified()));
        urls.add(new SitemapEntry(canonical("/claim-listing"), homeLastModified()));
        urls.add(new SitemapEntry(canonical("/notice-finder"), homeLastModified()));
        urls.add(new SitemapEntry(canonical("/partners/notice-kit"), homeLastModified()));
        return urls;
    }

    private List<SitemapEntry> portalSitemapEntries() {
        List<SitemapEntry> urls = new ArrayList<>();
        List<UtilityRecord> officialTesterUtilities = officialTesterUtilities();
        urls.add(new SitemapEntry(canonical("/submit-backflow-report"), latestUtilityModified(portalUtilities("all"))));
        urls.add(new SitemapEntry(canonical("/official-backflow-tester-lists"), latestUtilityModified(officialTesterUtilities)));
        urls.add(new SitemapEntry(canonical("/backflow-reporting-portals"), latestUtilityModified(portalUtilities("all"))));
        for (String portalSlug : PORTAL_SLUGS) {
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
        return urls;
    }

    private List<SitemapEntry> cityIntentSitemapEntries() {
        List<SitemapEntry> urls = new ArrayList<>();
        for (CityAliasRecord alias : registryService.listCityAliases()) {
            UtilityRecord utility = registryService.findUtilityById(alias.utilityId()).orElse(null);
            if (!canIndexCityAlias(alias, utility)) {
                continue;
            }
            urls.add(new SitemapEntry(canonical(cityPath(alias)), alias.lastReviewed()));
            cityIntentConfigs(alias, utility).stream()
                    .filter(intent -> canIndexCityIntent(alias, utility, intent))
                    .forEach(intent -> urls.add(new SitemapEntry(
                            canonical(cityIntentPath(alias, intent.slug())),
                            latestDate(alias.lastReviewed(), utility.lastVerified())
                    )));
        }
        return urls;
    }

    private List<SitemapEntry> guideSitemapEntries() {
        List<SitemapEntry> urls = new ArrayList<>();
        registryService.listPublishedStateGuides()
                .forEach(guide -> urls.add(new SitemapEntry(
                        canonical("/states/" + guide.state() + "/backflow-testing"),
                        guide.lastVerified()
                )));
        registryService.listPublishedGuides().forEach(guide -> urls.add(new SitemapEntry(
                canonical("/guides/" + guide.slug()),
                guide.lastReviewed()
        )));
        return urls;
    }

    private List<SitemapEntry> metroSitemapEntries() {
        return registryService.listPublishedMetros().stream()
                .map(metro -> new SitemapEntry(canonical(metroPath(metro)), metro.lastReviewed()))
                .toList();
    }

    private List<SitemapEntry> utilitySitemapEntries() {
        List<SitemapEntry> urls = new ArrayList<>();
        for (UtilityRecord utility : registryService.listPublishedUtilities()) {
            if (!canIndexUtility(utility)) {
                continue;
            }
            urls.add(new SitemapEntry(canonical(utilityPath(utility)), utility.lastVerified()));
            if (utility.supportsAnnualTestingPage() && utility.supportsCityIntent("annual-backflow-testing")) {
                urls.add(new SitemapEntry(canonical(utilityPath(utility) + "annual-testing"), utility.lastVerified()));
            }
            if (utility.supportsFailedTestPage() && utility.supportsCityIntent("failed-backflow-test")) {
                urls.add(new SitemapEntry(canonical(utilityPath(utility) + "failed-test"), utility.lastVerified()));
            }
            if (utility.supportsIrrigationPage() && utility.supportsCityIntent("irrigation-backflow-testing")) {
                urls.add(new SitemapEntry(canonical(utilityPath(utility) + "irrigation"), utility.lastVerified()));
            }
            if (utility.supportsFireLinePage() && utility.supportsCityIntent("fire-line-backflow-testing")) {
                urls.add(new SitemapEntry(canonical(utilityPath(utility) + "fire-line"), utility.lastVerified()));
            }
            if (utility.supportsApprovedTestersPage() && utility.supportsCityIntent("approved-backflow-testers")) {
                urls.add(new SitemapEntry(canonical(utilityPath(utility) + "approved-testers"), utility.lastVerified()));
            }
            if (utility.supportsFindATesterPage() && !registryService.findProvidersForUtility(utility.utilityId()).isEmpty()) {
                urls.add(new SitemapEntry(canonical(utilityPath(utility) + "find-a-tester"), utility.lastVerified()));
            }
        }
        return urls;
    }

    private List<SitemapEntry> providerSitemapEntries() {
        return registryService.listPublicProviders().stream()
                .map(provider -> new SitemapEntry(canonical(providerPath(provider)), provider.lastReviewed()))
                .toList();
    }

    private List<SitemapEntry> prioritySitemapEntries() {
        return prioritySitemapPaths().stream()
                .map(path -> new SitemapEntry(canonical(path), priorityLastModified(path)))
                .toList();
    }

    private List<SitemapEntry> dedupeSitemapEntries(List<SitemapEntry> entries) {
        Map<String, LocalDate> latestByUrl = new LinkedHashMap<>();
        for (SitemapEntry entry : entries) {
            latestByUrl.merge(entry.url(), entry.lastModified(), this::latestDate);
        }
        return latestByUrl.entrySet().stream()
                .map(entry -> new SitemapEntry(entry.getKey(), entry.getValue()))
                .toList();
    }

    private LocalDate latestSitemapModified(List<SitemapEntry> entries) {
        LocalDate latest = entries.stream()
                .map(SitemapEntry::lastModified)
                .filter(java.util.Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(homeLastModified());
        return effectiveSitemapLastModified(latest);
    }

    private String sitemapXml(List<SitemapEntry> urls) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        for (SitemapEntry entry : urls) {
            xml.append("<url><loc>")
                    .append(entry.url())
                    .append("</loc><lastmod>")
                    .append(effectiveSitemapLastModified(entry.lastModified()))
                    .append("</lastmod></url>");
        }
        xml.append("</urlset>");
        return xml.toString();
    }

    private String sitemapIndexXml(List<SitemapEntry> sitemaps) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        for (SitemapEntry entry : sitemaps) {
            xml.append("<sitemap><loc>")
                    .append(entry.url())
                    .append("</loc><lastmod>")
                    .append(entry.lastModified())
                    .append("</lastmod></sitemap>");
        }
        xml.append("</sitemapindex>");
        return xml.toString();
    }

    private String emptySitemap() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"></urlset>";
    }

    private String emptySitemapIndex() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"></sitemapindex>";
    }

    private List<PriorityRoute> priorityRoutes(int limit) {
        return priorityRoutesForUtilities(
                registryService.listPublishedUtilities(),
                PRIORITY_INTENT_SLUGS,
                true,
                true,
                limit
        );
    }

    private List<PriorityRoute> priorityRoutesForIntent(String intentSlug, int limit) {
        return priorityRoutesForUtilities(
                registryService.listPublishedUtilities(),
                Set.of(intentSlug),
                false,
                false,
                limit
        );
    }

    private List<PriorityRoute> priorityRoutesForState(String state, int limit) {
        return priorityRoutesForUtilities(
                registryService.listPublishedUtilitiesForState(state),
                PRIORITY_INTENT_SLUGS,
                true,
                true,
                limit
        );
    }

    private List<PriorityRoute> priorityRoutesForGuide(GuideRecord guide, List<UtilityRecord> relatedUtilities, int limit) {
        List<UtilityRecord> utilities = relatedUtilities == null || relatedUtilities.isEmpty()
                ? registryService.listPublishedUtilities()
                : relatedUtilities;
        return priorityRoutesForUtilities(
                utilities,
                priorityIntentSlugsForGuide(guide),
                true,
                true,
                limit
        );
    }

    private List<PriorityRoute> priorityRoutesForPortal(String portalSlug, int limit) {
        return priorityRoutesForUtilities(
                portalUtilities(portalSlug),
                Set.of("backflow-reporting-portal", "submit-backflow-report", "annual-backflow-testing", "failed-backflow-test"),
                !portalSlug.equals("all"),
                true,
                limit
        );
    }

    private Set<String> priorityIntentSlugsForGuide(GuideRecord guide) {
        String slug = guide == null || guide.slug() == null ? "" : guide.slug();
        return switch (slug) {
            case "backflow-reporting-portals" -> Set.of("backflow-reporting-portal", "submit-backflow-report", "failed-backflow-test");
            case "backflow-test-notice-next-steps" -> Set.of("annual-backflow-testing", "submit-backflow-report", "backflow-reporting-portal", "failed-backflow-test");
            case "failed-backflow-test-next-steps" -> Set.of("failed-backflow-test", "submit-backflow-report", "backflow-reporting-portal");
            case "approved-testers-vs-find-a-tester", "county-certified-vs-utility-approved-testers" -> Set.of("approved-backflow-testers", "submit-backflow-report");
            case "anniversary-date-vs-calendar-deadline" -> Set.of("annual-backflow-testing", "submit-backflow-report");
            case "residential-vs-commercial-backflow-rules", "rpz-vs-dcva-vs-pvb" -> Set.of("annual-backflow-testing", "irrigation-backflow-testing", "fire-line-backflow-testing");
            default -> PRIORITY_INTENT_SLUGS;
        };
    }

    private List<PriorityRoute> priorityRoutesForUtilities(
            List<UtilityRecord> utilities,
            Set<String> intentSlugs,
            boolean includeUtilityRoutes,
            boolean includeCityRoutes,
            int limit
    ) {
        Map<String, PriorityRoute> routesByPath = new LinkedHashMap<>();
        Set<String> allowedIntentSlugs = intentSlugs == null || intentSlugs.isEmpty()
                ? PRIORITY_INTENT_SLUGS
                : intentSlugs;
        for (UtilityRecord utility : utilities) {
            if (!canIndexUtility(utility)) {
                continue;
            }
            int baseScore = utility.indexQualityScore();
            if (includeUtilityRoutes && (utility.hasReportWorkflow() || utility.hasTesterGate() || utility.hasDeadlinePolicy())) {
                addPriorityRoute(routesByPath, new PriorityRoute(
                        "Utility workflow",
                        utility.utilityName(),
                        utility.verdictSummary(),
                        utilityPath(utility),
                        baseScore + structuredFactBoost(utility)
                ));
            }
            for (CityAliasRecord alias : publishedCityAliasesForUtility(utility.utilityId()).stream().limit(4).toList()) {
                if (includeCityRoutes) {
                    addPriorityRoute(routesByPath, new PriorityRoute(
                            "City route",
                            alias.city() + " backflow testing",
                            cityPageDescription(alias, utility),
                            cityPath(alias),
                            baseScore + structuredFactBoost(utility) + 4
                    ));
                }
                cityIntentConfigs(alias, utility).stream()
                        .filter(intent -> allowedIntentSlugs.contains(intent.slug()))
                        .filter(intent -> canIndexPriorityCityIntent(alias, utility, intent))
                        .forEach(intent -> addPriorityRoute(routesByPath, new PriorityRoute(
                                intent.eyebrow(),
                                intent.heading(),
                                intent.description(),
                                cityIntentPath(alias, intent.slug()),
                                baseScore
                                        + structuredFactBoost(utility)
                                        + utility.cityIntentEvidenceScore(intent.slug()) * 3
                                        + intentPriorityBoost(intent.slug())
                        )));
            }
        }
        return routesByPath.values().stream()
                .sorted(Comparator.comparingInt(PriorityRoute::score).reversed()
                        .thenComparing(PriorityRoute::label))
                .limit(Math.max(0, limit))
                .toList();
    }

    private void addPriorityRoute(Map<String, PriorityRoute> routesByPath, PriorityRoute route) {
        if (route == null || route.path() == null || route.path().isBlank()) {
            return;
        }
        PriorityRoute existing = routesByPath.get(route.path());
        if (existing == null || route.score() > existing.score()) {
            routesByPath.put(route.path(), route);
        }
    }

    private int structuredFactBoost(UtilityRecord utility) {
        int score = 0;
        score += utility.hasReportWorkflow() ? 4 : 0;
        score += utility.hasTesterGate() ? 3 : 0;
        score += utility.hasDeadlinePolicy() ? 3 : 0;
        score += utility.hasFailedTestPolicy() ? 2 : 0;
        score += utility.costBand() != null && utility.costBand().hasStructuredFees() ? 2 : 0;
        return score;
    }

    private int intentPriorityBoost(String slug) {
        return switch (slug) {
            case "submit-backflow-report" -> 9;
            case "backflow-reporting-portal" -> 8;
            case "annual-backflow-testing" -> 6;
            case "failed-backflow-test" -> 5;
            case "approved-backflow-testers" -> 4;
            case "irrigation-backflow-testing", "fire-line-backflow-testing" -> 2;
            default -> 0;
        };
    }

    private List<String> prioritySitemapPaths() {
        Set<String> paths = new LinkedHashSet<>(List.of(
                "/notice-finder",
                "/submit-backflow-report",
                "/guides/backflow-test-notice-next-steps",
                "/guides/backflow-reporting-portals",
                "/guides/backflow-test-cost",
                "/official-backflow-tester-lists",
                "/backflow-reporting-portals"
        ));
        PORTAL_SLUGS.stream()
                .filter(slug -> !portalUtilities(slug).isEmpty())
                .map(slug -> "/backflow-reporting-portals/" + slug)
                .forEach(paths::add);
        priorityRoutes(80).stream()
                .map(PriorityRoute::path)
                .forEach(paths::add);
        registryService.listPublishedUtilities().stream()
                .filter(this::canIndexUtility)
                .filter(utility -> utility.hasReportWorkflow() || usesPortalWorkflow(utility) || utility.hasTesterGate())
                .forEach(utility -> {
                    paths.add(utilityPath(utility));
                    if (utility.supportsApprovedTestersPage()) {
                        paths.add(utilityPath(utility) + "approved-testers");
                    }
                    if (utility.supportsAnnualTestingPage() && utility.hasDeadlinePolicy()) {
                        paths.add(utilityPath(utility) + "annual-testing");
                    }
                    publishedCityAliasesForUtility(utility.utilityId()).stream()
                            .filter(alias -> canIndexCityAlias(alias, utility))
                            .limit(4)
                            .forEach(alias -> {
                                paths.add(cityPath(alias));
                                cityIntentConfigs(alias, utility).stream()
                                        .filter(intent -> Set.of(
                                                "backflow-reporting-portal",
                                                "submit-backflow-report",
                                                "approved-backflow-testers",
                                                "annual-backflow-testing",
                                                "failed-backflow-test"
                                        ).contains(intent.slug()))
                                        .filter(intent -> canIndexPriorityCityIntent(alias, utility, intent))
                                        .forEach(intent -> paths.add(cityIntentPath(alias, intent.slug())));
                            });
                });
        return List.copyOf(paths);
    }

    private LocalDate priorityLastModified(String path) {
        for (UtilityRecord utility : registryService.listPublishedUtilities()) {
            if (path.startsWith(utilityPath(utility))) {
                return utility.lastVerified();
            }
        }
        for (CityAliasRecord alias : registryService.listCityAliases()) {
            if (path.equals(cityPath(alias)) || path.startsWith("/cities/" + alias.state() + "/" + alias.aliasSlug() + "/")) {
                return registryService.findUtilityById(alias.utilityId())
                        .map(utility -> latestDate(alias.lastReviewed(), utility.lastVerified()))
                        .orElse(alias.lastReviewed());
            }
        }
        if (path.startsWith("/backflow-reporting-portals/")) {
            String portalSlug = path.substring("/backflow-reporting-portals/".length());
            return latestUtilityModified(portalUtilities(portalSlug));
        }
        if (path.equals("/backflow-reporting-portals")) {
            return latestUtilityModified(portalUtilities("all"));
        }
        if (path.startsWith("/guides/")) {
            String slug = path.substring("/guides/".length());
            return registryService.findPublishedGuide(slug)
                    .map(GuideRecord::lastReviewed)
                    .orElse(homeLastModified());
        }
        return homeLastModified();
    }

    private PageMeta page(String title, String description, String path) {
        return page(title, description, path, null);
    }

    private PageMeta page(String title, String description, String path, String structuredDataJson) {
        return new PageMeta(title, description, canonical(path), false, structuredDataJson)
                .withDateModified(priorityLastModified(path));
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
                    case "aqua" -> utilityContainsAny(utility, "aqua backflow", "aquabackflow", "trackmybackflow", "track my backflow");
                    case "tokay" -> utilityContainsAny(utility, "tokay", "webtest", "web test");
                    case "sprybackflow" -> utilityContainsAny(utility, "sprybackflow", "spry backflow", "sprybackflow.aurorawater.org", "sprybackflow.com");
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
        counts.put("aqua", portalUtilities("aqua").size());
        counts.put("tokay", portalUtilities("tokay").size());
        counts.put("sprybackflow", portalUtilities("sprybackflow").size());
        return counts;
    }

    private List<NoticeFinderResult> noticeFinderResults(String query) {
        String normalizedQuery = normalizeSearch(query);
        if (normalizedQuery.isBlank()) {
            return List.of();
        }

        Map<String, NoticeFinderResult> resultsByPath = new LinkedHashMap<>();
        boolean hasLocalityHint = hasLocalityHint(normalizedQuery);
        if (isPortalNoticeQuery(normalizedQuery)) {
            addNoticeFinderResult(resultsByPath, new NoticeFinderResult(
                    "All reporting portal workflows",
                    "/backflow-reporting-portals",
                    "Portal hub",
                    "Compare source-backed portal families before choosing a tester or report route.",
                    List.of("BSI", "SwiftComply", "VEPO", "Aqua/TrackMyBackflow", "Tokay", "SpryBackflow"),
                    hasLocalityHint ? 42 : 82
            ));
        }
        for (String portalSlug : PORTAL_SLUGS) {
            int portalScore = portalQueryScore(portalSlug, normalizedQuery);
            if (hasLocalityHint) {
                portalScore -= 42;
            }
            if (portalScore > 0) {
                addNoticeFinderResult(resultsByPath, new NoticeFinderResult(
                        portalName(portalSlug) + " backflow reporting portal",
                        "/backflow-reporting-portals/" + portalSlug,
                        "Portal family",
                        portalDescription(portalSlug),
                        List.of("Portal comparison", "Tester credential gate", "Utility examples"),
                        portalScore
                ));
            }
        }

        for (CityAliasRecord alias : registryService.listCityAliases()) {
            if (alias.aliasMode() == AliasMode.NOINDEX_BRIDGE) {
                continue;
            }
            registryService.findUtilityById(alias.utilityId()).ifPresent(utility -> {
                int textScore = scoreCandidate(normalizedQuery, candidateText(alias, utility));
                int localityScore = localityBoost(normalizedQuery, alias);
                if (textScore == 0 && localityScore == 0 && !searchTokens(normalizedQuery).isEmpty()) {
                    return;
                }
                int score = textScore + localityScore + intentBoost(normalizedQuery, utility);
                if (score > 0) {
                    addNoticeFinderResult(resultsByPath, new NoticeFinderResult(
                            alias.city() + " backflow notice route",
                            selectCityNoticePath(alias, utility, normalizedQuery),
                            "City route",
                            alias.city() + " maps to " + utility.utilityName() + ". " + reportAcceptanceHint(utility),
                            noticeSignals(utility),
                            score + 12
                    ));
                }
            });
        }

        for (UtilityRecord utility : registryService.listPublishedUtilities()) {
            int textScore = scoreCandidate(normalizedQuery, candidateText(utility));
            if (textScore == 0 && !searchTokens(normalizedQuery).isEmpty()) {
                continue;
            }
            int score = textScore;
            if (utility.utilityName() != null && normalizeSearch(utility.utilityName()).contains(normalizedQuery)) {
                score += 40;
            }
            score += localityBoost(normalizedQuery, utility);
            score += intentBoost(normalizedQuery, utility);
            if (score > 0) {
                addNoticeFinderResult(resultsByPath, new NoticeFinderResult(
                        utility.utilityName() + " workflow",
                        selectUtilityNoticePath(utility, normalizedQuery),
                        "Utility workflow",
                        utility.verdictSummary(),
                        noticeSignals(utility),
                        score
                ));
            }
        }

        return resultsByPath.values().stream()
                .sorted(Comparator.comparingInt(NoticeFinderResult::score).reversed()
                        .thenComparing(NoticeFinderResult::label))
                .limit(12)
                .toList();
    }

    private void addNoticeFinderResult(Map<String, NoticeFinderResult> resultsByPath, NoticeFinderResult result) {
        NoticeFinderResult existing = resultsByPath.get(result.path());
        if (existing == null || result.score() > existing.score()) {
            resultsByPath.put(result.path(), result);
        }
    }

    private int portalQueryScore(String portalSlug, String normalizedQuery) {
        int score = 0;
        for (String term : portalSearchTerms(portalSlug)) {
            if (queryContains(normalizedQuery, term)) {
                score += 95;
            }
        }
        if (isPortalNoticeQuery(normalizedQuery) && !portalUtilities(portalSlug).isEmpty()) {
            score += 15;
        }
        return score;
    }

    private List<String> portalSearchTerms(String portalSlug) {
        return switch (portalSlug.toLowerCase(Locale.US)) {
            case "bsi" -> List.of("bsi", "backflow solutions", "backflowtest", "ccn");
            case "weirs" -> List.of("weirs", "water environmental inspection reporting system");
            case "swiftcomply" -> List.of("swiftcomply", "swift comply", "c3swift");
            case "vepo" -> List.of("vepo", "envirotrax", "bpat");
            case "aqua" -> List.of("aqua backflow", "trackmybackflow", "track my backflow", "hazard id", "site id");
            case "tokay" -> List.of("tokay", "webtest", "web test");
            case "sprybackflow" -> List.of("sprybackflow", "spry backflow", "aurora water backflow site", "greeley spry backflow");
            default -> List.of();
        };
    }

    private int scoreCandidate(String normalizedQuery, String candidateText) {
        String normalizedCandidate = normalizeSearch(candidateText);
        if (normalizedCandidate.isBlank()) {
            return 0;
        }
        int score = normalizedCandidate.contains(normalizedQuery) ? 72 : 0;
        for (String token : searchTokens(normalizedQuery)) {
            if (normalizedCandidate.contains(token)) {
                score += 12;
            }
        }
        return score;
    }

    private boolean hasLocalityHint(String normalizedQuery) {
        for (CityAliasRecord alias : registryService.listCityAliases()) {
            if (alias.aliasMode() == AliasMode.NOINDEX_BRIDGE) {
                continue;
            }
            if ((alias.city() != null && queryContains(normalizedQuery, alias.city()))
                    || (alias.aliasSlug() != null && queryContains(normalizedQuery, alias.aliasSlug()))) {
                return true;
            }
        }
        return false;
    }

    private int localityBoost(String normalizedQuery, CityAliasRecord alias) {
        int boost = 0;
        if (alias.city() != null && queryContains(normalizedQuery, alias.city())) {
            boost += 72;
        }
        if (alias.aliasSlug() != null && queryContains(normalizedQuery, alias.aliasSlug())) {
            boost += 24;
        }
        if (alias.state() != null && queryContains(normalizedQuery, stateLabel(alias.state()))) {
            boost += 8;
        }
        return boost;
    }

    private int localityBoost(String normalizedQuery, UtilityRecord utility) {
        int boost = 0;
        for (String city : utility.serviceAreaCities()) {
            if (queryContains(normalizedQuery, city)) {
                boost += 48;
                break;
            }
        }
        if (utility.state() != null && queryContains(normalizedQuery, stateLabel(utility.state()))) {
            boost += 8;
        }
        return boost;
    }

    private int intentBoost(String normalizedQuery, UtilityRecord utility) {
        int boost = 0;
        if (isSubmitReportNoticeQuery(normalizedQuery) && (utility.hasReportWorkflow() || usesPortalWorkflow(utility))) {
            boost += 38;
        }
        if (isPortalNoticeQuery(normalizedQuery) && usesPortalWorkflow(utility)) {
            boost += 32;
        }
        if (isTesterNoticeQuery(normalizedQuery) && (utility.supportsApprovedTestersPage() || utility.supportsFindATesterPage())) {
            boost += 34;
        }
        if (isFailedNoticeQuery(normalizedQuery) && !utility.failureHighlights().isEmpty()) {
            boost += 24;
        }
        if (isAnnualNoticeQuery(normalizedQuery) && utility.supportsAnnualTestingPage()) {
            boost += 30;
        }
        if (isFireLineNoticeQuery(normalizedQuery) && utility.supportsFireLinePage()) {
            boost += 18;
        }
        if (isIrrigationNoticeQuery(normalizedQuery) && utility.supportsIrrigationPage()) {
            boost += 18;
        }
        return boost;
    }

    private String candidateText(CityAliasRecord alias, UtilityRecord utility) {
        return List.of(
                alias.city(),
                alias.aliasSlug(),
                stateLabel(alias.state()),
                alias.justification(),
                candidateText(utility)
        ).stream()
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));
    }

    private String candidateText(UtilityRecord utility) {
        List<String> values = new ArrayList<>();
        values.add(utility.utilityName());
        values.add(utility.utilityId());
        values.add(utility.canonicalSlug());
        values.add(stateLabel(utility.state()));
        values.addAll(utility.serviceAreaCities());
        values.addAll(utility.serviceAreaCounties());
        values.addAll(utility.searchAliases());
        values.add(utility.verdictSummary());
        values.add(utility.whoIsAffected());
        values.add(utility.testingFrequency());
        values.add(utility.dueBasis());
        values.add(utility.officialListLabel());
        utility.submissionMethods().forEach(method -> {
            values.add(method.label());
            values.add(method.kind());
        });
        values.addAll(utility.workflowSteps());
        values.addAll(utility.failureHighlights());
        values.add(utility.sourceExcerpt());
        if (utility.costBand() != null) {
            values.add(utility.costBand().testingRange());
            values.add(utility.costBand().repairRetestRange());
            values.add(utility.costBand().pricingNotes());
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));
    }

    private String selectCityNoticePath(CityAliasRecord alias, UtilityRecord utility, String normalizedQuery) {
        if (isFailedNoticeQuery(normalizedQuery) && cityIntentConfig("failed-backflow-test", alias, utility) != null) {
            return cityIntentPath(alias, "failed-backflow-test");
        }
        if (isSubmitReportNoticeQuery(normalizedQuery) && cityIntentConfig("submit-backflow-report", alias, utility) != null) {
            return cityIntentPath(alias, "submit-backflow-report");
        }
        if (isTesterNoticeQuery(normalizedQuery) && cityIntentConfig("approved-backflow-testers", alias, utility) != null) {
            return cityIntentPath(alias, "approved-backflow-testers");
        }
        if (isFireLineNoticeQuery(normalizedQuery) && cityIntentConfig("fire-line-backflow-testing", alias, utility) != null) {
            return cityIntentPath(alias, "fire-line-backflow-testing");
        }
        if (isIrrigationNoticeQuery(normalizedQuery) && cityIntentConfig("irrigation-backflow-testing", alias, utility) != null) {
            return cityIntentPath(alias, "irrigation-backflow-testing");
        }
        if (isAnnualNoticeQuery(normalizedQuery) && cityIntentConfig("annual-backflow-testing", alias, utility) != null) {
            return cityIntentPath(alias, "annual-backflow-testing");
        }
        if (isPortalNoticeQuery(normalizedQuery) && cityIntentConfig("backflow-reporting-portal", alias, utility) != null) {
            return cityIntentPath(alias, "backflow-reporting-portal");
        }
        return cityPath(alias);
    }

    private String selectUtilityNoticePath(UtilityRecord utility, String normalizedQuery) {
        if (isFailedNoticeQuery(normalizedQuery)) {
            return utilityPath(utility) + "failed-test";
        }
        if (isTesterNoticeQuery(normalizedQuery) && testerPath(utility) != null) {
            return testerPath(utility);
        }
        if (isFireLineNoticeQuery(normalizedQuery) && utility.supportsFireLinePage()) {
            return utilityPath(utility) + "fire-line";
        }
        if (isIrrigationNoticeQuery(normalizedQuery) && utility.supportsIrrigationPage()) {
            return utilityPath(utility) + "irrigation";
        }
        if (isAnnualNoticeQuery(normalizedQuery) && utility.supportsAnnualTestingPage()) {
            return utilityPath(utility) + "annual-testing";
        }
        return utilityPath(utility);
    }

    private List<String> noticeSignals(UtilityRecord utility) {
        List<String> signals = new ArrayList<>();
        String portalSlug = portalSlugForUtility(utility);
        if (portalSlug != null) {
            signals.add("Portal: " + portalName(portalSlug));
        }
        if (utility.supportsApprovedTestersPage()) {
            signals.add("Tester gate: official list");
        } else if (utility.supportsFindATesterPage()) {
            signals.add("Tester gate: non-official directory");
        }
        if (utility.dueBasis() != null && !utility.dueBasis().isBlank()) {
            signals.add("Due basis: " + utility.dueBasis());
        }
        if (utility.costBand() != null && utility.costBand().pricingNotes() != null && !utility.costBand().pricingNotes().isBlank()) {
            signals.add("Fee clue: " + utility.costBand().pricingNotes());
        } else if (utility.costBand() != null && utility.costBand().testingRange() != null && !utility.costBand().testingRange().isBlank()) {
            signals.add("Cost clue: " + utility.costBand().testingRange());
        }
        if (!utility.failureHighlights().isEmpty()) {
            signals.add("Failed-test clue: " + utility.failureHighlights().get(0));
        }
        return signals.stream().limit(4).toList();
    }

    private String noticeIdentifierHint(UtilityRecord utility) {
        String portalSlug = portalSlugForUtility(utility);
        if (portalSlug == null) {
            return "Look for the utility name, service address, assembly serial number, account or notice ID, and due date.";
        }
        return switch (portalSlug) {
            case "bsi" -> "Look for the CCN, account number, BSI record, device ID, or assembly serial from the reminder.";
            case "weirs" -> "Look for the WEIRS route, service address, tester record, or assembly identifier.";
            case "swiftcomply" -> "Look for the SwiftComply or C3Swift account, device, address, or notice record.";
            case "vepo" -> "Look for the BPAT/tester registration context, Envirotrax record, service address, or assembly identifier.";
            case "aqua" -> "Look for the Hazard ID, Site ID, device record, or TrackMyBackflow customer record.";
            case "tokay" -> "Look for the Tokay/WebTest entry, tester login context, device record, or assembly identifier.";
            case "sprybackflow" -> "Look for the utility service address, responsible party or customer record, assembly record, and annual due-date clue before opening SpryBackflow.";
            default -> "Look for the utility name, service address, assembly serial number, account or notice ID, and due date.";
        };
    }

    private String reportAcceptanceHint(UtilityRecord utility) {
        if (usesPortalWorkflow(utility) && utility.supportsApprovedTestersPage()) {
            return "Report acceptance depends on the named portal and the utility-approved tester route; keep proof that the report was submitted.";
        }
        if (usesPortalWorkflow(utility)) {
            return "Report acceptance depends on using the named portal or online submission path; keep proof that the report was submitted.";
        }
        if (utility.supportsApprovedTestersPage()) {
            return "Report acceptance depends on the governing tester route and the utility's submission method; confirm status before scheduling.";
        }
        if (!utility.submissionMethods().isEmpty()) {
            return "Use the listed submission method and keep proof that the report was filed with the utility.";
        }
        return "Confirm the current submission path with the utility before scheduling, repair, or retest work.";
    }

    private Map<String, String> noticeIdentifierHintsFor(List<UtilityRecord> utilities) {
        return utilities.stream()
                .collect(Collectors.toMap(
                        UtilityRecord::utilityId,
                        this::noticeIdentifierHint,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private Map<String, String> reportAcceptanceHintsFor(List<UtilityRecord> utilities) {
        return utilities.stream()
                .collect(Collectors.toMap(
                        UtilityRecord::utilityId,
                        this::reportAcceptanceHint,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private Map<String, String> portalNamesFor(List<UtilityRecord> utilities) {
        return utilities.stream()
                .collect(Collectors.toMap(
                        UtilityRecord::utilityId,
                        this::portalDisplayName,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private Map<String, String> portalHubPathsFor(List<UtilityRecord> utilities) {
        return utilities.stream()
                .filter(utility -> portalHubPath(utility) != null)
                .collect(Collectors.toMap(
                        UtilityRecord::utilityId,
                        this::portalHubPath,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private boolean isFailedNoticeQuery(String normalizedQuery) {
        return queryContains(normalizedQuery, "failed")
                || queryContains(normalizedQuery, "fail")
                || queryContains(normalizedQuery, "repair")
                || queryContains(normalizedQuery, "retest");
    }

    private boolean isPortalNoticeQuery(String normalizedQuery) {
        return queryContains(normalizedQuery, "portal")
                || queryContains(normalizedQuery, "report")
                || queryContains(normalizedQuery, "upload")
                || queryContains(normalizedQuery, "submit")
                || queryContains(normalizedQuery, "submission")
                || PORTAL_SLUGS.stream().anyMatch(slug -> portalSearchTerms(slug).stream().anyMatch(term -> queryContains(normalizedQuery, term)));
    }

    private boolean isSubmitReportNoticeQuery(String normalizedQuery) {
        return queryContains(normalizedQuery, "submit")
                || queryContains(normalizedQuery, "submission")
                || queryContains(normalizedQuery, "upload")
                || queryContains(normalizedQuery, "file report")
                || queryContains(normalizedQuery, "file a report")
                || queryContains(normalizedQuery, "test report")
                || queryContains(normalizedQuery, "report submission");
    }

    private boolean isTesterNoticeQuery(String normalizedQuery) {
        return queryContains(normalizedQuery, "tester")
                || queryContains(normalizedQuery, "approved")
                || queryContains(normalizedQuery, "certified")
                || queryContains(normalizedQuery, "registered")
                || queryContains(normalizedQuery, "credential")
                || queryContains(normalizedQuery, "license")
                || queryContains(normalizedQuery, "calibration");
    }

    private boolean isAnnualNoticeQuery(String normalizedQuery) {
        return queryContains(normalizedQuery, "annual")
                || queryContains(normalizedQuery, "notice")
                || queryContains(normalizedQuery, "reminder")
                || queryContains(normalizedQuery, "due")
                || queryContains(normalizedQuery, "deadline")
                || queryContains(normalizedQuery, "anniversary");
    }

    private boolean isIrrigationNoticeQuery(String normalizedQuery) {
        return queryContains(normalizedQuery, "irrigation")
                || queryContains(normalizedQuery, "sprinkler")
                || queryContains(normalizedQuery, "reclaimed")
                || queryContains(normalizedQuery, "landscape");
    }

    private boolean isFireLineNoticeQuery(String normalizedQuery) {
        return queryContains(normalizedQuery, "fire line")
                || queryContains(normalizedQuery, "fireline")
                || queryContains(normalizedQuery, "fire protection")
                || queryContains(normalizedQuery, "fire sprinkler");
    }

    private boolean queryContains(String normalizedQuery, String term) {
        String normalizedTerm = normalizeSearch(term);
        return !normalizedTerm.isBlank() && normalizedQuery.contains(normalizedTerm);
    }

    private List<String> searchTokens(String normalizedQuery) {
        Set<String> stopWords = Set.of(
                "backflow", "test", "testing", "report", "reports", "notice",
                "annual", "city", "water", "utility", "utilities", "program"
        );
        return List.of(normalizedQuery.split(" ")).stream()
                .filter(token -> token.length() > 2)
                .filter(token -> !stopWords.contains(token))
                .distinct()
                .toList();
    }

    private String normalizeSearch(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.US)
                .replaceAll("[^a-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalizeReferralCode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.toLowerCase(Locale.US).replaceAll("[^a-z0-9_-]", "");
        return normalized.substring(0, Math.min(48, normalized.length()));
    }

    private String noticeFinderShareUrl(String query, String referralCode) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(canonical("/notice-finder"));
        if (query != null && !query.isBlank()) {
            builder.queryParam("q", query);
        }
        if (referralCode != null && !referralCode.isBlank()) {
            builder.queryParam("ref", referralCode);
        }
        return builder.build().encode().toUriString();
    }

    private String noticeFinderShareMessage(String query, String shareUrl) {
        if (query == null || query.isBlank()) {
            return "Find the right backflow notice route with BackflowPath: " + shareUrl;
        }
        return "I found a source-backed next step for this backflow notice (" + query + "): " + shareUrl;
    }

    private String noticeFinderShareEmailUrl(String shareMessage) {
        return "mailto:?subject="
                + UriUtils.encodeQueryParam("Backflow notice next step", StandardCharsets.UTF_8)
                + "&body="
                + UriUtils.encodeQueryParam(shareMessage, StandardCharsets.UTF_8);
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
        return portalSlug != null && PORTAL_SLUGS.contains(portalSlug.toLowerCase(Locale.US));
    }

    private String portalName(String portalSlug) {
        return switch (portalSlug.toLowerCase(Locale.US)) {
            case "bsi" -> "BSI";
            case "weirs" -> "WEIRS";
            case "swiftcomply" -> "SwiftComply";
            case "vepo" -> "VEPO/Envirotrax";
            case "aqua" -> "Aqua/TrackMyBackflow";
            case "tokay" -> "Tokay WebTest";
            case "sprybackflow" -> "SpryBackflow";
            default -> "Backflow reporting portals";
        };
    }

    private String portalDisplayName(UtilityRecord utility) {
        String portalSlug = portalSlugForUtility(utility);
        return portalSlug == null ? "reporting portal" : portalName(portalSlug);
    }

    private String portalDescription(String portalSlug) {
        return switch (portalSlug.toLowerCase(Locale.US)) {
            case "bsi" -> "Find utilities using BSI Online or Backflow Solutions for tester enrollment and backflow test report submission.";
            case "weirs" -> "Find utilities using WEIRS for tester lookup, water inspection, or backflow report submission.";
            case "swiftcomply" -> "Find utilities using SwiftComply or C3Swift for official backflow report submission, device records, and accepted filing proof.";
            case "vepo" -> "Find utilities using VEPO or Envirotrax for tester registration and backflow report submission.";
            case "aqua" -> "Find utilities using Aqua Backflow or TrackMyBackflow for Hazard ID, Site ID, test reports, filing fees, or tester registration.";
            case "tokay" -> "Find utilities using Tokay WebTest for tester approval, credentials, online backflow reports, and accepted submission proof.";
            case "sprybackflow" -> "Find utilities using SpryBackflow for official backflow test report submission, annual notices, and utility acceptance proof.";
            default -> "Find utility backflow reporting portal routes and online submission workflows.";
        };
    }

    private String portalDetailPageTitle(String portalSlug, String portalName) {
        return switch (portalSlug.toLowerCase(Locale.US)) {
            case "swiftcomply" -> "SwiftComply portal backflow report routes | BackflowPath";
            case "aqua" -> "Aqua Backflow and TrackMyBackflow portal routes | BackflowPath";
            case "tokay" -> "Tokay WebTest backflow report routes | BackflowPath";
            case "sprybackflow" -> "SpryBackflow backflow report portal routes | BackflowPath";
            default -> portalName + " backflow portal: city routes and report submission | BackflowPath";
        };
    }

    private String portalDetailPageDescription(String portalSlug, String portalName) {
        return switch (portalSlug.toLowerCase(Locale.US)) {
            case "swiftcomply" -> "Find SwiftComply and C3Swift backflow report routes by city, tester requirements, notice clues, and accepted filing proof.";
            case "aqua" -> "Find Aqua Backflow and TrackMyBackflow routes by city, including Hazard ID, Site ID, filing fee, tester gate, and accepted report proof.";
            case "tokay" -> "Find Tokay WebTest backflow report routes by utility, including tester approval, credentials, notice clues, and online submission proof.";
            case "sprybackflow" -> "Find SpryBackflow backflow report routes by utility, including annual notices, tester submission, and accepted filing proof.";
            default -> "Find " + portalName + " backflow report submission routes by city, tester requirements, notice clues, and accepted filing proof.";
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
        if (utilityContainsAny(utility, "aqua backflow", "aquabackflow", "trackmybackflow", "track my backflow")) {
            return "aqua";
        }
        if (utilityContainsAny(utility, "tokay", "webtest", "web test")) {
            return "tokay";
        }
        if (utilityContainsAny(utility, "sprybackflow", "spry backflow", "sprybackflow.aurorawater.org")) {
            return "sprybackflow";
        }
        if (utilityContainsAny(utility, "bsi", "backflow solutions", "backflowtest.com", "bsi online")) {
            return "bsi";
        }
        return null;
    }

    private String utilityPageTitle(UtilityRecord utility) {
        String location = utilitySearchLocation(utility);
        String portalVendor = utilityPortalVendor(utility);
        if (portalVendor != null) {
            String suffix = " backflow reports | " + shortenAtWord(portalVendor, 22) + " | BackflowPath";
            return shortenAtWord(location, Math.max(12, 70 - suffix.length())) + suffix;
        }
        if (utility.supportsApprovedTestersPage()) {
            return location + " utility backflow tester rules | BackflowPath";
        }
        return location + " utility backflow requirements | BackflowPath";
    }

    private String utilityPageDescription(UtilityRecord utility) {
        String location = shortenAtWord(utilitySearchLocation(utility), 46);
        String portalVendor = utilityPortalVendor(utility);
        if (portalVendor != null) {
            return "Check " + location + " backflow rules, tester eligibility, and "
                    + shortenAtWord(portalVendor, 24) + " report submission before the due date.";
        }
        return "Check " + location
                + " utility backflow rules, due dates, tester eligibility, report submission, and official sources before you act.";
    }

    private String utilityHeading(UtilityRecord utility) {
        return utilitySearchLocation(utility) + " utility backflow testing requirements";
    }

    private String utilitySubmissionLabel(UtilityRecord utility) {
        if (utility.reportWorkflow().portalName() != null && !utility.reportWorkflow().portalName().isBlank()) {
            return utility.reportWorkflow().portalName();
        }
        String portalVendor = utilityPortalVendor(utility);
        if (portalVendor != null) {
            return portalVendor + " report route";
        }
        return utility.submissionMethods().isEmpty()
                ? "Confirm with utility"
                : utility.submissionMethods().get(0).kind();
    }

    private String utilityPortalVendor(UtilityRecord utility) {
        String portalVendor = utility.reportWorkflow().portalVendor();
        if (portalVendor != null && !portalVendor.isBlank()) {
            return portalVendor.trim();
        }
        String portalName = utility.reportWorkflow().portalName();
        return portalName == null || portalName.isBlank() ? null : portalName.trim();
    }

    private String utilitySearchLocation(UtilityRecord utility) {
        if (utility.governingEntityType() != null
                && utility.governingEntityType().toLowerCase(Locale.US).contains("county")
                && !utility.serviceAreaCounties().isEmpty()) {
            return utility.serviceAreaCounties().get(0);
        }
        if (!utility.serviceAreaCities().isEmpty()) {
            return utility.serviceAreaCities().get(0);
        }
        String compactName = utility.utilityName()
                .replaceFirst("(?i)\\s+(public\\s+)?utilities.*$", "")
                .replaceFirst("(?i)\\s+cross[- ]connection.*$", "")
                .trim();
        return compactName.isBlank() ? utility.utilityName() : compactName;
    }

    private String shortenAtWord(String value, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.length() <= maxLength) {
            return text;
        }
        int boundary = text.lastIndexOf(' ', Math.max(1, maxLength));
        return (boundary > 20 ? text.substring(0, boundary) : text.substring(0, maxLength)).trim();
    }

    private String sentenceFragment(String value) {
        String fragment = value == null ? "" : value.trim();
        while (fragment.endsWith(".")) {
            fragment = fragment.substring(0, fragment.length() - 1).trim();
        }
        return fragment;
    }

    private String portalReportTitlePhrase(UtilityRecord utility) {
        String portalSlug = portalSlugForUtility(utility);
        return portalSlug == null ? "online reports" : portalName(portalSlug) + " reports";
    }

    private String reportWorkflowTitlePhrase(UtilityRecord utility) {
        String portalSlug = portalSlugForUtility(utility);
        if (portalSlug != null) {
            return portalName(portalSlug) + " reports";
        }
        if (utility.hasReportWorkflow()) {
            return "report submission";
        }
        return portalReportTitlePhrase(utility);
    }

    private String reportDeadlineTitlePhrase(UtilityRecord utility) {
        Integer days = utility.reportWorkflow().submissionDeadlineDaysAfterTest();
        if (days == null) {
            days = utility.deadlinePolicy().reportDueDaysAfterTest();
        }
        if (days == null) {
            return "";
        }
        if (days == 1) {
            return "1-day report deadline";
        }
        return days + "-day report deadline";
    }

    private String reportDeadlineMetaPhrase(UtilityRecord utility) {
        if (utility.reportWorkflow().submissionDeadlineDaysAfterTest() != null) {
            return utility.reportWorkflow().deadlineLabel();
        }
        if (utility.deadlinePolicy().reportDueDaysAfterTest() != null) {
            return utility.deadlinePolicy().reportDueLabel();
        }
        return "";
    }

    private String portalReportRoutingPhrase(UtilityRecord utility) {
        String portalSlug = portalSlugForUtility(utility);
        return portalSlug == null ? "online report routing" : portalName(portalSlug) + " report routing";
    }

    private String providerPageTitle(
            ProviderRecord provider,
            List<UtilityRecord> utilities,
            List<String> coverageCities
    ) {
        String market = coverageCities.isEmpty()
                ? (utilities.isEmpty() ? "provider profile" : utilitySearchLocation(utilities.getFirst()))
                : coverageCities.getFirst();
        String suffix = " | " + shortenAtWord(market, 24) + " | BackflowPath";
        return shortenAtWord(provider.providerName(), Math.max(12, 70 - suffix.length())) + suffix;
    }

    private String providerPageDescription(
            ProviderRecord provider,
            List<UtilityRecord> utilities,
            List<String> coverageCities
    ) {
        String market = coverageCities.isEmpty()
                ? (utilities.isEmpty() ? "a mapped utility workflow" : utilitySearchLocation(utilities.getFirst()))
                : coverageCities.getFirst();
        return shortenAtWord(provider.providerName(), 36)
                + " in " + shortenAtWord(market, 26)
                + ". Verify the utility source, credentials, due date, and report proof before booking.";
    }

    private String guidePageTitle(GuideRecord guide) {
        return switch (guide.slug()) {
            case "anniversary-date-vs-calendar-deadline" -> "Backflow test due dates | BackflowPath";
            case "backflow-test-cost" -> "Backflow test cost and fees | BackflowPath";
            case "backflow-reporting-portals" -> "SwiftComply, BSI and WEIRS backflow portals | BackflowPath";
            default -> shortenAtWord(guide.title(), 50) + " | BackflowPath";
        };
    }

    private String guidePageDescription(GuideRecord guide) {
        return switch (guide.slug()) {
            case "backflow-reporting-portals" -> "Find the right backflow reporting portal: SwiftComply, BSI, WEIRS, VEPO, Aqua, Tokay, or SpryBackflow.";
            case "residential-vs-commercial-backflow-rules" -> "How utilities split residential, commercial, hazard, irrigation, multifamily, and managed-property backflow cases.";
            default -> guide.description();
        };
    }

    private String metroPageTitle(MetroRecord metro) {
        return shortenAtWord(metro.title(), 50) + " | BackflowPath";
    }

    private String officialTesterPageTitle(UtilityRecord utility) {
        String location = utilitySearchLocation(utility);
        if (utilityContainsAny(utility, "weirs")) {
            return location + " utility WEIRS registered testers";
        }
        if (utilityContainsAny(utility, "bsi", "backflowtest.com", "backflow solutions")) {
            return location + " utility BSI tester route";
        }
        if (utilityContainsAny(utility, "vepo", "envirotrax")) {
            return location + " utility VEPO registered testers";
        }
        return location + " utility approved backflow testers";
    }

    private String officialTesterPageDescription(UtilityRecord utility) {
        return "Open the official " + utilitySearchLocation(utility)
                + " utility tester route and confirm credentials, registration, and report submission requirements.";
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

    private Map<String, String> cityIntentPathsBySlug(CityAliasRecord alias, UtilityRecord utility) {
        return cityIntentConfigs(alias, utility).stream()
                .collect(Collectors.toMap(
                        CityIntentConfig::slug,
                        intent -> cityIntentPath(alias, intent.slug()),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private List<CityIntentLink> cityIntentLinks(CityAliasRecord alias, UtilityRecord utility) {
        return cityIntentConfigs(alias, utility).stream()
                .map(intent -> new CityIntentLink(
                        intent.slug(),
                        cityIntentPath(alias, intent.slug()),
                        intent.heading(),
                        intent.description()
                ))
                .toList();
    }

    private String cityPageTitle(CityAliasRecord alias, UtilityRecord utility) {
        return alias.city() + " backflow testing requirements | BackflowPath";
    }

    private String cityPageDescription(CityAliasRecord alias, UtilityRecord utility) {
        StringBuilder description = new StringBuilder("Check ")
                .append(alias.city())
                .append(" backflow testing requirements, due dates, tester rules, ");
        if (usesPortalWorkflow(utility)) {
            description.append(portalDisplayName(utility)).append(" submission, ");
        } else {
            description.append("report submission, ");
        }
        description.append("and the governing utility workflow before you act.");
        return description.toString();
    }

    private List<CityIntentConfig> cityIntentConfigs(CityAliasRecord alias, UtilityRecord utility) {
        return List.of(
                        "annual-backflow-testing",
                        "backflow-reporting-portal",
                        "submit-backflow-report",
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
        CityIntentConfig intent = switch (slug) {
            case "annual-backflow-testing" -> annualCityIntent(alias, utility);
            case "backflow-reporting-portal" -> portalCityIntent(alias, utility);
            case "submit-backflow-report" -> submitReportCityIntent(alias, utility);
            case "approved-backflow-testers" -> approvedTesterCityIntent(alias, utility);
            case "failed-backflow-test" -> failedTestCityIntent(alias, utility);
            case "irrigation-backflow-testing" -> irrigationCityIntent(alias, utility);
            case "fire-line-backflow-testing" -> fireLineCityIntent(alias, utility);
            default -> null;
        };
        if (intent == null || !utility.supportsCityIntent(intent.slug())) {
            return null;
        }
        return intent;
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
                alias.city() + " annual backflow testing: verify who must test, the due-date rule, tester requirements, and the official submission workflow.",
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
        String heading = alias.city() + " " + portalLabel + " backflow reporting portal";
        return new CityIntentConfig(
                "backflow-reporting-portal",
                alias.city() + " " + portalLabel + " backflow reporting | BackflowPath",
                "Find the " + alias.city() + " " + portalLabel + " backflow report route, notice or device identifiers, tester gate, and proof of accepted submission.",
                "Portal city route",
                heading,
                "Use this page when a notice for " + alias.city() + " mentions BSI, SwiftComply, WEIRS, VEPO, Aqua/TrackMyBackflow, Tokay WebTest, a customer portal, or online backflow test report submission.",
                highlights,
                utility.workflowSteps(),
                portalHubPath(utility) == null ? utilityPath(utility) : portalHubPath(utility),
                portalHubLabel(utility) == null ? "Open utility submission workflow" : portalHubLabel(utility),
                List.of("backflow-test-notice-next-steps", "backflow-reporting-portals", "approved-testers-vs-find-a-tester", "backflow-test-cost")
        );
    }

    private CityIntentConfig submitReportCityIntent(CityAliasRecord alias, UtilityRecord utility) {
        if (!usesPortalWorkflow(utility)) {
            return null;
        }
        String portalLabel = portalDisplayName(utility);
        List<String> highlights = new ArrayList<>();
        if (utility.submissionMethods().isEmpty()) {
            highlights.add("Submission path: confirm the current report path with " + utility.utilityName() + ".");
        } else {
            utility.submissionMethods().forEach(method -> highlights.add("Submission path: " + method.label() + " - " + method.kind()));
        }
        highlights.add("Notice or device clue: " + noticeIdentifierHint(utility));
        highlights.add("Tester gate: " + testerAnswer(utility));
        highlights.add("Report acceptance: " + reportAcceptanceHint(utility));
        highlights.add("Due basis: " + utility.dueBasis());
        String heading = "Submit " + alias.city() + " " + portalLabel + " backflow test reports";
        return new CityIntentConfig(
                "submit-backflow-report",
                alias.city() + " " + portalLabel + " report submission | BackflowPath",
                "How to submit a backflow test report in " + alias.city() + " through " + portalLabel + ", including notice clues, tester requirements, and accepted filing proof.",
                "Report submission route",
                heading,
                "Use this page when the notice or tester workflow is about submitting, uploading, filing, or confirming a backflow test report for " + alias.city() + ".",
                highlights,
                submitReportWorkflowSteps(utility),
                utilityPath(utility),
                "Open utility source workflow",
                List.of("backflow-reporting-portals", "backflow-test-notice-next-steps", "approved-testers-vs-find-a-tester", "backflow-test-cost")
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
                alias.city() + " approved backflow testers | BackflowPath",
                "Find the official " + alias.city() + " approved backflow tester route and confirm utility registration, credentials, and report submission requirements.",
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
                alias.city() + " failed backflow test | BackflowPath",
                "What to do after a failed backflow test in " + alias.city() + ": repair, retest, and confirm accepted report submission with the utility.",
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
                alias.city() + " irrigation backflow testing | BackflowPath",
                "Check " + alias.city() + " irrigation backflow testing triggers, device rules, tester requirements, and the official utility workflow.",
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
                alias.city() + " fire-line backflow testing | BackflowPath",
                "Check " + alias.city() + " fire-line backflow testing triggers, assembly rules, tester requirements, and the official utility workflow.",
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
                .filter(alias -> canIndexCityAlias(alias, registryService.findUtilityById(alias.utilityId()).orElse(null)))
                .toList();
    }

    private List<CityAliasRecord> publishedCityAliasesForUtility(String utilityId) {
        return registryService.listCityAliases().stream()
                .filter(alias -> alias.utilityId().equals(utilityId))
                .filter(alias -> canIndexCityAlias(alias, registryService.findUtilityById(alias.utilityId()).orElse(null)))
                .toList();
    }

    private boolean canIndexUtility(UtilityRecord utility) {
        return utility != null && utility.meetsIndexQualityFloor();
    }

    private boolean canIndexCityAlias(CityAliasRecord alias, UtilityRecord utility) {
        return alias != null
                && utility != null
                && alias.aliasMode() != AliasMode.NOINDEX_BRIDGE
                && alias.lastReviewed() != null
                && alias.city() != null
                && !alias.city().isBlank()
                && alias.aliasSlug() != null
                && !alias.aliasSlug().isBlank()
                && canIndexUtility(utility);
    }

    private boolean canIndexCityIntent(CityAliasRecord alias, UtilityRecord utility, CityIntentConfig intent) {
        return intent != null
                && canIndexCityAlias(alias, utility)
                && utility.supportsCityIntent(intent.slug());
    }

    private boolean canIndexPriorityCityIntent(CityAliasRecord alias, UtilityRecord utility, CityIntentConfig intent) {
        if (!canIndexCityIntent(alias, utility, intent)) {
            return false;
        }
        return switch (intent.slug()) {
            case "submit-backflow-report" -> hasStrongSubmitReportEvidence(utility);
            case "backflow-reporting-portal" -> hasStrongPortalEvidence(utility);
            case "failed-backflow-test" -> hasStrongFailedTestEvidence(utility);
            case "approved-backflow-testers" -> hasStrongTesterEvidence(utility);
            case "annual-backflow-testing" -> hasStrongAnnualEvidence(utility);
            default -> true;
        };
    }

    private boolean hasStrongSubmitReportEvidence(UtilityRecord utility) {
        return utility.hasReportWorkflow()
                && utility.reportWorkflow().portalUrl() != null
                && !utility.reportWorkflow().portalUrl().isBlank()
                && utility.reportWorkflow().submitter() != null
                && !utility.reportWorkflow().submitter().isBlank()
                && !utility.reportWorkflow().requiredIdentifiers().isEmpty()
                && utility.reportWorkflow().acceptanceProof() != null
                && !utility.reportWorkflow().acceptanceProof().isBlank();
    }

    private boolean hasStrongPortalEvidence(UtilityRecord utility) {
        return utility.hasReportWorkflow()
                && ((utility.reportWorkflow().portalVendor() != null && !utility.reportWorkflow().portalVendor().isBlank())
                || (utility.reportWorkflow().portalName() != null && !utility.reportWorkflow().portalName().isBlank()))
                && utility.reportWorkflow().portalUrl() != null
                && !utility.reportWorkflow().portalUrl().isBlank();
    }

    private boolean hasStrongFailedTestEvidence(UtilityRecord utility) {
        return utility.hasFailedTestPolicy()
                && (utility.failedTestPolicy().repairDeadlineDays() != null
                || utility.failedTestPolicy().failedReportDeadlineHours() != null
                || utility.failedTestPolicy().retestRequired() != null
                || utility.failedTestPolicy().inspectionRequired() != null
                || utility.failedTestPolicy().shutoffRisk() != null);
    }

    private boolean hasStrongTesterEvidence(UtilityRecord utility) {
        return utility.hasTesterGate()
                && !utility.testerGate().credentialDocuments().isEmpty()
                && (utility.testerGate().licenseRequired() != null
                || utility.testerGate().utilityRegistrationRequired() != null
                || utility.testerGate().portalEnrollmentRequired() != null);
    }

    private boolean hasStrongAnnualEvidence(UtilityRecord utility) {
        return utility.hasDeadlinePolicy()
                && (utility.deadlinePolicy().reportDueDaysAfterTest() != null
                || !utility.deadlinePolicy().cadenceByPropertyType().isEmpty()
                || utility.deadlinePolicy().calendarWindow() != null && !utility.deadlinePolicy().calendarWindow().isBlank());
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
        String searchTitle = utilityFocusPageTitle(utility, eyebrow);
        String metaDescription = utilityFocusMetaDescription(utility, eyebrow);
        String requestHelpPath = LeadRoutingService.requestHelpPath(
                utility.utilityId(),
                path,
                eyebrow.toLowerCase(Locale.ROOT).replace(" ", "-"),
                "utility-focus"
        );
        model.addAttribute("page", page(
                searchTitle,
                metaDescription,
                path,
                combineStructuredData(
                        breadcrumbStructuredData(List.of(
                                new BreadcrumbItem("Home", canonical("/")),
                                new BreadcrumbItem(stateLabel(utility.state()), canonical("/states/" + utility.state() + "/backflow-testing")),
                                new BreadcrumbItem(utility.utilityName(), canonical(utilityPath(utility))),
                                new BreadcrumbItem(titleStem, canonical(path))
                        )),
                        webPageStructuredData(searchTitle, metaDescription, path, utility.lastVerified(), utilityAbout(utility), utility.sources()),
                        utilityServiceStructuredData(utility, path, searchTitle, metaDescription),
                        utilityAnswerCardStructuredData(utility, path, titleStem + " answer"),
                        workflowHowToStructuredData(titleStem, description, path, focus.workflowSteps()),
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
        model.addAttribute("noticeIdentifierHint", noticeIdentifierHint(utility));
        model.addAttribute("reportAcceptanceHint", reportAcceptanceHint(utility));
        model.addAttribute("requestHelpPath", requestHelpPath);
        model.addAttribute("faqItems", utilityFaqItems(utility));
        model.addAttribute("stateGuide", registryService.findPublishedStateGuide(utility.state()).orElse(null));
        model.addAttribute("relatedGuides", utilitySupportGuides(utility));
        return "pages/utility-focus-page";
    }

    private String utilityFocusPageTitle(UtilityRecord utility, String focus) {
        String location = utilitySearchLocation(utility);
        if (focus.equalsIgnoreCase("annual testing") && utilityContainsAny(utility, "irvine ranch", "irwd")) {
            return location + " backflow prevention and annual test | BackflowPath";
        }
        if (focus.equalsIgnoreCase("annual testing") && utilityContainsAny(utility, "parker water", "pwsd")) {
            return location + " backflow preventer testing and portal | BackflowPath";
        }
        return switch (focus.toLowerCase(Locale.US)) {
            case "annual testing" -> location + " utility annual backflow test | BackflowPath";
            case "failed test" -> location + " utility failed backflow test and retest | BackflowPath";
            case "approved testers" -> location + " utility approved backflow testers | BackflowPath";
            case "find a tester" -> location + " utility backflow tester directory | BackflowPath";
            case "irrigation" -> location + " utility irrigation backflow test | BackflowPath";
            case "fire line" -> location + " utility fire-line backflow test | BackflowPath";
            default -> location + " utility backflow requirements | BackflowPath";
        };
    }

    private String utilityFocusMetaDescription(UtilityRecord utility, String focus) {
        String location = utilitySearchLocation(utility);
        return switch (focus.toLowerCase(Locale.US)) {
            case "annual testing" -> "Check official " + location + " utility annual backflow rules, due dates, tester eligibility, and report submission steps.";
            case "failed test" -> "Check official " + location + " utility failed-test repair, retest, deadline, and report submission steps.";
            case "approved testers" -> "Open the official " + location + " utility tester route and confirm credentials, registration, and filing requirements.";
            case "find a tester" -> "Compare " + location + " providers after checking the utility's tester eligibility and report submission rules.";
            case "irrigation" -> "Check official " + location + " utility irrigation backflow triggers, device rules, tester eligibility, and filing steps.";
            case "fire line" -> "Check official " + location + " utility fire-line backflow triggers, assembly rules, tester eligibility, and filing steps.";
            default -> "Check official " + location + " utility backflow requirements, due dates, tester rules, and report submission steps.";
        };
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

    private List<FaqItem> noticeFinderFaqItems() {
        return List.of(
                new FaqItem(
                        "What should I paste into the BackflowPath notice finder?",
                        "Paste the city, utility, portal name, notice identifier, account clue, device clue, approved-tester wording, due-date wording, or failed-test phrase from the notice."
                ),
                new FaqItem(
                        "Which portal names can the notice finder route?",
                        "The finder recognizes BSI, Backflow Solutions, SwiftComply, C3Swift, WEIRS, VEPO, Envirotrax, Aqua Backflow, TrackMyBackflow, Tokay, and Tokay WebTest when those terms match source-backed pages."
                ),
                new FaqItem(
                        "What notice identifiers matter before scheduling a tester?",
                        "Keep the due date, service address, account number, CCN, Hazard ID, Site ID, device ID, assembly serial, or portal record visible so the tester can match the utility workflow."
                ),
                new FaqItem(
                        "What should I do if the notice says failed backflow test?",
                        "Open the failed-test route first. A failed assembly usually needs repair, retest, and accepted report submission, not only a generic annual testing appointment."
                )
        );
    }

    private List<FaqItem> portalFaqItems(String portalSlug, String portalName, List<UtilityRecord> utilities) {
        String utilityCount = utilities.size() == 1 ? "1 mapped utility" : utilities.size() + " mapped utilities";
        String familyNames = "all".equals(portalSlug)
                ? "BSI, SwiftComply, WEIRS, VEPO, Envirotrax, Aqua/TrackMyBackflow, Tokay WebTest, and local online portals"
                : portalName;
        return List.of(
                new FaqItem(
                        "Is " + portalName + " the same thing as the local backflow rule?",
                        "No. The portal may handle report submission, but the city, county, water district, or utility still controls deadlines, tester acceptance, fees, and failed-test handling."
                ),
                new FaqItem(
                        "What should I compare before using " + portalName + " for a backflow report?",
                        "Compare the utility name, service address, notice or device identifier, approved tester gate, report acceptance rule, filing fee, due window, and failed-test instructions."
                ),
                new FaqItem(
                        "How many BackflowPath utility workflows mention " + familyNames + "?",
                        "This portal view currently groups " + utilityCount + " with source-backed portal or online submission evidence."
                ),
                new FaqItem(
                        "Can any backflow tester submit through " + portalName + "?",
                        "Do not assume that. Many portals still require accepted tester credentials, current certification, license, insurance, gauge calibration, or separate utility approval before reports are accepted."
                )
        );
    }

    private List<FaqItem> submitReportHubFaqItems() {
        return List.of(
                new FaqItem(
                        "How do I know where to submit a backflow test report?",
                        "Start with the city or utility named on the notice, then match the portal name, device or account clue, accepted tester route, and report acceptance rule before filing."
                ),
                new FaqItem(
                        "Is a passed backflow test enough to close the compliance cycle?",
                        "No. Many utilities require the report to be entered through a named portal or online submission path. Keep proof that the report was submitted and accepted."
                ),
                new FaqItem(
                        "Which backflow report portals appear in BackflowPath?",
                        "BackflowPath currently groups source-backed routes for BSI, WEIRS, SwiftComply, VEPO, Envirotrax, Aqua Backflow, TrackMyBackflow, Tokay WebTest, SpryBackflow, and utility-run online submission workflows."
                ),
                new FaqItem(
                        "What should the owner keep after the tester files a report?",
                        "Keep the notice, due date, service address, device identifier, tester name, portal confirmation, accepted report receipt, or account history showing the submission was accepted."
                )
        );
    }

    private List<FaqItem> cityIntentFaqItems(CityAliasRecord alias, UtilityRecord utility, CityIntentConfig intent) {
        List<FaqItem> items = new ArrayList<>();
        if ("backflow-reporting-portal".equals(intent.slug())) {
            items.add(new FaqItem(
                    "Which backflow reporting portal should " + alias.city() + " use?",
                    alias.city() + " maps to " + utility.utilityName() + ". The stored portal context is " + portalDisplayName(utility) + ". " + reportAcceptanceHint(utility)
            ));
            items.add(new FaqItem(
                    "What notice or device ID should I keep for " + alias.city() + "?",
                    noticeIdentifierHint(utility)
            ));
        } else if ("annual-backflow-testing".equals(intent.slug())) {
            items.add(new FaqItem(
                    "Does " + alias.city() + " require annual backflow testing?",
                    utility.testingFrequency() + " " + utility.dueBasis()
            ));
            items.add(new FaqItem(
                    "What should I check on an annual notice for " + alias.city() + "?",
                    "Check the due date, service address, device record, accepted tester route, and submission method before scheduling."
            ));
        } else if ("approved-backflow-testers".equals(intent.slug())) {
            items.add(new FaqItem(
                    "Where should I find approved backflow testers for " + alias.city() + "?",
                    testerAnswer(utility)
            ));
            items.add(new FaqItem(
                    "Can I use a generic backflow tester search for " + alias.city() + "?",
                    "Use generic provider discovery only after the governing utility workflow is clear. Approval, reporting, and credential rules can be utility-specific."
            ));
        } else if ("submit-backflow-report".equals(intent.slug())) {
            items.add(new FaqItem(
                    "How do I submit a backflow test report for " + alias.city() + "?",
                    submissionAnswer(utility) + " " + reportAcceptanceHint(utility)
            ));
            items.add(new FaqItem(
                    "What information should be ready before filing the " + alias.city() + " report?",
                    noticeIdentifierHint(utility) + " Also keep the due date, service address, tester credential status, device type, and proof of submission."
            ));
            items.add(new FaqItem(
                    "Does the tester or owner submit the " + alias.city() + " report?",
                    "The field tester often controls portal entry, but the owner should keep the notice, due date, and proof that the report was accepted by " + utility.utilityName() + "."
            ));
        } else if ("failed-backflow-test".equals(intent.slug())) {
            items.add(new FaqItem(
                    "What should I do after a failed backflow test in " + alias.city() + "?",
                    utility.failureHighlights().isEmpty()
                            ? "Confirm repair, retest, and accepted report submission with " + utility.utilityName() + " before assuming the issue is closed."
                            : utility.failureHighlights().get(0)
            ));
            items.add(new FaqItem(
                    "Does a failed test still need report submission in " + alias.city() + "?",
                    reportAcceptanceHint(utility)
            ));
        } else {
            items.add(new FaqItem(
                    "Which utility controls this " + alias.city() + " backflow route?",
                    alias.city() + " maps to " + utility.utilityName() + ". " + alias.justification()
            ));
            items.add(new FaqItem(
                    "What should I verify before scheduling in " + alias.city() + "?",
                    schedulingChecklistAnswer(utility)
            ));
        }
        items.add(new FaqItem(
                "Who controls the rule for " + alias.city() + "?",
                alias.city() + " search demand is routed to " + utility.utilityName() + ". " + utility.whoIsAffected()
        ));
        items.add(new FaqItem(
                "What costs or fees should I expect for " + alias.city() + "?",
                costAnswer(utility)
        ));
        return items;
    }

    private List<String> submitReportWorkflowSteps(UtilityRecord utility) {
        List<String> steps = new ArrayList<>();
        steps.add("Match the utility notice to the service address, device or assembly record, and due date.");
        if (utility.supportsApprovedTestersPage()) {
            steps.add("Confirm the tester is accepted through the governing tester-list or approval route before the report is filed.");
        } else {
            steps.add("Confirm tester eligibility with the utility or portal before treating the report as accepted.");
        }
        if (utility.submissionMethods().isEmpty()) {
            steps.add("Use the official utility page or program phone to confirm the current test-report submission path.");
        } else {
            steps.add("File the result through the stored submission path: " + utility.submissionMethods().stream()
                    .map(method -> method.label())
                    .collect(Collectors.joining(", ")) + ".");
        }
        steps.add("Keep proof that the report was submitted and accepted; a passed field test alone may not close the compliance cycle.");
        if (!utility.failureHighlights().isEmpty()) {
            steps.add("If the assembly failed, follow the repair, retest, and resubmission sequence before assuming compliance is restored.");
        }
        return steps;
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
                || value.contains("aqua backflow")
                || value.contains("aquabackflow")
                || value.contains("trackmybackflow")
                || value.contains("tokay")
                || value.contains("webtest")
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
        append(text, utility.reportWorkflow().portalVendor());
        append(text, utility.reportWorkflow().portalName());
        append(text, utility.reportWorkflow().portalUrl());
        append(text, utility.reportWorkflow().submitter());
        append(text, utility.reportWorkflow().acceptanceProof());
        utility.reportWorkflow().requiredIdentifiers().forEach(value -> append(text, value));
        utility.reportWorkflow().sourceRefs().forEach(value -> append(text, value));
        append(text, utility.testerGate().deviceScopeLimit());
        utility.testerGate().credentialDocuments().forEach(value -> append(text, value));
        utility.testerGate().sourceRefs().forEach(value -> append(text, value));
        utility.deadlinePolicy().cadenceByPropertyType().forEach(value -> append(text, value));
        utility.deadlinePolicy().pastDueLadder().forEach(value -> append(text, value));
        append(text, utility.deadlinePolicy().calendarWindow());
        utility.failedTestPolicy().sourceRefs().forEach(value -> append(text, value));
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

    private String webPageStructuredData(
            String name,
            String description,
            String path,
            LocalDate dateModified,
            List<String> about,
            List<SourceLink> citations
    ) {
        String canonicalUrl = canonical(path);
        ObjectNode json = jsonLdObject("WebPage");
        json.put("@id", canonicalUrl + "#webpage");
        json.put("url", canonicalUrl);
        json.put("name", name);
        json.put("description", description);
        ObjectNode site = typedObject("WebSite");
        site.put("name", "BackflowPath");
        site.put("url", canonical("/"));
        json.set("isPartOf", site);
        ObjectNode reviewer = typedObject("Organization");
        reviewer.put("name", "BackflowPath editorial review");
        json.set("reviewedBy", reviewer);
        if (dateModified != null) {
            json.put("dateModified", dateModified.toString());
        }
        List<String> aboutItems = about == null ? List.of() : about.stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .limit(8)
                .toList();
        if (!aboutItems.isEmpty()) {
            ArrayNode aboutArray = jsonMapper.createArrayNode();
            for (String aboutItem : aboutItems) {
                ObjectNode thing = typedObject("Thing");
                thing.put("name", aboutItem);
                aboutArray.add(thing);
            }
            json.set("about", aboutArray);
        }
        List<SourceLink> citedSources = citations == null ? List.of() : citations.stream()
                .filter(source -> source != null && source.url() != null && !source.url().isBlank())
                .limit(6)
                .toList();
        if (!citedSources.isEmpty()) {
            ArrayNode citationArray = jsonMapper.createArrayNode();
            for (SourceLink source : citedSources) {
                ObjectNode citation = typedObject("CreativeWork");
                citation.put("name", source.label() == null || source.label().isBlank() ? source.url() : source.label());
                citation.put("url", source.url());
                citationArray.add(citation);
            }
            json.set("citation", citationArray);
        }
        return jsonString(json);
    }

    private String utilityServiceStructuredData(
            UtilityRecord utility,
            String path,
            String name,
            String description
    ) {
        String canonicalUrl = canonical(path);
        ObjectNode json = jsonLdObject("Service");
        json.put("@id", canonicalUrl + "#service");
        json.put("name", stripSiteSuffix(name));
        json.put("description", description);
        json.put("serviceType", "Backflow testing compliance guidance");
        json.put("url", canonicalUrl);
        ObjectNode provider = typedObject("Organization");
        provider.put("name", "BackflowPath");
        provider.put("url", canonical("/"));
        json.set("provider", provider);
        json.set("areaServed", utilityAreaServedArray(utility));
        ObjectNode audience = typedObject("Audience");
        audience.put("audienceType", "Property owners and backflow testers");
        json.set("audience", audience);
        if (utility.lastVerified() != null) {
            json.put("dateModified", utility.lastVerified().toString());
        }
        return jsonString(json);
    }

    private String utilityAnswerCardStructuredData(
            UtilityRecord utility,
            String path,
            String name
    ) {
        List<StructuredAnswerTerm> terms = new ArrayList<>();
        addAnswerTerm(terms, "Governing utility", utility.utilityName());
        addAnswerTerm(terms, "Testing or deadline rule", firstNonBlank(utility.testingFrequency(), utility.dueBasis()));
        addAnswerTerm(terms, "Tester gate", testerGateAnswer(utility));
        addAnswerTerm(terms, "Report route", reportRouteAnswer(utility));
        addAnswerTerm(terms, "Acceptance proof", firstNonBlank(utility.reportWorkflow().acceptanceProof(), reportAcceptanceHint(utility)));
        addAnswerTerm(terms, "Failed-test rule", failedTestAnswer(utility));
        if (utility.lastVerified() != null) {
            addAnswerTerm(terms, "Last verified", utility.lastVerified().toString());
        }
        if (terms.size() < 3) {
            return null;
        }

        String canonicalUrl = canonical(path);
        ObjectNode json = jsonLdObject("DefinedTermSet");
        json.put("@id", canonicalUrl + "#answer-card");
        json.put("name", name);
        json.put("url", canonicalUrl);
        json.put("description", "Source-backed answer fields for the local backflow workflow.");
        ArrayNode termArray = jsonMapper.createArrayNode();
        for (StructuredAnswerTerm term : terms) {
            ObjectNode termNode = typedObject("DefinedTerm");
            termNode.put("name", term.name());
            termNode.put("description", term.description());
            termArray.add(termNode);
        }
        json.set("hasDefinedTerm", termArray);
        return jsonString(json);
    }

    private ArrayNode utilityAreaServedArray(UtilityRecord utility) {
        ArrayNode entries = jsonMapper.createArrayNode();
        for (String city : utility.serviceAreaCities().stream().filter(value -> value != null && !value.isBlank()).limit(8).toList()) {
            ObjectNode cityNode = typedObject("City");
            cityNode.put("name", city);
            entries.add(cityNode);
        }
        for (String county : utility.serviceAreaCounties().stream().filter(value -> value != null && !value.isBlank()).limit(4).toList()) {
            ObjectNode countyNode = typedObject("AdministrativeArea");
            countyNode.put("name", county);
            entries.add(countyNode);
        }
        ObjectNode stateNode = typedObject("AdministrativeArea");
        stateNode.put("name", stateLabel(utility.state()));
        entries.add(stateNode);
        return entries;
    }

    private void addAnswerTerm(List<StructuredAnswerTerm> terms, String name, String description) {
        if (description != null && !description.isBlank()) {
            terms.add(new StructuredAnswerTerm(name, shortenAtWord(description, 220)));
        }
    }

    private String testerGateAnswer(UtilityRecord utility) {
        if (utility.supportsApprovedTestersPage()) {
            return firstNonBlank(utility.officialListLabel(), "Use the official utility approved tester route.");
        }
        if (utility.supportsFindATesterPage()) {
            return "Use provider options only after checking the utility tester and report submission rules.";
        }
        if (utility.hasTesterGate()) {
            return utility.testerGate().credentialSummary();
        }
        return "";
    }

    private String reportRouteAnswer(UtilityRecord utility) {
        ReportWorkflow workflow = utility.reportWorkflow();
        String portal = firstNonBlank(workflow.portalName(), workflow.portalVendor());
        if (portal != null && !portal.isBlank()) {
            return portal + " handles the report route.";
        }
        if (!utility.submissionMethods().isEmpty()) {
            SubmissionMethod method = utility.submissionMethods().get(0);
            return firstNonBlank(method.label(), method.kind());
        }
        return "";
    }

    private String failedTestAnswer(UtilityRecord utility) {
        FailedTestPolicy policy = utility.failedTestPolicy();
        return firstNonBlank(
                policy.repairDeadlineLabel(),
                policy.failedReportDeadlineLabel(),
                utility.failureHighlights().isEmpty() ? "" : utility.failureHighlights().get(0),
                utility.penalties()
        );
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String stripSiteSuffix(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(" | BackflowPath", "");
    }

    private List<String> utilityAbout(UtilityRecord utility) {
        List<String> about = new ArrayList<>();
        about.add("Backflow testing");
        about.add("Cross-connection control");
        about.add(utility.utilityName());
        about.add(stateLabel(utility.state()));
        if (usesPortalWorkflow(utility)) {
            about.add(portalDisplayName(utility));
        }
        if (utility.supportsApprovedTestersPage()) {
            about.add("Approved backflow testers");
        }
        return about;
    }

    private List<String> cityAbout(CityAliasRecord alias, UtilityRecord utility, String intentName) {
        List<String> about = new ArrayList<>(utilityAbout(utility));
        about.add(alias.city());
        about.add(intentName);
        return about;
    }

    private String faqStructuredData(List<FaqItem> faqItems) {
        ObjectNode json = jsonLdObject("FAQPage");
        ArrayNode questions = jsonMapper.createArrayNode();
        for (FaqItem item : faqItems) {
            ObjectNode question = typedObject("Question");
            question.put("name", item.question());
            ObjectNode answer = typedObject("Answer");
            answer.put("text", item.answer());
            question.set("acceptedAnswer", answer);
            questions.add(question);
        }
        json.set("mainEntity", questions);
        return jsonString(json);
    }

    private String noticeFinderStructuredData() {
        String noticeFinderUrl = canonical("/notice-finder");
        ObjectNode json = jsonLdObject("WebApplication");
        json.put("@id", noticeFinderUrl + "#notice-finder");
        json.put("name", "Backflow notice finder");
        json.put("url", noticeFinderUrl);
        json.put("applicationCategory", "BusinessApplication");
        json.put("operatingSystem", "Web");
        json.put("description", "Search a city, utility, portal name, notice identifier, tester clue, due date, or failed-test phrase to find the source-backed BackflowPath route.");
        ArrayNode features = jsonMapper.createArrayNode();
        List.of(
                "City and utility matching",
                "Reporting portal routing",
                "Notice and device identifier clues",
                "Approved tester route matching",
                "Failed-test and retest routing"
        ).forEach(features::add);
        json.set("featureList", features);
        ObjectNode action = typedObject("SearchAction");
        action.put("target", noticeFinderUrl + "?q={search_term_string}");
        action.put("query-input", "required name=search_term_string");
        json.set("potentialAction", action);
        return jsonString(json);
    }

    private String portalItemListStructuredData(String name, List<UtilityRecord> utilities) {
        List<StructuredListItem> items = new ArrayList<>();
        Set<String> seenUrls = new LinkedHashSet<>();
        for (UtilityRecord utility : utilities) {
            addStructuredListItem(
                    items,
                    seenUrls,
                    utility.utilityName() + " utility workflow",
                    canonical(utilityPath(utility))
            );
            for (CityAliasRecord alias : publishedCityAliasesForUtility(utility.utilityId())) {
                if (cityIntentConfig("submit-backflow-report", alias, utility) != null) {
                    addStructuredListItem(
                            items,
                            seenUrls,
                            alias.city() + " submit backflow report",
                            canonical(cityIntentPath(alias, "submit-backflow-report"))
                    );
                }
                if (cityIntentConfig("backflow-reporting-portal", alias, utility) != null) {
                    addStructuredListItem(
                            items,
                            seenUrls,
                            alias.city() + " backflow reporting portal",
                            canonical(cityIntentPath(alias, "backflow-reporting-portal"))
                    );
                }
            }
        }
        if (items.isEmpty()) {
            return null;
        }

        ObjectNode json = jsonLdObject("ItemList");
        json.put("name", name);
        ArrayNode itemArray = jsonMapper.createArrayNode();
        for (int i = 0; i < items.size(); i++) {
            StructuredListItem item = items.get(i);
            ObjectNode listItem = typedObject("ListItem");
            listItem.put("position", i + 1);
            listItem.put("name", item.name());
            listItem.put("url", item.url());
            itemArray.add(listItem);
        }
        json.set("itemListElement", itemArray);
        return jsonString(json);
    }

    private void addStructuredListItem(
            List<StructuredListItem> items,
            Set<String> seenUrls,
            String name,
            String url
    ) {
        if (url == null || url.isBlank() || seenUrls.contains(url) || items.size() >= 120) {
            return;
        }
        seenUrls.add(url);
        items.add(new StructuredListItem(name, url));
    }

    private String cityIntentHowToStructuredData(CityAliasRecord alias, CityIntentConfig intent) {
        return workflowHowToStructuredData(
                intent.heading(),
                intent.description(),
                cityIntentPath(alias, intent.slug()),
                intent.workflowSteps()
        );
    }

    private String workflowHowToStructuredData(String name, String description, String path, List<String> steps) {
        if (steps == null || steps.isEmpty()) {
            return null;
        }
        ObjectNode json = jsonLdObject("HowTo");
        json.put("name", name);
        json.put("description", description);
        json.put("url", canonical(path));
        ArrayNode stepArray = jsonMapper.createArrayNode();
        for (int i = 0; i < steps.size(); i++) {
            String step = steps.get(i);
            ObjectNode stepNode = typedObject("HowToStep");
            stepNode.put("position", i + 1);
            stepNode.put("name", howToStepName(step, i + 1));
            stepNode.put("text", step);
            stepArray.add(stepNode);
        }
        json.set("step", stepArray);
        return jsonString(json);
    }

    private String howToStepName(String step, int position) {
        if (step == null || step.isBlank()) {
            return "Step " + position;
        }
        String normalized = step.replaceAll("\\s+", " ").trim();
        int sentenceEnd = normalized.indexOf('.');
        if (sentenceEnd > 0) {
            normalized = normalized.substring(0, sentenceEnd);
        }
        return shortenAtWord(normalized, 72);
    }

    private String breadcrumbStructuredData(List<BreadcrumbItem> items) {
        ObjectNode json = jsonLdObject("BreadcrumbList");
        ArrayNode itemArray = jsonMapper.createArrayNode();
        for (int i = 0; i < items.size(); i++) {
            BreadcrumbItem item = items.get(i);
            ObjectNode listItem = typedObject("ListItem");
            listItem.put("position", i + 1);
            listItem.put("name", item.name());
            ObjectNode nestedItem = jsonMapper.createObjectNode();
            nestedItem.put("@id", item.url());
            nestedItem.put("name", item.name());
            listItem.set("item", nestedItem);
            itemArray.add(listItem);
        }
        json.set("itemListElement", itemArray);
        return jsonString(json);
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

    private ObjectNode jsonLdObject(String type) {
        ObjectNode node = typedObject(type);
        node.put("@context", "https://schema.org");
        return node;
    }

    private ObjectNode typedObject(String type) {
        ObjectNode node = jsonMapper.createObjectNode();
        node.put("@type", type);
        return node;
    }

    private String jsonString(JsonNode node) {
        try {
            return jsonMapper.writeValueAsString(node);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize JSON-LD.", exception);
        }
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
        LocalDate latest = effectiveSitemapLastModified(null);
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

    private LocalDate effectiveSitemapLastModified(LocalDate recordLastModified) {
        LocalDate contentLastModified = siteProperties.contentLastModified();
        if (contentLastModified == null) {
            return recordLastModified == null ? LocalDate.of(2000, 1, 1) : recordLastModified;
        }
        if (recordLastModified == null || contentLastModified.isAfter(recordLastModified)) {
            return contentLastModified;
        }
        return recordLastModified;
    }

    private record SitemapEntry(String url, LocalDate lastModified) {
    }

    private record BreadcrumbItem(String name, String url) {
    }

    private record StructuredListItem(String name, String url) {
    }

    private record StructuredAnswerTerm(String name, String description) {
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
