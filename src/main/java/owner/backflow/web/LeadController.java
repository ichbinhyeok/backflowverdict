package owner.backflow.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import owner.backflow.data.model.ProviderRecord;
import owner.backflow.files.BackflowRegistryService;
import owner.backflow.service.LeadAdminService;
import owner.backflow.service.LeadSubmissionGuardService;
import owner.backflow.service.LeadRecord;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class LeadController {
    private final LeadAdminService leadAdminService;
    private final BackflowRegistryService registryService;
    private final LeadSubmissionGuardService leadSubmissionGuardService;

    public LeadController(
            LeadAdminService leadAdminService,
            BackflowRegistryService registryService,
            LeadSubmissionGuardService leadSubmissionGuardService
    ) {
        this.leadAdminService = leadAdminService;
        this.registryService = registryService;
        this.leadSubmissionGuardService = leadSubmissionGuardService;
    }

    @GetMapping("/leads/new")
    public String newLead(
            @RequestParam(value = "utilityId", required = false) String utilityId,
            @RequestParam(value = "utilityName", required = false) String utilityName,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "issueType", required = false) String issueType,
            @RequestParam(value = "pageFamily", required = false) String pageFamily,
            @RequestParam(value = "error", required = false) String error,
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
        model.addAttribute("formError", formError(normalize(error)));
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
            @RequestParam(required = false) String consentToRouting,
            @RequestParam(required = false) String companyWebsite,
            HttpServletRequest request
    ) {
        if (companyWebsite != null && !companyWebsite.isBlank()) {
            return "redirect:/leads/thanks";
        }
        if (!"yes".equalsIgnoreCase(normalize(consentToRouting))) {
            return "redirect:" + leadFormRedirect(utilityId, sourcePage, issueType, pageFamily, "consent");
        }
        if (!leadSubmissionGuardService.tryAcquire(request.getRemoteAddr())) {
            return "redirect:" + leadFormRedirect(utilityId, sourcePage, issueType, pageFamily, "rate-limit");
        }
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

    private String formError(String errorCode) {
        return switch (errorCode) {
            case "consent" -> "Consent is required before BackflowPath can store or route your request.";
            case "rate-limit" -> "Too many requests came from this network in a short window. Please wait a few minutes and try again.";
            default -> "";
        };
    }

    private String leadFormRedirect(
            String utilityId,
            String sourcePage,
            String issueType,
            String pageFamily,
            String errorCode
    ) {
        return UriComponentsBuilder.fromPath("/leads/new")
                .queryParamIfPresent("utilityId", nonBlank(normalize(utilityId)))
                .queryParamIfPresent("source", nonBlank(normalize(sourcePage)))
                .queryParamIfPresent("issueType", nonBlank(normalize(issueType)))
                .queryParamIfPresent("pageFamily", nonBlank(normalize(pageFamily)))
                .queryParam("error", errorCode)
                .build()
                .encode()
                .toUriString();
    }

    private Optional<String> nonBlank(String value) {
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }
}
