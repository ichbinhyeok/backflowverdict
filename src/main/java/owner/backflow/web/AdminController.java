package owner.backflow.web;

import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import owner.backflow.service.AdminAuthService;
import owner.backflow.service.LeadAdminService;
import owner.backflow.service.LeadInboxItem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class AdminController {
    static final String ADMIN_SESSION_KEY = "adminAuthenticated";

    private final AdminAuthService adminAuthService;
    private final LeadAdminService leadAdminService;

    public AdminController(AdminAuthService adminAuthService, LeadAdminService leadAdminService) {
        this.adminAuthService = adminAuthService;
        this.leadAdminService = leadAdminService;
    }

    @GetMapping("/admin")
    public String admin(
            @RequestParam(value = "error", required = false) String error,
            HttpSession session,
            Model model
    ) {
        if (!adminAuthService.isConfigured()) {
            model.addAttribute("page", new PageMeta(
                    "Admin disabled | BackflowPath",
                    "Admin access is disabled until credentials are configured.",
                    "/admin",
                    true
            ));
            model.addAttribute("authenticated", false);
            model.addAttribute("adminConfigured", false);
            return "pages/admin";
        }

        if (!isAuthenticated(session)) {
            model.addAttribute("page", new PageMeta(
                    "Admin login | BackflowPath",
                    "Authenticate to view captured leads.",
                    "/admin",
                    true
            ));
            model.addAttribute("authenticated", false);
            model.addAttribute("adminConfigured", true);
            model.addAttribute("error", error != null);
            model.addAttribute("username", adminAuthService.username());
            return "pages/admin";
        }

        java.util.List<LeadInboxItem> inbox = leadAdminService.listInbox();
        model.addAttribute("page", new PageMeta(
                "Admin leads | BackflowPath",
                "View captured leads and lead sources.",
                "/admin",
                true
        ));
        model.addAttribute("authenticated", true);
        model.addAttribute("adminConfigured", true);
        model.addAttribute("inbox", inbox);
        model.addAttribute("leadCount", leadAdminService.leadCount());
        model.addAttribute("assignedLeadCount", leadAdminService.assignedLeadCount());
        model.addAttribute("unassignedLeadCount", leadAdminService.unassignedLeadCount());
        model.addAttribute("uncoveredLeadCount", leadAdminService.uncoveredLeadCount());
        model.addAttribute("issueCounts", leadAdminService.issueCounts());
        model.addAttribute("pageFamilyCounts", leadAdminService.pageFamilyCounts());
        model.addAttribute("utilityCounts", leadAdminService.utilityCounts());
        model.addAttribute("providerCoverageGaps", leadAdminService.providerCoverageGaps());
        model.addAttribute("publicProviderCount", leadAdminService.publicProviderCount());
        model.addAttribute("sponsorOnlyProviderCount", leadAdminService.sponsorOnlyProviderCount());
        model.addAttribute("sponsorActiveProviderCount", leadAdminService.sponsorActiveProviderCount());
        model.addAttribute("sponsorProspectProviderCount", leadAdminService.sponsorProspectProviderCount());
        model.addAttribute("sponsorProspectMetroCounts", leadAdminService.sponsorProspectMetroCounts());
        model.addAttribute("privateSponsorInventory", leadAdminService.privateSponsorInventory());
        model.addAttribute("deliveredLeadCount", leadAdminService.deliveredLeadCount());
        model.addAttribute("queuedDeliveryCount", leadAdminService.queuedDeliveryCount());
        model.addAttribute("recentDeliveries", leadAdminService.recentDeliveries());
        model.addAttribute("heldProviderCount", leadAdminService.heldProviderCount());
        model.addAttribute("username", adminAuthService.username());
        return "pages/admin";
    }

    @PostMapping("/admin/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session
    ) {
        ensureAdminConfigured();
        if (!adminAuthService.authenticate(username, password)) {
            return "redirect:/admin?error=1";
        }
        session.setAttribute(ADMIN_SESSION_KEY, Boolean.TRUE);
        return "redirect:/admin";
    }

    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin";
    }

    @PostMapping("/admin/leads/{leadId}/assign")
    public String assignLead(
            @PathVariable String leadId,
            @RequestParam String providerId,
            @RequestParam(value = "note", required = false) String note,
            HttpSession session
    ) {
        ensureAdminConfigured();
        if (!isAuthenticated(session)) {
            return "redirect:/admin";
        }
        try {
            leadAdminService.assignLead(leadId, providerId, note, adminAuthService.username());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/providers/{providerId}/sponsor-status")
    public String updateSponsorStatus(
            @PathVariable String providerId,
            @RequestParam String sponsorStatus,
            @RequestParam(value = "note", required = false) String note,
            HttpSession session
    ) {
        ensureAdminConfigured();
        if (!isAuthenticated(session)) {
            return "redirect:/admin";
        }
        try {
            leadAdminService.updateSponsorStatus(providerId, sponsorStatus, note, adminAuthService.username());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        return "redirect:/admin";
    }

    @GetMapping(value = "/admin/export.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> exportJson(HttpSession session) {
        ensureAdminConfigured();
        if (!isAuthenticated(session)) {
            return redirectToLogin();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"backflowpath-leads.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(leadAdminService.exportJson());
    }

    @GetMapping(value = "/admin/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv(HttpSession session) {
        ensureAdminConfigured();
        if (!isAuthenticated(session)) {
            return redirectToLogin();
        }
        MediaType csvMediaType = new MediaType("text", "csv", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"backflowpath-leads.csv\"")
                .contentType(csvMediaType)
                .body(leadAdminService.exportCsv());
    }

    private ResponseEntity<String> redirectToLogin() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/admin")
                .build();
    }

    private boolean isAuthenticated(HttpSession session) {
        Object authenticated = session.getAttribute(ADMIN_SESSION_KEY);
        return Boolean.TRUE.equals(authenticated);
    }

    private void ensureAdminConfigured() {
        if (!adminAuthService.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Admin credentials are not configured.");
        }
    }
}
