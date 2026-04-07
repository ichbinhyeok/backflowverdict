package owner.backflow.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import owner.backflow.config.AppSiteProperties;
import owner.backflow.service.LeadSubmissionGuardService;
import owner.backflow.service.ProviderClaimRecord;
import owner.backflow.service.ProviderClaimNotificationService;
import owner.backflow.service.ProviderClaimRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class ProviderPagesController {
    private final AppSiteProperties siteProperties;
    private final ProviderClaimRepository providerClaimRepository;
    private final ProviderClaimNotificationService providerClaimNotificationService;
    private final LeadSubmissionGuardService leadSubmissionGuardService;

    public ProviderPagesController(
            AppSiteProperties siteProperties,
            ProviderClaimRepository providerClaimRepository,
            ProviderClaimNotificationService providerClaimNotificationService,
            LeadSubmissionGuardService leadSubmissionGuardService
    ) {
        this.siteProperties = siteProperties;
        this.providerClaimRepository = providerClaimRepository;
        this.providerClaimNotificationService = providerClaimNotificationService;
        this.leadSubmissionGuardService = leadSubmissionGuardService;
    }

    @GetMapping("/for-providers")
    public String forProviders(Model model) {
        model.addAttribute("page", page(
                "For providers | BackflowPath",
                "How backflow testers and service companies can request a listing review, claim updates, and understand manual follow-up for featured placement on BackflowPath.",
                "/for-providers"
        ));
        return "pages/for-providers";
    }

    @GetMapping("/pricing")
    public String pricing(Model model) {
        model.addAttribute("page", page(
                "Pricing | BackflowPath",
                "BackflowPath provider pricing guide for listing claims, featured utility placement, and metro-level visibility after manual review.",
                "/pricing"
        ));
        return "pages/pricing";
    }

    @GetMapping("/claim-listing")
    public String claimListing(
            @RequestParam(value = "requestType", required = false) String requestType,
            @RequestParam(value = "error", required = false) String error,
            Model model
    ) {
        model.addAttribute("page", page(
                "Claim or request a listing | BackflowPath",
                "Submit a listing claim or provider request for manual review and email follow-up on BackflowPath.",
                "/claim-listing"
        ));
        model.addAttribute("selectedRequestType", normalize(requestType));
        model.addAttribute("formError", formError(normalize(error)));
        return "pages/claim-listing";
    }

    @PostMapping("/claim-listing")
    public String submitClaim(
            @RequestParam String fullName,
            @RequestParam String companyName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String serviceArea,
            @RequestParam(required = false) String requestType,
            @RequestParam(required = false) String listingReference,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String consentToReview,
            @RequestParam(required = false) String officeFax,
            HttpServletRequest request
    ) {
        if (officeFax != null && !officeFax.isBlank()) {
            return "redirect:/claim-listing/thanks";
        }
        if (!"yes".equalsIgnoreCase(normalize(consentToReview))) {
            return "redirect:" + claimRedirect(requestType, "consent");
        }
        if (!leadSubmissionGuardService.tryAcquire(request.getRemoteAddr())) {
            return "redirect:" + claimRedirect(requestType, "rate-limit");
        }

        ProviderClaimRecord savedClaim = providerClaimRepository.save(new ProviderClaimRecord(
                null,
                LocalDateTime.now(),
                fullName,
                companyName,
                email,
                phone,
                website,
                serviceArea,
                requestType,
                listingReference,
                notes,
                Optional.ofNullable(request.getHeader("Referer")).orElse("")
        ));
        providerClaimNotificationService.notifyInbox(savedClaim);
        return "redirect:/claim-listing/thanks";
    }

    @GetMapping("/claim-listing/thanks")
    public String claimThanks(Model model) {
        model.addAttribute("page", new PageMeta(
                "Listing request received | BackflowPath",
                "BackflowPath received your provider listing request and will review it before any publication, billing, or email follow-up.",
                canonical("/claim-listing/thanks"),
                true
        ));
        return "pages/claim-listing-thanks";
    }

    private PageMeta page(String title, String description, String path) {
        return new PageMeta(title, description, canonical(path), false);
    }

    private String canonical(String path) {
        String baseUrl = siteProperties.baseUrl() == null ? "" : siteProperties.baseUrl().trim().replaceAll("/+$", "");
        return baseUrl.isBlank() ? path : baseUrl + path;
    }

    private String formError(String errorCode) {
        return switch (errorCode) {
            case "consent" -> "Consent is required before BackflowPath can review or store a provider listing request.";
            case "rate-limit" -> "Too many requests came from this network in a short window. Please wait a few minutes and try again.";
            default -> "";
        };
    }

    private String claimRedirect(String requestType, String errorCode) {
        return UriComponentsBuilder.fromPath("/claim-listing")
                .queryParamIfPresent("requestType", nonBlank(normalize(requestType)))
                .queryParam("error", errorCode)
                .build()
                .encode()
                .toUriString();
    }

    private Optional<String> nonBlank(String value) {
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
