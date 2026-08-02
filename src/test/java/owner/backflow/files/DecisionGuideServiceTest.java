package owner.backflow.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

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
class DecisionGuideServiceTest {
    @Autowired
    private DecisionGuideService service;

    @Test
    void loadsTheValidatedWaveAAndWaveBDecisionGuidesFromData() {
        assertEquals(90, service.listAvailable().size());
        assertEquals(90, service.listPublished().size());
        assertTrue(service.findPublished("backflow-preventer").isPresent());
        assertTrue(service.findPublished("vacuum-breaker-leaking").isPresent());
        assertTrue(service.findPublished("failed-backflow-test").isPresent());
        assertTrue(service.findPublished("water-pressure-regulator").isPresent());
        assertTrue(service.findPublished("pressure-vacuum-breaker").isPresent());
        assertTrue(service.findPublished("backflow-preventer-installation").orElseThrow().indexable());
        assertTrue(service.findPublished("backflow-preventer-repair").orElseThrow().indexable());
        assertTrue(service.findPublished("backflow-preventer-repair-cost").orElseThrow().indexable());
        assertTrue(service.findPublished("backflow-preventer-replacement-cost").orElseThrow().indexable());
        assertTrue(service.findPublished("commercial-backflow-inspection").orElseThrow().indexable());
        assertTrue(service.findPublished("backflow-preventer-rebuild").orElseThrow().indexable());
        assertTrue(service.findPublished("commercial-backflow-installation-cost").orElseThrow().indexable());
        assertTrue(service.findPublished("fire-sprinkler-backflow-installation").orElseThrow().indexable());
        assertTrue(service.findPublished("rpz-vs-pvb").orElseThrow().indexable());
        assertTrue(service.findPublished("rpz-vs-dcva").orElseThrow().indexable());
        assertTrue(service.findPublished("backflow-preventer-types").orElseThrow().indexable());
        assertTrue(service.findPublished("backflow-test-notice").orElseThrow().indexable());
    }

    @Test
    void everyPublishedGuideHasAnExistingTechnicalDiagramAndSubstantiveDecisionData() {
        for (var guide : service.listAvailable()) {
            assertTrue(Files.isRegularFile(Path.of("src/main/resources/static/images/devices", guide.diagram() + ".svg")), guide.slug());
            assertTrue(guide.choices().size() >= 2, guide.slug());
            assertTrue(guide.steps().size() >= 3, guide.slug());
            assertTrue(guide.stopConditions().size() >= 2, guide.slug());
        }
    }

    @Test
    void indexableGuidesHaveExclusiveAliasOwnership() {
        java.util.Map<String, String> ownerByQuery = new java.util.HashMap<>();
        for (var guide : service.listPublished()) {
            for (String alias : guide.aliases()) {
                String normalized = alias.toLowerCase(java.util.Locale.US).replaceAll("[^a-z0-9]+", " ").trim();
                String prior = ownerByQuery.putIfAbsent(normalized, guide.slug());
                org.junit.jupiter.api.Assertions.assertNull(prior,
                        "Query alias '" + normalized + "' is owned by both " + prior + " and " + guide.slug());
            }
        }
    }

    @Test
    void queryOwnershipLedgerMatchesEveryWaveAQueryAlias() throws Exception {
        java.util.Set<String> expected = service.listPublished().stream()
                .flatMap(guide -> guide.aliases().stream().map(alias -> alias + "|" + guide.slug()))
                .collect(java.util.stream.Collectors.toSet());
        java.util.Set<String> actual = Files.readAllLines(Path.of("data/decision-query-ownership.csv")).stream()
                .skip(1)
                .filter(line -> !line.isBlank())
                .map(line -> {
                    String[] columns = line.split("\",\"", 4);
                    return columns[0].replaceFirst("^\"", "") + "|" + columns[1];
                })
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(expected, actual, "Wave A query ownership ledger drifted from guide data");
    }
}
