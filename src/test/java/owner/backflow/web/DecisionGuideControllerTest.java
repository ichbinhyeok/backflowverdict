package owner.backflow.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import owner.backflow.files.DecisionGuideService;
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
class DecisionGuideControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DecisionGuideService guideService;

    @Test
    void publishedDecisionGuidesRenderWithTheSharedDecisionContract() throws Exception {
        for (var guide : guideService.listAvailable()) {
            String path = guide.canonicalPath();
            mockMvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Working verdict")))
                    .andExpect(content().string(containsString("What you can check without taking it apart")))
                    .andExpect(content().string(containsString("Your utility sets the rule")))
                    .andExpect(content().string(containsString("Evidence ledger")))
                    .andExpect(content().string(containsString("\"@type\":\"Article\"")))
                    .andExpect(content().string(containsString("\"@type\":\"BreadcrumbList\"")))
                    .andExpect(content().string(containsString("href=\"https://backflowpath.com" + path + "\"")));
        }
    }

    @Test
    void interactiveGuideTypesRenderTheirWorkingControls() throws Exception {
        mockMvc.perform(get("/water-pressure-regulator-adjustment/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-pressure-tool")));
        mockMvc.perform(get("/water-pressure-regulator-cost/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-cost-tool")));
        mockMvc.perform(get("/backflow-repair-or-replace/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-score-tool")));
        mockMvc.perform(get("/backflow-preventer-leaking/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-choice-tool")));
    }

    @Test
    void slashlessGuideRedirectsToTheCanonicalPath() throws Exception {
        mockMvc.perform(get("/backflow-preventer"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "/backflow-preventer/"));
    }

    @Test
    void libraryGroupsPublishedDecisionPathsAndHasCanonicalRedirect() throws Exception {
        mockMvc.perform(get("/backflow-library/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Devices and systems")))
                .andExpect(content().string(containsString("Leaks, failures, and pressure symptoms")))
                .andExpect(content().string(containsString("Testing and compliance workflows")))
                .andExpect(content().string(containsString("href=\"/rpz-backflow-preventer/\"")));

        mockMvc.perform(get("/backflow-library"))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", "/backflow-library/"));
    }

    @Test
    void publishedDecisionGuidesAreIncludedInTheCoreSitemap() throws Exception {
        var result = mockMvc.perform(get("/sitemaps/core.xml").header("Host", "backflowpath.com"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertTrue(result.contains("https://backflowpath.com/backflow-library/"));
        for (var guide : guideService.listAvailable()) {
            boolean included = result.contains("https://backflowpath.com" + guide.canonicalPath());
            org.junit.jupiter.api.Assertions.assertEquals(guide.indexable(), included, guide.slug());
        }
    }

    @Test
    void heldGuidesStayAccessibleButNoindex() throws Exception {
        for (var guide : guideService.listAvailable().stream().filter(item -> !item.indexable()).toList()) {
            mockMvc.perform(get(guide.canonicalPath()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("<meta name=\"robots\" content=\"noindex,follow\"")));
        }
    }

    @Test
    void selectedUtilityAddsItsOfficialOverlay() throws Exception {
        mockMvc.perform(get("/backflow-test/").param("utility", "lee-county-utilities"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-utility-picker")))
                .andExpect(content().string(containsString("data-utility-filter")))
                .andExpect(content().string(containsString("data-utility-count")))
                .andExpect(content().string(containsString("Selected official record")))
                .andExpect(content().string(containsString("Lee County")))
                .andExpect(content().string(containsString("Last verified")));
    }

    @Test
    void getHelpIsNoindexAndUsesTheNewCanonical() throws Exception {
        mockMvc.perform(get("/get-help/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("noindex,follow")))
                .andExpect(content().string(containsString("href=\"/get-help/\"")));
    }
}
