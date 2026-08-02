package owner.backflow.files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "app.data.root=./data",
        "app.ops.write-freshness-report-on-startup=false"
})
class DecisionEvidenceServiceTest {
    @Autowired DecisionEvidenceService evidenceService;
    @Autowired DecisionGuideService guideService;

    @Test
    void everyAvailableGuideHasFreshClaimIdentifiedEvidence() {
        for (var guide : guideService.listAvailable()) {
            var sources = evidenceService.forGuide(guide);
            assertFalse(sources.isEmpty(), guide.slug());
            sources.forEach(source -> {
                assertFalse(source.claimId().isBlank());
                assertTrue(source.url().startsWith("https://"));
                long age = ChronoUnit.DAYS.between(LocalDate.parse(source.lastVerified()), LocalDate.now());
                assertTrue(age >= 0 && age <= 365, source.claimId());
            });
        }
    }
}
