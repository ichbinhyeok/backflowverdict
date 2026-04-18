package owner.backflow.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import owner.backflow.config.AppSiteProperties;
import owner.backflow.data.model.UtilityRecord;
import owner.backflow.files.BackflowRegistryService;
import owner.backflow.service.HandoffComposerService;
import owner.backflow.service.HandoffIssueType;
import owner.backflow.service.HandoffRecord;
import owner.backflow.service.HandoffResultStatus;
import owner.backflow.service.HandoffSubmissionStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.LocalDate;

@Controller
public class VendorPagesController {
    private final BackflowRegistryService registryService;
    private final AppSiteProperties siteProperties;
    private final HandoffComposerService handoffComposerService;

    public VendorPagesController(
            BackflowRegistryService registryService,
            AppSiteProperties siteProperties,
            HandoffComposerService handoffComposerService
    ) {
        this.registryService = registryService;
        this.siteProperties = siteProperties;
        this.handoffComposerService = handoffComposerService;
    }

    @GetMapping("/vendors/customer-briefs")
    public String vendorCustomerBriefsPage(Model model) {
        List<UtilityRecord> texasUtilities = registryService.listPublishedUtilitiesForState("texas");
        List<UtilityRecord> featuredUtilities = texasUtilities.stream()
                .filter(this::isDfwUtility)
                .limit(8)
                .toList();
        if (featuredUtilities.isEmpty()) {
            featuredUtilities = texasUtilities.stream().limit(8).toList();
        }

        Map<String, String> annualBriefPaths = new LinkedHashMap<>();
        Map<String, String> failedBriefPaths = new LinkedHashMap<>();
        Map<String, String> utilityPaths = new LinkedHashMap<>();
        for (UtilityRecord utility : featuredUtilities) {
            annualBriefPaths.put(utility.utilityId(), handoffBuilderPath(utility, "general-testing", utilityPath(utility) + "annual-testing"));
            failedBriefPaths.put(utility.utilityId(), handoffBuilderPath(utility, "failed-test-repair", utilityPath(utility) + "failed-test"));
            utilityPaths.put(utility.utilityId(), utilityPath(utility));
        }

        model.addAttribute("page", new PageMeta(
                "2-minute customer brief workflow for backflow vendors | BackflowPath",
                "Send a customer-ready backflow explanation after an annual notice or failed test without forwarding a portal screenshot or office record.",
                canonical("/vendors/customer-briefs"),
                false
        ));
        model.addAttribute("featuredUtilities", featuredUtilities);
        model.addAttribute("annualBriefPaths", annualBriefPaths);
        model.addAttribute("failedBriefPaths", failedBriefPaths);
        model.addAttribute("utilityPaths", utilityPaths);
        model.addAttribute("demoPath", trackedPath(
                "/vendors/customer-brief-demo",
                "vendor-workflow",
                "",
                "sample-demo",
                "/vendors/customer-briefs"
        ));
        model.addAttribute("texasUtilityCount", texasUtilities.size());
        model.addAttribute("dfwUtilityCount", featuredUtilities.size());
        return "pages/vendor-customer-briefs";
    }

    @GetMapping("/vendors/customer-brief-demo")
    public String vendorCustomerBriefDemoPage(Model model) {
        HandoffRecord annualDemo = annualNoticeDemo();
        HandoffRecord failedDemo = failedTestDemo();

        model.addAttribute("page", new PageMeta(
                "Customer brief demo for backflow vendor outreach | BackflowPath",
                "See the exact customer-facing brief a small backflow office can send after an annual notice or failed test, then move into the office workflow.",
                canonical("/vendors/customer-brief-demo"),
                true
        ));
        model.addAttribute("annualDemo", annualDemo);
        model.addAttribute("annualSubmissionGuidance", submissionGuidance(annualDemo));
        model.addAttribute("annualFullRuleTrackedPath", trackedPath(
                annualDemo.fullRulePath(),
                "vendor-demo",
                annualDemo.utilityId(),
                "annual-full-rule",
                "/vendors/customer-brief-demo"
        ));
        model.addAttribute("annualOfficialProgramTrackedPath", trackedPath(
                annualDemo.officialProgramUrl(),
                "vendor-demo",
                annualDemo.utilityId(),
                "annual-official-program",
                "/vendors/customer-brief-demo"
        ));
        model.addAttribute("annualStartPath", handoffBuilderPath(requiredUtility(annualDemo.utilityId()), HandoffIssueType.GENERAL_TESTING.code(), annualDemo.fullRulePath()));

        model.addAttribute("failedDemo", failedDemo);
        model.addAttribute("failedSubmissionGuidance", submissionGuidance(failedDemo));
        model.addAttribute("failedFullRuleTrackedPath", trackedPath(
                failedDemo.fullRulePath(),
                "vendor-demo",
                failedDemo.utilityId(),
                "failed-full-rule",
                "/vendors/customer-brief-demo"
        ));
        model.addAttribute("failedOfficialProgramTrackedPath", trackedPath(
                failedDemo.officialProgramUrl(),
                "vendor-demo",
                failedDemo.utilityId(),
                "failed-official-program",
                "/vendors/customer-brief-demo"
        ));
        model.addAttribute("failedStartPath", handoffBuilderPath(requiredUtility(failedDemo.utilityId()), HandoffIssueType.FAILED_TEST_REPAIR.code(), failedDemo.fullRulePath()));

        model.addAttribute("workflowPath", trackedPath(
                "/vendors/customer-briefs",
                "vendor-demo",
                "",
                "office-workflow",
                "/vendors/customer-brief-demo"
        ));
        return "pages/vendor-customer-brief-demo";
    }

    private boolean isDfwUtility(UtilityRecord utility) {
        String value = (utility.utilityName() + " " + utility.canonicalSlug()).toLowerCase(Locale.US);
        return value.contains("dallas")
                || value.contains("fort worth")
                || value.contains("fort-worth")
                || value.contains("grand prairie")
                || value.contains("grand-prairie")
                || value.contains("arlington")
                || value.contains("garland")
                || value.contains("irving")
                || value.contains("plano")
                || value.contains("frisco")
                || value.contains("lewisville")
                || value.contains("mesquite")
                || value.contains("richardson")
                || value.contains("carrollton")
                || value.contains("denton");
    }

    private String handoffBuilderPath(UtilityRecord utility, String issueType, String sourcePath) {
        return UriComponentsBuilder.fromPath("/handoffs/new")
                .queryParam("utilityId", utility.utilityId())
                .queryParam("issueType", issueType)
                .queryParam("sourcePath", sourcePath)
                .build()
                .encode()
                .toUriString();
    }

    private String utilityPath(UtilityRecord utility) {
        return "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/";
    }

    private UtilityRecord requiredUtility(String utilityId) {
        return registryService.findUtilityById(utilityId)
                .orElseThrow(() -> new IllegalStateException("Missing utility for vendor demo: " + utilityId));
    }

    private HandoffRecord annualNoticeDemo() {
        return composeDemoHandoff(
                "arlington-water",
                HandoffIssueType.GENERAL_TESTING,
                HandoffResultStatus.PASS,
                "Cedar Ridge Plaza",
                "120 Main Street, Arlington, TX",
                "ACC-ARL-2049",
                LocalDate.of(2026, 4, 18),
                LocalDate.of(2026, 5, 2),
                "Annual notice received. This sample shows the customer-facing brief an office can send after a passing annual test while the utility filing is still pending.",
                ""
        );
    }

    private HandoffRecord failedTestDemo() {
        return composeDemoHandoff(
                "fort-worth-water",
                HandoffIssueType.FAILED_TEST_REPAIR,
                HandoffResultStatus.FAIL,
                "Willow Creek Medical Center",
                "4850 Camp Bowie Boulevard, Fort Worth, TX",
                "ACC-FTW-1184",
                LocalDate.of(2026, 4, 16),
                LocalDate.of(2026, 4, 28),
                "",
                "The assembly failed the field test. Repair and a passing retest are still required before the failed-device file can close."
        );
    }

    private HandoffRecord composeDemoHandoff(
            String utilityId,
            HandoffIssueType issueType,
            HandoffResultStatus resultStatus,
            String propertyLabel,
            String siteAddress,
            String accountIdentifier,
            LocalDate testDate,
            LocalDate dueDate,
            String noticeSummary,
            String failedReason
    ) {
        return handoffComposerService.compose(
                utilityId,
                issueType.code(),
                "commercial",
                propertyLabel,
                siteAddress,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                resultStatus.code(),
                HandoffSubmissionStatus.PENDING.code(),
                testDate,
                dueDate,
                null,
                "",
                accountIdentifier,
                "Sample Backflow Office",
                "Office coordinator",
                "972-555-0144",
                "",
                "",
                "",
                "",
                "",
                noticeSummary,
                failedReason,
                "",
                "/vendors/customer-brief-demo"
        );
    }

    private String submissionGuidance(HandoffRecord handoff) {
        UtilityRecord utility = requiredUtility(handoff.utilityId());
        HandoffResultStatus resultStatus = HandoffResultStatus.from(handoff.resultStatus()).orElse(HandoffResultStatus.PASS);
        HandoffSubmissionStatus submissionStatus = HandoffSubmissionStatus.from(handoff.submissionStatus()).orElse(HandoffSubmissionStatus.PENDING);
        return handoffComposerService.submissionGuidance(utility, resultStatus, submissionStatus);
    }

    private String trackedPath(String destination, String pageFamily, String utilityId, String ctaType, String sourcePath) {
        return UriComponentsBuilder.fromPath("/r/cta")
                .queryParam("next", destination)
                .queryParam("pageFamily", pageFamily)
                .queryParam("utilityId", utilityId)
                .queryParam("ctaType", ctaType)
                .queryParam("source", sourcePath)
                .build()
                .encode()
                .toUriString();
    }

    private String canonical(String path) {
        String baseUrl = siteProperties.baseUrl() == null ? "" : siteProperties.baseUrl().trim();
        return baseUrl.replaceAll("/+$", "") + path;
    }
}
