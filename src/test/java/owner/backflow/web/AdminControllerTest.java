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
                .andExpect(content().string(containsString("Lead control room")))
                .andExpect(content().string(containsString("Storage snapshot")))
                .andExpect(content().string(containsString("Coverage watch")))
                .andExpect(content().string(containsString("No leads captured yet")))
                .andExpect(content().string(containsString("without provider coverage")))
                .andExpect(content().string(containsString("public provider profiles")))
                .andExpect(content().string(containsString("held provider rows")))
                .andExpect(content().string(containsString("Most recent requests first")));
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
                .andExpect(content().string(containsString("privacy and request handling notice")));

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
                        .param("note", "Start with the public provider list")
                        .param("_csrf", csrf(session)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(LEADS_ROOT.resolve("lead-assignments.json")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(LEADS_ROOT.resolve("lead-assignments.csv")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("lead-assignments.json")).contains("garland-polk-mechanical"));

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Polk Mechanical")))
                .andExpect(content().string(containsString("Start with the public provider list")))
                .andExpect(content().string(containsString("4 assignable")))
                .andExpect(content().string(containsString("4 public profile(s)")));
    }

    @Test
    void leadCaptureDoesNotExposeHeldDirectoryEmailsAndShowsConsentCopy() throws Exception {
        mockMvc.perform(get("/leads/new")
                        .param("utilityId", "dallas-water")
                        .param("source", "/utilities/texas/dallas-water-utilities/")
                        .param("issueType", "general-testing")
                        .param("pageFamily", "utility"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Official guidance stays separate from provider help")))
                .andExpect(content().string(containsString("Reviewed against the utility workflow")))
                .andExpect(content().string(containsString("What you are consenting to")))
                .andExpect(content().string(containsString("follow up with public provider options when appropriate")))
                .andExpect(content().string(containsString("BackflowPath does not treat this form as a private resale channel.")))
                .andExpect(content().string(not(containsString("Held directory emails"))))
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

        String unsignedLeadJson = Files.readString(LEADS_ROOT.resolve("leads.jsonl"));
        org.junit.jupiter.api.Assertions.assertTrue(unsignedLeadJson.contains("\"routingStatus\":\"HOLD_UNVERIFIED_CONTEXT\""));

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

        String signedLeadJson = Files.readString(LEADS_ROOT.resolve("leads.jsonl"));
        org.junit.jupiter.api.Assertions.assertTrue(signedLeadJson.contains("\"routingStatus\":\"VERIFIED_UTILITY_CONTEXT\""));
        org.junit.jupiter.api.Assertions.assertTrue(signedLeadJson.contains("\"utilityId\":\"dallas-water\""));
        org.junit.jupiter.api.Assertions.assertFalse(Files.exists(LEADS_ROOT.resolve("lead-deliveries.jsonl")));
    }

    @Test
    void sourceOnlyRoutingTokenPreservesVerifiedPageContextWithoutAutoRoute() throws Exception {
        MockHttpSession session = adminSession();

        mockMvc.perform(post("/leads")
                        .param("fullName", "Source Only Lead")
                        .param("phone", "555-121-3434")
                        .param("city", "Dallas")
                        .param("pageFamily", "state-index")
                        .param("sourcePage", "/states")
                        .param("rt", LeadRoutingService.issueToken("", "/states", "state-index"))
                        .param("consentToRouting", "yes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads/thanks"));

        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("leads.jsonl")).contains("\"routingStatus\":\"VERIFIED_PAGE_CONTEXT\""));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("leads.jsonl")).contains("\"sourcePage\":\"/states\""));
        org.junit.jupiter.api.Assertions.assertFalse(Files.exists(LEADS_ROOT.resolve("lead-deliveries.jsonl")));

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Verified page context")))
                .andExpect(content().string(containsString("/states")));
    }

    @Test
    void adminMutationsRejectMissingCsrfToken() throws Exception {
        MockHttpSession session = adminSession();

        mockMvc.perform(post("/admin/logout").session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/leads/{leadId}/assign", "missing-lead")
                        .session(session)
                        .param("providerId", "garland-polk-mechanical")
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
