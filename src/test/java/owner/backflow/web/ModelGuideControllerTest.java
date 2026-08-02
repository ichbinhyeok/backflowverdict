package owner.backflow.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import owner.backflow.files.ModelGuideService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.data.root=./data",
        "app.site.base-url=https://backflowpath.com",
        "app.ops.write-freshness-report-on-startup=false"
})
class ModelGuideControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ModelGuideService service;

    @Test
    void modelPassportsRenderCompatibilityDataAndStructuredEvidence() throws Exception {
        for (var guide : service.listPublished()) {
            mockMvc.perform(get(guide.canonicalPath()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("data-model-kit-tool")))
                    .andExpect(content().string(containsString(guide.kits().get(0).partNumber())))
                    .andExpect(content().string(containsString("Official source ledger")))
                    .andExpect(content().string(containsString("\"@type\":\"TechArticle\"")))
                    .andExpect(content().string(containsString("href=\"https://backflowpath.com" + guide.canonicalPath() + "\"")));
        }
    }

    @Test
    void globalFinderRendersEveryModelAndCanonicalRedirects() throws Exception {
        mockMvc.perform(get("/backflow-preventer-repair-kits/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-global-kit-tool")))
                .andExpect(content().string(containsString("Zurn Wilkins 975XL / 975XL2")))
                .andExpect(content().string(containsString("FEBCO 765")))
                .andExpect(content().string(containsString("Watts 800M4")));
        mockMvc.perform(get("/backflow-preventer-repair-kits"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "/backflow-preventer-repair-kits/"));
        mockMvc.perform(get("/models/febco-765"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "/models/febco-765/"));
    }

    @Test
    void indexableModelAssetsAreInCoreSitemap() throws Exception {
        String xml = mockMvc.perform(get("/sitemaps/core.xml").header("Host", "backflowpath.com"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertTrue(xml.contains("https://backflowpath.com/backflow-preventer-repair-kits/"));
        for (var guide : service.listIndexable()) {
            org.junit.jupiter.api.Assertions.assertTrue(xml.contains("https://backflowpath.com" + guide.canonicalPath()), guide.slug());
        }
    }
}
