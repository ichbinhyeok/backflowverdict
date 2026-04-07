package owner.backflow.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import owner.backflow.config.AppOpsProperties;
import owner.backflow.data.model.MetroRecord;
import owner.backflow.data.model.UtilityRecord;
import owner.backflow.files.BackflowRegistryService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class LeadRoutingService {
    private static volatile String signingSecret = "";

    private final BackflowRegistryService registryService;

    public LeadRoutingService(AppOpsProperties opsProperties, BackflowRegistryService registryService) {
        this.registryService = registryService;
        signingSecret = normalize(opsProperties.verificationToken());
    }

    public static String requestHelpPath(String utilityId, String sourcePage, String issueType, String pageFamily) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/leads/new")
                .queryParamIfPresent("utilityId", nonBlank(normalize(utilityId)))
                .queryParamIfPresent("source", nonBlank(normalize(sourcePage)))
                .queryParamIfPresent("issueType", nonBlank(normalize(issueType)))
                .queryParamIfPresent("pageFamily", nonBlank(normalize(pageFamily)));
        String token = issueToken(utilityId, sourcePage, pageFamily);
        if (!token.isBlank()) {
            builder.queryParam("rt", token);
        }
        return builder.build().encode().toUriString();
    }

    public LeadRoutingContext resolveTrustedContext(
            String submittedUtilityId,
            String submittedSourcePage,
            String submittedPageFamily,
            String routingToken
    ) {
        String normalizedUtilityId = normalize(submittedUtilityId);
        String normalizedSourcePage = normalize(submittedSourcePage);
        String normalizedPageFamily = normalize(submittedPageFamily);
        String normalizedToken = normalize(routingToken);

        if (normalizedUtilityId.isBlank() || normalizedSourcePage.isBlank() || normalizedPageFamily.isBlank()) {
            return hold("HOLD_UNVERIFIED_CONTEXT", "Lead was submitted without a full verified routing context.");
        }
        if (normalizedToken.isBlank()) {
            return hold("HOLD_UNVERIFIED_CONTEXT", "Lead was submitted without a trusted routing token.");
        }

        String expectedToken = issueToken(normalizedUtilityId, normalizedSourcePage, normalizedPageFamily);
        if (expectedToken.isBlank() || !MessageDigest.isEqual(
                expectedToken.getBytes(StandardCharsets.UTF_8),
                normalizedToken.getBytes(StandardCharsets.UTF_8)
        )) {
            return hold("HOLD_UNVERIFIED_CONTEXT", "Lead routing metadata did not pass server-side validation.");
        }

        UtilityRecord utility = resolveUtilityForSource(normalizedUtilityId, normalizedSourcePage);
        if (utility == null) {
            return hold("HOLD_CONTEXT_MISMATCH", "Lead utility did not match the verified source page.");
        }

        return new LeadRoutingContext(
                utility.utilityId(),
                utility.utilityName(),
                normalizedSourcePage,
                normalizedPageFamily,
                "AUTO_ROUTE_ELIGIBLE",
                "Verified utility routing metadata matched a server-issued token."
        );
    }

    public static String issueToken(String utilityId, String sourcePage, String pageFamily) {
        String secret = normalize(signingSecret);
        String normalizedUtilityId = normalize(utilityId);
        String normalizedSourcePage = normalize(sourcePage);
        String normalizedPageFamily = normalize(pageFamily);
        if (secret.isBlank()
                || normalizedUtilityId.isBlank()
                || normalizedSourcePage.isBlank()
                || normalizedPageFamily.isBlank()) {
            return "";
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload(normalizedUtilityId, normalizedSourcePage, normalizedPageFamily)
                    .getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign lead routing token.", exception);
        }
    }

    private UtilityRecord resolveUtilityForSource(String utilityId, String sourcePage) {
        UtilityRecord directUtility = resolveUtilityPage(utilityId, sourcePage);
        if (directUtility != null) {
            return directUtility;
        }

        UtilityRecord metroUtility = resolveMetroPage(utilityId, sourcePage);
        if (metroUtility != null) {
            return metroUtility;
        }

        return resolveStatePage(utilityId, sourcePage);
    }

    private UtilityRecord resolveUtilityPage(String utilityId, String sourcePage) {
        String trimmedSource = trimTrailingSlash(sourcePage);
        String[] segments = trimmedSource.split("/");
        if (segments.length < 4 || !"utilities".equals(segments[1])) {
            return null;
        }
        String state = segments[2];
        String utilitySlug = segments[3];
        return registryService.findPublishedUtility(state, utilitySlug)
                .filter(utility -> utility.utilityId().equalsIgnoreCase(utilityId))
                .orElse(null);
    }

    private UtilityRecord resolveMetroPage(String utilityId, String sourcePage) {
        String trimmedSource = trimTrailingSlash(sourcePage);
        String[] segments = trimmedSource.split("/");
        if (segments.length != 5
                || !"metros".equals(segments[1])
                || !"backflow-testing".equals(segments[4])) {
            return null;
        }
        String state = segments[2];
        String metroSlug = segments[3];
        MetroRecord metro = registryService.findPublishedMetro(state, metroSlug).orElse(null);
        if (metro == null || metro.utilityIds().stream().noneMatch(id -> id.equalsIgnoreCase(utilityId))) {
            return null;
        }
        return registryService.findUtilityById(utilityId).orElse(null);
    }

    private UtilityRecord resolveStatePage(String utilityId, String sourcePage) {
        String trimmedSource = trimTrailingSlash(sourcePage);
        String[] segments = trimmedSource.split("/");
        if (segments.length != 4
                || !"states".equals(segments[1])
                || !"backflow-testing".equals(segments[3])) {
            return null;
        }
        String state = segments[2];
        return registryService.findUtilityById(utilityId)
                .filter(utility -> utility.state().equalsIgnoreCase(state))
                .orElse(null);
    }

    private LeadRoutingContext hold(String status, String reason) {
        return new LeadRoutingContext("", "", "", "", status, reason);
    }

    private static String payload(String utilityId, String sourcePage, String pageFamily) {
        return utilityId + "\n" + sourcePage + "\n" + pageFamily;
    }

    private static String trimTrailingSlash(String value) {
        String normalized = normalize(value);
        if (normalized.endsWith("/") && normalized.length() > 1) {
            return normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static java.util.Optional<String> nonBlank(String value) {
        return value == null || value.isBlank() ? java.util.Optional.empty() : java.util.Optional.of(value);
    }
}
