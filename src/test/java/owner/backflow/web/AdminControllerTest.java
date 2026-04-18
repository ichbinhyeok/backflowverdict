package owner.backflow.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import owner.backflow.files.BackflowRegistryService;
import owner.backflow.service.AdminCsrfService;
import owner.backflow.service.LeadAdminService;
import owner.backflow.service.LeadRoutingService;
import owner.backflow.service.LeadSubmissionGuardService;

@SpringBootTest(properties = {
        "app.leads.root=build/test-data/admin-ui/leads",
        "app.admin.username=admin",
        "app.admin.password=tlsgur3108",
        "app.ops.verification-token=test-ops-token",
        "app.ops.write-freshness-report-on-startup=false"
})
@AutoConfigureMockMvc
class AdminControllerTest {
    private static final Path LEADS_ROOT = Path.of("build", "test-data", "admin-ui", "leads");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadAdminService leadAdminService;

    @Autowired
    private BackflowRegistryService registryService;

    @Autowired
    private LeadSubmissionGuardService leadSubmissionGuardService;

    @Autowired
    private AdminCsrfService adminCsrfService;

    @BeforeEach
    void resetLeadsRoot() throws IOException {
        leadSubmissionGuardService.clear();
        if (!Files.exists(LEADS_ROOT)) {
            registryService.reload();
            return;
        }
        try (Stream<Path> paths = Files.walk(LEADS_ROOT)) {
            paths.sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(LEADS_ROOT))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException exception) {
                            throw new IllegalStateException("Failed to clean test leads root", exception);
                        }
                    });
        }
        registryService.reload();
    }

    @Test
    void adminLoginAndDashboardRender() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign in")))
                .andExpect(content().string(containsString("Configured username")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));

        MockHttpSession invalidSession = new MockHttpSession();
        mockMvc.perform(post("/admin/login")
                        .session(invalidSession)
                        .param("username", "admin")
                        .param("password", "wrong"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/login")
                        .session(invalidSession)
                        .param("username", "admin")
                        .param("password", "wrong")
                        .param("_csrf", csrf(invalidSession)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?error=1"));

        MockHttpSession session = adminSession();

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin control room")))
                .andExpect(content().string(containsString("Storage snapshot")))
                .andExpect(content().string(containsString("Vendor workflow")))
                .andExpect(content().string(containsString("File-backed store preview")))
                .andExpect(content().string(containsString("No leads captured yet")))
                .andExpect(content().string(containsString("without provider coverage")))
                .andExpect(content().string(containsString("sponsor-only providers kept off public routes")))
                .andExpect(content().string(containsString("Sponsor prospect metros")))
                .andExpect(content().string(containsString("Dallas-Fort Worth backflow testing")))
                .andExpect(content().string(containsString("Private sponsor-only provider rows")))
                .andExpect(content().string(containsString("Next Day Backflow Testing")));
    }

    @Test
    void adminShowsVendorWorkflowSignalsAndStoragePreview() throws Exception {
        MockHttpSession session = adminSession();

        mockMvc.perform(post("/handoffs")
                        .param("utilityId", "dallas-water")
                        .param("issueType", "general-testing")
                        .param("resultStatus", "pass")
                        .param("submissionStatus", "submitted")
                        .param("propertyLabel", "Main Street Retail Center")
                        .param("siteAddress", "120 Main Street, Dallas, TX")
                        .param("vendorCompanyName", "DFW Backflow Services")
                        .param("vendorContactName", "Jordan Lee")
                        .param("vendorPhone", "972-555-0144")
                        .param("vendorEmail", "dispatch@dfwbackflow.example")
                        .param("dueDate", "2026-05-01")
                        .param("testDate", "2026-04-18")
                        .param("sourcePath", "/vendors/customer-brief-demo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/handoffs/*"));

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Free handoff channel signals")))
                .andExpect(content().string(containsString("DFW Backflow Services")))
                .andExpect(content().string(containsString("Jordan Lee")))
                .andExpect(content().string(containsString("972-555-0144")))
                .andExpect(content().string(containsString("Main Street Retail Center")))
                .andExpect(content().string(containsString("handoff_created")))
                .andExpect(content().string(containsString("handoffs.jsonl")))
                .andExpect(content().string(containsString("handoff-events.jsonl")));
    }

    @Test
    void leadCaptureIsStoredVisibleAndExportable() throws Exception {
        MockHttpSession session = adminSession();

        mockMvc.perform(get("/leads/new")
                        .param("utilityId", "dallas-water")
                        .param("source", "/utilities/texas/dallas-water-utilities/")
                        .param("issueType", "general-testing")
                        .param("pageFamily", "utility"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tell us what is going on")))
                .andExpect(content().string(containsString("Dallas Water Utilities")))
                .andExpect(content().string(containsString("privacy and lead routing notice")));

        mockMvc.perform(post("/leads")
                        .param("fullName", "=Jordan Lee")
                        .param("phone", "555-111-2222")
                        .param("email", "jordan@example.com")
                        .param("city", "Dallas")
                        .param("utilityId", "dallas-water")
                        .param("utilityName", "Dallas Water Utilities")
                        .param("propertyType", "commercial")
                        .param("issueType", "general-testing")
                        .param("pageFamily", "utility")
                        .param("notes", "@Need a quote this week.")
                        .param("sourcePage", "/utilities/texas/dallas-water-utilities/")
                        .param("consentToRouting", "yes")
                        .header("Referer", "https://example.com/source-page"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads/thanks"));

        org.junit.jupiter.api.Assertions.assertEquals(1, leadAdminService.leadCount());
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(LEADS_ROOT.resolve("leads.jsonl")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(LEADS_ROOT.resolve("leads.csv")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("leads.jsonl")).contains("=Jordan Lee"));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("leads.csv")).contains("'=Jordan Lee"));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("leads.csv")).contains("'@Need a quote this week."));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("leads.jsonl")).contains("\"routingStatus\":\"HOLD_UNVERIFIED_CONTEXT\""));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("leads.jsonl")).contains("\"submittedUtilityId\":\"dallas-water\""));

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("=Jordan Lee")))
                .andExpect(content().string(containsString("General testing")))
                .andExpect(content().string(containsString("Commercial")))
                .andExpect(content().string(containsString("Submitted utility only: dallas-water")))
                .andExpect(content().string(containsString("Hold unverified context")))
                .andExpect(content().string(containsString("/utilities/texas/dallas-water-utilities/")))
                .andExpect(content().string(containsString("https://example.com/source-page")))
                .andExpect(content().string(containsString("No assignable providers")));

        mockMvc.perform(get("/admin/export.json").session(session))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("backflowpath-leads.json")))
                .andExpect(content().string(containsString("=Jordan Lee")))
                .andExpect(content().string(containsString("\"pageFamily\"")));

        mockMvc.perform(get("/admin/export.csv").session(session))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("backflowpath-leads.csv")))
                .andExpect(content().string(containsString("'=Jordan Lee")))
                .andExpect(content().string(containsString("'@Need a quote this week.")))
                .andExpect(content().string(containsString("general-testing")));
    }

    @Test
    void leadCanBeAssignedToSingleProviderFromAdminInbox() throws Exception {
        MockHttpSession session = adminSession();

        mockMvc.perform(post("/leads")
                        .param("fullName", "Taylor Kim")
                        .param("phone", "555-222-3333")
                        .param("city", "Garland")
                        .param("utilityId", "garland-water")
                        .param("utilityName", "City of Garland Water Supply Protection")
                        .param("propertyType", "commercial")
                        .param("issueType", "failed-test")
                        .param("pageFamily", "failed-test")
                        .param("notes", "Need a same-week retest.")
                        .param("sourcePage", "/utilities/texas/garland-water-utilities/failed-test")
                        .param("rt", LeadRoutingService.issueToken(
                                "garland-water",
                                "/utilities/texas/garland-water-utilities/failed-test",
                                "failed-test"
                        ))
                        .param("consentToRouting", "yes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads/thanks"));

        String leadId = leadAdminService.listLeads().getFirst().leadId();

        mockMvc.perform(post("/admin/leads/{leadId}/assign", leadId)
                        .session(session)
                        .param("providerId", "garland-polk-mechanical")
                        .param("note", "Route to sponsor first")
                        .param("_csrf", csrf(session)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(LEADS_ROOT.resolve("lead-assignments.json")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(LEADS_ROOT.resolve("lead-assignments.csv")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("lead-assignments.json")).contains("garland-polk-mechanical"));

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Polk Mechanical")))
                .andExpect(content().string(containsString("Route to sponsor first")))
                .andExpect(content().string(containsString("4 assignable")))
                .andExpect(content().string(containsString("4 public")))
                .andExpect(content().string(containsString("0 sponsor-only")));
    }

    @Test
    void sponsorProspectCannotBeAssignedUntilActivated() throws Exception {
        MockHttpSession session = adminSession();

        mockMvc.perform(post("/leads")
                        .param("fullName", "Jordan Park")
                        .param("phone", "555-444-3333")
                        .param("city", "Garland")
                        .param("utilityId", "garland-water")
                        .param("utilityName", "City of Garland Water Supply Protection")
                        .param("propertyType", "commercial")
                        .param("issueType", "annual-testing")
                        .param("pageFamily", "utility")
                        .param("notes", "Route only to active sponsors.")
                        .param("sourcePage", "/utilities/texas/garland-water-utilities/")
                        .param("rt", LeadRoutingService.issueToken(
                                "garland-water",
                                "/utilities/texas/garland-water-utilities/",
                                "utility"
                        ))
                        .param("consentToRouting", "yes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads/thanks"));

        String leadId = leadAdminService.listLeads().getFirst().leadId();

        mockMvc.perform(post("/admin/leads/{leadId}/assign", leadId)
                        .session(session)
                        .param("providerId", "next-day-backflow-texas")
                        .param("note", "Should fail because this row is still a prospect")
                        .param("_csrf", csrf(session)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sponsorProspectCanBeActivatedAndThenAssigned() throws Exception {
        MockHttpSession session = adminSession();

        mockMvc.perform(post("/admin/providers/{providerId}/sponsor-status", "next-day-backflow-texas")
                        .session(session)
                        .param("sponsorStatus", "ACTIVE")
                        .param("note", "Contract signed")
                        .param("_csrf", csrf(session)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(LEADS_ROOT.resolve("provider-commercial-state.json")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(LEADS_ROOT.resolve("provider-commercial-state.csv")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("provider-commercial-state.json")).contains("next-day-backflow-texas"));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("provider-commercial-state.json")).contains("ACTIVE"));

        mockMvc.perform(post("/leads")
                        .param("fullName", "Casey Wu")
                        .param("phone", "555-777-9999")
                        .param("city", "Dallas")
                        .param("utilityId", "dallas-water")
                        .param("utilityName", "Dallas Water Utilities")
                        .param("propertyType", "commercial")
                        .param("issueType", "general-testing")
                        .param("pageFamily", "metro")
                        .param("notes", "Activated sponsor should be assignable.")
                        .param("sourcePage", "/metros/texas/dallas-fort-worth-metroplex/backflow-testing")
                        .param("rt", LeadRoutingService.issueToken(
                                "dallas-water",
                                "/metros/texas/dallas-fort-worth-metroplex/backflow-testing",
                                "metro"
                        ))
                        .param("consentToRouting", "yes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads/thanks"));

        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(LEADS_ROOT.resolve("lead-deliveries.jsonl")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(LEADS_ROOT.resolve("lead-deliveries.csv")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("lead-deliveries.jsonl")).contains("\"status\":\"QUEUED\""));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("lead-deliveries.jsonl")).contains("next-day-backflow-texas"));

        String leadId = leadAdminService.listLeads().getFirst().leadId();

        mockMvc.perform(post("/admin/leads/{leadId}/assign", leadId)
                        .session(session)
                        .param("providerId", "next-day-backflow-texas")
                        .param("note", "Route to active sponsor")
                        .param("_csrf", csrf(session)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1 sponsor-only providers active for internal lead routing")))
                .andExpect(content().string(containsString("1 delivery email(s) queued for manual send or SMTP setup")))
                .andExpect(content().string(containsString("Latest sponsor email delivery records")))
                .andExpect(content().string(containsString("Route to active sponsor")))
                .andExpect(content().string(containsString("Next Day Backflow Testing")));
    }

    @Test
    void providerClaimAppearsInAdminInbox() throws Exception {
        MockHttpSession session = adminSession();

        mockMvc.perform(post("/claim-listing")
                        .param("fullName", "Mina Kim")
                        .param("companyName", "Backflow Field Services")
                        .param("email", "mina@example.com")
                        .param("phone", "555-818-2121")
                        .param("website", "https://example.com")
                        .param("serviceArea", "Dallas-Fort Worth")
                        .param("requestType", "claim-existing-profile")
                        .param("listingReference", "https://backflowpath.com/providers/example/")
                        .param("notes", "Please review our profile and next steps.")
                        .param("consentToReview", "yes")
                        .header("Referer", "https://backflowpath.com/for-providers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/claim-listing/thanks"));

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1 provider requests")))
                .andExpect(content().string(containsString("Manual provider inbox")))
                .andExpect(content().string(containsString("Backflow Field Services")))
                .andExpect(content().string(containsString("Claim existing profile")))
                .andExpect(content().string(containsString("https://backflowpath.com/providers/example/")))
                .andExpect(content().string(containsString("Please review our profile and next steps.")));
    }

    @Test
    void leadCaptureDoesNotExposeSponsorEmailsAndShowsConsentCopy() throws Exception {
        MockHttpSession session = adminSession();

        mockMvc.perform(post("/admin/providers/{providerId}/sponsor-status", "next-day-backflow-texas")
                        .session(session)
                        .param("sponsorStatus", "ACTIVE")
                        .param("note", "Contract signed")
                        .param("_csrf", csrf(session)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        mockMvc.perform(get("/leads/new")
                        .param("utilityId", "dallas-water")
                        .param("source", "/utilities/texas/dallas-water-utilities/")
                        .param("issueType", "general-testing")
                        .param("pageFamily", "utility"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Official guidance stays separate from provider help")))
                .andExpect(content().string(containsString("Reviewed against the utility workflow")))
                .andExpect(content().string(containsString("What you are consenting to")))
                .andExpect(content().string(containsString("share the request with one or more active sponsors")))
                .andExpect(content().string(not(containsString("Active sponsor emails"))))
                .andExpect(content().string(not(containsString("Israel@nextdaybackflowtesting.com"))));
    }

    @Test
    void leadCaptureRequiresConsentSilentlyDropsHoneypotAndRateLimitsBurstTraffic() throws Exception {
        mockMvc.perform(post("/leads")
                        .param("fullName", "No Consent")
                        .param("phone", "555-000-1111")
                        .param("utilityId", "dallas-water")
                        .param("issueType", "general-testing")
                        .param("pageFamily", "utility")
                        .param("sourcePage", "/utilities/texas/dallas-water-utilities/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads/new?utilityId=dallas-water&source=/utilities/texas/dallas-water-utilities/&issueType=general-testing&pageFamily=utility&error=consent"));

        org.junit.jupiter.api.Assertions.assertEquals(0, leadAdminService.leadCount());

        mockMvc.perform(post("/leads")
                        .param("fullName", "Bot Submission")
                        .param("phone", "555-000-2222")
                        .param("utilityId", "dallas-water")
                        .param("issueType", "general-testing")
                        .param("pageFamily", "utility")
                        .param("sourcePage", "/utilities/texas/dallas-water-utilities/")
                        .param("consentToRouting", "yes")
                        .param("companyWebsite", "https://spam.example"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads/thanks"));

        org.junit.jupiter.api.Assertions.assertEquals(0, leadAdminService.leadCount());

        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/leads")
                            .with(request -> {
                                request.setRemoteAddr("203.0.113.20");
                                return request;
                            })
                            .param("fullName", "Rate Limited User " + attempt)
                            .param("phone", "555-000-333" + attempt)
                            .param("utilityId", "dallas-water")
                            .param("issueType", "general-testing")
                            .param("pageFamily", "utility")
                            .param("sourcePage", "/utilities/texas/dallas-water-utilities/")
                            .param("consentToRouting", "yes"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/leads/thanks"));
        }

        mockMvc.perform(post("/leads")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.20");
                            return request;
                        })
                        .param("fullName", "Rate Limited User 4")
                        .param("phone", "555-000-4444")
                        .param("utilityId", "dallas-water")
                        .param("issueType", "general-testing")
                        .param("pageFamily", "utility")
                        .param("sourcePage", "/utilities/texas/dallas-water-utilities/")
                        .param("consentToRouting", "yes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads/new?utilityId=dallas-water&source=/utilities/texas/dallas-water-utilities/&issueType=general-testing&pageFamily=utility&error=rate-limit"));

        org.junit.jupiter.api.Assertions.assertEquals(3, leadAdminService.leadCount());
    }

    @Test
    void autoRoutingRequiresVerifiedRoutingToken() throws Exception {
        MockHttpSession session = adminSession();

        mockMvc.perform(post("/admin/providers/{providerId}/sponsor-status", "next-day-backflow-texas")
                        .session(session)
                        .param("sponsorStatus", "ACTIVE")
                        .param("note", "Contract signed")
                        .param("_csrf", csrf(session)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        mockMvc.perform(post("/leads")
                        .param("fullName", "Unsigned Lead")
                        .param("phone", "555-101-2020")
                        .param("city", "Dallas")
                        .param("utilityId", "dallas-water")
                        .param("utilityName", "Dallas Water Utilities")
                        .param("pageFamily", "utility")
                        .param("sourcePage", "/utilities/texas/dallas-water-utilities/")
                        .param("consentToRouting", "yes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads/thanks"));

        org.junit.jupiter.api.Assertions.assertFalse(Files.exists(LEADS_ROOT.resolve("lead-deliveries.jsonl")));

        mockMvc.perform(post("/leads")
                        .param("fullName", "Signed Lead")
                        .param("phone", "555-303-4040")
                        .param("city", "Dallas")
                        .param("utilityId", "dallas-water")
                        .param("utilityName", "Dallas Water Utilities")
                        .param("pageFamily", "utility")
                        .param("sourcePage", "/utilities/texas/dallas-water-utilities/")
                        .param("rt", LeadRoutingService.issueToken(
                                "dallas-water",
                                "/utilities/texas/dallas-water-utilities/",
                                "utility"
                        ))
                        .param("consentToRouting", "yes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads/thanks"));

        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(LEADS_ROOT.resolve("lead-deliveries.jsonl")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("lead-deliveries.jsonl")).contains("next-day-backflow-texas"));
    }

    @Test
    void adminMutationsRejectMissingCsrfToken() throws Exception {
        MockHttpSession session = adminSession();

        mockMvc.perform(post("/admin/logout").session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/providers/{providerId}/sponsor-status", "next-day-backflow-texas")
                        .session(session)
                        .param("sponsorStatus", "ACTIVE")
                        .param("note", "Missing token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void ctaRedirectOnlyAllowsKnownHosts() throws Exception {
        mockMvc.perform(get("/r/cta")
                        .param("next", "https://www.anaheim.net/769/Cross-Connection-Control")
                        .param("pageFamily", "provider-profile")
                        .param("providerId", "anaheim-accurate-backflow")
                        .param("ctaType", "authority-source")
                        .param("source", "/providers/anaheim-accurate-backflow/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://www.anaheim.net/769/Cross-Connection-Control"));

        mockMvc.perform(get("/r/cta")
                        .param("next", "https://evil.example.com/phish")
                        .param("pageFamily", "provider-profile")
                        .param("providerId", "anaheim-accurate-backflow")
                        .param("ctaType", "authority-source")
                        .param("source", "/providers/anaheim-accurate-backflow/"))
                .andExpect(status().isNotFound());

        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(LEADS_ROOT.resolve("cta-clicks.jsonl")));
        org.junit.jupiter.api.Assertions.assertFalse(Files.readString(LEADS_ROOT.resolve("cta-clicks.jsonl")).contains("evil.example.com"));
    }

    private MockHttpSession adminSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/admin/login")
                        .session(session)
                        .param("username", "admin")
                        .param("password", "tlsgur3108")
                        .param("_csrf", csrf(session)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
        return session;
    }

    private String csrf(MockHttpSession session) {
        return adminCsrfService.ensureToken(session);
    }
}
