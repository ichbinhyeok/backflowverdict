package owner.backflow.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
import owner.backflow.service.LeadAdminService;

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

    @BeforeEach
    void resetLeadsRoot() throws IOException {
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
                .andExpect(content().string(containsString("Configured username")));

        mockMvc.perform(post("/admin/login")
                        .param("username", "admin")
                        .param("password", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?error=1"));

        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/admin/login")
                        .session(session)
                        .param("username", "admin")
                        .param("password", "tlsgur3108"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Lead inbox")))
                .andExpect(content().string(containsString("No leads captured yet")))
                .andExpect(content().string(containsString("without provider coverage")))
                .andExpect(content().string(containsString("sponsor-only providers kept off public routes")))
                .andExpect(content().string(containsString("Sponsor prospect metros")))
                .andExpect(content().string(containsString("Dallas-Fort Worth backflow testing")))
                .andExpect(content().string(containsString("Private sponsor-only provider rows")))
                .andExpect(content().string(containsString("Next Day Backflow Testing")));
    }

    @Test
    void leadCaptureIsStoredVisibleAndExportable() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/admin/login")
                        .session(session)
                        .param("username", "admin")
                        .param("password", "tlsgur3108"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        mockMvc.perform(get("/leads/new")
                        .param("utilityId", "dallas-water")
                        .param("source", "/utilities/texas/dallas-water-utilities/")
                        .param("issueType", "general-testing")
                        .param("pageFamily", "utility"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Lead form")))
                .andExpect(content().string(containsString("Dallas Water Utilities")));

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
                        .header("Referer", "https://example.com/source-page"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads/thanks"));

        org.junit.jupiter.api.Assertions.assertEquals(1, leadAdminService.leadCount());
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(LEADS_ROOT.resolve("leads.jsonl")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(LEADS_ROOT.resolve("leads.csv")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("leads.jsonl")).contains("=Jordan Lee"));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("leads.csv")).contains("'=Jordan Lee"));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(LEADS_ROOT.resolve("leads.csv")).contains("'@Need a quote this week."));

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("=Jordan Lee")))
                .andExpect(content().string(containsString("General testing")))
                .andExpect(content().string(containsString("Commercial")))
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
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/admin/login")
                        .session(session)
                        .param("username", "admin")
                        .param("password", "tlsgur3108"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

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
                        .param("sourcePage", "/utilities/texas/garland-water-utilities/failed-test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads/thanks"));

        String leadId = leadAdminService.listLeads().getFirst().leadId();

        mockMvc.perform(post("/admin/leads/{leadId}/assign", leadId)
                        .session(session)
                        .param("providerId", "garland-polk-mechanical")
                        .param("note", "Route to sponsor first"))
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
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/admin/login")
                        .session(session)
                        .param("username", "admin")
                        .param("password", "tlsgur3108"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

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
                        .param("sourcePage", "/utilities/texas/garland-water-utilities/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/leads/thanks"));

        String leadId = leadAdminService.listLeads().getFirst().leadId();

        mockMvc.perform(post("/admin/leads/{leadId}/assign", leadId)
                        .session(session)
                        .param("providerId", "next-day-backflow-texas")
                        .param("note", "Should fail because this row is still a prospect"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sponsorProspectCanBeActivatedAndThenAssigned() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/admin/login")
                        .session(session)
                        .param("username", "admin")
                        .param("password", "tlsgur3108"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        mockMvc.perform(post("/admin/providers/{providerId}/sponsor-status", "next-day-backflow-texas")
                        .session(session)
                        .param("sponsorStatus", "ACTIVE")
                        .param("note", "Contract signed"))
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
                        .param("sourcePage", "/metros/texas/dallas-fort-worth-metroplex/backflow-testing"))
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
                        .param("note", "Route to active sponsor"))
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
}
