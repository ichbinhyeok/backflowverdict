package owner.backflow.web;

import java.util.Optional;
import org.springframework.web.util.UriComponentsBuilder;

public final class CtaPaths {
    private CtaPaths() {
    }

    public static String trackedPath(
            String destination,
            String pageFamily,
            String utilityId,
            String providerId,
            String ctaType,
            String sourcePath
    ) {
        String normalizedDestination = normalize(destination);
        if (normalizedDestination.isBlank()) {
            return "";
        }
        return UriComponentsBuilder.fromPath("/r/cta")
                .queryParam("next", normalizedDestination)
                .queryParamIfPresent("pageFamily", nonBlank(pageFamily))
                .queryParamIfPresent("utilityId", nonBlank(utilityId))
                .queryParamIfPresent("providerId", nonBlank(providerId))
                .queryParamIfPresent("ctaType", nonBlank(ctaType))
                .queryParamIfPresent("source", nonBlank(sourcePath))
                .build()
                .encode()
                .toUriString();
    }

    private static Optional<String> nonBlank(String value) {
        String normalized = normalize(value);
        return normalized.isBlank() ? Optional.empty() : Optional.of(normalized);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
