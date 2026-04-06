package owner.backflow.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "app.admin.username=",
        "app.admin.password=",
        "app.ops.verification-token=test-ops-token",
        "app.ops.write-freshness-report-on-startup=false"
})
@AutoConfigureMockMvc
class AdminControllerDisabledTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminStaysDisabledUntilCredentialsAreConfigured() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin credentials are not configured")));

        mockMvc.perform(post("/admin/login")
                        .param("username", "admin")
                        .param("password", "tlsgur3108"))
                .andExpect(status().isServiceUnavailable());

        mockMvc.perform(get("/admin/export.csv"))
                .andExpect(status().isServiceUnavailable());
    }
}
