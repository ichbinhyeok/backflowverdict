package owner.backflow.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import owner.backflow.service.LeadPhotoService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class LeadController {
    private final LeadAdminService leadAdminService;
    private final BackflowRegistryService registryService;
    private final LeadSubmissionGuardService leadSubmissionGuardService;
    private final LeadRoutingService leadRoutingService;
    private final LeadPhotoService leadPhotoService;

    public LeadController(
            LeadAdminService leadAdminService,
            BackflowRegistryService registryService,
            LeadSubmissionGuardService leadSubmissionGuardService,
            LeadRoutingService leadRoutingService,
            LeadPhotoService leadPhotoService
    ) {
        this.leadAdminService = leadAdminService;
        this.registryService = registryService;
        this.leadSubmissionGuardService = leadSubmissionGuardService;
        this.leadRoutingService = leadRoutingService;
        this.leadPhotoService = leadPhotoService;
    }

    @GetMapping({"/leads/new", "/get-help", "/get-help/"})
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
        model.addAttribute("page", new PageMeta(
                "Get help with the next backflow step | BackflowVerdict",
                "Share the utility, device, timing, and task so the request keeps the context needed for a useful next step.",
                "/get-help/",
                true
        ));
        model.addAttribute("utilityId", normalizedUtilityId);
        model.addAttribute("utilityName", resolvedUtilityName);
        model.addAttribute("sourcePage", normalizedSourcePage);
        model.addAttribute("selectedIssueType", normalize(issueType));
        model.addAttribute("pageFamily", normalizedPageFamily);
        model.addAttribute("routingToken", trustedContext.sourceContextVerified() ? normalize(routingToken) : "");
        model.addAttribute("formError", model.containsAttribute("uploadError") ? model.getAttribute("uploadError") : formError(normalize(error)));
        return "pages/lead-capture";
    }

    @PostMapping("/leads")
    public String createLead(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phone,
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
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            @RequestParam Map<String, String> fields,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        if (companyWebsite != null && !companyWebsite.isBlank()) {
            return "redirect:/leads/thanks";
        }
        if (!"yes".equalsIgnoreCase(normalize(consentToRouting))) {
            preserveInput(fields, redirectAttributes);
            return "redirect:" + leadFormRedirect(utilityId, sourcePage, issueType, pageFamily, routingToken, "consent");
        }
        if ((normalize(phone).isBlank() && normalize(email).isBlank())
                || normalize(email).length() > 254
                || (!normalize(email).isBlank() && !normalize(email).matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) {
            preserveInput(fields, redirectAttributes);
            return "redirect:" + leadFormRedirect(utilityId, sourcePage, issueType, pageFamily, routingToken, "contact");
        }
        if (normalize(fullName).length() > 150 || normalize(phone).length() > 80 || normalize(city).length() > 200
                || normalize(notes).length() > 10000 || normalize(utilityId).length() > 200 || normalize(utilityName).length() > 500
                || normalize(issueType).length() > 80 || normalize(pageFamily).length() > 80 || normalize(sourcePage).length() > 2000) {
            preserveInput(fields, redirectAttributes);
            return "redirect:" + leadFormRedirect("", "", "", "", "", "length");
        }
        if (!leadSubmissionGuardService.tryAcquire(request.getRemoteAddr())) {
            preserveInput(fields, redirectAttributes);
            return "redirect:" + leadFormRedirect(utilityId, sourcePage, issueType, pageFamily, routingToken, "rate-limit");
        }
        LeadRoutingContext trustedContext = leadRoutingService.resolveTrustedContext(
                normalize(utilityId),
                normalize(sourcePage),
                normalize(pageFamily),
                normalize(routingToken)
        );
        List<byte[]> preparedPhotos;
        try { preparedPhotos = leadPhotoService.prepare(photos); }
        catch (IllegalArgumentException exception) {
            preserveInput(fields, redirectAttributes);
            redirectAttributes.addFlashAttribute("uploadError", exception.getMessage());
            return "redirect:" + leadFormRedirect(utilityId, sourcePage, issueType, pageFamily, routingToken, "photos");
        }
        String leadId = UUID.randomUUID().toString();
        leadPhotoService.save(leadId, preparedPhotos);
        try {
        leadAdminService.record(new LeadRecord(
                leadId,
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
        } catch (RuntimeException exception) { leadPhotoService.deleteAll(leadId); throw exception; }
        redirectAttributes.addFlashAttribute("leadSaved", true);
        redirectAttributes.addFlashAttribute("photoCount", preparedPhotos.size());
        return "redirect:/leads/thanks";
    }

    @GetMapping("/leads/thanks")
    public String thanks(Model model) {
        model.addAttribute("page", new PageMeta(
                "Request received | BackflowVerdict",
                "BackflowVerdict received your request and will review it against the local utility context.",
                "/leads/thanks",
                true
        ));
        return "pages/lead-thanks";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private void preserveInput(Map<String, String> fields, RedirectAttributes redirectAttributes) {
        Map<String, String> values = new java.util.HashMap<>();
        for (String key : List.of("fullName", "phone", "email", "city", "propertyType", "notes")) {
            String value = normalize(fields.get(key));
            values.put(key, value.substring(0, Math.min(value.length(), 10000)));
        }
        redirectAttributes.addFlashAttribute("formValues", values);
    }

    private String formError(String errorCode) {
        return switch (errorCode) {
            case "contact" -> "Enter a valid email address or phone number so we can reply.";
            case "length" -> "Keep the notes within 10,000 characters and shorten the contact or location fields before sending again.";
            case "consent" -> "Consent is required before BackflowVerdict can store and review your request.";
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
