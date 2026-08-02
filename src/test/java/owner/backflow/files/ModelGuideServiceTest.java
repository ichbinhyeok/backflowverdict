package owner.backflow.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import owner.backflow.BackflowApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = BackflowApplication.class)
@TestPropertySource(properties = {
        "app.data.root=./data",
        "app.ops.write-freshness-report-on-startup=false"
})
class ModelGuideServiceTest {
    @Autowired
    private ModelGuideService service;

    @Test
    void loadsTheOfficialDataFirstModelCohort() {
        assertEquals(10, service.listPublished().size());
        assertTrue(service.findPublished("wilkins-975xl").isPresent());
        assertTrue(service.findPublished("febco-765").isPresent());
        assertTrue(service.findPublished("watts-800m4").isPresent());
        assertTrue(service.findPublished("wilkins-720a").isPresent());
        assertTrue(service.findPublished("wilkins-375-large-relief").isPresent());
        assertTrue(service.findPublished("watts-009-service").isPresent());
        assertTrue(service.findPublished("watts-909-small").isPresent());
        assertTrue(service.findPublished("febco-825y").isPresent());
        assertTrue(service.findPublished("febco-860-large").isPresent());
        assertTrue(service.findPublished("wilkins-950xl").isPresent());
    }

    @Test
    void everyKitMapsToAPublishedSizeAndAUniqueModelPartScope() {
        for (var guide : service.listPublished()) {
            Set<String> keys = new HashSet<>();
            assertTrue(guide.sources().size() >= 2, guide.slug());
            for (var kit : guide.kits()) {
                assertTrue(guide.sizes().contains(kit.sizeBand()), guide.slug() + ": " + kit.sizeBand());
                assertTrue(keys.add(kit.sizeBand() + "|" + kit.task() + "|" + kit.partNumber()), guide.slug());
                assertTrue(!kit.compatibility().isBlank(), kit.partNumber());
            }
        }
    }
}
