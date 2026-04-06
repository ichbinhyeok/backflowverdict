package owner.backflow.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "app.ops.allow-local-requests=true",
        "app.ops.verification-token=test-ops-token",
        "app.ops.write-freshness-report-on-startup=false"
})
@AutoConfigureMockMvc
class OpsEndpointAccessInterceptorTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void forwardedHeadersDisableLoopbackShortcut() throws Exception {
        mockMvc.perform(post("/ops/verification/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewerInitials\":\"TL\",\"note\":\"forwarded\"}")
                        .header("X-Forwarded-For", "198.51.100.10")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isForbidden());
    }

    @Test
    void pureLoopbackRequestCanStillUseExplicitLocalOverride() throws Exception {
        mockMvc.perform(post("/ops/verification/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewerInitials\":\"TL\",\"note\":\"loopback\"}")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"ok\"")));
    }
}
