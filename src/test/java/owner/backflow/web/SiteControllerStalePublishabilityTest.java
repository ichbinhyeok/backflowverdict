package owner.backflow.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "app.ops.verification-token=test-ops-token",
        "app.ops.current-date=2026-09-01",
        "app.site.ga-measurement-id=G-TEST123",
        "app.site.support-email=support@backflowpath.com",
        "app.site.support-phone=+1-555-0100"
})
@AutoConfigureMockMvc
class SiteControllerStalePublishabilityTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void stalePagesStayCrawlableWhileReadyzReportsFreshnessDebt() throws Exception {
        mockMvc.perform(get("/states/colorado/backflow-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Colorado backflow testing requirements")));

        mockMvc.perform(get("/utilities/colorado/aurora-water/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Aurora Water Backflow Prevention")));

        mockMvc.perform(get("/readyz"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"ready\"")))
                .andExpect(content().string(containsString("\"publishedUtilityCount\":91")))
                .andExpect(content().string(containsString("\"staleUtilityCount\":91")));
    }
}
