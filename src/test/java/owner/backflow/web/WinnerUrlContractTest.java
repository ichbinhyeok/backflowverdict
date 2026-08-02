package owner.backflow.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.data.root=./data",
        "app.site.base-url=https://backflowpath.com",
        "app.ops.write-freshness-report-on-startup=false"
})
class WinnerUrlContractTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void currentGscWinnerUrlsRemainIndexableAndSelfCanonical() throws Exception {
        List<String> paths = new String(
                new ClassPathResource("seo/winner-url-contract.csv").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        ).lines().skip(1).filter(line -> !line.isBlank())
                .map(line -> line.replaceAll("^\"|\"$", ""))
                .toList();
        String sitemap = mockMvc.perform(get("/sitemap.xml").header("Host", "backflowpath.com"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        for (String path : paths) {
            mockMvc.perform(get(path).header("Host", "backflowpath.com"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("<link rel=\"canonical\" href=\"https://backflowpath.com" + path + "\"")))
                    .andExpect(content().string(containsString("<meta name=\"robots\" content=\"index,follow")));
            org.junit.jupiter.api.Assertions.assertTrue(
                    sitemap.contains("https://backflowpath.com" + path),
                    path + " must remain in the production sitemap"
            );
        }
        org.junit.jupiter.api.Assertions.assertEquals(107, paths.size(), "GSC winner baseline changed without review");
    }
}
