package owner.backflow.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
class DecisionGuideQualityContractTest {
    private static final Pattern INTERNAL_HREF = Pattern.compile("href=\"(/[^\"?#]*)(?:[?#][^\"]*)?\"");
    private static final Pattern H1 = Pattern.compile("<h1(?:\\s[^>]*)?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMG = Pattern.compile("<img\\s[^>]*>", Pattern.CASE_INSENSITIVE);

    @Autowired MockMvc mockMvc;
    @Autowired DecisionGuideService guideService;

    @Test
    void everyDecisionGuideHasOneH1AltTextAndNoBrokenTemplateMarkers() throws Exception {
        for (var guide : guideService.listPublished()) {
            String html = mockMvc.perform(get(guide.canonicalPath())).andReturn().getResponse().getContentAsString();
            assertEquals(1, count(H1, html), guide.slug() + " must have exactly one H1");
            assertFalse(html.contains("??/"), guide.slug() + " contains a broken template marker");
            Matcher images = IMG.matcher(html);
            while (images.find()) {
                assertTrue(images.group().matches("(?s).*\\salt=\"[^\"]*\".*"), guide.slug() + " image is missing alt");
            }
        }
    }

    @Test
    void decisionGuideInternalLinksResolveWithoutServerErrors() throws Exception {
        Set<String> checked = new HashSet<>();
        for (var guide : guideService.listPublished()) {
            String html = mockMvc.perform(get(guide.canonicalPath())).andReturn().getResponse().getContentAsString();
            Matcher matcher = INTERNAL_HREF.matcher(html);
            while (matcher.find()) {
                String path = matcher.group(1);
                if (!checked.add(path) || path.startsWith("//")) continue;
                int status = mockMvc.perform(get(path)).andReturn().getResponse().getStatus();
                assertTrue(status < 500, path + " returned " + status);
                assertTrue(status != 404, path + " is a broken internal link");
            }
        }
    }

    private int count(Pattern pattern, String value) {
        int count = 0;
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) count++;
        return count;
    }
}
