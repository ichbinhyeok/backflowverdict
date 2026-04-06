package owner.backflow.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import owner.backflow.data.model.ProviderRecord;
import owner.backflow.files.BackflowRegistryService;
import owner.backflow.service.LeadAdminService;
import owner.backflow.service.LeadRecord;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LeadController {
    private final LeadAdminService leadAdminService;
    private final BackflowRegistryService registryService;

    public LeadController(LeadAdminService leadAdminService, BackflowRegistryService registryService) {
        this.leadAdminService = leadAdminService;
        this.registryService = registryService;
    }

    @GetMapping("/leads/new")
    public String newLead(
            @RequestParam(value = "utilityId", required = false) String utilityId,
            @RequestParam(value = "utilityName", required = false) String utilityName,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "issueType", required = false) String issueType,
            @RequestParam(value = "pageFamily", required = false) String pageFamily,
            Model model
    ) {
        String normalizedUtilityId = normalize(utilityId);
        String resolvedUtilityName = registryService.findUtilityById(normalizedUtilityId)
                .map(utility -> utility.utilityName())
                .orElse(normalize(utilityName));
        List<ProviderRecord> activeSponsors = registryService.findActiveSponsorsForUtility(normalizedUtilityId);
        model.addAttribute("page", new PageMeta(
                "Request backflow help | BackflowPath",
                "Send a lead request for backflow testing, repair, or retest work.",
                "/leads/new",
                true
        ));
        model.addAttribute("utilityId", normalizedUtilityId);
        model.addAttribute("utilityName", resolvedUtilityName);
        model.addAttribute("sourcePage", normalize(source));
        model.addAttribute("selectedIssueType", normalize(issueType));
        model.addAttribute("pageFamily", normalize(pageFamily));
        model.addAttribute("activeSponsorCount", activeSponsors.size());
        model.addAttribute("activeSponsorEmails", activeSponsors.stream()
                .map(ProviderRecord::email)
                .filter(emailAddress -> emailAddress != null && !emailAddress.isBlank())
                .toList());
        return "pages/lead-capture";
    }

    @PostMapping("/leads")
    public String createLead(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String utilityId,
            @RequestParam(required = false) String utilityName,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) String issueType,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String sourcePage,
            @RequestParam(required = false) String pageFamily,
            HttpServletRequest request
    ) {
        leadAdminService.record(new LeadRecord(
                null,
                LocalDateTime.now(),
                fullName,
                phone,
                email,
                city,
                utilityId,
                utilityName,
                propertyType,
                issueType,
                pageFamily,
                notes,
                sourcePage,
                Optional.ofNullable(request.getHeader("Referer")).orElse("")
        ));
        return "redirect:/leads/thanks";
    }

    @GetMapping("/leads/thanks")
    public String thanks(Model model) {
        model.addAttribute("page", new PageMeta(
                "Request received | BackflowPath",
                "We received your request and stored it for review.",
                "/leads/thanks",
                true
        ));
        return "pages/lead-thanks";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
