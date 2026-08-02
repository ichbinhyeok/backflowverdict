package owner.backflow.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "app.data.root=./data",
        "app.ops.write-freshness-report-on-startup=false"
})
class RegistryIntegrityContractTest {
    @Autowired BackflowRegistryService registry;

    @Test
    void utilityIdentityBaselineCannotShrinkOrSilentlyChange() throws Exception {
        Set<String> expected = new String(
                new ClassPathResource("seo/utility-id-baseline.txt").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        ).lines().filter(line -> !line.isBlank()).collect(Collectors.toSet());
        Set<String> actual = registry.listAllUtilities().stream()
                .map(utility -> utility.utilityId())
                .collect(Collectors.toSet());
        assertEquals(expected, actual, "Utility ids changed; review redirects and GSC winners before accepting this diff");
        assertEquals(96, actual.size());
    }

    @Test
    void everyCityAliasStillResolvesToAUtility() {
        assertEquals(89, registry.listCityAliases().size());
        registry.listCityAliases().forEach(alias -> assertTrue(
                registry.findUtilityById(alias.utilityId()).isPresent(),
                alias.aliasSlug() + " references missing or unpublished " + alias.utilityId()
        ));
    }
}
