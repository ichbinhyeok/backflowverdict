package owner.backflow.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import owner.backflow.service.AdminAuthService;
import owner.backflow.service.AdminCsrfService;
import owner.backflow.service.LeadAdminService;
import owner.backflow.service.LeadPhotoService;
import owner.backflow.service.LeadRecord;
import owner.backflow.service.LeadReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class AdminReviewController {
    private final AdminAuthService auth;
    private final AdminCsrfService csrf;
    private final LeadAdminService leads;
    private final LeadPhotoService photos;
    private final LeadReviewService reviews;

    public AdminReviewController(AdminAuthService auth, AdminCsrfService csrf, LeadAdminService leads, LeadPhotoService photos, LeadReviewService reviews) {
        this.auth = auth; this.csrf = csrf; this.leads = leads; this.photos = photos; this.reviews = reviews;
    }

    @GetMapping("/admin/reviews/{leadId}")
    public String review(@PathVariable String leadId, HttpSession session, HttpServletResponse response, Model model) {
        if (!authorized(session)) return "redirect:/admin";
        var lead = lead(leadId);
        response.setHeader("Cache-Control", "no-store");
        model.addAttribute("page", new PageMeta("Request review | BackflowVerdict", "Private reference review workspace.", "/admin/reviews/" + leadId, true));
        model.addAttribute("lead", lead);
        model.addAttribute("photos", photos.list(leadId));
        model.addAttribute("review", reviews.find(leadId));
        model.addAttribute("csrfToken", csrf.ensureToken(session));
        return "pages/admin-review";
    }

    @PostMapping("/admin/reviews/{leadId}")
    public String save(@PathVariable String leadId, @RequestParam String confirmed, @RequestParam String candidates,
                       @RequestParam String unknowns, @RequestParam String nextSteps, @RequestParam(value = "_csrf", required = false) String token, HttpSession session) {
        if (!authorized(session)) return "redirect:/admin";
        csrf.requireValid(session, token); lead(leadId);
        try { reviews.save(leadId, confirmed, candidates, unknowns, nextSteps); }
        catch (IllegalArgumentException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage()); }
        return "redirect:/admin/reviews/" + leadId;
    }

    @GetMapping("/admin/reviews/{leadId}/reply.txt")
    public ResponseEntity<?> reply(@PathVariable String leadId, HttpSession session) {
        if (!authorized(session)) return login();
        lead(leadId);
        var review = reviews.find(leadId);
        if (review.savedAt().isBlank()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No completed review saved.");
        return ResponseEntity.ok().header("Cache-Control", "no-store").header("X-Content-Type-Options", "nosniff")
                .header("Content-Disposition", "attachment; filename=\"backflow-reference-review.txt\"")
                .contentType(new org.springframework.http.MediaType("text", "plain", StandardCharsets.UTF_8)).body(review.replyText());
    }

    @GetMapping("/admin/reviews/{leadId}/photos/{photoId}")
    public ResponseEntity<?> photo(@PathVariable String leadId, @PathVariable String photoId, HttpSession session) {
        if (!authorized(session)) return login();
        lead(leadId);
        try {
            return ResponseEntity.ok().header("Cache-Control", "no-store").header("X-Content-Type-Options", "nosniff")
                    .header("Content-Disposition", "inline; filename=\"request-photo.jpg\"")
                    .contentType(org.springframework.http.MediaType.IMAGE_JPEG).body(photos.read(leadId, photoId));
        } catch (IOException | IllegalArgumentException exception) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found."); }
    }

    @PostMapping("/admin/reviews/{leadId}/photos/delete")
    public String deletePhotos(@PathVariable String leadId, @RequestParam(value = "_csrf", required = false) String token, HttpSession session) {
        if (!authorized(session)) return "redirect:/admin";
        csrf.requireValid(session, token); lead(leadId); photos.deleteAll(leadId);
        return "redirect:/admin/reviews/" + leadId;
    }

    private LeadRecord lead(String id) {
        try { java.util.UUID.fromString(id); } catch (IllegalArgumentException exception) { throw new ResponseStatusException(HttpStatus.NOT_FOUND); }
        return leads.listLeads().stream().filter(lead -> lead.leadId().equals(id)).findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    private boolean authorized(HttpSession session) {
        if (!auth.isConfigured()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Admin credentials are not configured.");
        return Boolean.TRUE.equals(session.getAttribute(AdminController.ADMIN_SESSION_KEY));
    }
    private ResponseEntity<Void> login() { return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/admin").header("Cache-Control", "no-store").build(); }
}
