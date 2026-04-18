package owner.backflow.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import owner.backflow.config.AppSiteProperties;
import owner.backflow.data.model.UtilityRecord;
import owner.backflow.files.BackflowRegistryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class VendorPagesController {
    private final BackflowRegistryService registryService;
    private final AppSiteProperties siteProperties;

    public VendorPagesController(
            BackflowRegistryService registryService,
            AppSiteProperties siteProperties
    ) {
        this.registryService = registryService;
        this.siteProperties = siteProperties;
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
        model.addAttribute("texasUtilityCount", texasUtilities.size());
        model.addAttribute("dfwUtilityCount", featuredUtilities.size());
        return "pages/vendor-customer-briefs";
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

    private String canonical(String path) {
        String baseUrl = siteProperties.baseUrl() == null ? "" : siteProperties.baseUrl().trim();
        return baseUrl.replaceAll("/+$", "") + path;
    }
}
