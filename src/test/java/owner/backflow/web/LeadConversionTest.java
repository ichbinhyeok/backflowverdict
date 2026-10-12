package owner.backflow.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import owner.backflow.service.LeadAdminService;
import owner.backflow.service.LeadSubmissionGuardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "app.leads.root=build/test-data/lead-conversion",
        "app.leads.submission-rate-limit-max-attempts=1",
        "app.ops.write-freshness-report-on-startup=false"
})
@AutoConfigureMockMvc
class LeadConversionTest {
    @Autowired MockMvc mvc;
    @Autowired LeadAdminService leads;
    @Autowired LeadSubmissionGuardService guard;

    @BeforeEach void resetGuard() { guard.clear(); }

    @Test void successFlagExistsOnlyAfterPersistenceAndDoesNotReplay() throws Exception {
        int before = leads.leadCount();
        var result = mvc.perform(post("/leads").param("fullName", "Conversion test")
                        .param("phone", "555-0100").param("consentToRouting", "yes"))
                .andExpect(redirectedUrl("/leads/thanks"))
                .andExpect(flash().attribute("leadSaved", true)).andReturn();
        assertEquals(before + 1, leads.leadCount());
        mvc.perform(get("/leads/thanks").session((org.springframework.mock.web.MockHttpSession) result.getRequest().getSession()))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-lead-saved=\"true\"")));
        mvc.perform(get("/leads/thanks").session((org.springframework.mock.web.MockHttpSession) result.getRequest().getSession()))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("data-lead-saved=\"true\""))));
    }

    @Test void rejectedRequestsNeverReceiveTheSuccessFlag() throws Exception {
        int before = leads.leadCount();
        var spam = mvc.perform(post("/leads").param("fullName", "Spam test").param("phone", "555-0100")
                .param("consentToRouting", "yes").param("companyWebsite", "https://example.com"))
                .andExpect(redirectedUrl("/leads/thanks")).andReturn();
        assertFalse(spam.getFlashMap().containsKey("leadSaved"));
        var noConsent = mvc.perform(post("/leads").param("fullName", "Consent test").param("phone", "555-0100"))
                .andExpect(status().is3xxRedirection()).andReturn();
        assertFalse(noConsent.getFlashMap().containsKey("leadSaved"));
        guard.tryAcquire("127.0.0.1");
        var limited = mvc.perform(post("/leads").param("fullName", "Limit test").param("phone", "555-0100")
                .param("consentToRouting", "yes")).andExpect(status().is3xxRedirection()).andReturn();
        assertFalse(limited.getFlashMap().containsKey("leadSaved"));
        assertEquals(before, leads.leadCount());
    }
}
