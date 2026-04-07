package owner.backflow.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import owner.backflow.data.model.ProviderRecord;
import owner.backflow.files.BackflowRegistryService;
import owner.backflow.service.LeadAdminService;
import owner.backflow.service.LeadRoutingContext;
import owner.backflow.service.LeadRoutingService;
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
    private final LeadRoutingService leadRoutingService;

    public LeadController(
            LeadAdminService leadAdminService,
            BackflowRegistryService registryService,
            LeadSubmissionGuardService leadSubmissionGuardService,
            LeadRoutingService leadRoutingService
    ) {
        this.leadAdminService = leadAdminService;
        this.registryService = registryService;
        this.leadSubmissionGuardService = leadSubmissionGuardService;
        this.leadRoutingService = leadRoutingService;
    }

    @GetMapping("/leads/new")
    public String newLead(
            @RequestParam(value = "utilityId", required = false) String utilityId,
            @RequestParam(value = "utilityName", required = false) String utilityName,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "issueType", required = false) String issueType,
            @RequestParam(value = "pageFamily", required = false) String pageFamily,
            @RequestParam(value = "rt", required = false) String routingToken,
            @RequestParam(value = "error", required = false) String error,
            Model model
    ) {
        String normalizedUtilityId = normalize(utilityId);
        String normalizedSourcePage = normalize(source);
        String normalizedPageFamily = normalize(pageFamily);
        LeadRoutingContext trustedContext = leadRoutingService.resolveTrustedContext(
                normalizedUtilityId,
                normalizedSourcePage,
                normalizedPageFamily,
                normalize(routingToken)
        );
        String resolvedUtilityName = registryService.findUtilityById(normalizedUtilityId)
                .map(utility -> utility.utilityName())
                .orElse(normalize(utilityName));
        List<ProviderRecord> activeSponsors = trustedContext.autoRouteEligible()
                ? registryService.findActiveSponsorsForUtility(trustedContext.utilityId())
                : List.of();
        model.addAttribute("page", new PageMeta(
                "Request backflow help | BackflowPath",
                "Share your utility, deadline, or failed-test details so BackflowPath can review the next step.",
                "/leads/new",
                true
        ));
        model.addAttribute("utilityId", normalizedUtilityId);
        model.addAttribute("utilityName", resolvedUtilityName);
        model.addAttribute("sourcePage", normalizedSourcePage);
        model.addAttribute("selectedIssueType", normalize(issueType));
        model.addAttribute("pageFamily", normalizedPageFamily);
        model.addAttribute("routingToken", trustedContext.autoRouteEligible() ? normalize(routingToken) : "");
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
            @RequestParam(value = "rt", required = false) String routingToken,
            @RequestParam(required = false) String consentToRouting,
            @RequestParam(required = false) String companyWebsite,
            HttpServletRequest request
    ) {
        if (companyWebsite != null && !companyWebsite.isBlank()) {
            return "redirect:/leads/thanks";
        }
        if (!"yes".equalsIgnoreCase(normalize(consentToRouting))) {
            return "redirect:" + leadFormRedirect(utilityId, sourcePage, issueType, pageFamily, routingToken, "consent");
        }
        if (!leadSubmissionGuardService.tryAcquire(request.getRemoteAddr())) {
            return "redirect:" + leadFormRedirect(utilityId, sourcePage, issueType, pageFamily, routingToken, "rate-limit");
        }
        LeadRoutingContext trustedContext = leadRoutingService.resolveTrustedContext(
                normalize(utilityId),
                normalize(sourcePage),
                normalize(pageFamily),
                normalize(routingToken)
        );
        leadAdminService.record(new LeadRecord(
                null,
                LocalDateTime.now(),
                fullName,
                phone,
                email,
                city,
                trustedContext.utilityId(),
                trustedContext.utilityName(),
                propertyType,
                issueType,
                trustedContext.pageFamily(),
                notes,
                trustedContext.sourcePage(),
                Optional.ofNullable(request.getHeader("Referer")).orElse(""),
                normalize(utilityId),
                normalize(utilityName),
                normalize(pageFamily),
                normalize(sourcePage),
                trustedContext.routingStatus(),
                trustedContext.routingReason()
        ));
        return "redirect:/leads/thanks";
    }

    @GetMapping("/leads/thanks")
    public String thanks(Model model) {
        model.addAttribute("page", new PageMeta(
                "Request received | BackflowPath",
                "BackflowPath received your request and will review it against the local utility context.",
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
            String routingToken,
            String errorCode
    ) {
        return UriComponentsBuilder.fromPath("/leads/new")
                .queryParamIfPresent("utilityId", nonBlank(normalize(utilityId)))
                .queryParamIfPresent("source", nonBlank(normalize(sourcePage)))
                .queryParamIfPresent("issueType", nonBlank(normalize(issueType)))
                .queryParamIfPresent("pageFamily", nonBlank(normalize(pageFamily)))
                .queryParamIfPresent("rt", nonBlank(normalize(routingToken)))
                .queryParam("error", errorCode)
                .build()
                .encode()
                .toUriString();
    }

    private Optional<String> nonBlank(String value) {
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }
}
